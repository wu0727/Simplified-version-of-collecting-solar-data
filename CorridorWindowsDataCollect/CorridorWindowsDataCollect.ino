#include <SoftwareSerial.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <Wire.h>
#include <BH1750.h>
#include <EEPROM.h>
BH1750 lightMeter;
#define ONE_WIRE_BUS 2

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);
SoftwareSerial mySerial(10,11); // RX, TXx
#define VT_PIN A0 
#define AT_PIN A1
String ssid = "LoTS_AP";
String pwd = "LoTS708AP";
String ans;
int vt_read;
void setup(){
  Serial.begin(9600);
  // Initialize the I2C bus (BH1750 library doesn't do this automatically)
  Wire.begin();
  // On esp8266 you can select SCL and SDA pins using Wire.begin(D4, D3);
  lightMeter.begin();
  sensors.begin();
  Serial.println(F("BH1750 Test begin"));
  mySerial.begin(115200);
  mySerial.write("AT+UART_DEF=9600,8,1,0,0\r\n"); 
  mySerial.begin(9600);
  mySerial.println("AT+CWMODE=1\r\n");//工作模式
  mySerial.flush();
  mySerial.println("AT+CWJAP=\"" + ssid + "\",\"" + pwd + "\"\r\n");
}
void loop() {
  mySerial.println("AT+CIPSTART=\"TCP\",\"192.168.50.17\",2020");//依據ipconfig的IP位址 去更改
//溫度、光照度感測
  sensors.requestTemperatures(); // 要求匯流排上的所有感測器進行溫度轉換（不過我只有一個）
  float tem = sensors.getTempCByIndex(0);
  float lux = lightMeter.readLightLevel();
  Serial.print("Light: ");
  Serial.print(lux);
  Serial.println(" lx");
  Serial.print(tem);
  Serial.println("攝氏");
//電壓電流感測
  vt_read = analogRead(VT_PIN);
  int at_read = analogRead(AT_PIN);
  float voltage = vt_read * (5.0 / 1024.0) * 5.0;
  float current = at_read * (5.0 / 1024.0);
  if(lux < 100){
    current = 0.00;
    }
//資料傳遞
  //陣列資料為(1)窗邊位置(走廊)、(2)太陽能電壓、(3)太陽能電流、(4)溫度、(5)光照度
  ans ="1:"+String(voltage)+":"+String(current)+":"+String(tem)+":"+String(lux);  
  mySerial.println("AT+CIPSEND="+String(ans.length()+2));  //傳送信息
  mySerial.flush();  //等待序列埠傳送完畢
  delay(1000);
  mySerial.println(ans);
  mySerial.flush();  //等待序列埠傳送完畢
  delay(599000);//10分鐘


}
