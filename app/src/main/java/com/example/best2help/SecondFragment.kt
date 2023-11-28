package com.example.best2help

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.best2help.databinding.ActivityMainBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SecondFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class SecondFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // Initialize variables for dropdown
    private lateinit var textViewSkillset: TextView
    private var selectedSkillSet = BooleanArray(0)
    private val skillList = ArrayList<Int>()
    private var skillArray = emptyArray<String>()

    // Intialize variable for database
    private lateinit var dbref : DatabaseReference
    private lateinit var dbrefUser : DatabaseReference

    // Initiazlie inputs
    private lateinit var registerBtn: Button
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var etCpass: EditText
    private lateinit var etContact: EditText
    private lateinit var etaddress: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        fetchSkills()

        // Declare all the inputs
        registerBtn = view!!.findViewById(R.id.btn_signup)
        etUsername = view!!.findViewById(R.id.et_username)
        etEmail = view!!.findViewById(R.id.et_email)
        etPass = view!!.findViewById(R.id.et_password)
        etCpass = view!!.findViewById(R.id.et_cpassword)
        etContact = view!!.findViewById(R.id.et_contactNumber)
        etaddress = view!!.findViewById(R.id.et_addressInfo)

        registerBtn.setOnClickListener {

            // Perform validation
            if (textViewSkillset.text.isNotEmpty() && etUsername.text.isNotEmpty() && etEmail.text.isNotEmpty()
                && etPass.text.isNotEmpty() && etCpass.text.isNotEmpty() && etContact.text.isNotEmpty()
                && etaddress.text.isNotEmpty()) {

                if (isEmailValid(etEmail.text.toString())){

                    emailExists { emailExists ->

                        if (!emailExists) {
                            // Email not exists, handle accordingly

                            if (verifyPasswordFormat(etPass.text.toString())){

                                if (etPass.text.toString() == etCpass.text.toString()){

                                    if (validNumber(etContact.text.toString())){
                                        // To add data to Realtime Firebase

                                        val uid = generateUid()

                                        var intent = Intent(context, ConfirmRegisterActivity::class.java)
                                        intent.putExtra("EMAIL_KEY", etEmail.text.toString())
                                        intent.putExtra("ADDRESS_KEY", etaddress.text.toString())
                                        intent.putExtra("CONTACT_KEY", etContact.text.toString())
                                        intent.putExtra("PASS_KEY", etPass.text.toString())
                                        intent.putExtra("UID_KEY", uid)
                                        intent.putExtra("USERNAME_KEY", etUsername.text.toString())
                                        intent.putExtra("SKILLS_KEY", textViewSkillset.text.toString())
                                        startActivity(intent)

                                    } else {
                                        DialogUtils.errorDialog(requireContext(), "Oops, contact number format!")

                                    }

                                } else {
                                    DialogUtils.errorDialog(requireContext(), "Oops, password not match!")
                                }

                            } else {
                                DialogUtils.errorDialog(requireContext(), "Oops, Allow only alphabet characters, digits, and the specified symbols, and the password must be at least 8 characters long.")
                            }

                        } else {
                            // Email does not exist, handle accordingly
                            DialogUtils.errorDialog(requireContext(), "Oops, email existed!")
                        }
                    }

                } else {
                    DialogUtils.errorDialog(requireContext(), "Oops, email format is not right!")
                }

            } else {
                DialogUtils.errorDialog(requireContext(), "Oops, it's not complete yet!")
            }

//        DialogUtils.errorDialog(requireContext(), textViewSkillset.text.toString())
//        DialogUtils.errorDialog(requireContext(), etUsername.text.toString())

        //            var intent = Intent(context, ProfileActivity::class.java)
        //            startActivity(intent)
        }

        return view
    }

    private fun verifyPasswordFormat(password: String): Boolean {
        // Define a regular expression for password validation
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}\$"

        // Match the password against the regex
        return password.matches(passwordRegex.toRegex())
    }

    private fun fetchSkills() {
        dbref = FirebaseDatabase.getInstance().getReference("Skillset")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (skillSnapshot in snapshot.children) {
                        val skill = skillSnapshot.getValue(Skillset::class.java)
                        val skillName = skill?.skillsetName.toString()

                        // I want to append skillName to skillArray ... How ?
                        // Check if the skillName is not already in the skillArray
                        if (!skillArray.contains(skillName)) {
                            // Append skillName to skillArray
                            skillArray += skillName
                        }

//                        Toast.makeText(context, skill!!.skillsetName.toString(), Toast.LENGTH_SHORT).show()
//                        Toast.makeText(context, generateUid(), Toast.LENGTH_SHORT).show()

                    }
                }

                dropdownFunction()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+$")
        return email.matches(emailRegex)
    }

    private fun dropdownFunction() {
        textViewSkillset = requireView().findViewById(R.id.dropdown_skillset)
        selectedSkillSet = BooleanArray(skillArray.size)
        textViewSkillset.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select Skill Set")
            builder.setCancelable(false)

            builder.setMultiChoiceItems(skillArray, selectedSkillSet) { _, i, b ->
                // check condition
                if (b) {
                    // when checkbox selected
                    // Add position in skill list
                    skillList.add(i)
                    // Sort array list
                    skillList.sort()
                } else {
                    // when checkbox unselected
                    // Remove position from langList
                    skillList.remove(i)
                }
            }

            builder.setPositiveButton("OK") { _, _ ->
                // Initialize string builder
                val stringBuilder = StringBuilder()
                // use for loop
                for (j in skillList.indices) {
                    // concat array value
                    stringBuilder.append(skillArray[skillList[j]])
                    // check condition
                    if (j != skillList.size - 1) {
                        // When j value not equal
                        // to lang list size - 1
                        // add comma
                        stringBuilder.append(", ")
                    }
                }
                // set text on textView
                textViewSkillset.text = stringBuilder.toString()

                // Check value
                Toast.makeText(context, stringBuilder.toString(), Toast.LENGTH_SHORT).show()
            }

            builder.setNegativeButton("Cancel") { dialogInterface, _ ->
                // dismiss dialog
                dialogInterface.dismiss()
            }
            builder.setNeutralButton("Clear All") { _, _ ->
                // use for loop
                for (j in selectedSkillSet.indices) {
                    // remove all selection
                    selectedSkillSet[j] = false
                    // clear language list
                    skillList.clear()
                    // clear text view value
                    textViewSkillset.text = ""
                }
            }
            // show dialog
            builder.show()
        }
    }

    // Function to generate a UID with timestamp and random component
    private fun generateUid(): String {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val randomPart = UUID.randomUUID().toString().substring(0, 8)

        return "$timestamp-$randomPart"
    }

    private fun emailExists(email: String, callback: (Boolean) -> Unit) {
        // Try to find email
        val dbref = FirebaseDatabase.getInstance().getReference("Volunteer")
        val query = dbref.orderByChild("email").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SecondFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SecondFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun emailExists(callback: (Boolean) -> Unit) {
        // Try to find email
        etEmail = view!!.findViewById(R.id.et_email)

        val dbref = FirebaseDatabase.getInstance().getReference("Volunteer")
        val query = dbref.orderByChild("email").equalTo(etEmail.text.toString())

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }

    private fun validNumber(number: String): Boolean {
        // Define the regular expression for valid phone numbers
        val regex = Regex("^\\+60\\d{10}|^\\d{10,11}|^\\d{3}-\\d{7,8}$")

        // Check if the number matches the regular expression
        return regex.matches(number)
    }


}