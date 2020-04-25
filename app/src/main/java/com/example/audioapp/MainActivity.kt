package com.example.audioapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Transformations.map
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    var buttonStart: Button? = null
    var buttonStop: Button? = null
    var buttonPlayLastRecordAudio: Button? = null
    var buttonStopPlayingRecording: Button? = null
    var AudioSavePathInDevice: String? = null
    var mediaRecorder: MediaRecorder? = null
    var random: Random? = null
    var RandomAudioFileName = "ABCDEFGHIJKLMNOP"
    var mediaPlayer: MediaPlayer? = null
   private var audio:MediaPlayer?=null

    private lateinit var mMap: GoogleMap

    @SuppressLint("WrongConstant", "ObjectAnimatorBinding", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        buttonStart = findViewById<View>(R.id.button) as Button
        buttonStop = findViewById<View>(R.id.button2) as Button
        buttonPlayLastRecordAudio = findViewById<View>(R.id.button3) as Button
        buttonStopPlayingRecording = findViewById<View>(R.id.button4) as Button
        buttonStop!!.isEnabled = false
        buttonPlayLastRecordAudio!!.isEnabled = false
        buttonStopPlayingRecording!!.isEnabled = false
        random = Random()


        audio= MediaPlayer.create(this,R.raw.onebeep)
        audio?.setOnPreparedListener{
            print("READY TO GO !")
        }

        buttonStart!!.setOnTouchListener{_, event ->
            handleTouch(event)
            true
            record()
        }


        buttonStop!!.setOnClickListener {
            mediaRecorder!!.stop()
            buttonStop!!.isEnabled = false
            buttonPlayLastRecordAudio!!.isEnabled = true
            buttonStart!!.isEnabled = true
            buttonStopPlayingRecording!!.isEnabled = false
            Toast.makeText(this@MainActivity, "Recording Completed", Toast.LENGTH_LONG).show()
        }
        buttonPlayLastRecordAudio!!.setOnClickListener {
            buttonStop!!.isEnabled = false
            buttonStart!!.isEnabled = false
            buttonStopPlayingRecording!!.isEnabled = true
            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer!!.setDataSource(AudioSavePathInDevice)
                mediaPlayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mediaPlayer!!.start()
            Toast.makeText(this@MainActivity, "Recording Playing", Toast.LENGTH_LONG).show()
        }
        buttonStopPlayingRecording!!.setOnClickListener {
            buttonStop!!.isEnabled = false
            buttonStart!!.isEnabled = true
            buttonStopPlayingRecording!!.isEnabled = false
            buttonPlayLastRecordAudio!!.isEnabled = true
            if (mediaPlayer != null) {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                MediaRecorderReady()
            }
        }


    }

    private fun handleTouch(event: MotionEvent) {
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                println("down")
                audio?.start()
                Toast.makeText(this,"BEEP...",Toast.LENGTH_LONG).show()
            }
            MotionEvent.ACTION_CANCEL,MotionEvent.ACTION_UP->{
                println("up or cancel")
                audio?.pause()
                audio?.seekTo(0)
            }
            else->{
                println("other")
            }
        }

    }
    private fun MediaRecorderReady() {
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        mediaRecorder!!.setOutputFile(AudioSavePathInDevice)
    }

    private fun CreateRandomAudioFileName(string: Int): String {
        val stringBuilder = StringBuilder(string)
        var i = 0
        while (i < string) {
            stringBuilder.append(RandomAudioFileName[random!!.nextInt(RandomAudioFileName.length)])
            i++
        }
        return stringBuilder.toString()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO), RequestPermissionCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RequestPermissionCode -> if (grantResults.isNotEmpty()) {
                val StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (StoragePermission && RecordPermission) {
                    Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val RequestPermissionCode = 1
    }

    private fun record(): Boolean {
        if (checkPermission()) {
            AudioSavePathInDevice =
                Environment.getExternalStorageDirectory().absolutePath + "/" + CreateRandomAudioFileName(
                    5
                ) + "AudioRecording.3gp"
            MediaRecorderReady()
            try {
                mediaRecorder!!.prepare()
                mediaRecorder!!.start()
            } catch (e: IllegalStateException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
            buttonStart!!.isEnabled = false
            buttonStop!!.isEnabled = true
            Toast.makeText(this@MainActivity, "Recording started", Toast.LENGTH_LONG).show()
        } else {
            requestPermission()
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val gu = LatLng(28.364753,77.539902)
        mMap.addMarker(MarkerOptions().position(gu).title("Marker in GreaterNoida"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gu,10F))
    }
}
