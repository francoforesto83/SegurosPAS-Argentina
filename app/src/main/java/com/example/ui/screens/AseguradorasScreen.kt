package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CompaniaAseguradora
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AseguradorasScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val companias by viewModel.companias.collectAsStateWithLifecycle(emptyList())
    val polizas by viewModel.polizas.collectAsStateWithLifecycle(emptyList())

    var showFormDialog by remember { mutableStateOf(false) }
    var editingCompania by remember { mutableStateOf<CompaniaAseguradora?>(null) }
    var companiaToDelete by remember { mutableStateOf<CompaniaAseguradora?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(companias, searchQuery) {
        companias.filter {
            it.activo &&
            (searchQuery.isBlank() ||
             it.nombre.contains(searchQuery, ignoreCase = true) ||
             it.cuit.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("aseguradoras_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCompania = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_insurer_fab")
            ) {
                Icon(imageVector = Icons.Default.AddBusiness, contentDescription = "Nueva Aseguradora")
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
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar aseguradora por nombre o CUIT..."
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Business,
                    title = "Sin aseguradoras",
                    message = "No hay aseguradoras registradas."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { comp ->
                        val polizasCount = polizas.count { it.companiaId == comp.id && it.activo }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth().testTag("insurer_card_${comp.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = comp.nombre,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (comp.cuit.isNotBlank()) {
                                            Text(
                                                text = "CUIT: ${comp.cuit} • $polizasCount pólizas activas",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate700
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            editingCompania = comp
                                            showFormDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(
                                        onClick = { companiaToDelete = comp }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Baja", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Slate100,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (comp.contactoSiniestros.isNotBlank()) {
                                            Text("🚨 Contacto Siniestros: ${comp.contactoSiniestros}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Red600)
                                        }
                                        if (comp.contactoComercial.isNotBlank()) {
                                            Text("💼 Contacto Comercial: ${comp.contactoComercial}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (comp.observaciones.isNotBlank()) {
                                            Text("📝 ${comp.observaciones}", style = MaterialTheme.typography.bodySmall, color = Slate700)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick Action Buttons: Call, Email, Web
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (comp.telefono.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${comp.telefono}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Call, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Llamar", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }

                                    if (comp.email.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${comp.email}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Email, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Email", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }

                                    if (comp.sitioWeb.isNotBlank()) {
                                        FilledTonalButton(
                                            onClick = {
                                                val url = if (comp.sitioWeb.startsWith("http")) comp.sitioWeb else "https://${comp.sitioWeb}"
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Language, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Portal", style = MaterialTheme.typography.labelMedium)
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

    // Add / Edit Form Dialog
    if (showFormDialog) {
        var nombre by remember { mutableStateOf(editingCompania?.nombre ?: "") }
        var cuit by remember { mutableStateOf(editingCompania?.cuit ?: "") }
        var tel by remember { mutableStateOf(editingCompania?.telefono ?: "") }
        var email by remember { mutableStateOf(editingCompania?.email ?: "") }
        var web by remember { mutableStateOf(editingCompania?.sitioWeb ?: "") }
        var contactoSiniestros by remember { mutableStateOf(editingCompania?.contactoSiniestros ?: "") }
        var contactoComercial by remember { mutableStateOf(editingCompania?.contactoComercial ?: "") }
        var observaciones by remember { mutableStateOf(editingCompania?.observaciones ?: "") }

        Dialog(onDismissRequest = { showFormDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
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
                    Text(
                        text = if (editingCompania == null) "Nueva Aseguradora" else "Modificar Aseguradora",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Comercial *") },
                        modifier = Modifier.fillMaxWidth().testTag("insurer_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cuit,
                            onValueChange = { cuit = it },
                            label = { Text("CUIT") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tel,
                            onValueChange = { tel = it },
                            label = { Text("Teléfono Mesa Entrada") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Contacto") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = web,
                            onValueChange = { web = it },
                            label = { Text("Sitio Web Portal PAS") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contactoSiniestros,
                        onValueChange = { contactoSiniestros = it },
                        label = { Text("Contacto / Guardia Siniestros (0800 / WhatsApp)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contactoComercial,
                        onValueChange = { contactoComercial = it },
                        label = { Text("Contacto Comercial / Ejecutivo de Cuenta") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones / Notas") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showFormDialog = false }) { Text("Cancelar") }
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (nombre.isNotBlank()) {
                                    val toSave = CompaniaAseguradora(
                                        id = editingCompania?.id ?: 0,
                                        nombre = nombre.trim(),
                                        cuit = cuit.trim(),
                                        telefono = tel.trim(),
                                        email = email.trim(),
                                        sitioWeb = web.trim(),
                                        contactoSiniestros = contactoSiniestros.trim(),
                                        contactoComercial = contactoComercial.trim(),
                                        observaciones = observaciones.trim(),
                                        activo = true
                                    )
                                    viewModel.saveCompania(toSave) {
                                        showFormDialog = false
                                    }
                                }
                            },
                            modifier = Modifier.testTag("save_insurer_button")
                        ) {
                            Text("Guardar Aseguradora")
                        }
                    }
                }
            }
        }
    }

    // Confirm Delete Dialog
    if (companiaToDelete != null) {
        ConfirmDeleteDialog(
            title = "¿Dar de baja a la aseguradora?",
            message = "Se dará de baja '${companiaToDelete?.nombre}'. Se conservarán las pólizas históricas asociadas.",
            onConfirm = {
                companiaToDelete?.let { viewModel.bajaCompania(it.id) }
                companiaToDelete = null
            },
            onDismiss = { companiaToDelete = null }
        )
    }
}
