package com.orot.stt_demo.model

data class SttRes(
    val code: Int,
    val message: String,
    val contents:SttContents
)


data class SttContents(
    val id: String,
    val detail: String,
    val results: SttResults
)


data class SttResults(
    val utterances: List<SttUtterances>,
)

data class SttUtterances(
    val text: String,
)
