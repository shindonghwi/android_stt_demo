package com.orot.stt_demo

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import com.orot.stt_demo.databinding.ActivitySttBinding
import com.orot.stt_demo.model.SttRes
import com.orot.stt_demo.retrofit.ApiClient
import com.orot.stt_demo.retrofit.RetrofitInterface
import com.orot.stt_demo.utils.FileMultipart
import com.orot.stt_demo.utils.PcmRecorder
import com.orot.stt_demo.utils.coroutineScopeOnMain
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class STTActivity : AppCompatActivity() {

    lateinit var binding: ActivitySttBinding
    lateinit var retrofitInterface: RetrofitInterface
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var countDownTimer: CountDownTimer? = null
    var pcmRecorder: PcmRecorder = PcmRecorder()

    val maxTimerMinute = 5
    val maxTimerMinuteText = "05:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView<ActivitySttBinding?>(this, R.layout.activity_stt).apply {
                resultText.movementMethod = ScrollingMovementMethod() // 분석결과 텍스트 스크롤링 설정
                isRecording = false
                timerText = maxTimerMinuteText
            }

        binding.record.setOnClickListener {
            if (binding.isRecording == false) {
                if (pcmRecorder.mAudioRecord == null) {
                    pcmRecorder.createRecorder(this)
                }
                pcmRecorder.start()
                startCountUpTimer()
                Toast.makeText(this, "녹음을 시작합니다", Toast.LENGTH_SHORT).show()
                binding.buttonText = "녹음 중지"
            } else {
                pcmRecorder.stop()
                uploadRecordedFile()
                stopCountDownTimer()
            }
            binding.isRecording = binding.isRecording != true
        }
    }


    private fun uploadRecordedFile() {
        coroutineScopeOnMain {
            binding.apply {
                loadingIsShow = true
                buttonText = "음성 분석중..."
            }

            val body = FileMultipart.getFileBody("speech", pcmRecorder.getAudioFile())
            retrofitInterface = ApiClient.getSttApiClient().create(RetrofitInterface::class.java)
            val call: Call<SttRes> = retrofitInterface.uploadSttFile(body)

            call.enqueue(object : Callback<SttRes> {
                override fun onResponse(call: Call<SttRes>, response: Response<SttRes>) {
                    try {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                when (it.code) {
                                    701 -> {
                                        binding.result = "음성을 분석중입니다..."
                                        startBatchCheckApi(it.contents.id)
                                        pcmRecorder.removeFileFromFilePath(listOf("wav", "zip"))
                                    }
                                    else -> {
                                        showAlertDialog(this@STTActivity, it.message)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        showAlertDialog(this@STTActivity, e.message.toString())
                    }
                }

                override fun onFailure(call: Call<SttRes>, t: Throwable) {
                    uploadRecordedFile()
//                    showAlertDialog(this@STTActivity, t.message.toString())
                }
            })
        }
    }

    fun startBatchCheckApi(id: String) {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                checkBatchApi(id)
                handler?.postDelayed(this, 1000) // Call every second (1000 milliseconds)
            }
        }
        handler?.post(runnable!!)
    }

    fun stopBatchApi() {
        runnable?.let { handler?.removeCallbacks(it) }
        handler = null
        runnable = null
    }

    private fun checkBatchApi(id: String) {
        val requestBody = JsonObject().apply {
            addProperty("lang", "ko")
        }
        retrofitInterface = ApiClient.getSttApiClient().create(RetrofitInterface::class.java)
        val call: Call<SttRes> = retrofitInterface.checkBatchUploadSttFile(id, requestBody)

        call.enqueue(object : Callback<SttRes> {
            override fun onResponse(call: Call<SttRes>, response: Response<SttRes>) {
                try {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            when (it.code) {
                                711 -> {
                                    stopBatchApi()
                                    resultBatchApi(it.contents.id)
                                }
                                712 -> {
//                                    binding.result = "음성을 백그라운드에서 처리하고있습니다..."
                                }
                                else -> {
                                    showAlertDialog(this@STTActivity, it.message)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    showAlertDialog(this@STTActivity, e.message.toString())
                }
            }

            override fun onFailure(call: Call<SttRes>, t: Throwable) {
                showAlertDialog(this@STTActivity, t.message.toString())
            }
        })
    }

    private fun resultBatchApi(id: String) {
        retrofitInterface = ApiClient.getSttApiClient().create(RetrofitInterface::class.java)
        val call: Call<SttRes> = retrofitInterface.resultBatchUploadSttFile(id)

        call.enqueue(object : Callback<SttRes> {
            override fun onResponse(call: Call<SttRes>, response: Response<SttRes>) {
                try {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            when (it.code) {
                                700 -> {
                                    var result = ""
                                    it.contents.results.utterances.map { result += it.text }
                                    if (result.isEmpty()) {
                                        binding.result = "분석된 결과가 없습니다"
                                    } else {
                                        binding.result = result
                                    }
                                    Toast.makeText(
                                        applicationContext,
                                        "분석이 완료되었습니다",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.buttonText = "녹음 시작하기"
                                    binding.loadingIsShow = false
                                    stopCountDownTimer()
                                }
                                else -> {
                                    showAlertDialog(this@STTActivity, it.message)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    showAlertDialog(this@STTActivity, e.message.toString())
                }
            }

            override fun onFailure(call: Call<SttRes>, t: Throwable) {
                showAlertDialog(this@STTActivity, t.message.toString())
            }
        })
    }

    private fun showAlertDialog(context: Context, msg: String) {
        binding.loadingIsShow = false
        binding.buttonText = "녹음 시작하기"
        stopBatchApi()
        AlertDialog.Builder(context).apply {
            setTitle("에러 메세지")
            setMessage(msg)
            create()
            show()
        }
    }

    private fun startCountUpTimer() {
        val totalMinutes = maxTimerMinute
        val totalTimeInMillis = totalMinutes * 60 * 1000L

        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                binding.timerText = formattedTime
            }

            override fun onFinish() {
                binding.record.performClick()
            }
        }
        countDownTimer?.start()
    }

    private fun stopCountDownTimer() {
        binding.timerText = maxTimerMinuteText
        countDownTimer?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCountDownTimer()
    }

}