#include <Arduino.h>
#include <WS2812FX.h>
#include <WiFi.h>
#include <WiFiClient.h>
#include <WiFiAP.h>
#include <WiFiUdp.h>

#include <drESPmemory.h>

#define LED_PIN 23
#define DEBUG true // print for debugging

uint8_t gpTSonoff = 2;
uint8_t gpTSmode = 22;

uint32_t localPort = 8888;          // Port from which we receive UDP packets
const uint8_t NTP_PACKET_SIZE = 48; // Buffer size for received packets
char packetBuffer[NTP_PACKET_SIZE]; // Packet storage buffer

uint16_t ledCount = 10;

String ssid = "lampN000";
String pass = "12345678";
uint8_t mode_ = 5;
uint8_t brightness = 127;
uint16_t speed = 1000; // Set the animation speed. 10=very fast, 5000=very slow
int32_t color = 0xBB2649;

bool flOn = true;     // flag On/Off status of lamp
uint8_t maxMode = 57; // number of last mode
uint8_t part = 4;     // a part of the strip (the whole strip - 4 part)
uint8_t position = 0; // position of the part on the strip

WS2812FX ws2812fx = WS2812FX(ledCount, LED_PIN, NEO_GRB + NEO_KHZ800); // object for LED stripe
DR_LITTLEFS fileManager;                                               // object for work with LittleFS
WiFiUDP udp;                                                           // object for UDP

void moveSegment();

void setup()
{
  Serial.begin(115200);
  if (DEBUG)
  {
    Serial.println("========================= START setup() ===============================");
    delay(1000);
  }

  pinMode(gpTSonoff, INPUT);
  pinMode(gpTSmode, INPUT);

  // mount file system and read params from config file
  bool flSuccess = fileManager.initFS();
  if (flSuccess)
  {
    bool flConfig = fileManager.loadConfig("/config.json");
    if (flConfig)
    {
      // load config from file
      ssid = fileManager.getSSID();
      pass = fileManager.getPass();
      ledCount = fileManager.getCount();
      mode_ = fileManager.getMode();
      brightness = fileManager.getBrightness();
      speed = fileManager.getSpeed();
      color = fileManager.getColor();
      ws2812fx.setLength(ledCount);
      if (DEBUG)
        fileManager.printAll();
    }
  }

  WiFi.mode(WIFI_OFF); // turn off WIFI
  WiFi.mode(WIFI_AP);  // Put WIFI In Access Point Mode

  const char *_ssid = ssid.c_str(); // change ssid
  const char *_pass = pass.c_str(); // change Password

  WiFi.softAP(_ssid, _pass, 11, 0); // Run the access point with the given ssid and password on channel 11 (do not interfere with others)

  IPAddress myIP = WiFi.softAPIP();
  if (DEBUG)
  {
    Serial.print("AP IP address: ");
    Serial.println(myIP);
    Serial.println("Starting UDP");
  }
  udp.begin(localPort); // initialize UDP

  if (DEBUG)
  {
    Serial.print("SSID: ");
    Serial.println(ssid);
    Serial.print("pass: ");
    Serial.println(pass);
    Serial.print("mode: ");
    Serial.println(mode_);
    Serial.print("brightness: ");
    Serial.println(brightness);
    Serial.print("speed: ");
    Serial.println(speed);
  }

  ws2812fx.init();
  ws2812fx.setBrightness(brightness);
  ws2812fx.setSpeed(speed);
  ws2812fx.setColor(color);
  ws2812fx.setMode(mode_);
  ws2812fx.start();
  // parameters: index, first, last, mode, color, speed, options
  ws2812fx.setSegment(0, 0, ledCount - 1, mode_, color, speed, GAMMA);
}

