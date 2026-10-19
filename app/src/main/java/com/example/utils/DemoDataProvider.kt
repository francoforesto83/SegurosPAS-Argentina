package com.example.utils

import com.example.data.model.*

object DemoDataProvider {

    fun getDefaultConfig(): ConfiguracionOficina {
        return ConfiguracionOficina(
            id = 1,
            nombreOficina = "Organización Foresto - Asesores de Seguros",
            nombreProductor = "Franco Foresto",
            matriculaSSN = "PAS-89240",
            telefono = "+54 9 11 5555-0123",
            email = "francoforesto83@gmail.com",
            diasAlertaAnticipada = 15,
            moneda = "ARS ($)"
        )
    }

    fun getDemoCompanias(): List<CompaniaAseguradora> {
        return listOf(
            CompaniaAseguradora(
                id = 1,
                nombre = "La Segunda Seguros",
                cuit = "30-50004946-0",
                telefono = "0800-444-0504",
                email = "siniestros@lasegunda.com.ar",
                sitioWeb = "https://www.lasegunda.com.ar",
                contactoSiniestros = "0800-777-2484 (Mesa de Ayuda 24hs)",
                contactoComercial = "comercial@lasegunda.com.ar",
                observaciones = "Líder en coberturas automotor, agro e integral."
            ),
            CompaniaAseguradora(
                id = 2,
                nombre = "San Cristóbal Seguros",
                cuit = "30-50004314-4",
                telefono = "0810-222-8888",
                email = "contacto@sancristobal.com.ar",
                sitioWeb = "https://www.sancristobal.com.ar",
                contactoSiniestros = "WhatsApp +54 9 341 228-8888",
                contactoComercial = "agencias@sancristobal.com.ar",
                observaciones = "Excelente respuesta en reposición de cristales y auxilio mecánico."
            ),
            CompaniaAseguradora(
                id = 3,
                nombre = "Federación Patronal Seguros",
                cuit = "30-50004812-9",
                telefono = "0810-222-3372",
                email = "atencion@fedpat.com.ar",
                sitioWeb = "https://www.fedpat.com.ar",
                contactoSiniestros = "0800-222-0022 / App FedPat Móvil",
                contactoComercial = "productores@fedpat.com.ar",
                observaciones = "Solvencia destacada en el mercado asegurador argentino."
            ),
            CompaniaAseguradora(
                id = 4,
                nombre = "Sancor Seguros",
                cuit = "30-50004977-0",
                telefono = "0800-444-2850",
                email = "siniestros@sancorseguros.com",
                sitioWeb = "https://www.sancorseguros.com.ar",
                contactoSiniestros = "0800-777-4648",
                contactoComercial = "redcomercial@sancorseguros.com",
                observaciones = "Amplia cobertura en salud, vida, accidentes personales y autos."
            ),
            CompaniaAseguradora(
                id = 5,
                nombre = "Seguros Rivadavia",
                cuit = "30-50005196-1",
                telefono = "0810-999-3200",
                email = "info@segurosrivadavia.com",
                sitioWeb = "https://www.segurosrivadavia.com",
                contactoSiniestros = "0800-666-6789",
                contactoComercial = "operaciones@segurosrivadavia.com",
                observaciones = "Especialistas en automotores y transporte."
            )
        )
    }

