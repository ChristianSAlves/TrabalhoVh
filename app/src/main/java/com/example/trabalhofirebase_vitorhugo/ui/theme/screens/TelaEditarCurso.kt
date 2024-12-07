package com.example.trabalhofirebase_vitorhugo.ui.theme.screens

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
import com.example.trabalhofirebase_vitorhugo.model.Curso
import com.example.trabalhofirebase_vitorhugo.model.Professor
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun TelaEditarCurso(
    curso: Curso,
    docentesDisponiveis: List<Professor>, // Lista de professores
    onSave: (Curso) -> Unit, // Função para salvar as alterações
    onCancel: () -> Unit // Função para cancelar a edição e voltar
) {
    var nome by remember { mutableStateOf(curso.nome) }
    var idCurso by remember { mutableStateOf(curso.idCurso) }
    var numeroAlunos by remember { mutableStateOf(curso.numeroAlunos) }
    val db = Firebase.firestore

    // Inicializa os docentes Selecionados
    var docentesSelecionados by remember {
        mutableStateOf(
            docentesDisponiveis.filter { professor ->
                curso.docentes.contains(professor) // Verifica se o docente completo está na lista de docentes
            }.toSet()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Editar Curso",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo de texto para o nome
        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome Curso") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de texto para o idCurso
        TextField(
            value = idCurso,
            onValueChange = { idCurso = it },
            label = { Text("ID Curso") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Campo de texto para numero de Alunos
        TextField(
            value = numeroAlunos.toString(), // Convert Int to String
            onValueChange = { newText ->
                // Convert the input string to an Int
                val newNumber = newText.toIntOrNull() ?: 0
                numeroAlunos = newNumber
            },
            label = { Text("Número Alunos") },
            modifier = Modifier.fillMaxWidth()
        )

        // Seção para selecionar docentes
        Text(
            text = "Docentes do Curso",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.4f), // Limita a altura da lista
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(docentesDisponiveis) { professor ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            docentesSelecionados = if (docentesSelecionados.contains(professor)) {
                                docentesSelecionados - professor
                            } else {
                                docentesSelecionados + professor
                            }
                        }
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked = docentesSelecionados.contains(professor),
                        onCheckedChange = {
                            docentesSelecionados = if (it) {
                                docentesSelecionados + professor
                            } else {
                                docentesSelecionados - professor
                            }
                        }
                    )
                    Text(
                        text = professor.nome,
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
                    val updatedCurso = curso.copy(
                        nome = nome,
                        numeroAlunos = numeroAlunos,
                        docentes = docentesSelecionados.toList()
                    )

                    // Atualiza o curso no Firestore
                    db.collection("cursos").document(updatedCurso.idCurso).set(updatedCurso)
                        .addOnSuccessListener {
                            // Atualiza os professores antigos para remover o curso de seus cursosAtuacao
                            curso.docentes.forEach { professor ->
                                val professorRef = db.collection("professores").document(professor.matricula)
                                professorRef.get().addOnSuccessListener { documentSnapshot ->
                                    val existingProfessor = documentSnapshot.toObject(Professor::class.java)
                                    if (existingProfessor != null) {
                                        val updatedCursos = existingProfessor.cursosAtuacao.filterNot { it.idCurso == updatedCurso.idCurso }
                                        professorRef.set(existingProfessor.copy(cursosAtuacao = updatedCursos))
                                    }
                                }
                            }

                            // Atualiza os professores novos para adicionar o curso aos seus cursosAtuacao
                            docentesSelecionados.forEach { professor ->
                                val professorRef = db.collection("professores").document(professor.matricula)
                                professorRef.get().addOnSuccessListener { documentSnapshot ->
                                    val existingProfessor = documentSnapshot.toObject(Professor::class.java)
                                    if (existingProfessor != null) {
                                        val updatedCursos = existingProfessor.cursosAtuacao.toMutableList()
                                        if (!updatedCursos.any { it.idCurso == updatedCurso.idCurso }) {
                                            updatedCursos.add(updatedCurso)
                                        }
                                        professorRef.set(existingProfessor.copy(cursosAtuacao = updatedCursos))
                                    }
                                }
                            }

                            // Chama o onSave após a atualização
                            onSave(updatedCurso)
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace() // Trate o erro
                        }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Salvar")
            }


        }
    }
}