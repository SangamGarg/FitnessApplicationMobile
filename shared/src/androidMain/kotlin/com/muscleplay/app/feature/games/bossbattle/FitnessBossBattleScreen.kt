package com.muscleplay.app.feature.games.bossbattle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muscleplay.app.feature.games.camera.ExerciseCameraConfig
import com.muscleplay.app.feature.games.camera.ExerciseCameraScreen
import com.muscleplay.app.feature.games.core.ExerciseDetector
import com.muscleplay.app.feature.games.core.ExerciseState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val BOSS_MAX_HEALTH = 100

@Composable
fun FitnessBossBattleScreen(
    detector: ExerciseDetector,
    config: ExerciseCameraConfig,
    onBack: () -> Unit,
) {
    val engine = remember {
        FitnessBossBattleEngine(
            config = BossBattleConfig(
                durationSeconds = 60,
                bossMaxHealth = BOSS_MAX_HEALTH,
                damagePerRep = 10,
                comboEvery = 5,
                comboBonusDamage = 5,
                pointsPerRep = 100,
                perfectRepBonus = 25
            )
        )
    }

    val repBridge = remember(engine) {
        BossBattleRepBridge(engine)
    }

    var gameState by remember {
        mutableStateOf(engine.state)
    }

    var latestExerciseState by remember {
        mutableStateOf(ExerciseState())
    }

    // Advance the game clock once per second while the battle is running.
    LaunchedEffect(gameState.status) {
        while (gameState.status == BossBattleStatus.RUNNING) {
            delay(1000L.milliseconds)
            gameState = engine.tickSecond()
        }
    }

    ExerciseCameraScreen(
        detector = detector,
        config = config,
        onBack = onBack,
        onExerciseStateChanged = { exerciseState ->
            latestExerciseState = exerciseState

            repBridge.onExerciseState(exerciseState)
            gameState = engine.state
        },
        overlayContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                BossBattleOverlay(
                    state = gameState,
                    onStart = {
                        repBridge.start(
                            currentRepetitions =
                                latestExerciseState.repetitions
                        )
                        gameState = engine.state
                    },
                    onRestart = {
                        val resetState = detector.reset()
                        latestExerciseState = resetState

                        repBridge.reset(
                            currentRepetitions = resetState.repetitions
                        )
                        repBridge.start(
                            currentRepetitions = resetState.repetitions
                        )

                        gameState = engine.state
                    }
                )
            }
        }
    )
}

@Composable
private fun BossBattleOverlay(
    state: BossBattleState,
    onStart: () -> Unit,
    onRestart: () -> Unit,
) {
    val healthRatio = (
            state.bossHealth.toFloat() / BOSS_MAX_HEALTH
            ).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xE6151722)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚔ FITNESS BOSS BATTLE",
                color = Color(0xFFFFB74D),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BOSS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${state.bossHealth} / $BOSS_MAX_HEALTH HP",
                    color = Color(0xFFFF8A80),
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { healthRatio },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFF5252),
                trackColor = Color(0xFF49404A)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BattleStat(
                    label = "TIME",
                    value = "${(60 - state.elapsedSeconds).coerceAtLeast(0)}s"
                )
                BattleStat(
                    label = "REPS",
                    value = state.repetitions.toString()
                )
                BattleStat(
                    label = "SCORE",
                    value = state.score.toString()
                )
            }

            Text(
                text = "COMBO  ×${state.currentCombo}",
                color = Color(0xFFFFD54F),
                fontWeight = FontWeight.Bold
            )

            when (state.status) {
                BossBattleStatus.READY -> {
                    Text(
                        text = "Get ready! Every valid rep damages the boss.",
                        color = Color.White
                    )
                    Button(
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("START BATTLE")
                    }
                }

                BossBattleStatus.RUNNING -> {
                    Text(
                        text = when {
                            state.currentCombo >= 5 ->
                                "Combo active! Keep going!"

                            else ->
                                "Complete controlled reps to attack."
                        },
                        color = Color.White
                    )
                }

                BossBattleStatus.WON -> {
                    Text(
                        text = "🏆 BOSS DEFEATED!",
                        color = Color(0xFF7CF5B4),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Button(
                        onClick = onRestart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PLAY AGAIN")
                    }
                }

                BossBattleStatus.TIME_UP -> {
                    Text(
                        text = "TIME'S UP! Final score: ${state.score}",
                        color = Color(0xFFFFCC80),
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onRestart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TRY AGAIN")
                    }
                }
            }
        }
    }
}

@Composable
private fun BattleStat(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFFB8C0D4),
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
