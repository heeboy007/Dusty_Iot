import RPi.GPIO as GPIO
from stepper_driver import Stepper
import time

class Steppers_Ctrl:

    def __init__(self):
        self.STEP_PER_MILIMITER_X = 100
        self.STEP_PER_MILIMITER_Y = 100
        self.STEP_PER_MILIMITER_Z = 100
        self.STEP_PER_MILIMITER_E = 100
        self.x = 0
        self.y = 0
        self.z = 0
        self.e = 0
        self.power_relay_pin = 4
        self.step_x = Stepper(16, 20, 19, 26)
        self.step_y = Stepper(12, 5, 13, 6)
        self.step_z = Stepper(7, 8, 25, 24)
        self.step_e = Stepper(11, 9, 10, 22)
        #self.lcd = I2C_LCD_Driver.lcd(0x3F)
        GPIO.setup(self.power_relay_pin, GPIO.OUT, initial=GPIO.HIGH)
        #self.lcd.lcd_clear()
        #self.lcd.lcd_display_string("X:    mm", 1, 0)
        #self.lcd.lcd_display_string("Y:    mm", 1, 8)
        #self.lcd.lcd_display_string("Z:    mm", 2, 0)
        #self.lcd.lcd_display_string("E:    mm", 2, 8)
        
    """def display_cordinates(self, update):
        if update == 'x':
            self.lcd.lcd_display_string("%4d"%(self.x), 1, 2)
        elif update == 'y':
            self.lcd.lcd_display_string("%4d"%(self.y), 1, 10)            
        elif update == 'z':
            self.lcd.lcd_display_string("%4d"%(self.z), 2, 2)
        elif update == 'e':
            self.lcd.lcd_display_string("%4d"%(self.e), 2, 10)"""

    def enableSteppers(self):
        GPIO.output(self.power_relay_pin, GPIO.LOW)

    def disableSteppers(self):
        GPIO.output(self.power_relay_pin, GPIO.HIGH)

    def applyStep_X(self, steps):
        for i in range(abs(steps)):
            if steps > 0:
                self.step_x.moveStep(self.STEP_PER_MILIMITER_X)
                self.x += 1
            else:
                self.step_x.moveStep(self.STEP_PER_MILIMITER_X, False)
                self.x -= 1
            

    def applyStep_Y(self, steps):
        for i in range(abs(steps)):
            if steps > 0:
                self.step_y.moveStep(self.STEP_PER_MILIMITER_Y)
                self.y += 1
            else:
                self.step_y.moveStep(self.STEP_PER_MILIMITER_Y, False)
                self.y -= 1

    def applyStep_Z(self, steps):
        for i in range(abs(steps)):
            if steps > 0:
                self.step_z.moveStep(self.STEP_PER_MILIMITER_Z)
                self.z += 1
            else:
                self.step_z.moveStep(self.STEP_PER_MILIMITER_Z, False)
                self.z -= 1

    def applyStep_E(self, steps):
        for i in range(abs(steps)):
            if steps > 0:
                self.step_e.moveStep(self.STEP_PER_MILIMITER_E)
                self.e += 1
            else:
                self.step_e.moveStep(self.STEP_PER_MILIMITER_E, False)
                self.e -= 1
            
            
"""GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)

steps_ctrl = Steppers_Ctrl()

steps_ctrl.enableSteppers()
steps_ctrl.applyStep_X(24)
steps_ctrl.applyStep_Y(24)
steps_ctrl.applyStep_Z(24)
steps_ctrl.applyStep_E(24)
steps_ctrl.disableSteppers()
    
GPIO.cleanup()"""
