#!/bin/bash

sudo service mosquitto stop
sudo service mosquitto start

sudo python serial_collector.py
#sudo python database_handler.py &
