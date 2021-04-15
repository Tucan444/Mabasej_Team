import hashlib
import json
import os
import threading
import time
import engine
import requests
import uuid
import subprocess
from fastapi import FastAPI, Request, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from pydantic import BaseModel

check = engine.Scan()
check.check_to_go()
if check.state_list["error"]:
    for error in check.errors:
        print(error)
    check.fix_version()

with open("settings.json", "r", encoding='utf-8') as f:  # loading settings
    settings = json.load(f)

with open("filesystem.json", "r", encoding='utf-8') as f:  # loading filesystem
    filesystem = json.load(f)

IP = settings["IP"]
ID = settings["ID"]
location = settings["location"]
time_to_save = settings["time_to_save"]

app = FastAPI()  # init of FastAPI

origins = ["*", ]
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
log = engine.Log(settings["log"])  # init of LOG
update = engine.Update()
offline = []
save_time = time.time()

time_to_heartbeat = settings["time_to_heartbeat"]  # Raspberry will be requesting heartbeat every __ seconds
time_to_heartbeat_offline = settings[
    "time_to_heartbeat_offline"]  # Raspberry will be requesting heartbeat every __ seconds from offline rpi

# json variables
heartbeat_table = settings["heartbeat_table"]
sensors = {}

messages = []  # {user: "", timestamp: time.Time(), message: ""}

heartbeat_table["ID"].append(ID)
heartbeat_table["IP"].append(IP)
heartbeat_table["location"].append(location)
heartbeat_table["file_system"].append(filesystem)
heartbeat_table["last_heartbeat"].append(time_to_heartbeat)


class ServerTable(BaseModel):  # table of content for heartbeat request
    ID: list
    IP: list
    location: list
    file_system: list
    last_heartbeat: list


class Sensor(BaseModel):
    name: str
    value: str


@app.get("/")
def read_root():
    return "wikispot"


@app.post("/heartbeat")
def heartbeat(s_table: ServerTable, request: Request):
    log.message(f"server requested heartbeat {request.client.host}:{request.client.port}")
    log.debug(f"Recieved server table: {s_table}")

    try:
        for position, server_id in enumerate(s_table.ID):
            if server_id in heartbeat_table["ID"]:
                if heartbeat_table["last_heartbeat"][heartbeat_table["ID"].index(server_id)] < \
                        s_table.last_heartbeat[position]:
                    heartbeat_table["last_heartbeat"][heartbeat_table["ID"].index(server_id)] = s_table.last_heartbeat[
                        position]
                    log.debug(f"updated {server_id}`s heartbeat to {s_table.last_heartbeat[position]}")
                    heartbeat_table["file_system"][heartbeat_table["ID"].index(server_id)] = s_table.file_system[
                        position]
            elif server_id == ID:
                log.debug(f"Updated my heartbeat from {s_table.last_heartbeat[position]} to {time_to_heartbeat}")
                heartbeat_table["last_heartbeat"][heartbeat_table["ID"].index(ID)] = time_to_heartbeat
            else:
                heartbeat_table["ID"].append(s_table.ID[position])
                heartbeat_table["IP"].append(s_table.IP[position])
                heartbeat_table["location"].append(s_table.location[position])
                heartbeat_table["file_system"].append(s_table.file_system[position])
                heartbeat_table["last_heartbeat"].append(s_table.last_heartbeat[position])
    except Exception as error:
        log.error(f"heartbeat > {error}")

    if heartbeat_table["ID"][heartbeat_table["IP"].index(request.client.host)] in offline:
        offline.remove(heartbeat_table["ID"][heartbeat_table["IP"].index(request.client.host)])
        log.message(f"{request.client.host} gone online")

    return heartbeat_table, {"ID": ID, "file_system": filesystem, "location": location}


@app.get("/{IDx}/sensors")
def get_sensors(IDx: int, request: Request):
    global sensors
    log.message(f"sensor data sent to {request.client.host}:{request.client.port}")
    log.debug(f"sensor data: {sensors}")
    if IDx == ID:
        return sensors
    else:
        try:
            r = requests.get(f"""http://{heartbeat_table["IP"][heartbeat_table["ID"].index(IDx)]}:8000/{IDx}/sensors""")
            return r.json()
        except Exception as error:
            log.error(f"Sensor data download from {IDx} failed.\n ERROR: {error}")
            return f"Sensor data download from {IDx} failed.\n ERROR: {error}"


