package com.example.voicerecord

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var recording: Boolean = false
    private var playing: Boolean = false
    private var adapter: ArrayAdapter<String>? = null
    private var currentSong: String? = null
    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private lateinit var recordOutputDir: String
    private lateinit var errorOutputDir: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check for permissions and request if needed.
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, 0)
        } // if

        // Check if output dir exists and create it if it doesn't.
        recordOutputDir = getExternalFilesDir(null)?.absolutePath.plus("/recordings")
        if(!File(recordOutputDir).exists())
            File(recordOutputDir).mkdir()

        // Get error log output dir and create it if it doesn't exist.
        errorOutputDir = getExternalFilesDir(null)?.absolutePath.plus("/errorLogs")
        if(!File(errorOutputDir).exists())
            File(errorOutputDir).mkdir()

        // On-click listener for list of files.
        list.setOnItemClickListener { parent, view, position, id ->
            currentSong =
                recordOutputDir.plus("/").plus( adapter?.getItem(position))
            song_name.text = adapter?.getItem(position)
        } // setOnItemClickListener

        // On-click listener for record button.
        record_button.setOnClickListener {
            startRecording()
        } // record_button.setOnClickListener

        // On-click listener for stop recording button.
        stop_button.setOnClickListener {
            stopRecording()
        } // stop_button.setOnClickListener

        // On-click listener for play sound button.
        play_button.setOnClickListener {
            playSong()
        } // play_button.setOnClickListener

        // On-click listener for stop playing sound button.
        stop_playing_button.setOnClickListener {
            stopSong()
        } // stop_playing_button.setOnClickListener

        // On-click listener for send error logs button.
        send_logs_button.setOnClickListener {
            sendLogs()
        } // send_logs_button.setOnClickListener

        // On-touch listener for seek bar to disable interaction with it.
        seekBar.setOnTouchListener { v, event -> true }

        // Display the files from the directory in the GUI.
        updateList()
    } // onCreate

    // A function that gets called when the user makes a choice about accepting or denying the
    // permission requests. It is used to handle the case where the user has not accepted the
    // permissions required for the proper operation of the app.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0 && grantResults.isNotEmpty()
            && (grantResults[0] == PackageManager.PERMISSION_DENIED
                    || grantResults[1] == PackageManager.PERMISSION_DENIED
                    || grantResults[2] == PackageManager.PERMISSION_DENIED)
        ) {
            record_button.isEnabled = false
            stop_button.isEnabled = false
            stop_playing_button.isEnabled = false
            play_button.isEnabled = false
            Toast.makeText(
                this,
                "Insufficient permissions granted! Please restart the app and accept all permissions.",
                Toast.LENGTH_SHORT
            ).show()
        } // if
    } // onRequestPermissionsResult

    // Event listener function for start playing sound button.
    private fun playSong() {
        // Check if app is already playing sound.
        if (playing)
            Toast.makeText(this, "You are already playing a sound!", Toast.LENGTH_SHORT).show()
        // Check if file is selected for playback.
        else if (currentSong == null)
            Toast.makeText(this, "You haven't selected a file to play!", Toast.LENGTH_SHORT).show()
        else try {
            // Open file into app.
            val fis = FileInputStream(File(currentSong))

            // Set up media player.
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(fis.fd)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { stopSong() }

            playing = true

            initialiseSeekBar()
        } // try
        // Catch file I/O exceptions.
        catch (e: IOException) {
            e.printStackTrace()
            logError(e)
        } // catch
        // Catch media player prepare exceptions.
        catch (e: IllegalStateException) {
            e.printStackTrace()
            logError(e)
        } // catch
        // Catch media player media source exceptions.
        catch (e: IllegalArgumentException) {
            e.printStackTrace()
            logError(e)
        } // catch
    } // playSong

    // Event listener function for stop playing sound button.
    private fun stopSong() {
        // Check if app is playing sound.
        if (playing) {
            // Set seek bar to max value.
            seekBar.progress = mediaPlayer?.duration!!

            // Stop media player/
            mediaPlayer?.stop()
            mediaPlayer?.release()

            // Remove callbacks for seek bar updates.
            handler.removeCallbacks(runnable)

            playing = false
        } // if
        else Toast.makeText(this, "You aren't playing a sound!", Toast.LENGTH_SHORT).show()
    } // stopSong

    // A function that initialises the seek bar and sets up callbacks to update it.
    private fun initialiseSeekBar() {
        seekBar.max = mediaPlayer?.duration!!
        seekBar.progress = mediaPlayer?.currentPosition!!

        // Create the callback function to update seek bar.
        runnable = Runnable {
            seekBar.progress = mediaPlayer?.currentPosition!!

            // Keep calling the function until cancelled.
            handler.postDelayed(runnable, 500)
        }

        // Start the callback chain.
        handler.postDelayed(runnable, 500)
    } // initialiseSeekBar

    // A function that can be called to update the list.
    private fun updateList() {
        try {
            // Get the filenames of the desired directory as a list.
            val filesList = File((getExternalFilesDir(null)?.absolutePath.plus("/recordings"))).list()
            // Put the list into the adapter so it could be listed in the GUI.
            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filesList)
            // Add adapter to the list.
            list.adapter = adapter
        } // try
        // Catch exceptions when trying to list directory
        catch (e: IOException) {
            e.printStackTrace()
            logError(e)
        } // catch
    } // updateList

    // Event listener function for start recording sound button.
    private fun startRecording() {
        if (recording) {
            Toast.makeText(this, "You are already recording!", Toast.LENGTH_SHORT).show()
        } else try {
            //Get current time and date in proper format.
            val time = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Date())

            // Set output path.
            val output = "$recordOutputDir/Recording_$time.mp3"

            // Set up the media recorder.
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(output)
            } // mediaRecorder

            // Start recording.
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            recording = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } // try
        // Catch the media player exceptions.
        catch (e: IllegalStateException) {
            e.printStackTrace()
            logError(e)
        } // catch
        // Catch the file directory listing exceptions.
        catch (e: IOException) {
            e.printStackTrace()
            logError(e)
        } // catch
        catch (e: Exception){
            e.printStackTrace()
        }
    } // startRecording

    // Event listener function for stop recording sound button.
    private fun stopRecording() {
        // Check if recording wasn't already started.
        if (recording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            recording = false
            Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT).show()
            updateList()
        } // if
        else Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
    } // startRecording

    // A function that saves a thrown exception's stack trace to a log file.
    private fun logError(e: Exception)
    {
        try{
            // Get current date and time to be used as file name.
            val time = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(Date())

            // Create a print writer to write into the log file.
            val output = File("$errorOutputDir/$time.log").printWriter()

            // Save stack trace to log file and close stream.
            e.printStackTrace(output)
            output.close()
        } // try
        catch (e: Exception){
            e.printStackTrace()
        } // catch
    } // logError

    // A function that emails all error logs to an email address.
    private fun sendLogs(){
        val filesList = File(errorOutputDir).listFiles()

        // If there are no logs, notify the user.
        if (filesList.count() == 0)
            Toast.makeText(this, "There are no logs to send!", Toast.LENGTH_SHORT).show()
        else {
            // ACTION_SEND action to launch an email client installed on the Android device.
            val mIntent = Intent(Intent.ACTION_SEND)

            // To send an email you need to specify mailto: as URI using setData() method
            // and data type will be to text/plain using setType() method
            mIntent.data = Uri.parse("mailto:")
            mIntent.type = "text/plain"

            // Put recipient email address and email subject in intent.
            mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("partinov@gmail.com"))
            mIntent.putExtra(Intent.EXTRA_SUBJECT, "VoiceRecord App Error Report")

            // Save all logs to a single string as an email body.
            var emailBody = ""
            filesList.forEach { emailBody = emailBody.plus(it.readText()).plus("\n") }

            //put the message in the intent
            mIntent.putExtra(Intent.EXTRA_TEXT, emailBody)

            // Attempt to start the activity and send the email
            try {
                startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
            } // try
            // Catch any exceptions that might arise.
            catch (e: Exception){
                logError(e)
                e.printStackTrace()
            } // catch
        } // else
    } // sendLogs

} // MainActivity
