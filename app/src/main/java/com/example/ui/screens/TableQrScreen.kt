package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.viewmodel.AppViewModel
import com.example.data.TableEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableQrScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onSimulateCustomerScan: (TableEntity) -> Unit
) {
    val currentBusiness by viewModel.currentBusiness.collectAsState()
    val tables by viewModel.getTablesFlow(currentBusiness?.id ?: 1L).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesas y Códigos QR") },
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
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Mesa")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Escanee o seleccione la mesa para ordenar sin esperar al mesero:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (tables.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay mesas registradas. Pulse el botón + para añadir.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tables) { table ->
                        TableCard(
                            table = table,
                            onScanClick = { onSimulateCustomerScan(table) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var tableNumStr by remember { mutableStateOf("${tables.size + 1}") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Registrar Nueva Mesa") },
            text = {
                OutlinedTextField(
                    value = tableNumStr,
                    onValueChange = { tableNumStr = it },
                    label = { Text("Número de Mesa") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val num = tableNumStr.toIntOrNull() ?: (tables.size + 1)
                    currentBusiness?.id?.let { bId ->
                        viewModel.addTable(bId, num)
                    }
                    showAddDialog = false
                }) { Text("Añadir") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun TableCard(
    table: TableEntity,
    onScanClick: () -> Unit
) {
    val isOccupied = table.status == "Occupied"
    val containerColor = if (isOccupied) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isOccupied) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mesa #${table.tableNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = if (isOccupied) "Ocupada / Pedido Activo" else "Disponible",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Simular QR / Pedir", fontSize = 12.sp)
            }
        }
    }
}
