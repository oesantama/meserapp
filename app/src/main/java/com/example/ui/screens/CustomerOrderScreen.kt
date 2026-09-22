package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app.viewmodel.AppViewModel
import com.example.data.MenuItemEntity
import com.example.data.TableEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderScreen(
    viewModel: AppViewModel,
    selectedTable: TableEntity?,
    onBack: () -> Unit
) {
    val currentBusiness by viewModel.currentBusiness.collectAsState()
    val menuDays by viewModel.getMenuDaysFlow(currentBusiness?.id ?: 1L).collectAsState(initial = emptyList())
    val currentMenuId by viewModel.currentMenuId.collectAsState()
    val menuItems by viewModel.getMenuItemsFlow(currentMenuId).collectAsState(initial = emptyList())
    val orders by viewModel.getOrdersFlow(currentBusiness?.id ?: 1L).collectAsState(initial = emptyList())

    var customerName by remember { mutableStateOf("Cliente Mesa #${selectedTable?.tableNumber ?: 1}") }
    var selectedItemForCustomization by remember { mutableStateOf<MenuItemEntity?>(null) }
    var chosenOptions by remember { mutableStateOf(mutableSetOf<String>()) }
    var orderPlacedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú del Día - ${currentBusiness?.name ?: "Negocio"} (Mesa #${selectedTable?.tableNumber ?: 1})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Su Nombre / Referencia") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Seleccione sus platos y personalice (Arroz con frijol, con/sin huevo, etc.):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            if (menuItems.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay opciones en el menú del día actualmente.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(menuItems) { item ->
                        CustomerMenuItemCard(
                            item = item,
                            onCustomizeClick = {
                                selectedItemForCustomization = item
                                chosenOptions.clear()
                            }
                        )
                    }
                }
            }

            if (orderPlacedMessage) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("¡Pedido enviado con éxito a la cocina/meseros! No necesita esperar al mesero.", color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
        }
    }

    if (selectedItemForCustomization != null) {
        val item = selectedItemForCustomization!!
        val optionsList = item.customizationOptions.split("|")

        AlertDialog(
            onDismissRequest = { selectedItemForCustomization = null },
            title = { Text("Personalizar: ${item.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Seleccione los acompañamientos y preferencias:", style = MaterialTheme.typography.bodyMedium)
                    optionsList.forEach { opt ->
                        val isChecked = chosenOptions.contains(opt)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(opt)
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) chosenOptions.add(opt) else chosenOptions.remove(opt)
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val optionsSummary = if (chosenOptions.isEmpty()) "Sin personalizar" else chosenOptions.joinToString(", ")
                    val summary = "${item.name} [Opciones: $optionsSummary]"
                    currentBusiness?.id?.let { bId ->
                        viewModel.placeOrder(
                            businessId = bId,
                            tableId = selectedTable?.id,
                            tableNumber = selectedTable?.tableNumber,
                            customerName = customerName,
                            itemsSummary = summary,
                            totalAmount = item.price,
                            orderType = "DineInQR"
                        )
                    }
                    selectedItemForCustomization = null
                    orderPlacedMessage = true
                }) {
                    Text("Enviar Pedido al Instante")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemForCustomization = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CustomerMenuItemCard(
    item: MenuItemEntity,
    onCustomizeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (item.description.isNotBlank()) {
                    Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$${String.format("%.2f", item.price)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onCustomizeClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Elegir y Personalizar")
            }
        }
    }
}
