package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AlertaItem
import com.example.ui.MainViewModel
import com.example.ui.TipoAlerta
import com.example.ui.UrgenciaAlerta
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.utils.ArgentinaFormats
import com.example.utils.DateUtils
import com.example.utils.ExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VencimientosScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val alertas by viewModel.alertas.collectAsStateWithLifecycle()
    val config by viewModel.configuracion.collectAsStateWithLifecycle(null)

    var selectedTipo by remember { mutableStateOf("TODOS") }
    var selectedUrgencia by remember { mutableStateOf("TODOS") }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyPending by remember { mutableStateOf(false) }

    val filteredAlertas = remember(alertas, selectedTipo, selectedUrgencia, searchQuery, showOnlyPending) {
        alertas.filter { a ->
            val matchTipo = when (selectedTipo) {
                "POLIZAS" -> a.tipo == TipoAlerta.POLIZA
                "CARNETS" -> a.tipo == TipoAlerta.CARNET
                "VTV" -> a.tipo == TipoAlerta.VTV
                "SINIESTROS" -> a.tipo == TipoAlerta.SINIESTRO
                else -> true
            }

            val matchUrgencia = when (selectedUrgencia) {
                "VENCIDO" -> a.urgencia == UrgenciaAlerta.VENCIDO
                "HOY" -> a.urgencia == UrgenciaAlerta.HOY
                "DIAS_7" -> a.urgencia in listOf(UrgenciaAlerta.VENCIDO, UrgenciaAlerta.HOY, UrgenciaAlerta.DIAS_7)
                "DIAS_15" -> a.urgencia in listOf(UrgenciaAlerta.VENCIDO, UrgenciaAlerta.HOY, UrgenciaAlerta.DIAS_7, UrgenciaAlerta.DIAS_15)
                "DIAS_30" -> true
                else -> true
            }

            val matchPending = if (showOnlyPending) !a.revisada else true

            val matchSearch = searchQuery.isBlank() ||
                a.clienteNombre.contains(searchQuery, ignoreCase = true) ||
                a.titulo.contains(searchQuery, ignoreCase = true) ||
                a.subtitulo.contains(searchQuery, ignoreCase = true) ||
                (a.dominioVehiculo?.contains(searchQuery, ignoreCase = true) == true)

            matchTipo && matchUrgencia && matchPending && matchSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("vencimientos_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.exportVencimientosExcel(context) { file ->
                    ExportUtils.shareExportedFile(context, file, "Vencimientos y Alertas - SegurosPAS")
                }},
                icon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                text = { Text("Exportar Alertas a Excel") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("export_vencimientos_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Notice Card explaining direct action limitation compliance
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Navy800, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Centro de control de vencimientos. Tocá el botón de WhatsApp para abrir el chat oficial con el mensaje redactado listo para enviar desde tu dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SearchBarM3(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar alerta por cliente, patente o póliza..."
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Urgencia Filter Row (Horizontes temporales)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val urgencias = listOf(
                    "TODOS" to "Todos los vencimientos",
                    "VENCIDO" to "Vencidos",
                    "HOY" to "Vencen hoy",
                    "DIAS_7" to "Próx. 7 días",
                    "DIAS_15" to "Próx. 15 días",
                    "DIAS_30" to "Próx. 30 días"
                )
                items(urgencias) { (key, label) ->
                    FilterChip(
                        selected = selectedUrgencia == key,
                        onClick = { selectedUrgencia = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (key) {
                                "VENCIDO" -> Red100
                                "HOY" -> Amber100
                                "DIAS_7" -> Amber100
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Entity Type Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tipos = listOf(
                    "TODOS" to "Todas las categorías",
                    "POLIZAS" to "Pólizas",
                    "VTV" to "VTV / RTO",
                    "CARNETS" to "Carnets de Conducir",
                    "SINIESTROS" to "Seguimientos Siniestros"
                )
                items(tipos) { (key, label) ->
                    FilterChip(
                        selected = selectedTipo == key,
                        onClick = { selectedTipo = key },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pending switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filteredAlertas.size} vencimientos encontrados",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Solo pendientes", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(6.dp))
                    Switch(
                        checked = showOnlyPending,
                        onCheckedChange = { showOnlyPending = it },
                        modifier = Modifier.testTag("switch_only_pending")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredAlertas.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = "Sin vencimientos en este período",
                    message = "No existen registros que coincidan con los filtros seleccionados."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredAlertas, key = { it.clave }) { alert ->
                        val (icon, typeName) = when (alert.tipo) {
                            TipoAlerta.POLIZA -> Pair(Icons.Default.Description, "PÓLIZA")
                            TipoAlerta.CARNET -> Pair(Icons.Default.Badge, "CARNET DE CONDUCIR")
                            TipoAlerta.VTV -> Pair(Icons.Default.Build, "VTV / RTO")
                            TipoAlerta.SINIESTRO -> Pair(Icons.Default.CarCrash, "SINIESTRO")
                        }

                        val (statusType, iconTint) = when (alert.urgencia) {
                            UrgenciaAlerta.VENCIDO -> Pair(StatusType.DANGER, Red600)
                            UrgenciaAlerta.HOY -> Pair(StatusType.WARNING, Amber600)
                            UrgenciaAlerta.DIAS_7 -> Pair(StatusType.WARNING, Amber500)
                            UrgenciaAlerta.DIAS_15 -> Pair(StatusType.INFO, Navy700)
                            else -> Pair(StatusType.NEUTRAL, Slate700)
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (alert.revisada) Slate100 else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (alert.revisada) 0.dp else 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("alerta_item_${alert.clave}")
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
                                            .background(iconTint.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = icon, contentDescription = null, tint = iconTint)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = alert.titulo,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            StatusBadge(
                                                text = DateUtils.describeExpiry(alert.diasRestantes),
                                                statusType = statusType
                                            )
                                        }
                                        Text(
                                            text = "$typeName • ${alert.clienteNombre}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Slate100.copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("📋 Detalle: ${alert.subtitulo}", style = MaterialTheme.typography.bodySmall)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("🗓️ Vencimiento: ${DateUtils.toArgFormat(alert.fechaVencimiento)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (alert.diasRestantes != null && alert.diasRestantes < 0) {
                                                    "${kotlin.math.abs(alert.diasRestantes)} días de atraso"
                                                } else {
                                                    "${alert.diasRestantes ?: 0} días restantes"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (alert.diasRestantes != null && alert.diasRestantes < 0) Red600 else Slate700,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (alert.dominioVehiculo != null) {
                                            Text("🚗 Dominio: ${alert.dominioVehiculo}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Contact & Review actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // WhatsApp & Call Buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val contactNum = alert.clienteWhatsapp.ifBlank { alert.clienteTelefono }
                                        if (contactNum.isNotBlank()) {
                                            FilledTonalButton(
                                                onClick = {
                                                    val msg = "Hola ${alert.clienteNombre}, te saludamos de ${config?.nombreOficina ?: "tu asesoría de seguros"}. Te recordamos el vencimiento de tu ${alert.titulo} (${alert.subtitulo}) con fecha ${DateUtils.toArgFormat(alert.fechaVencimiento)}. ¡Estamos a tu disposición para gestionarlo con la compañía aseguradora!"
                                                    val url = ArgentinaFormats.buildWhatsAppUrl(contactNum, msg)
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.testTag("whatsapp_btn_${alert.clave}")
                                            ) {
                                                Icon(Icons.Default.Chat, null, Modifier.size(16.dp), tint = Green600)
                                                Spacer(Modifier.width(6.dp))
                                                Text("WhatsApp", style = MaterialTheme.typography.labelMedium)
                                            }
                                        }

                                        if (alert.clienteTelefono.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${alert.clienteTelefono}"))
                                                    context.startActivity(intent)
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Call, null, Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Llamar", style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }

                                    // Mark as Reviewed Toggle
                                    Button(
                                        onClick = { viewModel.toggleAlertaRevisada(alert.clave, alert.revisada) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (alert.revisada) Slate400 else Green600
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("toggle_review_${alert.clave}")
                                    ) {
                                        Icon(
                                            imageVector = if (alert.revisada) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            if (alert.revisada) "Gestionado" else "Marcar Revisado",
                                            style = MaterialTheme.typography.labelMedium
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
