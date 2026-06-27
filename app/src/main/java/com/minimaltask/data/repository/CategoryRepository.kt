package com.minimaltask.data.repository

import com.minimaltask.data.database.CategoryDao
import com.minimaltask.data.model.Category
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun observeCategories(): Flow<List<Category>> = categoryDao.observeCategories()

    suspend fun add(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) categoryDao.insert(Category(name = trimmed))
    }

    suspend fun update(category: Category, name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) categoryDao.update(category.copy(name = trimmed))
    }

    suspend fun delete(category: Category) = categoryDao.delete(category)
}
