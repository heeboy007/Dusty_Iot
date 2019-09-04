#include<LiquidCrystal_I2C.h>
#include<SimpleDHT.h>
#include<SoftwareSerial.h>
#include<Wire.h>
/*
 Standalone Sketch to use with a Arduino UNO and a
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
SimpleDHT11 dht11;

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
  Serial.begin(9600);
  lcd.begin();
  lcd.clear();
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

  static byte real_temperature = 0;
  static byte real_humidity = 0;
  int err = SimpleDHTErrSuccess;
  
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

  byte temperature, humidity;
  if ((err = dht11.read(dhtPin, &temperature, &humidity, NULL)) == SimpleDHTErrSuccess)
  {
    real_temperature = temperature;
    real_humidity = humidity;
  }
  
  Serial.print(dustDensity*100); // unit: ug/m3
  Serial.print(",");
  Serial.print(temperature);
  Serial.print(",
 
 ?);
  Serial.println(humidity);
  lcd.clear();
  lcd.print("T:");
  lcd.print((int)real_temperature);
  lcd.print((char)223);
  lcd.print("C");
  lcd.setCursor(8, 0);
  lcd.print("RH:");
  lcd.print((int)real_humidity);
  lcd.print("%");
  lcd.setCursor(0,1);
  lcd.print("PM2.5:");
  lcd.print(dustDensity*100);
  lcd.setCursor(11,1);
  lcd.print("ug/m3");

  setLed(dustDensity*100);

  delay(1000);
}
