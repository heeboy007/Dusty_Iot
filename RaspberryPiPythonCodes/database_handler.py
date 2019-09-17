#! /usr/bin/python
import RPi.GPIO as GPIO
import time
import paho.mqtt.client as mqtt

is_device_active = True
relay_pin = 17

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(relay_pin, GPIO.OUT)
GPIO.output(relay_pin, GPIO.LOW)
print("Setting off GPIO for power control...")

def on_connect(client, userdata, flags, rc):
    print("Connected...")
    print("Code:%d"%(rc))

def on_message(client, userdata, msg):
    str = msg.payload.decode()
    print(str)
    if str == "TurnOff":
        GPIO.output(relay_pin, GPIO.HIGH)
    elif str == "TurnOn":
        GPIO.output(relay_pin, GPIO.LOW)


mqttc = mqtt.Client(client_id="dusty_client", clean_session=False, protocol=mqtt.MQTTv31)
mqttc.username_pw_set("mosquitto", "k4BXSjCX48KyGYLT")
mqttc.on_connect = on_connect
mqttc.on_message = on_message
mqttc.connect("mqtt-dust.local", 1883, 60)
mqttc.subscribe("tester/myhome/req", 2)
mqttc.message_callback_add("tester/myhome/req", on_message)
mqttc.loop_forever()
