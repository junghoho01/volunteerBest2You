package com.example.best2help

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.best2help.databinding.ActivityForgotPasswordBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Random

class ForgotPassword : AppCompatActivity() {

    private lateinit var binding : ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnForgetPassword.setOnClickListener { sendPassword() }
    }

    private fun sendPassword() {

        if (emailNotNull()) {

            emailExists { emailExists ->
                if (emailExists) {
                    // Email exists, handle accordingly
                    var code = runRandomPasswordGenerator()

                    // Send Email
                    runEmailCode(code, binding.etEmail.text.toString())

                    // Update new password
                    updatePasswordToEmail(code, binding.etEmail.text.toString())

                } else {
                    // Email does not exist, handle accordingly
                    DialogUtils.errorDialog(this, "Oops, email does not exist!")
                }
            }

        } else {
            DialogUtils.errorDialog(this, "Oops, email must not be null!")
        }

    }

    private fun updatePasswordToEmail(pass: String, email: String) {

        DialogUtils.getDetails(email) { volunteer ->
            if (volunteer != null) {

                val volunteerPass = mapOf(
                    "password" to pass
                )

                val dbrefUser = FirebaseDatabase.getInstance().getReference("Volunteer").child(volunteer.uid.toString())

                dbrefUser.updateChildren(volunteerPass)
                    .addOnSuccessListener {
                        var intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("RESETPASS_KEY", "1") // Set one as first time
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        DialogUtils.errorDialog(this, "Oops, Fail to reset...")
                    }

            } else {
                // Volunteer not found
                DialogUtils.errorDialog(this, "Databaser Error...")
            }
        }
    }

    private fun emailExists(callback: (Boolean) -> Unit) {
        // Try to find email
        val dbref = FirebaseDatabase.getInstance().getReference("Volunteer")
        val query = dbref.orderByChild("email").equalTo(binding.etEmail.text.toString())

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }

    private fun emailNotNull(): Boolean {
        return binding.etEmail.text.isNotEmpty()
    }

    private fun runEmailCode(sendCode: String, receiverEmail: String?) {
        // Run the email code here
        val senderEmail = "appvolunteer4@gmail.com"
        val password = "piexyflafbtwkawh"
        Toast.makeText(this, receiverEmail, Toast.LENGTH_SHORT).show()
        EmailForgotPassword.sendEmail(senderEmail, receiverEmail, password, sendCode)
    }

    private fun runRandomPasswordGenerator(): String {
        val random = Random()
        val codeLength = 8
        val symbols = "!@#$%^&*()-_=+[{]};:'\",<.>/?"
        val allCharacters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$symbols"

        val generatedCode = StringBuilder()

        repeat(codeLength) {
            val randomIndex = random.nextInt(allCharacters.length)
            val randomChar = allCharacters[randomIndex]
            generatedCode.append(randomChar)
        }

        return generatedCode.toString()
    }

}