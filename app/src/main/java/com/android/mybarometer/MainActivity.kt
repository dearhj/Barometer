package com.android.mybarometer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE
import android.hardware.Sensor.TYPE_PRESSURE
import android.hardware.Sensor.TYPE_TEMPERATURE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow


class MainActivity : AppCompatActivity() {
    private var sensorManager: SensorManager? = null
    private var dashboardView: DashboardView? = null
    private var barometerValue: TextView? = null
    private var altitudeValue: TextView? = null
    private var temperatureValue: TextView? = null
    private var altitudeCheck: TextView? = null
    private var deleteCheck: TextView? = null
    private var currentBarometer = 0f
    private var mStandardAtmosphericPressure = 1013.25f
    private var sp: SharedPreferences? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.barometer_layout)
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        mStandardAtmosphericPressure =
            sp?.getFloat("StandardAtmosphericPressure", 1013.25f) ?: 1013.25f
        dashboardView = findViewById(R.id.barometerPanelView)
        barometerValue = findViewById(R.id.barometer_value)
        altitudeValue = findViewById(R.id.altitude_value)
        temperatureValue = findViewById(R.id.temperature_value)
        altitudeCheck = findViewById(R.id.altitude_check)
        deleteCheck = findViewById(R.id.delete_check)
        try {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        } catch (e: Exception) {
            e.printStackTrace()
        }
        altitudeCheck?.setOnClickListener { showEditTextDialog() }
        deleteCheck?.setOnClickListener {
            if ((sp?.getFloat("StandardAtmosphericPressure", 1013.25f) ?: 1013.25f) == 1013.25f) toast(getString(R.string.no_data))
            else showDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            sensorManager?.let {
                for (sensor in it.getSensorList(-1)!!) {
                    it.registerListener(gSensorListener, sensor, 3)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    var lastTime = 0L
    var temperatureLastTime = 0L
    private var gSensorListener = object : SensorEventListener {
        @SuppressLint("SetTextI18n")
        override fun onSensorChanged(event: SensorEvent?) {
            if (event!!.sensor.type == TYPE_PRESSURE) {
                if (System.currentTimeMillis() - lastTime > 1000) {
                    lastTime = System.currentTimeMillis()
                    currentBarometer = event.values[0]
                    val showPressureValue =
                        (currentBarometer * 100 + 0.5f).toInt() / 100f //四舍五入,保留两位小数
                    val altitude =
                        SensorManager.getAltitude(mStandardAtmosphericPressure, currentBarometer)
                    dashboardView?.udDataSpeed(showPressureValue)
                    barometerValue?.text = "$showPressureValue hPa"
                    altitudeValue?.text = "${(altitude * 100 + 0.5f).toInt() / 100f} m"
                    //模拟数据
                    val value = 25.6f
                    val fValue = (value * 9 / 5) + 32
                    temperatureValue?.text = "$value °C / $fValue °F"
//                    println("这里的数值是》》》》》    $currentBarometer     $altitude")
                }
            } else if (event.sensor.type == TYPE_TEMPERATURE || event.sensor.type == TYPE_AMBIENT_TEMPERATURE) {
                if (System.currentTimeMillis() - temperatureLastTime > 1000) {
                    temperatureLastTime = System.currentTimeMillis()
                    val temperatureSensorValue = event.values[0]
                    val value = 25.6f
                    val fValue = (value * 9 / 5) + 32
                    temperatureValue?.text = "$value °C / $fValue °F"
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }
    }

    private fun getBasePressure(f: Float): Float {
        return currentBarometer / (1.0f - (f / 44330.0f)).pow(5.255f)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(gSensorListener)
    }

    private var toast: Toast? = null
    private fun toast(msg: String) {
        if (toast != null) toast?.cancel()
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        toast?.show()
    }


    private fun showDialog() {
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_sure))
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                sp?.edit()?.putFloat("StandardAtmosphericPressure", 1013.25f)
                    ?.apply()
                mStandardAtmosphericPressure = 1013.25f
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showEditTextDialog() {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.input_layout, null)
        val inputValue = dialogView.findViewById<EditText>(R.id.text)
        val regex = "^(?:0|[0-9]\\d{0,3}(?:\\.\\d{1,2})?|10000(?:\\.0{1,2})?)$".toRegex()
        var lastValidValue = ""
        inputValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                try {
                    s?.let {
                        var input = it.toString()
                        if ((input.isEmpty() || regex.matches(input) || input.endsWith(".")) && !input.startsWith(
                                "."
                            )
                        ) {
                            if (input.startsWith("0") && input.length > 1 && !input.startsWith("0.")) {
                                input = input.substring(1)
                                inputValue.setText(input)
                                inputValue.setSelection(inputValue.text.length)
                            }
                            lastValidValue = input
                        } else {
                            inputValue.setText(lastValidValue)
                            inputValue.setSelection(inputValue.text.length)
                        }
                    }
                } catch (_: Exception) {
                }
            }
        })
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.currentAltitude))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                if(inputValue.text.toString() != "") {
                    val value = inputValue.text.toString().toFloat()
                    mStandardAtmosphericPressure = getBasePressure(value)
                    sp?.edit()
                        ?.putFloat("StandardAtmosphericPressure", mStandardAtmosphericPressure)
                        ?.apply()
                    altitudeValue?.text = "$value m"
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()
        dialog.show()
    }
}