#! /usr/bin/python
import paho.mqtt.client as mqtt
import time
import serial
import socket
import RPi.GPIO as GPIO

dusts = []
temps = []
humds = []
is_device_active = True
relay_pin = 17
sending_interval = 3
index_count = 0
print("\nInitializing...")

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(relay_pin, GPIO.OUT)
GPIO.output(relay_pin, GPIO.LOW)
print("Setting Off GPIO for power control...")

def on_message(client, userdata, msg):
    print(msg.payload.decode())
    if msg.payload.decode() == "TurnOff":
        GPIO.output(relay_pin, GPIO.HIGH)
        is_device_active = False
    elif msg.payload.decode() == "TurnOn":
        GPIO.output(relay_pin, GPIO.LOW)
        is_device_active = True

mqttc = mqtt.Client(client_id="dusty_server", clean_session=False, protocol=mqtt.MQTTv31)
mqttc.username_pw_set("mosquitto", "k4BXSjCX48KyGYLT")
mqttc.connect("mqtt-dust.local", 1883, 60)
mqttc.subscribe("tester/myhome/req", 2)
mqttc.on_message = on_message
mqttc.message_callback_add("tester/myhome/req", on_message)

print("Connected to Mqtt Server...")

ser = serial.Serial(
    port = '/dev/ttyS0',
    baudrate = 115200,
    parity = serial.PARITY_NONE,
    stopbits = serial.STOPBITS_ONE,
    bytesize = serial.EIGHTBITS,
    timeout = 5)
print("Serial Opened...")
#ser = serial.Serial('/dev/ttyUSB0', 9600);
temp_prev = 0
hum_prev = 0

time_str = time.strftime("%Y-%m-%d-%H-%M-%S", time.gmtime())
print("Getting Time...")

dust_store_file = open("../DataBase/%s-dust.txt"%(time_str), "w")
temp_store_file = open("../DataBase/%s-temp.txt"%(time_str), "w")
hum_store_file = open("../DataBase/%s-hum.txt"%(time_str), "w")
print("Generating Data Files...")

#first
raw_data = ser.readline()
no_parity_raw = raw_data[0:len(raw_data)-2].decode()
data = no_parity_raw.split(',')

#a bit different.
dust_store_file.write("FormatLevel:1")
dust_store_file.write("%s:%s\n"%(time_str,data[0]))
temp_prev = float(data[1])
hum_prev = float(data[2])
temp_store_file.write("FormatLevel:1")
temp_store_file.write("%s:%s\n"%(time_str,data[1]))
hum_store_file.write("FormatLevel:1")
hum_store_file.write("%s:%s\n"%(time_str,data[2]))
print("First Writer Done...")

while True:
    if is_device_active:
        raw_data = ser.readline()
        no_parity_raw = raw_data[0:len(raw_data)-2].decode()
        data = no_parity_raw.split(',')
	time_str = time.strftime("%Y-%m-%d-%H-%M-%S", time.gmtime())
        dust_store_file.write("%s:%s\n"%(time_str, data[0]))
        dust_store_file.flush()
        if temp_prev != float(data[1]):
            temp_store_file.write("%s:%s\n"%(time_str, data[1]))
            temp_prev = float(data[1])
            temp_store_file.flush()
        if hum_prev != float(data[2]):
            hum_store_file.write("%s:%s\n"%(time_str, data[2]))
            hum_prev = float(data[2])
            hum_store_file.flush()
	#print(no_parity_raw)
	dusts.append(float(data[0]))
	temps.append(float(data[1]))
	humds.append(float(data[2]))
	if len(dusts) > 50:
		dusts.pop(0)
	if len(temps) > 50:
		temps.pop(0)
	if len(humds) > 50:
		humds.pop(0)
        post_str = "%.2f,%.2f,%.2f"%(sum(dusts)/len(dusts), sum(temps)/len(temps), sum(humds)/len(humds))
	mqttc.publish("tester/myhome/dht", post_str, qos=0)
    #index_count += 1
    #if index_count > sending_interval:
    #    index_count = 0
    mqttc.loop()
    time.sleep(0.15)

dust_store_file.close()
temp_store_file.close()
hum_store_file.close()
