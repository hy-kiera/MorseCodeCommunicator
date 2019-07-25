// Bluetooth setting
#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

// Sensor setting
#define touchSensor 15
#define led_red 16
#define led_yellow 17
#define led_green 5
#define led_blue 23
#define led 13
#define speaker 19

String text;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("MorseCode_Translater"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");
  
  pinMode(touchSensor, INPUT);
  pinMode(led_red, OUTPUT);
  pinMode(led_yellow, OUTPUT);
  pinMode(led_green, OUTPUT);
  pinMode(led_blue, OUTPUT);
  pinMode(led, OUTPUT);
  pinMode(speaker, OUTPUT);
}

void loop() {
  int touchValue = digitalRead(touchSensor);

  // Bluetooth read & write
  if (Serial.available()) {
    SerialBT.write(Serial.read());
  }
  if (SerialBT.available()) {
    int a = SerialBT.read();
    Serial.write(a);
    
    if(a == '.'){
      digitalWrite(led, HIGH);
      digitalWrite(speaker, HIGH);
      delay(100);
      digitalWrite(led, LOW);
      digitalWrite(speaker, LOW);
      delay(250);
    }
    else if(a == '-'){
      digitalWrite(led, HIGH);
      digitalWrite(speaker, HIGH);
      delay(500);
      digitalWrite(led, LOW);
      digitalWrite(speaker, LOW);
      delay(250);
    }
    else{
      delay(1000);
    }
  }
  //delay(20);

  // touchSensor value
  if(touchValue == 1){  // means "Dot"
    digitalWrite(led_red, HIGH);
    
    delay(500);
    digitalWrite(led_red, LOW);
    touchValue = digitalRead(touchSensor);
    if(touchValue == 1){  // means "Dash"
      digitalWrite(led_yellow, HIGH);
    
      delay(500);
      digitalWrite(led_yellow, LOW);
      touchValue = digitalRead(touchSensor);
      if(touchValue == 1){  // means "Space"
        digitalWrite(led_green, HIGH);
    
        delay(500);
        digitalWrite(led_green, LOW);
        touchValue = digitalRead(touchSensor);
        if(touchValue == 1){  // means "Send"
          Serial.println();
          Serial.print("Send Message : " + text);
          SerialBT.print(text + " ");
          text = "";
          Serial.println();
          digitalWrite(led_blue, HIGH);
    
          delay(500);
          digitalWrite(led_blue, LOW);
        }
        else{
          text += " ";
          Serial.print(" ");
        }
      }
      else{
        text += "-";
        Serial.print("-");
      }
    }
    else{
      text += ".";
      Serial.print(".");
    }
  }
  else{  // means "No Input"
    digitalWrite(led_red, LOW);
    digitalWrite(led_yellow, LOW);
    digitalWrite(led_green, LOW);
    digitalWrite(led_blue, LOW);
  }
}
