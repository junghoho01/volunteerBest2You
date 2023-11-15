package com.example.best2help

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.TextView

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

}