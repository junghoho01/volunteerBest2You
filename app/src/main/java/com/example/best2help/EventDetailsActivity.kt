package com.example.best2help

import android.content.ContentValues.TAG
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.best2help.databinding.ActivityEventDetailsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class EventDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding : ActivityEventDetailsBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    // Assuming event.eventStartTime is a string in the format "HH:mm"
    val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the intent that started this activity
        val intent = intent
        val id = intent.getStringExtra("EVENTID_KEY").toString()

        showDetails(id)

        // Initialize mapView
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.imgArrowBack.setOnClickListener {
            finish()
        }
    }

    private fun joinEvent(eventId: String) {
        val sharedPref = getSharedPreferences("my_app_session", MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", null).toString()

        DialogUtils.independentJoin(this, "Are you sure you wanna join?", eventId, userEmail)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // To use address
        val address = binding.tvAddressDetails.text.toString()
        val geocoder = Geocoder(this)

        try {
            val locationList = geocoder.getFromLocationName(address, 1)
            if (locationList!!.isNotEmpty()){
                val latitude = locationList[0].latitude
                val longitude = locationList[0].longitude

                val eventLocation = LatLng(latitude, longitude)
                googleMap.addMarker(MarkerOptions().position(eventLocation).title("Event Location"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(eventLocation))
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            } else {
                DialogUtils.errorDialog(this, "Oops, Map Error!")
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., IOException, IllegalArgumentException) here
            e.printStackTrace()
        }

//        // Set up the marker for the event location
//        val eventLocation = LatLng(12.3456, 78.9101) // Replace with your actual coordinates
//        googleMap.addMarker(MarkerOptions().position(eventLocation).title("Event Location"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(eventLocation))
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
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

            val startTime = inputFormat.parse(event.eventStartTime)
            val formattedTime = outputFormat.format(startTime)
            binding.tvTimeDetails.text = formattedTime

            // To fetch contactNo and email

            DialogUtils.getOrganizerDetails(event.tryNia.toString()) { org ->
                if (org != null) {
                    // org found, do something with the details
                    binding.tvEmailDetails.text = org.organizerEmail.toString()
                    binding.tvContactNoDetails.text = org.phoneNo.toString()
                } else {
                    // Volunteer not found
                    DialogUtils.errorDialog(this, "Databaser Error...")
                }
            }


            Picasso.get().load(event.eventPicName).into(binding.imgEvent)
        }

        binding.btnJoinEvent.setOnClickListener { joinEvent(event!!.eventId.toString()) }
    }

    private fun formatSkills(skillSet: String): String {
        // Split the skillSet string into a list of skills
        val skillsList = skillSet.split(",")

        // Create a formatted string with bullet points
        return skillsList.joinToString(separator = "\n● ")
    }

}