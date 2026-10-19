package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.*
import com.example.utils.ExportUtils

@Composable
fun BackupScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showConfirmClear by remember { mutableStateOf(false) }
    var showConfirmReloadDemo by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("backup_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Privacy & Storage Notice
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Navy800,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Almacenamiento Local y Seguridad de Datos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Por estrictas razones de privacidad y confidencialidad de la información aseguradora argentina, todos los datos residen de forma persistente y segura en la base de datos local SQLite/Room de este dispositivo. No se envían datos a servidores externos sin tu autorización.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
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

        // 1. Backup Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Generar Copia de Seguridad Completa (JSON)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Genera un archivo JSON consolidado con todos los clientes, vehículos, pólizas, aseguradoras, siniestros y seguimientos. Podés guardarlo o transferirlo a otra computadora o celular.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.exportFullBackupJson(context) { file ->
                                statusMessage = "Copia generada: ${file.name}"
                                ExportUtils.shareExportedFile(context, file, "Copia de Seguridad - SegurosPAS")
                            }
                        },
                        modifier = Modifier.testTag("create_backup_button")
                    ) {
                        Icon(Icons.Default.Save, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Descargar Copia de Seguridad")
                    }
                }
            }
        }

        // 2. Restore Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, null, tint = Teal600, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Restaurar Copia de Seguridad", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Pegá o cargá un archivo de respaldo JSON generado previamente para importar y sincronizar la base de datos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                    Spacer(Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = { showRestoreDialog = true },
                        modifier = Modifier.testTag("open_restore_dialog_button")
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Abrir Asistente de Restauración")
                    }
                }
            }
        }

        // 3. Demo Data Management Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Science, null, tint = Amber600, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Datos de Demostración (Argentina)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Permite recargar clientes, patentes argentinas, pólizas de La Segunda, San Cristóbal, Federación Patronal, vencimientos y siniestros de prueba para evaluar todas las pantallas del software.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showConfirmReloadDemo = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber600),
                            modifier = Modifier.testTag("reload_demo_data_button")
                        ) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Recargar Datos Demo")
                        }

                        OutlinedButton(
                            onClick = { showConfirmClear = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red600),
                            modifier = Modifier.testTag("clear_database_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Limpiar Base de Datos")
                        }
                    }
                }
            }
        }
    }

    // Confirm Reload Demo Dialog
    if (showConfirmReloadDemo) {
        AlertDialog(
            onDismissRequest = { showConfirmReloadDemo = false },
            icon = { Icon(Icons.Default.Science, null, tint = Amber600) },
            title = { Text("¿Recargar Datos de Prueba?") },
            text = { Text("Se restablecerán los clientes, pólizas, vehículos y siniestros de ejemplo para Argentina.") },
            confirmButton = {
                Button(onClick = {
                    showConfirmReloadDemo = false
                    viewModel.reloadDemoData {
                        statusMessage = "¡Datos de demostración recargados con éxito!"
                    }
                }) {
                    Text("Confirmar Recarga")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmReloadDemo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirm Clear All Dialog
    if (showConfirmClear) {
        ConfirmDeleteDialog(
            title = "¿Vaciar la base de datos?",
            message = "Se eliminarán todos los registros de clientes, vehículos, pólizas y siniestros. Esta acción no se puede deshacer.",
            onConfirm = {
                showConfirmClear = false
                viewModel.clearAllData {
                    statusMessage = "Base de datos vaciada. Lista para cargar datos reales."
                }
            },
            onDismiss = { showConfirmClear = false }
        )
    }

    // Restore JSON Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Pegar Respaldo JSON") },
            text = {
                Column {
                    Text("Pegá el contenido JSON de tu copia de seguridad a continuación:", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        placeholder = { Text("{\"clientes\": [...], \"polizas\": [...]}") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonInput.isNotBlank()) {
                            viewModel.restoreBackupJson(restoreJsonInput) { success, msg ->
                                showRestoreDialog = false
                                restoreJsonInput = ""
                                statusMessage = msg
                            }
                        }
                    }
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