    fun getDemoClientes(): List<Cliente> {
        return listOf(
            Cliente(
                id = 1,
                tipo = "PERSONA_FISICA",
                nombre = "Martín Alejandro Rossi",
                documentoTipo = "DNI",
                documentoNumero = "32.456.789",
                fechaNacimiento = "14/05/1986",
                telefono = "+54 9 11 4455-6677",
                whatsapp = "+54 9 11 4455-6677",
                email = "martin.rossi@gmail.com",
                domicilio = "Av. Santa Fe 3240, Piso 4 B",
                localidad = "Palermo, CABA",
                provincia = "CABA",
                observaciones = "Cliente puntual en pagos por débito directo. 2 vehículos y seguro de hogar.",
                fechaAlta = System.currentTimeMillis() - 86400000L * 180
            ),
            Cliente(
                id = 2,
                tipo = "PERSONA_FISICA",
                nombre = "Luciana Gómez Pereyra",
                documentoTipo = "DNI",
                documentoNumero = "35.890.123",
                fechaNacimiento = "22/11/1990",
                telefono = "+54 9 351 556-7890",
                whatsapp = "+54 9 351 556-7890",
                email = "luciana.gomez@hotmail.com",
                domicilio = "Bv. Chacabuco 780",
                localidad = "Córdoba Capital",
                provincia = "Córdoba",
                observaciones = "Profesional independiente. Póliza de auto y seguro de vida.",
                fechaAlta = System.currentTimeMillis() - 86400000L * 120
            ),
            Cliente(
                id = 3,
                tipo = "PERSONA_JURIDICA",
                nombre = "Distribuidora Los Andes S.R.L.",
                documentoTipo = "CUIT",
                documentoNumero = "30-71458923-8",
                fechaNacimiento = "",
                telefono = "+54 9 261 423-1122",
                whatsapp = "+54 9 261 544-3322",
                email = "administracion@losandesdistribuidora.com.ar",
                domicilio = "Carril Rodríguez Peña 2450",
                localidad = "Godoy Cruz",
                provincia = "Mendoza",
                observaciones = "Flota de 3 utilitarios y seguro integral de depósito comercial.",
                fechaAlta = System.currentTimeMillis() - 86400000L * 300
            ),
            Cliente(
                id = 4,
                tipo = "PERSONA_FISICA",
                nombre = "Carlos Eduardo Benítez",
                documentoTipo = "DNI",
                documentoNumero = "27.654.321",
                fechaNacimiento = "03/08/1979",
                telefono = "+54 9 341 688-9900",
                whatsapp = "+54 9 341 688-9900",
                email = "carlos.benitez@arnet.com.ar",
                domicilio = "Bv. Oroño 1420",
                localidad = "Rosario",
                provincia = "Santa Fe",
                observaciones = "Tiene siniestro reciente en gestión con San Cristóbal.",
                fechaAlta = System.currentTimeMillis() - 86400000L * 90
            ),
            Cliente(
                id = 5,
                tipo = "PERSONA_FISICA",
                nombre = "Valeria Inés Domínguez",
                documentoTipo = "DNI",
                documentoNumero = "38.123.456",
                fechaNacimiento = "19/02/1994",
                telefono = "+54 9 11 6722-1144",
                whatsapp = "+54 9 11 6722-1144",
                email = "valeria.dominguez@outlook.com",
                domicilio = "Calle 48 N° 832",
                localidad = "La Plata",
                provincia = "Buenos Aires",
                observaciones = "VTV por vencer la próxima semana.",
                fechaAlta = System.currentTimeMillis() - 86400000L * 60
            )
        )
    }

