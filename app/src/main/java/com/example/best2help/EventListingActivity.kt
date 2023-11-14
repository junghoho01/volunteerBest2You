package com.example.best2help

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.best2help.databinding.ActivityEventListingBinding
import com.google.firebase.database.*

class EventListingActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEventListingBinding
    private lateinit var dbref : DatabaseReference
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventArrayList: ArrayList<Event>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recycler View
        eventRecyclerView = findViewById(R.id.eventListRecyclerView)
        eventRecyclerView.layoutManager = LinearLayoutManager(this)
        eventRecyclerView.setHasFixedSize(true)
        eventArrayList = arrayListOf<Event>()

        getEventListDate()

        binding.imgArrowBack.setOnClickListener {
            toHomeActivity()
        }
    }

    private fun toHomeActivity() {
        var intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun getEventListDate() {
        try {
            // Get data from event
            dbref = FirebaseDatabase.getInstance().getReference("Event")
            dbref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (eventSnapshot in snapshot.children) {
                            val event = eventSnapshot.getValue(Event::class.java)

                            // Event status need to be approve
                            if (event?.eventApproval == "Approve") {

                                if (event?.eventStatus == "Ongoing") {
                                    eventArrayList.add(event!!)
                                }

                            }
                        }

                        // For listener
                        var adapter = EventListAdapter(eventArrayList)

                        // Show only the top 10 events
                        eventRecyclerView.adapter = adapter
                        adapter.setOnItemClickListener(object : EventListAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                var id = eventArrayList[position].eventId
                                var intent = Intent(this@EventListingActivity, EventDetailsActivity::class.java)
                                intent.putExtra("EVENTID_KEY", id)
                                startActivity(intent)
                            }
                        })
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