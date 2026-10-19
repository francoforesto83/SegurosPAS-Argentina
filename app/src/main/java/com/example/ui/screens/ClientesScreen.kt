package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
fun ClientesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clientes by viewModel.clientes.collectAsStateWithLifecycle(emptyList())
    val vehiculos by viewModel.vehiculos.collectAsStateWithLifecycle(emptyList())
    val polizas by viewModel.polizas.collectAsStateWithLifecycle(emptyList())
    val siniestros by viewModel.siniestros.collectAsStateWithLifecycle(emptyList())

    val searchQuery by viewModel.searchClientes.collectAsStateWithLifecycle()
    var selectedTipoFilter by remember { mutableStateOf("TODOS") }

    // Dialog state
    var showFormDialog by remember { mutableStateOf(false) }
    var editingCliente by remember { mutableStateOf<Cliente?>(null) }
    var viewingCliente by remember { mutableStateOf<Cliente?>(null) }
    var clienteToDelete by remember { mutableStateOf<Cliente?>(null) }
    var duplicateWarning by remember { mutableStateOf<String?>(null) }

    val filteredClientes = remember(clientes, searchQuery, selectedTipoFilter) {
        clientes.filter { c ->
            c.activo &&
            (selectedTipoFilter == "TODOS" || c.tipo == selectedTipoFilter) &&
            (searchQuery.isBlank() ||
             c.nombre.contains(searchQuery, ignoreCase = true) ||
             c.documentoNumero.contains(searchQuery, ignoreCase = true) ||
             c.telefono.contains(searchQuery, ignoreCase = true) ||
             c.email.contains(searchQuery, ignoreCase = true) ||
             c.localidad.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("clientes_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCliente = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_client_fab")
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Registrar Cliente")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Bar
            SearchBarM3(
                query = searchQuery,
                onQueryChange = { viewModel.searchClientes.value = it },
                placeholder = "Buscar cliente por nombre, DNI, CUIT, teléfono o email..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "TODOS" to "Todos (${clientes.count { it.activo }})",
                    "PERSONA_FISICA" to "Particulares / Personas Físicas",
                    "PERSONA_JURIDICA" to "Empresas / Jurídicas"
                )
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = selectedTipoFilter == key,
                        onClick = { selectedTipoFilter = key },
                        label = { Text(label) },
                        leadingIcon = if (selectedTipoFilter == key) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredClientes.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.PersonSearch,
                    title = "No se encontraron clientes",
                    message = if (searchQuery.isNotEmpty()) "No hay resultados para '$searchQuery'." else "No hay clientes cargados en el sistema.",
                    actionButtonText = if (searchQuery.isEmpty()) "+ Cargar primer cliente" else null,
                    onActionClick = {
                        editingCliente = null
                        showFormDialog = true
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredClientes, key = { it.id }) { cliente ->
                        val clientVehiculos = vehiculos.filter { it.clienteId == cliente.id && it.activo }
                        val clientPolizas = polizas.filter { it.clienteId == cliente.id && it.activo }
                        val clientSiniestros = siniestros.filter { it.clienteId == cliente.id && it.activo }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingCliente = cliente }
                                .testTag("client_card_${cliente.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (cliente.tipo == "PERSONA_JURIDICA") Teal100 else Navy100
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (cliente.tipo == "PERSONA_JURIDICA") Icons.Default.Business else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (cliente.tipo == "PERSONA_JURIDICA") Teal600 else Navy800,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cliente.nombre,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${cliente.documentoTipo}: ${ArgentinaFormats.formatDocumento(cliente.documentoNumero, cliente.documentoTipo)} • ${cliente.localidad}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            editingCliente = cliente
                                            showFormDialog = true
                                        },
                                        modifier = Modifier.testTag("edit_client_${cliente.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(
                                        onClick = { clienteToDelete = cliente },
                                        modifier = Modifier.testTag("delete_client_${cliente.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Baja", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick Badges: Associated assets
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AssistChip(
                                        onClick = { viewingCliente = cliente },
                                        label = { Text("${clientVehiculos.size} autos") },
                                        leadingIcon = { Icon(Icons.Default.DirectionsCar, null, Modifier.size(16.dp)) }
                                    )
                                    AssistChip(
                                        onClick = { viewingCliente = cliente },
                                        label = { Text("${clientPolizas.size} pólizas") },
                                        leadingIcon = { Icon(Icons.Default.Description, null, Modifier.size(16.dp)) }
                                    )
                                    if (clientSiniestros.isNotEmpty()) {
                                        AssistChip(
                                            onClick = { viewingCliente = cliente },
                                            label = { Text("${clientSiniestros.size} siniestros") },
                                            leadingIcon = { Icon(Icons.Default.Warning, null, Modifier.size(16.dp), tint = Amber600) }
                                        )
                                    }
                                }

                                // Quick Contact row
                                if (cliente.telefono.isNotBlank() || cliente.whatsapp.isNotBlank() || cliente.email.isNotBlank()) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cliente.telefono.ifBlank { cliente.whatsapp },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (cliente.whatsapp.isNotBlank()) {
                                                FilledTonalButton(
                                                    onClick = {
                                                        val url = ArgentinaFormats.buildWhatsAppUrl(
                                                            cliente.whatsapp,
                                                            "Hola ${cliente.nombre}, te escribimos de la oficina de seguros para saludarte y ponernos a tu disposición."
                                                        )
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Icon(Icons.Default.Chat, null, Modifier.size(14.dp), tint = Green600)
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("WhatsApp", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }

                                            if (cliente.telefono.isNotBlank()) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cliente.telefono}"))
                                                        context.startActivity(intent)
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Icon(Icons.Default.Call, null, Modifier.size(14.dp))
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("Llamar", style = MaterialTheme.typography.labelSmall)
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
        }
    }

    // Client Detail Sheet Dialog
    if (viewingCliente != null) {
        val client = viewingCliente!!
        val clientVehiculos = vehiculos.filter { it.clienteId == client.id && it.activo }
        val clientPolizas = polizas.filter { it.clienteId == client.id && it.activo }
        val clientSiniestros = siniestros.filter { it.clienteId == client.id && it.activo }

        Dialog(onDismissRequest = { viewingCliente = null }) {
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
                            text = "Ficha de Cliente",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { viewingCliente = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = client.nombre,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${client.documentoTipo}: ${client.documentoNumero} • ${if (client.tipo == "PERSONA_FISICA") "Persona Física" else "Persona Jurídica"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate700
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact & Address Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📍 Domicilio: ${client.domicilio.ifBlank { "-" }}, ${client.localidad} (${client.provincia})", style = MaterialTheme.typography.bodyMedium)
                            Text("📞 Teléfono: ${client.telefono.ifBlank { "-" }}", style = MaterialTheme.typography.bodyMedium)
                            Text("💬 WhatsApp: ${client.whatsapp.ifBlank { "-" }}", style = MaterialTheme.typography.bodyMedium)
                            Text("✉️ Email: ${client.email.ifBlank { "-" }}", style = MaterialTheme.typography.bodyMedium)
                            if (client.fechaNacimiento.isNotBlank()) {
                                Text("🎂 Fecha Nacimiento: ${client.fechaNacimiento}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (client.observaciones.isNotBlank()) {
                                Text("📝 Observaciones: ${client.observaciones}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Associated Vehicles
                    Text(
                        text = "Vehículos Registrados (${clientVehiculos.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (clientVehiculos.isEmpty()) {
                        Text("No posee vehículos asociados.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    } else {
                        clientVehiculos.forEach { v ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate100),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DirectionsCar, null, tint = Teal600)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("${v.marca} ${v.modelo} (${v.anio})", fontWeight = FontWeight.Bold)
                                        Text("Patente: ${v.dominio} • Uso: ${v.uso}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Associated Policies
                    Text(
                        text = "Pólizas del Cliente (${clientPolizas.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (clientPolizas.isEmpty()) {
                        Text("No posee pólizas asociadas.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    } else {
                        clientPolizas.forEach { p ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate100),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Póliza: ${p.numeroPoliza}", fontWeight = FontWeight.Bold)
                                        Text(p.estadoAdministrativo, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Ramo: ${p.ramo} • Cobertura: ${p.tipoCobertura}", style = MaterialTheme.typography.bodySmall)
                                    Text("Vigencia: ${DateUtils.toArgFormat(p.fechaInicio)} al ${DateUtils.toArgFormat(p.fechaFin)}", style = MaterialTheme.typography.bodySmall)
                                    Text("Premio: ${ArgentinaFormats.formatCurrency(p.premioTotal)} (${p.cantidadCuotas} cuotas)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Associated Claims
                    if (clientSiniestros.isNotEmpty()) {
                        Text(
                            text = "Historial de Siniestros (${clientSiniestros.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        clientSiniestros.forEach { s ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Amber100.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("${s.numeroInterno} - ${s.tipoSiniestro}", fontWeight = FontWeight.Bold)
                                    Text("Estado: ${s.estado} • Fecha: ${DateUtils.toArgFormat(s.fechaSiniestro)}", style = MaterialTheme.typography.bodySmall)
                                    Text(s.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                editingCliente = client
                                viewingCliente = null
                                showFormDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Editar")
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showFormDialog) {
        var tipo by remember { mutableStateOf(editingCliente?.tipo ?: "PERSONA_FISICA") }
        var nombre by remember { mutableStateOf(editingCliente?.nombre ?: "") }
        var docTipo by remember { mutableStateOf(editingCliente?.documentoTipo ?: "DNI") }
        var docNum by remember { mutableStateOf(editingCliente?.documentoNumero ?: "") }
        var fechaNac by remember { mutableStateOf(editingCliente?.fechaNacimiento ?: "") }
        var tel by remember { mutableStateOf(editingCliente?.telefono ?: "") }
        var wpp by remember { mutableStateOf(editingCliente?.whatsapp ?: "") }
        var email by remember { mutableStateOf(editingCliente?.email ?: "") }
        var domicilio by remember { mutableStateOf(editingCliente?.domicilio ?: "") }
        var localidad by remember { mutableStateOf(editingCliente?.localidad ?: "") }
        var provincia by remember { mutableStateOf(editingCliente?.provincia ?: "Buenos Aires") }
        var observaciones by remember { mutableStateOf(editingCliente?.observaciones ?: "") }

        var validationError by remember { mutableStateOf<String?>(null) }

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
                        text = if (editingCliente == null) "Nuevo Cliente" else "Modificar Cliente",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Tipo Selector
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = tipo == "PERSONA_FISICA",
                            onClick = {
                                tipo = "PERSONA_FISICA"
                                docTipo = "DNI"
                            },
                            label = { Text("Persona Física") }
                        )
                        FilterChip(
                            selected = tipo == "PERSONA_JURIDICA",
                            onClick = {
                                tipo = "PERSONA_JURIDICA"
                                docTipo = "CUIT"
                            },
                            label = { Text("Persona Jurídica / Empresa") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text(if (tipo == "PERSONA_FISICA") "Nombre y Apellido *" else "Razón Social *") },
                        modifier = Modifier.fillMaxWidth().testTag("client_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = docTipo,
                            onValueChange = { docTipo = it },
                            label = { Text("Tipo Doc") },
                            modifier = Modifier.width(100.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = docNum,
                            onValueChange = { docNum = it },
                            label = { Text("Número DNI / CUIT *") },
                            modifier = Modifier.weight(1f).testTag("client_doc_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fechaNac,
                        onValueChange = { fechaNac = it },
                        label = { Text("Fecha Nacimiento (DD/MM/AAAA)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tel,
                            onValueChange = { tel = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = wpp,
                            onValueChange = { wpp = it },
                            label = { Text("WhatsApp") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = domicilio,
                        onValueChange = { domicilio = it },
                        label = { Text("Domicilio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = localidad,
                            onValueChange = { localidad = it },
                            label = { Text("Localidad") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = provincia,
                            onValueChange = { provincia = it },
                            label = { Text("Provincia") },
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showFormDialog = false }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (nombre.isBlank() || docNum.isBlank()) {
                                    validationError = "Por favor completá los campos obligatorios (Nombre y Documento)."
                                    return@Button
                                }
                                val toSave = Cliente(
                                    id = editingCliente?.id ?: 0,
                                    tipo = tipo,
                                    nombre = nombre.trim(),
                                    documentoTipo = docTipo.trim(),
                                    documentoNumero = docNum.trim(),
                                    fechaNacimiento = fechaNac.trim(),
                                    telefono = tel.trim(),
                                    whatsapp = wpp.trim(),
                                    email = email.trim(),
                                    domicilio = domicilio.trim(),
                                    localidad = localidad.trim(),
                                    provincia = provincia.trim(),
                                    observaciones = observaciones.trim(),
                                    activo = true
                                )
                                viewModel.saveCliente(
                                    cliente = toSave,
                                    onDuplicateFound = { dup ->
                                        duplicateWarning = "Advertencia: Ya existe un cliente con el documento ${dup.documentoNumero} (${dup.nombre})."
                                    },
                                    onSaved = {
                                        showFormDialog = false
                                    }
                                )
                            },
                            modifier = Modifier.testTag("save_client_button")
                        ) {
                            Text("Guardar Cliente")
                        }
                    }
                }
            }
        }
    }

    // Duplicate Warning Dialog
    if (duplicateWarning != null) {
        AlertDialog(
            onDismissRequest = { duplicateWarning = null },
            icon = { Icon(Icons.Default.Warning, null, tint = Amber600) },
            title = { Text("Documento Duplicado") },
            text = { Text(duplicateWarning!!) },
            confirmButton = {
                Button(onClick = { duplicateWarning = null }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Confirm Delete Dialog
    if (clienteToDelete != null) {
        ConfirmDeleteDialog(
            title = "¿Dar de baja al cliente?",
            message = "Se dará de baja a '${clienteToDelete?.nombre}'. Esta acción conserva el historial administrativo para auditoría.",
            onConfirm = {
                clienteToDelete?.let { viewModel.bajaCliente(it.id) }
                clienteToDelete = null
            },
            onDismiss = { clienteToDelete = null }
        )
    }
}
