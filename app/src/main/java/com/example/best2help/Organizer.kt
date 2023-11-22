package com.example.best2help

data class Organizer(
    val activationCode: String ?= null,
    val orgId: String ?= null,
    val organizerCfmPassword: String ?= null,
    val organizerEmail: String ?= null,
    val organizerName: String ?= null,
    val organizerPassword: String ?= null,
    val phoneNo: String ?= null,
    val profPicName: String ?= null,
    val verifyStatus: Boolean ?= null,
)
