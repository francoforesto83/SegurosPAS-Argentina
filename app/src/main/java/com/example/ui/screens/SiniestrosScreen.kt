package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
fun SiniestrosScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val siniestros by viewModel.siniestros.collectAsStateWithLifecycle(emptyList())
    val clientes by viewModel.clientes.collectAsStateWithLifecycle(emptyList())
    val companias by viewModel.companias.collectAsStateWithLifecycle(emptyList())
    val vehiculos by viewModel.vehiculos.collectAsStateWithLifecycle(emptyList())
    val polizas by viewModel.polizas.collectAsStateWithLifecycle(emptyList())

    val searchQuery by viewModel.searchSiniestros.collectAsStateWithLifecycle()
    var selectedEstadoFilter by remember { mutableStateOf("TODOS") }

    val clientesMap = remember(clientes) { clientes.associateBy { it.id } }
    val companiasMap = remember(companias) { companias.associateBy { it.id } }
    val vehiculosMap = remember(vehiculos) { vehiculos.associateBy { it.id } }

    var showFormDialog by remember { mutableStateOf(false) }
    var editingSiniestro by remember { mutableStateOf<Siniestro?>(null) }
    var viewingSiniestro by remember { mutableStateOf<Siniestro?>(null) }
    var siniestroToDelete by remember { mutableStateOf<Siniestro?>(null) }

    val filteredSiniestros = remember(siniestros, searchQuery, selectedEstadoFilter, clientesMap, companiasMap, vehiculosMap) {
        siniestros.filter { s ->
            val cli = clientesMap[s.clienteId]
            val comp = companiasMap[s.companiaId]
            val veh = s.vehiculoId?.let { vehiculosMap[it] }

            s.activo &&
            (selectedEstadoFilter == "TODOS" || s.estado.equals(selectedEstadoFilter, ignoreCase = true)) &&
            (searchQuery.isBlank() ||
             s.numeroInterno.contains(searchQuery, ignoreCase = true) ||
             s.numeroDenuncia.contains(searchQuery, ignoreCase = true) ||
             (cli?.nombre?.contains(searchQuery, ignoreCase = true) == true) ||
             (comp?.nombre?.contains(searchQuery, ignoreCase = true) == true) ||
             (veh?.dominio?.contains(searchQuery, ignoreCase = true) == true) ||
             s.tipoSiniestro.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("siniestros_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingSiniestro = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_claim_fab")
            ) {
                Icon(imageVector = Icons.Default.AddAlert, contentDescription = "Nuevo Siniestro")
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
                onQueryChange = { viewModel.searchSiniestros.value = it },
                placeholder = "Buscar siniestro por número, cliente, aseguradora, patente..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Estado Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val estados = listOf(
                    "TODOS", "Nuevo", "Denunciado", "Documentación pendiente",
                    "En evaluación", "En reparación", "Pendiente de resolución", "Resuelto", "Cerrado", "Rechazado"
                )
                items(estados) { est ->
                    FilterChip(
                        selected = selectedEstadoFilter == est,
                        onClick = { selectedEstadoFilter = est },
                        label = { Text(if (est == "TODOS") "Todos (${siniestros.count { it.activo }})" else est) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredSiniestros.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CarCrash,
                    title = "No se encontraron siniestros",
                    message = if (searchQuery.isNotEmpty()) "No hay siniestros para '$searchQuery'." else "No hay siniestros registrados.",
                    actionButtonText = if (searchQuery.isEmpty()) "+ Cargar nuevo siniestro" else null,
                    onActionClick = {
                        editingSiniestro = null
                        showFormDialog = true
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSiniestros, key = { it.id }) { s ->
                        val titular = clientesMap[s.clienteId]
                        val comp = companiasMap[s.companiaId]
                        val veh = s.vehiculoId?.let { vehiculosMap[it] }
                        val daysSeguimiento = DateUtils.daysUntil(s.fechaProximoSeguimiento)

                        val statusType = when (s.estado) {
                            "Nuevo" -> StatusType.WARNING
                            "Denunciado", "En evaluación", "En reparación", "Pendiente de resolución" -> StatusType.INFO
                            "Resuelto", "Cerrado" -> StatusType.SUCCESS
                            "Rechazado" -> StatusType.DANGER
                            else -> StatusType.NEUTRAL
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingSiniestro = s }
                                .testTag("claim_card_${s.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Amber100),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.CarCrash, contentDescription = null, tint = Amber600)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${s.numeroInterno} • ${s.tipoSiniestro}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            StatusBadge(
                                                text = s.estado,
                                                statusType = statusType
                                            )
                                        }
                                        Text(
                                            text = "${titular?.nombre ?: "Cliente"} • ${comp?.nombre ?: "Aseguradora"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            editingSiniestro = s
                                            showFormDialog = true
                                        },
                                        modifier = Modifier.testTag("edit_claim_${s.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(
                                        onClick = { siniestroToDelete = s },
                                        modifier = Modifier.testTag("delete_claim_${s.id}")
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Baja", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Slate100,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (s.numeroDenuncia.isNotBlank()) {
                                            Text("🏷️ N° Denuncia Cía: ${s.numeroDenuncia}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                        if (veh != null) {
                                            Text("🚗 Vehículo: ${veh.dominio} (${veh.marca} ${veh.modelo})", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text("📅 Fecha hecho: ${DateUtils.toArgFormat(s.fechaSiniestro)} ${s.horaSiniestro} • ${s.lugar}", style = MaterialTheme.typography.bodySmall)
                                        Text("💥 Daños: ${s.danos.ifBlank { s.descripcion }}", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        if (s.importeEstimado > 0) {
                                            Text("💵 Importe Estimado: ${ArgentinaFormats.formatCurrency(s.importeEstimado)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        }
                                        if (daysSeguimiento != null) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("🔔 Próximo seguimiento: ${DateUtils.toArgFormat(s.fechaProximoSeguimiento)}", style = MaterialTheme.typography.bodySmall, color = if (daysSeguimiento <= 0) Red600 else Slate700, fontWeight = FontWeight.Bold)
                                                Text("Resp: ${s.responsable}", style = MaterialTheme.typography.bodySmall, color = Slate700)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { viewingSiniestro = s }) {
                                        Icon(Icons.Default.History, null, Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Ver gestiones y seguimiento")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Siniestro Detail & Gestiones Log Dialog
    if (viewingSiniestro != null) {
        val s = viewingSiniestro!!
        val titular = clientesMap[s.clienteId]
        val comp = companiasMap[s.companiaId]
        val veh = s.vehiculoId?.let { vehiculosMap[it] }

        val gestionesFlow = remember(s.id) { viewModel.getGestiones(s.id) }
        val gestiones by gestionesFlow.collectAsStateWithLifecycle(emptyList())

        var nuevaGestionDesc by remember { mutableStateOf("") }
        var nuevaGestionTipo by remember { mutableStateOf("Llamada") }

        Dialog(onDismissRequest = { viewingSiniestro = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Detalle del Siniestro", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { viewingSiniestro = null }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${s.numeroInterno} - ${s.tipoSiniestro}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Aseguradora: ${comp?.nombre ?: "-"} • Denuncia Cía: ${s.numeroDenuncia.ifBlank { "Pendiente" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fact Sheet Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("👤 Cliente: ${titular?.nombre ?: "ID #${s.clienteId}"} (${titular?.telefono ?: ""})", fontWeight = FontWeight.Bold)
                            if (veh != null) {
                                Text("🚘 Vehículo: ${veh.dominio} - ${veh.marca} ${veh.modelo}")
                            }
                            Text("📍 Lugar del hecho: ${s.lugar}")
                            Text("🗓️ Fecha y hora: ${DateUtils.toArgFormat(s.fechaSiniestro)} a las ${s.horaSiniestro} hs")
                            Text("📝 Relato: ${s.descripcion}")
                            if (s.datosTerceros.isNotBlank()) {
                                Text("👥 Terceros Involucrados: ${s.datosTerceros}")
                            }
                            if (s.danos.isNotBlank()) {
                                Text("💥 Daños verificados: ${s.danos}")
                            }
                            if (s.documentacionPendiente.isNotBlank()) {
                                Text("📑 Documentación Pendiente: ${s.documentacionPendiente}", color = Amber600, fontWeight = FontWeight.Bold)
                            }
                            Text("💵 Importe Estimado: ${ArgentinaFormats.formatCurrency(s.importeEstimado)}")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Follow-ups / Gestiones Section
                    Text("Gestiones y Seguimiento Histórico (${gestiones.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Add new follow-up row
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Registrar Nueva Gestión:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val tipos = listOf("Llamada", "WhatsApp", "Peritaje", "Presupuesto", "Liquidación")
                                tipos.forEach { t ->
                                    FilterChip(
                                        selected = nuevaGestionTipo == t,
                                        onClick = { nuevaGestionTipo = t },
                                        label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = nuevaGestionDesc,
                                onValueChange = { nuevaGestionDesc = it },
                                placeholder = { Text("Detalle de la gestión realizada...") },
                                modifier = Modifier.fillMaxWidth().testTag("add_gestion_input"),
                                singleLine = false,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (nuevaGestionDesc.isNotBlank()) {
                                        viewModel.addGestion(
                                            siniestroId = s.id,
                                            descripcion = nuevaGestionDesc.trim(),
                                            responsable = s.responsable,
                                            tipoGestion = nuevaGestionTipo
                                        )
                                        nuevaGestionDesc = ""
                                    }
                                },
                                modifier = Modifier.align(Alignment.End).testTag("save_gestion_button")
                            ) {
                                Text("Guardar Gestión")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timeline of gestiones
                    if (gestiones.isEmpty()) {
                        Text("No se han registrado gestiones aún.", color = Slate500, style = MaterialTheme.typography.bodySmall)
                    } else {
                        gestiones.forEach { g ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = when (g.tipoGestion) {
                                            "Llamada" -> Icons.Default.Call
                                            "WhatsApp" -> Icons.Default.Chat
                                            "Peritaje" -> Icons.Default.Search
                                            "Presupuesto" -> Icons.Default.Receipt
                                            else -> Icons.Default.Assignment
                                        },
                                        contentDescription = null,
                                        tint = Navy700,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(g.tipoGestion, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text(g.fecha, style = MaterialTheme.typography.labelSmall, color = Slate500)
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(g.descripcion, style = MaterialTheme.typography.bodySmall)
                                        Text("Responsable: ${g.responsable}", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Form Dialog
    if (showFormDialog) {
        var numInterno by remember { mutableStateOf(editingSiniestro?.numeroInterno ?: "SIN-${DateUtils.todayIso().replace("-", "")}-${(10..99).random()}") }
        var numDenuncia by remember { mutableStateOf(editingSiniestro?.numeroDenuncia ?: "") }
        var selectedClienteId by remember { mutableStateOf(editingSiniestro?.clienteId ?: (clientes.firstOrNull()?.id ?: 0L)) }
        var selectedVehiculoId by remember { mutableStateOf(editingSiniestro?.vehiculoId) }
        var selectedCompaniaId by remember { mutableStateOf(editingSiniestro?.companiaId ?: (companias.firstOrNull()?.id ?: 1L)) }
        var fechaSiniestro by remember { mutableStateOf(editingSiniestro?.fechaSiniestro ?: DateUtils.todayIso()) }
        var horaSiniestro by remember { mutableStateOf(editingSiniestro?.horaSiniestro ?: "12:00") }
        var lugar by remember { mutableStateOf(editingSiniestro?.lugar ?: "") }
        var tipoSiniestro by remember { mutableStateOf(editingSiniestro?.tipoSiniestro ?: "Choque simple") }
        var descripcion by remember { mutableStateOf(editingSiniestro?.descripcion ?: "") }
        var datosTerceros by remember { mutableStateOf(editingSiniestro?.datosTerceros ?: "") }
        var danos by remember { mutableStateOf(editingSiniestro?.danos ?: "") }
        var importeEstimadoStr by remember { mutableStateOf(editingSiniestro?.importeEstimado?.toInt()?.toString() ?: "0") }
        var docPendiente by remember { mutableStateOf(editingSiniestro?.documentacionPendiente ?: "") }
        var responsable by remember { mutableStateOf(editingSiniestro?.responsable ?: "Oficina") }
        var proximoSeguimiento by remember { mutableStateOf(editingSiniestro?.fechaProximoSeguimiento ?: DateUtils.todayIso()) }
        var estado by remember { mutableStateOf(editingSiniestro?.estado ?: "Nuevo") }
        var observaciones by remember { mutableStateOf(editingSiniestro?.observaciones ?: "") }

        var validationError by remember { mutableStateOf<String?>(null) }
        var cliExpanded by remember { mutableStateOf(false) }
        var compExpanded by remember { mutableStateOf(false) }
        var vehExpanded by remember { mutableStateOf(false) }

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
                        text = if (editingSiniestro == null) "Nuevo Siniestro" else "Modificar Siniestro",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = numInterno,
                            onValueChange = { numInterno = it },
                            label = { Text("N° Interno Siniestro *") },
                            modifier = Modifier.weight(1f).testTag("claim_number_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = numDenuncia,
                            onValueChange = { numDenuncia = it },
                            label = { Text("N° Denuncia Aseguradora") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Client Selector
                    val curCli = clientesMap[selectedClienteId]
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = curCli?.nombre ?: "Seleccionar Cliente...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cliente *") },
                            trailingIcon = { IconButton(onClick = { cliExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { cliExpanded = true }
                        )
                        DropdownMenu(expanded = cliExpanded, onDismissRequest = { cliExpanded = false }) {
                            clientes.filter { it.activo }.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.nombre} (${c.documentoNumero})") },
                                    onClick = {
                                        selectedClienteId = c.id
                                        selectedVehiculoId = null
                                        cliExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compania Selector
                    val curComp = companiasMap[selectedCompaniaId]
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = curComp?.nombre ?: "Seleccionar Aseguradora...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Compañía Aseguradora *") },
                            trailingIcon = { IconButton(onClick = { compExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { compExpanded = true }
                        )
                        DropdownMenu(expanded = compExpanded, onDismissRequest = { compExpanded = false }) {
                            companias.filter { it.activo }.forEach { comp ->
                                DropdownMenuItem(
                                    text = { Text(comp.nombre) },
                                    onClick = {
                                        selectedCompaniaId = comp.id
                                        compExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vehicle Selector
                    val curVeh = selectedVehiculoId?.let { vehiculosMap[it] }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = curVeh?.let { "${it.dominio} (${it.marca} ${it.modelo})" } ?: "Sin vehículo involucrado",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vehículo Involucrado") },
                            trailingIcon = { IconButton(onClick = { vehExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { vehExpanded = true }
                        )
                        DropdownMenu(expanded = vehExpanded, onDismissRequest = { vehExpanded = false }) {
                            DropdownMenuItem(text = { Text("Ninguno / Sin vehículo") }, onClick = { selectedVehiculoId = null; vehExpanded = false })
                            clientVehicles.forEach { v ->
                                DropdownMenuItem(
                                    text = { Text("${v.dominio} - ${v.marca} ${v.modelo}") },
                                    onClick = { selectedVehiculoId = v.id; vehExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fechaSiniestro,
                            onValueChange = { fechaSiniestro = it },
                            label = { Text("Fecha (YYYY-MM-DD) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = horaSiniestro,
                            onValueChange = { horaSiniestro = it },
                            label = { Text("Hora (HH:mm)") },
                            modifier = Modifier.width(110.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = lugar,
                            onValueChange = { lugar = it },
                            label = { Text("Lugar del Hecho") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tipoSiniestro,
                            onValueChange = { tipoSiniestro = it },
                            label = { Text("Tipo Siniestro") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción del Siniestro") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = datosTerceros,
                        onValueChange = { datosTerceros = it },
                        label = { Text("Terceros Involucrados (Nombre, Contacto, Compañía, Patente)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = danos,
                            onValueChange = { danos = it },
                            label = { Text("Daños Registrados") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = importeEstimadoStr,
                            onValueChange = { importeEstimadoStr = it },
                            label = { Text("Importe Est. ($)") },
                            modifier = Modifier.width(130.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = estado,
                            onValueChange = { estado = it },
                            label = { Text("Estado Siniestro") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = responsable,
                            onValueChange = { responsable = it },
                            label = { Text("Responsable") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = proximoSeguimiento,
                            onValueChange = { proximoSeguimiento = it },
                            label = { Text("Próx. Seguimiento (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = docPendiente,
                            onValueChange = { docPendiente = it },
                            label = { Text("Doc. Pendiente") },
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
                        maxLines = 2
                    )

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = validationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showFormDialog = false }) { Text("Cancelar") }
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (numInterno.isBlank()) {
                                    validationError = "Por favor ingresá un número interno de siniestro."
                                    return@Button
                                }
                                val toSave = Siniestro(
                                    id = editingSiniestro?.id ?: 0,
                                    numeroInterno = numInterno.trim(),
                                    numeroDenuncia = numDenuncia.trim(),
                                    clienteId = selectedClienteId,
                                    vehiculoId = selectedVehiculoId,
                                    companiaId = selectedCompaniaId,
                                    fechaSiniestro = DateUtils.toIsoFormat(fechaSiniestro.trim()),
                                    horaSiniestro = horaSiniestro.trim(),
                                    lugar = lugar.trim(),
                                    tipoSiniestro = tipoSiniestro.trim(),
                                    descripcion = descripcion.trim(),
                                    datosTerceros = datosTerceros.trim(),
                                    danos = danos.trim(),
                                    importeEstimado = importeEstimadoStr.toDoubleOrNull() ?: 0.0,
                                    documentacionPendiente = docPendiente.trim(),
                                    responsable = responsable.trim(),
                                    fechaUltimoContacto = DateUtils.todayIso(),
                                    fechaProximoSeguimiento = DateUtils.toIsoFormat(proximoSeguimiento.trim()),
                                    estado = estado.trim(),
                                    observaciones = observaciones.trim(),
                                    activo = true
                                )
                                viewModel.saveSiniestro(toSave) {
                                    showFormDialog = false
                                }
                            },
                            modifier = Modifier.testTag("save_claim_button")
                        ) {
                            Text("Guardar Siniestro")
                        }
                    }
                }
            }
        }
    }

    // Confirm Delete Dialog
    if (siniestroToDelete != null) {
        ConfirmDeleteDialog(
            title = "¿Dar de baja el siniestro?",
            message = "Se dará de baja el siniestro '${siniestroToDelete?.numeroInterno}'. Se conservará el historial administrativo.",
            onConfirm = {
                siniestroToDelete?.let { viewModel.bajaSiniestro(it.id) }
                siniestroToDelete = null
            },
            onDismiss = { siniestroToDelete = null }
        )
    }
}
