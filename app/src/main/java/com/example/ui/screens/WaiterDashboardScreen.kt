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
import com.example.data.OrderEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiterDashboardScreen(
    viewModel: AppViewModel,
    onLogout: () -> Unit
) {
    val currentBusiness by viewModel.currentBusiness.collectAsState()
    val orders by viewModel.getOrdersFlow(currentBusiness?.id ?: 1L).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Meseros & Personal - ${currentBusiness?.name ?: ""}") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión")
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
            Text(
                text = "Pedidos en tiempo real (Mesas QR y Mostrador):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay pedidos activos en este momento.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(orders) { order ->
                        WaiterOrderCard(
                            order = order,
                            onUpdateStatus = { newStatus ->
                                currentBusiness?.id?.let { bId ->
                                    viewModel.updateOrderStatus(order.id, newStatus, bId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WaiterOrderCard(
    order: OrderEntity,
    onUpdateStatus: (String) -> Unit
) {
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
                Text(
                    text = "Mesa #${order.tableNumber ?: "N/A"} - ${order.customerName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (order.status) {
                        "Pending" -> MaterialTheme.colorScheme.errorContainer
                        "Preparing" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = " Estado: ${order.status} ",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Ítems: ${order.itemsSummary}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Total: $${String.format("%.2f", order.totalAmount)} | Tipo: ${order.orderType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { onUpdateStatus("Preparing") }, modifier = Modifier.weight(1f)) {
                    Text("Preparando")
                }
                OutlinedButton(onClick = { onUpdateStatus("Ready") }, modifier = Modifier.weight(1f)) {
                    Text("Listo")
                }
                Button(onClick = { onUpdateStatus("Delivered") }, modifier = Modifier.weight(1f)) {
                    Text("Entregado")
                }
            }
        }
    }
}
