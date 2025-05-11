package com.example.shoppinglist.data

import androidx.room.*
import com.example.shoppinglist.module.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDAO {
        @Query("SELECT * FROM product")
        fun getAllProducts(): Flow<List<Product>>

        @Query("SELECT * FROM product WHERE id = :id")
        fun getProductById(id: Int): Product?

        @Insert
        suspend fun insertAll(vararg product: Product)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertProducts(vararg product: Product)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertProducts(product: List<Product>)

        @Update
        suspend fun update(product: Product)

        @Delete
        suspend fun delete(product: Product)

        @Query("DELETE FROM product")
        suspend fun deleteAll()

        @Query("SELECT COUNT(*) FROM product")
        suspend fun getProductCount(): Int

}