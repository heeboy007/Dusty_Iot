import RPi.GPIO as GPIO
import time

GPIO.cleanup()
GPIO.setmode(GPIO.BCM)

for i in range(2, 27):
    GPIO.setup(i, GPIO.OUT, initial=0)
    print("Pin : %d"%i)
    input()
    GPIO.output(i, GPIO.HIGH)
    time.sleep(1)
    GPIO.output(i, GPIO.LOW)
    time.sleep(1)
    GPIO.output(i, GPIO.HIGH)
    time.sleep(1)
    GPIO.output(i, GPIO.LOW)

GPIO.cleanup()
