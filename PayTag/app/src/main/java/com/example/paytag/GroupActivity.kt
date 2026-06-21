package com.example.paytag

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.paytag.data.GroupRepository
import com.example.paytag.databinding.ActivityGroupBinding

class GroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupBinding
    private val repository = GroupRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository.init(this)
        binding = ActivityGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateGroup.setOnClickListener { createGroup() }
        binding.btnJoinGroup.setOnClickListener { showJoinDialog() }
        binding.btnSkip.setOnClickListener {
            repository.completeSetup()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun createGroup() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreateGroup.isEnabled = false

        try {
            val group = repository.createGroup()
            binding.progressBar.visibility = View.GONE
            showGroupCode(group.code)
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            binding.btnCreateGroup.isEnabled = true
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGroupCode(code: String) {
        AlertDialog.Builder(this)
            .setTitle("Group Created!")
            .setMessage(
                "Your group code is:\n\n" +
                "$code\n\n" +
                "Share this code with your partner.\n" +
                "They can join by tapping 'Join Group'.\n\n" +
                "You can also skip this and use PayTag solo."
            )
            .setPositiveButton("Done") { _, _ ->
                repository.completeSetup()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showJoinDialog() {
        val input = EditText(this)
        input.hint = "Enter 6-digit code"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(this)
            .setTitle("Join Group")
            .setMessage("Enter the code shared by your partner")
            .setView(input)
            .setPositiveButton("Join") { _, _ ->
                val code = input.text.toString().trim()
                if (code.length != 6) {
                    Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                joinGroup(code)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun joinGroup(code: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnJoinGroup.isEnabled = false

        try {
            val group = repository.joinGroup(code)
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Joined group!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            binding.btnJoinGroup.isEnabled = true
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }
}
