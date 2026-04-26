package com.example.expensetracker.data.repository

import com.example.expensetracker.data.db.dao.CategoryDao
import com.example.expensetracker.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryInUseException(val name: String, val count: Int) :
    Exception("分类「$name」下有 $count 笔支出，无法删除")

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getAll(): Flow<List<CategoryEntity>> = categoryDao.getAll()

    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun save(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun update(category: CategoryEntity) = categoryDao.update(category)

    suspend fun delete(category: CategoryEntity) {
        val count = categoryDao.getExpenseCount(category.id)
        if (count > 0) throw CategoryInUseException(category.name, count)
        categoryDao.delete(category)
    }

    suspend fun getExpenseCount(categoryId: Long): Int =
        categoryDao.getExpenseCount(categoryId)
}
