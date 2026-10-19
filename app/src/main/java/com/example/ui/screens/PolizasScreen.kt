package com.example.ui.screens

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
fun PolizasScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val polizas by viewModel.polizas.collectAsStateWithLifecycle(emptyList())
    val clientes by viewModel.clientes.collectAsStateWithLifecycle(emptyList())
    val companias by viewModel.companias.collectAsStateWithLifecycle(emptyList())
    val vehiculos by viewModel.vehiculos.collectAsStateWithLifecycle(emptyList())

    val searchQuery by viewModel.searchPolizas.collectAsStateWithLifecycle()
    var selectedRamoFilter by remember { mutableStateOf("TODOS") }
    var selectedEstadoFilter by remember { mutableStateOf("TODOS") }

    val clientesMap = remember(clientes) { clientes.associateBy { it.id } }
    val companiasMap = remember(companias) { companias.associateBy { it.id } }
    val vehiculosMap = remember(vehiculos) { vehiculos.associateBy { it.id } }

    var showFormDialog by remember { mutableStateOf(false) }
    var editingPoliza by remember { mutableStateOf<Poliza?>(null) }
    var renewingPoliza by remember { mutableStateOf<Poliza?>(null) }
    var polizaToDelete by remember { mutableStateOf<Poliza?>(null) }

    val filteredPolizas = remember(polizas, searchQuery, selectedRamoFilter, selectedEstadoFilter, clientesMap, companiasMap, vehiculosMap) {
        polizas.filter { p ->
            val cli = clientesMap[p.clienteId]
            val comp = companiasMap[p.companiaId]
            val veh = p.vehiculoId?.let { vehiculosMap[it] }

            p.activo &&
            (selectedRamoFilter == "TODOS" || p.ramo.equals(selectedRamoFilter, ignoreCase = true)) &&
            (selectedEstadoFilter == "TODOS" || p.estadoAdministrativo.equals(selectedEstadoFilter, ignoreCase = true)) &&
            (searchQuery.isBlank() ||
             p.numeroPoliza.contains(searchQuery, ignoreCase = true) ||
             (cli?.nombre?.contains(searchQuery, ignoreCase = true) == true) ||
             (comp?.nombre?.contains(searchQuery, ignoreCase = true) == true) ||
             (veh?.dominio?.contains(searchQuery, ignoreCase = true) == true) ||
             p.tipoCobertura.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("polizas_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingPoliza = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_policy_fab")
            ) {
                Icon(imageVector = Icons.Default.NoteAdd, contentDescription = "Nueva Póliza")
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
                onQueryChange = { viewModel.searchPolizas.value = it },
                placeholder = "Buscar póliza, cliente, patente o aseguradora..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ramo Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val ramos = listOf("TODOS", "Automotor", "Hogar", "Integral de Comercio", "Vida", "Accidentes Personales", "Responsabilidad Civil")
                items(ramos) { r ->
                    FilterChip(
                        selected = selectedRamoFilter == r,
                        onClick = { selectedRamoFilter = r },
                        label = { Text(if (r == "TODOS") "Todos los ramos" else r) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Estado Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val estados = listOf("TODOS", "Vigente", "Próxima a vencer", "Vencida", "Renovada", "Cotización", "Anulada")
                items(estados) { est ->
                    FilterChip(
                        selected = selectedEstadoFilter == est,
                        onClick = { selectedEstadoFilter = est },
                        label = { Text(if (est == "TODOS") "Todos los estados" else est) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (est) {
                                "Vencida" -> Red100
                                "Próxima a vencer" -> Amber100
                                "Vigente" -> Green100
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredPolizas.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Description,
                    title = "No se encontraron pólizas",
                    message = if (searchQuery.isNotEmpty()) "No hay resultados para los filtros seleccionados." else "No hay pólizas registradas aún.",
                    actionButtonText = if (searchQuery.isEmpty()) "+ Emitir nueva póliza" else null,
                    onActionClick = {
                        editingPoliza = null
                        showFormDialog = true
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPolizas, key = { it.id }) { poliza ->
                        val titular = clientesMap[poliza.clienteId]
                        val compania = companiasMap[poliza.companiaId]
                        val vehiculo = poliza.vehiculoId?.let { vehiculosMap[it] }
                        val days = DateUtils.daysUntil(poliza.fechaFin)

                        val statusType = when (poliza.estadoAdministrativo) {
                            "Vencida" -> StatusType.DANGER
                            "Próxima a vencer" -> StatusType.WARNING
                            "Vigente" -> StatusType.SUCCESS
                            "Renovada" -> StatusType.INFO
                            else -> StatusType.NEUTRAL
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("policy_card_${poliza.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Póliza ${poliza.numeroPoliza}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (poliza.endoso.isNotBlank() && poliza.endoso != "0") {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Slate200
                                                ) {
                                                    Text(
                                                        "Endoso ${poliza.endoso}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${compania?.nombre ?: "Aseguradora"} • ${poliza.ramo}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    StatusBadge(
                                        text = poliza.estadoAdministrativo,
                                        statusType = statusType
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Details box
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Slate100,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("👤 Asegurado: ${titular?.nombre ?: "ID #${poliza.clienteId}"} (${titular?.documentoNumero ?: ""})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        if (vehiculo != null) {
                                            Text("🚘 Vehículo: ${vehiculo.dominio} - ${vehiculo.marca} ${vehiculo.modelo} (${vehiculo.anio})", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text("🛡️ Cobertura: ${poliza.tipoCobertura}", style = MaterialTheme.typography.bodySmall)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🗓️ Vigencia: ${DateUtils.toArgFormat(poliza.fechaInicio)} al ${DateUtils.toArgFormat(poliza.fechaFin)}", style = MaterialTheme.typography.bodySmall)
                                            Text(DateUtils.describeExpiry(days), style = MaterialTheme.typography.labelSmall, color = if (days != null && days < 0) Red600 else Slate700, fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("💰 Premio Total: ${ArgentinaFormats.formatCurrency(poliza.premioTotal)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text("${poliza.cantidadCuotas} cuotas (${poliza.estadoPago})", style = MaterialTheme.typography.bodySmall, color = Slate700)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Renewal Action
                                    Button(
                                        onClick = { renewingPoliza = poliza },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                        modifier = Modifier.testTag("renew_policy_${poliza.id}")
                                    ) {
                                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Renovar Póliza")
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingPoliza = poliza
                                                showFormDialog = true
                                            },
                                            modifier = Modifier.testTag("edit_policy_${poliza.id}")
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                        }

                                        IconButton(
                                            onClick = { polizaToDelete = poliza },
                                            modifier = Modifier.testTag("delete_policy_${poliza.id}")
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Baja", tint = MaterialTheme.colorScheme.error)
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

    // Renewal Flow Dialog
    if (renewingPoliza != null) {
        val old = renewingPoliza!!
        var nuevoNumero by remember { mutableStateOf(old.numeroPoliza) }
        var nuevoInicio by remember { mutableStateOf(old.fechaFin) }
        var nuevoFin by remember { mutableStateOf(DateUtils.addOneYear(old.fechaFin)) }
        var nuevoPremioStr by remember { mutableStateOf(old.premioTotal.toInt().toString()) }
        var cuotasStr by remember { mutableStateOf(old.cantidadCuotas.toString()) }
        var formaPago by remember { mutableStateOf(old.formaPago) }
        var obsRenovacion by remember { mutableStateOf("Renovación de póliza ${old.numeroPoliza}. Período anterior cumplido.") }

        Dialog(onDismissRequest = { renewingPoliza = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Autorenew, null, tint = Navy800, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Renovar Póliza",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Se creará un nuevo período de vigencia vinculado a la póliza anterior, conservando todo el historial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nuevoNumero,
                        onValueChange = { nuevoNumero = it },
                        label = { Text("Número de la Nueva Póliza *") },
                        modifier = Modifier.fillMaxWidth().testTag("renew_number_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nuevoInicio,
                            onValueChange = { nuevoInicio = it },
                            label = { Text("Nuevo Inicio (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = nuevoFin,
                            onValueChange = { nuevoFin = it },
                            label = { Text("Nuevo Fin (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f).testTag("renew_end_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nuevoPremioStr,
                            onValueChange = { nuevoPremioStr = it },
                            label = { Text("Premio Total ($ ARS) *") },
                            modifier = Modifier.weight(1f).testTag("renew_premium_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cuotasStr,
                            onValueChange = { cuotasStr = it },
                            label = { Text("Cuotas") },
                            modifier = Modifier.width(90.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = formaPago,
                        onValueChange = { formaPago = it },
                        label = { Text("Forma de Pago") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = obsRenovacion,
                        onValueChange = { obsRenovacion = it },
                        label = { Text("Notas de Renovación") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { renewingPoliza = null }) {
                            Text("Cancelar")
                        }
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val premio = nuevoPremioStr.toDoubleOrNull() ?: old.premioTotal
                                val cuotas = cuotasStr.toIntOrNull() ?: 12
                                viewModel.renewPoliza(
                                    polizaAnterior = old,
                                    nuevoNumero = nuevoNumero.trim(),
                                    nuevoInicio = nuevoInicio.trim(),
                                    nuevoFin = nuevoFin.trim(),
                                    nuevoPremio = premio,
                                    nuevasCuotas = cuotas,
                                    nuevaFormaPago = formaPago.trim(),
                                    observaciones = obsRenovacion.trim(),
                                    onDone = {
                                        renewingPoliza = null
                                    }
                                )
                            },
                            modifier = Modifier.testTag("confirm_renewal_button")
                        ) {
                            Text("Confirmar Renovación")
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Form Dialog
    if (showFormDialog) {
        var numeroPoliza by remember { mutableStateOf(editingPoliza?.numeroPoliza ?: "") }
        var endoso by remember { mutableStateOf(editingPoliza?.endoso ?: "0") }
        var selectedClienteId by remember { mutableStateOf(editingPoliza?.clienteId ?: (clientes.firstOrNull()?.id ?: 0L)) }
        var selectedVehiculoId by remember { mutableStateOf(editingPoliza?.vehiculoId) }
        var selectedCompaniaId by remember { mutableStateOf(editingPoliza?.companiaId ?: (companias.firstOrNull()?.id ?: 1L)) }
        var ramo by remember { mutableStateOf(editingPoliza?.ramo ?: "Automotor") }
        var cobertura by remember { mutableStateOf(editingPoliza?.tipoCobertura ?: "Terceros Completo") }
        var fechaInicio by remember { mutableStateOf(editingPoliza?.fechaInicio ?: DateUtils.todayIso()) }
        var fechaFin by remember { mutableStateOf(editingPoliza?.fechaFin ?: DateUtils.addOneYear(DateUtils.todayIso())) }
        var premioStr by remember { mutableStateOf(editingPoliza?.premioTotal?.toInt()?.toString() ?: "120000") }
        var cuotasStr by remember { mutableStateOf(editingPoliza?.cantidadCuotas?.toString() ?: "12") }
        var formaPago by remember { mutableStateOf(editingPoliza?.formaPago ?: "Débito Automático CBU") }
        var estadoPago by remember { mutableStateOf(editingPoliza?.estadoPago ?: "Al Día") }
        var productor by remember { mutableStateOf(editingPoliza?.productor ?: "Franco Foresto") }
        var observaciones by remember { mutableStateOf(editingPoliza?.observaciones ?: "") }
        var estadoAdmin by remember { mutableStateOf(editingPoliza?.estadoAdministrativo ?: "Vigente") }

        var validationError by remember { mutableStateOf<String?>(null) }
        var clientMenuExpanded by remember { mutableStateOf(false) }
        var compMenuExpanded by remember { mutableStateOf(false) }
        var vehMenuExpanded by remember { mutableStateOf(false) }

        // Associated client vehicles
        val clientVehicles = remember(selectedClienteId, vehiculos) {
            vehiculos.filter { it.clienteId == selectedClienteId && it.activo }
        }

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
                        text = if (editingPoliza == null) "Nueva Póliza" else "Modificar Póliza",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = numeroPoliza,
                            onValueChange = { numeroPoliza = it },
                            label = { Text("Número de Póliza *") },
                            modifier = Modifier.weight(1f).testTag("policy_number_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endoso,
                            onValueChange = { endoso = it },
                            label = { Text("Endoso") },
                            modifier = Modifier.width(90.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Client Selector
                    val curCli = clientesMap[selectedClienteId]
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = curCli?.nombre ?: "Seleccionar Cliente Asegurado...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cliente Asegurado *") },
                            trailingIcon = {
                                IconButton(onClick = { clientMenuExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { clientMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = clientMenuExpanded,
                            onDismissRequest = { clientMenuExpanded = false }
                        ) {
                            clientes.filter { it.activo }.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.nombre} (${c.documentoNumero})") },
                                    onClick = {
                                        selectedClienteId = c.id
                                        selectedVehiculoId = null
                                        clientMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Insurer Selector
                    val curComp = companiasMap[selectedCompaniaId]
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = curComp?.nombre ?: "Seleccionar Compañía Aseguradora...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Compañía Aseguradora *") },
                            trailingIcon = {
                                IconButton(onClick = { compMenuExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { compMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = compMenuExpanded,
                            onDismissRequest = { compMenuExpanded = false }
                        ) {
                            companias.filter { it.activo }.forEach { comp ->
                                DropdownMenuItem(
                                    text = { Text(comp.nombre) },
                                    onClick = {
                                        selectedCompaniaId = comp.id
                                        compMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ramo,
                            onValueChange = { ramo = it },
                            label = { Text("Ramo (Automotor, Hogar...)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cobertura,
                            onValueChange = { cobertura = it },
                            label = { Text("Tipo Cobertura") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Vehicle selector if ramo is Automotor or has vehicles
                    if (ramo.contains("Auto", ignoreCase = true) || clientVehicles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val curVeh = selectedVehiculoId?.let { vehiculosMap[it] }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = curVeh?.let { "${it.dominio} (${it.marca} ${it.modelo})" } ?: "Ninguno / No corresponde",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Vehículo Asegurado") },
                                trailingIcon = {
                                    IconButton(onClick = { vehMenuExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { vehMenuExpanded = true }
                            )
                            DropdownMenu(
                                expanded = vehMenuExpanded,
                                onDismissRequest = { vehMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ninguno / Sin vehículo") },
                                    onClick = {
                                        selectedVehiculoId = null
                                        vehMenuExpanded = false
                                    }
                                )
                                clientVehicles.forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text("${v.dominio} - ${v.marca} ${v.modelo} (${v.anio})") },
                                        onClick = {
                                            selectedVehiculoId = v.id
                                            vehMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fechaInicio,
                            onValueChange = { fechaInicio = it },
                            label = { Text("Vigencia Desde (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = fechaFin,
                            onValueChange = { fechaFin = it },
                            label = { Text("Vigencia Hasta (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = premioStr,
                            onValueChange = { premioStr = it },
                            label = { Text("Premio Total ($ ARS) *") },
                            modifier = Modifier.weight(1f).testTag("policy_premium_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cuotasStr,
                            onValueChange = { cuotasStr = it },
                            label = { Text("Cuotas") },
                            modifier = Modifier.width(90.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formaPago,
                            onValueChange = { formaPago = it },
                            label = { Text("Forma de Pago") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = estadoPago,
                            onValueChange = { estadoPago = it },
                            label = { Text("Estado Pago") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = estadoAdmin,
                            onValueChange = { estadoAdmin = it },
                            label = { Text("Estado Administrativo") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = productor,
                            onValueChange = { productor = it },
                            label = { Text("Productor / Responsable") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones / Notas") },
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
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (numeroPoliza.isBlank()) {
                                    validationError = "Por favor ingresá el número de póliza."
                                    return@Button
                                }
                                val toSave = Poliza(
                                    id = editingPoliza?.id ?: 0,
                                    numeroPoliza = numeroPoliza.trim(),
                                    endoso = endoso.trim(),
                                    clienteId = selectedClienteId,
                                    vehiculoId = selectedVehiculoId,
                                    companiaId = selectedCompaniaId,
                                    ramo = ramo.trim(),
                                    tipoCobertura = cobertura.trim(),
                                    fechaEmision = fechaInicio.trim(),
                                    fechaInicio = DateUtils.toIsoFormat(fechaInicio.trim()),
                                    fechaFin = DateUtils.toIsoFormat(fechaFin.trim()),
                                    premioTotal = premioStr.toDoubleOrNull() ?: 0.0,
                                    formaPago = formaPago.trim(),
                                    cantidadCuotas = cuotasStr.toIntOrNull() ?: 12,
                                    estadoPago = estadoPago.trim(),
                                    productor = productor.trim(),
                                    observaciones = observaciones.trim(),
                                    estadoAdministrativo = estadoAdmin.trim(),
                                    activo = true
                                )
                                viewModel.savePoliza(toSave) {
                                    showFormDialog = false
                                }
                            },
                            modifier = Modifier.testTag("save_policy_button")
                        ) {
                            Text("Guardar Póliza")
                        }
                    }
                }
            }
        }
    }

    // Confirm Delete Dialog
    if (polizaToDelete != null) {
        ConfirmDeleteDialog(
            title = "¿Dar de baja a la póliza?",
            message = "Se dará de baja la póliza '${polizaToDelete?.numeroPoliza}'. Esta acción conserva los registros de pago y siniestros históricos.",
            onConfirm = {
                polizaToDelete?.let { viewModel.bajaPoliza(it.id) }
                polizaToDelete = null
            },
            onDismiss = { polizaToDelete = null }
        )
    }
}
