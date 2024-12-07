package com.example.trabalhofirebase_vitorhugo.ui.theme.components

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

fun calcularTempoDeServico(dataEntrada: String): Int {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato da data
        val dataInicio = formatter.parse(dataEntrada) ?: return 0
        val dataAtual = Calendar.getInstance()

        Log.i("logTempo2", "${dataInicio}")

        val inicio = Calendar.getInstance().apply { time = dataInicio }
        val anosDeServico = dataAtual.get(Calendar.YEAR) - inicio.get(Calendar.YEAR)



        // Verifica se o ano atual ainda não passou do mês/dia de início
        if (dataAtual.get(Calendar.DAY_OF_YEAR) < inicio.get(Calendar.DAY_OF_YEAR)) {
            anosDeServico - 1
        } else {
            anosDeServico
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0 // Retorna 0 anos em caso de erro
    }
}
