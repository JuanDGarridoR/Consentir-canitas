package com.example.consentircanitas.data

import android.content.Context
import com.example.consentircanitas.model.AdultoMayor
import com.example.consentircanitas.model.ContactoEmergencia
import com.example.consentircanitas.model.DatosMedicos
import com.example.consentircanitas.model.DatosPersonales
import org.json.JSONArray

object AdultoRepository {

    fun cargarAdultos(context: Context): List<AdultoMayor> {
        val json = context.assets.open("datos.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(json)
        val adultos = mutableListOf<AdultoMayor>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)

            val datosPersonalesJson = item.getJSONObject("datos_personales")
            val contactoJson = item.getJSONObject("contacto_emergencia")
            val datosMedicosJson = item.getJSONObject("datos_medicos")

            val adulto = AdultoMayor(
                datosPersonales = DatosPersonales(
                    nombreCompleto = datosPersonalesJson.optString("nombre_completo"),
                    numeroDocumento = datosPersonalesJson.optString("numero_documento"),
                    fechaNacimiento = datosPersonalesJson.optString("fecha_nacimiento"),
                    genero = datosPersonalesJson.optString("genero"),
                    direccion = datosPersonalesJson.optString("direccion"),
                    telefono = datosPersonalesJson.optString("telefono")
                ),
                contactoEmergencia = ContactoEmergencia(
                    nombre = contactoJson.optString("nombre"),
                    parentesco = contactoJson.optString("parentesco"),
                    telefono = contactoJson.optString("telefono")
                ),
                datosMedicos = DatosMedicos(
                    tipoSangre = datosMedicosJson.optString("tipo_sangre"),
                    eps = datosMedicosJson.optString("eps"),
                    enfermedades = datosMedicosJson.optString("enfermedades"),
                    alergias = datosMedicosJson.optString("alergias"),
                    discapacidad = datosMedicosJson.optString("discapacidad")
                )
            )

            adultos.add(adulto)
        }

        return adultos
    }

    fun buscarPorCedula(context: Context, cedulaQR: String): AdultoMayor? {
        val cedulaLimpia = cedulaQR.trim().replace("*", "")

        return cargarAdultos(context).firstOrNull { adulto ->
            adulto.datosPersonales.numeroDocumento == cedulaLimpia
        }
    }
}
