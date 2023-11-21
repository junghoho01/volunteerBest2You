package com.example.best2help

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.best2help.databinding.ActivityHomeBinding
import com.google.firebase.database.*


class HomeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHomeBinding
    private lateinit var dbref : DatabaseReference

    // For 10 Latest
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventArrayList: ArrayList<Event>

    // For Recently Added
    private lateinit var recentlyAddedeventRecyclerView: RecyclerView
    private lateinit var recentlyAddedeventArrayList: ArrayList<Event>

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

        // Recently Added
        recentlyAddedeventRecyclerView = findViewById(R.id.tv_recentlyAddedRecyclerView)
        recentlyAddedeventRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recentlyAddedeventRecyclerView.setHasFixedSize(true)
        recentlyAddedeventArrayList = arrayListOf<Event>()

        getEventDate()

        fetchRecentlyAddedEvent()

        binding.tvMore.setOnClickListener { toEventListing() }

        binding.imgProfile.setOnClickListener{ toProfile() }
        binding.imgHistory.setOnClickListener{ toHistory() }
    }

    private fun toHistory() {
        var intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }

    private fun toProfile() {
        var intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun fetchRecentlyAddedEvent() {
        try {
            // Get data from event
            dbref = FirebaseDatabase.getInstance().getReference("Event")
            dbref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (eventSnapshot in snapshot.children) {
                            val event = eventSnapshot.getValue(Event::class.java)

                            // Event status need to be approve
                            if (event?.eventApproval == "Approve" && event.eventStatus == "Ongoing") {
                                recentlyAddedeventArrayList.add(event!!)
                            }
                        }

                        recentlyAddedeventArrayList.sortBy { it.eventStartDate }

                        // For listener
                        var adapter = EventAdapter(ArrayList(recentlyAddedeventArrayList.take(5)))
                        // Show only the top 10 events
                        recentlyAddedeventRecyclerView.adapter = adapter
                        adapter.setOnItemClickListener(object : EventAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                var id = recentlyAddedeventArrayList[position].eventId
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

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            finish() // Exit the app
        } else {
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000) // Reset the flag after 2 seconds
        }
    }
}