    fun getDemoVehiculos(): List<Vehiculo> {
        return listOf(
            Vehiculo(
                id = 1,
                clienteId = 1,
                dominio = "AF 382 KR",
                marca = "Toyota",
                modelo = "Corolla Cross XEI 2.0",
                anio = 2023,
                tipoVehiculo = "SUV",
                numeroChasis = "8AJBA3BE7P123987",
                numeroMotor = "M20A-FKS-987654",
                uso = "Particular",
                conductorHabitual = "Martín Alejandro Rossi",
                vencimientoCarnet = "2026-10-24", // Vence en 7 días!
                vencimientoVtv = "2027-04-15",
                observaciones = "Vehículo en impecable estado. Guardado en garaje."
            ),
            Vehiculo(
                id = 2,
                clienteId = 1,
                dominio = "AA 415 ZQ",
                marca = "Volkswagen",
                modelo = "Gol Trend 1.6 MSI",
                anio = 2017,
                tipoVehiculo = "Automóvil",
                numeroChasis = "9BWAA45U3HP112233",
                numeroMotor = "CFZ-445566",
                uso = "Particular",
                conductorHabitual = "Camila Rossi",
                vencimientoCarnet = "2027-08-10",
                vencimientoVtv = "2026-10-18", // Vence MAÑANA!
                observaciones = "Uso urbano para traslados universitarios."
            ),
            Vehiculo(
                id = 3,
                clienteId = 2,
                dominio = "AG 120 TY",
                marca = "Peugeot",
                modelo = "208 Allure 1.6",
                anio = 2024,
                tipoVehiculo = "Automóvil",
                numeroChasis = "8AD2P4F41RP778899",
                numeroMotor = "EC5-889900",
                uso = "Particular",
                conductorHabitual = "Luciana Gómez Pereyra",
                vencimientoCarnet = "2026-10-17", // Vence HOY!
                vencimientoVtv = "2027-01-20",
                observaciones = "0km asegurado todo riesgo."
            ),
            Vehiculo(
                id = 4,
                clienteId = 3,
                dominio = "AF 902 MN",
                marca = "Renault",
                modelo = "Kangoo Express Confort 1.6 SCe",
                anio = 2022,
                tipoVehiculo = "Utilitario",
                numeroChasis = "8A11RF4A6NL334455",
                numeroMotor = "H4M-334455",
                uso = "Comercial / Carga",
                conductorHabitual = "Jorge Valdez (Chofer)",
                vencimientoCarnet = "2026-11-05", // Vence en 19 días
                vencimientoVtv = "2026-10-10", // Vencida hace 7 días!
                observaciones = "Mantenimiento al día en concesionaria."
            ),
            Vehiculo(
                id = 5,
                clienteId = 4,
                dominio = "AD 540 PL",
                marca = "Ford",
                modelo = "Ranger XLS 3.2 4x4",
                anio = 2019,
                tipoVehiculo = "Pick-up",
                numeroChasis = "8AFDY6A92KJ667788",
                numeroMotor = "P5AT-667788",
                uso = "Particular / Mixto",
                conductorHabitual = "Carlos Eduardo Benítez",
                vencimientoCarnet = "2027-03-30",
                vencimientoVtv = "2026-11-12", // Vence en 26 días
                observaciones = "Tiene siniestro por impacto trasero registrado."
            ),
            Vehiculo(
                id = 6,
                clienteId = 5,
                dominio = "AC 881 BV",
                marca = "Chevrolet",
                modelo = "Cruze LTZ 1.4T Sedán",
                anio = 2018,
                tipoVehiculo = "Automóvil",
                numeroChasis = "9BGBC69V0JG114477",
                numeroMotor = "LE2-998811",
                uso = "Particular",
                conductorHabitual = "Valeria Inés Domínguez",
                vencimientoCarnet = "2026-10-22", // Vence en 5 días!
                vencimientoVtv = "2026-10-25", // Vence en 8 días!
                observaciones = "Alerta de VTV y Carnet coordinada para renovación."
            )
        )
    }

