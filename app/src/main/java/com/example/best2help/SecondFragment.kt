package com.example.best2help

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*

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
    private lateinit var textView: TextView
    private var selectedSkillSet = BooleanArray(0)
    private val skillList = ArrayList<Int>()
    private var skillArray = emptyArray<String>()
    private lateinit var dbref : DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    interface FetchSkillsCallback {
        fun onSkillsFetched()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        fetchSkills()

        return view
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
                    }
                }

                dropdownFunction()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun dropdownFunction() {
        textView = view!!.findViewById(R.id.dropdown_skillset)
        selectedSkillSet = BooleanArray(skillArray.size)
        textView.setOnClickListener {
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
                textView.text = stringBuilder.toString()

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
                    textView.text = ""
                }
            }
            // show dialog
            builder.show()
        }
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
}