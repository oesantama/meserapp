package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {
    val allBusinesses: Flow<List<BusinessEntity>> = db.businessDao().getAllBusinesses()

    suspend fun getBusinessById(id: Long) = db.businessDao().getBusinessById(id)
    suspend fun insertBusiness(business: BusinessEntity) = db.businessDao().insertBusiness(business)
    suspend fun updateBusiness(business: BusinessEntity) = db.businessDao().updateBusiness(business)

    fun getUsersByBusiness(businessId: Long) = db.userDao().getUsersByBusiness(businessId)
    suspend fun loginUser(username: String, pin: String) = db.userDao().loginUser(username, pin)
    suspend fun insertUser(user: UserEntity) = db.userDao().insertUser(user)

    fun getMenuDays(businessId: Long) = db.menuDayDao().getMenuDaysByBusiness(businessId)
    suspend fun insertMenuDay(day: MenuDayEntity) = db.menuDayDao().insertMenuDay(day)
    suspend fun deleteMenuDay(day: MenuDayEntity) = db.menuDayDao().deleteMenuDay(day)

    fun getMenuItems(menuId: Long) = db.menuItemDao().getItemsForMenu(menuId)
    fun getAllMenuItems(businessId: Long) = db.menuItemDao().getAllItemsForBusiness(businessId)
    suspend fun insertMenuItem(item: MenuItemEntity) = db.menuItemDao().insertMenuItem(item)
    suspend fun deleteMenuItem(item: MenuItemEntity) = db.menuItemDao().deleteMenuItem(item)

    fun getTables(businessId: Long) = db.tableDao().getTablesByBusiness(businessId)
    suspend fun insertTable(table: TableEntity) = db.tableDao().insertTable(table)
    suspend fun updateTableStatus(tableId: Long, status: String) = db.tableDao().updateTableStatus(tableId, status)
    suspend fun deleteTable(table: TableEntity) = db.tableDao().deleteTable(table)

    fun getOrders(businessId: Long) = db.orderDao().getOrdersByBusiness(businessId)
    suspend fun insertOrder(order: OrderEntity) = db.orderDao().insertOrder(order)
    suspend fun updateOrderStatus(orderId: Long, status: String) = db.orderDao().updateOrderStatus(orderId, status)
    suspend fun getUnsyncedOrders(businessId: Long) = db.orderDao().getUnsyncedOrders(businessId)
    suspend fun markAllSynced(businessId: Long) = db.orderDao().markAllSynced(businessId)

    fun getAuditLogs(businessId: Long) = db.auditLogDao().getLogsByBusiness(businessId)
    suspend fun insertLog(log: AuditLogEntity) = db.auditLogDao().insertLog(log)
}
