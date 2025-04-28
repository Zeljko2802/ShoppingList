package com.example.shoppinglist
import com.example.shoppinglist.module.Product

import androidx.compose.foundation.background
import com.example.shoppinglist.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // arrayListe der Produkte erstellen
        val products = arrayListOf(
            Product(1, "Beer", 25, R.drawable.bier),
            Product(2, "Laptop", 10, R.drawable.laptop),
            Product(3, "Headphones", 30, R.drawable.kopfhoerer),
            Product(4, "Bottle of water", 50, R.drawable.wasserflasche),
            Product(5, "Tomato", 21, R.drawable.tomaten),
            Product(6, "Perfume", 20, R.drawable.parfuem),
            Product(7, "Spread", 40, R.drawable.aufstrich),
        )

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        //Hintergund und Display Einstellungen UI
                    .fillMaxSize()
                    .background(color = androidx.compose.ui.graphics.Color(0xFFCAE1FF)),
                    color = androidx.compose.ui.graphics.Color(0xFFCAE1FF)
                ) {
                    ProductListScreen(products)
                }
            }
        }
    }
}
//Erstellen der Produktliste mit vertikaler ausrichtung
@Composable
fun ProductListScreen(products: List<Product>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Shopping by List",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        //Scrollfunkion hinzufügen
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //Prodkute werden aufgerufen
            items(products) { product ->
                ProductItem(product)
            }
        }
    }
}
@Composable
fun ProductItem(product: Product) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        //Produkte werden in einer Reihe nebeneiander angezeigt
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //zeigt das Bilder der Produkte an
            Image(
                painter = painterResource(id = product.image),
                contentDescription = product.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))
            //zeigt die Produkttexte an
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))
                //Angabe der vorhandenen Menge
                Text(
                    text = "Available: ${product.quantity}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    //anzeige des Dialigfensters, sofern es angeklickt wurde
    if (showDialog) {
        ProductDetailDialog(
            product = product,
            onDismiss = { showDialog = false }
        )
    }
}
//Aufruf des Dialog Fensters (Pop-Up)
@Composable
fun ProductDetailDialog(product: Product, onDismiss: () -> Unit) {
    var selectedQuantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = product.name) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = product.image),
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Product ID: ${product.id}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${product.name}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Available quantity: ${product.quantity}")

                Spacer(modifier = Modifier.height(16.dp))

                // Mengen Eingabefeld
                OutlinedTextField(
                    value = selectedQuantity,
                    onValueChange = {
                        // Nur Integer sind Erlaub
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
        },
        //Button um das Produkt in den Warenkorb zu legen
        confirmButton = {
            Button(onClick = {
                onDismiss()
            }) {
                Text("Add") //Noch kein Warenkorb hinzugefügt => Version 1.1.2
            }
        },
        //Pop up Fenster schließen
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}