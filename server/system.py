import sys
import os
import json
import zipfile
import requests

arguments = sys.argv
arguments.remove(sys.argv[0])
url = "https://raw.githubusercontent.com/UntriexTv/test_directory/main/ver.json"

if len(arguments) == 0:
    sys.exit()

command = arguments[0]
if command in ["u", "update"]:
    try:
        server_version = json.loads(requests.get(url).text)
    except Exception as error:
        print(f"CAN'T DOWNLOAD VERSION LIST. ERROR: {error}")
        sys.exit()
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
        try:
            with open("version.json", "r", encoding='utf-8') as f:  # loading settings
                version = json.load(f)
        except:
            version = {"id": 0, "version": "recovery"}
        for ver, data in enumerate(server_version.values()):
            if data["id"] > version["id"]:
                version_download = list(server_version.keys())[ver]
    try:
        with open("update.zip", "wb", encoding='utf-8') as save:
            save.write(
                bytes(requests.get(
                    f"https://github.com/UntriexTv/test_directory/releases/download/{version_download}/update.zip").content))
    except Exception as error:
        print(f"FAILED TO DOWNLOAD UPDATE. ERROR: {error}")
        sys.exit()
    with zipfile.ZipFile("update.zip", "r") as zip_ref:
        zip_ref.extractall("")
    os.remove("update.zip")
    print("SUCCESS")
    print(f"""Update from version {version["version"]} to {version_download} was sucesfull""")

if command == "clean":
    if arguments[1] == "all":
        open("log.txt", "w").close()

        with open("settings.json", "r", encoding='utf-8') as file:
            settings = json.load(file)

        for line in settings["heartbeat_table"]:
            settings["heartbeat_table"][line] = []

        with open("settings.json", "w", encoding='utf-8') as file:
            json.dump(settings, file, indent=2)

    if arguments[1] == "log":
        open("log.txt", "w").close()

    if arguments[1] == "heartbeat_table":
        with open("settings.json", "r", encoding='utf-8') as file:
            settings = json.load(file)

        for line in settings["heartbeat_table"]:
            settings["heartbeat_table"][line] = []

        with open("settings.json", "w", encoding='utf-8') as file:
            json.dump(settings, file, indent=2)


