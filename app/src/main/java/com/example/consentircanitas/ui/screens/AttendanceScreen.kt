package com.example.consentircanitas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.consentircanitas.model.Person
import com.example.consentircanitas.ui.components.PersonItem
import androidx.compose.ui.Alignment

@Composable
fun AttendanceScreen(navController: NavController) {

    val people = listOf(
        Person("María González", 78, true),
        Person("José Luis Ramírez", 82, true),
        Person("Carmen Salazar", 75, false),
        Person("Antonio Vargas", 79, true),
        Person("Luisa Pérez", 81, false),
        Person("Miguel Ángel Torres", 76, true),
    )

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
                Icon(Icons.Default.ArrowBack, contentDescription = "")
            }

            Text("Registro de Asistencia", fontSize = 20.sp)
        }

        Text(
            text = "Jueves, 24 Oct",
            fontSize = 22.sp,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(people) { person ->
                PersonItem(person)
            }
        }
    }
}