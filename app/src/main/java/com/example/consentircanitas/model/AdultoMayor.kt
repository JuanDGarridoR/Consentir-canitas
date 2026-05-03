package com.example.consentircanitas.model

data class AdultoMayor(
    val datosPersonales: DatosPersonales,
    val contactoEmergencia: ContactoEmergencia,
    val datosMedicos: DatosMedicos
) {
    // La cédula es única, así que se usa como identificador interno.
    val id: String
        get() = datosPersonales.numeroDocumento
}

data class DatosPersonales(
    val nombreCompleto: String,
    val numeroDocumento: String,
    val fechaNacimiento: String,
    val genero: String,
    val direccion: String,
    val telefono: String
)

data class ContactoEmergencia(
    val nombre: String,
    val parentesco: String,
    val telefono: String
)

data class DatosMedicos(
    val tipoSangre: String,
    val eps: String,
    val enfermedades: String,
    val alergias: String,
    val discapacidad: String
)
