package com.example.best2help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class EventAdapter(private val eventList : ArrayList<Event>) : RecyclerView.Adapter<EventAdapter.MyViewHolder>(){

    private lateinit var mListener : onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.event_list,parent, false)
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: EventAdapter.MyViewHolder, position: Int) {
        val eventName : Event = eventList[position]
        holder.title.text = eventName.eventName
        Picasso.get().load(eventName.eventPicName).into(holder.eventPic)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val title : TextView = itemView.findViewById(R.id.tv_eventTitle)
        val eventPic : ImageView = itemView.findViewById(R.id.img_event)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

    }
}