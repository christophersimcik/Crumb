package com.example.crumb.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.crumb.Dao.IntervalDao
import com.example.crumb.Dao.ScheduleDao
import com.example.crumb.Models.Interval
import com.example.crumb.Models.Schedule

@Database(
    entities = [Schedule::class, Interval::class], version = 2, exportSchema = false)

abstract class DatabaseScheduler : RoomDatabase() {
    abstract fun getScheduleDao(): ScheduleDao
    abstract fun getIntervalDao(): IntervalDao

    companion object {
        private const val DATABASE_NAME = "Schedules database"
        private var instance: DatabaseScheduler? = null
        fun getInstance(context: Context): DatabaseScheduler? {
            if (instance == null) {
                synchronized(DatabaseScheduler::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DatabaseScheduler::class.java,
                        DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance
        }
    }
}




