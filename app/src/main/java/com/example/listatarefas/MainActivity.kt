package com.example.listatarefas

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listatarefas.data.Task
import com.example.listatarefas.data.TaskDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : AppCompatActivity(), TaskAdapter.TaskListener {

    private lateinit var adapter: TaskAdapter
    private lateinit var db: TaskDatabase
    private var filtroAtual = "TODAS" // TODAS, PENDENTES, CONCLUIDAS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = TaskDatabase.getDatabase(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerTarefas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddTask).setOnClickListener {
            mostrarDialogoTarefa(null)
        }

        configurarFiltros()
        listarTarefas()
    }

    private fun configurarFiltros() {
        val btnTodas = findViewById<Button>(R.id.btnFiltroTodas)
        val btnPendentes = findViewById<Button>(R.id.btnFiltroPendentes)
        val btnConcluidas = findViewById<Button>(R.id.btnFiltroConcluidas)

        fun atualizarBotoes(selecionado: String) {
            filtroAtual = selecionado
            listarTarefas()

            val corAtiva = getColor(R.color.purple_500) // Ou cor primária do tema
            val corInativa = android.graphics.Color.GRAY
            val corTextoAtivo = android.graphics.Color.WHITE
            val corTextoInativo = android.graphics.Color.BLACK

            btnTodas.setBackgroundColor(if (selecionado == "TODAS") corAtiva else android.graphics.Color.TRANSPARENT)
            btnTodas.setTextColor(if (selecionado == "TODAS") corTextoAtivo else corTextoInativo)

            btnPendentes.setBackgroundColor(if (selecionado == "PENDENTES") corAtiva else android.graphics.Color.TRANSPARENT)
            btnPendentes.setTextColor(if (selecionado == "PENDENTES") corTextoAtivo else corTextoInativo)

            btnConcluidas.setBackgroundColor(if (selecionado == "CONCLUIDAS") corAtiva else android.graphics.Color.TRANSPARENT)
            btnConcluidas.setTextColor(if (selecionado == "CONCLUIDAS") corTextoAtivo else corTextoInativo)
        }

        btnTodas.setOnClickListener { atualizarBotoes("TODAS") }
        btnPendentes.setOnClickListener { atualizarBotoes("PENDENTES") }
        btnConcluidas.setOnClickListener { atualizarBotoes("CONCLUIDAS") }
        
        // Estado inicial
        atualizarBotoes("TODAS")
    }

    private fun listarTarefas() {
        CoroutineScope(Dispatchers.IO).launch {
            val tarefas = when (filtroAtual) {
                "PENDENTES" -> db.taskDao().listarPendentes()
                "CONCLUIDAS" -> db.taskDao().listarConcluidas()
                else -> db.taskDao().listarTarefas()
            }
            withContext(Dispatchers.Main) {
                adapter.atualizarLista(tarefas)
            }
        }
    }

    private fun mostrarDialogoTarefa(task: Task?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_task, null)
        val editTitulo = dialogView.findViewById<TextInputEditText>(R.id.editDialogTitulo)
        val btnData = dialogView.findViewById<Button>(R.id.btnDialogData)
        val btnHora = dialogView.findViewById<Button>(R.id.btnDialogHora)
        val btnSalvar = dialogView.findViewById<Button>(R.id.btnDialogSalvar)
        val txtTitle = dialogView.findViewById<TextView>(R.id.txtDialogTitle)

        var dataSelecionada: String? = task?.data
        var horaSelecionada: String? = task?.hora

        if (task != null) {
            txtTitle.text = "Editar Tarefa"
            editTitulo.setText(task.titulo)
            btnData.text = task.data ?: "Data"
            btnHora.text = task.hora ?: "Hora"
        } else {
            txtTitle.text = "Nova Tarefa"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnData.setOnClickListener {
            val calendario = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val diaFormatado = String.format("%02d/%02d/%d", day, month + 1, year)
                dataSelecionada = diaFormatado
                btnData.text = diaFormatado
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnHora.setOnClickListener {
            val calendario = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                val horaFormatada = String.format("%02d:%02d", hour, minute)
                horaSelecionada = horaFormatada
                btnHora.text = horaFormatada
            }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true).show()
        }

        btnSalvar.setOnClickListener {
            val titulo = editTitulo.text.toString().trim()
            if (titulo.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (task == null) {
                        val novaTask = Task(titulo = titulo, data = dataSelecionada, hora = horaSelecionada)
                        db.taskDao().inserir(novaTask)
                        scheduleAlarm(novaTask)
                    } else {
                        task.titulo = titulo
                        task.data = dataSelecionada
                        task.hora = horaSelecionada
                        db.taskDao().atualizar(task)
                        scheduleAlarm(task)
                    }
                    withContext(Dispatchers.Main) {
                        listarTarefas()
                        dialog.dismiss()
                    }
                }
            } else {
                Toast.makeText(this, "Título é obrigatório", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun scheduleAlarm(task: Task) {
        if (task.data == null || task.hora == null) return

        try {
            val partsData = task.data!!.split("/")
            val partsHora = task.hora!!.split(":")
            
            if (partsData.size == 3 && partsHora.size == 2) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, partsData[0].toInt())
                    set(Calendar.MONTH, partsData[1].toInt() - 1)
                    set(Calendar.YEAR, partsData[2].toInt())
                    set(Calendar.HOUR_OF_DAY, partsHora[0].toInt())
                    set(Calendar.MINUTE, partsHora[1].toInt())
                    set(Calendar.SECOND, 0)
                }

                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    val intent = Intent(this, AlarmReceiver::class.java).apply {
                        putExtra("titulo", task.titulo)
                        putExtra("id", task.id)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        task.id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    // Usar setExactAndAllowWhileIdle para garantir precisão
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onEditar(task: Task) {
        mostrarDialogoTarefa(task)
    }

    override fun onDeletar(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().deletar(task)
            withContext(Dispatchers.Main) {
                listarTarefas()
            }
        }
    }

    override fun onStatusAlterado(task: Task, concluida: Boolean) {
        task.concluida = concluida
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().atualizar(task)
            // Atualizar lista apenas se estiver filtrando, para remover visualmente se necessário
            if (filtroAtual != "TODAS") {
                withContext(Dispatchers.Main) {
                    listarTarefas()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_stats -> {
                startActivity(Intent(this, StatisticsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                val sharedPreferences = getSharedPreferences("app_session", MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                startActivity(Intent(this, TelaLoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
