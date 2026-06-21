package com.example.paytag

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.paytag.data.GroupRepository
import com.example.paytag.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val repository = GroupRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository.init(this)

        if (repository.isLoggedIn() && repository.isSetupComplete()) {
            navigateToMain()
            return
        }

        if (repository.isLoggedIn() && !repository.isSetupComplete()) {
            navigateToGroupSetup()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val name = binding.nameEdit.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            repository.login(name)
            navigateToGroupSetup()
        }
    }

    private fun navigateToGroupSetup() {
        startActivity(Intent(this, GroupActivity::class.java))
        finish()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
