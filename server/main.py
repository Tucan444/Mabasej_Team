from fastapi import FastAPI, Request
from fastapi.responses import FileResponse
from pydantic import BaseModel
import engine
import requests
import time
import json
import os
import threading
import hashlib

with open("settings.json", "r") as f:  # loading settings
    settings = json.load(f)

with open("filesystem.json", "r") as f:  # loading settings
    filesystem = json.load(f)

IP = settings["IP"]
ID = settings["ID"]
location = settings["location"]

app = FastAPI()  # init of FastAPI
log = engine.Log(settings["log"])  # init of LOG
offline = []

time_to_heartbeat = settings["time_to_heartbeat"]  # Raspberry will be requesting heartbeat every __ seconds
time_to_heartbeat_offline = settings[
    "time_to_heartbeat_offline"]  # Raspberry will be requesting heartbeat every __ seconds from offline rpi

# json variables
heartbeat_table = settings["heartbeat_table"]
sensors = {  # List of "live" data like tempeature, etc.
    "teplota": 24,
    "vlhkosť": 25,
    "počet ľudí": 10,
    "doba čakania": 2
}

heartbeat_table["ID"].append(ID)
heartbeat_table["IP"].append(IP)
heartbeat_table["location"].append(location)
heartbeat_table["file_system"].append(filesystem)
heartbeat_table["last_heartbeat"].append(time_to_heartbeat)


# Todo better "host" ID handeling

class ServerTable(BaseModel):  # table of content for heartbeat request
    ID: list
    IP: list
    location: list
    file_system: list
    last_heartbeat: list


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


@app.get("/sensors")
def get_sensors(request: Request):
    log.message(f"sensor data sent to {request.client.host}:{request.client.port}")
    log.debug(f"sensor data: {sensors}")
    return sensors


@app.get("/files/{IDx}/{file}")
def get_file(IDx: int, file: str):
    server_ip = heartbeat_table["IP"][heartbeat_table["ID"].index(IDx)]
    if IDx == ID:
        return FileResponse(f"files/{file}")
    elif IDx in heartbeat_table["ID"]:
        if os.path.isdir(f"cache/{IDx}"):
            if os.path.isfile(f"cache/{IDx}/{file}"):
                with open(f"cache/{IDx}/{file}", "rb") as compared_file:
                    m = hashlib.md5()
                    for line in compared_file:
                        m.update(line)
                rr = requests.get(f"""http://{server_ip}:8000/compare/{file}""")
                if rr.text.strip('"') != str(m.hexdigest()):
                    log.message(f"{file} on server {server_ip} is changed.")
                else:
                    log.debug(f"returning cached file cache/{IDx}{file}")
                    return FileResponse(f"cache/{IDx}/{file}")
        else:
            os.mkdir(f"cache/{IDx}")
        log.message(f"downloading {file} from {server_ip}")
        r = requests.get(f"http://{server_ip}:8000/files/{IDx}/{file}")
        with open(f"cache/{IDx}/{file}", "wb") as save:
            save.write(bytes(r.content))
        return FileResponse(f"cache/{IDx}/{file}")


@app.post("/update")
def update_sensors():
    pass
    # Todo Make option to upload "live data" manually to rpi


@app.get("/compare/{file}")
def comparision(file: str):
    with open(f"files/{file}", "rb") as compared_file:
        m = hashlib.md5()
        for line in compared_file:
            m.update(line)
    return m.hexdigest()


@app.get("/devices_list")
def get_devices_list():
    return heartbeat_table["file_system"]


def send_heartbeat(ip, id):
    global heartbeat_table
    log.message(f"""sending heartbeat to {ip}({"offline" if id in offline else "online"})""")
    cache_request = requests.post(f"http://{ip}:8000/heartbeat", data=json.dumps(heartbeat_table))
    heartbeat_table = dict(cache_request.json()[0])
    log.debug(json.dumps(cache_request.json(), indent=4))


def mainloop():
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
                        if heartbeat_table["ID"][device_number] in offline:
                            offline.remove(heartbeat_table["ID"][device_number])
                            log.message(f"""{heartbeat_table["IP"][device_number]} gone online""")
                        heartbeat_table["last_heartbeat"][int(device_number)] = int(time_to_heartbeat) + 5
                log.debug(f"""{device_ID} : time to heartbeat : {heartbeat_table["last_heartbeat"][device_number]}""")
                heartbeat_table["last_heartbeat"][device_number] -= 1
        time.sleep(1)


thread_1 = threading.Thread(target=mainloop, daemon=True)
thread_1.start()

# Todo in next release: disconnect offline client after set time
# Todo send to mobile
# Todo new filesystem handeling
# Todo implement update system
# Todo settings for easy adding/editing files/id/text