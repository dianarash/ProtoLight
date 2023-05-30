package com.IFNTUOG.DRashkevych;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class DashboardActivity extends AppCompatActivity {
    //Створємо елементи екрану
    Spinner spnrMode;
    ImageButton ibtnOnOff;
    Button btnColor;
    SeekBar skbrBrightness;
    SeekBar skbrScalePartition;
    SeekBar skbrScalePosition;
    CircularSeekBar cskbrSpeed;
    TextView txtvSpeed;
    NavigationView menuNavigation;
    DrawerLayout drwltMenu;
    ColorPickerView clpckColor;
    Dialog dlgSettings;
    Dialog dlgManual;

    //Створюємо об'єкти
    UDP udp;
    Device device;
    List<Device> allDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

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

        btnColor = findViewById(R.id.btnColor);
        ibtnOnOff = findViewById(R.id.ibtnOnOff);
        spnrMode = findViewById(R.id.spnrMode);
        skbrBrightness = findViewById(R.id.skbrBrightness);
        skbrScalePartition = findViewById(R.id.skbrScalePartition);
        skbrScalePosition = findViewById(R.id.skbrScalePosition);
        skbrScalePosition.setEnabled(false);
        skbrScalePosition.setBackground(getResources().getDrawable(R.drawable.custom_seekbar_full));
        skbrScalePosition.getThumb().mutate().setAlpha(0);
        skbrScalePosition.setProgressDrawable(getResources().getDrawable(R.drawable.custom_seekbar_full));
        cskbrSpeed = findViewById(R.id.cskbrSpeed);
        txtvSpeed = findViewById(R.id.txtvSpeed);

        drwltMenu = findViewById(R.id.drwltMenu);
        menuNavigation = findViewById(R.id.menuNavigation);
        clpckColor = findViewById(R.id.colorPickerView);

        skbrBrightness.setProgress(device.getBrightness());
        cskbrSpeed.setProgress(device.getSpeed());
        txtvSpeed.setText(String.format("%s: \n%d", getResources().getString(R.string.speed), (int) (cskbrSpeed.getProgress() / 3.6)));
        btnColor.setBackgroundColor(device.getColor());

        dlgSettings = new Dialog(DashboardActivity.this);
        dlgManual = new Dialog(DashboardActivity.this);

        clpckColor.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
                btnColor.setBackgroundColor(selectedColor);
                device.setColor(selectedColor);
                if(udp.isTestApp()){
                    Toast.makeText(DashboardActivity.this,
                            "selectedColor:" + Integer.toHexString(selectedColor).toUpperCase(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        menuNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_setup:
                        dlgSettings.setContentView(R.layout.settings_dialog_layout);
                        dlgSettings.setCancelable(true);

                        Button btnSave = dlgSettings.findViewById(R.id.btnSave);
                        Switch swtchLangugeSet = dlgSettings.findViewById(R.id.swtchLanguageSet);
                        EditText edtxtSSID, edtxtPass, edtxtLEDcount;
                        edtxtSSID = dlgSettings.findViewById(R.id.edtxtSSID);
                        edtxtPass = dlgSettings.findViewById(R.id.edtxtPass);
                        edtxtLEDcount = dlgSettings.findViewById(R.id.edtxtLEDcount);

                        edtxtSSID.setText(device.getSsid());
                        edtxtPass.setText(device.getPass());
                        swtchLangugeSet.setChecked(device.isLanguage());
                        edtxtLEDcount.setText(Integer.toString(device.getLedCount()));
                        btnSave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String value = String.valueOf(edtxtSSID.getText());
                                device.setSsid(value);
                                sendOrToast(String.format("cmd_ssid:%s", value));
                                value = String.valueOf(edtxtPass.getText());
                                device.setPass(value);
                                sendOrToast(String.format("cmd_pass:%s", value));
                                value = edtxtLEDcount.getText().toString();
                                device.setLedCount(Integer.parseInt(value));
                                sendOrToast(String.format("cmd_ledc:%s", value));
                                device.setLanguage(swtchLangugeSet.isChecked());
                                int count = dbDao.update(device);    //Update record. Return count updated. 0 - message about problem

                                if(swtchLangugeSet.isChecked()){
                                    setLanguage(DashboardActivity.this, "uk");
                                } else {
                                    setLanguage(DashboardActivity.this, "en");
                                }
                                DashboardActivity.this.recreate();

                                dlgSettings.dismiss();
                            }
                        });

                        dlgSettings.show();

                        drwltMenu.close();
                        return true;
                    case R.id.menu_save:
                        device.setColor(clpckColor.getSelectedColor());
                        device.setSpeed((int) cskbrSpeed.getProgress());
                        device.setMode(spnrMode.getSelectedItemPosition());
                        device.setBrightness(skbrBrightness.getProgress());
                        int count = dbDao.update(device);    //Update record. Return count updated. 0 - message about problem
                        sendOrToast("cmd_save:");
                        drwltMenu.close();
                        return true;
                    case R.id.menu_manual:
                        dlgManual.setContentView(R.layout.manual_dialog_layout);
                        dlgManual.setCancelable(true);
                        TextView txtvManual = dlgManual.findViewById(R.id.txtvManual);
                        txtvManual.setText(getResources().getString(R.string.manual));
                        dlgManual.show();
                        drwltMenu.close();
                        return true;
                }
                return false;
            }
        });

        cskbrSpeed.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@Nullable CircularSeekBar circularSeekBar, float v, boolean b) {}

            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {
                int value = (int) (cskbrSpeed.getProgress() / 3.6);
                txtvSpeed.setText(String.format("%s: \n%d", getResources().getString(R.string.speed), value));
                sendOrToast(String.format("cmd_spd_:%d", value));
            }

            @Override
            public void onStartTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {}
        });

        skbrBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = skbrBrightness.getProgress() * 10 + 5;
                sendOrToast(String.format("cmd_brgh:%d", value));
            }
        });

        skbrScalePartition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = skbrScalePartition.getProgress();
                sendOrToast(String.format("cmd_prtn:%d", value));
                sendOrToast(String.format("cmd_pos_:%d", skbrScalePosition.getProgress()));
                if(value < skbrScalePartition.getMax()){
                    switch (value){
                        case 3:
                            skbrScalePosition.setThumb(getResources().getDrawable(R.drawable.custom_seekbar_thump4));
                            break;
                        case 2:
                            skbrScalePosition.setThumb(getResources().getDrawable(R.drawable.custom_seekbar_thump3));
                            break;
                        case 1:
                            skbrScalePosition.setThumb(getResources().getDrawable(R.drawable.custom_seekbar_thump2));
                            break;
                        case 0:
                            skbrScalePosition.setThumb(getResources().getDrawable(R.drawable.custom_seekbar_thump1));
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + skbrScalePartition.getProgress());
                    }
                    skbrScalePosition.setEnabled(true);
                    skbrScalePosition.setBackground(getResources().getDrawable(R.drawable.custom_seekbar));
                    skbrScalePosition.setProgressDrawable(getResources().getDrawable(R.drawable.custom_seekbar));
                    skbrScalePosition.getThumb().mutate().setAlpha(255);
                }else{
                    skbrScalePosition.setEnabled(false);
                    skbrScalePosition.setBackground(getResources().getDrawable(R.drawable.custom_seekbar_full));
                    skbrScalePosition.setProgressDrawable(getResources().getDrawable(R.drawable.custom_seekbar_full));
                    skbrScalePosition.getThumb().mutate().setAlpha(0);
                }
            }
        });

        skbrScalePosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendOrToast(String.format("cmd_pos_:%d", skbrScalePosition.getProgress()));
            }
        });

        // Налаштовуємо адаптер для збереження списку ефектів
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.modes,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Викликаємо адаптер
        spnrMode.setAdapter(adapter);
        spnrMode.setSelection(device.getMode());
        spnrMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sendOrToast(String.format("cmd_efct:%d", position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void onClickOnOff(View view){
        int value = 0;
        if(device.isOn()){
            ibtnOnOff.setImageResource(R.drawable.ic_baseline_lightbulb_off_24);
            //змінюємо стан лампи
            device.setOn(false);
            // робимо недоступними віджети керування лампою
            skbrBrightness.setEnabled(false);
            spnrMode.setEnabled(false);
            cskbrSpeed.setEnabled(false);
            cskbrSpeed.setAlpha(.5f);
            skbrScalePartition.setEnabled(false);
            skbrScalePosition.setVisibility(View.INVISIBLE);
            if(btnColor.getText().equals(getResources().getString(R.string.btn_color))){
                txtvSpeed.setVisibility(View.INVISIBLE);
            } else {
                clpckColor.setVisibility(View.INVISIBLE);
            }
            btnColor.setEnabled(false);
            btnColor.setAlpha(.5f);
        } else {
            value = 1;
            ibtnOnOff.setImageResource(R.drawable.ic_baseline_lightbulb_on_24);
            //змінюємо стан лампи
            device.setOn(true);
            // активуємо віджети керування лампою
            skbrBrightness.setEnabled(true);
            spnrMode.setEnabled(true);
            cskbrSpeed.setEnabled(true);
            cskbrSpeed.setAlpha(1f);
            skbrScalePartition.setEnabled(true);
            skbrScalePosition.setVisibility(View.VISIBLE);
            if(btnColor.getText().equals(getResources().getString(R.string.btn_color))){
                txtvSpeed.setVisibility(View.VISIBLE);
            } else {
                clpckColor.setVisibility(View.VISIBLE);
            }
            btnColor.setEnabled(true);
            btnColor.setAlpha(1f);
        }
        sendOrToast(String.format("cmd_onof:%d", value));
    }

    public void onClickColor(View view){
        if(btnColor.getText().equals(getResources().getString(R.string.btn_color))){
            btnColor.setText(getResources().getString(R.string.btn_color2));
            clpckColor.setVisibility(View.VISIBLE);
            cskbrSpeed.setVisibility(View.INVISIBLE);
            txtvSpeed.setVisibility(View.INVISIBLE);
        } else {
            btnColor.setText(getResources().getString(R.string.btn_color));
            clpckColor.setVisibility(View.INVISIBLE);
            cskbrSpeed.setVisibility(View.VISIBLE);
            txtvSpeed.setVisibility(View.VISIBLE);
            sendOrToast(String.format("cmd_clr_:%s", Integer.toHexString(device.getColor()).toUpperCase()));
        }
    }

    public void onClickMenu(View view){
        drwltMenu.open();
    }

    public void sendOrToast(String command){
        if (!udp.isTestApp()) {
            try {
                udp.sender(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), command, Toast.LENGTH_SHORT).show();
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