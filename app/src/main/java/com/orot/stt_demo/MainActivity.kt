package com.orot.stt_demo

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.orot.stt_demo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        requestPermissions()

        /** 화자 등록 */
        binding.register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        /** 화자 인식 */
        binding.recognition.setOnClickListener {
            startActivity(Intent(this, RecognitionActivity::class.java))
        }

        /** 음성 인식 */
        binding.stt.setOnClickListener {
            startActivity(Intent(this, STTActivity::class.java))
        }
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE),
            1000
        )
    }
}