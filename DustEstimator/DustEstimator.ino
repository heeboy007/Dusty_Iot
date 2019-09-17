#include<LiquidCrystal_I2C.h>
#include"DHT.h"
#include<Wire.h>

/*
 Standalone Sketch to use with a Arduino Nano and a
 Sharp Optical Dust Sensor GP2Y1010AU0F
*/

int measurePin = A0; //Connect dust sensor to Arduino A0 pin
int ledPower = 12;   //Connect 3 led driver pins of dust sensor to Arduino D2
int dhtPin = 2;
int motorPin = 11;
int bluePin = 8;
int greenPin = 7;
int yellowPin = 6;
int redPin = 5;

int samplingTime = 280;
int deltaTime = 40;
int sleepTime = 9680;
int motorSpeed = 150;

float voMeasured = 0;
float calcVoltage = 0;
float dustDensity = 0;

LiquidCrystal_I2C lcd(0x3F, 16, 2);
DHT dht(dhtPin, DHT22);

void setLed(int value){

  if(value >= 75){
    digitalWrite(bluePin, LOW);
    digitalWrite(greenPin, LOW);
    digitalWrite(yellowPin, LOW);
    digitalWrite(redPin, HIGH);
  }
  else if(value >= 35){
    digitalWrite(bluePin, LOW);
    digitalWrite(greenPin, LOW);
    digitalWrite(yellowPin, HIGH);
    digitalWrite(redPin, LOW);
  }
  else if(value >= 15){
    digitalWrite(bluePin, LOW);
    digitalWrite(greenPin, HIGH);
    digitalWrite(yellowPin, LOW);
    digitalWrite(redPin, LOW);
  }
  else{
    digitalWrite(bluePin, HIGH);
    digitalWrite(greenPin, LOW);
    digitalWrite(yellowPin, LOW);
    digitalWrite(redPin, LOW);
  }
    
}

void setup(){
  Serial.begin(115200);
  lcd.begin();
  lcd.clear();
  dht.begin();
  pinMode(motorPin, OUTPUT);
  pinMode(bluePin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(yellowPin, OUTPUT);
  pinMode(redPin, OUTPUT);
  pinMode(ledPower,OUTPUT);
  analogWrite(motorPin, motorSpeed);
  digitalWrite(bluePin, HIGH);
  digitalWrite(greenPin, HIGH);
  digitalWrite(yellowPin, HIGH);
  digitalWrite(redPin, HIGH);
}

void loop(){

  static float real_temperature = 0.0f;
  static float real_humidity = 0.0f;
  static byte internal_clock = 0;
  
  digitalWrite(ledPower,LOW); // power on the LED
  delayMicroseconds(samplingTime);

  voMeasured = analogRead(measurePin); // read the dust value

  delayMicroseconds(deltaTime);
  digitalWrite(ledPower,HIGH); // turn the LED off
  delayMicroseconds(sleepTime);

  // 0 - 5V mapped to 0 - 1023 integer values
  // recover voltage
  calcVoltage = voMeasured * (5.0 / 1024.0);

  // linear eqaution taken from http://www.howmuchsnow.com/arduino/airquality/
  // Chris Nafis (c) 2012
  dustDensity = 0.17 * calcVoltage - 0.1;
  dustDensity *= 100;
  if(dustDensity < 0.0f){
    dustDensity = 0.0f;
  }

  if(!(internal_clock % 2)){
    float temperature = dht.readTemperature(), humidity = dht.readHumidity();
    if (!(isnan(temperature) || isnan(humidity)))
    {
      real_temperature = temperature;
      real_humidity = humidity;
    }
  }
  
  Serial.print(dustDensity); // unit: ug/m3
  Serial.print(",");
  Serial.print(real_temperature);
  Serial.print(",");
  Serial.println(real_humidity);
  
  if(internal_clock == 0){
    lcd.clear();
    lcd.print("Temp:");
    lcd.print(real_temperature);
    lcd.print((char)223);
    lcd.print("C");
    lcd.setCursor(0,1);
    lcd.print("Humi:");
    lcd.print(real_humidity);
    lcd.print("%");
  }
  else if(internal_clock == 4) {
    lcd.clear();
    lcd.print("PM2.5:");
    lcd.print(dustDensity);
    lcd.print("ug/m3");
    lcd.setCursor(0,1);
    lcd.print("Stat:");
    if(dustDensity >= 75){
      lcd.print("Terrible");
    }
    else if(dustDensity >= 35){
      lcd.print("Bad");
    }
    else if(dustDensity >= 15){
      lcd.print("Normal");
    }
    else{
      lcd.print("Good");
    }
  }
  internal_clock++;
  if(internal_clock > 8)
    internal_clock = 0;
  setLed(dustDensity);

  delay(500);
}