    fun getDemoPolizas(): List<Poliza> {
        return listOf(
            // Póliza 1: Vigente próxima a vencer (vence en 5 días)
            Poliza(
                id = 1,
                numeroPoliza = "POL-2025-88401",
                endoso = "0",
                clienteId = 1,
                vehiculoId = 1,
                companiaId = 1, // La Segunda
                ramo = "Automotor",
                tipoCobertura = "Todo Riesgo con Franquicia ($150.000)",
                fechaEmision = "2025-10-22",
                fechaInicio = "2025-10-22",
                fechaFin = "2026-10-22", // Vence en 5 días
                premioTotal = 145000.0,
                formaPago = "Débito Automático CBU",
                cantidadCuotas = 12,
                estadoPago = "Al Día",
                productor = "Franco Foresto",
                observaciones = "Póliza anual con refacturación trimestral.",
                estadoAdministrativo = "Próxima a vencer"
            ),
            // Póliza 2: Vencida hace 3 días
            Poliza(
                id = 2,
                numeroPoliza = "POL-2025-77312",
                endoso = "0",
                clienteId = 1,
                vehiculoId = 2,
                companiaId = 2, // San Cristóbal
                ramo = "Automotor",
                tipoCobertura = "Terceros Completo con Granizo",
                fechaEmision = "2025-10-14",
                fechaInicio = "2025-10-14",
                fechaFin = "2026-10-14", // Venció hace 3 días!
                premioTotal = 82000.0,
                formaPago = "Tarjeta de Crédito Visa",
                cantidadCuotas = 12,
                estadoPago = "Al Día",
                productor = "Franco Foresto",
                observaciones = "Requiere gestión urgente de renovación con el cliente.",
                estadoAdministrativo = "Vencida"
            ),
            // Póliza 3: Vigente amplia (vence en 2027)
            Poliza(
                id = 3,
                numeroPoliza = "POL-2026-99201",
                endoso = "0",
                clienteId = 2,
                vehiculoId = 3,
                companiaId = 3, // Federación Patronal
                ramo = "Automotor",
                tipoCobertura = "Todo Riesgo 0km",
                fechaEmision = "2026-03-01",
                fechaInicio = "2026-03-01",
                fechaFin = "2027-03-01",
                premioTotal = 189000.0,
                formaPago = "Débito Automático CBU",
                cantidadCuotas = 12,
                estadoPago = "Al Día",
                productor = "Franco Foresto",
                observaciones = "Reposición a nuevo en primer año.",
                estadoAdministrativo = "Vigente"
            ),
            // Póliza 4: Ramo Integral de Comercio (vence en 14 días)
            Poliza(
                id = 4,
                numeroPoliza = "INT-2025-33410",
                endoso = "1",
                clienteId = 3,
                vehiculoId = null,
                companiaId = 4, // Sancor
                ramo = "Integral de Comercio",
                tipoCobertura = "Incendio Edificio/Contenido, Robo y Cristales",
                fechaEmision = "2025-10-31",
                fechaInicio = "2025-10-31",
                fechaFin = "2026-10-31", // Vence en 14 días!
                premioTotal = 265000.0,
                formaPago = "Transferencia Bancaria",
                cantidadCuotas = 6,
                estadoPago = "Al Día",
                productor = "Franco Foresto",
                observaciones = "Inspección previa aprobada con sistema de cámaras.",
                estadoAdministrativo = "Próxima a vencer"
            ),
            // Póliza 5: Flota automotor utilitarios
            Poliza(
                id = 5,
                numeroPoliza = "FLT-2026-11890",
                endoso = "0",
                clienteId = 3,
                vehiculoId = 4,
                companiaId = 1, // La Segunda
                ramo = "Automotor",
                tipoCobertura = "Terceros Completo y Carga General",
                fechaEmision = "2026-01-15",
                fechaInicio = "2026-01-15",
                fechaFin = "2027-01-15",
                premioTotal = 95000.0,
                formaPago = "Cheque 30 días",
                cantidadCuotas = 12,
                estadoPago = "Al Día",
                productor = "Franco Foresto",
                observaciones = "Póliza con cláusula de no repetición.",
                estadoAdministrativo = "Vigente"
            ),
            // Póliza 6: Ranger con siniestro
            Poliza(
                id = 6,
                numeroPoliza = "POL-2025-55421",
                endoso = "2",
                clienteId = 4,
                vehiculoId = 5,
                companiaId = 2, // San Cristóbal
                ramo = "Automotor",
                tipoCobertura = "Todo Riesgo",
                fechaEmision = "2025-12-05",
                fechaInicio = "2025-12-05",
                fechaFin = "2026-12-05",
                premioTotal = 160000.0,
                formaPago = "Débito Automático CBU",
                cantidadCuotas = 12,
                estadoPago = "Al Día",
                productor = "Franco Foresto",
                observaciones = "Siniestro abierto en proceso de peritaje.",
                estadoAdministrativo = "Vigente"
            ),
            // Póliza 7: Chevrolet Cruze
            Poliza(
                id = 7,
                numeroPoliza = "POL-2026-12004",
                endoso = "0",
                clienteId = 5,
                vehiculoId = 6,
                companiaId = 5, // Seguros Rivadavia
                ramo = "Automotor",
                tipoCobertura = "Terceros Completo Premium",
                fechaEmision = "2026-06-10",
                fechaInicio = "2026-06-10",
                fechaFin = "2027-06-10",
                premioTotal = 98000.0,
                formaPago = "Tarjeta de Crédito Mastercard",
                cantidadCuotas = 12,
                estadoPago = "Al Día",
                productor = "Franco Foresto",
                observaciones = "Excelente cliente, renovó automáticamente.",
                estadoAdministrativo = "Vigente"
            )
        )
    }