void loop()
{
  // On/Off by TSonoff
  if (digitalRead(gpTSonoff))
  {
    flOn = !flOn;
    if (flOn)
      ws2812fx.start();
    else
      ws2812fx.stop();
    delay(100);
    while (digitalRead(gpTSonoff))
    {
    }
  }
  // next mode by TSmode
  if (digitalRead(gpTSmode))
  {
    mode_++;
    if (mode_ > maxMode)
      mode_ = 1;
    ws2812fx.setMode(mode_);
    delay(100);
    while (digitalRead(gpTSmode))
    {
    }
  }
  ws2812fx.service();

  int cb = udp.parsePacket();
  if (cb)
  {
    udp.read(packetBuffer, NTP_PACKET_SIZE);
    String request = packetBuffer;
    if (DEBUG)
    {
      Serial.println(packetBuffer);
      Serial.println(request);
    }
    if (request.indexOf("cmd_chck:") != -1)
    {
      if (DEBUG)
        Serial.println(request.substring(9));
      // send a reply, to the IP address and port that sent us the packet we received
      udp.beginPacket(udp.remoteIP(), udp.remotePort());
      udp.write('O');
      udp.endPacket();
    }
    // new SSID
    if (request.indexOf("cmd_ssid:") != -1)
    {
      ssid = request.substring(9);
      if (DEBUG)
        Serial.println(ssid);
      fileManager.setSSID(ssid.c_str());
      fileManager.saveConfig("/config.json");
    }
    // new PASS
    if (request.indexOf("cmd_pass:") != -1)
    {
      pass = request.substring(9);
      if (DEBUG)
        Serial.println(pass);
      fileManager.setPass(pass.c_str());
      fileManager.saveConfig("/config.json");
    }
    // new COUNT
    if (request.indexOf("cmd_ledc:") != -1)
    {
      int value = request.substring(9).toInt();
      if (DEBUG)
        Serial.println(value);
      fileManager.setCount(value);
      fileManager.saveConfig("/config.json");
    }
    // new MODE
    if (request.indexOf("cmd_efct:") != -1)
    {
      mode_ = request.substring(9).toInt();
      if (DEBUG)
        Serial.println(mode_);
      ws2812fx.setMode(mode_);
    }
    // new BRIGHTNESS
    if (request.indexOf("cmd_brgh:") != -1)
    {
      brightness = request.substring(9).toInt();
      if (DEBUG)
        Serial.println(brightness);
      ws2812fx.setBrightness(brightness);
    }
    // new SPEED
    if (request.indexOf("cmd_spd_:") != -1)
    {
      speed = request.substring(9).toInt();
      if (DEBUG)
        Serial.println(speed);
      speed = map(speed, 0, 100, 3000, 100);
      ws2812fx.setSpeed(speed);
    }
    // new COLOR
    if (request.indexOf("cmd_clr_:") != -1)
    {
      color = strtol(request.substring(11).c_str(), NULL, 16);
      if (DEBUG)
        Serial.println(color);
      ws2812fx.setColor(color);
    }
    // ON/OFF
    if (request.indexOf("cmd_onof:") != -1)
    {
      int value = request.substring(9).toInt();
      if (DEBUG)
        Serial.println(value);
      if (value)
      {
        ws2812fx.start();
        flOn = true;
      }
      else
      {
        ws2812fx.stop();
        flOn = false;
      }
    }
    // new part
    if (request.indexOf("cmd_prtn:") != -1)
    {
      part = request.substring(9).toInt();
      if (DEBUG)
        Serial.println(part);
      if (part == 4)
      {
        // parameters: index, first, last, mode, color, speed, options
        ws2812fx.setSegment(0, 0, ledCount - 1, mode_, color, speed, GAMMA);
      }
      else
        moveSegment();
    }
    // new position
    if (request.indexOf("cmd_pos_:") != -1)
    {
      position = request.substring(9).toInt();
      if (DEBUG)
        Serial.println(position);
      if (part < 4)
        moveSegment();
    }
    // save settings
    if (request.indexOf("cmd_save:") != -1)
    {
      if (DEBUG)
        Serial.println("save config");
      fileManager.setMode(mode_);
      fileManager.setBrightness(brightness);
      fileManager.setSpeed(speed);
      fileManager.setColor(color);
      fileManager.saveConfig("/config.json");
    }
    memset(packetBuffer, 0, NTP_PACKET_SIZE); // clear the buffer to receive the next command
  }
}

void moveSegment()
{
  int16_t posFirst = map(position, 0, 100, 0, ledCount - 1) - ledCount / (5 - part) / 2;
  if (posFirst < 0)
  {
    // went beyond the beginning of the strip
    posFirst = 0;
  }
  uint16_t posLast = posFirst + ledCount / (5 - part);
  if (posLast > ledCount - 1)
  {
    // passed the end of the strip
    posLast = ledCount - 1;
    posFirst = posLast - ledCount / (5 - part);
  }
  if (DEBUG)
    Serial.println("---" + String(posFirst) + " --- " + String(posLast));
  ws2812fx.stop();
  // parameters: index, first, last, mode, color, speed, options
  ws2812fx.setSegment(0, posFirst, posLast, mode_, color, speed, GAMMA);
  ws2812fx.start();
}