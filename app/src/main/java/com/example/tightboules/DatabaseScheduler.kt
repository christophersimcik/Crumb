package com.example.tightboules

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Schedule::class, Interval::class], version = 2, exportSchema = false)

abstract class DatabaseScheduler : RoomDatabase() {
    abstract fun getScheduleDao(): ScheduleDao
    abstract fun getIntervalDao(): IntervalDao

    companion object {
        const val DATABASE_NAME = "Schedules database"
        private var instance: DatabaseScheduler? = null
        fun getInstance(context: Context): DatabaseScheduler? {
            if (instance == null) {
                synchronized(DatabaseScheduler::class) {
                    instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        DatabaseScheduler::class.java, DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance
        }
    }
}




