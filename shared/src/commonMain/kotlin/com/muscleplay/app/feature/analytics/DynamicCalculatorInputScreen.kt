package com.muscleplay.app.feature.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muscleplay.app.feature.analytics.data.AnalyticsRepository
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)
private val CardBackground = Color(0xFF181824)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicCalculatorInputScreen(
    calculatorKey: String,
    analyticsRepository: AnalyticsRepository? = null,
    onBack: () -> Unit,
) {
    val viewModel = remember(analyticsRepository) { AnalyticsViewModel(analyticsRepository) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val formattedName = remember(calculatorKey) {
        calculatorKey.replace("-", " ")
            .split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
    }

    var weightText by remember { mutableStateOf("70") }
    var heightText by remember { mutableStateOf("175") }
    var ageText by remember { mutableStateOf("25") }
    var repsText by remember { mutableStateOf("10") }
    var isMaleGender by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(formattedName, fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                item {
                    Text(
                        text = "Smart $formattedName Calculator 🧮",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangeSecondary
                    )
                    Text(
                        text = "Enter your metrics to run a live smart calculation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Inputs
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (calculatorKey.lowercase().trim()) {
                                "bmi", "ideal-weight", "body-fat" -> {
                                    InputRow("Height (cm)", heightText) { heightText = it }
                                    if (calculatorKey != "bmi") {
                                        InputRow("Weight (kg)", weightText) { weightText = it }
                                    }
                                }

                                "bmr", "tdee" -> {
                                    GenderSelector(isMale = isMaleGender) { isMaleGender = it }
                                    InputRow("Weight (kg)", weightText) { weightText = it }
                                    InputRow("Height (cm)", heightText) { heightText = it }
                                    InputRow("Age (years)", ageText) { ageText = it }
                                }

                                "one-rep-max" -> {
                                    InputRow("Lifted Weight (kg)", weightText) { weightText = it }
                                    InputRow("Repetitions (1-12)", repsText) { repsText = it }
                                }

                                else -> {
                                    InputRow("Weight (kg)", weightText) { weightText = it }
                                    InputRow("Height (cm)", heightText) { heightText = it }
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    val isCalculating = uiState.calculationState is CalculationUiState.Calculating
                    Button(
                        onClick = {
                            val inputsMap = mutableMapOf<String, JsonElement>()
                            weightText.toDoubleOrNull()?.let { inputsMap["weightKg"] = JsonPrimitive(it) }
                            heightText.toDoubleOrNull()?.let { inputsMap["heightCm"] = JsonPrimitive(it) }
                            ageText.toIntOrNull()?.let { inputsMap["age"] = JsonPrimitive(it) }
                            repsText.toIntOrNull()?.let { inputsMap["reps"] = JsonPrimitive(it) }
                            inputsMap["gender"] = JsonPrimitive(if (isMaleGender) "male" else "female")

                            viewModel.performSmartCalculation(calculatorKey, inputsMap)
                        },
                        enabled = !isCalculating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        if (isCalculating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = "Calculate")
                                Text("Calculate Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Calculation Results Render
                when (val calcState = uiState.calculationState) {
                    is CalculationUiState.Success -> {
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = CardBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Calculation Result 🎯",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeSecondary
                                    )

                                    calcState.resultMap.forEach { (k, v) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = k.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = v.jsonPrimitive.content,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is CalculationUiState.Error -> {
                        item {
                            Surface(
                                color = OrangePrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = calcState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun InputRow(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(input.filter { it.isDigit() || it == '.' })
        },
        label = { Text(label, color = OrangePrimary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
private fun GenderSelector(isMale: Boolean, onGenderSelected: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = isMale,
            onClick = { onGenderSelected(true) },
            label = { Text("♂ Male", fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = OrangePrimary,
                selectedLabelColor = Color.White,
                containerColor = Color.White.copy(alpha = 0.08f),
                labelColor = Color.White.copy(alpha = 0.7f)
            ),
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = !isMale,
            onClick = { onGenderSelected(false) },
            label = { Text("♀ Female", fontWeight = FontWeight.Bold) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = OrangePrimary,
                selectedLabelColor = Color.White,
                containerColor = Color.White.copy(alpha = 0.08f),
                labelColor = Color.White.copy(alpha = 0.7f)
            ),
            modifier = Modifier.weight(1f)
        )
    }
}
