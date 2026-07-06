package com.kira.superspm.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

object AccelerometerService : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var isRunning = false
    private var hasSensor = false

    private var currentAccelerationX = 0.0
    private var currentAccelerationY = 0.0
    private var currentAccelerationZ = 0.0
    private var estimatedSpeed = 0.0
    private var lastUpdateTime = 0L

    private var speedAccumulator = 0.0
    private var totalDistance = 0.0

    var onAccelerationUpdate: ((Double, Double, Double) -> Unit)? = null
    var onSpeedEstimateUpdate: ((Double) -> Unit)? = null
    var onDistanceUpdate: ((Double) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun hasSensor(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        if (linearAccel != null) return true
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return accel != null
    }

    fun start(context: Context) {
        if (isRunning) {
            sensorManager?.unregisterListener(this)
        }

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        if (accelerometer == null) {
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }

        if (accelerometer != null) {
            hasSensor = true
            try {
                val registered = sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                if (registered == true) {
                    isRunning = true
                    lastUpdateTime = 0L
                    speedAccumulator = 0.0
                } else {
                    isRunning = false
                    onError?.invoke("传感器注册失败")
                }
            } catch (e: SecurityException) {
                isRunning = false
                onError?.invoke("传感器权限被拒绝")
            } catch (e: Exception) {
                isRunning = false
                onError?.invoke("启动传感器失败: ${e.message}")
            }
        } else {
            hasSensor = false
            isRunning = false
            onError?.invoke("设备不支持加速度计")
        }
    }

    fun stop() {
        if (!isRunning) return

        sensorManager?.unregisterListener(this)
        isRunning = false
        currentAccelerationX = 0.0
        currentAccelerationY = 0.0
        currentAccelerationZ = 0.0
        estimatedSpeed = 0.0
        speedAccumulator = 0.0
        totalDistance = 0.0
    }

    fun isRunning(): Boolean = isRunning

    fun getCurrentAcceleration(): Triple<Double, Double, Double> {
        return Triple(currentAccelerationX, currentAccelerationY, currentAccelerationZ)
    }

    fun getEstimatedSpeed(): Double = estimatedSpeed

    fun getTotalDistance(): Double = totalDistance

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        currentAccelerationX = event.values[0].toDouble()
        currentAccelerationY = event.values[1].toDouble()
        currentAccelerationZ = event.values[2].toDouble()

        onAccelerationUpdate?.invoke(currentAccelerationX, currentAccelerationY, currentAccelerationZ)

        val currentTime = System.currentTimeMillis()
        if (lastUpdateTime == 0L) {
            lastUpdateTime = currentTime
            return
        }

        val deltaTime = (currentTime - lastUpdateTime) / 1000.0
        lastUpdateTime = currentTime

        val magnitude = kotlin.math.sqrt(
            currentAccelerationX * currentAccelerationX +
            currentAccelerationY * currentAccelerationY +
            currentAccelerationZ * currentAccelerationZ
        )

        if (magnitude > 0.1) {
            speedAccumulator += magnitude * deltaTime
        }

        speedAccumulator *= 0.95

        estimatedSpeed = speedAccumulator * 3.6

        totalDistance += estimatedSpeed / 3.6 * deltaTime

        onSpeedEstimateUpdate?.invoke(estimatedSpeed)
        onDistanceUpdate?.invoke(totalDistance)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun resetDistance() {
        totalDistance = 0.0
        speedAccumulator = 0.0
    }
}
