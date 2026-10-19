package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SegurosRepository
import com.example.utils.DateUtils
import com.example.utils.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class DashboardKpis(
    val totalClientes: Int = 0,
    val totalVehiculos: Int = 0,
    val polizasVigentes: Int = 0,
    val polizasProximas: Int = 0,
    val polizasVencidas: Int = 0,
    val siniestrosAbiertos: Int = 0,
    val siniestrosEnGestion: Int = 0,
    val siniestrosCerrados: Int = 0,
    val carnetsPorVencer: Int = 0,
    val carnetsVencidos: Int = 0,
    val vtvsPorVencer: Int = 0,
    val vtvsVencidas: Int = 0,
    val vencimientos7Dias: Int = 0,
    val vencimientos15Dias: Int = 0,
    val vencimientos30Dias: Int = 0
)

data class AlertaItem(
    val clave: String,
    val tipo: TipoAlerta,
    val titulo: String,
    val subtitulo: String,
    val clienteNombre: String,
    val clienteTelefono: String,
    val clienteWhatsapp: String,
    val dominioVehiculo: String?,
    val fechaVencimiento: String,
    val diasRestantes: Long?,
    val urgencia: UrgenciaAlerta,
    val revisada: Boolean,
    val entidadId: Long
)

enum class TipoAlerta { POLIZA, CARNET, VTV, SINIESTRO }
enum class UrgenciaAlerta { VENCIDO, HOY, DIAS_7, DIAS_15, DIAS_30, FUTURO }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SegurosRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SegurosRepository(db)
    }

    // Raw sources
    val clientes = repository.allClientes
    val vehiculos = repository.allVehiculos
    val polizas = repository.allPolizas
    val companias = repository.allCompanias
    val siniestros = repository.allSiniestros
    val configuracion = repository.configuracion
    val alertasRevisadas = repository.allAlertasRevisadas

    // Search and filter states
    val searchClientes = MutableStateFlow("")
    val searchVehiculos = MutableStateFlow("")
    val searchPolizas = MutableStateFlow("")
    val filterRamoPoliza = MutableStateFlow("TODOS")
    val filterCompaniaPoliza = MutableStateFlow<Long?>(null)
    val filterEstadoPoliza = MutableStateFlow("TODOS")

    val searchSiniestros = MutableStateFlow("")
    val filterEstadoSiniestro = MutableStateFlow("TODOS")

    val filterTipoAlerta = MutableStateFlow("TODOS")
    val filterUrgenciaAlerta = MutableStateFlow("TODOS")

    // Dynamic Alertas calculation
    val alertas: StateFlow<List<AlertaItem>> = combine(
        clientes, vehiculos, polizas, siniestros, alertasRevisadas
    ) { clis, vehs, pols, sins, revs ->
        val clisMap = clis.associateBy { it.id }
        val vehsMap = vehs.associateBy { it.id }
        val revSet = revs.map { it.claveAlerta }.toSet()
        val list = mutableListOf<AlertaItem>()

        // 1. Policy Expirations
        pols.filter { it.activo && it.estadoAdministrativo != "Anulada" && it.estadoAdministrativo != "Renovada" }.forEach { p ->
            val days = DateUtils.daysUntil(p.fechaFin)
            if (days != null && days <= 30) {
                val urgencia = when {
                    days < 0 -> UrgenciaAlerta.VENCIDO
                    days == 0L -> UrgenciaAlerta.HOY
                    days in 1..7 -> UrgenciaAlerta.DIAS_7
                    days in 8..15 -> UrgenciaAlerta.DIAS_15
                    else -> UrgenciaAlerta.DIAS_30
                }
                val cli = clisMap[p.clienteId]
                val veh = p.vehiculoId?.let { vehsMap[it] }
                val key = "POLIZA_${p.id}_${p.fechaFin}"
                list.add(
                    AlertaItem(
                        clave = key,
                        tipo = TipoAlerta.POLIZA,
                        titulo = "Póliza N° ${p.numeroPoliza} (${p.ramo})",
                        subtitulo = veh?.let { "Vehículo: ${it.dominio} (${it.marca} ${it.modelo})" } ?: "Cobertura: ${p.tipoCobertura}",
                        clienteNombre = cli?.nombre ?: "Cliente #${p.clienteId}",
                        clienteTelefono = cli?.telefono ?: "",
                        clienteWhatsapp = cli?.whatsapp.takeIf { !it.isNullOrBlank() } ?: cli?.telefono ?: "",
                        dominioVehiculo = veh?.dominio,
                        fechaVencimiento = p.fechaFin,
                        diasRestantes = days,
                        urgencia = urgencia,
                        revisada = revSet.contains(key),
                        entidadId = p.id
                    )
                )
            }
        }

        // 2. Driver License Expirations (Carnets)
        vehs.filter { it.activo && it.vencimientoCarnet.isNotBlank() }.forEach { v ->
            val days = DateUtils.daysUntil(v.vencimientoCarnet)
            if (days != null && days <= 30) {
                val urgencia = when {
                    days < 0 -> UrgenciaAlerta.VENCIDO
                    days == 0L -> UrgenciaAlerta.HOY
                    days in 1..7 -> UrgenciaAlerta.DIAS_7
                    days in 8..15 -> UrgenciaAlerta.DIAS_15
                    else -> UrgenciaAlerta.DIAS_30
                }
                val cli = clisMap[v.clienteId]
                val key = "CARNET_${v.id}_${v.vencimientoCarnet}"
                list.add(
                    AlertaItem(
                        clave = key,
                        tipo = TipoAlerta.CARNET,
                        titulo = "Vencimiento Carnet de Conducir",
                        subtitulo = "Conductor: ${v.conductorHabitual.ifBlank { cli?.nombre ?: "" }} - Auto: ${v.dominio}",
                        clienteNombre = cli?.nombre ?: "Cliente #${v.clienteId}",
                        clienteTelefono = cli?.telefono ?: "",
                        clienteWhatsapp = cli?.whatsapp.takeIf { !it.isNullOrBlank() } ?: cli?.telefono ?: "",
                        dominioVehiculo = v.dominio,
                        fechaVencimiento = v.vencimientoCarnet,
                        diasRestantes = days,
                        urgencia = urgencia,
                        revisada = revSet.contains(key),
                        entidadId = v.id
                    )
                )
            }
        }

        // 3. VTV / RTO Expirations
        vehs.filter { it.activo && it.vencimientoVtv.isNotBlank() }.forEach { v ->
            val days = DateUtils.daysUntil(v.vencimientoVtv)
            if (days != null && days <= 30) {
                val urgencia = when {
                    days < 0 -> UrgenciaAlerta.VENCIDO
                    days == 0L -> UrgenciaAlerta.HOY
                    days in 1..7 -> UrgenciaAlerta.DIAS_7
                    days in 8..15 -> UrgenciaAlerta.DIAS_15
                    else -> UrgenciaAlerta.DIAS_30
                }
                val cli = clisMap[v.clienteId]
                val key = "VTV_${v.id}_${v.vencimientoVtv}"
                list.add(
                    AlertaItem(
                        clave = key,
                        tipo = TipoAlerta.VTV,
                        titulo = "Vencimiento VTV / RTO",
                        subtitulo = "Patente: ${v.dominio} (${v.marca} ${v.modelo} ${v.anio})",
                        clienteNombre = cli?.nombre ?: "Cliente #${v.clienteId}",
                        clienteTelefono = cli?.telefono ?: "",
                        clienteWhatsapp = cli?.whatsapp.takeIf { !it.isNullOrBlank() } ?: cli?.telefono ?: "",
                        dominioVehiculo = v.dominio,
                        fechaVencimiento = v.vencimientoVtv,
                        diasRestantes = days,
                        urgencia = urgencia,
                        revisada = revSet.contains(key),
                        entidadId = v.id
                    )
                )
            }
        }

        // 4. Claims Follow-up Overdue
        sins.filter { it.activo && it.estado != "Cerrado" && it.estado != "Resuelto" && it.estado != "Rechazado" && it.fechaProximoSeguimiento.isNotBlank() }.forEach { s ->
            val days = DateUtils.daysUntil(s.fechaProximoSeguimiento)
            if (days != null && days <= 7) {
                val urgencia = when {
                    days < 0 -> UrgenciaAlerta.VENCIDO
                    days == 0L -> UrgenciaAlerta.HOY
                    else -> UrgenciaAlerta.DIAS_7
                }
                val cli = clisMap[s.clienteId]
                val veh = s.vehiculoId?.let { vehsMap[it] }
                val key = "SINIESTRO_${s.id}_${s.fechaProximoSeguimiento}"
                list.add(
                    AlertaItem(
                        clave = key,
                        tipo = TipoAlerta.SINIESTRO,
                        titulo = "Seguimiento Siniestro ${s.numeroInterno}",
                        subtitulo = "Estado: ${s.estado} - ${s.tipoSiniestro}",
                        clienteNombre = cli?.nombre ?: "Cliente #${s.clienteId}",
                        clienteTelefono = cli?.telefono ?: "",
                        clienteWhatsapp = cli?.whatsapp.takeIf { !it.isNullOrBlank() } ?: cli?.telefono ?: "",
                        dominioVehiculo = veh?.dominio,
                        fechaVencimiento = s.fechaProximoSeguimiento,
                        diasRestantes = days,
                        urgencia = urgencia,
                        revisada = revSet.contains(key),
                        entidadId = s.id
                    )
                )
            }
        }

        // Sort: Vencidos first, then Hoy, then ascending days remaining
        list.sortedWith(compareBy({ it.revisada }, { it.diasRestantes ?: 999L }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard KPIs
    val dashboardKpis: StateFlow<DashboardKpis> = combine(
        clientes, vehiculos, polizas, siniestros, alertas
    ) { clis, vehs, pols, sins, alts ->
        var polVigentes = 0
        var polProximas = 0
        var polVencidas = 0

        pols.filter { it.activo }.forEach { p ->
            val days = DateUtils.daysUntil(p.fechaFin)
            if (p.estadoAdministrativo == "Anulada" || p.estadoAdministrativo == "Renovada") {
                // Not in active count
            } else if (days != null && days < 0) {
                polVencidas++
            } else if (days != null && days <= 15) {
                polProximas++
            } else {
                polVigentes++
            }
        }

        val sinsAbiertos = sins.count { it.activo && it.estado == "Nuevo" || it.estado == "Denunciado" }
        val sinsGestion = sins.count { it.activo && it.estado in listOf("Documentación pendiente", "En evaluación", "En reparación", "Pendiente de resolución") }
        val sinsCerrados = sins.count { it.activo && it.estado in listOf("Resuelto", "Cerrado", "Rechazado") }

        var carnetVencidos = 0
        var carnetProximos = 0
        var vtvVencidas = 0
        var vtvProximas = 0

        vehs.filter { it.activo }.forEach { v ->
            val dCarnet = DateUtils.daysUntil(v.vencimientoCarnet)
            if (dCarnet != null) {
                if (dCarnet < 0) carnetVencidos++ else if (dCarnet <= 30) carnetProximos++
            }
            val dVtv = DateUtils.daysUntil(v.vencimientoVtv)
            if (dVtv != null) {
                if (dVtv < 0) vtvVencidas++ else if (dVtv <= 30) vtvProximas++
            }
        }

        val vto7 = alts.count { !it.revisada && it.urgencia in listOf(UrgenciaAlerta.VENCIDO, UrgenciaAlerta.HOY, UrgenciaAlerta.DIAS_7) }
        val vto15 = alts.count { !it.revisada && it.urgencia in listOf(UrgenciaAlerta.VENCIDO, UrgenciaAlerta.HOY, UrgenciaAlerta.DIAS_7, UrgenciaAlerta.DIAS_15) }
        val vto30 = alts.count { !it.revisada }

        DashboardKpis(
            totalClientes = clis.count { it.activo },
            totalVehiculos = vehs.count { it.activo },
            polizasVigentes = polVigentes,
            polizasProximas = polProximas,
            polizasVencidas = polVencidas,
            siniestrosAbiertos = sinsAbiertos,
            siniestrosEnGestion = sinsGestion,
            siniestrosCerrados = sinsCerrados,
            carnetsPorVencer = carnetProximos,
            carnetsVencidos = carnetVencidos,
            vtvsPorVencer = vtvProximas,
            vtvsVencidas = vtvVencidas,
            vencimientos7Dias = vto7,
            vencimientos15Dias = vto15,
            vencimientos30Dias = vto30
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardKpis())

    // CRUD - Clientes
    fun saveCliente(cliente: Cliente, onDuplicateFound: ((Cliente) -> Unit)? = null, onSaved: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.checkClienteDuplicate(cliente.documentoNumero)
            if (existing != null && existing.id != cliente.id && cliente.id == 0L) {
                onDuplicateFound?.invoke(existing)
            }
            val id = if (cliente.id == 0L) {
                repository.insertCliente(cliente)
            } else {
                repository.updateCliente(cliente)
                cliente.id
            }
            onSaved(id)
        }
    }

    fun bajaCliente(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.bajaCliente(id)
        }
    }

    // CRUD - Vehículos
    fun saveVehiculo(vehiculo: Vehiculo, onDuplicateDominio: ((Vehiculo) -> Unit)? = null, onSaved: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.checkVehiculoDuplicate(vehiculo.dominio)
            if (existing != null && existing.id != vehiculo.id && vehiculo.id == 0L) {
                onDuplicateDominio?.invoke(existing)
            }
            val id = if (vehiculo.id == 0L) {
                repository.insertVehiculo(vehiculo)
            } else {
                repository.updateVehiculo(vehiculo)
                vehiculo.id
            }
            onSaved(id)
        }
    }

    fun bajaVehiculo(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.bajaVehiculo(id)
        }
    }

    fun getVehiculosForCliente(clienteId: Long) = repository.getVehiculosByCliente(clienteId)
    fun getPolizasForCliente(clienteId: Long) = repository.getPolizasByCliente(clienteId)
    fun getSiniestrosForCliente(clienteId: Long) = repository.getSiniestrosByCliente(clienteId)

    fun getPolizasForVehiculo(vehiculoId: Long) = repository.getPolizasByVehiculo(vehiculoId)
    fun getSiniestrosForVehiculo(vehiculoId: Long) = repository.getSiniestrosByVehiculo(vehiculoId)

    // CRUD - Pólizas
    fun savePoliza(poliza: Poliza, onSaved: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Recalculate automatic status from dates if not manually locked
            val days = DateUtils.daysUntil(poliza.fechaFin)
            val computedStatus = if (poliza.estadoAdministrativo in listOf("Cotización", "Pendiente de Emisión", "Renovada", "Anulada")) {
                poliza.estadoAdministrativo
            } else if (days != null && days < 0) {
                "Vencida"
            } else if (days != null && days <= 15) {
                "Próxima a vencer"
            } else {
                "Vigente"
            }

            val toSave = poliza.copy(estadoAdministrativo = computedStatus)
            val id = if (toSave.id == 0L) {
                repository.insertPoliza(toSave)
            } else {
                repository.updatePoliza(toSave)
                toSave.id
            }
            onSaved(id)
        }
    }

    fun renewPoliza(
        polizaAnterior: Poliza,
        nuevoNumero: String,
        nuevoInicio: String,
        nuevoFin: String,
        nuevoPremio: Double,
        nuevasCuotas: Int,
        nuevaFormaPago: String,
        observaciones: String,
        onDone: (Long) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = repository.renewPoliza(
                polizaAnterior = polizaAnterior,
                nuevoNumero = nuevoNumero,
                nuevoInicio = nuevoInicio,
                nuevoFin = nuevoFin,
                nuevoPremio = nuevoPremio,
                nuevasCuotas = nuevasCuotas,
                nuevaFormaPago = nuevaFormaPago,
                observacionesRenovacion = observaciones
            )
            onDone(newId)
        }
    }

    fun bajaPoliza(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.bajaPoliza(id)
        }
    }

    // CRUD - Compañías
    fun saveCompania(compania: CompaniaAseguradora, onSaved: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = if (compania.id == 0L) {
                repository.insertCompania(compania)
            } else {
                repository.updateCompania(compania)
                compania.id
            }
            onSaved(id)
        }
    }

    fun bajaCompania(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.bajaCompania(id)
        }
    }

    // CRUD - Siniestros
    fun saveSiniestro(siniestro: Siniestro, onSaved: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = if (siniestro.id == 0L) {
                repository.insertSiniestro(siniestro)
            } else {
                repository.updateSiniestro(siniestro)
                siniestro.id
            }
            onSaved(id)
        }
    }

    fun bajaSiniestro(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.bajaSiniestro(id)
        }
    }

    // Gestiones de siniestro
    fun getGestiones(siniestroId: Long) = repository.getGestionesBySiniestro(siniestroId)

    fun addGestion(siniestroId: Long, descripcion: String, responsable: String, tipoGestion: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertGestion(
                GestionSiniestro(
                    siniestroId = siniestroId,
                    fecha = DateUtils.currentTimestampFormatted(),
                    descripcion = descripcion,
                    responsable = responsable,
                    tipoGestion = tipoGestion
                )
            )
        }
    }

    fun deleteGestion(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGestion(id)
        }
    }

    // Alertas Revisadas
    fun toggleAlertaRevisada(clave: String, currentlyRevisada: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentlyRevisada) {
                repository.desmarcarAlertaRevisada(clave)
            } else {
                repository.marcarAlertaRevisada(clave)
            }
        }
    }

    // Configuración
    fun saveConfiguracion(config: ConfiguracionOficina) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveConfiguracion(config)
        }
    }

    // Demo Data & Resets
    fun reloadDemoData(onFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.reloadDemoData()
            onFinished()
        }
    }

    fun clearAllData(onFinished: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            onFinished()
        }
    }

    // Backup & Restore
    fun exportFullBackupJson(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val clis = repository.getAllClientesRaw()
            val vehs = repository.getAllVehiculosRaw()
            val pols = repository.getAllPolizasRaw()
            val comps = repository.getAllCompaniasRaw()
            val sins = repository.getAllSiniestrosRaw()
            val gests = repository.getAllGestionesRaw()
            val cfg = repository.configuracion.firstOrNull()

            val json = ExportUtils.createJsonBackup(clis, vehs, pols, comps, sins, gests, cfg)
            val filename = "Backup_SegurosPAS_${DateUtils.todayIso()}.json"
            val file = File(context.cacheDir, filename)
            file.writeText(json, Charsets.UTF_8)
            onReady(file)
        }
    }

    fun restoreBackupJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = repository.restoreFromJson(jsonString)
            if (res.isSuccess) {
                onResult(true, "Se restauraron correctamente ${res.getOrNull()} registros.")
            } else {
                onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Error al restaurar archivo")
            }
        }
    }

    // Excel / CSV Exports
    fun exportClientesExcel(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getAllClientesRaw().filter { it.activo }
            val (headers, rows) = ExportUtils.buildClientesCsvData(list)
            val file = ExportUtils.exportToExcelCsv(context, "Clientes_${DateUtils.todayIso()}.csv", headers, rows)
            onReady(file)
        }
    }

    fun exportPolizasExcel(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pols = repository.getAllPolizasRaw().filter { it.activo }
            val clis = repository.getAllClientesRaw().associateBy { it.id }
            val comps = repository.getAllCompaniasRaw().associateBy { it.id }
            val vehs = repository.getAllVehiculosRaw().associateBy { it.id }
            val (headers, rows) = ExportUtils.buildPolizasCsvData(pols, clis, comps, vehs)
            val file = ExportUtils.exportToExcelCsv(context, "Polizas_${DateUtils.todayIso()}.csv", headers, rows)
            onReady(file)
        }
    }

    fun exportVehiculosExcel(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val vehs = repository.getAllVehiculosRaw().filter { it.activo }
            val clis = repository.getAllClientesRaw().associateBy { it.id }
            val (headers, rows) = ExportUtils.buildVehiculosCsvData(vehs, clis)
            val file = ExportUtils.exportToExcelCsv(context, "Vehiculos_${DateUtils.todayIso()}.csv", headers, rows)
            onReady(file)
        }
    }

    fun exportSiniestrosExcel(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val sins = repository.getAllSiniestrosRaw().filter { it.activo }
            val clis = repository.getAllClientesRaw().associateBy { it.id }
            val comps = repository.getAllCompaniasRaw().associateBy { it.id }
            val vehs = repository.getAllVehiculosRaw().associateBy { it.id }
            val (headers, rows) = ExportUtils.buildSiniestrosCsvData(sins, clis, comps, vehs)
            val file = ExportUtils.exportToExcelCsv(context, "Siniestros_${DateUtils.todayIso()}.csv", headers, rows)
            onReady(file)
        }
    }

    fun exportVencimientosExcel(context: Context, onReady: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val alts = alertas.value
            val headers = listOf("Tipo", "Título", "Detalle", "Cliente", "Teléfono / WhatsApp", "Patente", "Fecha Vto", "Días Restantes", "Urgencia", "Estado Gestión")
            val rows = alts.map { a ->
                listOf(
                    a.tipo.name,
                    a.titulo,
                    a.subtitulo,
                    a.clienteNombre,
                    a.clienteWhatsapp.ifBlank { a.clienteTelefono },
                    a.dominioVehiculo ?: "N/A",
                    DateUtils.toArgFormat(a.fechaVencimiento),
                    (a.diasRestantes ?: 0).toString(),
                    a.urgencia.name,
                    if (a.revisada) "Revisado / Gestionado" else "Pendiente"
                )
            }
            val file = ExportUtils.exportToExcelCsv(context, "Vencimientos_${DateUtils.todayIso()}.csv", headers, rows)
            onReady(file)
        }
    }
}
