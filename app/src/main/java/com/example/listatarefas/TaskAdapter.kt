package com.example.listatarefas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.listatarefas.R
import com.example.listatarefas.data.Task

class TaskAdapter(
    private var tarefas: MutableList<Task>,
    private val listener: TaskListener
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    interface TaskListener {
        fun onEditar(task: Task)
        fun onDeletar(task: Task)
        fun onStatusAlterado(task: Task, concluida: Boolean)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.textTitulo)
        val data: TextView = itemView.findViewById(R.id.textData)
        val hora: TextView = itemView.findViewById(R.id.textHora)
        val checkConcluida: CheckBox = itemView.findViewById(R.id.checkboxConcluida)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditar)
        val btnDeletar: ImageButton = itemView.findViewById(R.id.btnDeletar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tarefas[position]
        holder.titulo.text = task.titulo
        holder.data.text = task.data ?: ""
        holder.hora.text = task.hora ?: ""
        holder.hora.text = task.hora ?: ""
        
        // Remover listener anterior para evitar disparo acidental ao setar isChecked
        holder.checkConcluida.setOnCheckedChangeListener(null)
        holder.checkConcluida.isChecked = task.concluida

        // Alterar status
        holder.checkConcluida.setOnCheckedChangeListener { _, isChecked ->
            listener.onStatusAlterado(task, isChecked)
        }

        // Editar ao clicar no título
        holder.titulo.setOnClickListener {
            listener.onEditar(task)
        }

        // Editar ao clicar no botão
        holder.btnEditar.setOnClickListener {
            listener.onEditar(task)
        }

        // Deletar
        holder.btnDeletar.setOnClickListener {
            listener.onDeletar(task)
        }
    }

    override fun getItemCount(): Int = tarefas.size

    fun atualizarLista(novasTarefas: List<Task>) {
        tarefas.clear()
        tarefas.addAll(novasTarefas)
        notifyDataSetChanged()
    }
}
