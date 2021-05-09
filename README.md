# Mabasej_Team
We are working on system, that will help tourists in cities to get information about city more easily.

## Hardware
- Raspberry PI (for now tested only on rpi4. Works on rpi zero too, but it will be slow if more devices are connected)
- External/Internal WiFi antena

## Software
- python 3.9.2 compatible server with basic web interface
- Kotlin based mobile app

## Install
Wikispot is in testing stages, but it is possible to install it using our .img file (link coming soon) based on DietPi or custom script.

| Device                | Server compatible                                                                           |  Instalation  |
| :-------------------- | :------------------------------------------------------------------------------------------ | :-----------: |
| Ubuntu (I7, 16GB ram) | :heavy_check_mark: WORKING (Only server)                                                    | Manual/script |
| RPI 4b (2GB)          | :heavy_check_mark: WORKING                                                                  | .img/script   |
| RPI 400 (4GB)         | :grey_question: Untested. Should work.                                                      | .img/script   |
| RPI 3b+               | :grey_question: Untested. Should work.                                                      | .img/script   |
| RPI zero w            | :white_check_mark: Working with fewer devices (Only server. No AP, Computer vision)         | .img/script   |
| RPI 2                 | :question: Untested.                                                                        | :x:           |
| RPI                   | :question: Untested.                                                                        | :x:           |

### Fresh istall (.img) Only RPI
login credentials
> login: dietpi

> password: WikiSpot2021

requirements:
1. WikiSpot image file (download: *soon*)
2. MicroSd card (recommended: >=16GB, :exclamation: ALL DATA STORED ON SD CARD WILL BE FORMATED :exclamation:)
3. BalenaEtcher (or another sd card flasher) *link:*  https://www.balena.io/etcher/
4. SD card reader

Install:
1. Download all required files (wikispot.img and balenaetcher) and install BalenaEtcher
2. Insert SD card into computer/reader, open BalenaEtcher -> chose Flash from file -> chose downloaded wikispot.img -> Select your sd in *Select target* -> Flash!
3. :exclamation: WINDOWS will show unformated drive. Cancel it. It is because of uncompatible format for windows :exclamation:
4. After flashing open partition *boot* (should apear as USB), find file *dietpi.txt* and open it in text editor.
   - Accept license by changing `AUTO_SETUP_ACCEPT_LICENSE=0` to `AUTO_SETUP_ACCEPT_LICENSE=1`
   - Change name of WikiSpot `AUTO_SETUP_NET_HOSTNAME=WikiSpot-CHANGE_ME` by changing only *CHANGE_ME*
   - You can set static ip address by changing `AUTO_SETUP_NET_USESTATIC=0` to `AUTO_SETUP_NET_USESTATIC=1` And entering your setting into required lines.
   - If you want to use computer vision plugin with rpi camera set `ENABLE_COMPUTER_VISION_PLUGIN=0` to `ENABLE_COMPUTER_VISION_PLUGIN=1` (*recommended only on RPI4)
   - If you want to use RPI as access point to WikiSpot change `#AUTO_SETUP_INSTALL_SOFTWARE_ID=60` to `AUTO_SETUP_INSTALL_SOFTWARE_ID=60`
   - *wifi setup in testing*
5. :grey_exclamation:For advanced users:grey_exclamation: You can now change contens of WikiSpot server in `/boot/WikiSpot`according to an example (*coming soon*)
6. Eject sd card from computer, insert it in Raspberry Pi and power it on. :bangbang:Raspberry Pi needs to be connected to intenet via Ethernet (*wifi coming soon*) othervise the setup will crash.
7. The setup will take approximately 25-40 min (RPI 4b (2gb) and 70 mb download speed)
8. Done you can start using WikiSpot and edit contents of WikiSpot with our app (*coming soon*)



This is not finished product
