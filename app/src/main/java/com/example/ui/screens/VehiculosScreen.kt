package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.utils.ArgentinaFormats
import com.example.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val vehiculos by viewModel.vehiculos.collectAsStateWithLifecycle(emptyList())
    val clientes by viewModel.clientes.collectAsStateWithLifecycle(emptyList())
    val polizas by viewModel.polizas.collectAsStateWithLifecycle(emptyList())
    val siniestros by viewModel.siniestros.collectAsStateWithLifecycle(emptyList())

    val searchQuery by viewModel.searchVehiculos.collectAsStateWithLifecycle()
    var selectedTipoFilter by remember { mutableStateOf("TODOS") }

    val clientesMap = remember(clientes) { clientes.associateBy { it.id } }

    var showFormDialog by remember { mutableStateOf(false) }
    var editingVehiculo by remember { mutableStateOf<Vehiculo?>(null) }
    var viewingVehiculo by remember { mutableStateOf<Vehiculo?>(null) }
    var vehiculoToDelete by remember { mutableStateOf<Vehiculo?>(null) }
    var duplicateDominioWarning by remember { mutableStateOf<String?>(null) }

    val filteredVehiculos = remember(vehiculos, searchQuery, selectedTipoFilter, clientesMap) {
        vehiculos.filter { v ->
            val cli = clientesMap[v.clienteId]
            v.activo &&
            (selectedTipoFilter == "TODOS" || v.tipoVehiculo.equals(selectedTipoFilter, ignoreCase = true)) &&
            (searchQuery.isBlank() ||
             v.dominio.contains(searchQuery, ignoreCase = true) ||
             v.marca.contains(searchQuery, ignoreCase = true) ||
             v.modelo.contains(searchQuery, ignoreCase = true) ||
             (cli?.nombre?.contains(searchQuery, ignoreCase = true) == true) ||
             v.conductorHabitual.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("vehiculos_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingVehiculo = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_vehicle_fab")
            ) {
                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = "Registrar Vehículo")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            SearchBarM3(
                query = searchQuery,
                onQueryChange = { viewModel.searchVehiculos.value = it },
                placeholder = "Buscar por patente, marca, modelo o titular..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val types = listOf("TODOS", "Automóvil", "SUV", "Pick-up", "Utilitario", "Motocicleta", "Camión")
                items(types) { t ->
                    FilterChip(
                        selected = selectedTipoFilter == t,
                        onClick = { selectedTipoFilter = t },
                        label = { Text(if (t == "TODOS") "Todos (${vehiculos.count { it.activo }})" else t) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredVehiculos.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.DirectionsCar,
                    title = "No se encontraron vehículos",
                    message = if (searchQuery.isNotEmpty()) "No hay resultados para '$searchQuery'." else "No hay vehículos asegurados registrados.",
                    actionButtonText = if (searchQuery.isEmpty()) "+ Registrar primer vehículo" else null,
                    onActionClick = {
                        editingVehiculo = null
                        showFormDialog = true
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredVehiculos, key = { it.id }) { vehiculo ->
                        val titular = clientesMap[vehiculo.clienteId]
                        val vPolizas = polizas.filter { it.vehiculoId == vehiculo.id && it.activo }
                        val vSiniestros = siniestros.filter { it.vehiculoId == vehiculo.id && it.activo }

                        val daysCarnet = DateUtils.daysUntil(vehiculo.vencimientoCarnet)
                        val daysVtv = DateUtils.daysUntil(vehiculo.vencimientoVtv)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingVehiculo = vehiculo }
                                .testTag("vehicle_card_${vehiculo.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Argentine License Plate Pill
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Slate900,
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = ArgentinaFormats.formatPatente(vehiculo.dominio),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${vehiculo.marca} ${vehiculo.modelo} (${vehiculo.anio})",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Titular: ${titular?.nombre ?: "ID #${vehiculo.clienteId}"} • ${vehiculo.tipoVehiculo}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            editingVehiculo = vehiculo
                                            showFormDialog = true
                                        },
                                        modifier = Modifier.testTag("edit_vehicle_${vehiculo.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(
                                        onClick = { vehiculoToDelete = vehiculo },
                                        modifier = Modifier.testTag("delete_vehicle_${vehiculo.id}")
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Baja", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Expiration alerts pill row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (daysVtv != null) {
                                        val vtvStatus = when {
                                            daysVtv < 0 -> StatusType.DANGER
                                            daysVtv <= 15 -> StatusType.WARNING
                                            else -> StatusType.SUCCESS
                                        }
                                        StatusBadge(
                                            text = "VTV: ${DateUtils.describeExpiry(daysVtv)}",
                                            statusType = vtvStatus
                                        )
                                    }

                                    if (daysCarnet != null) {
                                        val carnetStatus = when {
                                            daysCarnet < 0 -> StatusType.DANGER
                                            daysCarnet <= 15 -> StatusType.WARNING
                                            else -> StatusType.INFO
                                        }
                                        StatusBadge(
                                            text = "Carnet: ${DateUtils.describeExpiry(daysCarnet)}",
                                            statusType = carnetStatus
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Conductor: ${vehiculo.conductorHabitual.ifBlank { titular?.nombre ?: "-" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        AssistChip(
                                            onClick = { viewingVehiculo = vehiculo },
                                            label = { Text("${vPolizas.size} pólizas") }
                                        )
                                        if (vSiniestros.isNotEmpty()) {
                                            AssistChip(
                                                onClick = { viewingVehiculo = vehiculo },
                                                label = { Text("${vSiniestros.size} siniestros") },
                                                leadingIcon = { Icon(Icons.Default.Warning, null, Modifier.size(14.dp), tint = Amber600) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Vehicle Detail Sheet Dialog
    if (viewingVehiculo != null) {
        val veh = viewingVehiculo!!
        val titular = clientesMap[veh.clienteId]
        val vPolizas = polizas.filter { it.vehiculoId == veh.id && it.activo }
        val vSiniestros = siniestros.filter { it.vehiculoId == veh.id && it.activo }

        Dialog(onDismissRequest = { viewingVehiculo = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ficha del Vehículo",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { viewingVehiculo = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate900
                    ) {
                        Text(
                            text = ArgentinaFormats.formatPatente(veh.dominio),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${veh.marca} ${veh.modelo} (${veh.anio})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tipo: ${veh.tipoVehiculo} • Uso: ${veh.uso}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate700
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Vehicle Technical Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("👤 Titular: ${titular?.nombre ?: "ID #${veh.clienteId}"}", fontWeight = FontWeight.Bold)
                            Text("🚘 Conductor Habitual: ${veh.conductorHabitual.ifBlank { titular?.nombre ?: "-" }}")
                            Text("📄 N° Chasis: ${veh.numeroChasis.ifBlank { "-" }}")
                            Text("⚙️ N° Motor: ${veh.numeroMotor.ifBlank { "-" }}")
                            Text("🗓️ Vto. VTV/RTO: ${DateUtils.toArgFormat(veh.vencimientoVtv)} (${DateUtils.describeExpiry(DateUtils.daysUntil(veh.vencimientoVtv))})")
                            Text("🪪 Vto. Carnet Conductor: ${DateUtils.toArgFormat(veh.vencimientoCarnet)} (${DateUtils.describeExpiry(DateUtils.daysUntil(veh.vencimientoCarnet))})")
                            if (veh.observaciones.isNotBlank()) {
                                Text("📝 Observaciones: ${veh.observaciones}")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Associated Policies
                    Text("Pólizas Asociadas (${vPolizas.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (vPolizas.isEmpty()) {
                        Text("No registra pólizas activas actualmente.", color = Slate500, style = MaterialTheme.typography.bodySmall)
                    } else {
                        vPolizas.forEach { p ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate100),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Póliza: ${p.numeroPoliza}", fontWeight = FontWeight.Bold)
                                        Text(p.estadoAdministrativo, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Cobertura: ${p.tipoCobertura} • Ramo: ${p.ramo}", style = MaterialTheme.typography.bodySmall)
                                    Text("Vigencia: ${DateUtils.toArgFormat(p.fechaInicio)} al ${DateUtils.toArgFormat(p.fechaFin)}", style = MaterialTheme.typography.bodySmall)
                                    Text("Premio: ${ArgentinaFormats.formatCurrency(p.premioTotal)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Associated Claims
                    if (vSiniestros.isNotEmpty()) {
                        Text("Historial de Siniestros (${vSiniestros.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        vSiniestros.forEach { s ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Amber100.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("${s.numeroInterno} - ${s.tipoSiniestro}", fontWeight = FontWeight.Bold)
                                    Text("Estado: ${s.estado} • Fecha: ${DateUtils.toArgFormat(s.fechaSiniestro)}", style = MaterialTheme.typography.bodySmall)
                                    Text(s.danos, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = {
                            editingVehiculo = veh
                            viewingVehiculo = null
                            showFormDialog = true
                        }) {
                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Editar")
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Form Dialog
    if (showFormDialog) {
        var dominio by remember { mutableStateOf(editingVehiculo?.dominio ?: "") }
        var marca by remember { mutableStateOf(editingVehiculo?.marca ?: "") }
        var modelo by remember { mutableStateOf(editingVehiculo?.modelo ?: "") }
        var anioStr by remember { mutableStateOf(editingVehiculo?.anio?.toString() ?: "2023") }
        var tipoVehiculo by remember { mutableStateOf(editingVehiculo?.tipoVehiculo ?: "Automóvil") }
        var selectedClienteId by remember { mutableStateOf(editingVehiculo?.clienteId ?: (clientes.firstOrNull()?.id ?: 0L)) }
        var uso by remember { mutableStateOf(editingVehiculo?.uso ?: "Particular") }
        var conductorHabitual by remember { mutableStateOf(editingVehiculo?.conductorHabitual ?: "") }
        var vtoCarnet by remember { mutableStateOf(editingVehiculo?.vencimientoCarnet ?: "") }
        var vtoVtv by remember { mutableStateOf(editingVehiculo?.vencimientoVtv ?: "") }
        var chasis by remember { mutableStateOf(editingVehiculo?.numeroChasis ?: "") }
        var motor by remember { mutableStateOf(editingVehiculo?.numeroMotor ?: "") }
        var observaciones by remember { mutableStateOf(editingVehiculo?.observaciones ?: "") }

        var validationError by remember { mutableStateOf<String?>(null) }
        var clientDropdownExpanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showFormDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (editingVehiculo == null) "Nuevo Vehículo" else "Modificar Vehículo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Dominio / Patente Input
                    OutlinedTextField(
                        value = dominio,
                        onValueChange = { dominio = it.uppercase() },
                        label = { Text("Patente / Dominio * (ej. AF 123 CD o AAA 123)") },
                        modifier = Modifier.fillMaxWidth().testTag("vehicle_domain_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Client Titular dropdown selector
                    val currentClient = clientesMap[selectedClienteId]
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentClient?.nombre ?: "Seleccionar titular...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cliente Titular *") },
                            trailingIcon = {
                                IconButton(onClick = { clientDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { clientDropdownExpanded = true }
                                .testTag("select_client_dropdown")
                        )

                        DropdownMenu(
                            expanded = clientDropdownExpanded,
                            onDismissRequest = { clientDropdownExpanded = false }
                        ) {
                            clientes.filter { it.activo }.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.nombre} (${c.documentoNumero})") },
                                    onClick = {
                                        selectedClienteId = c.id
                                        clientDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = marca,
                            onValueChange = { marca = it },
                            label = { Text("Marca * (Toyota, Ford...)") },
                            modifier = Modifier.weight(1f).testTag("vehicle_brand_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = modelo,
                            onValueChange = { modelo = it },
                            label = { Text("Modelo *") },
                            modifier = Modifier.weight(1f).testTag("vehicle_model_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = anioStr,
                            onValueChange = { anioStr = it },
                            label = { Text("Año") },
                            modifier = Modifier.width(110.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tipoVehiculo,
                            onValueChange = { tipoVehiculo = it },
                            label = { Text("Tipo (Auto, SUV...)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uso,
                            onValueChange = { uso = it },
                            label = { Text("Uso (Particular, Comercial...)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = conductorHabitual,
                            onValueChange = { conductorHabitual = it },
                            label = { Text("Conductor habitual") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vtoCarnet,
                            onValueChange = { vtoCarnet = it },
                            label = { Text("Vto. Carnet (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f).testTag("vehicle_carnet_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = vtoVtv,
                            onValueChange = { vtoVtv = it },
                            label = { Text("Vto. VTV/RTO (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f).testTag("vehicle_vtv_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = chasis,
                            onValueChange = { chasis = it },
                            label = { Text("N° Chasis") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = motor,
                            onValueChange = { motor = it },
                            label = { Text("N° Motor") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = validationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showFormDialog = false }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (dominio.isBlank() || marca.isBlank() || modelo.isBlank()) {
                                    validationError = "Por favor completá los campos obligatorios (Patente, Marca y Modelo)."
                                    return@Button
                                }
                                val toSave = Vehiculo(
                                    id = editingVehiculo?.id ?: 0,
                                    clienteId = selectedClienteId,
                                    dominio = dominio.trim(),
                                    marca = marca.trim(),
                                    modelo = modelo.trim(),
                                    anio = anioStr.toIntOrNull() ?: 2023,
                                    tipoVehiculo = tipoVehiculo.trim(),
                                    numeroChasis = chasis.trim(),
                                    numeroMotor = motor.trim(),
                                    uso = uso.trim(),
                                    conductorHabitual = conductorHabitual.trim(),
                                    vencimientoCarnet = DateUtils.toIsoFormat(vtoCarnet.trim()),
                                    vencimientoVtv = DateUtils.toIsoFormat(vtoVtv.trim()),
                                    observaciones = observaciones.trim(),
                                    activo = true
                                )
                                viewModel.saveVehiculo(
                                    vehiculo = toSave,
                                    onDuplicateDominio = { dup ->
                                        duplicateDominioWarning = "Advertencia: Ya existe registrado un vehículo con la patente ${dup.dominio} (${dup.marca} ${dup.modelo})."
                                    },
                                    onSaved = {
                                        showFormDialog = false
                                    }
                                )
                            },
                            modifier = Modifier.testTag("save_vehicle_button")
                        ) {
                            Text("Guardar Vehículo")
                        }
                    }
                }
            }
        }
    }

    // Duplicate Patente Warning Dialog
    if (duplicateDominioWarning != null) {
        AlertDialog(
            onDismissRequest = { duplicateDominioWarning = null },
            icon = { Icon(Icons.Default.Warning, null, tint = Amber600) },
            title = { Text("Patente Duplicada") },
            text = { Text(duplicateDominioWarning!!) },
            confirmButton = {
                Button(onClick = { duplicateDominioWarning = null }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Confirm Delete Dialog
    if (vehiculoToDelete != null) {
        ConfirmDeleteDialog(
            title = "¿Dar de baja al vehículo?",
            message = "Se dará de baja el vehículo con patente '${vehiculoToDelete?.dominio}'. Esta acción conserva el historial para auditoría y siniestros.",
            onConfirm = {
                vehiculoToDelete?.let { viewModel.bajaVehiculo(it.id) }
                vehiculoToDelete = null
            },
            onDismiss = { vehiculoToDelete = null }
        )
    }
}
