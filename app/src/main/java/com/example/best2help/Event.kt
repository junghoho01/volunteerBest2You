package com.example.best2help

data class Event(
    val eventName: String ?= null,
    val eventPicName: String? = null,
    val eventId: String? = null,
    val eventApproval: String? = null,
    val skillSet: String? = null,
    val location: String? = null,
    val desc: String? = null,
    val eventStartDate: String? = null,
    val eventStartTime: String? = null,
    val eventStatus: String? = null,
)