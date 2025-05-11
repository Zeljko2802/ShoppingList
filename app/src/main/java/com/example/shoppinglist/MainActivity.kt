package com.example.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoppinglist.data.ProductDatabase
import com.example.shoppinglist.data.ProductRepository
import com.example.shoppinglist.module.Product
import android.graphics.BitmapFactory
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import coil.compose.AsyncImage
import com.example.shoppinglist.data.api.PexelsApi
import java.util.concurrent.Executor

class MainActivity : ComponentActivity() {
    private lateinit var repository: ProductRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database, DAO, and repository
        val database = ProductDatabase.getDatabase(this)
        val productDAO = database.productDAO()
        repository = ProductRepository(productDAO, this)

        // Populate database with initial data if empty
        lifecycleScope.launch {
           /* try{
                android.util.Log.d("API_Test", "Testing Pexels API directly")
                val response = PexelsApi.service.searchPhotos("apple")
                android.util.Log.d("API_Test","Response received! Photo count: ${response.photos.size}")
                if(response.photos.isNotEmpty()){
                    android.util.Log.d("API_Test","First image URL: ${response.photos[0].src.medium}")
                }
            }catch (e: Exception){
                android.util.Log.d("API_Test","API test failed witherror ${e.message}")
                e.printStackTrace()
            } */
            android.util.Log.d("MainActivity", "Populating database")
            repository.populateDatabase()
            android.util.Log.d("MainActivity", "Database Population completed")
        }

        setContent {
            MaterialTheme {
                var showAddDialog by remember { mutableStateOf(false) }

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showAddDialog = true },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Product"
                            )
                        }
                    }
                ) { paddingValues ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFCAE1FF))
                            .padding(paddingValues)
                    ) {

                        val products = repository.allProducts.collectAsState(initial = emptyList())
                        android.util.Log.d(
                            "MainActivity",
                            "Collected ${products.value.size} products"
                        )

                        ProductListScreen(
                            products = products.value,
                            onDelete = { product ->
                                lifecycleScope.launch {
                                    repository.delete(product)
                                }
                            },
                            onUpdate = { updatedProduct ->
                                lifecycleScope.launch {
                                    repository.update(updatedProduct)
                                }
                            }
                        )
                    }
                    if (showAddDialog) {
                        AddProductDialog(
                            onDismiss = { showAddDialog = false },
                            onProductAdded = { newProduct ->
                                lifecycleScope.launch {
                                    val productWithImage = newProduct.copy(
                                        imageData = repository.getImageForProduct(newProduct.name)
                                    )
                                    repository.insert(productWithImage)
                                    android.util.Log.d("MainActivity", "Product added with image")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductListScreen(
    modifier: Modifier = Modifier,
    products: List<Product>,
    onDelete: (Product) -> Unit,
    onUpdate: (Product) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Shopping by List",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = products,
                key = { it.uid }
            ) { product ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { dismissValue ->
                        if (dismissValue == DismissValue.DismissedToEnd ||
                            dismissValue == DismissValue.DismissedToStart) {
                            onDelete(product)
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    background = {
                        val color = androidx.compose.ui.graphics.Color(0xFFCAE1FF)
                        val alignment = if (dismissState.dismissDirection == DismissDirection.StartToEnd)
                            Alignment.CenterStart else Alignment.CenterEnd

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    },
                    dismissContent = {
                        SwipeableProductItem(
                            product = product,
                            onUpdate = onUpdate
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SwipeableProductItem(
    product: Product,
    onUpdate: (Product) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            product.imageData?.let { imageData ->
                val bitmap = remember(imageData) {
                    BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Available: ${product.quantity}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    if (showDialog) {
        ProductDetailDialog(
            product = product,
            onDismiss = { showDialog = false },
            onUpdate = onUpdate
        )
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onProductAdded: (Product) -> Unit
){
    var productName by remember { mutableStateOf("") }
    var productQuantity by remember { mutableStateOf("") }
    var isSearchingImage by remember { mutableStateOf(false) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Product") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                        previewImageUrl = null
                    },
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = productQuantity,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                            productQuantity = it
                        }
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (previewImageUrl != null) {
                    Text(
                        text = "Preview image for ${productName}:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AsyncImage(
                        model = previewImageUrl,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Button(
                        onClick = {
                            if (productName.isNotBlank()) {
                                isSearchingImage = true
                                coroutineScope.launch {
                                    try {
                                        val response = PexelsApi.service.searchPhotos(productName)
                                        if (response.photos.isNotEmpty()) {
                                            previewImageUrl = response.photos[0].src.medium
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("AddProductDialog", "Error searching image: ${e.message}")
                                    } finally {
                                        isSearchingImage = false
                                    }
                                }
                            }
                        },
                        enabled = productName.isNotBlank() && !isSearchingImage,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        if (isSearchingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Find Product Image")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productName.isNotBlank() && productQuantity.isNotBlank()) {
                        val quantity = productQuantity.toIntOrNull() ?: 0
                        val newProduct = Product(
                            id = (0..10000).random(),
                            name = productName,
                            quantity = quantity,
                            imageData = null // Will be filled by repository
                        )
                        onProductAdded(newProduct)
                        onDismiss()
                    }
                },
                enabled = productName.isNotBlank() && productQuantity.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onUpdate: (Product) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var selectedQuantity by remember { mutableStateOf(product.quantity.toString()) }
    var editedName by remember { mutableStateOf(product.name ?: "") }
    var editedQuantity by remember { mutableStateOf(product.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            if (isEditing) {
                Text("Edit Product")
            } else {
                Text(text = product.name ?: "")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Convert ByteArray to Bitmap
                product.imageData?.let { imageData ->
                    val bitmap = remember(imageData) {
                        BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                    }

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    // Editing mode
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Product Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editedQuantity,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                editedQuantity = it
                            }
                        },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Viewing mode
                    Text("Product ID: ${product.id}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Name: ${product.name}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Available quantity: ${product.quantity}")

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = selectedQuantity,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                                selectedQuantity = it
                            }
                        },
                        label = { Text("Quantity to buy") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                Button(
                    onClick = {
                        if (editedName.isNotBlank() && editedQuantity.isNotBlank()) {
                            val updatedProduct = product.copy(
                                name = editedName,
                                quantity = editedQuantity.toIntOrNull() ?: 0
                            )
                            onUpdate(updatedProduct)
                            onDismiss()
                        }
                    },
                    enabled = editedName.isNotBlank() && editedQuantity.isNotBlank()
                ) {
                    Text("Save")
                }
            } else {
                Button(onClick = {
                    onDismiss()
                }) {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            if (isEditing) {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel")
                }
            } else {
                Row {
                    TextButton(onClick = { isEditing = true }) {
                        Text("Edit")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    )
}