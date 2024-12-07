package com.example.trabalhofirebase_vitorhugo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trabalhofirebase_vitorhugo.model.Curso
import com.example.trabalhofirebase_vitorhugo.model.Professor
import com.example.trabalhofirebase_vitorhugo.ui.theme.TrabalhoFirebaseVitorHugoTheme
import com.example.trabalhofirebase_vitorhugo.ui.theme.screens.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrabalhoFirebaseVitorHugoTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    // Firebase Firestore Instance
    val db = Firebase.firestore

    // Cursos e professores como estado reativo
    var cursos by remember { mutableStateOf<List<Curso>>(emptyList()) }
    var professores by remember { mutableStateOf<List<Professor>>(emptyList()) }

    // Carregar dados iniciais de Firestore
    LaunchedEffect(Unit) {
        db.collection("cursos").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                cursos = snapshot.documents.mapNotNull { document ->
                    document.toObject(Curso::class.java)?.copy(idCurso = document.id)
                }
            }
        }

        db.collection("professores").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                professores = snapshot.documents.mapNotNull { document ->
                    document.toObject(Professor::class.java)?.copy(matricula = document.id)
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "tela_inicial") {
        composable("tela_inicial") {
            TelaInicial(
                navigateToProfessores = { navController.navigate("tela_professores") },
                navigateToCursos = { navController.navigate("tela_cursos") }
            )
        }

        composable("tela_professores") {
            TelaProfessores(
                professores = professores,
                onAddProfessor = { navController.navigate("tela_adicionar_professor") },
                onDeleteProfessor = { professor ->
                    db.collection("professores").document(professor.matricula).delete()
                },
                onEditProfessor = { professor ->
                    navController.navigate("tela_editar_professor/${professor.matricula}")
                },
                onBack = { navController.popBackStack() } // Navega de volta para a tela inicial
            )
        }

        composable("tela_cursos") {
            TelaCursos(
                cursos = cursos,
                onAddCurso = { navController.navigate("tela_adicionar_curso") },
                onEditCurso = { curso ->
                    navController.navigate("tela_editar_curso/${curso.idCurso}")
                },
                onDeleteCurso = { curso ->
                    db.collection("cursos").document(curso.idCurso).delete()
                },
                onBack = { navController.popBackStack() } // Navega de volta para a tela inicial
            )
        }

        composable("tela_editar_professor/{matricula}") { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula")
            val professor = professores.firstOrNull { it.matricula == matricula }

            if (professor != null) {
                TelaEditarProfessor(
                    professor = professor,
                    cursosDisponiveis = cursos,
                    onSave = { updatedProfessor ->
                        db.collection("professores").document(updatedProfessor.matricula).set(updatedProfessor)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }

        composable("tela_adicionar_professor") {
            TelaAdicionarProfessor(
                cursosDisponiveis = cursos,
                onSave = { novoProfessor ->
                    db.collection("professores").document(novoProfessor.matricula).set(novoProfessor).addOnSuccessListener {
                        navController.popBackStack()
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("tela_editar_curso/{idCurso}") { backStackEntry ->
            val idCurso = backStackEntry.arguments?.getString("idCurso")
            val curso = cursos.firstOrNull { it.idCurso == idCurso }

            if (curso != null) {
                TelaEditarCurso(
                    curso = curso,
                    docentesDisponiveis = professores,
                    onSave = { updatedCurso ->
                        db.collection("cursos").document(updatedCurso.idCurso).set(updatedCurso)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }

        composable("tela_adicionar_curso") {
            TelaAdicionarCurso(
                docentesDisponiveis = professores,
                onSave = {
                    navController.popBackStack() // Apenas volta para a tela anterior
                },
                onCancel = { navController.popBackStack() }
            )
        }

    }
}



