package com.example.consentircanitas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.consentircanitas.data.AdultoRepository
import com.example.consentircanitas.data.AsistenciaRepository
import com.example.consentircanitas.model.AdultoMayor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ModoCalendario {
    CERRADO,
    DIAS,
    MESES,
    ANIOS
}

@Composable
fun AttendanceScreen(navController: NavController) {

    val context = LocalContext.current

    var fechaSeleccionada by remember {
        mutableStateOf(AsistenciaRepository.obtenerFechaHoy())
    }

    var modoCalendario by remember {
        mutableStateOf(ModoCalendario.CERRADO)
    }

    var adultos by remember { mutableStateOf<List<AdultoMayor>>(emptyList()) }
    var tituloActividad by remember { mutableStateOf("") }
    var descripcionActividad by remember { mutableStateOf("") }
    var mensajeGuardado by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun cargarDatosDelDia(fecha: String) {
        try {
            AsistenciaRepository.cargarAsistenciaPorFecha(context, fecha)
            adultos = AdultoRepository.cargarAdultos(context)

            val actividad = AsistenciaRepository.obtenerActividadDia(context, fecha)
            tituloActividad = actividad.titulo
            descripcionActividad = actividad.descripcion

            mensajeGuardado = ""
            error = null
        } catch (e: Exception) {
            error = "No se pudo cargar la asistencia: ${e.message}"
        }
    }

    LaunchedEffect(fechaSeleccionada) {
        cargarDatosDelDia(fechaSeleccionada)
    }

    val adultosOrdenados = adultos.sortedWith(
        compareByDescending<AdultoMayor> {
            AsistenciaRepository.estaPresente(it.datosPersonales.numeroDocumento)
        }.thenBy {
            it.datosPersonales.nombreCompleto
        }
    )

    val totalPresentes = adultos.count {
        AsistenciaRepository.estaPresente(it.datosPersonales.numeroDocumento)
    }

    val totalAusentes = adultos.size - totalPresentes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Text(
                text = "Registro por Días",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Fecha de asistencia",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFEAF8F3),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    modoCalendario =
                                        if (modoCalendario == ModoCalendario.CERRADO) {
                                            ModoCalendario.DIAS
                                        } else {
                                            ModoCalendario.CERRADO
                                        }
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = formatearFechaBonita(fechaSeleccionada),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF20B486),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Toca para cambiar la fecha",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        if (modoCalendario != ModoCalendario.CERRADO) {
                            Spacer(modifier = Modifier.height(16.dp))

                            SelectorCalendario(
                                fechaSeleccionada = fechaSeleccionada,
                                modoCalendario = modoCalendario,
                                onModoChange = { nuevoModo ->
                                    modoCalendario = nuevoModo
                                },
                                onFechaCambioSinCerrar = { nuevaFecha ->
                                    fechaSeleccionada = nuevaFecha
                                },
                                onFechaSeleccionada = { nuevaFecha ->
                                    fechaSeleccionada = nuevaFecha
                                    modoCalendario = ModoCalendario.CERRADO
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                fechaSeleccionada = AsistenciaRepository.obtenerFechaHoy()
                                modoCalendario = ModoCalendario.CERRADO
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ir a hoy")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Actividad del día",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = tituloActividad,
                            onValueChange = { tituloActividad = it },
                            label = { Text("Título de la actividad") },
                            placeholder = { Text("Ej: Taller de memoria") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = descripcionActividad,
                            onValueChange = { descripcionActividad = it },
                            label = { Text("Descripción") },
                            placeholder = { Text("Ej: Se realizó una actividad de memoria y coordinación.") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                AsistenciaRepository.guardarActividadDia(
                                    context = context,
                                    fecha = fechaSeleccionada,
                                    titulo = tituloActividad,
                                    descripcion = descripcionActividad
                                )

                                mensajeGuardado = "Actividad guardada correctamente."
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF20B486)
                            )
                        ) {
                            Text("Guardar actividad")
                        }

                        if (mensajeGuardado.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = mensajeGuardado,
                                color = Color(0xFF20B486),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumenCard(
                        titulo = "Presentes",
                        cantidad = totalPresentes,
                        color = Color(0xFF20B486),
                        modifier = Modifier.weight(1f)
                    )

                    ResumenCard(
                        titulo = "Ausentes",
                        cantidad = totalAusentes,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            error?.let {
                item {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            items(
                items = adultosOrdenados,
                key = { adulto -> adulto.id }
            ) { adulto ->

                val presente = AsistenciaRepository.estaPresente(
                    adulto.datosPersonales.numeroDocumento
                )

                AdultoAsistenciaItem(
                    adulto = adulto,
                    presente = presente
                )
            }
        }
    }
}

@Composable
fun SelectorCalendario(
    fechaSeleccionada: String,
    modoCalendario: ModoCalendario,
    onModoChange: (ModoCalendario) -> Unit,
    onFechaCambioSinCerrar: (String) -> Unit,
    onFechaSeleccionada: (String) -> Unit
) {
    when (modoCalendario) {
        ModoCalendario.DIAS -> {
            VistaDiasDelMes(
                fechaSeleccionada = fechaSeleccionada,
                onModoChange = onModoChange,
                onFechaCambioSinCerrar = onFechaCambioSinCerrar,
                onFechaSeleccionada = onFechaSeleccionada
            )
        }

        ModoCalendario.MESES -> {
            VistaMeses(
                fechaSeleccionada = fechaSeleccionada,
                onModoChange = onModoChange,
                onFechaCambioSinCerrar = onFechaCambioSinCerrar
            )
        }

        ModoCalendario.ANIOS -> {
            VistaAnios(
                fechaSeleccionada = fechaSeleccionada,
                onModoChange = onModoChange,
                onFechaCambioSinCerrar = onFechaCambioSinCerrar
            )
        }

        ModoCalendario.CERRADO -> {}
    }
}

@Composable
fun VistaDiasDelMes(
    fechaSeleccionada: String,
    onModoChange: (ModoCalendario) -> Unit,
    onFechaCambioSinCerrar: (String) -> Unit,
    onFechaSeleccionada: (String) -> Unit
) {
    val calendario = obtenerCalendarioDesdeFecha(fechaSeleccionada)
    val anio = calendario.get(Calendar.YEAR)
    val mes = calendario.get(Calendar.MONTH)

    val diasCalendario = generarDiasDelMes(fechaSeleccionada)
    val nombresDias = listOf("L", "M", "M", "J", "V", "S", "D")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF7F7F7),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    onFechaCambioSinCerrar(sumarMeses(fechaSeleccionada, -1))
                }
            ) {
                Text("<")
            }

            Text(
                text = "${nombreMes(mes)} $anio",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.clickable {
                    onModoChange(ModoCalendario.MESES)
                }
            )

            TextButton(
                onClick = {
                    onFechaCambioSinCerrar(sumarMeses(fechaSeleccionada, 1))
                }
            ) {
                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            nombresDias.forEach { dia ->
                Text(
                    text = dia,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        diasCalendario.chunked(7).forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth()) {
                semana.forEach { dia ->
                    val fechaDia = if (dia != null) {
                        construirFecha(anio, mes, dia)
                    } else {
                        ""
                    }

                    val seleccionado = fechaDia == fechaSeleccionada

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dia != null) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = if (seleccionado) {
                                            Color(0xFF20B486)
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onFechaSeleccionada(fechaDia)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dia.toString(),
                                    color = if (seleccionado) Color.White else Color.Black,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Toca el mes para ver todos los meses.",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VistaMeses(
    fechaSeleccionada: String,
    onModoChange: (ModoCalendario) -> Unit,
    onFechaCambioSinCerrar: (String) -> Unit
) {
    val calendario = obtenerCalendarioDesdeFecha(fechaSeleccionada)
    val anio = calendario.get(Calendar.YEAR)
    val mesSeleccionado = calendario.get(Calendar.MONTH)

    val meses = listOf(
        "Ene", "Feb", "Mar", "Abr",
        "May", "Jun", "Jul", "Ago",
        "Sep", "Oct", "Nov", "Dic"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF7F7F7),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    onFechaCambioSinCerrar(sumarAnios(fechaSeleccionada, -1))
                }
            ) {
                Text("<")
            }

            Text(
                text = anio.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.clickable {
                    onModoChange(ModoCalendario.ANIOS)
                }
            )

            TextButton(
                onClick = {
                    onFechaCambioSinCerrar(sumarAnios(fechaSeleccionada, 1))
                }
            ) {
                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        meses.chunked(3).forEachIndexed { filaIndex, fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fila.forEachIndexed { columnaIndex, nombreMes ->
                    val mesIndex = filaIndex * 3 + columnaIndex
                    val seleccionado = mesIndex == mesSeleccionado

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(
                                color = if (seleccionado) Color(0xFF20B486) else Color.White,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                val nuevaFecha = cambiarMes(fechaSeleccionada, mesIndex)
                                onFechaCambioSinCerrar(nuevaFecha)
                                onModoChange(ModoCalendario.DIAS)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nombreMes,
                            color = if (seleccionado) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = "Toca el año para ver otros años.",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VistaAnios(
    fechaSeleccionada: String,
    onModoChange: (ModoCalendario) -> Unit,
    onFechaCambioSinCerrar: (String) -> Unit
) {
    val calendario = obtenerCalendarioDesdeFecha(fechaSeleccionada)
    val anioSeleccionado = calendario.get(Calendar.YEAR)

    var inicioRango by remember(fechaSeleccionada) {
        mutableStateOf(anioSeleccionado - 5)
    }

    val anios = (inicioRango..inicioRango + 11).toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF7F7F7),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    inicioRango -= 12
                }
            ) {
                Text("<")
            }

            Text(
                text = "$inicioRango - ${inicioRango + 11}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            TextButton(
                onClick = {
                    inicioRango += 12
                }
            ) {
                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        anios.chunked(3).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fila.forEach { anio ->
                    val seleccionado = anio == anioSeleccionado

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(
                                color = if (seleccionado) Color(0xFF20B486) else Color.White,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                val nuevaFecha = cambiarAnio(fechaSeleccionada, anio)
                                onFechaCambioSinCerrar(nuevaFecha)
                                onModoChange(ModoCalendario.MESES)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = anio.toString(),
                            color = if (seleccionado) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ResumenCard(
    titulo: String,
    cantidad: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = cantidad.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = titulo,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun AdultoAsistenciaItem(
    adulto: AdultoMayor,
    presente: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = if (presente) Color(0xFF20B486) else Color.LightGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = adulto.datosPersonales.nombreCompleto
                        .take(1)
                        .uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = adulto.datosPersonales.nombreCompleto,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Documento: ${adulto.datosPersonales.numeroDocumento}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Text(
                    text = "Teléfono: ${adulto.datosPersonales.telefono}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Text(
                text = if (presente) "Presente" else "Ausente",
                color = if (presente) Color(0xFF20B486) else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun obtenerCalendarioDesdeFecha(fecha: String): Calendar {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = formato.parse(fecha) ?: Date()

    return Calendar.getInstance().apply {
        time = date
        firstDayOfWeek = Calendar.MONDAY
    }
}

fun construirFecha(anio: Int, mes: Int, dia: Int): String {
    val calendario = Calendar.getInstance().apply {
        set(Calendar.YEAR, anio)
        set(Calendar.MONTH, mes)
        set(Calendar.DAY_OF_MONTH, dia)
    }

    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formato.format(calendario.time)
}

fun generarDiasDelMes(fecha: String): List<Int?> {
    val calendario = obtenerCalendarioDesdeFecha(fecha)

    calendario.set(Calendar.DAY_OF_MONTH, 1)

    val diasEnMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    val diaSemanaPrimerDia = calendario.get(Calendar.DAY_OF_WEEK)

    val espaciosAntes = when (diaSemanaPrimerDia) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    val lista = mutableListOf<Int?>()

    repeat(espaciosAntes) {
        lista.add(null)
    }

    for (dia in 1..diasEnMes) {
        lista.add(dia)
    }

    while (lista.size % 7 != 0) {
        lista.add(null)
    }

    return lista
}

fun sumarMeses(fecha: String, meses: Int): String {
    val calendario = obtenerCalendarioDesdeFecha(fecha)
    val diaActual = calendario.get(Calendar.DAY_OF_MONTH)

    calendario.set(Calendar.DAY_OF_MONTH, 1)
    calendario.add(Calendar.MONTH, meses)

    val maxDiaNuevoMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    calendario.set(Calendar.DAY_OF_MONTH, minOf(diaActual, maxDiaNuevoMes))

    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formato.format(calendario.time)
}

fun sumarAnios(fecha: String, anios: Int): String {
    val calendario = obtenerCalendarioDesdeFecha(fecha)
    val diaActual = calendario.get(Calendar.DAY_OF_MONTH)

    calendario.set(Calendar.DAY_OF_MONTH, 1)
    calendario.add(Calendar.YEAR, anios)

    val maxDiaNuevoMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    calendario.set(Calendar.DAY_OF_MONTH, minOf(diaActual, maxDiaNuevoMes))

    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formato.format(calendario.time)
}

fun cambiarMes(fecha: String, nuevoMes: Int): String {
    val calendario = obtenerCalendarioDesdeFecha(fecha)
    val diaActual = calendario.get(Calendar.DAY_OF_MONTH)

    calendario.set(Calendar.DAY_OF_MONTH, 1)
    calendario.set(Calendar.MONTH, nuevoMes)

    val maxDiaNuevoMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    calendario.set(Calendar.DAY_OF_MONTH, minOf(diaActual, maxDiaNuevoMes))

    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formato.format(calendario.time)
}

fun cambiarAnio(fecha: String, nuevoAnio: Int): String {
    val calendario = obtenerCalendarioDesdeFecha(fecha)
    val diaActual = calendario.get(Calendar.DAY_OF_MONTH)

    calendario.set(Calendar.DAY_OF_MONTH, 1)
    calendario.set(Calendar.YEAR, nuevoAnio)

    val maxDiaNuevoMes = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    calendario.set(Calendar.DAY_OF_MONTH, minOf(diaActual, maxDiaNuevoMes))

    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formato.format(calendario.time)
}

fun nombreMes(mes: Int): String {
    return when (mes) {
        0 -> "Enero"
        1 -> "Febrero"
        2 -> "Marzo"
        3 -> "Abril"
        4 -> "Mayo"
        5 -> "Junio"
        6 -> "Julio"
        7 -> "Agosto"
        8 -> "Septiembre"
        9 -> "Octubre"
        10 -> "Noviembre"
        11 -> "Diciembre"
        else -> ""
    }
}

fun formatearFechaBonita(fecha: String): String {
    return try {
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "CO"))

        val date = formatoEntrada.parse(fecha) ?: Date()

        formatoSalida.format(date).replaceFirstChar {
            it.uppercase()
        }
    } catch (e: Exception) {
        fecha
    }
}