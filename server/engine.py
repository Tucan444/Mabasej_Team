from datetime import datetime
import json
import requests
import os
import subprocess


class Log:
    def __init__(self, settings=None):
        if settings is None:
            settings = {"save_error": True, "print_error": True, "save_warning": True, "print_warning": True,
                        "save_message": False, "print_message": True, "enable_debug": False}
        self.save_error = settings["save_error"]
        self.save_warning = settings["save_warning"]
        self.save_messages = settings["save_message"]
        self.print_error = settings["print_error"]
        self.print_warning = settings["print_warning"]
        self.print_messages = settings["print_message"]
        self.debug_e = settings["enable_debug"]

    def error(self, error):
        if self.print_error:
            print(f"{datetime.now()} -> ERROR: {error}")
        if self.save_error:
            with open("log.txt", "a", encoding='utf-8') as file:
                file.write(f"\n{datetime.now()} -> ERROR: {error}")

    def warning(self, warning):
        if self.print_warning:
            print(f"{datetime.now()} -> Warning: {warning}")
        if self.save_warning:
            with open("log.txt", "a", encoding='utf-8') as file:
                file.write(f"\n{datetime.now()} -> Warning: {warning}")

    def message(self, message):
        if self.print_messages:
            print(f"{datetime.now()} -> message: {message}")
        if self.save_messages:
            with open("log.txt", "a", encoding='utf-8') as file:
                file.write(f"\n{datetime.now()} -> message: {message}")

    def debug(self, debug):
        if self.debug_e:
            print(f"{datetime.now()} -> DEBUG: {debug}")


class Update:
    def __init__(self):
        with open("version.json", "r", encoding='utf-8') as f:  # loading settings
            version = json.load(f)
        self.url = "https://raw.githubusercontent.com/UntriexTv/test_directory/main/ver.json"
        self.version = version["version"]
        self.id = version["id"]

    def get_updates(self):
        return json.loads(requests.get(self.url).text)

    def get_version(self):
        return {"version": self.version, "id": self.id}


class Scan:
    def __init__(self):
        self.cache_exist = os.path.isdir("cache")
        self.files_exist = os.path.isdir("files")
        if os.path.isfile("update.zip"):
            os.remove("update.zip")
        self.filesystem_exist = os.path.isfile("filesystem.json")
        self.settings_exist = os.path.isfile("settings.json")
        self.version_exist = os.path.isfile("version.json")
        self.errors = []
        self.state_list = {
            "error": [],
            "files": [],        # 0 = does not exist, 1 = cant read, 2 = some values missing
            "filesystem": [],
            "settings": [],
            "version": [],
            "system": []
        }

    def check_to_go(self):
        filesystem = ""
        if self.cache_exist is False:
            os.mkdir("cache")
        if self.filesystem_exist is False:
            self.state_list["error"].append("filesystem")
            self.state_list["filesystem"].append(0)
            self.errors.append("Filesystem is missing")
        else:
            try:
                with open("filesystem.json", "r", encoding='utf-8') as f:
                    filesystem = json.load(f)
            except:
                self.state_list["error"].append("filesystem")
                self.state_list["filesystem"].append(1)
                self.errors.append("Filesystem is corrupted")
            else:
                filesystem_keys = filesystem.keys()
                for check in ["ID", "location", "description", "files"]:
                    if check not in filesystem_keys:
                        self.state_list["error"].append("filesystem")
                        self.state_list["filesystem"].append(2)
        if self.files_exist is False:
            self.state_list["error"].append("files")
            self.state_list["files"].append(0)
            self.errors.append("Files folder does not exists")
        elif filesystem:
            for file in dict(filesystem)["files"]:
                if not os.path.isfile(f"""files/{dict(file)["name"]}{dict(file)["format"]}"""):
                    self.errors.append(f"""{dict(file)["name"]}{dict(file)["format"]} does not exists in file folder.""")
                    if "files" not in self.state_list["error"]:
                        self.state_list["error"].append("files")
                        self.state_list["files"].append(2)
        if self.settings_exist is False:
            self.state_list["error"].append("settings")
            self.state_list["settings"].append(0)
        if self.version_exist is False:
            self.state_list["error"].append("version")
            self.state_list["version"].append(0)


    def fix_version(self):
        o = subprocess.check_output(["python3", "system.py", "update"])
        print(o)