@app.get("/files/{IDx}/{file}")
def get_file(IDx: int, file: str, request: Request):
    log.debug(f"""{request.client} requested {file} from {"this server" if IDx == ID else f"id {IDx}"}""")
    server_ip = heartbeat_table["IP"][heartbeat_table["ID"].index(IDx)]
    if IDx == ID:
        if os.path.isfile(f"files/{file}"):
            return FileResponse(f"files/{file}")
        else:
            return f"File {file} does not exist."
    if IDx not in heartbeat_table["ID"]:
        log.error(f"{request.client} tried to access id ({IDx}) that does not exist.")
        return f"ERROR: {IDx} does not exist."
    else:
        if os.path.isdir(f"cache/{IDx}"):
            if os.path.isfile(f"cache/{IDx}/{file}"):
                with open(f"cache/{IDx}/{file}", "rb") as compared_file:
                    m = hashlib.md5()
                    for line in compared_file:
                        m.update(line)
                rr = requests.get(f"""http://{server_ip}:8000/compare/{file}""")
                if rr.text.strip('"') != str(m.hexdigest()):
                    log.warning(f"{file} on server {server_ip} is changed.")
                else:
                    log.debug(f"returning cached file cache/{IDx}{file}")
                    return FileResponse(f"cache/{IDx}/{file}")
        else:
            os.mkdir(f"cache/{IDx}")
        r = requests.get(f"http://{server_ip}:8000/files/{IDx}/{file}")
        if "does not exist" in r.text:
            log.error(f"{request.client} tried to access file ({file}) on id {IDx} that does not exist.")
            return f"ERROR: {file} does not exist."
        log.message(f"Downloaded {file} from {server_ip}")
        if ".txt" in file:
            with open(f"cache/{IDx}/{file}", "wb", encoding='utf-8') as save:
                save.write(bytes(r.content))
        else:
            with open(f"cache/{IDx}/{file}", "wb") as save:
                save.write(bytes(r.content))
        return FileResponse(f"cache/{IDx}/{file}")


@app.post("/update_sensor")
def update_sensors(data: Sensor, request: Request):
    global sensors
    if data.name in sensors:
        log.message(f"{request.client.host} updated sensor {data.name} with value {data.value}")
        sensors[data.name] = data.value
    else:
        log.warning(f"{request.client} created new sensor.\n SENSOR: {data}")
        sensors[data.name] = data.value
        return f"Successfuly made"


@app.get("/compare/{file}")
def comparision(file: str):
    try:
        with open(f"files/{file}", "rb") as compared_file:
            m = hashlib.md5()
            for line in compared_file:
                m.update(line)
        return m.hexdigest()
    except FileNotFoundError:
        return f"ERROR {file} does not exist"


@app.get("/devices_list")
def get_devices_list():
    return [{"connected_id": ID}, *heartbeat_table["file_system"]]


@app.get("/admin/get/{command}")
def admin_get(command: str):
    if command == "get_updates":
        return [update.get_version(), update.get_updates()]
    if "update-" in command:
        state = []
        version = command.split("-")[1]
        for rpi in heartbeat_table["IP"]:
            if rpi != IP:
                r = requests.get(f"""http://{rpi}:8000/admin/get/update_one-{version}""")
                if r.text.strip('"').split("\\n")[0] == "SUCCESS":
                    log.message(f"{rpi} was updated to {version}")
                else:
                    log.warning(f"""{rpi} failed to update. Manual update may be needed for proper working of network.
                    Response from server: {r.text}""")
                state.append({rpi: r.text.strip('"').split("\\n")})
        # Todo Remove development comments
        # subprocess.check_output(f"""python3 system.py update -version {version}""")
        log.message(f"All devices in network should be updated to {version}")
        state.append({IP: "updated"})
        return state
    if "update_one-" in command:
        state = subprocess.check_output(["python3", "system.py", "update", "-version", f"""{command.split("-")[1]}"""])
        log.warning(state.decode("utf-8"))
        return state.decode("utf-8")
    if command == "settings":
        return settings
    if command == "filesystem":
        return filesystem


