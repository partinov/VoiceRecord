package com.example.voicerecord

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.widget.Adapter
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var recording: Boolean = false
    private var adapter: ArrayAdapter<String>? = null

    @RequiresApi(Build.VERSION_CODES.O)
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
        } // stop_button.setOnClickListener

        list.setOnItemClickListener { parent, view, position, id ->
            val element = adapter?.getItem(position)


        }

        // Display the files from the directory in the GUI.
        updateList()
    } // onCreate

    // A function that can be called to update the list.
    private fun updateList(){
        // Get the filenames of the desired directory as a list.
        val filesList = File((getExternalFilesDir(null)?.absolutePath ?: null)).list()
        // Put the list into the adapter so it could be listed in the GUI.
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filesList)
        // Add adapter to the list.
        list.adapter = adapter
    } // updateList

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording() {
        //Get current time and date in proper format and output dir.
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss_dd-MM-yyyy"))
        val outputDir = getExternalFilesDir(null)?.absolutePath ?: null

        // Set output path.
        val output = "$outputDir/Recording_$time.mp3"

        // Set up the media recorder.
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
            recording = true
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
        // Check if recording was actually started.
        if(recording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            recording = false
            Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT).show()
            updateList()
        } // if
        else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        } // else
    } // startRecording

} // MainActivity
