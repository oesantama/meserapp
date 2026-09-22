package com.example.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)

    val businesses: StateFlow<List<BusinessEntity>> = repository.allBusinesses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentBusiness = MutableStateFlow<BusinessEntity?>(null)
    val currentBusiness: StateFlow<BusinessEntity?> = _currentBusiness.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentMenuId = MutableStateFlow<Long>(1L)
    val currentMenuId: StateFlow<Long> = _currentMenuId.asStateFlow()

    private val _syncStatus = MutableStateFlow<String>("Sincronizado (En línea)")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed initial demo data if empty
            db.businessDao().getAllBusinesses().collect { list ->
                if (list.isEmpty()) {
                    seedDemoData()
                } else if (_currentBusiness.value == null) {
                    _currentBusiness.value = list.first()
                }
            }
        }
    }

    private suspend fun seedDemoData() {
        val bId = repository.insertBusiness(
            BusinessEntity(
                name = "Restaurante El Sabor Casero",
                businessType = "Restaurante",
                address = "Calle Principal #123",
                phone = "555-0199",
                isTabletServerMode = false
            )
        )
        val bId2 = repository.insertBusiness(
            BusinessEntity(
                name = "Panadería La Espiga Dorada",
                businessType = "Panadería",
                address = "Av. Central #45",
                phone = "555-0288",
                isTabletServerMode = false
            )
        )

        // Users for restaurant
        repository.insertUser(UserEntity(businessId = bId, username = "admin", fullName = "Carlos Administrador", role = "Admin", pinOrPassword = "123"))
        repository.insertUser(UserEntity(businessId = bId, username = "mesero", fullName = "Juan Mesero", role = "Waiter", pinOrPassword = "123"))
        repository.insertUser(UserEntity(businessId = bId, username = "cliente", fullName = "María Cliente", role = "Customer", pinOrPassword = "123"))

        // Menu day & items for restaurant
        val menuId = repository.insertMenuDay(MenuDayEntity(businessId = bId, dayName = "Menú de Hoy - Lunes", description = "Almuerzos caseros tradicionales con opciones personalizables"))
        _currentMenuId.value = menuId

        repository.insertMenuItem(MenuItemEntity(
            menuId = menuId,
            businessId = bId,
            category = "Plato Principal",
            name = "Bandeja Ejecutiva del Día",
            price = 8.50,
            description = "Incluye arroz, proteína, ensalada y sopa.",
            customizationOptions = "Arroz con frijol|Solo frijol|Con huevo|Sin huevo|Con ensalada|Sin ensalada|Pollo|Res"
        ))
        repository.insertMenuItem(MenuItemEntity(
            menuId = menuId,
            businessId = bId,
            category = "Bebida",
            name = "Jugo Natural de Fruta",
            price = 2.00,
            description = "Maracuyá, Lulo o Mora",
            customizationOptions = "En agua|En leche|Con azúcar|Sin azúcar"
        ))

        // Tables for restaurant
        for (i in 1..6) {
            repository.insertTable(TableEntity(
                businessId = bId,
                tableNumber = i,
                qrCodeToken = "QR_TABLE_${bId}_$i",
                status = if (i % 2 == 0) "Occupied" else "Available"
            ))
        }

        repository.insertLog(AuditLogEntity(businessId = bId, action = "SISTEMA_INICIADO", details = "Se inicializó la base de datos local Room."))
    }

    fun selectBusiness(business: BusinessEntity) {
        _currentBusiness.value = business
    }

    fun setCurrentMenuId(id: Long) {
        _currentMenuId.value = id
    }

    suspend fun login(username: String, pin: String): Boolean {
        val user = repository.loginUser(username, pin)
        if (user != null) {
            _currentUser.value = user
            val b = repository.getBusinessById(user.businessId)
            if (b != null) {
                _currentBusiness.value = b
            }
            repository.insertLog(AuditLogEntity(businessId = user.businessId, action = "LOGIN", details = "Usuario ${user.fullName} (${user.role}) inició sesión."))
            return true
        }
        return false
    }

    fun logout() {
        _currentUser.value = null
    }

    fun toggleTabletServerMode(enabled: Boolean) {
        val b = _currentBusiness.value ?: return
        viewModelScope.launch {
            val updated = b.copy(isTabletServerMode = enabled)
            repository.updateBusiness(updated)
            _currentBusiness.value = updated
            val modeDesc = if (enabled) "Modo Servidor Tablet ACTIVADO (Operando offline/local)" else "Modo Servidor Tablet DESACTIVADO (En línea)"
            _syncStatus.value = if (enabled) "Modo Tablet Local (Pendiente sincronizar)" else "Sincronizado (En línea)"
            repository.insertLog(AuditLogEntity(businessId = b.id, action = "MODO_TABLET", details = modeDesc))
        }
    }

    fun syncDataWithServer() {
        val b = _currentBusiness.value ?: return
        viewModelScope.launch {
            _syncStatus.value = "Sincronizando con servidor..."
            kotlinx.coroutines.delay(1200)
            repository.markAllSynced(b.id)
            val updated = b.copy(lastSyncedAt = System.currentTimeMillis())
            repository.updateBusiness(updated)
            _currentBusiness.value = updated
            _syncStatus.value = "Sincronizado exitosamente"
            repository.insertLog(AuditLogEntity(businessId = b.id, action = "SINCRONIZACION", details = "Datos sincronizados con éxito con el servidor central."))
        }
    }

    fun addBusiness(name: String, type: String, address: String, phone: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val bId = repository.insertBusiness(BusinessEntity(name = name, businessType = type, address = address, phone = phone))
            // create default menu & tables
            val mId = repository.insertMenuDay(MenuDayEntity(businessId = bId, dayName = "Menú General", description = "Menú principal del negocio"))
            for (i in 1..4) {
                repository.insertTable(TableEntity(businessId = bId, tableNumber = i, qrCodeToken = "QR_${bId}_$i", status = "Available"))
            }
            repository.insertLog(AuditLogEntity(businessId = bId, action = "CREAR_NEGOCIO", details = "Se creó el negocio $name de tipo $type"))
            onComplete(bId)
        }
    }

    fun addMenuDay(businessId: Long, dayName: String, description: String) {
        viewModelScope.launch {
            repository.insertMenuDay(MenuDayEntity(businessId = businessId, dayName = dayName, description = description))
            repository.insertLog(AuditLogEntity(businessId = businessId, action = "CREAR_MENU", details = "Se creó el menú: $dayName"))
        }
    }

    fun addMenuItem(menuId: Long, businessId: Long, category: String, name: String, price: Double, description: String, options: String) {
        viewModelScope.launch {
            repository.insertMenuItem(MenuItemEntity(menuId = menuId, businessId = businessId, category = category, name = name, price = price, description = description, customizationOptions = options))
            repository.insertLog(AuditLogEntity(businessId = businessId, action = "CREAR_ITEM_MENU", details = "Se añadió plato/producto: $name ($$price)"))
        }
    }

    fun addTable(businessId: Long, tableNumber: Int) {
        viewModelScope.launch {
            repository.insertTable(TableEntity(businessId = businessId, tableNumber = tableNumber, qrCodeToken = "QR_${businessId}_$tableNumber", status = "Available"))
            repository.insertLog(AuditLogEntity(businessId = businessId, action = "CREAR_MESA", details = "Mesa #$tableNumber registrada."))
        }
    }

    fun placeOrder(businessId: Long, tableId: Long?, tableNumber: Int?, customerName: String, itemsSummary: String, totalAmount: Double, orderType: String) {
        viewModelScope.launch {
            val isOffline = _currentBusiness.value?.isTabletServerMode == true
            repository.insertOrder(OrderEntity(
                businessId = businessId,
                tableId = tableId,
                tableNumber = tableNumber,
                customerName = customerName,
                itemsSummary = itemsSummary,
                totalAmount = totalAmount,
                status = "Pending",
                orderType = orderType,
                isSynced = !isOffline
            ))
            if (tableId != null) {
                repository.updateTableStatus(tableId, "Occupied")
            }
            repository.insertLog(AuditLogEntity(businessId = businessId, action = "NUEVO_PEDIDO", details = "Pedido de $customerName ($orderType) por $$totalAmount. Sincronizado: ${!isOffline}"))
        }
    }

    fun updateOrderStatus(orderId: Long, status: String, businessId: Long) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            repository.insertLog(AuditLogEntity(businessId = businessId, action = "ESTADO_PEDIDO", details = "Pedido #$orderId cambiado a estado: $status"))
        }
    }

    fun getMenuDaysFlow(businessId: Long) = repository.getMenuDays(businessId)
    fun getMenuItemsFlow(menuId: Long) = repository.getMenuItems(menuId)
    fun getTablesFlow(businessId: Long) = repository.getTables(businessId)
    fun getOrdersFlow(businessId: Long) = repository.getOrders(businessId)
    fun getAuditLogsFlow(businessId: Long) = repository.getAuditLogs(businessId)
    fun getUsersByBusiness(businessId: Long) = repository.getUsersByBusiness(businessId)

    fun addUser(businessId: Long, username: String, fullName: String, role: String, pin: String) {
        viewModelScope.launch {
            repository.insertUser(UserEntity(businessId = businessId, username = username, fullName = fullName, role = role, pinOrPassword = pin))
            repository.insertLog(AuditLogEntity(businessId = businessId, action = "CREAR_USUARIO", details = "Se creó usuario $fullName ($role)"))
        }
    }
}
