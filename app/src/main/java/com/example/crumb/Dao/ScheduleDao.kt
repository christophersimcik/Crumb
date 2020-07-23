package com.example.crumb.Dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.crumb.Models.Schedule

@Dao
interface ScheduleDao {

    //query operations

    @Query("SELECT * from schedule_table ORDER BY id DESC")
    fun getAll(): LiveData<List<Schedule>>

    @Query("SELECT * from schedule_table where id = :id")
    suspend fun getSelected(id : Long) : Schedule

    @Query("SELECT * from schedule_table where id = :id")
    fun getSelectedAsLiveData(id : Long) : LiveData<Schedule>

    @Query("SELECT start_time from schedule_table where id = :id")
    suspend fun getStartTime(id : Long) : Int

    @Query("SELECT description from schedule_table where id = :id")
    suspend fun getNotes(id : Long) : String

    @Query("SELECT end_time from schedule_table where id = :id")
    suspend fun getEndTime(id : Long) : Int

    @Query("SELECT duration from schedule_table where id = :id")
    suspend fun getDuration(id : Long) : Int

    @Query("SELECT * from schedule_table where name like '%'||:name||'%'")
    suspend fun getDuplicateCount(name : String) : List<Schedule>

    @Query("SELECT name from schedule_table where id = :id")
    fun getName(id : Long) : LiveData<String >

    @Query("SELECT COUNT(*) from schedule_table where name like '%'||:name||'%'")
    suspend fun getCount(name : String) : Int

    // updates
    @Query("UPDATE schedule_table SET steps = steps + :quantity where id = :id")
    suspend fun giveSteps(id : Long, quantity : Int)

    // updates
    @Query("UPDATE schedule_table SET name = :name where id = :id")
    suspend fun updateScheduleName(id : Long, name : String)

    // updates
    @Query("UPDATE schedule_table SET description = :notes where id = :id")
    suspend fun updateScheduleNotes(id : Long, notes : String)

    @Update
    suspend fun updateSingle(schedule : Schedule)

    // start time
    @Query("UPDATE schedule_table SET start_time = :start where id = :id")
    suspend fun updateStartTime(id : Long, start : Int)
    // end time
    @Query("UPDATE schedule_table SET end_time = :end where id = :id")
    suspend fun updateEndTime(id : Long, end : Int)
    // subtract step
    @Query("UPDATE schedule_table SET steps = steps - :quantity where id = :id")
    suspend fun takeSteps(id : Long, quantity : Int)

    // add duration
    @Query("UPDATE schedule_table SET duration = duration + :amount where id = :id")
    suspend fun giveDuration(id : Long, amount : Int)
    // subtract duration
    @Query("UPDATE schedule_table SET duration = duration - :amount where id = :id")
    suspend fun takeDuration(id : Long, amount : Int)
    // change duration
    @Query("UPDATE schedule_table SET duration = :amount where id = :id")
    suspend fun changeDuration(id : Long, amount : Int)

    //insertion operations
    @Insert()
    suspend fun insert(schedule: Schedule)

    // deletion operations
    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("DELETE from schedule_table WHERE steps = 0")
    suspend fun deleteEmpties()

    @Query("DELETE from schedule_table")
    suspend fun deleteAll()
}
