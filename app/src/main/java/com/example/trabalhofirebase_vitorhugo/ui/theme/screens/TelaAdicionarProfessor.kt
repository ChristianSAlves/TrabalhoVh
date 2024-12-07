package com.example.trabalhofirebase_vitorhugo.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.trabalhofirebase_vitorhugo.model.Professor
import com.example.trabalhofirebase_vitorhugo.model.Curso
import com.example.trabalhofirebase_vitorhugo.ui.theme.components.DatePickerDocked
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


@Composable
fun TelaAdicionarProfessor(
    cursosDisponiveis: List<Curso>, // Lista de cursos disponíveis para seleção
    onSave: (Professor) -> Unit, // Função para salvar o novo professor
    onCancel: () -> Unit // Função para cancelar a adição e voltar
) {
    // Variáveis de estado para os dados do novo professor
    var nome by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var salario by remember { mutableStateOf("") }
    var areaAtuacao by remember { mutableStateOf("") }
    var dataEntrada by remember { mutableStateOf("") } // Armazena a data formatada

    // Cursos selecionados
    var cursosSelecionados by remember { mutableStateOf(setOf<Curso>()) }


    // Referência ao Firestore
    val db = Firebase.firestore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Adicionar Professor",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo de texto para o nome
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de texto para a matrícula
        OutlinedTextField(
            value = matricula,
            onValueChange = { matricula = it },
            label = { Text("Matrícula") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de texto para o salário
        OutlinedTextField(
            value = salario,
            onValueChange = { salario = it },
            label = { Text("Salário") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Campo de texto para a área de atuação
        OutlinedTextField(
            value = areaAtuacao,
            onValueChange = { areaAtuacao = it },
            label = { Text("Área de Atuação") },
            modifier = Modifier.fillMaxWidth()
        )

        DatePickerDocked(
            onDateSelected = { selectedDate ->
                dataEntrada = selectedDate
                println("Data selecionada: $dataEntrada") // Debug
            },
            label = "Data de Entrada"
        )


        // Seção para selecionar cursos de atuação
        Text(
            text = "Cursos de Atuação",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.4f), // Limita a altura da lista
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cursosDisponiveis) { curso ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            cursosSelecionados = if (cursosSelecionados.contains(curso)) {
                                cursosSelecionados - curso
                            } else {
                                cursosSelecionados + curso
                            }
                        }
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked = cursosSelecionados.contains(curso),
                        onCheckedChange = {
                            cursosSelecionados = if (it) {
                                cursosSelecionados + curso
                            } else {
                                cursosSelecionados - curso
                            }
                        }
                    )
                    Text(
                        text = curso.nome,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Botões Cancelar e Salvar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    val novoProfessor = Professor(
                        nome = nome,
                        matricula = matricula,
                        salario = salario.toDoubleOrNull() ?: 0.0,
                        areaAtuacao = areaAtuacao,
                        dataEntrada = dataEntrada, // Usa a data selecionada
                        cursosAtuacao = cursosSelecionados.toList()
                    )


                    // Adiciona o professor ao Firestore
                    db.collection("professores")
                        .document(matricula) // Usa a matrícula como ID
                        .set(novoProfessor)
                        .addOnSuccessListener {
                            Log.i("LogTeste", "${novoProfessor}")
                            // Atualiza cada curso selecionado para incluir o professor na lista de docentes
                            cursosSelecionados.forEach { curso ->
                                val cursoRef = db.collection("cursos").document(curso.idCurso)
                                cursoRef.get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val existingCurso = documentSnapshot.toObject(Curso::class.java)
                                        if (existingCurso != null) {
                                            // Atualiza a lista de docentes do curso
                                            val updatedDocentes = existingCurso.docentes.toMutableList()
                                            if (!updatedDocentes.any { it.matricula == matricula }) {
                                                updatedDocentes.add(novoProfessor) // Adiciona o novo professor
                                            }

                                            // Atualiza o curso no Firestore
                                            cursoRef.set(
                                                existingCurso.copy(docentes = updatedDocentes)
                                            )
                                        }
                                    }
                            }

                            // Chama o onSave para finalizar a adição
                            onSave(novoProfessor)
                        }
                        .addOnFailureListener { e ->
                            // Trate o erro, se necessário
                            e.printStackTrace()
                        }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Salvar")
            }

        }
    }
}

