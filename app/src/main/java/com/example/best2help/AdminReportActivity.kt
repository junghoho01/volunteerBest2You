package com.example.best2help

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.best2help.databinding.ActivityAdminReportBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.FirebaseDatabase.getInstance
import com.google.firebase.database.ValueEventListener
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.Policy.getInstance
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.getInstance
import java.util.Currency.getInstance

class AdminReportActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdminReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchVolunteerCounts()

        binding.exportToPdfButton.setOnClickListener {
            exportChartToPdfAndShare()
        }
    }

    data class EventDetails(
        val eventName: String,
        val volunteerCount: Int
    )

    // Get from realtime database
    private fun fetchVolunteerCounts() {
        val eventsRef = FirebaseDatabase.getInstance().getReference("JointEvent")

        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val eventDetailsMap = mutableMapOf<String, Int>()

                    for (eventSnapshot in snapshot.children) {
                        val eventName = eventSnapshot.child("jointeventName").getValue(String::class.java)

                        if (eventName != null) {
                            // Increment the count for each event
                            eventDetailsMap[eventName] = (eventDetailsMap[eventName] ?: 0) + 1
                        }
                    }

                    // Convert the map to a list of EventDetails
                    val eventDetailsList = eventDetailsMap.entries.map { (eventName, volunteerCount) ->
                        EventDetails(eventName, volunteerCount)
                    }

                    // Update the bar chart with the fetched data
                    updateBarChart(eventDetailsList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }

    // Update the data to chart
    private fun updateBarChart(eventDetailsList: List<EventDetails>) {
        // Example data for the chart
        val entries = eventDetailsList.mapIndexed { index, eventDetails ->
            BarEntry(index.toFloat(), eventDetails.volunteerCount.toFloat())
        }

        val dataSet = BarDataSet(entries, "Volunteer Counts")

        // Customize colors for each bar
        dataSet.colors = getColors(eventDetailsList.size)

        // Set labels for the x-axis (event names)
        val labels = eventDetailsList.map { it.eventName }
        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f

        val data = BarData(dataSet)

        val barChart = binding.barChart
        barChart.data = data
        barChart.invalidate() // Refresh the chart
    }

    private fun getColors(size: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val colorTemplate = ColorTemplate.MATERIAL_COLORS

        for (i in 0 until size) {
            colors.add(colorTemplate[i % colorTemplate.size])
        }

        return colors
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

//    private fun exportChartToPdfAndShare() {
//
//        try {
//
//            // Get the external files directory
//            val externalFilesDir = getExternalFilesDir(null)
//
//            // Create a file in the external files directory
//            val pdfFile = File(externalFilesDir, "chart_report.pdf")
//
//            // Initialize PdfWriter and PdfDocument
//            val writer = PdfWriter(pdfFile)
//            val pdf = PdfDocument(writer)
//            val document = Document(pdf)
//
//            // Convert the BarChart to a bitmap image
//            val chartBitmap = binding.barChart.getChartBitmap()
//
//            // Convert the bitmap to a byte array
//            val stream = ByteArrayOutputStream()
//            chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//            val byteArray = stream.toByteArray()
//
//            // Create an ImageData instance from the byte array
//            val imageData = ImageDataFactory.create(byteArray)
//
//            // Create an Image instance from the ImageData
//            val chartImage = Image(imageData)
//
//            // Add the chart image to the PDF document
//            document.add(chartImage)
//
//            // Close the document
//            document.close()
//
//            // Share the PDF file using ACTION_SEND
//            val shareIntent = Intent(Intent.ACTION_SEND)
//            shareIntent.type = "application/pdf"
//            val uri = FileProvider.getUriForFile(
//                this,
//                "com.example.best2help.fileprovider",
//                pdfFile
//            )
//            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
//            startActivity(Intent.createChooser(shareIntent, "Share PDF"))
//
//            Log.d("PDF_EXPORT", "PDF file path: ${pdfFile.absolutePath}")
//
//
//        } catch (e: Exception) {
//            // Log additional information for debugging
//            Log.e("PDF_EXPORT", "Error exporting PDF: ${e.message}")
//            e.printStackTrace()
//
//            // Display a toast message with a generic error
//            showToast("Error exporting PDF. Check logs for details.")
//        }
//
//    }

//    private fun exportChartToPdfAndShare() {
//        try {
//
//            // Get the external files directory
//            val externalFilesDir = getExternalFilesDir(null)
//
//            // Create a file in the external files directory
//            val pdfFile = File(externalFilesDir, "chart_report.pdf")
//
//            // Initialize PdfWriter and PdfDocument
//            val writer = PdfWriter(pdfFile)
//            val pdf = PdfDocument(writer)
//            val document = Document(pdf)
//
//            // Add a title to the document
//            val title = Paragraph("Volunteer Counts Report")
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(18f)
//            document.add(title)
//
//            // Add a subtitle with the current date
//            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//            val subtitle = Paragraph("Report generated on: $currentDate")
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(14f)
//                .setMarginBottom(20f)
//            document.add(subtitle)
//
//            // Convert the BarChart to a bitmap image
//            val chartBitmap = binding.barChart.getChartBitmap()
//
//            // Convert the bitmap to a byte array
//            val stream = ByteArrayOutputStream()
//            chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//            val byteArray = stream.toByteArray()
//
//            // Create an ImageData instance from the byte array
//            val imageData = ImageDataFactory.create(byteArray)
//
//            // Create an Image instance from the ImageData
//            val chartImage = Image(imageData)
//
//            // Add the chart image to the PDF document
//            document.add(chartImage)
//
//            // Close the document
//            document.close()
//
//            // Share the PDF file using ACTION_SEND
//            val shareIntent = Intent(Intent.ACTION_SEND)
//            shareIntent.type = "application/pdf"
//            val uri = FileProvider.getUriForFile(
//                this,
//                "com.example.best2help.fileprovider",
//                pdfFile
//            )
//            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
//            startActivity(Intent.createChooser(shareIntent, "Share PDF"))
//
//        } catch (e: Exception) {
//            // Log additional information for debugging
//            Log.e("PDF_EXPORT", "Error exporting PDF: ${e.message}")
//            e.printStackTrace()
//
//            // Display a toast message with a generic error
//            showToast("Error exporting PDF. Check logs for details.")
//        }
//    }

    private fun exportChartToPdfAndShare() {
        try {

            // Get the external files directory
            val externalFilesDir = getExternalFilesDir(null)

            // Create a file in the external files directory
            val pdfFile = File(externalFilesDir, "chart_report.pdf")

            // Initialize PdfWriter and PdfDocument
            val writer = PdfWriter(pdfFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Add a title to the document
            val title = Paragraph("Volunteer Counts Report")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f)
            document.add(title)

            // Add a subtitle with the current date
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val subtitle = Paragraph("Report generated on: $currentDate")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14f)
                .setMarginBottom(20f)
            document.add(subtitle)

            // Convert the BarChart to a bitmap image
            val chartBitmap = binding.barChart.getChartBitmap()

            // Convert the bitmap to a byte array
            val stream = ByteArrayOutputStream()
            chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            // Create an ImageData instance from the byte array
            val imageData = ImageDataFactory.create(byteArray)

            // Create an Image instance from the ImageData
            val chartImage = Image(imageData)

            // Adjust chart image size to fit the page
            val pageSize = PageSize.A5
            val scaleFactor = pageSize.width / chartImage.imageScaledWidth
            chartImage.scaleToFit(pageSize.width, chartImage.imageScaledHeight * scaleFactor)

            // Add the chart image to the PDF document
            document.add(chartImage)

            // Add X-axis labels under the chart
            val xAxisLabels = (binding.barChart.xAxis.valueFormatter as? IndexAxisValueFormatter)?.let { formatter ->
                (0 until formatter.values.size).map { formatter.getFormattedValue(it.toFloat()) }
            } ?: emptyList<String>()

            val xLabelsParagraph = Paragraph("Event Labels: ${xAxisLabels.joinToString(", ")}")
                .setTextAlignment(TextAlignment.LEFT)
                .setFontSize(12f)
                .setMarginBottom(10f)
            document.add(xLabelsParagraph)

            // Close the document
            document.close()

            // Share the PDF file using ACTION_SEND
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/pdf"
            val uri = FileProvider.getUriForFile(
                this,
                "com.example.best2help.fileprovider",
                pdfFile
            )
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(shareIntent, "Share PDF"))

        } catch (e: Exception) {
            // Log additional information for debugging
            Log.e("PDF_EXPORT", "Error exporting PDF: ${e.message}")
            e.printStackTrace()

            // Display a toast message with a generic error
            showToast("Error exporting PDF. Check logs for details.")
        }
    }

}