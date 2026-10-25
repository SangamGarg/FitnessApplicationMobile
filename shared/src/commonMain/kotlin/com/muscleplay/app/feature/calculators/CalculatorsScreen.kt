package com.muscleplay.app.feature.calculators

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)

@Composable
fun CalculatorsScreen() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var weightText by rememberSaveable { mutableStateOf("70") }
    var heightText by rememberSaveable { mutableStateOf("175") }
    var ageText by rememberSaveable { mutableStateOf("25") }
    var repsText by rememberSaveable { mutableStateOf("10") }
    var counter by rememberSaveable { mutableIntStateOf(0) } // Preserving state test counter

    // Ambient Glowing Background Pulsing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "calcPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Glowing Background Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeGlow.copy(alpha = pulseAlpha), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.2f),
                    radius = w * 0.65f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrangeSecondary.copy(alpha = pulseAlpha * 0.6f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.8f),
                    radius = w * 0.55f
                )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
        ) {
            item {
                Column {
                    Surface(
                        color = OrangePrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "FITNESS CALCULATORS",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Smart Analytics 🧮",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Calculate your body composition and training targets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Calculator Mode Switcher Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("BMI", "BMR & TDEE", "1-Rep Max").forEachIndexed { index, title ->
                        FilterChip(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = { Text(title, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }

            // Interactive Calculator Glass Inputs & Results Card
            item {
                GlassCardContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val weight = weightText.toDoubleOrNull() ?: 70.0
                        val height = heightText.toDoubleOrNull() ?: 175.0
                        val age = ageText.toIntOrNull() ?: 25
                        val reps = repsText.toIntOrNull() ?: 10

                        when (selectedTab) {
                            0 -> { // BMI Calculator
                                Text("Body Mass Index (BMI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                                InputRow("Weight (kg)", weightText) { weightText = it }
                                InputRow("Height (cm)", heightText) { heightText = it }

                                val heightM = height / 100.0
                                val bmi = if (heightM > 0) weight / (heightM * heightM) else 0.0
                                val roundedBmi = (bmi * 10).roundToInt() / 10.0
                                val (status, statusColor) = when {
                                    bmi < 18.5 -> "Underweight" to OrangeSecondary
                                    bmi < 25.0 -> "Healthy Weight" to Color(0xFF4CAF50)
                                    bmi < 30.0 -> "Overweight" to OrangeSecondary
                                    else -> "Obese" to Color(0xFFFF5252)
                                }

                                ResultBox(
                                    primaryValue = "$roundedBmi",
                                    unit = "BMI Index",
                                    badgeText = status,
                                    badgeColor = statusColor
                                )
                            }
                            1 -> { // BMR & TDEE Calculator
                                Text("Basal Metabolic Rate (BMR)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                                InputRow("Weight (kg)", weightText) { weightText = it }
                                InputRow("Height (cm)", heightText) { heightText = it }
                                InputRow("Age (years)", ageText) { ageText = it }

                                val bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5
                                val tdee = bmr * 1.55 // Moderate activity

                                ResultBox(
                                    primaryValue = "${bmr.roundToInt()}",
                                    unit = "kcal / day BMR",
                                    badgeText = "TDEE ~ ${tdee.roundToInt()} kcal",
                                    badgeColor = OrangeSecondary
                                )
                            }
                            2 -> { // 1-Rep Max Calculator
                                Text("1-Rep Max Estimate (1RM)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)

                                InputRow("Lifted Weight (kg)", weightText) { weightText = it }
                                InputRow("Repetitions Done", repsText) { repsText = it }

                                val oneRepMax = weight * (1 + (reps / 30.0))

                                ResultBox(
                                    primaryValue = "${oneRepMax.roundToInt()}",
                                    unit = "kg Estimated 1RM",
                                    badgeText = "Max Strength Benchmark",
                                    badgeColor = OrangePrimary
                                )
                            }
                        }
                    }
                }
            }

            // State Preservation Test Box
            item {
                GlassCardContainer {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("State Preservation Test", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Saved Counter: $counter", style = MaterialTheme.typography.bodyMedium, color = OrangeSecondary)
                        }
                        Button(
                            onClick = { counter++ },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+1", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputRow(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = OrangePrimary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = OrangePrimary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
        ),
        singleLine = true
    )
}

@Composable
private fun ResultBox(primaryValue: String, unit: String, badgeText: String, badgeColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, OrangePrimary.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = primaryValue, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(text = unit, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            }

            Surface(
                color = badgeColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassCardContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        OrangePrimary.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}
