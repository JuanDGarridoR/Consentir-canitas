package com.example.consentircanitas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.consentircanitas.ui.components.CardOption
import com.example.consentircanitas.ui.theme.*

@Composable
fun HomeScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Grupo Consentir Canitas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Sistema de control y asistencia para nuestros adultos mayores.",
            color = TextSecondary,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¿Qué deseas hacer hoy?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        CardOption(
            title = "Tomar Asistencia",
            description = "Escanea el QR del carnet para registrar su llegada",
            color = PrimaryGreen
        ) {
            navController.navigate("scan")
        }

        Spacer(modifier = Modifier.height(20.dp))

        CardOption(
            title = "Registro por Días",
            description = "Revisa quiénes asistieron hoy o en reuniones pasadas",
            color = PrimaryBlue
        ) {
            navController.navigate("attendance")
        }
    }
}