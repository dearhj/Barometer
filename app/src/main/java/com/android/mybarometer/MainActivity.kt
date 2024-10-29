package com.android.mybarometer

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_PRESSURE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow


class MainActivity : AppCompatActivity() {
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var dashboardView1: DashboardView? = null
    private var textView: TextView? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.barometer_layout)
        getBasePressure(369.0f, 995.78f)
        textView = findViewById(R.id.text)
        dashboardView1 = findViewById(R.id.barometerPanelView)
        try {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        sensor = sensorManager?.getDefaultSensor(TYPE_PRESSURE)
        if (sensor == null) {
            dashboardView1?.udDataSpeed(970f)
            return
        }
        sensorManager?.registerListener(
            gSensorListener, sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }



    //TYPE_PRESSURE
    //TYPE_RELATIVE_HUMIDITY
    //TYPE_TEMPERATURE

    private var gSensorListener = object : SensorEventListener {
        @SuppressLint("SetTextI18n")
        override fun onSensorChanged(event: SensorEvent?) {
            if (event!!.sensor.type == TYPE_PRESSURE) {
                val a = event.values[0]
                textView?.text = "气压数据是： $a"
                val little = SensorManager.getAltitude(1013.25f, a)
                println("这里的数值是》》》》》    $a     $little")

//                if (sensorId == Sensor.TYPE_PROXIMITY) {
//                    binding.tv.text = buildString {
//                        if (event.values[0] < sensor!!.maximumRange) appendLine("检测到物体靠近 ")
//                        else appendLine("没有检测有物体靠近")
//                    }
//                    count++
//                    if (auto() && count > 4) delayPass(1000)
//                } else {
//                    binding.tv.text = buildString {
//                        val values = event.values
//                        if (values.size == 3) {
//                            appendLine("x=${event.values[0]}")
//                            appendLine("y=${event.values[1]}")
//                            appendLine("z=${event.values[2]}")
//                        } else event.values.forEach { appendLine(it.toString()) }
//                    }
//                    count++
//                    if (auto() && count > 50) pass()
//                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }

    private fun getBasePressure(f: Float, f2: Float): Float {
        val pow = f2 / (1.0f - (f / 44330.0f)).pow(5.255f)
        Log.d("BarometerActivity", "getBasePressure, p0 = $pow")
        return pow
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(gSensorListener, sensor)
    }
}