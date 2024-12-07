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
fun TelaAdicionarCurso(
    docentesDisponiveis: List<Professor>, // Lista de professores disponíveis
    onSave: (Curso) -> Unit, // Função para voltar após salvar
    onCancel: () -> Unit // Função para cancelar a adição e voltar
) {
    // Variáveis de estado para os dados do novo curso
    var nome by remember { mutableStateOf("") }
    var idCurso by remember { mutableStateOf("") }
    var numeroAlunos by remember { mutableStateOf("") }

    // Docentes Selecionados
    var docentesSelecionados by remember { mutableStateOf(setOf<Professor>()) }

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
            text = "Adicionar Curso",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo de texto para o nome
        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de texto para idCurso
        TextField(
            value = idCurso,
            onValueChange = { idCurso = it },
            label = { Text("ID Curso") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de número de Alunos
        TextField(
            value = numeroAlunos,
            onValueChange = { numeroAlunos = it },
            label = { Text("Número de Alunos") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Seção para selecionar Docentes
        Text(
            text = "Docentes",
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
                    // Verifica se `idCurso` foi preenchido
                    if (idCurso.isEmpty()) {
                        println("O ID do curso não pode estar vazio!")
                        return@Button
                    }

                    val novoCurso = Curso(
                        idCurso = idCurso, // ID definido pelo usuário
                        nome = nome,
                        numeroAlunos = numeroAlunos.toIntOrNull() ?: 0,
                        docentes = docentesSelecionados.toList() // Salvando os objetos Professor diretamente
                    )

                    // Adicionar o curso ao Firestore com `idCurso` como chave primária
                    db.collection("cursos")
                        .document(idCurso) // Define manualmente o ID do documento
                        .set(novoCurso) // Salva o curso no Firestore
                        .addOnSuccessListener {
                            // Atualiza os professores no Firestore para incluir o curso em sua lista de atuação
                            docentesSelecionados.forEach { professor ->
                                val professorRef = db.collection("professores").document(professor.matricula)
                                professorRef.get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val existingProfessor = documentSnapshot.toObject(Professor::class.java)
                                        if (existingProfessor != null) {
                                            // Atualiza a lista de cursos do professor
                                            val updatedCursos = existingProfessor.cursosAtuacao.toMutableList()
                                            if (!updatedCursos.any { it.idCurso == novoCurso.idCurso }) {
                                                updatedCursos.add(novoCurso)
                                            }

                                            // Atualiza o professor no Firestore
                                            professorRef.set(
                                                existingProfessor.copy(cursosAtuacao = updatedCursos)
                                            )
                                        }
                                    }
                            }

                            // Após todas as atualizações, chama o onSave para finalizar
                            onSave(novoCurso)
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

