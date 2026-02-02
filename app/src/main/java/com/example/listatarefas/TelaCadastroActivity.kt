package com.example.listatarefas

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.listatarefas.data.TaskDatabase
import com.example.listatarefas.data.User
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaCadastroActivity : AppCompatActivity() {

    private lateinit var db: TaskDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        db = TaskDatabase.getDatabase(this)

        val editNome = findViewById<TextInputEditText>(R.id.editNomeCadastro)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmailCadastro)
        val editSenha = findViewById<TextInputEditText>(R.id.editSenhaCadastro)
        val editConfSenha = findViewById<TextInputEditText>(R.id.editConfirmarSenha)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)
        val txtLogin = findViewById<TextView>(R.id.txtLoginLink)

        btnCadastrar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()
            val confSenha = editConfSenha.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha != confSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val userExiste = db.userDao().verificarEmailExiste(email)
                if (userExiste != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TelaCadastroActivity, "Email já cadastrado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val novoUsuario = User(nome = nome, email = email, senha = senha)
                    db.userDao().inserir(novoUsuario)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TelaCadastroActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish() // Retorna para a tela de login
                    }
                }
            }
        }

        txtLogin.setOnClickListener {
            finish()
        }
    }
}
