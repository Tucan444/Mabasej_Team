# Mabasej_Team
We are working on system, that will help tourists in cities to get information about city more easily.

# Hardware
- work in progress (but probabbly we will use rpi with external antena for wifi)

# Software
- python 3.9.2 compatible server with basic web interface
- python 3.x based mobile app with help of android studio

# Server
To run server you need to install
- hypercorn - "pip install hypercorn"
- fastapi - "pip install fastapi"
- requests - "pip install requests"

then run by command - "hypercorn main:app --bind <IP:port>"
To connect to another rpi you need to edit settings.json with different ID and fill heartbeat table.

This is not finished product
