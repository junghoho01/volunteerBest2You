package com.example.best2help

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.w3c.dom.Text
import java.time.LocalDate

class EventListJoinedAdapter (private val eventList : ArrayList<JointEvent>) : RecyclerView.Adapter<EventListJoinedAdapter.MyViewHolder>(){

//    private lateinit var mListener : onItemClickListener
//
//    interface onItemClickListener {
//        fun onItemClick(position: Int)
//    }
//
//    fun setOnItemClickListener(listener: onItemClickListener) {
//        mListener = listener
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.joined_event_cardlist,parent, false)
        return MyViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val eventName : JointEvent = eventList[position]
        val currentDate = LocalDate.now()
        holder.title.text = eventName.jointeventName.toString()

        //GetEventDetail
        DialogUtils.getEventDetails(eventName.eventID.toString()) { event ->
            if (event != null) {
                // Volunteer found, do something with the details
                    holder.eventDate.text = event.eventStartDate
                    Picasso.get().load(event.eventPicName).into(holder.eventPic)

                if (event.eventStartDate != null && event.eventStartDate < currentDate.toString()) {
                    // eventStartDate is in the past
                    holder.eventStatus.text = "Completed"
                } else {
                    // eventStartDate is in the future or today
                    holder.eventStatus.text = "Ongoing"
                }

            } else {
                // Volunteer not found
            }
        }

//        holder.eventDate.text = eventName.eventStartDate // Need to retrieve ...
//        Picasso.get().load(eventName.eventPicName).into(holder.eventPic)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val title : TextView = itemView.findViewById(R.id.tv_eventTitle)
        val eventDate : TextView = itemView.findViewById(R.id.tv_eventDate)
        val eventPic : ImageView = itemView.findViewById(R.id.img_event)
        val eventStatus : TextView = itemView.findViewById(R.id.tv_eventStatus)

//        init {
//            itemView.setOnClickListener {
//                listener.onItemClick(adapterPosition)
//            }
//        }
    }

}