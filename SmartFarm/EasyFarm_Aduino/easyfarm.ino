#include "DHT.h"  //온습도센서 라이브러리 호출
#include <SoftwareSerial.h>
#include "WiFiEsp.h"
SoftwareSerial BT(8, 7);
SoftwareSerial Serial1(2, 3);
byte buffer[1024];
int bufferPosition;

#define DEVICE "v6CCEA3CAAE1C545"
char ssid[] = "PuniPaw";            // your network SSID (name)
char pass[] = "c6:5d:ba:c2:02:7f";  // your network password
const char accessToken[] = "o.6Nt4ZiJj6R62kGc2J7SFo6dpW5CweIQ7";
int status = WL_IDLE_STATUS;  // the Wifi radio's status
boolean getIsConnected = false;
char server[] = "api.pushingbox.com";

// Initialize the Ethernet client object
WiFiEspClient client;

#define DHTTYPE DHT11  // DHT11 온습도 센서 사용
#define DHT_PIN A0
DHT dht(DHT_PIN, DHTTYPE);  //DHT 설정(A0,DHT11)

#define Moisture_PIN A3
#define Lux_PIN A5
#define LED_PIN 5
#define Motor_PIN 10
int pumpcount = 0;
bool LED_State;
bool set_LED_State;
int preSoil;

void sendNotimessage() {
  Serial.println("Starting connection to server...");
  client.stop();
  // if you get a connection, report back via serial
  if (client.connect(server, 80)) {
    Serial.println("Connected to server");
    // Make a HTTP request
    client.print(F("GET /pushingbox?devid="));
    client.print(DEVICE);
    client.print(F(" HTTP/1.1\r\n"));
    client.print(F("Host: api.pushingbox.com\r\n"));
    client.print(F("User-Agent: Arduino\r\n"));
    client.print(F("\r\n\r\n"));

  } else {
    // if you couldn't make a connection
    Serial.println("Connection failed");
    getIsConnected = false;
  }
}

//데이터 클래스
class Value_Data {
private:
  //사용자가 설정한 데이터값
  int setMoisture;  //습도

public:
  Value_Data() {
  }
  int getHumidity()  // 현재 습도
  {
    float humidity = dht.readHumidity();
    return (int)humidity;
  }

  int getLux()  //현재조도량
  {
    int lux = analogRead(Lux_PIN);
    return lux;
  }

  void setledState(bool state) {  //현재 led 상태 설정
    set_LED_State = state;
  }

  bool getledState() {  //현재 led 상태
    return LED_State;
  }

  float getTemperature()  //현재온도
  {
    float temp = dht.readTemperature();
    return temp;
  }

  int getMoisture() {  //현재토양수분
    int moisture = analogRead(Moisture_PIN);
    moisture = (1000 - moisture) / 10;
    return moisture;
  }

  int get_setMoisture()  //get 설정습도
  {
    return setMoisture;
  }

  void set_Moisture(int a)  //set 설정습도
  {
    setMoisture = a;
  }
};

