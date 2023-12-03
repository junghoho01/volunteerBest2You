package com.example.best2help

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.database.ValueEventListener
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

    private fun updateBarChart(eventDetailsList: List<EventDetails>) {
        val entries = eventDetailsList.mapIndexed { index, eventDetails ->
            BarEntry(index.toFloat(), eventDetails.volunteerCount.toFloat())
        }

        val dataSet = BarDataSet(entries, "Volunteer Counts")
        dataSet.colors = getColors(eventDetailsList.size)

        val labels = eventDetailsList.map { it.eventName }
        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f

        val data = BarData(dataSet)
        data.barWidth = 0.6f // Adjust bar width for better separation

        val barChart = binding.barChart
        barChart.data = data
        barChart.invalidate()

        // Customize chart appearance
        barChart.description.isEnabled = false // Disable description
        barChart.legend.isEnabled = false // Disable legend
        barChart.axisRight.isEnabled = false // Disable right axis

        // Customize X-axis appearance
        xAxis.textSize = 6f
        xAxis.labelRotationAngle = -45f // Rotate labels for better readability
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        // Customize Y-axis appearance
        val yAxis = barChart.axisLeft
        yAxis.textSize = 12f
    }

    private fun getColors(size: Int): List<Int> {
        // Using a predefined color template
        return ColorTemplate.MATERIAL_COLORS.toList().take(size)
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    private fun exportChartToPdfAndShare() {
        try {
            val externalFilesDir = getExternalFilesDir(null)
            val pdfFile = File(externalFilesDir, "chart_report.pdf")

            pdfFile.outputStream().use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdf = PdfDocument(writer)
                val document = Document(pdf)

                // Add a title to the document
                val title = Paragraph("Volunteer Counts Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(30f)
                    .setMarginTop(300f)
                document.add(title)

                // Add a subtitle with the current date
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val subtitle = Paragraph("Report generated on: $currentDate")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20f)
                    .setMarginBottom(20f)
                document.add(subtitle)

                val chartBitmap = binding.barChart.getChartBitmap()
                val stream = ByteArrayOutputStream()
                chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()

                val imageData = ImageDataFactory.create(byteArray)
                val chartImage = Image(imageData)

                val pageSize = PageSize.A5
                val scaleFactor = pageSize.width / chartImage.imageScaledWidth
                chartImage.scaleToFit(pageSize.width, chartImage.imageScaledHeight * scaleFactor)
                chartImage.setMargins(0f, 0f, 50f, 0f)

                document.add(chartImage)

                val xAxisLabels =
                    (binding.barChart.xAxis.valueFormatter as? IndexAxisValueFormatter)?.let { formatter ->
                        (0 until formatter.values.size).map { formatter.getFormattedValue(it.toFloat()) }
                    } ?: emptyList<String>()

                val xLabelsTitle = Paragraph("X-Axis Labels in Sequence:")
                    .setFontSize(15f)
                    .setMarginBottom(10f)

                val xLabelsTable = Table(1).apply {
                    setFontSize(15f)
                    setMarginBottom(10f)

                    // Populate the table with numbered labels
                    xAxisLabels.forEachIndexed { index, label ->
                        addCell(Cell().add(Paragraph("${index + 1}. $label")))
                    }
                }

                document.add(xLabelsTitle)
                document.add(xLabelsTable)


                document.close()

                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "application/pdf"
                val uri = FileProvider.getUriForFile(
                    this,
                    "com.example.best2help.fileprovider",
                    pdfFile
                )
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(shareIntent, "Share PDF"))
            }
        } catch (e: Exception) {
            Log.e("PDF_EXPORT", "Error exporting PDF: ${e.message}")
            e.printStackTrace()
            showToast("Error exporting PDF. Check logs for details.")
        }
    }

}