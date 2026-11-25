package com.muscleplay.app.feature.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muscleplay.app.core.network.dto.HealthMetricDto
import com.muscleplay.app.core.network.dto.HealthSnapshotDto
import com.muscleplay.app.core.network.dto.MeasureOthersDto
import com.muscleplay.app.core.network.dto.PopularCalculatorDto
import com.muscleplay.app.core.network.dto.WeightAnalyticsDto
import kotlin.math.log10
import kotlin.math.roundToInt

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val OrangeGlow = Color(0xFFFF3D00)
private val DarkBackground = Color(0xFF0F0F17)
private val TextMuted = Color(0xFF9E9B9E)

private enum class ActivityLevel(val label: String, val multiplier: Double) {
    SEDENTARY("Sedentary (Office job)", 1.2),
    LIGHT("Lightly Active (1-3 days/wk)", 1.375),
    MODERATE("Moderately Active (3-5 days/wk)", 1.55),
    VERY_ACTIVE("Very Active (6-7 days/wk)", 1.725),
    ATHLETE("Athlete / Heavy Duty", 1.9)
}

private enum class MacroGoal(val label: String, val proteinPct: Double, val carbsPct: Double, val fatPct: Double) {
    FAT_LOSS("Fat Loss (40P / 30C / 30F)", 0.40, 0.30, 0.30),
    MAINTENANCE("Maintenance (30P / 40C / 30F)", 0.30, 0.40, 0.30),
    MUSCLE_GAIN("Muscle Gain (30P / 50C / 20F)", 0.30, 0.50, 0.20)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    analyticsRepository: com.muscleplay.app.feature.analytics.data.AnalyticsRepository? = null,
    onNavigateToAllCalculators: () -> Unit = {},
    onNavigateToCalculatorInput: (String) -> Unit = {},
    onNavigateToDietHome: () -> Unit = {},
) {
    val viewModel = remember(analyticsRepository) { AnalyticsViewModel(analyticsRepository) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 90.dp)
        ) {
            // Section 1: Header
            item {
                when (val state = uiState.analyticsHomeState) {
                    is AnalyticsHomeUiState.Success -> {
                        val data = state.homeData
                        Column {
                            Surface(
                                color = OrangePrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "FITNESS ANALYTICS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = data.title ?: "Smart Analytics 🧮",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            data.subtitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OrangeSecondary
                                )
                            }
                        }
                    }

                    is AnalyticsHomeUiState.Loading -> {
                        ShimmerAnalyticsHeader()
                    }

                    is AnalyticsHomeUiState.Error -> {
                        Column {
                            Text(
                                text = "Smart Analytics 🧮",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Section 2: Health Snapshot Banner
            item {
                when (val state = uiState.analyticsHomeState) {
                    is AnalyticsHomeUiState.Success -> {
                        state.homeData.healthSnapshot?.let { snapshot ->
                            HealthSnapshotBanner(
                                snapshot = snapshot,
                                isExpanded = uiState.isHealthSnapshotExpanded,
                                onToggleExpand = { viewModel.toggleHealthSnapshotExpanded() }
                            )
                        }
                    }

                    is AnalyticsHomeUiState.Loading -> {
                        ShimmerBannerCard()
                    }

                    is AnalyticsHomeUiState.Error -> {
                        Surface(
                            color = OrangePrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { viewModel.loadAnalyticsHome(forceRefresh = true) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Weight Analytics Banner
            item {
                (uiState.analyticsHomeState as? AnalyticsHomeUiState.Success)?.homeData?.weightAnalytics?.let { weightData ->
                    WeightAnalyticsBanner(weightData = weightData)
                }
            }

            // Section 4: Measure Others Banner
            item {
                (uiState.analyticsHomeState as? AnalyticsHomeUiState.Success)?.homeData?.measureOthers?.let { measureData ->
                    MeasureOthersBanner(
                        measureData = measureData,
                        onAction = onNavigateToDietHome
                    )
                }
            }

            // Section 5: Popular Calculators Banner
            item {
                val calculators = (uiState.analyticsHomeState as? AnalyticsHomeUiState.Success)?.homeData?.popularCalculators
                    ?: emptyList()
                if (calculators.isNotEmpty()) {
                    PopularCalculatorsBanner(
                        calculators = calculators,
                        onViewAll = onNavigateToAllCalculators,
                        onCalculatorClick = onNavigateToCalculatorInput
                    )
                }
            }

            // Section 6: Existing 5-in-1 Calculator Section (Placed below Popular Calculators)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                FiveInOneCalculatorSection()
            }
        }
    }
}

@Composable
private fun HealthSnapshotBanner(
    snapshot: HealthSnapshotDto,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
) {
    val sortedMetrics = remember(snapshot.metrics) {
        snapshot.metrics.sortedBy { it.displayOrder ?: 0 }
    }
    val displayedMetrics = if (isExpanded) sortedMetrics else sortedMetrics.take(4)

    GlassCardContainer {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = snapshot.title ?: "Health Snapshot 📊",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    snapshot.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                if (sortedMetrics.size > 4) {
                    TextButton(onClick = onToggleExpand) {
                        Text(
                            text = if (isExpanded) "Collapse" else "View All",
                            fontWeight = FontWeight.Bold,
                            color = OrangeSecondary
                        )
                    }
                }
            }

            snapshot.lastUpdatedAt?.let {
                Text(
                    text = "Last updated: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                displayedMetrics.chunked(2).forEach { rowMetrics ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMetrics.forEach { metric ->
                            HealthMetricCard(metric = metric, modifier = Modifier.weight(1f))
                        }
                        if (rowMetrics.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthMetricCard(metric: HealthMetricDto, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.06f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mapMetricIcon(metric.iconKey ?: metric.key),
                    contentDescription = null,
                    tint = OrangeSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = metric.title.ifBlank { metric.key },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1
                )
                Text(
                    text = "${metric.value} ${metric.unit ?: ""}".trim(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun WeightAnalyticsBanner(weightData: WeightAnalyticsDto) {
    GlassCardContainer {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight Analytics ⚖️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                weightData.goalName?.let {
                    Surface(
                        color = OrangePrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OrangeSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weightData.currentWeightKg?.let {
                    WeightStatItem("Current", "${it.roundToInt()} kg")
                }
                weightData.targetWeightKg?.let {
                    WeightStatItem("Target", "${it.roundToInt()} kg")
                }
                weightData.weightChangeKg?.let {
                    WeightStatItem("Change", "${if (it > 0) "+$it" else "$it"} kg")
                }
            }

            weightData.progressPercent?.let { pct ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Goal Progress", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("$pct%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    LinearProgressIndicator(
                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = OrangePrimary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightStatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun MeasureOthersBanner(measureData: MeasureOthersDto, onAction: () -> Unit) {
    GlassCardContainer {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = measureData.title ?: "Track More Metrics 📏",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                measureData.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text(measureData.buttonText ?: "Log Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PopularCalculatorsBanner(
    calculators: List<PopularCalculatorDto>,
    onViewAll: () -> Unit,
    onCalculatorClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Popular Calculators 🔥",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            TextButton(onClick = onViewAll) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View All", fontWeight = FontWeight.Bold, color = OrangeSecondary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(calculators, key = { it.key }) { calc ->
                Surface(
                    onClick = { onCalculatorClick(calc.key) },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.width(160.dp).height(120.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(OrangePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = mapMetricIcon(calc.iconKey ?: calc.key),
                                contentDescription = null,
                                tint = OrangeSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = calc.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            calc.description?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiveInOneCalculatorSection() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var isImperialUnits by rememberSaveable { mutableStateOf(false) }
    var isMaleGender by rememberSaveable { mutableStateOf(true) }

    var weightText by rememberSaveable { mutableStateOf("70") }
    var heightText by rememberSaveable { mutableStateOf("175") }
    var ageText by rememberSaveable { mutableStateOf("25") }
    var repsText by rememberSaveable { mutableStateOf("10") }
    var neckText by rememberSaveable { mutableStateOf("38") }
    var waistText by rememberSaveable { mutableStateOf("82") }
    var hipText by rememberSaveable { mutableStateOf("95") }
    var customCalorieGoalText by rememberSaveable { mutableStateOf("2200") }

    var selectedActivityLevel by remember { mutableStateOf(ActivityLevel.MODERATE) }
    var selectedMacroGoal by remember { mutableStateOf(MacroGoal.FAT_LOSS) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "5-in-1 Fitness Calculator ⚙️",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Surface(
                onClick = { isImperialUnits = !isImperialUnits },
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Straighten, contentDescription = "Units", tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                    Text(
                        text = if (isImperialUnits) "Imperial" else "Metric",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            val tabs = listOf("BMI", "BMR & TDEE", "Macro Split", "1-Rep Max", "Body Fat %")
            itemsIndexed(tabs) { index, title ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        GlassCardContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val rawWeight = weightText.toDoubleOrNull() ?: 70.0
                val rawHeight = heightText.toDoubleOrNull() ?: 175.0
                val age = ageText.toIntOrNull() ?: 25
                val reps = repsText.toIntOrNull() ?: 10
                val rawNeck = neckText.toDoubleOrNull() ?: 38.0
                val rawWaist = waistText.toDoubleOrNull() ?: 82.0
                val rawHip = hipText.toDoubleOrNull() ?: 95.0

                val weightKg = if (isImperialUnits) rawWeight / 2.20462 else rawWeight
                val heightCm = if (isImperialUnits) rawHeight * 2.54 else rawHeight
                val neckCm = if (isImperialUnits) rawNeck * 2.54 else rawNeck
                val waistCm = if (isImperialUnits) rawWaist * 2.54 else rawWaist
                val hipCm = if (isImperialUnits) rawHip * 2.54 else rawHip

                val weightLabel = if (isImperialUnits) "Weight (lbs)" else "Weight (kg)"
                val heightLabel = if (isImperialUnits) "Height (inches)" else "Height (cm)"
                val circumLabelSuffix = if (isImperialUnits) "(inches)" else "(cm)"
                val weightUnitName = if (isImperialUnits) "lbs" else "kg"

                when (selectedTab) {
                    0 -> {
                        Text("Body Mass Index (BMI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)

                        InputRow(weightLabel, weightText) { weightText = it }
                        InputRow(heightLabel, heightText) { heightText = it }

                        val heightM = heightCm / 100.0
                        val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 0.0
                        val roundedBmi = (bmi * 10).roundToInt() / 10.0

                        val (status, statusColor) = when {
                            bmi < 18.5 -> "Underweight" to OrangeSecondary
                            bmi < 25.0 -> "Healthy Weight" to Color(0xFF4CAF50)
                            bmi < 30.0 -> "Overweight" to OrangeSecondary
                            else -> "Obese" to Color(0xFFFF5252)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ResultBox(
                                primaryValue = "$roundedBmi",
                                unit = "BMI Score",
                                badgeText = status,
                                badgeColor = statusColor
                            )

                            Text("BMI Spectrum Gauge", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            val gaugeProgress = ((bmi - 12.0) / (35.0 - 12.0)).toFloat().coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { gaugeProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = statusColor,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }

                    1 -> {
                        Text("Basal Metabolic Rate & TDEE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)

                        GenderSelector(isMale = isMaleGender) { isMaleGender = it }
                        InputRow(weightLabel, weightText) { weightText = it }
                        InputRow(heightLabel, heightText) { heightText = it }
                        InputRow("Age (years)", ageText) { ageText = it }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Activity Level", style = MaterialTheme.typography.labelSmall, color = OrangePrimary, fontWeight = FontWeight.Bold)
                            ActivityLevel.entries.forEach { act ->
                                Surface(
                                    onClick = { selectedActivityLevel = act },
                                    color = if (selectedActivityLevel == act) OrangePrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedActivityLevel == act) OrangePrimary else Color.White.copy(alpha = 0.1f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(act.label, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                        if (selectedActivityLevel == act) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        val bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age) + (if (isMaleGender) 5 else -161)
                        val tdee = bmr * selectedActivityLevel.multiplier

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ResultBox(
                                primaryValue = "${bmr.roundToInt()}",
                                unit = "BMR (kcal / day)",
                                badgeText = "TDEE: ${tdee.roundToInt()} kcal",
                                badgeColor = OrangeSecondary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CalorieTargetChip("Fat Loss (-500)", "${(tdee - 500).roundToInt()} kcal", Color(0xFF4CAF50), Modifier.weight(1f))
                                CalorieTargetChip("Muscle Gain (+300)", "${(tdee + 300).roundToInt()} kcal", OrangePrimary, Modifier.weight(1f))
                            }
                        }
                    }

                    2 -> {
                        Text("Macro Nutrient Split", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)

                        InputRow("Daily Calorie Target (kcal)", customCalorieGoalText) { customCalorieGoalText = it }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Nutrient Goal Ratio", style = MaterialTheme.typography.labelSmall, color = OrangePrimary, fontWeight = FontWeight.Bold)
                            MacroGoal.entries.forEach { goal ->
                                Surface(
                                    onClick = { selectedMacroGoal = goal },
                                    color = if (selectedMacroGoal == goal) OrangePrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedMacroGoal == goal) OrangePrimary else Color.White.copy(alpha = 0.1f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(goal.label, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                        if (selectedMacroGoal == goal) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        val calories = customCalorieGoalText.toDoubleOrNull() ?: 2200.0
                        val proteinGrams = ((calories * selectedMacroGoal.proteinPct) / 4.0).roundToInt()
                        val carbsGrams = ((calories * selectedMacroGoal.carbsPct) / 4.0).roundToInt()
                        val fatGrams = ((calories * selectedMacroGoal.fatPct) / 9.0).roundToInt()

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MacroGramCard("Protein", "${proteinGrams}g", OrangePrimary, Modifier.weight(1f))
                                MacroGramCard("Carbs", "${carbsGrams}g", OrangeSecondary, Modifier.weight(1f))
                                MacroGramCard("Fats", "${fatGrams}g", Color(0xFF2196F3), Modifier.weight(1f))
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            ) {
                                Box(modifier = Modifier.weight(selectedMacroGoal.proteinPct.toFloat()).fillMaxHeight().background(OrangePrimary))
                                Box(modifier = Modifier.weight(selectedMacroGoal.carbsPct.toFloat()).fillMaxHeight().background(OrangeSecondary))
                                Box(modifier = Modifier.weight(selectedMacroGoal.fatPct.toFloat()).fillMaxHeight().background(Color(0xFF2196F3)))
                            }
                        }
                    }

                    3 -> {
                        Text("1-Rep Max (1RM) & Strength Grid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)

                        InputRow("Lifted Weight ($weightUnitName)", weightText) { weightText = it }
                        InputRow("Repetitions Done (1-12)", repsText) { repsText = it }

                        val oneRepMax = rawWeight * (1 + (reps / 30.0))
                        val rounded1RM = oneRepMax.roundToInt()

                        ResultBox(
                            primaryValue = "$rounded1RM $weightUnitName",
                            unit = "Estimated 1RM Max",
                            badgeText = "Max Strength Benchmark",
                            badgeColor = OrangePrimary
                        )

                        Text("Training Strength Percentages Grid", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "95% 1RM (~2 Reps)" to (rounded1RM * 0.95).roundToInt(),
                                "90% 1RM (~4 Reps)" to (rounded1RM * 0.90).roundToInt(),
                                "85% 1RM (~6 Reps)" to (rounded1RM * 0.85).roundToInt(),
                                "80% 1RM (~8 Reps)" to (rounded1RM * 0.80).roundToInt(),
                                "75% 1RM (~10 Reps)" to (rounded1RM * 0.75).roundToInt()
                            ).forEach { (pctLabel, targetWeight) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(pctLabel, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                    Text("$targetWeight $weightUnitName", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OrangeSecondary)
                                }
                            }
                        }
                    }

                    4 -> {
                        Text("Body Fat % (US Navy Method)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)

                        GenderSelector(isMale = isMaleGender) { isMaleGender = it }
                        InputRow(heightLabel, heightText) { heightText = it }
                        InputRow("Neck Circumference $circumLabelSuffix", neckText) { neckText = it }
                        InputRow("Waist Circumference $circumLabelSuffix", waistText) { waistText = it }
                        if (!isMaleGender) {
                            InputRow("Hip Circumference $circumLabelSuffix", hipText) { hipText = it }
                        }

                        val bodyFatPct = if (isMaleGender) {
                            val diff = (waistCm - neckCm).coerceAtLeast(1.0)
                            (86.010 * log10(diff) - 70.041 * log10(heightCm) + 36.76).coerceIn(4.0, 50.0)
                        } else {
                            val sum = (waistCm + hipCm - neckCm).coerceAtLeast(1.0)
                            (163.205 * log10(sum) - 97.684 * log10(heightCm) - 78.387).coerceIn(8.0, 55.0)
                        }

                        val roundedFatPct = (bodyFatPct * 10).roundToInt() / 10.0
                        val fatMassKg = weightKg * (bodyFatPct / 100.0)
                        val leanMassKg = weightKg - fatMassKg

                        val (fatCategory, categoryColor) = when {
                            bodyFatPct < 10.0 -> "Essential / Lean" to Color(0xFF2196F3)
                            bodyFatPct < 18.0 -> "Athletic / Fitness" to Color(0xFF4CAF50)
                            bodyFatPct < 25.0 -> "Average Health" to OrangeSecondary
                            else -> "High Fat %" to Color(0xFFFF5252)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ResultBox(
                                primaryValue = "$roundedFatPct %",
                                unit = "Estimated Body Fat",
                                badgeText = fatCategory,
                                badgeColor = categoryColor
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CalorieTargetChip("Lean Mass", "${if (isImperialUnits) (leanMassKg * 2.20462).roundToInt() else leanMassKg.roundToInt()} $weightUnitName", Color(0xFF4CAF50), Modifier.weight(1f))
                                CalorieTargetChip("Fat Mass", "${if (isImperialUnits) (fatMassKg * 2.20462).roundToInt() else fatMassKg.roundToInt()} $weightUnitName", OrangePrimary, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun mapMetricIcon(iconKey: String?): ImageVector {
    return when (iconKey?.uppercase()?.trim()) {
        "HEIGHT" -> Icons.Default.Height
        "WEIGHT" -> Icons.Default.MonitorWeight
        "BMI" -> Icons.Default.Speed
        "BMR", "TDEE", "DAILY-CALORIES", "CALORIES" -> Icons.Default.LocalFireDepartment
        "WATER" -> Icons.Default.WaterDrop
        "GENDER" -> Icons.Default.Person
        "GOAL", "FITNESS_GOAL" -> Icons.Default.EmojiEvents
        "ACTIVITY", "ACTIVITY_LEVEL" -> Icons.Default.AccessibilityNew
        else -> Icons.Default.Analytics
    }
}

@Composable
private fun ShimmerAnalyticsHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = alpha))
        )
    }
}

@Composable
private fun ShimmerBannerCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    GlassCardContainer {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
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

@Composable
private fun CalorieTargetChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
private fun MacroGramCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
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
private fun GlassCardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
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
