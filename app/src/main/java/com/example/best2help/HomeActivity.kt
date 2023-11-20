package com.example.best2help

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.best2help.databinding.ActivityHomeBinding
import com.google.firebase.database.*


class HomeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHomeBinding
    private lateinit var dbref : DatabaseReference
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventArrayList: ArrayList<Event>
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val sharedPref = getSharedPreferences("my_app_session", Context.MODE_PRIVATE)
//        val userEmail = sharedPref.getString("user_email", null).toString()
//
//        DialogUtils.getDetails(userEmail) { volunteer ->
//            if (volunteer != null) {
//                // Volunteer found, do something with the details
//                DialogUtils.succsessDialog(this, volunteer.uid.toString())
//            } else {
//                // Volunteer not found
//                DialogUtils.errorDialog(this, "Problem...")
//            }
//        }

        // Get value
        val intent = intent
        val flag = intent.getStringExtra("FLAG_KEY").toString()

        if (flag == "1") {
            DialogUtils.succsessDialog(this, "Congratulations! You have register successfully!")
        }

        // Recycler View
        eventRecyclerView = findViewById(R.id.eventRecyclerView)
        eventRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        eventRecyclerView.setHasFixedSize(true)
        eventArrayList = arrayListOf<Event>()

        getEventDate()

        binding.tvMore.setOnClickListener { toEventListing() }
    }

    private fun toEventListing() {
        var intent = Intent(this, EventListingActivity::class.java)
        startActivity(intent)
    }

    private fun getEventDate() {
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
                        var adapter = EventAdapter(ArrayList(eventArrayList.take(10)))
                        // Show only the top 10 events
                        eventRecyclerView.adapter = adapter
                        adapter.setOnItemClickListener(object : EventAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                var id = eventArrayList[position].eventId
                                var intent = Intent(this@HomeActivity, EventDetailsActivity::class.java)
                                intent.putExtra("EVENTID_KEY", id)
                                startActivity(intent)
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled error
                    Log.e(TAG, "Firebase Database onCancelled: ${error.message}")
                }
            })
        } catch (e: Exception) {
            // Handle other exceptions
            Log.e(TAG, "Error fetching event data: ${e.message}", e)
        }
    }
}