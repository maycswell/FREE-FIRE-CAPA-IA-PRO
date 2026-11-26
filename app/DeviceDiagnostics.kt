
package br.com.freefirecapaia.pro

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.app.ActivityManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

data class DeviceStats(
    val totalRam: Long,
    val availableRam: Long,
    val battery: Int,
    val temperature: Float?,
    val device: String,
    val androidVersion: String
)

private fun readDeviceStats(context: Context): DeviceStats {
    val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE)
                as ActivityManager

    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)

    val batteryManager =
        context.getSystemService(Context.BATTERY_SERVICE)
                as BatteryManager

    val batteryLevel = batteryManager.getIntProperty(
        BatteryManager.BATTERY_PROPERTY_CAPACITY
    ).coerceIn(0, 100)

    val batteryIntent = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    val temperatureValue = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_TEMPERATURE,
        -1
    ) ?: -1

    val temperature = if (temperatureValue >= 0) {
        temperatureValue / 10f
    } else {
        null
    }

    return DeviceStats(
        totalRam = memoryInfo.totalMem,
        availableRam = memoryInfo.availMem,
        battery = batteryLevel,
        temperature = temperature,
        device = "${Build.MANUFACTURER} ${Build.MODEL}",
        androidVersion = Build.VERSION.RELEASE
    )
}

private fun formatRam(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return String.format(Locale.getDefault(), "%.2f GB", gb)
}

@Composable
fun DeviceDiagnosticsScreen() {
    val context = LocalContext.current

    var stats by remember {
        mutableStateOf(readDeviceStats(context))
    }

    val usedRam = stats.totalRam - stats.availableRam
    val ramUsage = if (stats.totalRam > 0) {
        usedRam.toFloat() / stats.totalRam.toFloat()
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            "DIAGNÓSTICO DO DISPOSITIVO",
            color = Color(0xFFE53935),
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        DiagnosticCard("DISPOSITIVO", stats.device)
        DiagnosticCard("ANDROID", "Versão ${stats.androidVersion}")
        DiagnosticCard("BATERIA", "${stats.battery}%")

        DiagnosticCard(
            "TEMPERATURA DA BATERIA",
            stats.temperature?.let {
                String.format(Locale.getDefault(), "%.1f °C", it)
            } ?: "Não disponível"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF191919)
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "MEMÓRIA RAM",
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Total: ${formatRam(stats.totalRam)}",
                    color = Color.White
                )

                Text(
                    "Disponível: ${formatRam(stats.availableRam)}",
                    color = Color.White
                )

                Text(
                    "Em uso: ${formatRam(usedRam)}",
                    color = Color.LightGray
                )

                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { ramUsage.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFE53935),
                    trackColor = Color.DarkGray
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                stats = readDeviceStats(context)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935)
            )
        ) {
            Text("ATUALIZAR DIAGNÓSTICO")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Os dados são informativos. A temperatura é da " +
            "bateria, não necessariamente do processador. " +
            "A memória disponível pode mudar durante o uso.",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DiagnosticCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF191919)
        )
    ) {
        Column(Modifier.padding(15.dp)) {
            Text(
                title,
                color = Color(0xFFE53935),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(5.dp))

            Text(
                value,
                color = Color.White,
                fontSize = 17.sp
            )
        }
    }
}
