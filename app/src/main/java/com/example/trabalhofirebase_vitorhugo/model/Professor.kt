package com.example.trabalhofirebase_vitorhugo.model

data class Professor(
    val matricula: String = "",
    val nome: String = "",
    val salario: Double = 0.0,
    val areaAtuacao: String = "",
    val dataEntrada: String = "",
    val cursosAtuacao: List<Curso> = emptyList()
)

