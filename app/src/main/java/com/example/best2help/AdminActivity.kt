package com.example.best2help

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.best2help.databinding.ActivityAdminBinding
import com.example.best2help.databinding.ActivityAdminReportBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnReport.setOnClickListener {
            toReport()
        }

        binding.btnVolunteer.setOnClickListener {
            toVolunteer()
        }
    }

    private fun toVolunteer() {
        var intent = Intent(this, AdminVolunteerActivity::class.java)
        startActivity(intent)
    }

    private fun toReport() {
        var intent = Intent(this, AdminReportActivity::class.java)
        startActivity(intent)
    }
}