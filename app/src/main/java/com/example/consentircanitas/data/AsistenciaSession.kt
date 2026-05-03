package com.example.consentircanitas.data

import com.example.consentircanitas.model.AdultoMayor

object AsistenciaSession {
    private val _escaneados = mutableListOf<AdultoMayor>()

    val escaneados: List<AdultoMayor> get() = _escaneados.toList()

    fun registrar(adulto: AdultoMayor) {
        // Evitar duplicados
        if (_escaneados.none { it.datosPersonales.numeroDocumento == adulto.datosPersonales.numeroDocumento }) {
            _escaneados.add(adulto)
        }
    }

    fun limpiar() {
        _escaneados.clear()
    }
}