class Control_Humidity {  //습도 관리 클래스
private:
  Value_Data Data;

public:
  Control_Humidity() {
  }
  Control_Humidity(Value_Data a) {
    Data = a;
  }

public:
  void maintain_hum() {
    Serial.println(F("<<maintain_hum>>"));
    Serial.print(F("now Soil-->"));
    Serial.println(Data.getMoisture());
    Serial.print(F("set Soil-->"));
    Serial.println(Data.get_setMoisture());

    Serial.println();
    preSoil = Data.getMoisture();

    if ((Data.getMoisture() < Data.get_setMoisture())) {  // 현재습도가 설정습도에 도달하지 못했을 때
      WaterPump(); //물공급
      pumpcount++;
      Serial.print("pre Soil: ");
      Serial.println(preSoil);
      delay(300000); //습도 오르는 시간 동안 딜레이 5분 설정
      if(preSoil > Data.getMoisture()){ // 현재 습도가 이전에 측정한 습도보다 낮을 때 -> 물이 부족하다고 판단
        Serial.println("lack of water");
        sendNotimessage();
        delay(600000); //물을 다시 채울 시간 동안 딜레이 10분 설정
      }
    } else {  //설정습도에 도달했을 때
      Serial.println("Humidity is sufficient. no water supply.");
      pumpcount = 0;
      delay(1800000);  // 습도 재측정 까지의 딜레이 30분 설정
    }

    Serial.print(F("now Soil-->"));
    Serial.println(Data.getMoisture());
    Serial.print(F("set Soil-->"));
    Serial.println(Data.get_setMoisture());
    Serial.print(F("Pump Count: "));
    Serial.println(pumpcount);
    Serial.println(F("<<maintain_hum quit>>"));
  }

private:
  void WaterPump()  //급수기능
  {
    Serial.println(F("supply water."));
    digitalWrite(Motor_PIN, HIGH);
    delay(1000); //1초간 급수
    digitalWrite(Motor_PIN, LOW);
  }
};

class Control_Lux  //조도 관리 클래스
{
private:
  Value_Data Data;

public:
  Control_Lux(Value_Data a) {
    Data = a;
  }

public:
  void maintain_lux() {
    Serial.println(F("<<maintain_lux start>>"));
    Serial.print(F("now lux: "));
    Serial.println(Data.getLux());

    /*
    조도 제어 알고리즘
    */
    if (set_LED_State == false) { //LED를 Off 했을 때
      if (Data.getLux() < 300) {      //빛이 부족할 때
        digitalWrite(LED_PIN, HIGH);  //LED ON
        LED_State = true;
      } else {                       //빛이 충분할 때
        digitalWrite(LED_PIN, LOW);  //LED OFF
        LED_State = false;
      }
    }
    else if(set_LED_State == true){ //LED를 On 했을 때
      digitalWrite(LED_PIN, HIGH);
      LED_State = true;
    }

    Serial.println(F("<<maintain_lux quit>>\n\n"));
  }
};

Value_Data Data;
Control_Humidity Hum(Data);
Control_Lux Lux(Data);

void setup() {
  Serial.begin(9600);
  Serial1.begin(9600);
  dht.begin();
  BT.begin(9600);

  bufferPosition = 0;

  WiFi.init(&Serial1);

  check for the presence of the shield
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    // don't continue
    while (true);
  }

  attempt to connect to WiFi network
  while (status != WL_CONNECTED) {
    Serial.print("Attempting to connect to WPA SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network
    status = WiFi.begin(ssid, pass);
  }

  // you're connected now, so print out the data
  Serial.println("You're connected to the network");

  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);
  pinMode(Motor_PIN, OUTPUT);
}

void loop() {

  //블루투스 통해 수신
  while(BT.available()) {
    byte data = BT.read();
    Serial.write(data);
    buffer[bufferPosition++] = data;
    if (data == '\n') {
      buffer[bufferPosition] = '\0';
      if (buffer[0] == 'f' || buffer[0] == 't') {
        if (buffer[0] == 'f') {
          set_LED_State = false;
          digitalWrite(LED_PIN, LOW);
        } else {
          set_LED_State = true;
          digitalWrite(LED_PIN, HIGH);
        }
      } else {
        if (buffer[2] == '\0') {
          Data.set_Moisture(5);
        } else if (buffer[3] == '\0') {
          Data.set_Moisture((buffer[0] - '0') * 10 + buffer[1] - '0');
        } 
        else {
          break;
        }
      }
      bufferPosition = 0;
    }
  }

  Serial.print("now Temp: ");
  Serial.println(Data.getTemperature());
  Serial.print("now Humidity: ");
  Serial.println(Data.getHumidity());

  BT.write(Data.getTemperature());
  BT.write(Data.getHumidity());
  BT.write(LED_State);

  Control_Humidity Hum(Data);
  Control_Lux Lux(Data);
  Hum.maintain_hum();
  Lux.maintain_lux();
  
  delay(1000);
}