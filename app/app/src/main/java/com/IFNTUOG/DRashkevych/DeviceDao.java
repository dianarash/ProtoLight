package com.IFNTUOG.DRashkevych;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DeviceDao {

    @Insert
    void insertDevice(Device device);
    //Select All Data
    @Query("SELECT * FROM device")
    List<Device> getAllData();
    //Select record by id
    @Query("SELECT * FROM device WHERE id = :id")
    Device getById(long id);
    //Update record. Return count updated
    @Update
    int update(Device device);
    //Delete record. Return count updated
    @Delete
    int delete(Device device);
}
