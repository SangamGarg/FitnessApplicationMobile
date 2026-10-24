
package com.muscleplay.app.feature.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RepChallengeScreen(
    onBack: () -> Unit
) {
    var remainingSeconds by remember { mutableIntStateOf(30) }
    var completedReps by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }

    val isFinished = remainingSeconds == 0
    val score = completedReps * 10

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000.milliseconds)
            remainingSeconds -= 1

            if (remainingSeconds == 0) {
                isRunning = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "REP CHALLENGE",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Complete as many reps as possible!",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TIME LEFT",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = "${remainingSeconds}s",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )

                LinearProgressIndicator(
                    progress = { remainingSeconds / 30f },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "REPS",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = completedReps.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { completedReps += 1 },
            enabled = isRunning && !isFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("COUNT REP", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    hasStarted = true
                    isRunning = !isRunning
                },
                enabled = !isFinished,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    when {
                        isRunning -> "Pause"
                        hasStarted -> "Resume"
                        else -> "Start"
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    remainingSeconds = 30
                    completedReps = 0
                    isRunning = false
                    hasStarted = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Restart")
            }
        }

        if (isFinished) {
            Spacer(Modifier.height(20.dp))

            Text(
                text = "Challenge complete!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text("You completed $completedReps reps and scored $score points.")
        }
    }
}
