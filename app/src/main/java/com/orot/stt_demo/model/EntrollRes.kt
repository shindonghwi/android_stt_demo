package com.orot.stt_demo.model

data class EnrollRes(
    val code: Int,
    val message: String,
    val contents:ResContents
)


data class ResContents(
    val enrollment: ResEnrollment,
    val recognition: ResRecognition,
    val notice: String?
)

data class ResEnrollment(
    val group_id: String,
    val speaker_id: String,
    val uuid: String,
    val audio_path: String,
    val text_dependent: Boolean
)

data class ResRecognition(
    val speaker_id: String,
    val score: Float
)