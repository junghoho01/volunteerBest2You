package com.example.best2help

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Paint.Join
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.best2help.databinding.ActivityHistoryBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHistoryBinding
    private lateinit var dbref : DatabaseReference
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventArrayList: ArrayList<JointEvent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recycler View
        eventRecyclerView = findViewById(R.id.joinedEventListRecyclerView)
        eventRecyclerView.layoutManager = LinearLayoutManager(this)
        eventRecyclerView.setHasFixedSize(true)
        eventArrayList = arrayListOf<JointEvent>()

        getEventListDate()

        binding.imgArrowBack.setOnClickListener {
            finish()
        }
    }

    private fun getEventListDate() {
        val sharedPref = getSharedPreferences("my_app_session", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", null).toString()

        try {
            // Get data from event
            dbref = FirebaseDatabase.getInstance().getReference("JointEvent")
            dbref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val unsortedList = ArrayList<JointEvent>()

                        for (eventSnapshot in snapshot.children) {
                            val event = eventSnapshot.getValue(JointEvent::class.java)

                            if (event!!.volunterEmail.toString() == userEmail) {
                                unsortedList.add(event)
                            }
                        }

                        // Reverse the order of the list
                        val reversedList = unsortedList.reversed()

                        var adapter = EventListJoinedAdapter(ArrayList(reversedList))
                        eventRecyclerView.adapter = adapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled error
                    Log.e(ContentValues.TAG, "Firebase Database onCancelled: ${error.message}")
                }
            })
        } catch (e: Exception) {
            // Handle other exceptions
            Log.e(ContentValues.TAG, "Error fetching event data: ${e.message}", e)
        }
    }

}