@app.post("/admin/upload_file")
async def create_upload_file(uploaded_file: UploadFile = File(...), patch: str = ""):
    file_location = f"{patch}{uploaded_file.filename}"
    with open(file_location, "wb+") as file_object:
        file_object.write(uploaded_file.file.read())
    return {"info": f"file '{uploaded_file.filename}' saved at '{file_location}'"}


# Todo upload of update file and settings


@app.get("/messages/get")
def get_messages(timestamp):
    for position, message in enumerate(reversed(messages)):
        if message["timestamp"] == timestamp:
            return reversed(messages)[:position]


@app.get("/messages/reqister")
def get_messages():
    return [uuid.uuid4().hex[24:], messages[:9]]


@app.post("/messages/post")
def get_messages(m_sender: str = None, message: str = None):
    if m_sender and message:
        messages.append({"sender": m_sender, "message": message, "timestamp": time.time()})
        return "successful"
    else:
        return "Empty message/sender"


@app.get("/debug")
def debug_esp():
    return "test successful"


def send_heartbeat(ip, id):
    global heartbeat_table
    log.message(f"""sending heartbeat to {ip}({"offline" if id in offline else "online"})""")
    cache_request = requests.post(f"http://{ip}:8000/heartbeat", data=json.dumps(heartbeat_table))
    heartbeat_table = dict(cache_request.json()[0])
    log.debug(json.dumps(cache_request.json(), indent=4))


def mainloop():
    global save_time
    while True:
        for device_number, device_ID in enumerate(heartbeat_table["ID"]):
            if device_ID != ID:
                if heartbeat_table["last_heartbeat"][device_number] < 0:
                    try:
                        send_heartbeat(heartbeat_table["IP"][device_number], heartbeat_table["ID"][device_number])
                    except requests.exceptions.ConnectionError:
                        if heartbeat_table["ID"][device_number] not in offline:
                            log.warning(f"""{heartbeat_table["IP"][device_number]} disconnected/is not available""")
                            offline.append(heartbeat_table["ID"][device_number])
                            heartbeat_table["last_heartbeat"][int(device_number)] = int(time_to_heartbeat_offline)
                        else:
                            offline.remove(heartbeat_table["ID"][device_number])
                            log.message(f"""Removing {device_ID} because of long inactivity.""")
                            del heartbeat_table["ID"][device_number]
                            del heartbeat_table["IP"][device_number]
                            del heartbeat_table["location"][device_number]
                            del heartbeat_table["file_system"][device_number]
                            del heartbeat_table["last_heartbeat"][device_number]
                    else:
                        if heartbeat_table["ID"][device_number] in offline:
                            offline.remove(heartbeat_table["ID"][device_number])
                            log.message(f"""{heartbeat_table["IP"][device_number]} gone online""")
                        heartbeat_table["last_heartbeat"][int(device_number)] = int(time_to_heartbeat) + 5
                try:
                    log.debug(
                        f"""{device_ID} : time to heartbeat : {heartbeat_table["last_heartbeat"][device_number]}""")
                    heartbeat_table["last_heartbeat"][device_number] -= 1
                except IndexError:
                    pass
            if time.time() - time_to_save > save_time and settings["save_table"]:
                save_time = time.time()
                log.message("Saving heartbeat table.")
                log.debug(f"Saving heartbeat table: {heartbeat_table}")
                settings["heartbeat_table"] = heartbeat_table
                with open("settings.json", "w", encoding='utf-8') as file:
                    json.dump(settings, file, indent=2)
        time.sleep(1)


print(f"""Starting WikiSpot V{update.get_version()["version"]}""")
print("GitHub: https://github.com/Tucan444/Mabasej_Team")
thread_1 = threading.Thread(target=mainloop, daemon=True)
thread_1.start()

# Todo settings for easy adding/editing files/id/text
