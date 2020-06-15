package com.example.tightboules

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_table")
data class Schedule(
    @PrimaryKey var id : Long,
    @ColumnInfo(name = "name") var name : String,
    @ColumnInfo(name = "date") var date : String,
    @ColumnInfo(name = "description") var description : String,
    @ColumnInfo(name = "start_time") var start: Int,
    @ColumnInfo(name = "end_time") var end : Int,
    @ColumnInfo(name = "duration") var duration : Int,
    @ColumnInfo(name = "steps") var steps : Int
)