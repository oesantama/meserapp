package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.viewmodel.AppViewModel
import com.example.data.TableEntity
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                var scannedTable by remember { mutableStateOf<TableEntity?>(null) }

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { role ->
                                when (role) {
                                    "Admin" -> navController.navigate("admin_dashboard") { popUpTo("login") { inclusive = true } }
                                    "Waiter" -> navController.navigate("waiter_dashboard") { popUpTo("login") { inclusive = true } }
                                    else -> navController.navigate("customer_order") { popUpTo("login") { inclusive = true } }
                                }
                            }
                        )
                    }

                    composable("admin_dashboard") {
                        AdminDashboardScreen(
                            viewModel = viewModel,
                            onNavigate = { route -> navController.navigate(route) },
                            onLogout = {
                                viewModel.logout()
                                navController.navigate("login") { popUpTo("admin_dashboard") { inclusive = true } }
                            }
                        )
                    }

                    composable("waiter_dashboard") {
                        WaiterDashboardScreen(
                            viewModel = viewModel,
                            onLogout = {
                                viewModel.logout()
                                navController.navigate("login") { popUpTo("waiter_dashboard") { inclusive = true } }
                            }
                        )
                    }

                    composable("customer_order") {
                        CustomerOrderScreen(
                            viewModel = viewModel,
                            selectedTable = scannedTable,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("menu_manager") {
                        MenuManagerScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("table_qr") {
                        TableQrScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onSimulateCustomerScan = { table ->
                                scannedTable = table
                                navController.navigate("customer_order")
                            }
                        )
                    }

                    composable("sync_server") {
                        SyncServerScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("audit_logs") {
                        AuditLogsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("staff_management") {
                        StaffManagementScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
