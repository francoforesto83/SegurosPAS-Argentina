package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.utils.DateUtils
import com.example.utils.ExportUtils

@Composable
fun ExportarInformesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clientes by viewModel.clientes.collectAsStateWithLifecycle(emptyList())
    val vehiculos by viewModel.vehiculos.collectAsStateWithLifecycle(emptyList())
    val polizas by viewModel.polizas.collectAsStateWithLifecycle(emptyList())
    val siniestros by viewModel.siniestros.collectAsStateWithLifecycle(emptyList())
    val alertas by viewModel.alertas.collectAsStateWithLifecycle()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("exportar_informes_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Centro de Exportación Excel / CSV",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Genera archivos con encabezados en español, fechas en formato DD/MM/AAAA y codificación UTF-8 BOM compatible con Microsoft Excel y Google Sheets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (statusMessage != null) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Green100)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Green600)
                        Spacer(Modifier.width(8.dp))
                        Text(statusMessage!!, color = Green600, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Export Modules List
        item {
            Text(
                text = "Exportación de Módulos Individuales",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            ExportModuleCard(
                title = "Pólizas de Seguros",
                description = "Exporta ${polizas.count { it.activo }} pólizas con número, asegurado, ramo, cobertura, prima, fechas e historial.",
                filename = "Polizas_${DateUtils.todayIso()}.csv",
                icon = Icons.Default.Description,
                iconColor = Navy800,
                onExport = {
                    isExporting = true
                    viewModel.exportPolizasExcel(context) { file ->
                        isExporting = false
                        statusMessage = "Archivo generado: ${file.name}"
                        ExportUtils.shareExportedFile(context, file, "Pólizas - SegurosPAS")
                    }
                }
            )
        }

        item {
            ExportModuleCard(
                title = "Vencimientos y Alertas",
                description = "Exporta ${alertas.size} alertas de vencimiento (Pólizas, Carnets, VTV/RTO y Siniestros) con días restantes.",
                filename = "Vencimientos_${DateUtils.todayIso()}.csv",
                icon = Icons.Default.Warning,
                iconColor = Amber600,
                onExport = {
                    isExporting = true
                    viewModel.exportVencimientosExcel(context) { file ->
                        isExporting = false
                        statusMessage = "Archivo generado: ${file.name}"
                        ExportUtils.shareExportedFile(context, file, "Vencimientos y Alertas - SegurosPAS")
                    }
                }
            )
        }

        item {
            ExportModuleCard(
                title = "Clientes Registrados",
                description = "Exporta ${clientes.count { it.activo }} clientes con tipo, DNI/CUIT, teléfonos, WhatsApp, domicilio y correo.",
                filename = "Clientes_${DateUtils.todayIso()}.csv",
                icon = Icons.Default.People,
                iconColor = Teal600,
                onExport = {
                    isExporting = true
                    viewModel.exportClientesExcel(context) { file ->
                        isExporting = false
                        statusMessage = "Archivo generado: ${file.name}"
                        ExportUtils.shareExportedFile(context, file, "Clientes - SegurosPAS")
                    }
                }
            )
        }

        item {
            ExportModuleCard(
                title = "Parque Automotor Asegurado",
                description = "Exporta ${vehiculos.count { it.activo }} vehículos con patente, marca, modelo, chasis, motor y vencimientos de VTV/Carnet.",
                filename = "Vehiculos_${DateUtils.todayIso()}.csv",
                icon = Icons.Default.DirectionsCar,
                iconColor = Navy700,
                onExport = {
                    isExporting = true
                    viewModel.exportVehiculosExcel(context) { file ->
                        isExporting = false
                        statusMessage = "Archivo generado: ${file.name}"
                        ExportUtils.shareExportedFile(context, file, "Vehículos - SegurosPAS")
                    }
                }
            )
        }

        item {
            ExportModuleCard(
                title = "Siniestros y Reclamos",
                description = "Exporta ${siniestros.count { it.activo }} siniestros con número de denuncia, daños, importes, estado y responsables.",
                filename = "Siniestros_${DateUtils.todayIso()}.csv",
                icon = Icons.Default.CarCrash,
                iconColor = Red600,
                onExport = {
                    isExporting = true
                    viewModel.exportSiniestrosExcel(context) { file ->
                        isExporting = false
                        statusMessage = "Archivo generado: ${file.name}"
                        ExportUtils.shareExportedFile(context, file, "Siniestros - SegurosPAS")
                    }
                }
            )
        }
    }
}

@Composable
fun ExportModuleCard(
    title: String,
    description: String,
    filename: String,
    icon: ImageVector,
    iconColor: Color,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(2.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Slate700)
                Spacer(Modifier.height(4.dp))
                Text(text = "📄 $filename", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.width(10.dp))

            Button(
                onClick = onExport,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("export_btn_${title.lowercase().take(6)}")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Exportar")
            }
        }
    }
}
