package com.example.crumb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey


@Entity(
    tableName = "interval_table",
    foreignKeys = [
        ForeignKey(
            entity = Schedule::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = CASCADE
        )
    ]
)
data class Interval(
    @PrimaryKey
    @ColumnInfo(name = "primary_id") var id: Long,
    @ColumnInfo(name = "parent_id", index = true) var parentId: Long,
    @ColumnInfo(name = "sequence") var sequence: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "notes") var notes: String,
    @ColumnInfo(name = "time") var time: Int,
    @ColumnInfo(name = "span") var span : Int,
    @ColumnInfo(name = "percentage") var percentage : Float,
    @ColumnInfo(name = "alarm_on") var alarm_on : Boolean,
    @ColumnInfo(name = "intent_id") var intent_id : String,
    @ColumnInfo(name = "pending_intent_id") var pending_intent_id : Int,
    @ColumnInfo(name = "alarm time") var alarm_time : Long,
    @ColumnInfo(name = "color") var color : Int
)