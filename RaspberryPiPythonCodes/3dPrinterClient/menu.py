import i2c_lcd_driver
from util import PrinterState

class Menu:
    
    def __init__(self):
        self.lcd = I2C_LCD_Driver.lcd(0x3F)
        self.menu_tree = [
            
            ]

    def update(self, inputs):
        if inputs == False:
            self.defaultInfoScreen(10, 10, PrinterState.IDLE)
        #else:   

    def getStateText(self, state):
        if state == PrinterState.INITING:
            return "Initing Printer"
        elif state == PrinterState.STEPPER_DISABLED:
            return "Stepper Disabled"
        elif state == PrinterState.IDLE:
            return "Idle"
        elif state == PrinterState.PRINTING:
            return "Printing..."
        elif state == PrinterState.PREHEAT_NOZZLE:
            return "Preheat : Nozzle"
        elif state == PrinterState.PREHEAT_BED:
            return "Preheat : Bed"
        elif state == PrinterState.PREHEAT_BOTH:
            return "Preheating..."
        
    def defaultInfoScreen(self, nozzle_temp, bed_temp, state):
        self.lcd.lcd_display_string("N:%5dcB:%5dc"%(nozzle_temp, bed_temp), 1, 0)
        self.lcd.lcd_display_string(self.getStateText(state), 2, 0)
