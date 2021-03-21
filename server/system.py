import sys
import os
import json
import zipfile
import requests

arguments = sys.argv
arguments.remove(sys.argv[0])

if len(arguments) == 0:
    sys.exit()

command = arguments[0]
if command in ["u", "update"]:
    with open("version.json", "r") as f:  # loading settings
        version = json.load(f)
    url = version["url"]
    server_version = json.loads(requests.get(url).text)
    if "-version" in arguments:
        try:
            version_download = arguments[arguments.index("-version") + 1]
        except IndexError:
            print("Version argument is empty.")
            sys.exit()
        if version_download not in list(server_version.keys()):
            print("Version not found.")
            sys.exit()

    else:
        for ver, data in enumerate(server_version.values()):
            if data["id"] > version["id"]:
                version_download = list(server_version.keys())[ver]

    with open("update.zip", "wb") as save:
        save.write(
            bytes(requests.get(
                f"https://github.com/UntriexTv/test_directory/releases/download/{version_download}/update.zip").content))
    print("Download succefull")
    print("Extracting update")
    if not os.path.isdir("update"):
        os.mkdir("update")
    with zipfile.ZipFile("update.zip", "r") as zip_ref:
        zip_ref.extractall("")
    os.rmdir("update")
    os.remove("update.zip")
    print(f"update to {version_download} was succefull.")

if command == "clean":
    if arguments[1] == "all":
        open("log.txt", "w").close()

        with open("settings.json", "r") as file:
            settings = json.load(file)

        for line in settings["heartbeat_table"]:
            settings["heartbeat_table"][line] = []

        with open("settings.json", "w") as file:
            json.dump(settings, file, indent=2)

    if arguments[1] == "log":
        open("log.txt", "w").close()

    if arguments[1] == "heartbeat_table":
        with open("settings.json", "r") as file:
            settings = json.load(file)

        for line in settings["heartbeat_table"]:
            settings["heartbeat_table"][line] = []

        with open("settings.json", "w") as file:
            json.dump(settings, file, indent=2)
