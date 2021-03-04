from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()
location = "izba"
ID = 55
IP = "192.168.1.25"
filesystem = {
    "otvaracie_hod": ["t", {"pon": "10-25"}, {"uto": "10-25"}],
    "prehliadka": ["pdf", "/files/prehliadka.pdf"],
    "fotky_hrad": ["png_z", ["/files/hrad1.png", "/files/hrad2.png"]]
}
heartbeat_table = {
    "ID": [1, 2, 3, 4, 5, 6, 7],
    "IP": ["192.168.1.11", "192.168.1.12", "192.168.1.13", "192.168.1.14", "192.168.1.16", "192.168.1.17"],
    "location": ["1", "2", "3", "4", "5", "6", "hrad"],
    "file_system": ["x", "x", "x", "x", "x", "x", "x"],
    "last_heartbeat": [15, 15, 15, 15, 15, 15, 15]
}


class Server_table(BaseModel):
    ID: list
    IP: list
    location: list
    file_system: list
    last_heartbeat: list


@app.post("/heartbeat")
def heartbeat(s_table: Server_table):
    for position, server_id in enumerate(s_table.ID):
        if server_id in heartbeat_table["ID"]:
            if heartbeat_table["last_heartbeat"][heartbeat_table["ID"].index(server_id)] > s_table.last_heartbeat[position]:
                heartbeat_table["last_heartbeat"][heartbeat_table["ID"].index(server_id)] = s_table.last_heartbeat[position]
        else:
            heartbeat_table["ID"].append(s_table.ID[position])
            heartbeat_table["IP"].append(s_table.IP[position])
            heartbeat_table["location"].append(s_table.location[position])
            heartbeat_table["file_system"].append(s_table.file_system[position])
            heartbeat_table["last_heartbeat"].append(s_table.last_heartbeat[position])
    print(heartbeat_table)
    return heartbeat_table, {"ID": ID, "file_system": filesystem, "location": location}