package com.example.voicerecord

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var output: String? = ""
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        record_button.setOnClickListener {
            // Check for permissions.
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // If permissions aren't granted, request them.
                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions,0)
            } // if
            else {
                startRecording()
            } // else
        } // record_button.setOnClickListener

        stop_button.setOnClickListener {
            stopRecording()
        }
    } // onCreate

    private fun startRecording() {
        // Set output path.
        output = (getExternalFilesDir(null)?.absolutePath ?: null) + "/recording.mp3"

        // Set up media recorder.
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(output)
        } // mediaRecorder

        // Start recording.
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } // try
        catch (e: IllegalStateException) {
            e.printStackTrace()
        } // catch
        catch (e: IOException) {
            e.printStackTrace()
        } // catch
    } // startRecording

    private fun stopRecording() {
        if(state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT).show()
        } // if
        else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        } // else
    } // startRecording



} // MainActivity
