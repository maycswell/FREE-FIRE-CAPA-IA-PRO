
package br.com.freefirecapaia.pro

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HistoryRed = Color(0xFFE53935)
private val HistoryPanel = Color(0xFF191919)
private val HistoryWhite = Color.White
private val HistoryGray = Color.LightGray

private class TrainingRepository(context: Context) {

    private val prefs = context.getSharedPreferences(
        "capa_ia_training",
        Context.MODE_PRIVATE
    )

    fun attempts() = prefs.getInt("attempts", 0)

    fun hits() = prefs.getInt("hits", 0)

    fun sensitivity() = prefs.getInt("sensitivity", 100)

    fun tests() = prefs.getInt("tests", 0)

    fun lastDate() = prefs.getString("last_date", "Nenhum teste")!!

    fun addAttempt() {
        prefs.edit()
            .putInt("attempts", attempts() + 1)
            .putString("last_date", currentDate())
            .apply()
    }

    fun addHit() {
        if (hits() < attempts()) {
            prefs.edit()
                .putInt("hits", hits() + 1)
                .putString("last_date", currentDate())
                .apply()
        }
    }

    fun saveSensitivity(value: Int) {
        prefs.edit()
            .putInt("sensitivity", value.coerceIn(0, 200))
            .apply()
    }

    fun saveTest() {
        prefs.edit()
            .putInt("tests", tests() + 1)
            .putString("last_date", currentDate())
            .apply()
    }

    fun reset() {
        prefs.edit().clear().apply()
    }

    private fun currentDate(): String {
        return SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(Date())
    }
}

@Composable
fun TrainingScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val repository = remember {
        TrainingRepository(context.applicationContext)
    }

    var attempts by remember {
        mutableIntStateOf(repository.attempts())
    }

    var hits by remember {
        mutableIntStateOf(repository.hits())
    }

    var tests by remember {
        mutableIntStateOf(repository.tests())
    }

    var lastDate by remember {
        mutableStateOf(repository.lastDate())
    }

    val percentage = if (attempts > 0) {
        hits * 100 / attempts
    } else {
        0
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "TREINAMENTO DE MIRA",
            color = HistoryRed,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(18.dp))

        HistoryCard("TENTATIVAS", attempts.toString())
        HistoryCard("ACERTOS", hits.toString())
        HistoryCard("TAXA DE ACERTOS", "$percentage%")
        HistoryCard("TESTES SALVOS", tests.toString())
        HistoryCard("ÚLTIMA ATIVIDADE", lastDate)

        Spacer(Modifier.height(15.dp))

        Button(
            onClick = {
                repository.addAttempt()
                attempts = repository.attempts()
                lastDate = repository.lastDate()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = HistoryRed
            )
        ) {
            Text("REGISTRAR TENTATIVA")
        }

        OutlinedButton(
            onClick = {
                if (hits < attempts) {
                    repository.addHit()
                    hits = repository.hits()
                    lastDate = repository.lastDate()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTRAR ACERTO")
        }

        OutlinedButton(
            onClick = {
                repository.saveTest()
                tests = repository.tests()
                lastDate = repository.lastDate()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SALVAR RESULTADO DO TESTE")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Registre cada tentativa e marque os acertos correspondentes. " +
            "Os resultados ficam armazenados no celular.",
            color = HistoryGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SensitivityScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val repository = remember {
        TrainingRepository(context.applicationContext)
    }

    var value by remember {
        mutableIntStateOf(repository.sensitivity())
    }

    var recommendation by remember {
        mutableIntStateOf(repository.sensitivity())
    }

    var saved by remember {
        mutableStateOf(false)
    }

    val attempts = repository.attempts()
    val hits = repository.hits()

    val percentage = if (attempts > 0) {
        hits * 100 / attempts
    } else {
        0
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "SENSIBILIDADE IA",
            color = HistoryRed,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        HistoryCard(
            "SENSIBILIDADE ATUAL",
            value.toString()
        )

        Text(
            "Ajuste de teste",
            color = HistoryWhite
        )

        Slider(
            value = value.toFloat(),
            onValueChange = {
                value = it.toInt()
                saved = false
            },
            valueRange = 0f..200f,
            colors = SliderDefaults.colors(
                thumbColor = HistoryRed,
                activeTrackColor = HistoryRed
            )
        )

        Button(
            onClick = {
                repository.saveSensitivity(value)
                saved = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = HistoryRed
            )
        ) {
            Text("SALVAR CONFIGURAÇÃO")
        }

        if (saved) {
            Text(
                "Configuração salva no celular.",
                color = Color(0xFF4CAF50)
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            "ANÁLISE DOS RESULTADOS",
            color = HistoryRed,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "Tentativas: $attempts",
            color = HistoryWhite
        )

        Text(
            "Acertos: $hits",
            color = HistoryWhite
        )

        Text(
            "Taxa de acertos: $percentage%",
            color = HistoryWhite
        )

        Spacer(Modifier.height(15.dp))

        Button(
            onClick = {
                recommendation = when {
                    attempts < 10 -> value
                    percentage >= 70 ->
                        (value + 5).coerceAtMost(200)
                    percentage < 40 ->
                        (value - 5).coerceAtLeast(0)
                    else -> value
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = HistoryRed
            )
        ) {
            Text("ANALISAR E RECOMENDAR")
        }

        Spacer(Modifier.height(12.dp))

        HistoryCard(
            "RECOMENDAÇÃO EXPERIMENTAL",
            recommendation.toString()
        )

        Text(
            if (attempts < 10) {
                "Registre pelo menos 10 tentativas para obter uma recomendação inicial."
            } else {
                "Teste a recomendação em uma sessão de treinamento antes de decidir se deseja utilizá-la."
            },
            color = HistoryGray,
            fontSize = 12.sp
        )

        Text(
            "A recomendação é experimental e não garante mais acertos. " +
            "Ela não altera automaticamente o Free Fire.",
            color = HistoryGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun HistoryCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(
            containerColor = HistoryPanel
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                title,
                color = HistoryRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                value,
                color = HistoryWhite,
                fontSize = 20.sp
            )
        }
    }
}
