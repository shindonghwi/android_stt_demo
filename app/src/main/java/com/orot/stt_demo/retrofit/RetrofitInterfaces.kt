package com.orot.stt_demo.retrofit

import com.google.gson.JsonObject
import com.orot.stt_demo.model.EnrollRes
import com.orot.stt_demo.model.SttRes
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitInterface {

    @Multipart
    @POST("spkrec/enroll")
    fun enrollRegister(
        @Query("group_id") group_id: String,
        @Query("speaker_id") speaker_id: String,
        @Query("text_dependent") text_dependent: Boolean,
        @Part file: MultipartBody.Part
    ): Call<EnrollRes>

    @Multipart
    @POST("spkrec/run")
    fun enrollRun(
        @Query("group_id") group_id: String,
        @Part file: MultipartBody.Part
    ): Call<EnrollRes>

    @Multipart
    @POST("speech2text/upload")
    fun uploadSttFile(
        @Part speech: MultipartBody.Part
    ): Call<SttRes>

    @POST("speech2text/batch/{id}")
    fun checkBatchUploadSttFile(
        @Path("id") id: String,
        @Body requestBody: JsonObject,
        @Header("accept") acceptHeader: String = "application/json",
        @Header("Content-Type") contentTypeHeader: String = "application/json",
    ): Call<SttRes>

    @GET("speech2text/result/{id}?result_type=json")
    fun resultBatchUploadSttFile(
        @Path("id") id: String,
    ): Call<SttRes>


}
