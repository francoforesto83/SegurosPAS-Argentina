package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.utils.DemoDataProvider
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class SegurosRepository(private val database: AppDatabase) {

    // Clientes
    val allClientes: Flow<List<Cliente>> = database.clienteDao().getAllClientes()
    suspend fun getAllClientesRaw(): List<Cliente> = database.clienteDao().getAllClientesRaw()
    suspend fun getClienteByIdSync(id: Long): Cliente? = database.clienteDao().getClienteByIdSync(id)
    suspend fun checkClienteDuplicate(doc: String): Cliente? = database.clienteDao().findByDocumento(doc)
    suspend fun insertCliente(cliente: Cliente): Long = database.clienteDao().insert(cliente)
    suspend fun updateCliente(cliente: Cliente) = database.clienteDao().update(cliente)
    suspend fun bajaCliente(id: Long) = database.clienteDao().bajaLogica(id)

    // Vehículos
    val allVehiculos: Flow<List<Vehiculo>> = database.vehiculoDao().getAllVehiculos()
    suspend fun getAllVehiculosRaw(): List<Vehiculo> = database.vehiculoDao().getAllVehiculosRaw()
    suspend fun getVehiculoByIdSync(id: Long): Vehiculo? = database.vehiculoDao().getVehiculoByIdSync(id)
    fun getVehiculosByCliente(clienteId: Long): Flow<List<Vehiculo>> = database.vehiculoDao().getVehiculosByCliente(clienteId)
    suspend fun checkVehiculoDuplicate(dominio: String): Vehiculo? = database.vehiculoDao().findByDominio(dominio)
    suspend fun insertVehiculo(vehiculo: Vehiculo): Long = database.vehiculoDao().insert(vehiculo)
    suspend fun updateVehiculo(vehiculo: Vehiculo) = database.vehiculoDao().update(vehiculo)
    suspend fun bajaVehiculo(id: Long) = database.vehiculoDao().bajaLogica(id)

    // Pólizas
    val allPolizas: Flow<List<Poliza>> = database.polizaDao().getAllPolizas()
    suspend fun getAllPolizasRaw(): List<Poliza> = database.polizaDao().getAllPolizasRaw()
    suspend fun getPolizaByIdSync(id: Long): Poliza? = database.polizaDao().getPolizaByIdSync(id)
    fun getPolizasByCliente(clienteId: Long): Flow<List<Poliza>> = database.polizaDao().getPolizasByCliente(clienteId)
    fun getPolizasByVehiculo(vehiculoId: Long): Flow<List<Poliza>> = database.polizaDao().getPolizasByVehiculo(vehiculoId)
    suspend fun insertPoliza(poliza: Poliza): Long = database.polizaDao().insert(poliza)
    suspend fun updatePoliza(poliza: Poliza) = database.polizaDao().update(poliza)
    suspend fun bajaPoliza(id: Long) = database.polizaDao().bajaLogica(id)

    /**
     * Renews a policy: creates a new one with linked previous policy id, and updates
     * the previous one's administrative state to "Renovada", preserving the complete historical record.
     */
    suspend fun renewPoliza(
        polizaAnterior: Poliza,
        nuevoNumero: String,
        nuevoInicio: String,
        nuevoFin: String,
        nuevoPremio: Double,
        nuevasCuotas: Int,
        nuevaFormaPago: String,
        observacionesRenovacion: String
    ): Long {
        // Mark previous as Renovada
        val updatedOld = polizaAnterior.copy(estadoAdministrativo = "Renovada")
        database.polizaDao().update(updatedOld)

        // Insert new renewed policy
        val nuevaPoliza = Poliza(
            numeroPoliza = nuevoNumero,
            endoso = "0",
            clienteId = polizaAnterior.clienteId,
            vehiculoId = polizaAnterior.vehiculoId,
            companiaId = polizaAnterior.companiaId,
            ramo = polizaAnterior.ramo,
            tipoCobertura = polizaAnterior.tipoCobertura,
            fechaEmision = nuevoInicio,
            fechaInicio = nuevoInicio,
            fechaFin = nuevoFin,
            premioTotal = nuevoPremio,
            formaPago = nuevaFormaPago,
            cantidadCuotas = nuevasCuotas,
            estadoPago = "Al Día",
            productor = polizaAnterior.productor,
            observaciones = observacionesRenovacion.ifBlank { "Renovación de póliza ${polizaAnterior.numeroPoliza}" },
            polizaAnteriorId = polizaAnterior.id,
            estadoAdministrativo = "Vigente",
            activo = true
        )
        return database.polizaDao().insert(nuevaPoliza)
    }

    // Compañías
    val allCompanias: Flow<List<CompaniaAseguradora>> = database.companiaDao().getAllCompanias()
    suspend fun getAllCompaniasRaw(): List<CompaniaAseguradora> = database.companiaDao().getAllCompaniasRaw()
    suspend fun insertCompania(compania: CompaniaAseguradora): Long = database.companiaDao().insert(compania)
    suspend fun updateCompania(compania: CompaniaAseguradora) = database.companiaDao().update(compania)
    suspend fun bajaCompania(id: Long) = database.companiaDao().bajaLogica(id)

    // Siniestros
    val allSiniestros: Flow<List<Siniestro>> = database.siniestroDao().getAllSiniestros()
    suspend fun getAllSiniestrosRaw(): List<Siniestro> = database.siniestroDao().getAllSiniestrosRaw()
    suspend fun getSiniestroByIdSync(id: Long): Siniestro? = database.siniestroDao().getSiniestroByIdSync(id)
    fun getSiniestrosByCliente(clienteId: Long): Flow<List<Siniestro>> = database.siniestroDao().getSiniestrosByCliente(clienteId)
    fun getSiniestrosByVehiculo(vehiculoId: Long): Flow<List<Siniestro>> = database.siniestroDao().getSiniestrosByVehiculo(vehiculoId)
    suspend fun insertSiniestro(siniestro: Siniestro): Long = database.siniestroDao().insert(siniestro)
    suspend fun updateSiniestro(siniestro: Siniestro) = database.siniestroDao().update(siniestro)
    suspend fun bajaSiniestro(id: Long) = database.siniestroDao().bajaLogica(id)

    // Gestiones de siniestro
    fun getGestionesBySiniestro(siniestroId: Long): Flow<List<GestionSiniestro>> = database.gestionSiniestroDao().getGestionesBySiniestro(siniestroId)
    suspend fun getAllGestionesRaw(): List<GestionSiniestro> = database.gestionSiniestroDao().getAllGestionesRaw()
    suspend fun insertGestion(gestion: GestionSiniestro): Long = database.gestionSiniestroDao().insert(gestion)
    suspend fun deleteGestion(id: Long) = database.gestionSiniestroDao().deleteById(id)

    // Alertas Revisadas
    val allAlertasRevisadas: Flow<List<AlertaRevisada>> = database.alertaRevisadaDao().getAllAlertasRevisadas()
    suspend fun marcarAlertaRevisada(clave: String) = database.alertaRevisadaDao().marcarRevisada(AlertaRevisada(clave))
    suspend fun desmarcarAlertaRevisada(clave: String) = database.alertaRevisadaDao().desmarcarRevisada(clave)

    // Configuración
    val configuracion: Flow<ConfiguracionOficina?> = database.configuracionDao().getConfiguracion()
    suspend fun saveConfiguracion(config: ConfiguracionOficina) = database.configuracionDao().saveConfiguracion(config)

    // Demo Data & Resets
    suspend fun reloadDemoData() {
        database.gestionSiniestroDao().clearAll()
        database.siniestroDao().clearAll()
        database.polizaDao().clearAll()
        database.vehiculoDao().clearAll()
        database.clienteDao().clearAll()
        database.companiaDao().clearAll()
        database.alertaRevisadaDao().clearAll()

        database.configuracionDao().saveConfiguracion(DemoDataProvider.getDefaultConfig())
        database.companiaDao().insertAll(DemoDataProvider.getDemoCompanias())
        database.clienteDao().insertAll(DemoDataProvider.getDemoClientes())
        database.vehiculoDao().insertAll(DemoDataProvider.getDemoVehiculos())
        database.polizaDao().insertAll(DemoDataProvider.getDemoPolizas())
        database.siniestroDao().insertAll(DemoDataProvider.getDemoSiniestros())
        database.gestionSiniestroDao().insertAll(DemoDataProvider.getDemoGestiones())
    }

    suspend fun clearAllData() {
        database.gestionSiniestroDao().clearAll()
        database.siniestroDao().clearAll()
        database.polizaDao().clearAll()
        database.vehiculoDao().clearAll()
        database.clienteDao().clearAll()
        database.alertaRevisadaDao().clearAll()
    }

    /**
     * Restores full backup JSON
     */
    suspend fun restoreFromJson(jsonString: String): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            var count = 0

            if (root.has("clientes")) {
                val array = root.getJSONArray("clientes")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    database.clienteDao().insert(
                        Cliente(
                            id = if (obj.has("id")) obj.getLong("id") else 0,
                            tipo = obj.optString("tipo", "PERSONA_FISICA"),
                            nombre = obj.optString("nombre", ""),
                            documentoTipo = obj.optString("documentoTipo", "DNI"),
                            documentoNumero = obj.optString("documentoNumero", ""),
                            fechaNacimiento = obj.optString("fechaNacimiento", ""),
                            telefono = obj.optString("telefono", ""),
                            whatsapp = obj.optString("whatsapp", ""),
                            email = obj.optString("email", ""),
                            domicilio = obj.optString("domicilio", ""),
                            localidad = obj.optString("localidad", ""),
                            provincia = obj.optString("provincia", "Buenos Aires"),
                            observaciones = obj.optString("observaciones", ""),
                            activo = obj.optBoolean("activo", true)
                        )
                    )
                    count++
                }
            }

            if (root.has("vehiculos")) {
                val array = root.getJSONArray("vehiculos")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    database.vehiculoDao().insert(
                        Vehiculo(
                            id = if (obj.has("id")) obj.getLong("id") else 0,
                            clienteId = obj.optLong("clienteId", 1),
                            dominio = obj.optString("dominio", ""),
                            marca = obj.optString("marca", ""),
                            modelo = obj.optString("modelo", ""),
                            anio = obj.optInt("anio", 2020),
                            tipoVehiculo = obj.optString("tipoVehiculo", "Automóvil"),
                            numeroChasis = obj.optString("numeroChasis", ""),
                            numeroMotor = obj.optString("numeroMotor", ""),
                            uso = obj.optString("uso", "Particular"),
                            conductorHabitual = obj.optString("conductorHabitual", ""),
                            vencimientoCarnet = obj.optString("vencimientoCarnet", ""),
                            vencimientoVtv = obj.optString("vencimientoVtv", ""),
                            observaciones = obj.optString("observaciones", ""),
                            activo = obj.optBoolean("activo", true)
                        )
                    )
                    count++
                }
            }

            if (root.has("polizas")) {
                val array = root.getJSONArray("polizas")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    database.polizaDao().insert(
                        Poliza(
                            id = if (obj.has("id")) obj.getLong("id") else 0,
                            numeroPoliza = obj.optString("numeroPoliza", ""),
                            endoso = obj.optString("endoso", "0"),
                            clienteId = obj.optLong("clienteId", 1),
                            vehiculoId = if (obj.isNull("vehiculoId")) null else obj.optLong("vehiculoId"),
                            companiaId = obj.optLong("companiaId", 1),
                            ramo = obj.optString("ramo", "Automotor"),
                            tipoCobertura = obj.optString("tipoCobertura", "Terceros Completo"),
                            fechaEmision = obj.optString("fechaEmision", ""),
                            fechaInicio = obj.optString("fechaInicio", ""),
                            fechaFin = obj.optString("fechaFin", ""),
                            premioTotal = obj.optDouble("premioTotal", 0.0),
                            formaPago = obj.optString("formaPago", "Débito Automático CBU"),
                            cantidadCuotas = obj.optInt("cantidadCuotas", 12),
                            estadoPago = obj.optString("estadoPago", "Al Día"),
                            productor = obj.optString("productor", "Productor Asesor"),
                            observaciones = obj.optString("observaciones", ""),
                            estadoAdministrativo = obj.optString("estadoAdministrativo", "Vigente"),
                            activo = obj.optBoolean("activo", true)
                        )
                    )
                    count++
                }
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
