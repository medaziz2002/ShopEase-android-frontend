package com.example.project180.Model




/*
data class ProductDto(
    val id: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val price: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val size:String? = null,
    val sellerTelephone:String?=null,
    val categoryId: Int? = null,
    val categoryDto: CategoryDto? = null,
    val images: List<ImageDto>? = null,
    val sellerId: Int? = null,
    val sellerName: String? = null
)
*/



data class ProductDto(
    val id: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val price: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val size: List<String>? = null,  // Pour les vêtements
    val weight: List<String>? = null, // Pour la nourriture
    val sellerTelephone: String? = null,
    val categoryId: Int? = null,
    val categoryDto: CategoryDto? = null,
    val images: List<ImageDto>? = null,
    val sellerId: Int? = null,
    val sellerName: String? = null,
    val sellerPic: String? = null
)
