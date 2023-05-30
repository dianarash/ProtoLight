package com.IFNTUOG.DRashkevych;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Device.class},
        version = 3,
        exportSchema = false
        )
public abstract class DeviceDB extends RoomDatabase {
    public abstract DeviceDao userDao();

}
