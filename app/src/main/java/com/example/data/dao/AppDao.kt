package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllClientes(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes")
    suspend fun getAllClientesRaw(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    fun getClienteById(id: Long): Flow<Cliente?>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun getClienteByIdSync(id: Long): Cliente?

    @Query("SELECT * FROM clientes WHERE documentoNumero = :doc AND activo = 1 LIMIT 1")
    suspend fun findByDocumento(doc: String): Cliente?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: Cliente): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clientes: List<Cliente>)

    @Update
    suspend fun update(cliente: Cliente)

    @Query("UPDATE clientes SET activo = 0 WHERE id = :id")
    suspend fun bajaLogica(id: Long)

    @Delete
    suspend fun delete(cliente: Cliente)

    @Query("DELETE FROM clientes")
    suspend fun clearAll()
}

@Dao
interface VehiculoDao {
    @Query("SELECT * FROM vehiculos WHERE activo = 1 ORDER BY dominio ASC")
    fun getAllVehiculos(): Flow<List<Vehiculo>>

    @Query("SELECT * FROM vehiculos")
    suspend fun getAllVehiculosRaw(): List<Vehiculo>

    @Query("SELECT * FROM vehiculos WHERE clienteId = :clienteId AND activo = 1 ORDER BY dominio ASC")
    fun getVehiculosByCliente(clienteId: Long): Flow<List<Vehiculo>>

    @Query("SELECT * FROM vehiculos WHERE id = :id LIMIT 1")
    fun getVehiculoById(id: Long): Flow<Vehiculo?>

    @Query("SELECT * FROM vehiculos WHERE id = :id LIMIT 1")
    suspend fun getVehiculoByIdSync(id: Long): Vehiculo?

    @Query("SELECT * FROM vehiculos WHERE UPPER(REPLACE(dominio, ' ', '')) = UPPER(REPLACE(:dominio, ' ', '')) AND activo = 1 LIMIT 1")
    suspend fun findByDominio(dominio: String): Vehiculo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehiculo: Vehiculo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehiculos: List<Vehiculo>)

    @Update
    suspend fun update(vehiculo: Vehiculo)

    @Query("UPDATE vehiculos SET activo = 0 WHERE id = :id")
    suspend fun bajaLogica(id: Long)

    @Delete
    suspend fun delete(vehiculo: Vehiculo)

    @Query("DELETE FROM vehiculos")
    suspend fun clearAll()
}

@Dao
interface PolizaDao {
    @Query("SELECT * FROM polizas WHERE activo = 1 ORDER BY id DESC")
    fun getAllPolizas(): Flow<List<Poliza>>

    @Query("SELECT * FROM polizas")
    suspend fun getAllPolizasRaw(): List<Poliza>

    @Query("SELECT * FROM polizas WHERE clienteId = :clienteId AND activo = 1 ORDER BY id DESC")
    fun getPolizasByCliente(clienteId: Long): Flow<List<Poliza>>

    @Query("SELECT * FROM polizas WHERE vehiculoId = :vehiculoId AND activo = 1 ORDER BY id DESC")
    fun getPolizasByVehiculo(vehiculoId: Long): Flow<List<Poliza>>

    @Query("SELECT * FROM polizas WHERE id = :id LIMIT 1")
    fun getPolizaById(id: Long): Flow<Poliza?>

    @Query("SELECT * FROM polizas WHERE id = :id LIMIT 1")
    suspend fun getPolizaByIdSync(id: Long): Poliza?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poliza: Poliza): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(polizas: List<Poliza>)

    @Update
    suspend fun update(poliza: Poliza)

    @Query("UPDATE polizas SET activo = 0 WHERE id = :id")
    suspend fun bajaLogica(id: Long)

    @Delete
    suspend fun delete(poliza: Poliza)

    @Query("DELETE FROM polizas")
    suspend fun clearAll()
}

