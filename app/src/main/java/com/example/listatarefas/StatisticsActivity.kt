package com.example.listatarefas

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.listatarefas.data.TaskDatabase
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticsActivity : AppCompatActivity() {

    private lateinit var db: TaskDatabase
    private lateinit var pieChart: PieChart
    private lateinit var txtTotal: TextView
    private lateinit var txtConcluidas: TextView
    private lateinit var txtPendentes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        db = TaskDatabase.getDatabase(this)
        pieChart = findViewById(R.id.pieChart)
        txtTotal = findViewById(R.id.txtTotal)
        txtConcluidas = findViewById(R.id.txtConcluidas)
        txtPendentes = findViewById(R.id.txtPendentes)

        carregarEstatisticas()
    }

    private fun carregarEstatisticas() {
        CoroutineScope(Dispatchers.IO).launch {
            val todas = db.taskDao().listarTarefas()
            val concluidas = todas.count { it.concluida }
            val pendentes = todas.count { !it.concluida }
            val total = todas.size

            withContext(Dispatchers.Main) {
                txtTotal.text = "Total de Tarefas: $total"
                txtConcluidas.text = "Concluídas: $concluidas"
                txtPendentes.text = "Pendentes: $pendentes"

                configurarGrafico(concluidas, pendentes)
            }
        }
    }

    private fun configurarGrafico(concluidas: Int, pendentes: Int) {
        val entries = ArrayList<PieEntry>()
        if (concluidas > 0) entries.add(PieEntry(concluidas.toFloat(), "Concluídas"))
        if (pendentes > 0) entries.add(PieEntry(pendentes.toFloat(), "Pendentes"))

        val dataSet = PieDataSet(entries, "Tarefas")
        dataSet.colors = listOf(Color.GREEN, Color.RED)
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.BLACK

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Produtividade"
        pieChart.setCenterTextSize(18f)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
