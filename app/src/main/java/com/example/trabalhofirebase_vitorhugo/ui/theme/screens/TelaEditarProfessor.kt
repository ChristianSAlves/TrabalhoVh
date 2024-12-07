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
import com.example.trabalhofirebase_vitorhugo.model.Professor
import com.example.trabalhofirebase_vitorhugo.model.Curso
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun TelaEditarProfessor(
    professor: Professor,
    cursosDisponiveis: List<Curso>, // Lista de cursos disponíveis para seleção
    onSave: (Professor) -> Unit, // Função para salvar as alterações
    onCancel: () -> Unit // Função para cancelar a edição e voltar
) {
    // Variáveis de estado para edição
    var nome by remember { mutableStateOf(professor.nome) }
    var salario by remember { mutableStateOf(professor.salario.toString()) }
    var areaAtuacao by remember { mutableStateOf(professor.areaAtuacao) }
    val db = Firebase.firestore

    // Inicializa cursos selecionados com base nos cursos do professor
    var cursosSelecionados by remember {
        mutableStateOf(
            cursosDisponiveis.filter { curso ->
                professor.cursosAtuacao.contains(curso) // Verifica se o curso completo está em cursosAtuacao
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
            text = "Editar Professor",
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

        // Campo de texto para o salário
        TextField(
            value = salario,
            onValueChange = { salario = it },
            label = { Text("Salário") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Campo de texto para a área de atuação
        TextField(
            value = areaAtuacao,
            onValueChange = { areaAtuacao = it },
            label = { Text("Área de Atuação") },
            modifier = Modifier.fillMaxWidth()
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
                    val updatedProfessor = professor.copy(
                        nome = nome,
                        salario = salario.toDoubleOrNull() ?: professor.salario,
                        areaAtuacao = areaAtuacao,
                        cursosAtuacao = cursosSelecionados.toList()
                    )


                    // Atualiza o professor no Firestore
                    db.collection("professores").document(updatedProfessor.matricula).set(updatedProfessor)
                        .addOnSuccessListener {
                            // Atualiza os cursos antigos para remover o professor de suas listas de docentes
                            professor.cursosAtuacao.forEach { cursoAntigo ->
                                val cursoRef = db.collection("cursos").document(cursoAntigo.idCurso)
                                cursoRef.get().addOnSuccessListener { documentSnapshot ->
                                    val existingCurso = documentSnapshot.toObject(Curso::class.java)
                                    if (existingCurso != null) {
                                        val updatedDocentes = existingCurso.docentes.filterNot { it.matricula == professor.matricula }
                                        cursoRef.set(existingCurso.copy(docentes = updatedDocentes))
                                    }
                                }
                            }

                            // Atualiza os cursos novos para incluir o professor em suas listas de docentes
                            cursosSelecionados.forEach { cursoNovo ->
                                val cursoRef = db.collection("cursos").document(cursoNovo.idCurso)
                                cursoRef.get().addOnSuccessListener { documentSnapshot ->
                                    val existingCurso = documentSnapshot.toObject(Curso::class.java)
                                    if (existingCurso != null) {
                                        val updatedDocentes = existingCurso.docentes.toMutableList()
                                        if (!updatedDocentes.any { it.matricula == updatedProfessor.matricula }) {
                                            updatedDocentes.add(updatedProfessor)
                                        }
                                        cursoRef.set(existingCurso.copy(docentes = updatedDocentes))
                                    }
                                }
                            }

                            // Chama o onSave após a atualização
                            onSave(updatedProfessor)
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





