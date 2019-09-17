import RPi.GPIO as GPIO
import time

class Stepper:

    def __init__(self, in1, in2, in3, in4):
        self.stepper_in_1 = in1
        self.stepper_in_2 = in2
        self.stepper_in_3 = in3
        self.stepper_in_4 = in4
        self.step_number = 0
        GPIO.setup(self.stepper_in_1, GPIO.OUT, initial=0)
        GPIO.setup(self.stepper_in_2, GPIO.OUT, initial=0)
        GPIO.setup(self.stepper_in_3, GPIO.OUT, initial=0)
        GPIO.setup(self.stepper_in_4, GPIO.OUT, initial=0)

        self.seq = [
            [1, 0, 0, 0],
            [1, 1, 0, 0],
            [0, 1, 0, 0],
            [0, 1, 1, 0],
            [0, 0, 1, 0],
            [0, 0, 1, 1],
            [0, 0, 0, 1],
            [1, 0, 0, 0]
            ]
        
    def oneStep(self, direction):
        if direction == True:
            GPIO.output(self.stepper_in_4, GPIO.HIGH if self.seq[self.step_number][0] else GPIO.LOW)
            GPIO.output(self.stepper_in_3, GPIO.HIGH if self.seq[self.step_number][1] else GPIO.LOW)
            GPIO.output(self.stepper_in_2, GPIO.HIGH if self.seq[self.step_number][2] else GPIO.LOW)
            GPIO.output(self.stepper_in_1, GPIO.HIGH if self.seq[self.step_number][3] else GPIO.LOW)
            self.step_number += 1
            if self.step_number > 7:
                self.step_number = 0
        else:
            GPIO.output(self.stepper_in_4, GPIO.HIGH if self.seq[7-self.step_number][0] else GPIO.LOW)
            GPIO.output(self.stepper_in_3, GPIO.HIGH if self.seq[7-self.step_number][1] else GPIO.LOW)
            GPIO.output(self.stepper_in_2, GPIO.HIGH if self.seq[7-self.step_number][2] else GPIO.LOW)
            GPIO.output(self.stepper_in_1, GPIO.HIGH if self.seq[7-self.step_number][3] else GPIO.LOW)
            self.step_number -= 1
            if self.step_number < 0:
                self.step_number = 7        

    def moveStep(self, steps, direction=True):
        for i in range(steps+1):
            self.oneStep(direction)
            time.sleep(0.002)
        self.resetPins()

    def resetPins(self):
        GPIO.output(self.stepper_in_4, GPIO.LOW)
        GPIO.output(self.stepper_in_3, GPIO.LOW)
        GPIO.output(self.stepper_in_2, GPIO.LOW)
        GPIO.output(self.stepper_in_1, GPIO.LOW)