@Dao
interface CompaniaDao {
    @Query("SELECT * FROM companias_aseguradoras WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllCompanias(): Flow<List<CompaniaAseguradora>>

    @Query("SELECT * FROM companias_aseguradoras")
    suspend fun getAllCompaniasRaw(): List<CompaniaAseguradora>

    @Query("SELECT * FROM companias_aseguradoras WHERE id = :id LIMIT 1")
    fun getCompaniaById(id: Long): Flow<CompaniaAseguradora?>

    @Query("SELECT * FROM companias_aseguradoras WHERE id = :id LIMIT 1")
    suspend fun getCompaniaByIdSync(id: Long): CompaniaAseguradora?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(compania: CompaniaAseguradora): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(companias: List<CompaniaAseguradora>)

    @Update
    suspend fun update(compania: CompaniaAseguradora)

    @Query("UPDATE companias_aseguradoras SET activo = 0 WHERE id = :id")
    suspend fun bajaLogica(id: Long)

    @Query("DELETE FROM companias_aseguradoras")
    suspend fun clearAll()
}

@Dao
interface SiniestroDao {
    @Query("SELECT * FROM siniestros WHERE activo = 1 ORDER BY id DESC")
    fun getAllSiniestros(): Flow<List<Siniestro>>

    @Query("SELECT * FROM siniestros")
    suspend fun getAllSiniestrosRaw(): List<Siniestro>

    @Query("SELECT * FROM siniestros WHERE clienteId = :clienteId AND activo = 1 ORDER BY id DESC")
    fun getSiniestrosByCliente(clienteId: Long): Flow<List<Siniestro>>

    @Query("SELECT * FROM siniestros WHERE vehiculoId = :vehiculoId AND activo = 1 ORDER BY id DESC")
    fun getSiniestrosByVehiculo(vehiculoId: Long): Flow<List<Siniestro>>

    @Query("SELECT * FROM siniestros WHERE id = :id LIMIT 1")
    fun getSiniestroById(id: Long): Flow<Siniestro?>

    @Query("SELECT * FROM siniestros WHERE id = :id LIMIT 1")
    suspend fun getSiniestroByIdSync(id: Long): Siniestro?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(siniestro: Siniestro): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(siniestros: List<Siniestro>)

    @Update
    suspend fun update(siniestro: Siniestro)

    @Query("UPDATE siniestros SET activo = 0 WHERE id = :id")
    suspend fun bajaLogica(id: Long)

    @Query("DELETE FROM siniestros")
    suspend fun clearAll()
}

@Dao
interface GestionSiniestroDao {
    @Query("SELECT * FROM gestiones_siniestro WHERE siniestroId = :siniestroId ORDER BY id DESC")
    fun getGestionesBySiniestro(siniestroId: Long): Flow<List<GestionSiniestro>>

    @Query("SELECT * FROM gestiones_siniestro")
    suspend fun getAllGestionesRaw(): List<GestionSiniestro>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gestion: GestionSiniestro): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(gestiones: List<GestionSiniestro>)

    @Query("DELETE FROM gestiones_siniestro WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM gestiones_siniestro")
    suspend fun clearAll()
}

@Dao
interface AlertaRevisadaDao {
    @Query("SELECT * FROM alertas_revisadas")
    fun getAllAlertasRevisadas(): Flow<List<AlertaRevisada>>

    @Query("SELECT * FROM alertas_revisadas")
    suspend fun getAllAlertasRevisadasRaw(): List<AlertaRevisada>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun marcarRevisada(alerta: AlertaRevisada)

    @Query("DELETE FROM alertas_revisadas WHERE claveAlerta = :clave")
    suspend fun desmarcarRevisada(clave: String)

    @Query("DELETE FROM alertas_revisadas")
    suspend fun clearAll()
}

@Dao
interface ConfiguracionDao {
    @Query("SELECT * FROM configuracion_oficina WHERE id = 1 LIMIT 1")
    fun getConfiguracion(): Flow<ConfiguracionOficina?>

    @Query("SELECT * FROM configuracion_oficina WHERE id = 1 LIMIT 1")
    suspend fun getConfiguracionSync(): ConfiguracionOficina?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfiguracion(config: ConfiguracionOficina)
}
