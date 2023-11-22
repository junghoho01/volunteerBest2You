package com.example.best2help

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object DialogUtils {

    private var previousDialog: Dialog? = null

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

    fun succsessDialogV2(context: Context, title: String, uid: String, bestMatchEvent: Event) {

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
            declineEvent(context, uid, bestMatchEvent)
            dialog.dismiss()
        }

        // Set the current dialog as the previous dialog
        previousDialog = dialog

        // Set canceledOnTouchOutside to false
        dialog.setCanceledOnTouchOutside(false)

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

    fun joinEventDialog(
        context: Context,
        title: String,
        uid: String,
        bestMatchEvent: Event,
        userEmail: String
    ) {

        //Dismiss the previous dialog if it exists
        previousDialog?.dismiss()

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_joinevent)
        val text = dialog.findViewById<TextView>(R.id.tv_title)
        text.text = title
        val btnDeny = dialog.findViewById<Button>(R.id.btnDeny)
        val btnAccept = dialog.findViewById<Button>(R.id.btnAccept)
        btnDeny.setOnClickListener {
            declineEvent(context, uid, bestMatchEvent)
        }

        btnAccept.setOnClickListener {
            acceptEvent(context, uid, bestMatchEvent, userEmail)
        }

        // Set the current dialog as the previous dialog
        previousDialog = dialog

        dialog.show()
    }

    fun dismissDialog(context: Context) {
        //Dismiss the previous dialog if it exists
        previousDialog?.dismiss()
    }

    private fun acceptEvent(context: Context, uid: String, bestMatchEvent: Event, userEmail: String) {
        var dbrefJointEvent = FirebaseDatabase.getInstance().getReference("JointEvent")
        val jointUniqueId = generateUid()

        getDetails(userEmail) { volunteer ->
            if (volunteer != null) {
                // Volunteer found, do something with the details

                val reference = dbrefJointEvent.child(jointUniqueId)
                val jointEventData = mapOf(
                    "address" to bestMatchEvent.location.toString(),
                    "eventID" to bestMatchEvent.eventId.toString(),
                    "jointeventId" to jointUniqueId,
                    "jointeventName" to bestMatchEvent.eventName.toString(),
                    "phoneNo" to volunteer.contact.toString(),
                    "skillSet" to volunteer.skills.toString(),
                    "tryNia" to bestMatchEvent.tryNia.toString(),
                    "volunterEmail" to userEmail,
                    "volunterName" to volunteer.username.toString()
                )

                // Will also decline
                // To optimize the loading

                reference.setValue(jointEventData).addOnSuccessListener {
                    succsessDialogV2(context, "Joined Successfully!", uid, bestMatchEvent)

                }.addOnFailureListener {
                    errorDialog(context, "Oops, Fail to join!")
                }

            } else {
                // Volunteer not found
                errorDialog(context, "Database Error...")
            }
        }
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

                            // Refresh Activity
                            val intent = Intent(context, EventListingActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
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

                            // Refresh Activity
                            val intent = Intent(context, EventListingActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
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

    fun getDetails(email: String, callback: (Volunteer?) -> Unit) {
        val dbrefUser = FirebaseDatabase.getInstance().getReference("Volunteer")

        val query = dbrefUser.orderByChild("email").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming there's only one user with the provided email
                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(Volunteer::class.java)
                        callback.invoke(user)
                        return
                    }
                }
                callback.invoke(null)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                callback.invoke(null)
            }
        })
    }

    private fun generateUid(): String {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val randomPart = UUID.randomUUID().toString().substring(0, 8)

        return "$timestamp-$randomPart"
    }

    fun getEventDetails(eventId: String, callback: (Event?) -> Unit) {
        val dbrefUser = FirebaseDatabase.getInstance().getReference("Event")

        val query = dbrefUser.orderByChild("eventId").equalTo(eventId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming there's only one user with the provided email
                    for (userSnapshot in dataSnapshot.children) {
                        val eventDetails = userSnapshot.getValue(Event::class.java)
                        callback.invoke(eventDetails)
                        return
                    }
                }
                callback.invoke(null)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                callback.invoke(null)
            }
        })
    }

    fun getOrganizerDetails(organizerId: String, callback: (Organizer?) -> Unit) {
        val dbrefUser = FirebaseDatabase.getInstance().getReference("Organizer")

        val query = dbrefUser.orderByChild("orgId").equalTo(organizerId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming there's only one user with the provided email
                    for (userSnapshot in dataSnapshot.children) {
                        val orgDetails = userSnapshot.getValue(Organizer::class.java)
                        callback.invoke(orgDetails)
                        return
                    }
                }
                callback.invoke(null)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                callback.invoke(null)
            }
        })
    }


}