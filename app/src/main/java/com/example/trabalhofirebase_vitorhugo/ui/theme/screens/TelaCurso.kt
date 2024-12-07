package com.example.trabalhofirebase_vitorhugo.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trabalhofirebase_vitorhugo.model.Curso
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.trabalhofirebase_vitorhugo.model.Professor

@Composable
fun TelaCursos(
    cursos: List<Curso>,
    onAddCurso: () -> Unit,
    onEditCurso: (Curso) -> Unit,
    onDeleteCurso: (Curso) -> Unit,
    onBack: () -> Unit // Callback para voltar à tela inicial
) {

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
                text = "Cursos",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onAddCurso() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Adicionar Curso")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cursos) { curso ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Nome: ${curso.nome}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "ID Curso: ${curso.idCurso}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Número de Alunos: ${curso.numeroAlunos}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Docentes: ${curso.docentes.joinToString(", ") { it.nome }}", // Exibe os docentes do curso
                            style = MaterialTheme.typography.bodySmall
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { onEditCurso(curso) }) {
                                Text("Editar")
                            }
                            TextButton(
                                onClick = {
                                    val db = Firebase.firestore

                                    // Para cada professor que leciona o curso
                                    curso.docentes.forEach { professor ->
                                        val professorRef = db.collection("professores").document(professor.matricula)
                                        professorRef.get()
                                            .addOnSuccessListener { documentSnapshot ->
                                                val existingProfessor = documentSnapshot.toObject(Professor::class.java)
                                                if (existingProfessor != null) {
                                                    // Atualiza a lista de cursos removendo o curso
                                                    val updatedCursos = existingProfessor.cursosAtuacao.filterNot { it.idCurso == curso.idCurso }

                                                    // Atualiza o professor no Firestore
                                                    professorRef.set(
                                                        existingProfessor.copy(cursosAtuacao = updatedCursos)
                                                    )
                                                }
                                            }
                                    }

                                    // Após atualizar os professores, remove o curso do Firestore
                                    db.collection("cursos").document(curso.idCurso)
                                        .delete()
                                        .addOnSuccessListener {
                                            onDeleteCurso(curso) // Chama o callback para atualizar a UI
                                        }
                                        .addOnFailureListener { e ->
                                            e.printStackTrace() // a vida cuida do erro
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


