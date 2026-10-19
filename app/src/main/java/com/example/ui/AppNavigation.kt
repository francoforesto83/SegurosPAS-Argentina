package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    CLIENTES("Clientes", Icons.Default.People),
    VEHICULOS("Vehículos", Icons.Default.DirectionsCar),
    POLIZAS("Pólizas", Icons.Default.Description),
    VENCIMIENTOS("Vencimientos", Icons.Default.Warning),
    SINIESTROS("Siniestros", Icons.Default.CarCrash),
    ASEGURADORAS("Aseguradoras", Icons.Default.Business),
    EXPORTAR("Informes y Excel", Icons.Default.FileDownload),
    BACKUP("Copias de Seguridad", Icons.Default.Backup),
    CONFIGURACION("Configuración", Icons.Default.Settings)
}
