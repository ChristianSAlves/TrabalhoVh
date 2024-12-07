package com.example.trabalhofirebase_vitorhugo.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TelaInicial(navigateToProfessores: () -> Unit, navigateToCursos: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = navigateToProfessores,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Gerenciar Professores")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = navigateToCursos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Gerenciar Cursos")
        }
    }
}

