package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExportUtils {

    /**
     * CSV/Excel generator using UTF-8 BOM so Microsoft Excel for Windows and Mac opens it cleanly
     * with semicolons (standard for Argentine Spanish locale) or commas.
     */
    fun exportToExcelCsv(context: Context, filename: String, headers: List<String>, rows: List<List<String>>): File {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM so Excel opens accents and ñ properly
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Header row
                writer.append(headers.joinToString(";") { escapeCsv(it) })
                writer.append("\r\n")

                // Rows
                for (row in rows) {
                    writer.append(row.joinToString(";") { escapeCsv(it) })
                    writer.append("\r\n")
                }
                writer.flush()
            }
        }
        return file
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(";") || str.contains("\n") || str.contains("\r") || str.contains("\"")) {
            str = "\"$str\""
        }
        return str
    }

    fun shareExportedFile(context: Context, file: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir archivo Excel / CSV"))
        } catch (e: Exception) {
            // Fallback to simple intent if fileprovider not defined
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Archivo generado: ${file.name} (${file.length()} bytes)")
            }
            context.startActivity(Intent.createChooser(intent, "Descarga de archivo"))
        }
    }

    fun buildClientesCsvData(clientes: List<Cliente>): Pair<List<String>, List<List<String>>> {
        val headers = listOf("ID", "Tipo", "Nombre / Razón Social", "Tipo Doc", "Documento", "Fecha Nac.", "Teléfono", "WhatsApp", "Email", "Domicilio", "Localidad", "Provincia", "Observaciones", "Fecha Alta")
        val rows = clientes.map { c ->
            listOf(
                c.id.toString(),
                if (c.tipo == "PERSONA_FISICA") "Persona Física" else "Persona Jurídica",
                c.nombre,
                c.documentoTipo,
                c.documentoNumero,
                DateUtils.toArgFormat(c.fechaNacimiento),
                c.telefono,
                c.whatsapp,
                c.email,
                c.domicilio,
                c.localidad,
                c.provincia,
                c.observaciones,
                DateUtils.toArgFormat(DateUtils.todayIso())
            )
        }
        return Pair(headers, rows)
    }

    fun buildPolizasCsvData(
        polizas: List<Poliza>,
        clientesMap: Map<Long, Cliente>,
        companiasMap: Map<Long, CompaniaAseguradora>,
        vehiculosMap: Map<Long, Vehiculo>
    ): Pair<List<String>, List<List<String>>> {
        val headers = listOf(
            "N° Póliza", "Endoso", "Cliente Asegurado", "DNI/CUIT Cliente", "Aseguradora",
            "Ramo", "Cobertura", "Vehículo (Patente)", "Vigencia Desde", "Vigencia Hasta",
            "Premio Total ($ ARS)", "Forma de Pago", "Cuotas", "Estado Pago", "Estado Vigencia", "Productor", "Observaciones"
        )
        val rows = polizas.map { p ->
            val cli = clientesMap[p.clienteId]
            val comp = companiasMap[p.companiaId]
            val veh = p.vehiculoId?.let { vehiculosMap[it] }
            listOf(
                p.numeroPoliza,
                p.endoso,
                cli?.nombre ?: "ID ${p.clienteId}",
                cli?.documentoNumero ?: "",
                comp?.nombre ?: "ID ${p.companiaId}",
                p.ramo,
                p.tipoCobertura,
                veh?.let { "${it.dominio} (${it.marca} ${it.modelo})" } ?: "N/A",
                DateUtils.toArgFormat(p.fechaInicio),
                DateUtils.toArgFormat(p.fechaFin),
                String.format(java.util.Locale.US, "%.2f", p.premioTotal),
                p.formaPago,
                p.cantidadCuotas.toString(),
                p.estadoPago,
                p.estadoAdministrativo,
                p.productor,
                p.observaciones
            )
        }
        return Pair(headers, rows)
    }

    fun buildVehiculosCsvData(
        vehiculos: List<Vehiculo>,
        clientesMap: Map<Long, Cliente>
    ): Pair<List<String>, List<List<String>>> {
        val headers = listOf(
            "ID", "Patente / Dominio", "Marca", "Modelo", "Año", "Tipo",
            "Uso", "Titular", "Conductor Habitual", "Vto. Carnet", "Vto. VTV/RTO",
            "N° Chasis", "N° Motor", "Observaciones"
        )
        val rows = vehiculos.map { v ->
            val cli = clientesMap[v.clienteId]
            listOf(
                v.id.toString(),
                v.dominio,
                v.marca,
                v.modelo,
                v.anio.toString(),
                v.tipoVehiculo,
                v.uso,
                cli?.nombre ?: "ID ${v.clienteId}",
                v.conductorHabitual,
                DateUtils.toArgFormat(v.vencimientoCarnet),
                DateUtils.toArgFormat(v.vencimientoVtv),
                v.numeroChasis,
                v.numeroMotor,
                v.observaciones
            )
        }
        return Pair(headers, rows)
    }

    fun buildSiniestrosCsvData(
        siniestros: List<Siniestro>,
        clientesMap: Map<Long, Cliente>,
        companiasMap: Map<Long, CompaniaAseguradora>,
        vehiculosMap: Map<Long, Vehiculo>
    ): Pair<List<String>, List<List<String>>> {
        val headers = listOf(
            "N° Interno", "N° Denuncia Cía", "Cliente", "Aseguradora", "Vehículo",
            "Fecha", "Hora", "Lugar", "Tipo Siniestro", "Descripción",
            "Terceros", "Denuncia Policial", "Daños", "Importe Estimado ($)", "Estado", "Doc. Pendiente", "Responsable", "Observaciones"
        )
        val rows = siniestros.map { s ->
            val cli = clientesMap[s.clienteId]
            val comp = companiasMap[s.companiaId]
            val veh = s.vehiculoId?.let { vehiculosMap[it] }
            listOf(
                s.numeroInterno,
                s.numeroDenuncia,
                cli?.nombre ?: "ID ${s.clienteId}",
                comp?.nombre ?: "ID ${s.companiaId}",
                veh?.dominio ?: "N/A",
                DateUtils.toArgFormat(s.fechaSiniestro),
                s.horaSiniestro,
                s.lugar,
                s.tipoSiniestro,
                s.descripcion,
                s.datosTerceros,
                s.denunciaPolicial,
                s.danos,
                String.format(java.util.Locale.US, "%.2f", s.importeEstimado),
                s.estado,
                s.documentacionPendiente,
                s.responsable,
                s.observaciones
            )
        }
        return Pair(headers, rows)
    }

    /**
     * Backup to JSON string containing all tables
     */
    fun createJsonBackup(
        clientes: List<Cliente>,
        vehiculos: List<Vehiculo>,
        polizas: List<Poliza>,
        companias: List<CompaniaAseguradora>,
        siniestros: List<Siniestro>,
        gestiones: List<GestionSiniestro>,
        config: ConfiguracionOficina?
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("sistema", "SegurosPAS Argentina")

        val cArray = JSONArray()
        clientes.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("tipo", c.tipo)
            obj.put("nombre", c.nombre)
            obj.put("documentoTipo", c.documentoTipo)
            obj.put("documentoNumero", c.documentoNumero)
            obj.put("fechaNacimiento", c.fechaNacimiento)
            obj.put("telefono", c.telefono)
            obj.put("whatsapp", c.whatsapp)
            obj.put("email", c.email)
            obj.put("domicilio", c.domicilio)
            obj.put("localidad", c.localidad)
            obj.put("provincia", c.provincia)
            obj.put("observaciones", c.observaciones)
            obj.put("activo", c.activo)
            cArray.put(obj)
        }
        root.put("clientes", cArray)

        val vArray = JSONArray()
        vehiculos.forEach { v ->
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("clienteId", v.clienteId)
            obj.put("dominio", v.dominio)
            obj.put("marca", v.marca)
            obj.put("modelo", v.modelo)
            obj.put("anio", v.anio)
            obj.put("tipoVehiculo", v.tipoVehiculo)
            obj.put("numeroChasis", v.numeroChasis)
            obj.put("numeroMotor", v.numeroMotor)
            obj.put("uso", v.uso)
            obj.put("conductorHabitual", v.conductorHabitual)
            obj.put("vencimientoCarnet", v.vencimientoCarnet)
            obj.put("vencimientoVtv", v.vencimientoVtv)
            obj.put("observaciones", v.observaciones)
            obj.put("activo", v.activo)
            vArray.put(obj)
        }
        root.put("vehiculos", vArray)

        val pArray = JSONArray()
        polizas.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("numeroPoliza", p.numeroPoliza)
            obj.put("endoso", p.endoso)
            obj.put("clienteId", p.clienteId)
            obj.put("vehiculoId", p.vehiculoId ?: JSONObject.NULL)
            obj.put("companiaId", p.companiaId)
            obj.put("ramo", p.ramo)
            obj.put("tipoCobertura", p.tipoCobertura)
            obj.put("fechaEmision", p.fechaEmision)
            obj.put("fechaInicio", p.fechaInicio)
            obj.put("fechaFin", p.fechaFin)
            obj.put("premioTotal", p.premioTotal)
            obj.put("formaPago", p.formaPago)
            obj.put("cantidadCuotas", p.cantidadCuotas)
            obj.put("estadoPago", p.estadoPago)
            obj.put("productor", p.productor)
            obj.put("observaciones", p.observaciones)
            obj.put("estadoAdministrativo", p.estadoAdministrativo)
            obj.put("activo", p.activo)
            pArray.put(obj)
        }
        root.put("polizas", pArray)

        val compArray = JSONArray()
        companias.forEach { comp ->
            val obj = JSONObject()
            obj.put("id", comp.id)
            obj.put("nombre", comp.nombre)
            obj.put("cuit", comp.cuit)
            obj.put("telefono", comp.telefono)
            obj.put("email", comp.email)
            obj.put("sitioWeb", comp.sitioWeb)
            obj.put("contactoSiniestros", comp.contactoSiniestros)
            obj.put("contactoComercial", comp.contactoComercial)
            obj.put("observaciones", comp.observaciones)
            obj.put("activo", comp.activo)
            compArray.put(obj)
        }
        root.put("companias", compArray)

        val sArray = JSONArray()
        siniestros.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("numeroInterno", s.numeroInterno)
            obj.put("numeroDenuncia", s.numeroDenuncia)
            obj.put("clienteId", s.clienteId)
            obj.put("vehiculoId", s.vehiculoId ?: JSONObject.NULL)
            obj.put("polizaId", s.polizaId ?: JSONObject.NULL)
            obj.put("companiaId", s.companiaId)
            obj.put("fechaSiniestro", s.fechaSiniestro)
            obj.put("horaSiniestro", s.horaSiniestro)
            obj.put("lugar", s.lugar)
            obj.put("tipoSiniestro", s.tipoSiniestro)
            obj.put("descripcion", s.descripcion)
            obj.put("datosTerceros", s.datosTerceros)
            obj.put("danos", s.danos)
            obj.put("importeEstimado", s.importeEstimado)
            obj.put("documentacionPendiente", s.documentacionPendiente)
            obj.put("responsable", s.responsable)
            obj.put("estado", s.estado)
            obj.put("observaciones", s.observaciones)
            obj.put("activo", s.activo)
            sArray.put(obj)
        }
        root.put("siniestros", sArray)

        return root.toString(2)
    }
}