    fun getDemoSiniestros(): List<Siniestro> {
        return listOf(
            Siniestro(
                id = 1,
                numeroInterno = "SIN-2026-0042",
                numeroDenuncia = "SC-889124/26",
                clienteId = 4,
                vehiculoId = 5,
                polizaId = 6,
                companiaId = 2, // San Cristóbal
                fechaSiniestro = "2026-10-12",
                horaSiniestro = "18:45",
                lugar = "Av. Pellegrini y Corrientes, Rosario",
                tipoSiniestro = "Choque simple",
                descripcion = "Impacto trasero por frenada brusca en semáforo. Daño en paragolpes trasero y portón.",
                datosTerceros = "Mariano Duarte - Cel: +54 9 341 555-1122 - Seguro: La Caja",
                datosOtrosVehiculos = "Fiat Cronos - Patente: AE 491 QW",
                denunciaPolicial = "Comisaría 2da Rosario - Acta 1092/26",
                danos = "Paragolpes trasero fisurado, rotura de ópticas traseras izquierdas y abolladura en portón.",
                importeEstimado = 780000.0,
                documentacionPendiente = "Presupuesto de taller y fotos del tercero.",
                responsable = "Franco Foresto",
                fechaUltimoContacto = "2026-10-15",
                fechaProximoSeguimiento = "2026-10-18",
                estado = "En evaluación",
                observaciones = "Cliente solicitó celeridad porque necesita la camioneta para trabajar."
            ),
            Siniestro(
                id = 2,
                numeroInterno = "SIN-2026-0038",
                numeroDenuncia = "LS-441209/26",
                clienteId = 1,
                vehiculoId = 1,
                polizaId = 1,
                companiaId = 1, // La Segunda
                fechaSiniestro = "2026-09-28",
                horaSiniestro = "14:15",
                lugar = "Ruta Panamericana Km 42",
                tipoSiniestro = "Granizo / Cristales",
                descripcion = "Piedra de granizo quebró parabrisas delantero y abolló capot.",
                datosTerceros = "Sin terceros involucrados (fenómeno climático).",
                danos = "Parabrisas con astillado radial y microbollos en techo y capot.",
                importeEstimado = 420000.0,
                documentacionPendiente = "Ninguna. Orden de reposición enviada a Cristales San Martín.",
                responsable = "Franco Foresto",
                fechaUltimoContacto = "2026-10-10",
                fechaProximoSeguimiento = "2026-10-20",
                estado = "En reparación",
                observaciones = "Turno en taller especializado asignado para el 22/10."
            )
        )
    }

    fun getDemoGestiones(): List<GestionSiniestro> {
        return listOf(
            GestionSiniestro(
                id = 1,
                siniestroId = 1,
                fecha = "12/10/2026 19:30",
                descripcion = "Llamada inicial del cliente notificando colisión trasera. Se envían pautas para denuncia y toma de fotografías.",
                responsable = "Franco Foresto",
                tipoGestion = "Llamada"
            ),
            GestionSiniestro(
                id = 2,
                siniestroId = 1,
                fecha = "13/10/2026 11:00",
                descripcion = "Carga de la denuncia administrativa en el portal de San Cristóbal y generación de número de siniestro.",
                responsable = "Franco Foresto",
                tipoGestion = "Denuncia"
            ),
            GestionSiniestro(
                id = 3,
                siniestroId = 1,
                fecha = "15/10/2026 16:45",
                descripcion = "Contacto con el perito asignado de la compañía. Se remitieron fotos de alta resolución.",
                responsable = "Franco Foresto",
                tipoGestion = "Peritaje"
            ),
            GestionSiniestro(
                id = 4,
                siniestroId = 2,
                fecha = "29/09/2026 09:15",
                descripcion = "Autorización inmediata de reposición de parabrisas original homologado.",
                responsable = "Franco Foresto",
                tipoGestion = "Liquidación"
            )
        )
    }
}
