package com.example.consentircanitas.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActividadDia(
    val titulo: String = "",
    val descripcion: String = ""
)

object AsistenciaRepository {

    private val _cedulasPresentes = mutableStateListOf<String>()

    val cedulasPresentes: List<String>
        get() = _cedulasPresentes

    fun obtenerFechaHoy(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun obtenerClaveAsistencia(fecha: String): String {
        return "asistencia_$fecha"
    }

    private fun obtenerClaveTitulo(fecha: String): String {
        return "actividad_titulo_$fecha"
    }

    private fun obtenerClaveDescripcion(fecha: String): String {
        return "actividad_descripcion_$fecha"
    }

    fun cargarAsistenciaHoy(context: Context) {
        cargarAsistenciaPorFecha(context, obtenerFechaHoy())
    }

    fun cargarAsistenciaPorFecha(context: Context, fecha: String) {
        val preferencias = context.getSharedPreferences("asistencia", Context.MODE_PRIVATE)

        val cedulasGuardadas = preferencias.getStringSet(
            obtenerClaveAsistencia(fecha),
            emptySet()
        ) ?: emptySet()

        _cedulasPresentes.clear()
        _cedulasPresentes.addAll(cedulasGuardadas)
    }

    fun registrarPresente(context: Context, cedula: String) {
        registrarPresenteEnFecha(
            context = context,
            cedula = cedula,
            fecha = obtenerFechaHoy()
        )
    }

    fun registrarPresenteEnFecha(context: Context, cedula: String, fecha: String) {
        val cedulaLimpia = cedula.trim().replace("*", "")

        if (cedulaLimpia.isBlank()) return

        cargarAsistenciaPorFecha(context, fecha)

        if (!_cedulasPresentes.contains(cedulaLimpia)) {
            _cedulasPresentes.add(cedulaLimpia)
        }

        val preferencias = context.getSharedPreferences("asistencia", Context.MODE_PRIVATE)

        preferencias.edit()
            .putStringSet(obtenerClaveAsistencia(fecha), _cedulasPresentes.toSet())
            .apply()
    }

    fun estaPresente(cedula: String): Boolean {
        val cedulaLimpia = cedula.trim().replace("*", "")
        return _cedulasPresentes.contains(cedulaLimpia)
    }

    fun totalPresentes(): Int {
        return _cedulasPresentes.size
    }

    fun guardarActividadDia(
        context: Context,
        fecha: String,
        titulo: String,
        descripcion: String
    ) {
        val preferencias = context.getSharedPreferences("asistencia", Context.MODE_PRIVATE)

        preferencias.edit()
            .putString(obtenerClaveTitulo(fecha), titulo)
            .putString(obtenerClaveDescripcion(fecha), descripcion)
            .apply()
    }

    fun obtenerActividadDia(context: Context, fecha: String): ActividadDia {
        val preferencias = context.getSharedPreferences("asistencia", Context.MODE_PRIVATE)

        return ActividadDia(
            titulo = preferencias.getString(obtenerClaveTitulo(fecha), "") ?: "",
            descripcion = preferencias.getString(obtenerClaveDescripcion(fecha), "") ?: ""
        )
    }
}