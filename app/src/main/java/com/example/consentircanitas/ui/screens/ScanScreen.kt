package com.example.consentircanitas.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.consentircanitas.data.AdultoRepository
import com.example.consentircanitas.model.AdultoMayor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(navController: NavController) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mensaje by remember { mutableStateOf("Centra el código QR del carnet en el recuadro para registrar asistencia.") }
    var adultoEncontrado by remember { mutableStateOf<AdultoMayor?>(null) }
    var cedulaLeida by remember { mutableStateOf("") }
    var escaneando by remember { mutableStateOf(true) }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text("Tomar Asistencia", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Escanea el carnet",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Centra el código QR del carnet en el recuadro para registrar asistencia.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Visor de cámara — usa BarcodeView en vez de DecoratedBarcodeView
        // para evitar el overlay con texto de código de barras
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            // Fondo negro redondeado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
            )

            // Cámara
            if (cameraPermission.status.isGranted) {
                AndroidView(
                    factory = { ctx ->
                        BarcodeView(ctx).apply {
                            cameraSettings.requestedCameraId = 0

                            val callback = object : BarcodeCallback {
                                override fun barcodeResult(result: BarcodeResult?) {
                                    if (!escaneando) return
                                    val contenido = result?.text?.trim() ?: return
                                    if (contenido.isBlank()) return

                                    escaneando = false
                                    cedulaLeida = contenido

                                    android.util.Log.d("QR_DEBUG", "Leído: '$contenido'")

                                    val adulto = AdultoRepository.buscarPorCedula(
                                        context = ctx,
                                        cedulaQR = contenido
                                    )

                                    adultoEncontrado = adulto
                                    mensaje = if (adulto != null) {
                                        "Asistencia registrada correctamente."
                                    } else {
                                        "No se encontró a nadie con cédula: $contenido"
                                    }
                                }
                            }

                            decodeContinuous(callback)
                            resume()

                            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                                override fun onResume(owner: LifecycleOwner) { resume() }
                                override fun onPause(owner: LifecycleOwner) { pause() }
                                override fun onDestroy(owner: LifecycleOwner) { pause() }
                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            // Esquinas decorativas encima de la cámara
            val cornerColor = Color(0xFF20B486)
            val cornerSize = 28.dp
            val cornerStroke = 3.dp

            // Superior izquierda
            Box(modifier = Modifier.align(Alignment.TopStart)) {
                Box(modifier = Modifier.width(cornerSize).height(cornerStroke).background(cornerColor))
                Box(modifier = Modifier.width(cornerStroke).height(cornerSize).background(cornerColor))
            }
            // Superior derecha
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                Box(modifier = Modifier.width(cornerSize).height(cornerStroke).background(cornerColor).align(Alignment.TopEnd))
                Box(modifier = Modifier.width(cornerStroke).height(cornerSize).background(cornerColor).align(Alignment.TopEnd))
            }
            // Inferior izquierda
            Box(modifier = Modifier.align(Alignment.BottomStart)) {
                Box(modifier = Modifier.width(cornerSize).height(cornerStroke).background(cornerColor).align(Alignment.BottomStart))
                Box(modifier = Modifier.width(cornerStroke).height(cornerSize).background(cornerColor).align(Alignment.BottomStart))
            }
            // Inferior derecha
            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                Box(modifier = Modifier.width(cornerSize).height(cornerStroke).background(cornerColor).align(Alignment.BottomEnd))
                Box(modifier = Modifier.width(cornerStroke).height(cornerSize).background(cornerColor).align(Alignment.BottomEnd))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mensaje de estado
        Text(
            text = mensaje,
            color = if (adultoEncontrado != null) Color(0xFF20B486)
            else if (cedulaLeida.isNotBlank()) Color(0xFFFF6B6B)
            else Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        // Botón para escanear otro
        if (!escaneando) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    escaneando = true
                    adultoEncontrado = null
                    cedulaLeida = ""
                    mensaje = "Centra el código QR del carnet en el recuadro para registrar asistencia."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20B486))
            ) {
                Text("Escanear otro QR", fontWeight = FontWeight.SemiBold)
            }
        }

        // Tarjeta del adulto encontrado
        adultoEncontrado?.let { adulto ->
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text(
                        text = adulto.datosPersonales.nombreCompleto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("Cédula", adulto.datosPersonales.numeroDocumento)
                    InfoRow("Teléfono", adulto.datosPersonales.telefono)
                    InfoRow("EPS", adulto.datosMedicos.eps)
                    InfoRow("Tipo de sangre", adulto.datosMedicos.tipoSangre)

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (adulto.activo) Color(0xFF20B486).copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (adulto.activo) "✅  Participante activo" else "❌  Participante inactivo",
                            color = if (adulto.activo) Color(0xFF20B486) else Color.Red,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        Text(text = value.ifBlank { "—" }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}