package com.example.best2help

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.best2help.databinding.ActivityProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding

    // Initialize variables for dropdown
    private lateinit var textViewSkillset: TextView
    private var selectedSkillSet = BooleanArray(0)
    private val skillList = ArrayList<Int>()
    private var skillArray = emptyArray<String>()

    // Intialize variable for database
    private lateinit var dbref : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchSkills()

        binding.imgArrowBack.setOnClickListener {
            finish()
        }

        val sharedPref = getSharedPreferences("my_app_session", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", null).toString()

        DialogUtils.getDetails(userEmail) { volunteer ->
            if (volunteer != null) {
                // Volunteer found, do something with the details
                binding.etUsername.setText(volunteer.username.toString())
                binding.etEmail.setText(volunteer.email.toString())
                binding.etContactNumber.setText(volunteer.contact.toString())
                binding.etAddressInfo.setText(volunteer.address.toString())
                binding.dropdownSkillset.text = volunteer.skills.toString()
                dropdownFunction(volunteer.skills.toString())
            } else {
                // Volunteer not found
                DialogUtils.errorDialog(this, "Databaser Error...")
            }
        }

        binding.btnSave.setOnClickListener{
            updateData()
        }
    }

    private fun updateData() {
        // Validate everything first
        var username = binding.etUsername.text.toString()
        var contactNo = binding.etContactNumber.text.toString()
        var address =  binding.etAddressInfo.text.toString()
        var skillset = binding.dropdownSkillset.text.toString()

        val sharedPref = getSharedPreferences("my_app_session", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", null).toString()

        DialogUtils.getDetails(userEmail) { volunteer ->
            if (volunteer != null) {
                if (username.isNotEmpty() && contactNo.isNotEmpty() && address.isNotEmpty() && skillset.isNotEmpty()){

                    val volunteerMap = mapOf(
                        "username" to username,
                        "contact" to contactNo,
                        "address" to address,
                        "skills" to skillset
                    )

                    val dbrefUser = FirebaseDatabase.getInstance().getReference("Volunteer").child(volunteer.uid.toString())

                    dbrefUser.updateChildren(volunteerMap)
                        .addOnSuccessListener {
                            DialogUtils.succsessDialog(this, "Update Succesfully!")
                        }
                        .addOnFailureListener {
                            DialogUtils.errorDialog(this, "Oops, Fail to update...")
                        }

                } else {
                    DialogUtils.errorDialog(this, "Oops, it's not complete yet!")
                }
            } else {
                // Volunteer not found
                DialogUtils.errorDialog(this, "Databaser Error...")
            }
        }
    }

    private fun dropdownFunction(volunteerSkills: String) {
        textViewSkillset = findViewById(R.id.dropdown_skillset)
        selectedSkillSet = BooleanArray(skillArray.size)

        // Split the volunteer skills string into a list of skills
        val volunteerSkillsList = volunteerSkills.split(", ")

        val selectedSkillsSet = HashSet<String>(volunteerSkillsList)

        for (i in skillArray.indices) {
            // Check if the skill is in the volunteer's skills list
            selectedSkillSet[i] = selectedSkillsSet.contains(skillArray[i])
        }

        textViewSkillset.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Skill Set")
            builder.setCancelable(false)

            builder.setMultiChoiceItems(skillArray, selectedSkillSet) { _, i, b ->
                // Update selectedSkillsSet based on user selection
                if (b) {
                    selectedSkillsSet.add(skillArray[i])
                } else {
                    selectedSkillsSet.remove(skillArray[i])
                }
            }

            builder.setPositiveButton("OK") { _, _ ->
                // Convert the selectedSkillsSet to a list
                val selectedSkillsList = selectedSkillsSet.toList()

                // Initialize string builder
                val stringBuilder = StringBuilder()
                // use for loop
                for (j in selectedSkillsList.indices) {
                    // concat array value
                    stringBuilder.append(selectedSkillsList[j])
                    // check condition
                    if (j != selectedSkillsList.size - 1) {
                        // When j value not equal
                        // to lang list size - 1
                        // add comma
                        stringBuilder.append(", ")
                    }
                }
                // set text on textView
                textViewSkillset.text = stringBuilder.toString()

                // Check value
                Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_SHORT).show()
            }

            builder.setNegativeButton("Cancel") { dialogInterface, _ ->
                // dismiss dialog
                dialogInterface.dismiss()
            }
            builder.setNeutralButton("Clear All") { _, _ ->
                // clear selectedSkillsSet
                selectedSkillsSet.clear()
            }
            // show dialog
            builder.show()
        }
    }


    private fun fetchSkills() {
        dbref = FirebaseDatabase.getInstance().getReference("Skillset")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (skillSnapshot in snapshot.children) {
                        val skill = skillSnapshot.getValue(Skillset::class.java)
                        val skillName = skill?.skillsetName.toString()

                        // Check if the skillName is not already in the skillArray
                        if (!skillArray.contains(skillName)) {
                            // Append skillName to skillArray
                            skillArray += skillName
                        }
                    }
                }

                val sharedPref = getSharedPreferences("my_app_session", Context.MODE_PRIVATE)
                val userEmail = sharedPref.getString("user_email", null).toString()

                // Now, you have the updated skillArray
                // Pass the volunteer's skills to the dropdownFunction
                DialogUtils.getDetails(userEmail) { volunteer ->
                    if (volunteer != null) {
                        // Volunteer found, do something with the details
                        // ...

                        // Call dropdownFunction with volunteer's skills
                        dropdownFunction(volunteer.skills.toString())
                    } else {
                        // Volunteer not found
                        DialogUtils.errorDialog(this@ProfileActivity, "Databaser Error...")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

}