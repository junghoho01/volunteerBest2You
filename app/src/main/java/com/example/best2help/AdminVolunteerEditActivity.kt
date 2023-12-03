package com.example.best2help

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.best2help.databinding.ActivityAdminVolunteerEditBinding
import com.google.firebase.database.*

class AdminVolunteerEditActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdminVolunteerEditBinding
    private lateinit var dbref : DatabaseReference

    // Initialize variables for dropdown
    private lateinit var textViewSkillset: TextView
    private var selectedSkillSet = BooleanArray(0)
    private val skillList = ArrayList<Int>()
    private var skillArray = emptyArray<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminVolunteerEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgArrowBack.setOnClickListener {
            finish()
        }

        val intent = intent
        val email = intent.getStringExtra("VOLUNTEER_EMAIL") // For string data
        val uid = intent.getStringExtra("VOLUNTEER_UID") // For string data

        fetchSkills(email)
        populateVolunteerForm(email)

        binding.btnSave.setOnClickListener{
            updateData(email, uid)
        }
        
        binding.btnDelete.setOnClickListener {
            deleteData(uid)
        }
    }

    private fun deleteData(uid: String?) {
        if (uid != null) {
            val dbrefUser = FirebaseDatabase.getInstance().getReference("Volunteer").child(uid)

            dbrefUser.removeValue()
                .addOnSuccessListener {
//                    DialogUtils.succsessDialog(this, "Delete Succesfully!")
                    var intent = Intent(this, AdminVolunteerActivity::class.java)
                    intent.putExtra("FLAG", "1")
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    DialogUtils.errorDialog(this, "Oops, Fail to delete...")
                }
        } else {
            DialogUtils.errorDialog(this, "Oops, Invalid user ID!")
        }
    }

    private fun updateData(email: String?, uid: String?) {
        // Validate everything first
        var username = binding.etUsername.text.toString()
        var contactNo = binding.etContactNumber.text.toString()
        var address =  binding.etAddressInfo.text.toString()
        var skillset = binding.dropdownSkillset.text.toString()
        var password = binding.etPassword.text.toString()

        if (username.isNotEmpty() && contactNo.isNotEmpty() && address.isNotEmpty() && skillset.isNotEmpty() && password.isNotEmpty()
            && verifyPasswordFormat(password)){

            if(validNumber(contactNo)){
                val volunteerMap = mapOf(
                    "username" to username,
                    "contact" to contactNo,
                    "address" to address,
                    "skills" to skillset,
                    "password" to password
                )

                val dbrefUser = FirebaseDatabase.getInstance().getReference("Volunteer").child(uid!!)

                dbrefUser.updateChildren(volunteerMap)
                    .addOnSuccessListener {
                        DialogUtils.succsessDialog(this, "Update Succesfully!")
                    }
                    .addOnFailureListener {
                        DialogUtils.errorDialog(this, "Oops, Fail to update...")
                    }
            } else {
                DialogUtils.errorDialog(this, "Oops, wrong number format!")

            }

        } else {
            DialogUtils.errorDialog(this, "Oops, it's not complete yet!")
        }
    }

    private fun populateVolunteerForm(email: String?) {
        DialogUtils.getDetails(email!!) { volunteer ->
            if (volunteer != null) {

                binding.etUsername.setText(volunteer.username.toString())
                binding.etEmail.setText(volunteer.email.toString())
                binding.etContactNumber.setText(volunteer.contact.toString())
                binding.etAddressInfo.setText(volunteer.address.toString())
                binding.dropdownSkillset.text = volunteer.skills.toString()
                binding.etPassword.setText(volunteer.password.toString())
                dropdownFunction(volunteer.skills.toString())

            } else {
                // Volunteer not found
                DialogUtils.errorDialog(this, "Database Error...")
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

                // Update the selectedSkillSet array to reflect the changes
                for (i in skillArray.indices) {
                    selectedSkillSet[i] = false
                }

                // Clear the text in the textViewSkillset
                textViewSkillset.text = ""
            }
            // show dialog
            builder.show()
        }
    }

    private fun fetchSkills(email: String?) {
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

                // Now, you have the updated skillArray
                // Pass the volunteer's skills to the dropdownFunction
                DialogUtils.getDetails(email!!) { volunteer ->
                    if (volunteer != null) {
                        // Volunteer found, do something with the details
                        // ...

                        // Call dropdownFunction with volunteer's skills
                        dropdownFunction(volunteer.skills.toString())
                    } else {
                        // Volunteer not found
                        DialogUtils.errorDialog(this@AdminVolunteerEditActivity, "Databaser Error...")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun validNumber(number: String): Boolean {
        // Define the regular expression for valid phone numbers
        val regex = Regex("^\\+60\\d{10}|^\\d{10,11}|^\\d{3}-\\d{7,8}$")

        // Check if the number matches the regular expression
        return regex.matches(number)
    }

    private fun verifyPasswordFormat(password: String): Boolean {
        // Define a regular expression for password validation
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}\$"

        // Match the password against the regex
        return password.matches(passwordRegex.toRegex())
    }
}