from fastapi import FastAPI, Request
from fastapi.responses import FileResponse
from pydantic import BaseModel
import engine
import requests
import time
import json
import os
import threading

app = FastAPI()

sensors = {
    "teplota": 24,
    "vlhkosť": 25,
    "počet ľudí": 10,
    "doba čakania": 2
}
log = engine.Log(print_m=True, debug=False)

time_to_heartbeat = 20  # Seconds
location = "2"
ID = 2
IP = "192.168.1.99"

filesystem = {
    "otvaracie_hod": ["t", {"pon": "10-25"}, {"uto": "10-25"}],
    "prehliadka": ["pdf", "/files/prehliadka.pdf"],
    "fotky_hrad": ["png_z", ["/files/hrad1.png", "/files/hrad2.png"]]
}
heartbeat_table = {
    "ID": [1],
    "IP": ["192.168.1.231"],
    "location": ["1"],
    "file_system": ["x"],
    "last_heartbeat": [20]
}
heartbeat_table["ID"].append(ID)
heartbeat_table["IP"].append(IP)
heartbeat_table["location"].append(location)
heartbeat_table["file_system"].append(filesystem)
heartbeat_table["last_heartbeat"].append(time_to_heartbeat)


# Todo better "host" ID handeling

class Server_table(BaseModel):
    ID: list
    IP: list
    location: list
    file_system: list
    last_heartbeat: list


@app.post("/heartbeat")
def heartbeat(s_table: Server_table, request: Request):
    log.message(f"heartbeat requested: {request.client.host}:{request.client.port}")
    log.debug(f"Recieved server table: {s_table}")
    try:
        for position, server_id in enumerate(s_table.ID):
            if server_id in heartbeat_table["ID"]:
                if heartbeat_table["last_heartbeat"][heartbeat_table["ID"].index(server_id)] < \
                        s_table.last_heartbeat[position]:
                    heartbeat_table["last_heartbeat"][heartbeat_table["ID"].index(server_id)] = s_table.last_heartbeat[
                        position]
                    log.debug(f"updated {server_id}`s heartbeat to {s_table.last_heartbeat[position]}")
                    # Todo update filesystem too. Now updating only last heartbeat
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
    return heartbeat_table, {"ID": ID, "file_system": filesystem, "location": location}


@app.get("/sensors")
def get_sensors(request: Request):
    log.message(f"sensor data sent to {request.client.host}:{request.client.port}")
    log.debug(f"sensor data: {sensors}")
    return sensors


@app.get("/files/{IDx}/{file}")
def get_file(IDx: int, file: str):
    if IDx == ID:
        return FileResponse(f"files/{file}")
    elif IDx in heartbeat_table["ID"]:
        r = requests.get(f"""http://{heartbeat_table["IP"][heartbeat_table["ID"].index(IDx)]}:8000/files/{IDx}/{file}""")
        r.encoding = "utf-8"
        if os.path.isdir(f"cache/{IDx}"):
            if os.path.isfile(f"cache/{IDx}/{file}"):
                pass
                # Todo cache time to live/compare files on server and cache with not resource heavy function
            else:
                with open(f"cache/{IDx}/{file}", "wb") as save:
                    save.write(bytes(r.content))
        else:
            os.mkdir(f"cache/{IDx}")
            with open(f"cache/{IDx}/{file}", "wb") as save:
                save.write(bytes(r.content))
        return FileResponse(f"cache/{IDx}/{file}")
    # Todo Get files function for client (phone/ther rpi)


@app.post("/update")
def update_sensors():
    pass
    # Todo Make option to upload "live data" manually to rpi

def send_heartbeat(ip):
    global heartbeat_table
    log.message(f"requesting heartbeat from {ip}")
    cache_request = requests.post(f"http://{ip}:8000/heartbeat", data=json.dumps(heartbeat_table))
    heartbeat_table = dict(cache_request.json()[0])
    #Todo test heartbeat table update
    log.debug(json.dumps(cache_request.json(), indent=4))


def mainloop():
    while True:
        for device_number, device_ID in enumerate(heartbeat_table["ID"]):
            if device_ID != ID:
                if heartbeat_table["last_heartbeat"][device_number] < 0:
                    send_heartbeat(heartbeat_table["IP"][device_number])
                    heartbeat_table["last_heartbeat"][int(device_number)] = int(time_to_heartbeat) + 5
                log.debug(f"""{device_ID} : time to heartbeat : {heartbeat_table["last_heartbeat"][device_number]}""")
                heartbeat_table["last_heartbeat"][device_number] -= 1
        time.sleep(1)


thread_1 = threading.Thread(target=mainloop, daemon=True)
thread_1.start()
