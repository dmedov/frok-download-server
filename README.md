frok-download-server
====================

1. create configuration file /etc/frok/frok-ds.conf like that:
PHOTO_BASE_PATH = /home/zda/faces
FROK_SERVER = 127.0.0.1:27015
TARGET_PHOTOS_PATH = /home/zda/faces

2. to build use maven (sudo apt-get install maven)
mvn package (create war snapshot in target directory)



