package com.example.best2help

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.best2help.databinding.ActivityEventDetailsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class EventDetailsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEventDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the intent that started this activity
        val intent = intent
        val id = intent.getStringExtra("EVENTID_KEY").toString()

        showDetails(id)
    }

    private fun showDetails(id: String) {
        try {
            // Get a reference to the Firebase database
            val dbref = FirebaseDatabase.getInstance().getReference("Event")

            // Use orderByChild to query based on the event ID
            val query = dbref.orderByChild("eventId").equalTo(id)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (eventSnapshot in snapshot.children) {
                            val event = eventSnapshot.getValue(Event::class.java)

                            // Now 'event' contains the details of the event with the specified ID
                            // You can update your UI or perform any other actions here
                            updateUI(event)
                        }
                    } else {
                        // Handle the case where no event with the specified ID is found
                        // For example, display an error message or navigate back
                        Log.d(TAG, "No event found with ID: $id")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled error
                    Log.e(TAG, "Firebase Database onCancelled: ${error.message}")
                }
            })
        } catch (e: Exception) {
            // Handle other exceptions
            Log.e(TAG, "Error fetching event details: ${e.message}", e)
        }
    }

    private fun updateUI(event: Event?) {
        if (event != null) {
            binding.tvTitle.text = "Event - " + event.eventName
            binding.tvEventDesc.text = event.desc
            binding.tvPositionDetails.text = "● " + formatSkills(event.skillSet!!)
            binding.tvAddressDetails.text = event.location
            binding.tvDateDetails.text = event.eventStartDate
            binding.tvTimeDetails.text = event.eventStartTime
            Picasso.get().load(event.eventPicName).into(binding.imgEvent)

        }
    }

    private fun formatSkills(skillSet: String): String {
        // Split the skillSet string into a list of skills
        val skillsList = skillSet.split(",")

        // Create a formatted string with bullet points
        return skillsList.joinToString(separator = "\n● ")
    }
}