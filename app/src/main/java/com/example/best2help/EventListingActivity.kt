package com.example.best2help

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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

        binding.btnAutoJoinEvent.setOnClickListener {
            DialogUtils.matchingDialog(this, "Matching Event...")

            // Hold for 4 seconds
            Handler().postDelayed({
                // Your code to be executed after 2 seconds
                // Dismiss the previous dialog if it exists
                DialogUtils.dismissDialog(this)
                matchEvent()

            }, 4000)
        }
    }

    private fun matchEvent() {
//
//        // Hold for 2 seconds
//        Handler().postDelayed({
//            // Your code to be executed after 2 seconds
//            // Dismiss the previous dialog if it exists
//            DialogUtils.dismissDialog(this)
//        }, 2000) // 2000 milliseconds = 2 seconds

        // Get event with valid eventApproval / eventStatus
//        DialogUtils.succsessDialog(this, eventArrayList.count().toString())

        // Get Own skillset
        try {
            // Get a reference to the Firebase database
            val dbref = FirebaseDatabase.getInstance().getReference("Volunteer")

            val sharedPref = getSharedPreferences("my_app_session", Context.MODE_PRIVATE)
            val userEmail = sharedPref.getString("user_email", null).toString()

            DialogUtils.getDetails(userEmail) { volunteer ->
                if (volunteer != null) {
                    // Volunteer found, do something with the details
                    // Use orderByChild to query based on the uid
                    val query = dbref.orderByChild("uid").equalTo(volunteer.uid.toString())

                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (eventSnapshot in snapshot.children) {
                                    val volunteer = eventSnapshot.getValue(Volunteer::class.java)
                                    splitSkillset(volunteer!!.skills.toString())
                                }
                            } else {
                                // Handle the case where no event with the specified ID is found
                                // For example, display an error message or navigate back
                                Log.d(ContentValues.TAG, "No volunteer found with ID")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle onCancelled error
                            Log.e(ContentValues.TAG, "Firebase Database onCancelled: ${error.message}")
                        }
                    })
                } else {
                    // Volunteer not found
                    DialogUtils.errorDialog(this, "Databaser Error...")
                }
            }

        } catch (e: Exception) {
            // Handle other exceptions
            Log.e(ContentValues.TAG, "Error fetching volunteer details: ${e.message}", e)
        }
    }

    private fun splitSkillset(skills: String) {

        // User skill in list
        val userSkillList = skills.split(", ");

        // Count total valid event
        var count = eventArrayList.count()


        // Initialize variable
        var bestMatchEvent: Event? = null
        var maxMatchingSkills = 0

        // Run the matching
        for (i in 0 until count){
            val eventSkill = eventArrayList[i].skillSet

            // Split the eventSkill string into a list of strings and trim each skill
            val eventSkillList = eventSkill!!.split(",").map { it.trim() }

            // Convert both lists to sets for finding intersection
            val userSkillSet = userSkillList.toSet()
            val eventSkillSet = eventSkillList.toSet()

            //DialogUtils.succsessDialog(this, userSkillSet.toString() + eventSkillSet.toString())

            val matchingSkills = userSkillSet.intersect(eventSkillSet).count()

            // Check if the current event has more matching skills than the previous best match
            if (matchingSkills > maxMatchingSkills) {
                // Update the best match event
                bestMatchEvent = eventArrayList[i]
                maxMatchingSkills = matchingSkills
            }

        }

        if (bestMatchEvent != null){
            DialogUtils.joinEventDialog(this, "Best Match Event: ${bestMatchEvent?.eventName.toString()}", "20231116143606-cf5e191f", bestMatchEvent)
        } else {
            DialogUtils.errorDialog(this, "Oops, no event matched you..")
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