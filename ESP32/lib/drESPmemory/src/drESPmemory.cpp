#include <cstring>
#include <string>
#include "drESPmemory.h"

DR_LITTLEFS::DR_LITTLEFS()
{}

bool DR_LITTLEFS::initFS()
{
  Serial.println("Mounting FS...");
  if (!LittleFS.begin()) {
    Serial.println("Failed to mount file system");
    return false;
  }
  return true;
}

bool DR_LITTLEFS::loadConfig(String fileName) {
  Serial.println("Load config");
  File configFile = LittleFS.open(fileName, "r");
  if (!configFile) {
    Serial.println("Failed to open config file");
    return false;
  }

  size_t size = configFile.size();
  if (size > 1024) {
    Serial.println("Config file size is too large");
    return false;
  }

  // Allocate a buffer to store contents of the file.
  std::unique_ptr<char[]> buf(new char[size]);

  // We don't use String here because ArduinoJson library requires the input
  // buffer to be mutable. If you don't use ArduinoJson, you may as well
  // use configFile.readString instead.
  configFile.readBytes(buf.get(), size);

  StaticJsonDocument<200> config;
  auto error = deserializeJson(config, buf.get());
  if (error) {
    Serial.println("Failed to parse config file");
    return false;
  }

  const char* _ssid = config["ssid"];
  this->ssid = _ssid;
  const char* _pass = config["pass"];
  this->pass = _pass;
  this->mode = config["mode"];
  this->brightness = config["brightness"];
  this->speed = config["speed"];
  this->ledcount = config["ledcount"];

  configFile.close();
  return true;
}

String DR_LITTLEFS::getSSID(){
  return this->ssid;
}

void DR_LITTLEFS::setSSID(const char* newValue){
  this->ssid = newValue;
}

String DR_LITTLEFS::getPass(){
  return this->pass;
}

void DR_LITTLEFS::setPass(const char* newValue){
  this->pass = newValue;
}

uint8_t DR_LITTLEFS::getMode(){
  return this->mode;
}

void DR_LITTLEFS::setMode(uint8_t newValue){
  this->mode = newValue;
}

uint8_t DR_LITTLEFS::getBrightness(){
  return this->brightness;
}

void DR_LITTLEFS::setBrightness(uint8_t newValue){
  this->brightness = newValue;
}

uint16_t DR_LITTLEFS::getSpeed(){
  return this->speed;
}

void DR_LITTLEFS::setSpeed(uint16_t newValue){
  this->speed = newValue;
}

uint16_t DR_LITTLEFS::getCount(){
  return this->ledcount;
}

void DR_LITTLEFS::setCount(uint16_t newValue){
  this->speed = newValue;
}

int32_t DR_LITTLEFS::getColor(){
  return this->mode;
}

void DR_LITTLEFS::setColor(int32_t newValue){
  this->color = newValue;
}

bool DR_LITTLEFS::saveConfig(String fileName) {
  Serial.println(">>>>>>>>>>>>>>>>>>> Save config <<<<<<<<<<<<<<<<<<<<<<");
  StaticJsonDocument<200> config;
  config["ssid"] = this->ssid.c_str();
  config["pass"] = this->pass.c_str();
  config["ledcount"] = this->ledcount;
  config["mode"] = this->mode;
  config["brightness"] = this->brightness;
  config["speed"] = this->speed;
  config["color"] = this->color;

  File configFile = LittleFS.open(fileName, "w");
  if (!configFile) {
    Serial.println("Failed to open config file for writing");
    return false;
  }

  serializeJson(config, configFile);
  return true;
}

void DR_LITTLEFS::printAll()
{
  Serial.print("From file: \n---ssid: ");
  Serial.println(this->ssid);
  Serial.print("---pass: ");
  Serial.println(this->pass);
  printf("---LED count: %d \n", this->ledcount);
  printf("---mode: %d \n", this->mode);
  printf("---brightness: %d \n", this->brightness);
  printf("---speed: %d \n", this->speed);
}