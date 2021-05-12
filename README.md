1. [Mabasej team project](#mabasej_team)
2. [App](#app)
   1. [Installation from apk](#install)
4. [Server](#server)
   1. [Install .img (RPI) server+web+computer_vision](#image-install)
   2. [Install script (DietPi, work in progress) server](#script-install)
   3. [Install manually (work in progress)](#manual-install)
   4. [Server filesystem](#server-filesystem)
   5. [filesystem.json](#filesystem)
   6. [settings.json](#settings)

# Mabasej_Team
We are working on system, that will help tourists in cities to get information about city more easily.
| Part of project | State | Available for easy install | Comments |
| :-------------- | :---- | :------------------------: | :------: |
| Server          | Working | 🟢 | Fully working if config is correct |
| Computer Vision Plugin | Working | 🟢 |                         |
| Web             | Demo Working | 🟠 |                           |
| Android app     | Working | 🟢 |  🟠manual server ip🟠 </br>(wokring automatically if connected to AP on rpi)|


# App
Is programmed in android studio (kotlin)
It is based on simple interface with only few buttons, but lot of informations:
- Home- *Shows contents of the server that the user is connected to*
- Explore- *Shows all servers in list with small description and photo.* *User can acces map locations of server or all informations about server*
- Map- *Map that contains pins from all servers and user can open server info from here.* *Also it is possible to start navigation with google maps from here*
- Chat- *Users connected to same server can chat here*
- Settings- *Simple settings for theme and hidden debug menu*

## Install
Requirements: 
- Android 6 and newer
- [WikiSpot.apk](https://github.com/Tucan444/Mabasej_Team/releases/tag/V1.0)

Steps:

1. Download `wikispot.apk` to your mobile and install it. (you will probablly need to enable installing apps from your browser)
2. After instalation open app and go to settings.
3. Click on the top right corner and debug menu should show up.
4. Change ip to your server ip and chose `CHANGE URL`
5. Chose `Home` menu and you should see your server.

⚠️ Because of android limitations we were not able to do automatic connection, but we are working on workaround ⚠️



# Server
Wikispot is in testing stages, but it is possible to install it using our [.img file](https://github.com/Tucan444/Mabasej_Team/releases/tag/V1.0) based on DietPi or custom script.

| Device                | Server compatible                                                                           |  Instalation  |
| :-------------------- | :------------------------------------------------------------------------------------------ | :-----------: |
| Ubuntu (I7, 16GB ram) | :heavy_check_mark: WORKING (Only server automated setup)                                    | Manual/script |
| RPI 4b (2GB)          | :heavy_check_mark: WORKING                                                                  | .img/script   |
| RPI 400 (4GB)         | :grey_question: Untested. Should work.                                                      | .img/script   |
| RPI 3b+               | :grey_question: Untested. Should work.                                                      | .img/script   |
| RPI zero w            | :white_check_mark: Working with fewer devices (Only server. No AP, Computer vision)         | .img/script   |
| RPI 2                 | :question: Untested.                                                                        | :x:           |
| RPI                   | :question: Untested.                                                                        | :x:           |


## image install

login credentials
> login: dietpi

> password: WikiSpot


requirements:
1. [WikiSpot image file](https://github.com/Tucan444/Mabasej_Team/releases/tag/V1.0)
2. MicroSd card (recommended: >=16GB, :exclamation: ALL DATA STORED ON SD CARD WILL BE FORMATED :exclamation:)
3. [BalenaEtcher](https://www.balena.io/etcher/) (or another sd card flasher) *link:*  https://www.balena.io/etcher/
4. SD card reader


Install:
1. Download all required files (wikispot.img and balenaetcher) and install BalenaEtcher
2. Insert SD card into computer/reader, open BalenaEtcher -> chose Flash from file -> chose downloaded wikispot.img -> Select your sd in *Select target* -> Flash!
3. :exclamation: WINDOWS will show unformated drive. Cancel it. It is because of uncompatible format for windows :exclamation:
4. After flashing open partition *boot* (should apear as USB), find file *dietpi.txt* and open it in text editor.
   - Accept license by changing `AUTO_SETUP_ACCEPT_LICENSE=0` to `AUTO_SETUP_ACCEPT_LICENSE=1`
   - Change name of WikiSpot `AUTO_SETUP_NET_HOSTNAME=WikiSpot-CHANGE_ME` by changing only *CHANGE_ME* or leave *CHANGE_ME* for random number name *WikiSpot-54346
   - You can set static ip address by changing `AUTO_SETUP_NET_USESTATIC=0` to `AUTO_SETUP_NET_USESTATIC=1` And entering your setting into required lines.
   - If you want to use computer vision plugin with rpi camera set `ENABLE_COMPUTER_VISION_PLUGIN=0` to `ENABLE_COMPUTER_VISION_PLUGIN=1` (*recommended only on RPI4)
   - If you want to use RPI as access point to WikiSpot change `#AUTO_SETUP_INSTALL_SOFTWARE_ID=60` to `AUTO_SETUP_INSTALL_SOFTWARE_ID=60`
   - *wifi setup in testing*
5. :grey_exclamation:For advanced users:grey_exclamation: You can now change contens of WikiSpot server in `/boot/WikiSpot`according to an example in server filesystem
6. Eject sd card from computer, insert it in Raspberry Pi and power it on. :bangbang:Raspberry Pi needs to be connected to intenet via Ethernet (*wifi coming soon*) othervise the setup will crash.
7. The setup will take approximately 25-40 min (RPI 4b (2gb) and 70 mb download speed)
8. Done you can start using WikiSpot and edit contents of WikiSpot with our app (*coming soon*)


## Script install
*coming soon*


## Manual install
*coming soon*


## Server filesystem
Tree view of server

```
└── test_directory
    ├── cache                                      # files forwarded from another servers to client
    ├── engine.py                                  # engine for server (log, recovery, update)
    ├── files                                      # content of WikiSpot server
    │   └── test.jpg
    ├── filesystem.json                            # data settings of server (name, description, files)
    ├── main.py                                    # main server file
    ├── plugins                                    # plugins file
    │   └── computer_vision                        # oficial WikiSpot computer vision plugin for RPI 4
    │       ├── MobileNetSSD_deploy.caffemodel
    │       ├── MobileNetSSD_deploy.prototxt
    │       └── com_vision.py
    ├── run.py                                     # start script for server
    ├── settings.json                              # settings (log, debug, connected WikiSpots, cache size,...)
    ├── system.py                                  # update and clean script
    └── version.json                               # version of WikiSpot
```


### filesystem
File: `filesystem.json`

```
{
  "ID": 0,                                                           # ID of WikiSpots, Needs to be different, because network will crash
  "location": "25.997417761947318, -97.15738221291177",              # Location of WikiSpot server copied from google maps
  "description": {
    "title": "WikiSpot demo",                                        # Name of WikiSpot server (swiming pool, school, ...)  
    "description_s": "This is showcase of WikiSpot",                 # Short description showed on web/app in list of servers
    "description_l": "This will show after opening server in app",   # Long description showed after opening the server in web/app
    "photo_s": "test.jpg",                                           # Small image showed on web/app in list of servers
    "photo_b": "test.png"                                            # Big image showed after opening the server in web/app
  },
  "files": [                                                         # files on server in /files that will be mediated to the web/app
    {
      "name": "test",                                                # Name of the file, without spaces. App will change "_" to spaces
      "format": ".jpg",                                              # Format of the file
      "description": "This is test file"                             # Description showed next to the file
    }
  ]
}
```

To manualy add new file to server (on setup or via ssh) add file to `server_directory/files`
and add record for file into `files` list in `filesystem.json`. :exclamation:do not forget "," after last record:exclamation:

```

    {
      "name": "new_file_name",
      "format": ".txt",
      "description": "This is how you add new file"
    }
```


### settings
File: `settings.json`

```
{
  "time_to_heartbeat": 20,                # Time to ping of another online servers in seconds
  "time_to_heartbeat_offline": 25,        # Time to ping of another offline servers in seconds
  "save_table": true,                     # Save connected servers to reconnect after restart
  "time_to_save": 60,                     # Time to save server in seconds
  "max_mess": 20,                         # Maximum messages stored in RAM
  "cache_size_mb": 1000,                  # Maximum size of cache directory in mb
  "clear_cache_on_startup": false,        # Remove contents of cache on startup (slower first downloads)
  "log": {                                # Log settings
    "save_error": true,                   # Save errors into log.txt
    "print_error": true,                  # Print errors into console (if running as service into linux log)
    "save_warning": true,                 # Save warnings into log.txt
    "print_warning": true,                # Print warnings into console (if running as service into linux log)
    "save_message": false,                # Save messages (new server, etc. not messages from  clients) into log.txt
    "print_message": true,                # Print messages into console (if running as service into linux log)
    "enable_debug": false                 # Enable debug into console (if running as service into linux log)
  },
  "heartbeat_table": {                    # Saved servers
    "ID": [],
    "IP": [],
    "location": [],
    "file_system": [],
    "last_heartbeat": []
  }
}
```

If you want to manually add server on first setup or via ssh fill heartbeat table like this.

```
"heartbeat_table": {                    # Saved servers
    "ID": [1],                          # ID of server as integer (number)
    "IP": ["192.168.1.2"],              # IP of server as string
    "location": [""],                   # Empty string as placeholder. location will be downloaded after first connection
    "file_system": [""],                # Empty string as placeholder. filesystem will be downloaded after first connection
    "last_heartbeat": [10]              # After how many seconds will server try to connect for the first time
  }
```

:bangbang:If the server will be offline for long time (heartbeat + offline heartbeat) it will be removed from heartbeat table. If the save function is disabled server will trying to connect after restart:bangbang:



*This is not finished product*
