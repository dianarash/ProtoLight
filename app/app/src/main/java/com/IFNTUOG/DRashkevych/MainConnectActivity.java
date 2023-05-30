package com.IFNTUOG.DRashkevych;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainConnectActivity extends AppCompatActivity {
    Button btnConnect;
    UDP udp;
    Device device;
    List<Device> allDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_connect);

        udp = new UDP(getString(R.string.server_ip), 8888);

        // створюємо об'єкт - база даних
        DeviceDB db = Room.databaseBuilder(getApplicationContext(),
                DeviceDB.class, "devices").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        // створюємо інтерфейс для роботи з базою даних
        DeviceDao dbDao = db.userDao();
        //зчитуємо всі записи БД
        allDevices = dbDao.getAllData();
        // створюємо об'єкт - девайс для роботи із параметрами
        device = new Device();
        if(allDevices.size() == 0){
            //якщо база даних порожня записуємо в неї пристрій з параметрами за замовчуванням
            dbDao.insertDevice(device);
            allDevices = dbDao.getAllData();
        }
        device = allDevices.get(0);

        // встановлюємо мову згідно конфігурації
        String configLocale = String.valueOf(getResources().getConfiguration().locale);
        if(device.isLanguage()){
            if(configLocale.equals("en")){
                setLanguage(MainConnectActivity.this, "uk");
                MainConnectActivity.this.recreate();
            }
        } else {
            if(!configLocale.equals("en")){
                setLanguage(MainConnectActivity.this, "en");
                MainConnectActivity.this.recreate();
            }
        }
        btnConnect = findViewById(R.id.btnConnect);
    }

    //listener for button ConnectToLamp
    public void onClickConnect(View view) throws IOException {
        // Перевіряємо зв'язок із пристроєм
        if( udp.checkConnect("cmd_chck:")){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.con_succ),
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.con_unsucc),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void setLanguage(Activity activity, String languageCode){
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
    }
}