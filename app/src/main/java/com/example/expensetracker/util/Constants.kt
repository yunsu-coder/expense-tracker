package com.example.expensetracker.util

import com.example.expensetracker.data.db.entity.CategoryEntity

const val PAGE_SIZE = 20
const val DB_NAME = "expense_tracker.db"

val SEED_CATEGORIES = listOf(
    CategoryEntity(id = 0, name = "餐饮", colorHex = 0xFF4CAF50, sortOrder = 0),
    CategoryEntity(id = 0, name = "交通", colorHex = 0xFF2196F3, sortOrder = 1),
    CategoryEntity(id = 0, name = "购物", colorHex = 0xFFFF9800, sortOrder = 2),
    CategoryEntity(id = 0, name = "居住", colorHex = 0xFF795548, sortOrder = 3),
    CategoryEntity(id = 0, name = "娱乐", colorHex = 0xFF9C27B0, sortOrder = 4),
    CategoryEntity(id = 0, name = "医疗", colorHex = 0xFFF44336, sortOrder = 5),
    CategoryEntity(id = 0, name = "教育", colorHex = 0xFF00BCD4, sortOrder = 6),
    CategoryEntity(id = 0, name = "其他", colorHex = 0xFF607D8B, sortOrder = 7)
)
