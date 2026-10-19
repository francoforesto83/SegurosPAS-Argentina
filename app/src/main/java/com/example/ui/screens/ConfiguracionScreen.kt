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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ConfiguracionOficina
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun ConfiguracionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config by viewModel.configuracion.collectAsStateWithLifecycle(null)

    var nombreOficina by remember(config) { mutableStateOf(config?.nombreOficina ?: "Organización de Seguros PAS") }
    var nombreProductor by remember(config) { mutableStateOf(config?.nombreProductor ?: "Franco Foresto") }
    var matriculaSSN by remember(config) { mutableStateOf(config?.matriculaSSN ?: "PAS-89240") }
    var telefono by remember(config) { mutableStateOf(config?.telefono ?: "+54 9 11 5555-0123") }
    var email by remember(config) { mutableStateOf(config?.email ?: "contacto@segurospas.com.ar") }
    var diasAlerta by remember(config) { mutableStateOf(config?.diasAlertaAnticipada ?: 15) }
    var moneda by remember(config) { mutableStateOf(config?.moneda ?: "ARS ($)") }

    var savedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("configuracion_screen"),
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
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Configuración de la Oficina de Seguros",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Datos del Productor Asesor de Seguros (PAS) y parámetros del sistema de alertas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (savedMessage) {
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
                        Text("¡Configuración guardada correctamente!", color = Green600, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Datos Profesionales y Registro SSN", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = nombreOficina,
                        onValueChange = { nombreOficina = it },
                        label = { Text("Nombre de la Organización / Broker") },
                        modifier = Modifier.fillMaxWidth().testTag("config_office_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nombreProductor,
                        onValueChange = { nombreProductor = it },
                        label = { Text("Nombre del Productor Asesor (PAS)") },
                        modifier = Modifier.fillMaxWidth().testTag("config_producer_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = matriculaSSN,
                        onValueChange = { matriculaSSN = it },
                        label = { Text("Matrícula Superintendencia de Seguros (SSN)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono de Contacto / WhatsApp") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Parámetros de Alertas y Moneda", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Text("Anticipación para alertas preventivas de renovación:", style = MaterialTheme.typography.bodySmall)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7 to "7 días", 15 to "15 días", 30 to "30 días").forEach { (days, label) ->
                            FilterChip(
                                selected = diasAlerta == days,
                                onClick = { diasAlerta = days },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    OutlinedTextField(
                        value = moneda,
                        onValueChange = { moneda = it },
                        label = { Text("Moneda por defecto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.saveConfiguracion(
                        ConfiguracionOficina(
                            id = 1,
                            nombreOficina = nombreOficina.trim(),
                            nombreProductor = nombreProductor.trim(),
                            matriculaSSN = matriculaSSN.trim(),
                            telefono = telefono.trim(),
                            email = email.trim(),
                            diasAlertaAnticipada = diasAlerta,
                            moneda = moneda.trim()
                        )
                    )
                    savedMessage = true
                },
                modifier = Modifier.fillMaxWidth().testTag("save_config_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar Cambios de Configuración")
            }
        }
    }
}
