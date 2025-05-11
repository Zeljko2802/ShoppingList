package com.example.shoppinglist.module

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import android.util.Base64


@Entity(tableName ="product")
data class Product(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "image_data") val imageData: ByteArray? = null
){
    override fun equals(other: Any?): Boolean {
        if(this == other) return true
        if(javaClass != other?.javaClass) return false

        other as Product

        if (uid != other.uid) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (quantity != other.quantity) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid
        result = 31 * result + id
        result = 31 * result + (name.hashCode())
        result = 31 * result + quantity
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}

class Converters{
    @TypeConverter
    fun fromByteArray(value: ByteArray?): String?{
        return value?.let { Base64.encodeToString(it, Base64.DEFAULT) }
    }

    @TypeConverter
    fun toByteArray(value: String?): ByteArray? {
        return value?.let { Base64.decode(it, Base64.DEFAULT) }
    }
}

