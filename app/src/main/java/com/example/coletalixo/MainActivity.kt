
package com.example.coletalixo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null

    private val trashList = mutableListOf<String>()
    private lateinit var trashAdapter: TrashAdapter
    private lateinit var tvCount: TextView
    private lateinit var imgPhoto: ImageView

    private var lastUpdateTime = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    // Formatador de data/hora
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                imgPhoto.setImageBitmap(imageBitmap)

                val timestamp = dateFormatter.format(Date())

                trashList.add("📸 Foto tirada — $timestamp")
                trashAdapter.notifyDataSetChanged()
                tvCount.text = "Total: ${trashList.size}"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCount = findViewById(R.id.tvCount)
        imgPhoto = findViewById(R.id.imgPhoto)

        // RecyclerView
        val rv: RecyclerView = findViewById(R.id.rvTrash)
        rv.layoutManager = LinearLayoutManager(this)
        trashAdapter = TrashAdapter(trashList)
        rv.adapter = trashAdapter

        // Botão Reset
        val btnReset = findViewById<Button>(R.id.btnReset)
        btnReset.setOnClickListener {
            trashList.clear()
            trashAdapter.notifyDataSetChanged()

            tvCount.text = "Total: 0"
            Toast.makeText(this, "Informações zeradas!", Toast.LENGTH_SHORT).show()
        }

        // Botão Câmera
        val btnCamera = findViewById<Button>(R.id.btnCamera)
        btnCamera.setOnClickListener { openCamera() }

        // Sensor acelerômetro
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    // Acelerômetro
    override fun onSensorChanged(event: SensorEvent?) {
        val curTime = System.currentTimeMillis()

        if ((curTime - lastUpdateTime) > 120) {
            val diffTime = curTime - lastUpdateTime
            lastUpdateTime = curTime

            val x = event!!.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

            if (speed > 900) {
                val timestamp = dateFormatter.format(Date())

                trashList.add("📱 Movimento detectado lixo Coletado. — $timestamp")
                trashAdapter.notifyDataSetChanged()

                tvCount.text = "Total: ${trashList.size}"
            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        accelSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}







