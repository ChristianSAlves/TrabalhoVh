package com.example.trabalhofirebase_vitorhugo.model

data class Curso(
    val idCurso: String = "",
    val nome: String = "",
    val numeroAlunos: Int = 0,
    val docentes: List<Professor> = emptyList()
)
