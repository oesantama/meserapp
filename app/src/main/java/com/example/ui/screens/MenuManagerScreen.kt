package com.example.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManagerScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val currentBusiness by viewModel.currentBusiness.collectAsState()
    val menuDays by viewModel.getMenuDaysFlow(currentBusiness?.id ?: 1L).collectAsState(initial = emptyList())
    val currentMenuId by viewModel.currentMenuId.collectAsState()

    val menuItems by viewModel.getMenuItemsFlow(currentMenuId).collectAsState(initial = emptyList())

    var showAddMenuDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Menú del Día y Artículos") },
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddItemDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Añadir Plato / Opción") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Select Menu Day
            Text("Seleccione Día del Menú:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (menuDays.isEmpty()) {
                Button(onClick = {
                    currentBusiness?.id?.let { bId ->
                        viewModel.addMenuDay(bId, "Menú del Día - Hoy", "Opciones de almuerzo y platos principales")
                    }
                }) {
                    Text("Crear Menú Inicial para Hoy")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    menuDays.forEach { m ->
                        FilterChip(
                            selected = m.id == currentMenuId,
                            onClick = { viewModel.setCurrentMenuId(m.id) },
                            label = { Text(m.dayName) },
                            leadingIcon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Platos y Opciones Personalizables",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showAddMenuDialog = true }) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Día")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (menuItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No hay platos en este menú. Añada opciones como 'Arroz con frijol', 'Con huevo', etc.")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(menuItems) { item ->
                        MenuItemCard(item)
                    }
                }
            }
        }
    }

    if (showAddMenuDialog) {
        AddMenuDayDialog(
            onDismiss = { showAddMenuDialog = false },
            onSave = { dayName, desc ->
                currentBusiness?.id?.let { bId ->
                    viewModel.addMenuDay(bId, dayName, desc)
                }
                showAddMenuDialog = false
            }
        )
    }

    if (showAddItemDialog) {
        AddMenuItemDialog(
            onDismiss = { showAddItemDialog = false },
            onSave = { category, name, price, desc, options ->
                currentBusiness?.id?.let { bId ->
                    viewModel.addMenuItem(currentMenuId, bId, category, name, price, desc, options)
                }
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun MenuItemCard(item: MenuItemEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "$${String.format("%.2f", item.price)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Categoría: ${item.category}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Opciones personalizables: ${item.customizationOptions.replace("|", ", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddMenuDayDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var dayName by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nuevo Menú (Día)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dayName,
                    onValueChange = { dayName = it },
                    label = { Text("Nombre (ej. Menú Martes, Especial Fin de Semana)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (dayName.isNotBlank()) onSave(dayName, desc) }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun AddMenuItemDialog(onDismiss: () -> Unit, onSave: (String, String, Double, String, String) -> Unit) {
    var category by remember { mutableStateOf("Plato Principal") }
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("8.50") }
    var desc by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("Arroz con frijol|Solo frijol|Con huevo|Sin huevo|Con ensalada") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Plato u Opción al Menú") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría (Plato Principal, Bebida, Acompañamiento)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Plato (ej. Bandeja Paisa o Arroz con Pollo)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Precio ($)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción (ej. Ingredientes frescos)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = options,
                    onValueChange = { options = it },
                    label = { Text("Opciones separadas por | (ej. Con arroz|Solo frijol|Con huevo)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = priceStr.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) onSave(category, name, p, desc, options)
            }) { Text("Añadir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
