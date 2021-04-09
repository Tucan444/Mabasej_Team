# Mabasej_Team
We are working on system, that will help tourists in cities to get information about city more easily.

# Hardware
- Raspberry PI (for now tested only on rpi4. Works on rpi zero too, but it will be slow if more devices are connected)
- External/Internal WiFi antena

# Software
- python 3.9.2 compatible server with basic web interface
- Kotlin based mobile app

# Server
To run server you need to install
- hypercorn - "pip install hypercorn"
- fastapi - "pip install fastapi"
- requests - "pip install requests"
- aiofiles - "pip install aiofiles"

then run by command - "hypercorn main:app --bind <IP:port>"
To connect to another rpi you need to edit settings.json with different ID and fill heartbeat table.

This is not finished product
