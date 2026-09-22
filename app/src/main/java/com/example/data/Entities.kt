package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val businessType: String, // "Restaurante", "Tienda", "Panadería"
    val address: String,
    val phone: String,
    val isTabletServerMode: Boolean = false,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val businessId: Long,
    val username: String,
    val fullName: String,
    val role: String, // "Admin", "Waiter", "Customer"
    val pinOrPassword: String
)

@Entity(tableName = "menu_days")
data class MenuDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val businessId: Long,
    val dayName: String, // e.g. "Lunes", "Martes", "Menú del Día - Hoy"
    val description: String
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val menuId: Long,
    val businessId: Long,
    val category: String, // "Plato Principal", "Bebida", "Acompañamiento"
    val name: String,
    val price: Double,
    val description: String,
    val allowsCustomization: Boolean = true,
    // JSON or comma separated options like "Arroz con frijol, Solo frijol, Con huevo, Sin huevo, Con ensalada"
    val customizationOptions: String = "Arroz con frijol|Solo frijol|Con huevo|Sin huevo|Con ensalada|Sin ensalada"
)

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val businessId: Long,
    val tableNumber: Int,
    val qrCodeToken: String,
    val status: String // "Available", "Occupied", "Reserved"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val businessId: Long,
    val tableId: Long?,
    val tableNumber: Int?,
    val customerName: String,
    val itemsSummary: String, // Details of items + custom options
    val totalAmount: Double,
    val status: String, // "Pending", "Preparing", "Ready", "Delivered", "Completed"
    val orderType: String, // "DineInQR", "Delivery", "WalkIn"
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val businessId: Long,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
