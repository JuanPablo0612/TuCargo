package com.juanpablo0612.tucargo.data.tracking.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocationEntity::class], version = 1, exportSchema = false)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile private var instance: LocationDatabase? = null

        fun build(context: Context): LocationDatabase =
            instance ?: synchronized(this) {
                instance ?: Room
                    .databaseBuilder(context, LocationDatabase::class.java, "tucargo_locations.db")
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
