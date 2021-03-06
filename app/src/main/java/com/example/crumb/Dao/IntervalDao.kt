package com.example.crumb.Dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.crumb.Models.Interval

@Dao
interface IntervalDao {

    @Query("SELECT * FROM interval_table WHERE parent_id = :id ORDER BY time ASC ")
    fun getAllIntervalsInSchedule(id: Long): LiveData<List<Interval>>

    @Query("SELECT * FROM interval_table WHERE parent_id = :id ORDER BY time ASC ")
    suspend fun getAsList(id: Long): List<Interval>

    @Query("SELECT * FROM  interval_table WHERE parent_id = :id AND sequence = 1")
    suspend fun getStart(id: Long): Interval

    @Query("SELECT notes from interval_table where primary_id = :id")
    suspend fun getNotes(id : Long) : String

    @Query("SELECT percentage FROM  interval_table WHERE parent_id = :id ORDER BY primary_id ASC ")
    suspend fun getAllPercentages(id: Long): List<Float>

    @Query("SELECT color FROM interval_table WHERE parent_id = :id ORDER BY primary_id ASC ")
    suspend fun getAllColors(id: Long): List<Int>

    @Query("SELECT COUNT(alarm_on) from interval_table WHERE parent_id = :id AND alarm_on = 1")
    suspend fun getAlarmCount(id : Long) : Int

    //updates

    @Update
    suspend fun update(interval: Interval)

    @Update
    suspend fun updateAll(interval : List<Interval>)

    @Insert
    suspend fun insert(interval: Interval)

    @Insert
    suspend fun insertList(list : List<Interval>)

    @Delete
    suspend fun delete(interval: Interval)

    @Query("UPDATE interval_table SET alarm_on = 1 - alarm_on WHERE primary_id  = :myID ")
    suspend fun toggleAlarm(myID : Long) : Int


}