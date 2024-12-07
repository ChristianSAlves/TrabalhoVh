package com.example.trabalhofirebase_vitorhugo.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trabalhofirebase_vitorhugo.model.Professor
import com.example.trabalhofirebase_vitorhugo.ui.theme.components.calcularTempoDeServico
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.trabalhofirebase_vitorhugo.model.Curso

@Composable
fun TelaProfessores(
    professores: List<Professor>,
    onAddProfessor: () -> Unit,
    onDeleteProfessor: (Professor) -> Unit,
    onEditProfessor: (Professor) -> Unit,
    onBack: () -> Unit // Callback para voltar à tela inicial
) {
    var tempoServicoTexto by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { onBack() }) { // Botão para voltar
                Text("Voltar")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Professores",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = { onAddProfessor() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adicionar Professor")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(professores) { professor ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Nome: ${professor.nome}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Matrícula: ${professor.matricula}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Área de Atuação: ${professor.areaAtuacao}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Salário: R$${professor.salario}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Data Entrada: ${professor.dataEntrada}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Cursos: ${professor.cursosAtuacao.joinToString(", ") { it.nome }}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botão para exibir o tempo de serviço
                        Button(
                            onClick = {
                                val tempoServico = calcularTempoDeServico(professor.dataEntrada)
                                tempoServicoTexto = "Tempo de Serviço: $tempoServico anos"
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Mostrar Tempo de Serviço")
                        }

                       // Exibe o tempo de serviço na tela
                        if (tempoServicoTexto.isNotEmpty()) {
                            Text(
                                text = tempoServicoTexto,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { onEditProfessor(professor) }) {
                                Text("Editar")
                            }
                            TextButton(
                                onClick = {
                                    val db = Firebase.firestore

                                    // Para cada curso onde o professor atua
                                    professor.cursosAtuacao.forEach { curso ->
                                        val cursoRef = db.collection("cursos").document(curso.idCurso)
                                        cursoRef.get()
                                            .addOnSuccessListener { documentSnapshot ->
                                                val existingCurso = documentSnapshot.toObject(Curso::class.java)
                                                if (existingCurso != null) {
                                                    // Atualiza a lista de docentes removendo o professor
                                                    val updatedDocentes = existingCurso.docentes.filterNot { it.matricula == professor.matricula }

                                                    // Atualiza o curso no Firestore
                                                    cursoRef.set(
                                                        existingCurso.copy(docentes = updatedDocentes)
                                                    )
                                                }
                                            }
                                    }

                                    // Após atualizar os cursos, remove o professor do Firestore
                                    db.collection("professores").document(professor.matricula)
                                        .delete()
                                        .addOnSuccessListener {
                                            onDeleteProfessor(professor) // Chama o callback para atualizar a UI
                                        }
                                        .addOnFailureListener { e ->
                                            e.printStackTrace()
                                        }
                                }
                            ) {
                                Text("Excluir")
                            }

                        }
                    }
                }
            }
        }

    }
}

