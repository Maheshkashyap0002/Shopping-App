package com.shoppingappmahesh.data.util

import com.google.firebase.database.FirebaseDatabase
import com.shoppingappmahesh.domain.model.Category
import com.shoppingappmahesh.domain.model.Product
import javax.inject.Inject

class FirebaseDataSeeder @Inject constructor(
    private val database: FirebaseDatabase
) {
    fun seedData() {
        val productsRef = database.getReference("products")
        
        // Check if data already exists to avoid overwriting your manual changes in Firebase Console
        productsRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                performInitialSeed()
            }
        }
    }

    private fun performInitialSeed() {
        val categoriesRef = database.getReference("categories")
        val productsRef = database.getReference("products")

        val categories = listOf(
            Category(id = "1", name = "Clothes", icon = com.shoppingappmahesh.R.drawable.ic_launcher_foreground),
            Category(id = "2", name = "Mobiles", icon = com.shoppingappmahesh.R.drawable.ic_launcher_foreground),
            Category(id = "3", name = "Shoes", icon = com.shoppingappmahesh.R.drawable.ic_launcher_foreground),
            Category(id = "4", name = "Watches", icon = com.shoppingappmahesh.R.drawable.ic_launcher_foreground),
            Category(id = "5", name = "Laptops", icon = com.shoppingappmahesh.R.drawable.ic_launcher_foreground)
        )

        categories.forEach { categoriesRef.child(it.id).setValue(it) }

        val clothImages = listOf(
            "https://images.unsplash.com/photo-1591047139829-d91aecb6caea",
            "https://images.unsplash.com/photo-1551028719-00167b16eac5",
            "https://images.unsplash.com/photo-1543076447-215ad9ba6923",
            "https://images.unsplash.com/photo-1521572267360-ee0c2909d518",
            "https://images.unsplash.com/photo-1434389677669-e08b4cac3105"
        )
        val mobileImages = listOf(
            "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9",
            "https://images.unsplash.com/photo-1592899677977-9c10ca588bbd",
            "https://images.unsplash.com/photo-1567581935884-3349723552ca"
        )

        val products = mutableListOf<Product>()
        
        for (i in 1..100) {
            val catId = (i % 5 + 1).toString()
            val category = categories.find { it.id == catId }?.name ?: "General"
            
            val baseImage = when(catId) {
                "1" -> clothImages[i % clothImages.size]
                "2" -> mobileImages[i % mobileImages.size]
                "3" -> "https://images.unsplash.com/photo-1542291026-7eec264c27ff"
                "4" -> "https://images.unsplash.com/photo-1523275335684-37898b6baf30"
                else -> "https://images.unsplash.com/photo-1496181133206-80ce9b88a853"
            }

            val price = 499.0 + (i * 200)
            val discountPrice = price * 0.75
            
            val isSnake = i % 15 == 0
            val finalName = if(isSnake) "Snake Jacket Pro Elite v$i" else "$category Premium Model #$i"
            val finalPrice = if(isSnake) 1.0 else price
            val finalDiscount = if(isSnake) 1.0 else discountPrice

            products.add(
                Product(
                    id = "p$i",
                    name = finalName,
                    description = "High-quality $category item with premium materials. This product offers extreme durability and a stylish look suitable for any occasion.",
                    price = finalPrice,
                    discountPrice = finalDiscount,
                    stock = 15,
                    categoryId = catId,
                    images = listOf("$baseImage?q=80&w=800&auto=format&fit=crop")
                )
            )
        }

        products.forEach { productsRef.child(it.id).setValue(it) }
    }
}