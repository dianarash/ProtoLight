#pragma once

#include <SPI.h>
#include <ArduinoJson.h>
#include <FS.h>
#include <LittleFS.h>

class DR_LITTLEFS
{
  private:
    String ssid, pass;
    uint8_t mode, brightness;
    uint16_t speed, ledcount;
    int32_t color;
    File configFile;
  public:
    DR_LITTLEFS();
    void printAll();
    bool initFS();
    bool loadConfig(String);
    bool saveConfig(String);
    String getSSID();
    void setSSID(const char*);
    String getPass();
    void setPass(const char*);
    uint8_t getMode();
    void setMode(uint8_t);
    uint8_t getBrightness();
    void setBrightness(uint8_t);
    uint16_t getSpeed();
    void setSpeed(uint16_t);
    uint16_t getCount();
    void setCount(uint16_t);
    int32_t getColor();
    void setColor(int32_t);
};
