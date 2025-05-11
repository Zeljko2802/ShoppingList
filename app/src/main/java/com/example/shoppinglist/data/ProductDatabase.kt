package com.example.shoppinglist.data

import androidx.room.*
import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.shoppinglist.module.Product
import com.example.shoppinglist.module.Converters


@Database(entities = [Product::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ProductDatabase : RoomDatabase(){
        abstract fun productDAO() : ProductDAO

        companion object{
            @Volatile
            private var INSTANCE: ProductDatabase? = null

            fun getDatabase(context: Context): ProductDatabase{
                return INSTANCE ?: synchronized(this){
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ProductDatabase::class.java,
                        "shopping_list_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                        INSTANCE = instance
                        instance

                }
            }
        }
    }