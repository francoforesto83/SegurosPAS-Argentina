package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val alertas by viewModel.alertas.collectAsStateWithLifecycle()
                val pendingAlertsCount = remember(alertas) { alertas.count { !it.revisada } }
                val config by viewModel.configuracion.collectAsStateWithLifecycle(null)

                // Handle system back navigation cleanly
                BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                    currentScreen = AppScreen.DASHBOARD
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(300.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            // Drawer Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(24.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = config?.nombreOficina ?: "SegurosPAS Argentina",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${config?.nombreProductor ?: "Productor Asesor"} • ${config?.matriculaSSN ?: "PAS-89240"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Navigation Items
                            AppScreen.values().forEach { screen ->
                                NavigationDrawerItem(
                                    label = { Text(screen.title, fontWeight = FontWeight.Medium) },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (screen == AppScreen.VENCIMIENTOS && pendingAlertsCount > 0) {
                                                    Badge(containerColor = Red600) {
                                                        Text(pendingAlertsCount.toString())
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = screen.icon, contentDescription = null)
                                        }
                                    },
                                    selected = currentScreen == screen,
                                    onClick = {
                                        currentScreen = screen
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier
                                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                                        .testTag("nav_item_${screen.name.lowercase()}"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(Modifier.weight(1f))

                            Divider(color = Slate200, modifier = Modifier.padding(horizontal = 16.dp))
                            Text(
                                text = "Versión 1.0 • Gestión PAS Argentina",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = currentScreen.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier.testTag("open_drawer_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menú")
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { currentScreen = AppScreen.VENCIMIENTOS },
                                        modifier = Modifier.testTag("top_bell_button")
                                    ) {
                                        BadgedBox(
                                            badge = {
                                                if (pendingAlertsCount > 0) {
                                                    Badge(containerColor = Red600) {
                                                        Text(pendingAlertsCount.toString())
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Alertas"
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                val bottomDestinations = listOf(
                                    AppScreen.DASHBOARD,
                                    AppScreen.POLIZAS,
                                    AppScreen.VENCIMIENTOS,
                                    AppScreen.CLIENTES,
                                    AppScreen.SINIESTROS
                                )
                                bottomDestinations.forEach { screen ->
                                    NavigationBarItem(
                                        selected = currentScreen == screen,
                                        onClick = { currentScreen = screen },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (screen == AppScreen.VENCIMIENTOS && pendingAlertsCount > 0) {
                                                        Badge(containerColor = Red600) {
                                                            Text(pendingAlertsCount.toString())
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(imageVector = screen.icon, contentDescription = screen.title)
                                            }
                                        },
                                        label = { Text(screen.title) },
                                        modifier = Modifier.testTag("bottom_nav_${screen.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        val contentModifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)

                        when (currentScreen) {
                            AppScreen.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateTo = { currentScreen = it },
                                modifier = contentModifier
                            )
                            AppScreen.CLIENTES -> ClientesScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            AppScreen.VEHICULOS -> VehiculosScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            AppScreen.POLIZAS -> PolizasScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            AppScreen.VENCIMIENTOS -> VencimientosScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            AppScreen.SINIESTROS -> SiniestrosScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            AppScreen.ASEGURADORAS -> AseguradorasScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            AppScreen.EXPORTAR -> ExportarInformesScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            AppScreen.BACKUP -> BackupScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                            AppScreen.CONFIGURACION -> ConfiguracionScreen(
                                viewModel = viewModel,
                                modifier = contentModifier
                            )
                        }
                    }
                }
            }
        }
    }
}
