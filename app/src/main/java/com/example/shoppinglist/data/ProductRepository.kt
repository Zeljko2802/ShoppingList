package com.example.shoppinglist.data

import com.example.shoppinglist.data.api.PexelsApi
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import com.example.shoppinglist.module.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory

class ProductRepository(private val productDAO: ProductDAO, private val context: Context) {
    val allProducts: Flow<List<Product>> = productDAO.getAllProducts()

    suspend fun insert(product: Product) {
        productDAO.insertProducts(product)
    }

    suspend fun update(product: Product) {
        productDAO.update(product)
    }

    suspend fun delete(product: Product) {
        productDAO.delete(product)
    }

    suspend fun deleteAll() {
        productDAO.deleteAll()
    }

    // Convert drawable resource to ByteArray
    fun drawableToByteArray(resourceId: Int): ByteArray {
        val drawable = ContextCompat.getDrawable(context, resourceId)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
    fun getDefaultProductImage(): ByteArray{
        val defaultResourceId = context.resources.getIdentifier("default_product", "drawable", context.packageName)
        return if (defaultResourceId != 0){
            drawableToByteArray(defaultResourceId)
        }else{
            val fallbackResourceId = context.resources.getIdentifier("bier", "drawable", context.packageName)
            drawableToByteArray(fallbackResourceId)
        }
    }


    suspend fun getImageForProduct(productName: String): ByteArray? {
        return try {
            val response = PexelsApi.service.searchPhotos(productName)
            if (response.photos.isNotEmpty()) {
                val imageUrl = response.photos[0].src.medium
                // Download the image
                downloadImage(imageUrl)
            } else {
                // Fall back to default image
                getDefaultProductImage()
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error fetching image: ${e.message}")
            // Fall back to default image
            getDefaultProductImage()
        }
    }

    private suspend fun downloadImage(url: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val response = OkHttpClient().newCall(
                Request.Builder().url(url).build()
            ).execute()

            if (response.isSuccessful) {
                response.body?.bytes() ?: ByteArray(0)
            } else {
                throw IOException("Failed to download image: ${response.code}")
            }
        }
    }
    // Populate database with initial products
    suspend fun populateDatabase() = withContext(Dispatchers.IO) {

        val count = productDAO.getProductCount()
        android.util.Log.d("ProductRepository", "Current product count: $count")
        // Check if database is empty
        if (count == 0) {
            android.util.Log.d("ProductRepository", "Database is empty, populating with initial data")

            val resourceIds = listOf(
                context.resources.getIdentifier("bier", "drawable", context.packageName),
                context.resources.getIdentifier("laptop", "drawable", context.packageName),
                context.resources.getIdentifier("kopfhoerer", "drawable", context.packageName),
                context.resources.getIdentifier("wasserflasche", "drawable", context.packageName),
                context.resources.getIdentifier("tomaten", "drawable", context.packageName),
                context.resources.getIdentifier("parfuem", "drawable", context.packageName),
                context.resources.getIdentifier("aufstrich", "drawable", context.packageName)
            )
            android.util.Log.d("ProductRepository", "Resource IDs $resourceIds")

            val products = listOf(
                Product(id = 1, name = "Beer", quantity = 25, imageData = drawableToByteArray(context.resources.getIdentifier("bier", "drawable", context.packageName))),
                Product(id = 2, name = "Laptop", quantity = 10, imageData = drawableToByteArray(context.resources.getIdentifier("laptop", "drawable", context.packageName))),
                Product(id = 3, name = "Headphones", quantity = 30, imageData = drawableToByteArray(context.resources.getIdentifier("kopfhoerer", "drawable", context.packageName))),
                Product(id = 4, name = "Bottle of water", quantity = 50, imageData = drawableToByteArray(context.resources.getIdentifier("wasserflasche", "drawable", context.packageName))),
                Product(id = 5, name = "Tomato", quantity = 21, imageData = drawableToByteArray(context.resources.getIdentifier("tomaten", "drawable", context.packageName))),
                Product(id = 6, name = "Perfume", quantity = 20, imageData = drawableToByteArray(context.resources.getIdentifier("parfuem", "drawable", context.packageName))),
                Product(id = 7, name = "Spread", quantity = 40, imageData = drawableToByteArray(context.resources.getIdentifier("aufstrich", "drawable", context.packageName)))
            )
            productDAO.insertProducts(products)
            android.util.Log.d("ProductRepository", "Inserted ${products.size} products")
        }
    }
}