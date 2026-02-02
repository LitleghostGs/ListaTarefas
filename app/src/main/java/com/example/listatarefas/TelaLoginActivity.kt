package com.example.listatarefas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.listatarefas.data.TaskDatabase
import kotlinx.coroutines.launch

class TelaLoginActivity : AppCompatActivity() {

    private lateinit var db: TaskDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_login)

        // Verificar sessão
        val sharedPreferences = getSharedPreferences("app_session", MODE_PRIVATE)
        if (sharedPreferences.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        db = TaskDatabase.getDatabase(this)

        val campoEmail = findViewById<EditText>(R.id.editEmail)
        val campoSenha = findViewById<EditText>(R.id.editSenha)
        val botaoEntrar = findViewById<Button>(R.id.btnEntrar)
        val txtCadastrar = findViewById<TextView>(R.id.txtCadastrar)

        botaoEntrar.setOnClickListener {
            val email = campoEmail.text.toString().trim()
            val senha = campoSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val user = db.userDao().autenticar(email, senha)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (user != null) {
                        // Salvar sessão
                        sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
                        
                        val intent = Intent(this@TelaLoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@TelaLoginActivity, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        txtCadastrar.setOnClickListener {
            startActivity(Intent(this, TelaCadastroActivity::class.java))
        }
    }
}
