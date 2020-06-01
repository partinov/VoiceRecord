package com.example

import android.content.pm.PackageManager
import com.example.voicerecord.MainActivity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MainActivityTest {

    private val m = MainActivity()

    @org.junit.jupiter.api.Test
    fun recordAudioPermissionGrantedTest() {
        assertEquals(m.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO), PackageManager.PERMISSION_GRANTED)
    } // recordAudioPermissionGrantedTest

    @org.junit.jupiter.api.Test
    fun readExternalStoragePermissionGrantedTest() {
        assertEquals(m.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)
    } // readExternalStoragePermissionGrantedTest

    @org.junit.jupiter.api.Test
    fun writeExternalStoragePermissionGrantedTest() {
        assertEquals(m.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)
    } // writeExternalStoragePermissionGrantedTest


    //@org.junit.jupiter.api.Test
    //fun onRequestPermissionsResultTest() {
    //
    //}

    // GUI TESTING NEEDS TO BE DONE

} //