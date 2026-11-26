
package br.com.freefirecapaia.pro

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.sqrt

@Composable
fun MotionMonitorScreen() {
    val context = LocalContext.current

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE)
                as SensorManager
    }

    val accelerometer = remember {
        sensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER
        )
    }

    var movement by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember {
        mutableStateOf(accelerometer != null)
    }

    DisposableEffect(sensorManager, accelerometer) {
        if (accelerometer == null) {
            sensorAvailable = false
            onDispose { }
        } else {
            val gravity = FloatArray(3)

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val alpha = 0.8f

                    for (i in 0..2) {
                        gravity[i] =
                            alpha * gravity[i] +
                            (1 - alpha) * event.values[i]
                    }

                    val x = event.values[0] - gravity[0]
                    val y = event.values[1] - gravity[1]
                    val z = event.values[2] - gravity[2]

                    val intensity = sqrt(
                        x * x + y * y + z * z
                    )

                    movement = intensity
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int
                ) {}
            }

            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val status = when {
        !sensorAvailable -> "SENSOR INDISPONÍVEL"
        movement < 0.35f -> "MOVIMENTO BAIXO"
        movement < 1.2f -> "MOVIMENTO MODERADO"
        else -> "MOVIMENTO ALTO"
    }

    val statusColor = when {
        !sensorAvailable -> Color.Gray
        movement < 0.35f -> Color(0xFF4CAF50)
        movement < 1.2f -> Color(0xFFFFC107)
        else -> Color(0xFFE53935)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "MONITOR DE MOVIMENTO IA",
            color = Color(0xFFE53935),
            fontSize = 21.sp
        )

        Spacer(Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF191919)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier.padding(20.dp)
            ) {
                Text(
                    "ESTADO DO SENSOR",
                    color = Color.LightGray
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    status,
                    color = statusColor,
                    fontSize = 21.sp
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    "Intensidade do movimento",
                    color = Color.White
                )

                Text(
                    String.format(
                        Locale.getDefault(),
                        "%.2f m/s²",
                        movement
                    ),
                    color = Color.White,
                    fontSize = 29.sp
                )

                Spacer(Modifier.height(15.dp))

                LinearProgressIndicator(
                    progress = {
                        (movement / 5f).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = statusColor,
                    trackColor = Color.DarkGray
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "O acelerômetro mede a aceleração do aparelho " +
            "após uma estimativa da gravidade. Os valores " +
            "são indicadores experimentais, não uma medida " +
            "clínica de tremor ou estabilidade da mira.",
            color = Color.LightGray,
            fontSize = 13.sp
        )

        if (!sensorAvailable) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Este dispositivo não disponibilizou o acelerômetro.",
                color = Color(0xFFFFC107)
            )
        }
    }
}
