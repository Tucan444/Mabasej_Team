import requests, json, uuid


class Server:
    def __init__(self, server_ip="0.0.0.0"):
        self.device_id = None
        self.server = None
        self.client_id = uuid.uuid4().hex[24:]
        try:
            r = requests.get(f"http://{server_ip}:8000/discovery")
            if r.text.strip('"') == "Success":
                self.server = server_ip
                rr = requests.get(f"http://{server_ip}:8000/devices_list")
                self.device_id = dict(rr.json()[0])["connected_id"]
        except Exception as error:
            print(f"Program experienced a ERROR\nError: {error}")
            self.server = None

    def get_sensors(self, server_id=None):
        if not server_id:
            cache = self.device_id
        else:
            cache = server_id
        if self.server:
            r = requests.get(f"http://{self.server}:8000/{cache}/sensors")
            return dict(r.json())
        else:
            raise Exception("Module was not inicialized/server was not found")

    def update_sensors(self, name, value, ID=None):
        if not name and not value:
            raise Exception("Invalid values were passed")
        r = requests.post(f"http://{self.server}:8000/{ID if ID else self.device_id}/update_sensor")
        return r.text

    def update_server(self, version, update_all=True):
        if update_all:
            r = requests.get(f"http://{self.server}:8000/admin/get/update-{version}")
        else:
            r = requests.get(f"http://{self.server}:8000/admin/get/update_one-{version}")
        return r.text

    def get_versions(self):
        return requests.get(f"http://{self.server}:8000/admin/get/get_updates").text

    def post_message(self, message):
        r = requests.post(f"http://{self.server}:8000/messages/post", json={"m_sender": self.client_id,
                                                                            "message": str(message)})
        return r.text

    def get_messages(self, timestamp=""):
        r = requests.get(f"http://{self.server}:8000/messages/get", data={"timestamp": timestamp})
        return r.text

    def upload_file(self, file_path, save_path):
        with open(file_path, "r") as cache:
            r = requests.get(f"http://{self.server}:8000/messages/get", files={"uploaded_file": cache, "patch": save_path})
        return r.text
