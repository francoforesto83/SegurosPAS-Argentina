package com.example.utils

import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

object ArgentinaFormats {
    private val localeAr = Locale("es", "AR")
    private val currencyFormat = NumberFormat.getCurrencyInstance(localeAr)

    fun formatCurrency(amount: Double): String {
        return try {
            currencyFormat.format(amount)
        } catch (e: Exception) {
            "$ $amount"
        }
    }

    /**
     * Formats Argentine Document:
     * DNI: 34.567.890
     * CUIT/CUIL: 20-34567890-9
     */
    fun formatDocumento(docRaw: String, tipo: String): String {
        val clean = docRaw.replace(Regex("[^0-9]"), "")
        return when {
            tipo.equals("CUIT", ignoreCase = true) || tipo.equals("CUIL", ignoreCase = true) || clean.length == 11 -> {
                if (clean.length == 11) {
                    "${clean.substring(0, 2)}-${clean.substring(2, 10)}-${clean.substring(10)}"
                } else clean
            }
            tipo.equals("DNI", ignoreCase = true) && clean.length in 7..8 -> {
                if (clean.length == 8) {
                    "${clean.substring(0, 2)}.${clean.substring(2, 5)}.${clean.substring(5)}"
                } else {
                    "${clean.substring(0, 1)}.${clean.substring(1, 4)}.${clean.substring(4)}"
                }
            }
            else -> docRaw
        }
    }

    /**
     * Standardizes Argentine License Plate (Patente):
     * Mercosur: AA 123 BB
     * Traditional: AAA 123
     */
    fun formatPatente(raw: String): String {
        val clean = raw.uppercase().replace(Regex("[^A-Z0-9]"), "")
        return when (clean.length) {
            7 -> { // Mercosur: AA123BB -> AA 123 BB
                "${clean.substring(0, 2)} ${clean.substring(2, 5)} ${clean.substring(5, 7)}"
            }
            6 -> { // Traditional: AAA123 -> AAA 123
                "${clean.substring(0, 3)} ${clean.substring(3, 6)}"
            }
            else -> clean
        }
    }

    /**
     * Sanitizes phone number to international Argentina format (+54 9 ...)
     */
    fun sanitizeWhatsAppNumber(phone: String): String {
        var clean = phone.replace(Regex("[^0-9+]"), "")
        if (clean.startsWith("+549")) return clean.replace("+", "")
        if (clean.startsWith("+54")) {
            val rest = clean.removePrefix("+54")
            return "549" + (if (rest.startsWith("9")) rest.removePrefix("9") else rest).removePrefix("0").removePrefix("15")
        }
        if (clean.startsWith("54")) return clean
        // If national number like 011 15-4444-5555 or 11 4444-5555
        var num = clean.removePrefix("0")
        if (num.startsWith("15")) num = num.removePrefix("15")
        return "549$num"
    }

    /**
     * Builds WhatsApp Web/App intent URI with encoded Argentine message
     */
    fun buildWhatsAppUrl(phone: String, message: String): String {
        val number = sanitizeWhatsAppNumber(phone)
        val encodedMsg = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            ""
        }
        return "https://wa.me/$number?text=$encodedMsg"
    }
}
