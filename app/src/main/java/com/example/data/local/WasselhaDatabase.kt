package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity

@Database(entities = [OrderEntity::class, ProductEntity::class], version = 1, exportSchema = false)
abstract class WasselhaDatabase : RoomDatabase() {

    abstract fun orderDao(): OrderDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: WasselhaDatabase? = null

        fun getInstance(context: Context): WasselhaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WasselhaDatabase::class.java,
                    "wasselha_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
