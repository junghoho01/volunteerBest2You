package com.example.best2help

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.renderscript.Sampler.Value
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object DialogUtils {

    private var previousDialog: Dialog? = null
    private lateinit var dbref: DatabaseReference

    fun errorDialog(context: Context, title: String) {

        //Dismiss the previous dialog if it exists
        previousDialog?.dismiss()

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_denied)
        val text = dialog.findViewById<TextView>(R.id.tv_title)
        text.text = title
        val btnClose = dialog.findViewById<Button>(R.id.btnDeniedClose)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Set the current dialog as the previous dialog
        previousDialog = dialog

        dialog.show()
    }

    fun succsessDialog(context: Context, title: String) {

        //Dismiss the previous dialog if it exists
        previousDialog?.dismiss()

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_success)
        val text = dialog.findViewById<TextView>(R.id.tv_title)
        text.text = title
        val btnClose = dialog.findViewById<Button>(R.id.btnDeniedClose)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Set the current dialog as the previous dialog
        previousDialog = dialog

        dialog.show()
    }

    fun matchingDialog(context: Context, title: String) {

        //Dismiss the previous dialog if it exists
        previousDialog?.dismiss()

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_matching)
        val text = dialog.findViewById<TextView>(R.id.tv_title)
        text.text = title
//        val btnClose = dialog.findViewById<Button>(R.id.btnDeniedClose)
//        btnClose.setOnClickListener {
//            dialog.dismiss()
//        }

        // Set the current dialog as the previous dialog
        previousDialog = dialog

        dialog.show()
    }

    fun joinEventDialog(context: Context, title: String, uid: String, bestMatchEvent: Event) {

        //Dismiss the previous dialog if it exists
        previousDialog?.dismiss()

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_joinevent)
        val text = dialog.findViewById<TextView>(R.id.tv_title)
        text.text = title
        val btnDeny = dialog.findViewById<Button>(R.id.btnDeny)
        val btnAccept = dialog.findViewById<Button>(R.id.btnDeny)

        btnDeny.setOnClickListener {
            declineEvent(context, uid, bestMatchEvent)
        }

        // Set the current dialog as the previous dialog
        previousDialog = dialog

        dialog.show()
    }

    fun dismissDialog(context: Context) {
        //Dismiss the previous dialog if it exists
        previousDialog?.dismiss()
    }

    fun declineEvent(context: Context, uid: String, bestMatchEvent: Event) {
        val dbrefUser = FirebaseDatabase.getInstance().getReference("Volunteer").child(uid)
        val declineEventRef = dbrefUser.child("declineEvent")

        declineEventRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get the current skillString
                    val currentSkillString = snapshot.getValue(String::class.java) ?: ""

                    // Append the new eventId
                    val eventId = bestMatchEvent.eventId
                    var appendSkillset = ""

                    appendSkillset = if (currentSkillString.isEmpty()){
                        eventId!!
                    } else {
                        "$currentSkillString;$eventId"
                    }

                    // Update the entire list in the database
                    declineEventRef.setValue(appendSkillset)
                        .addOnSuccessListener {
                            // Update successful, handle success
                            // You can add any additional logic here
                            dismissDialog(context)
                        }
                        .addOnFailureListener {
                            // Update failed, handle failure
                            // You can add any additional logic here
                            dismissDialog(context)
                        }

                } else {
                    // If the node doesn't exist, create it with the new eventId
                    val eventId = bestMatchEvent.eventId
                    declineEventRef.setValue(eventId)
                        .addOnSuccessListener {
                            // Update successful, handle success
                            // You can add any additional logic here
                            dismissDialog(context)
                        }
                        .addOnFailureListener {
                            // Update failed, handle failure
                            // You can add any additional logic here
                            dismissDialog(context)
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                errorDialog(context, "Database Error")
            }
        })
    }

}