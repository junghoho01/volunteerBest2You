package com.example.best2help

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.best2help.databinding.ActivityAdminVolunteerBinding
import com.example.best2help.databinding.ActivityEventListingBinding
import com.google.firebase.database.*

class AdminVolunteerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdminVolunteerBinding
    private lateinit var dbref : DatabaseReference
    private lateinit var volunteerRecyclerView: RecyclerView
    private lateinit var volunteerArrayList: ArrayList<Volunteer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminVolunteerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recycler View
        volunteerRecyclerView = findViewById(R.id.volunteerListRecyclerView)
        volunteerRecyclerView.layoutManager = LinearLayoutManager(this)
        volunteerRecyclerView.setHasFixedSize(true)
        volunteerArrayList = arrayListOf<Volunteer>()

        getVolunteerList()

        binding.imgArrowBack.setOnClickListener {
            var intent = Intent(this, AdminVolunteerActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getVolunteerList() {
        dbref = FirebaseDatabase.getInstance().getReference("Volunteer")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (eventSnapshot in snapshot.children) {
                        val volunteer = eventSnapshot.getValue(Volunteer::class.java)

                        if(volunteer!!.username != "admin"){
                            volunteerArrayList.add(volunteer!!)
                        }
                    }

                    // For listener
                    var adapter = VolunteerListAdapter(volunteerArrayList)
                    volunteerRecyclerView.adapter = adapter
                    adapter.setOnItemClickListener(object : VolunteerListAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            var uid = volunteerArrayList[position].uid
                            var email = volunteerArrayList[position].email
//                            DialogUtils.succsessDialog(this@AdminVolunteerActivity, username.toString())
                            var intent = Intent(this@AdminVolunteerActivity, AdminVolunteerEditActivity::class.java)
                            intent.putExtra("VOLUNTEER_EMAIL", email)
                            intent.putExtra("VOLUNTEER_UID", uid)
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
    }

}