package com.orot.stt_demo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.orot.stt_demo.databinding.ActivityRecognitionBinding
import com.orot.stt_demo.model.EnrollRes
import com.orot.stt_demo.retrofit.ApiClient
import com.orot.stt_demo.retrofit.RetrofitInterface
import com.orot.stt_demo.utils.FileMultipart
import com.orot.stt_demo.utils.PcmRecorder
import com.orot.stt_demo.utils.coroutineScopeOnMain
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecognitionActivity : AppCompatActivity() {

    lateinit var binding: ActivityRecognitionBinding
    lateinit var pcmRecorder: PcmRecorder
    lateinit var retrofitInterface: RetrofitInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recognition)

        registerEnroll(isFirstCall = true)

        pcmRecorder = PcmRecorder().apply {
            createRecorder(this@RecognitionActivity)
        }

        /** 그룹 등록기능 */
        binding.groupRegister.setOnClickListener {
            if (binding.registeredGroup.text.isNullOrEmpty()) {
                if (binding.groupEtext.text.isNullOrEmpty()) {
                    Toast.makeText(this, "그룹을 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    binding.registeredGroup.text = binding.groupEtext.text
                    binding.groupEtext.setText("")
                }
            } else {
                Toast.makeText(this, "먼저 그룹을 삭제해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        /** 그룹 삭제기능 */
        binding.groupDelete.setOnClickListener {
            if (binding.registeredGroup.text.isNullOrEmpty()) {
                Toast.makeText(this, "먼저 그룹을 등록해주세요", Toast.LENGTH_SHORT).show()
            } else {
                binding.registeredGroup.text = ""
            }
        }

        /** 녹음 기능 */
        binding.record.setOnClickListener {
            if (binding.registeredGroup.text.isNullOrEmpty()) {
                Toast.makeText(this, "먼저 그룹을 등록해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pcmRecorder.mAudioRecord == null) {
                pcmRecorder.createRecorder(this)
            }
            pcmRecorder.start()
            binding.status = true
        }

        /** 중지 기능 */
        binding.stop.setOnClickListener {
            if (pcmRecorder.isRecording) {
                binding.status = false
                pcmRecorder.stop()
                registerEnroll(isFirstCall = false)
            } else {
                Toast.makeText(this, "현재는 녹음중이 아닙니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerEnroll(isFirstCall: Boolean) {
        coroutineScopeOnMain {
            if (!isFirstCall) {
                binding.loadingIsShow = true
            }
            val body = FileMultipart.getFileBody("file", pcmRecorder.getAudioFile())
            retrofitInterface = ApiClient.getMago52ApiClient().create(RetrofitInterface::class.java)
            val call: Call<EnrollRes> = retrofitInterface.enrollRun(
                group_id = binding.registeredGroup.text.toString(),
                file = body,
            )

            call.enqueue(object : Callback<EnrollRes> {
                override fun onResponse(call: Call<EnrollRes>, response: Response<EnrollRes>) {
                    if (!isFirstCall) {
                        binding.loadingIsShow = false
                        Log.w("ASDasd", "onResponse: ${response.body()}", )
                        try {
                            if (response.isSuccessful) {
                                response.body()?.let {
                                    when (it.code) {
                                        700 -> {
                                            Toast.makeText(
                                                this@RecognitionActivity,
                                                "녹음 성공",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            binding.enrollName = it.contents.recognition.speaker_id
                                            binding.enrollScore = it.contents.recognition.score.toString()
                                        }
                                        else -> {
                                            showAlertDialog(this@RecognitionActivity, it.message)
                                        }
                                    }
                                }
                            }

                            pcmRecorder.removeFileFromFilePath(listOf("wav", "zip"))
                        } catch (e: Exception) {
                            showAlertDialog(this@RecognitionActivity, e.message.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<EnrollRes>, t: Throwable) {
                    if (!isFirstCall) {
                        binding.loadingIsShow = false
                        showAlertDialog(this@RecognitionActivity, t.message.toString())
                    }
                }
            })
        }
    }

    private fun showAlertDialog(context: Context, msg: String) {
        AlertDialog.Builder(context).apply {
            setTitle("에러메세지")
            setMessage(msg)
            create()
            show()
        }
    }

}