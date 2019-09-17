import enum

class PrinterState(enum.Enum):

    INITING = 0
    STEPPER_DISABLED = 1
    IDLE = 2
    PRINTING = 3
    PREHEAT_NOZZLE = 5
    PREHEAT_BED = 6
    PREHEAT_BOTH = 7
