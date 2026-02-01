package com.example.listatarefas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listatarefas.R
import com.example.listatarefas.data.Task
import com.example.listatarefas.data.TaskDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), TaskAdapter.TaskListener {

    private lateinit var adapter: TaskAdapter
    private lateinit var db: TaskDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = TaskDatabase.getDatabase(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerTarefas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TaskAdapter(mutableListOf(), this)
        recyclerView.adapter = adapter

        val editNovaTarefa = findViewById<EditText>(R.id.editNovaTarefa)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)

        btnAdicionar.setOnClickListener {
            val titulo = editNovaTarefa.text.toString().trim()
            if (titulo.isNotEmpty()) {
                val novaTask = Task(titulo = titulo)
                CoroutineScope(Dispatchers.IO).launch {
                    db.taskDao().inserir(novaTask)
                    listarTarefas()
                }
                editNovaTarefa.text.clear()
            }
        }

        listarTarefas()
    }

    private fun listarTarefas() {
        CoroutineScope(Dispatchers.IO).launch {
            val tarefas = db.taskDao().listarTarefas()
            runOnUiThread {
                adapter.atualizarLista(tarefas)
            }
        }
    }

    override fun onEditar(task: Task) {
        val editText = EditText(this)
        editText.setText(task.titulo)
        AlertDialog.Builder(this)
            .setTitle("Editar Tarefa")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val novoTitulo = editText.text.toString()
                if (novoTitulo.isNotEmpty()) {
                    task.titulo = novoTitulo
                    CoroutineScope(Dispatchers.IO).launch {
                        db.taskDao().atualizar(task)
                        listarTarefas()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDeletar(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().deletar(task)
            listarTarefas()
        }
    }

    override fun onStatusAlterado(task: Task, concluida: Boolean) {
        task.concluida = concluida
        CoroutineScope(Dispatchers.IO).launch {
            db.taskDao().atualizar(task)
            listarTarefas()
        }
    }
}
