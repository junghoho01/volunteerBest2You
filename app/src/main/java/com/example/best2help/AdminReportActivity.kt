package com.example.best2help

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class AdminReportActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdminReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        var barChart = binding.barChart
//
//        // Example data for the chart
//        val entries = listOf(
//            BarEntry(1f, 50f),
//            BarEntry(2f, 80f),
//            BarEntry(3f, 60f),
//            BarEntry(4f, 60f),
//            BarEntry(5f, 60f),
//            BarEntry(6f, 60f),
//            BarEntry(7f, 60f),
//            BarEntry(8f, 60f),
//            // Add more entries as needed
//        )
//
//        val dataSet = BarDataSet(entries, "Sample Data")
//        val data = BarData(dataSet)
//
//        barChart.data = data
//        barChart.invalidate() // Refresh the chart

        fetchVolunteerCounts()
    }

    data class EventDetails(
        val eventName: String,
        val volunteerCount: Int
    )

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
        // Example data for the chart
        val entries = eventDetailsList.mapIndexed { index, eventDetails ->
            BarEntry((index + 1).toFloat(), eventDetails.volunteerCount.toFloat())
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
}