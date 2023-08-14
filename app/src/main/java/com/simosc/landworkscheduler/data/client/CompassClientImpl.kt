package com.simosc.landworkscheduler.data.client

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.simosc.landworkscheduler.domain.client.CompassClient
import com.simosc.landworkscheduler.domain.exception.NoAccelerometerSensorException
import com.simosc.landworkscheduler.domain.exception.NoMagnetometerSensorException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


class CompassClientImpl(
    private val sensorManager: SensorManager
): CompassClient {

    constructor(
        context: Context
    ) : this(
        context.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
    )

    override fun getBearingUpdates(
        interval: Long,
        fasterInterval: Long,
        minDegreesDiff: Float,
    ): Flow<Float> {
        return callbackFlow {

            val accelerometer: Sensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            ) ?: throw NoAccelerometerSensorException()

            val magnetometer: Sensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD
            ) ?: throw NoMagnetometerSensorException()

            val rMatrix = FloatArray(9)
            val iMatrix = FloatArray(9)
            val aData = FloatArray(3)
            val mData = FloatArray(3)

            var lastUpdate: Long? = null
            var lastAzimuth: Float? = null

            val sensorListener = object : SensorEventListener{

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

                override fun onSensorChanged(event: SensorEvent) {
                    when(event.sensor){
                        accelerometer -> System.arraycopy(
                            event.values,
                            0,
                            aData,
                            0,
                            event.values.size
                        )

                        magnetometer -> System.arraycopy(
                            event.values,
                            0,
                            mData,
                            0,
                            event.values.size
                        )

                    }
                    val success = SensorManager.getRotationMatrix(
                        rMatrix,
                        iMatrix,
                        aData,
                        mData
                    )

                    if (success) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rMatrix, orientation)
                        val currAzimuth = Math.toDegrees(
                            orientation[0].toDouble()
                        ).toFloat()

                        val currTimeMillis = System.currentTimeMillis()
                        val doUpdate = lastUpdate?.let{ previousTimeMillis ->
                            if(currTimeMillis.minus(previousTimeMillis).absoluteValue < interval)
                                lastAzimuth?.let{ previousAzimuth ->
                                    currTimeMillis.minus(previousTimeMillis).absoluteValue >= fasterInterval &&
                                    currAzimuth.minus(previousAzimuth).absoluteValue >= minDegreesDiff
                                } ?: true
                            else true
                        } ?: true

                        if(doUpdate){
                            lastUpdate = System.currentTimeMillis()
                            lastAzimuth = currAzimuth
                            launch {
                                send(currAzimuth)
                            }
                        }

                    }
                }
            }

            sensorManager.registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            sensorManager.registerListener(
                sensorListener,
                magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )

            awaitClose {
                sensorManager.unregisterListener(
                    sensorListener,
                    accelerometer
                )
                sensorManager.unregisterListener(
                    sensorListener,
                    magnetometer
                )
            }
        }
    }
}