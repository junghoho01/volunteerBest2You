package com.example.best2help

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.best2help.databinding.ActivityConfirmRegisterBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Random

class ConfirmRegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityConfirmRegisterBinding
    private lateinit var dbrefUser : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val email = intent.getStringExtra("EMAIL_KEY").toString()
        val address = intent.getStringExtra("ADDRESS_KEY").toString()
        val contact = intent.getStringExtra("CONTACT_KEY").toString()
        val pass = intent.getStringExtra("PASS_KEY").toString()
        val uid = intent.getStringExtra("UID_KEY").toString()
        val username = intent.getStringExtra("USERNAME_KEY").toString()
        val skills = intent.getStringExtra("SKILLS_KEY").toString()


        // Congrats first
        DialogUtils.succsessDialog(this, "Yeah, Register almost there!")

        var code = runRandomCodeGenerator()

        // Send Email
        runEmailCode(code, email)

        binding.btnSubmit.setOnClickListener {

            validateCode(code, email, address, contact, pass, uid, username, skills)

        }
    }

    private fun validateCode(
        receiveCode: String,
        email: String,
        address: String,
        contact: String,
        pass: String,
        uid: String,
        username: String,
        skills: String
    ) {
        var code = binding.etVfCode.text.toString()

        if(code.isNotEmpty()){

            // Check if the code same with the generated code
            if(code == receiveCode){

                // Add the data to DB
                dbrefUser = FirebaseDatabase.getInstance().getReference("Volunteer")
                val reference = dbrefUser.child(uid)
                val userData = mapOf(
                    "address" to address,
                    "contact" to contact,
                    "email" to email,
                    "password" to pass,
                    "uid" to uid,
                    "username" to username,
                    "skills" to skills,
                    "declineEvent" to "",
                    "forgotPass" to "1",
                )
                reference.setValue(userData)

                var intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("FLAG_KEY", "1") // Set one as first time
                startActivity(intent)

            } else {
                DialogUtils.errorDialog(this, "Oops, wrong code!")
            }

        } else {
            DialogUtils.errorDialog(this, "Oops, please enter the code!")
        }
    }

    private fun runEmailCode(sendCode: String, receiverEmail: String?) {
        // Run the email code here
        val senderEmail = "appvolunteer4@gmail.com"
        val password = "piexyflafbtwkawh"
        Toast.makeText(this, receiverEmail, Toast.LENGTH_SHORT).show()
        VerificationCode.sendEmail(senderEmail, receiverEmail, password, sendCode)
    }

    private fun runRandomCodeGenerator(): String {
        val random = Random()
        val codeLength = 6
        val generatedCode = StringBuilder()

        repeat(codeLength) {
            val randomNumber = random.nextInt(10) // Generates a random number between 0 and 9
            generatedCode.append(randomNumber)
        }

        return generatedCode.toString()
    }
}