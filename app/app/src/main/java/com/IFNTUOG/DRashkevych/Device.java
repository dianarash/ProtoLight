package com.IFNTUOG.DRashkevych;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "device")
public class Device {

    //Primary key
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "ssid")
    private String ssid;
    @ColumnInfo(name = "pass")
    private String pass;
    @ColumnInfo(name = "led_count")
    private int ledCount;
    @ColumnInfo(name = "speed")
    private int speed;
    @ColumnInfo(name = "brightness")
    private int brightness;
    @ColumnInfo(name = "mode")
    private int mode;
    @ColumnInfo(name = "on")
    private boolean on;
    @ColumnInfo(name = "color")
    private int color;

    @ColumnInfo(name = "language")
    private boolean language;

    // конструктор
    public Device(){
        this.ssid = "lampN000";
        this.pass = "12345678";
        this.ledCount = 10;
        this.speed = 45;
        this.brightness = 12;
        this.mode = 33;
        this.on = true;
        this.color = 65280;
        this.language = true;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getLedCount() {
        return ledCount;
    }

    public void setLedCount(int ledCount) {
        this.ledCount = ledCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isOn() {return on;}

    public void setOn(boolean on) {this.on = on;}

    public boolean isLanguage() {
        return language;
    }

    public void setLanguage(boolean language) {
        this.language = language;
    }

}
