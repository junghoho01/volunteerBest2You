package com.example.best2help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class FirstFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // Intialize variable for database
    private lateinit var dbref : DatabaseReference

    // Initiazlie inputs
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button


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

        val view = inflater.inflate(R.layout.fragment_first, container, false)

        // Declare all the inputs
        btnLogin = view!!.findViewById(R.id.btn_login)
        etEmail = view.findViewById(R.id.et_email)
        etPassword= view.findViewById(R.id.et_password)

        view.findViewById<TextView>(R.id.tv_forgotPassword).setOnClickListener {
            // Start the new activity here
            val intent = Intent(activity, ForgotPassword::class.java)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.btn_login).setOnClickListener {

            if (etEmail.text.isNotEmpty() && etPassword.text.isNotEmpty()) {

                // Retrieve from realtime firebase
                dbref = FirebaseDatabase.getInstance().getReference("Volunteer")

                val query = dbref.orderByChild("email").equalTo(etEmail.text.toString())

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Email exists in the database, check password
                            for (userSnapshot in dataSnapshot.children) {
                                val user = userSnapshot.getValue(Volunteer::class.java)
                                if (user?.password == etPassword.text.toString()) {
                                    // Password matches, login successful

                                    val sharedPref = requireActivity().getSharedPreferences("my_app_session", Context.MODE_PRIVATE)
                                    val editor = sharedPref.edit()

                                    editor.putString("user_email", etEmail.text.toString())
                                    editor.apply()

                                    val intent = Intent(activity, HomeActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    // Password doesn't match, show an error message
                                    DialogUtils.errorDialog(requireContext(), "Oops, Invalid credential!")
                                }
                            }
                        } else {
                            // Email doesn't exist, show an error message
                            DialogUtils.errorDialog(requireContext(), "Oops, Invalid credential!")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                        DialogUtils.errorDialog(requireContext(), "Oops, Database Error!")
                    }
                })

            }
            else {
                DialogUtils.errorDialog(requireContext(), "Oops, it's not complete yet!")
            }
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FirstFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FirstFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}