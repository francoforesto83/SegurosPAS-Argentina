package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String = "PERSONA_FISICA", // PERSONA_FISICA | PERSONA_JURIDICA
    val nombre: String, // Nombre y Apellido o Razón Social
    val documentoTipo: String = "DNI", // DNI | CUIT | CUIL | Pasaporte
    val documentoNumero: String, // ej. 34.567.890 o 20-34567890-9
    val fechaNacimiento: String = "", // DD/MM/AAAA
    val telefono: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val domicilio: String = "",
    val localidad: String = "",
    val provincia: String = "Buenos Aires",
    val observaciones: String = "",
    val fechaAlta: Long = System.currentTimeMillis(),
    val activo: Boolean = true // Baja lógica
)

@Entity(tableName = "vehiculos")
data class Vehiculo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteId: Long,
    val dominio: String, // Patente ej. AF 123 CD o AA 123 BB o AAA 123
    val marca: String,
    val modelo: String,
    val anio: Int,
    val tipoVehiculo: String = "Automóvil", // Automóvil, Pick-up, SUV, Motocicleta, Camión, Utilitario
    val numeroChasis: String = "",
    val numeroMotor: String = "",
    val uso: String = "Particular", // Particular, Comercial, Carga, Transporte
    val conductorHabitual: String = "",
    val vencimientoCarnet: String = "", // YYYY-MM-DD
    val vencimientoVtv: String = "", // YYYY-MM-DD
    val observaciones: String = "",
    val activo: Boolean = true
)

@Entity(tableName = "polizas")
data class Poliza(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numeroPoliza: String,
    val endoso: String = "0",
    val clienteId: Long,
    val vehiculoId: Long? = null,
    val companiaId: Long,
    val ramo: String = "Automotor", // Automotor, Hogar, Vida, Accidentes Personales, Comercio, Responsabilidad Civil, Integral
    val tipoCobertura: String = "Terceros Completo", // RC, Terceros Básico, Terceros Completo con Granizo, Todo Riesgo
    val fechaEmision: String = "", // YYYY-MM-DD
    val fechaInicio: String, // YYYY-MM-DD
    val fechaFin: String, // YYYY-MM-DD
    val premioTotal: Double = 0.0,
    val formaPago: String = "Débito Automático CBU", // Débito CBU, Tarjeta de Crédito, Cupón / Pago Fácil, Efectivo
    val cantidadCuotas: Int = 12,
    val estadoPago: String = "Al Día", // Al Día, Cuota Pendiente, En Mora, Pagado Total
    val productor: String = "Productor Asesor",
    val observaciones: String = "",
    val archivoReferencia: String = "",
    val polizaAnteriorId: Long? = null, // Historial de renovación consecutiva
    val estadoAdministrativo: String = "Vigente", // Cotización, Pendiente, Vigente, Próxima a vencer, Vencida, Renovada, Anulada
    val activo: Boolean = true
)

@Entity(tableName = "companias_aseguradoras")
data class CompaniaAseguradora(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val cuit: String = "",
    val telefono: String = "",
    val email: String = "",
    val sitioWeb: String = "",
    val contactoSiniestros: String = "",
    val contactoComercial: String = "",
    val observaciones: String = "",
    val activo: Boolean = true
)

@Entity(tableName = "siniestros")
data class Siniestro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numeroInterno: String,
    val numeroDenuncia: String = "",
    val clienteId: Long,
    val vehiculoId: Long? = null,
    val polizaId: Long? = null,
    val companiaId: Long,
    val fechaSiniestro: String, // YYYY-MM-DD
    val horaSiniestro: String = "12:00",
    val lugar: String = "",
    val tipoSiniestro: String = "Choque simple", // Choque simple, Robo total, Robo ruedas/parcial, Granizo, Rotura cristales, Incendio, Terceros
    val descripcion: String = "",
    val datosTerceros: String = "",
    val datosOtrosVehiculos: String = "",
    val denunciaPolicial: String = "",
    val danos: String = "",
    val importeEstimado: Double = 0.0,
    val documentacionPendiente: String = "",
    val responsable: String = "Oficina",
    val fechaUltimoContacto: String = "", // YYYY-MM-DD
    val fechaProximoSeguimiento: String = "", // YYYY-MM-DD
    val estado: String = "Nuevo", // Nuevo, Denunciado, Documentación pendiente, En evaluación, En reparación, Pendiente de resolución, Resuelto, Cerrado, Rechazado
    val fechaCierre: String? = null,
    val observaciones: String = "",
    val activo: Boolean = true
)

@Entity(tableName = "gestiones_siniestro")
data class GestionSiniestro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siniestroId: Long,
    val fecha: String, // YYYY-MM-DD HH:mm
    val descripcion: String,
    val responsable: String,
    val tipoGestion: String = "Llamada" // Llamada, WhatsApp, Envío de Fotos, Peritaje, Presupuesto Taller, Liquidación
)

@Entity(tableName = "alertas_revisadas")
data class AlertaRevisada(
    @PrimaryKey val claveAlerta: String, // ej. "POLIZA_10_2026-10-25" o "VTV_3_2026-11-01"
    val fechaRevision: Long = System.currentTimeMillis()
)

@Entity(tableName = "configuracion_oficina")
data class ConfiguracionOficina(
    @PrimaryKey val id: Int = 1,
    val nombreOficina: String = "Organización de Seguros Argentina",
    val nombreProductor: String = "Productor Asesor de Seguros",
    val matriculaSSN: String = "PAS-89240",
    val telefono: String = "+54 9 11 5555-0123",
    val email: String = "contacto@segurospas.com.ar",
    val diasAlertaAnticipada: Int = 15, // 7, 15, 30
    val moneda: String = "ARS ($)"
)
