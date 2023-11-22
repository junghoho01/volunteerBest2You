package com.example.best2help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val eventName : JointEvent = eventList[position]
        holder.title.text = eventName.jointeventName.toString()

        //GetEventDetail
        DialogUtils.getEventDetails(eventName.eventID.toString()) { event ->
            if (event != null) {
                // Volunteer found, do something with the details
                    holder.eventDate.text = event.eventStartDate
                    Picasso.get().load(event.eventPicName).into(holder.eventPic)

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

//        init {
//            itemView.setOnClickListener {
//                listener.onItemClick(adapterPosition)
//            }
//        }
    }

}