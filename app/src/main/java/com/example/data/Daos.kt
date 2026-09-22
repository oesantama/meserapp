package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE id = :id")
    suspend fun getBusinessById(id: Long): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity): Long

    @Update
    suspend fun updateBusiness(business: BusinessEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE businessId = :businessId")
    fun getUsersByBusiness(businessId: Long): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username = :username AND pinOrPassword = :pin")
    suspend fun loginUser(username: String, pin: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
}

@Dao
interface MenuDayDao {
    @Query("SELECT * FROM menu_days WHERE businessId = :businessId")
    fun getMenuDaysByBusiness(businessId: Long): Flow<List<MenuDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuDay(menuDay: MenuDayEntity): Long

    @Delete
    suspend fun deleteMenuDay(menuDay: MenuDayEntity)
}

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items WHERE menuId = :menuId")
    fun getItemsForMenu(menuId: Long): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE businessId = :businessId")
    fun getAllItemsForBusiness(businessId: Long): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity): Long

    @Delete
    suspend fun deleteMenuItem(item: MenuItemEntity)
}

@Dao
interface TableDao {
    @Query("SELECT * FROM tables WHERE businessId = :businessId")
    fun getTablesByBusiness(businessId: Long): Flow<List<TableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: TableEntity): Long

    @Query("UPDATE tables SET status = :status WHERE id = :tableId")
    suspend fun updateTableStatus(tableId: Long, status: String)

    @Delete
    suspend fun deleteTable(table: TableEntity)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getOrdersByBusiness(businessId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE businessId = :businessId AND isSynced = 0")
    suspend fun getUnsyncedOrders(businessId: Long): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE orders SET isSynced = 1 WHERE businessId = :businessId")
    suspend fun markAllSynced(businessId: Long)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs WHERE businessId = :businessId ORDER BY timestamp DESC")
    fun getLogsByBusiness(businessId: Long): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long
}
