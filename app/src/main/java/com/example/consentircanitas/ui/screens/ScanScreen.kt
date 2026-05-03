package com.example.consentircanitas.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.consentircanitas.data.AdultoRepository
import com.example.consentircanitas.data.AsistenciaRepository
import com.example.consentircanitas.model.AdultoMayor
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
fun ScanScreen(navController: NavController) {

    val context = LocalContext.current

    var codigoLeido by remember { mutableStateOf("") }
    var cedulaBuscada by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("Apunta la cámara al código de barras del carnet.") }
    var adultoEncontrado by remember { mutableStateOf<AdultoMayor?>(null) }

    var permisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permisoCamara = granted

        if (!granted) {
            mensaje = "Debes permitir el uso de la cámara para escanear."
        }
    }

    LaunchedEffect(Unit) {
        AsistenciaRepository.cargarAsistenciaHoy(context)

        if (!permisoCamara) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun procesarCodigo(codigo: String) {
        codigoLeido = codigo.trim().replace("*", "")

        val cedulaExtraida = extraerCedulaDesdeCodigo(codigoLeido)
        cedulaBuscada = cedulaExtraida

        val adulto = AdultoRepository.buscarPorCedula(
            context = context,
            cedulaQR = cedulaExtraida
        )

        adultoEncontrado = adulto

        if (adulto != null) {
            AsistenciaRepository.registrarPresente(
                context = context,
                cedula = adulto.datosPersonales.numeroDocumento
            )

            mensaje = "${adulto.datosPersonales.nombreCompleto} marcado como PRESENTE."
        } else {
            mensaje = "No se encontró una persona con la cédula $cedulaExtraida."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Text(
                text = "Tomar Asistencia",
                color = Color.White,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .width(330.dp)
                .height(220.dp)
                .align(Alignment.CenterHorizontally)
                .border(
                    width = 3.dp,
                    color = Color(0xFF20B486),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(6.dp)
                .clip(RoundedCornerShape(20.dp))
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            if (permisoCamara) {
                BarcodeCameraPreview(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .clipToBounds(),
                    onBarcodeDetected = { codigo ->
                        procesarCodigo(codigo)
                    }
                )
            } else {
                Text(
                    text = "Permiso de cámara requerido",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(80.dp)
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp)
                    )
            )

            Text(
                text = "CÓDIGO DE BARRAS",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Centra el código de barras dentro del recuadro blanco.",
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = mensaje,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (codigoLeido.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Código leído: $codigoLeido",
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (cedulaBuscada.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cédula buscada: $cedulaBuscada",
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        adultoEncontrado?.let { adulto ->
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = adulto.datosPersonales.nombreCompleto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Documento: ${adulto.datosPersonales.numeroDocumento}",
                        color = Color.DarkGray
                    )

                    Text(
                        text = "Teléfono: ${adulto.datosPersonales.telefono}",
                        color = Color.DarkGray
                    )

                    Text(
                        text = "EPS: ${adulto.datosMedicos.eps}",
                        color = Color.DarkGray
                    )

                    Text(
                        text = "Tipo de sangre: ${adulto.datosMedicos.tipoSangre}",
                        color = Color.DarkGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun BarcodeCameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            clipToOutline = true
        }
    }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { previewUseCase ->
                    previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { imageAnalysis ->
                    imageAnalysis.setAnalyzer(
                        cameraExecutor,
                        BarcodeAnalyzer { codigo ->
                            onBarcodeDetected(codigo)
                        }
                    )
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val mainHandler = Handler(Looper.getMainLooper())

    private var ultimoCodigoDetectado = ""
    private var contadorLecturasIguales = 0
    private var ultimoCodigoEnviado = ""
    private var ultimoEnvioTiempo = 0L

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(
                    BarcodeFormat.CODE_39
                ),
                DecodeHintType.TRY_HARDER to true
            )
        )
    }

    override fun analyze(image: ImageProxy) {
        try {
            val width = image.width
            val height = image.height
            val data = obtenerDatosLuminancia(image)

            val codigo = intentarLeer(data, width, height)
                ?: intentarLeer(rotar90(data, width, height), height, width)
                ?: intentarLeer(rotar270(data, width, height), height, width)

            if (!codigo.isNullOrBlank()) {
                val codigoLimpio = codigo
                    .trim()
                    .replace("*", "")

                val esCedulaValida = codigoLimpio.matches(Regex("\\d{5,12}"))

                if (esCedulaValida) {
                    if (codigoLimpio == ultimoCodigoDetectado) {
                        contadorLecturasIguales++
                    } else {
                        ultimoCodigoDetectado = codigoLimpio
                        contadorLecturasIguales = 1
                    }

                    val ahora = System.currentTimeMillis()
                    val puedeEnviar = contadorLecturasIguales >= 2 &&
                            (codigoLimpio != ultimoCodigoEnviado || ahora - ultimoEnvioTiempo > 2500)

                    if (puedeEnviar) {
                        ultimoCodigoEnviado = codigoLimpio
                        ultimoEnvioTiempo = ahora

                        mainHandler.post {
                            onBarcodeDetected(codigoLimpio)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }

    private fun intentarLeer(data: ByteArray, width: Int, height: Int): String? {
        return try {
            val source = PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )

            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(bitmap)

            reader.reset()

            result.text
        } catch (e: NotFoundException) {
            reader.reset()
            null
        } catch (e: Exception) {
            reader.reset()
            null
        }
    }

    private fun obtenerDatosLuminancia(image: ImageProxy): ByteArray {
        val plane = image.planes[0]
        val buffer = plane.buffer

        val width = image.width
        val height = image.height

        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        val data = ByteArray(width * height)
        val row = ByteArray(rowStride)

        var outputOffset = 0

        for (y in 0 until height) {
            buffer.position(y * rowStride)

            val length = if (buffer.remaining() < rowStride) {
                buffer.remaining()
            } else {
                rowStride
            }

            buffer.get(row, 0, length)

            var inputOffset = 0

            for (x in 0 until width) {
                data[outputOffset++] = row[inputOffset]
                inputOffset += pixelStride
            }
        }

        return data
    }

    private fun rotar90(data: ByteArray, width: Int, height: Int): ByteArray {
        val rotated = ByteArray(data.size)

        var index = 0

        for (x in 0 until width) {
            for (y in height - 1 downTo 0) {
                rotated[index++] = data[y * width + x]
            }
        }

        return rotated
    }

    private fun rotar270(data: ByteArray, width: Int, height: Int): ByteArray {
        val rotated = ByteArray(data.size)

        var index = 0

        for (x in width - 1 downTo 0) {
            for (y in 0 until height) {
                rotated[index++] = data[y * width + x]
            }
        }

        return rotated
    }
}

fun extraerCedulaDesdeCodigo(codigo: String): String {
    val textoLimpio = codigo
        .trim()
        .replace("*", "")

    if (textoLimpio.all { it.isDigit() }) {
        return textoLimpio
    }

    val numerosEncontrados = Regex("\\d+")
        .findAll(textoLimpio)
        .map { it.value }
        .toList()

    return numerosEncontrados.maxByOrNull { it.length } ?: textoLimpio
}
