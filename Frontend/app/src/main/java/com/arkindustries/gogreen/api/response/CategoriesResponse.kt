package com.arkindustries.gogreen.api.response

data class CategoryResponse(
    val category: Category
)

data class Category(
    val _id: String,
    val title: String,
)