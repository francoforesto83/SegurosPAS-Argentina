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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.*
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.utils.ArgentinaFormats
import com.example.utils.DateUtils

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateTo: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val kpis by viewModel.dashboardKpis.collectAsStateWithLifecycle()
    val polizas by viewModel.polizas.collectAsStateWithLifecycle(emptyList())
    val companias by viewModel.companias.collectAsStateWithLifecycle(emptyList())
    val siniestros by viewModel.siniestros.collectAsStateWithLifecycle(emptyList())
    val alertas by viewModel.alertas.collectAsStateWithLifecycle()
    val config by viewModel.configuracion.collectAsStateWithLifecycle(null)

    val companiasMap = companias.associateBy { it.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = config?.nombreOficina ?: "Organización de Seguros PAS",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Matrícula SSN: ${config?.matriculaSSN ?: "PAS-89240"} | ${config?.nombreProductor ?: "Productor Asesor"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fecha actual: ${DateUtils.todayArg()} (Argentina)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    FilledTonalButton(
                        onClick = { onNavigateTo(AppScreen.VENCIMIENTOS) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quick_alerts_button")
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vencimientos (${alertas.count { !it.revisada }})")
                    }
                }
            }
        }

        // 2. Horizon Notification Strip (7, 15, 30 días)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (kpis.vencimientos7Dias > 0) Red100 else Slate100,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTo(AppScreen.VENCIMIENTOS) }
                        .testTag("horizon_7_days")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (kpis.vencimientos7Dias > 0) Red600 else Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Próx. 7 días",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (kpis.vencimientos7Dias > 0) Red600 else Slate700
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${kpis.vencimientos7Dias} alertas",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (kpis.vencimientos7Dias > 0) Red600 else Slate900
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (kpis.vencimientos15Dias > 0) Amber100 else Slate100,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTo(AppScreen.VENCIMIENTOS) }
                        .testTag("horizon_15_days")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = if (kpis.vencimientos15Dias > 0) Amber600 else Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Próx. 15 días",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (kpis.vencimientos15Dias > 0) Amber600 else Slate700
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${kpis.vencimientos15Dias} alertas",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (kpis.vencimientos15Dias > 0) Amber600 else Slate900
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate100,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTo(AppScreen.VENCIMIENTOS) }
                        .testTag("horizon_30_days")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Navy700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Próx. 30 días",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate700
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${kpis.vencimientos30Dias} alertas",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                    }
                }
            }
        }

        // 3. Grid of Main Stat Cards
        item {
            Text(
                text = "Indicadores Principales",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Clientes",
                    value = kpis.totalClientes.toString(),
                    subtitle = "Físicas y Jurídicas",
                    icon = Icons.Default.People,
                    iconColor = Navy700,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(AppScreen.CLIENTES) }
                )
                StatCard(
                    title = "Vehículos",
                    value = kpis.totalVehiculos.toString(),
                    subtitle = "Patentes activas",
                    icon = Icons.Default.DirectionsCar,
                    iconColor = Teal600,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(AppScreen.VEHICULOS) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Pólizas Vigentes",
                    value = kpis.polizasVigentes.toString(),
                    subtitle = "${kpis.polizasProximas} próx. a vencer",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Green600,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(AppScreen.POLIZAS) }
                )
                StatCard(
                    title = "Pólizas Vencidas",
                    value = kpis.polizasVencidas.toString(),
                    subtitle = "Requieren renovación",
                    icon = Icons.Default.ErrorOutline,
                    iconColor = Red600,
                    containerColor = if (kpis.polizasVencidas > 0) Red100.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(AppScreen.POLIZAS) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Siniestros Gestión",
                    value = kpis.siniestrosEnGestion.toString(),
                    subtitle = "${kpis.siniestrosAbiertos} nuevos / ${kpis.siniestrosCerrados} cerrados",
                    icon = Icons.Default.CarCrash,
                    iconColor = Amber600,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(AppScreen.SINIESTROS) }
                )
                StatCard(
                    title = "VTV y Carnets",
                    value = "${kpis.carnetsVencidos + kpis.vtvsVencidas}",
                    subtitle = "${kpis.carnetsPorVencer + kpis.vtvsPorVencer} por vencer",
                    icon = Icons.Default.Badge,
                    iconColor = Navy800,
                    containerColor = if ((kpis.carnetsVencidos + kpis.vtvsVencidas) > 0) Amber100.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(AppScreen.VENCIMIENTOS) }
                )
            }
        }

        // 4. Urgent Alerts Section (Top 4)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alertas Prioritarias",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { onNavigateTo(AppScreen.VENCIMIENTOS) },
                    modifier = Modifier.testTag("see_all_alerts_button")
                ) {
                    Text("Ver todas (${alertas.size})")
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        val pendingAlerts = alertas.filter { !it.revisada }.take(4)
        if (pendingAlerts.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Green600)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "No hay alertas pendientes urgentes. ¡Todo al día!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate700
                        )
                    }
                }
            }
        } else {
            items(pendingAlerts) { alert ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (alert.tipo) {
                            TipoAlerta.POLIZA -> Icons.Default.Description
                            TipoAlerta.CARNET -> Icons.Default.Badge
                            TipoAlerta.VTV -> Icons.Default.Build
                            TipoAlerta.SINIESTRO -> Icons.Default.CarCrash
                        }
                        val (badgeColor, statusType) = when (alert.urgencia) {
                            UrgenciaAlerta.VENCIDO -> Pair(Red600, StatusType.DANGER)
                            UrgenciaAlerta.HOY -> Pair(Amber600, StatusType.WARNING)
                            UrgenciaAlerta.DIAS_7 -> Pair(Amber500, StatusType.WARNING)
                            else -> Pair(Navy700, StatusType.INFO)
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(badgeColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = alert.titulo,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge(
                                    text = DateUtils.describeExpiry(alert.diasRestantes),
                                    statusType = statusType
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${alert.clienteNombre} • ${alert.subtitulo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Vencimiento: ${DateUtils.toArgFormat(alert.fechaVencimiento)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Quick WhatsApp button
                        if (alert.clienteWhatsapp.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val msg = "Hola ${alert.clienteNombre}, te contactamos de ${config?.nombreOficina ?: "tu oficina de seguros"} para recordarte el vencimiento de tu ${alert.titulo} (${alert.subtitulo}) previsto para el ${DateUtils.toArgFormat(alert.fechaVencimiento)}. ¡Quedamos a tu disposición para gestionarlo!"
                                    val url = ArgentinaFormats.buildWhatsAppUrl(alert.clienteWhatsapp, msg)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("whatsapp_alert_${alert.clave}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Contactar por WhatsApp",
                                    tint = Green600
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.toggleAlertaRevisada(alert.clave, alert.revisada) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Marcar como gestionada",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // 5. Visual Distribution Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Distribución de Pólizas por Compañía",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val activePolizas = polizas.filter { it.activo }
                    val totalPols = activePolizas.size
                    val polsByCompany = activePolizas.groupBy { it.companiaId }

                    if (totalPols == 0) {
                        Text("No hay pólizas registradas aún.", color = Slate500, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        val colors = listOf(Navy800, Teal600, Amber600, Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFF14B8A6))
                        var colorIdx = 0
                        polsByCompany.forEach { (compId, list) ->
                            val compName = companiasMap[compId]?.nombre ?: "Compañía #$compId"
                            val col = colors[colorIdx % colors.size]
                            colorIdx++
                            ProgressBarItem(
                                label = compName,
                                count = list.size,
                                total = totalPols,
                                color = col
                            )
                        }
                    }
                }
            }
        }

        // 6. Distribution by Branch (Ramo)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Distribución por Ramo de Cobertura",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val activePolizas = polizas.filter { it.activo }
                    val totalPols = activePolizas.size
                    val polsByRamo = activePolizas.groupBy { it.ramo }

                    if (totalPols == 0) {
                        Text("No hay pólizas registradas aún.", color = Slate500, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        val colors = listOf(Teal600, Navy700, Amber500, Green600, Color(0xFF8B5CF6))
                        var idx = 0
                        polsByRamo.forEach { (ramo, list) ->
                            val col = colors[idx % colors.size]
                            idx++
                            ProgressBarItem(
                                label = ramo,
                                count = list.size,
                                total = totalPols,
                                color = col
                            )
                        }
                    }
                }
            }
        }

        // 7. Quick Action Bar
        item {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigateTo(AppScreen.CLIENTES) },
                    modifier = Modifier.weight(1f).testTag("action_new_client"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clientes")
                }

                FilledTonalButton(
                    onClick = { onNavigateTo(AppScreen.POLIZAS) },
                    modifier = Modifier.weight(1f).testTag("action_new_policy"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pólizas")
                }

                OutlinedButton(
                    onClick = { onNavigateTo(AppScreen.EXPORTAR) },
                    modifier = Modifier.weight(1f).testTag("action_export_excel"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excel")
                }
            }
        }
    }
}
