package com.muscleplay.app.feature.games.squat

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.muscleplay.app.feature.games.ExerciseHelpContents
import com.muscleplay.app.feature.games.ExerciseHelpDialog
import com.muscleplay.app.feature.games.ExerciseRulesDialog
import com.muscleplay.app.feature.games.ExerciseRulesPreferences
import com.muscleplay.app.feature.games.PoseSkeletonOverlay
import com.muscleplay.app.feature.games.core.ExercisePhase
import com.muscleplay.app.feature.games.core.ExerciseState
import com.muscleplay.app.ml.PoseLandmarkerHelper
import com.muscleplay.app.ml.PoseOverlayLandmark
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.milliseconds
import com.muscleplay.app.ml.liveness.LivenessChallengeEvaluator
import com.muscleplay.app.ml.liveness.LivenessConfig
import com.muscleplay.app.ml.liveness.LivenessStatus
import com.muscleplay.app.ml.liveness.toLivenessPose

private const val NO_PERSON_WARNING = "No person detected — move into view"

private const val LEGS_WARNING = "Move your legs into view"

@Composable
actual fun SquatCameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val rulesPreferences = remember(context) {
        ExerciseRulesPreferences(context.applicationContext)
    }

    var rulesDecisionReady by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }

    // Read the saved preference before composing anything camera-related.
    LaunchedEffect(Unit) {
        showRulesDialog = rulesPreferences.shouldShowRules("squat")
        rulesDecisionReady = true
    }

    if (!rulesDecisionReady) {
        return
    }

    if (showRulesDialog) {

        ExerciseRulesDialog(
            title = "Squat", rules = listOf(
                "Keep your full body inside the camera frame.",
                "Make sure your legs, knees, and feet are visible.",
                "Perform each squat slowly and with control.",
                "Stand upright to begin and complete each repetition.",
                "Stop if you experience pain, dizziness, or discomfort.",

                // Skeleton overlay guidance
                "Green skeleton: Your pose is detected reliably while standing.",
                "Yellow skeleton: The app is waiting for your starting position or tracking an active squat.",
                "Red skeleton: Your pose is unreliable. Adjust your position so your legs and joints are visible.",
                "Use the Show/Hide Skeleton button to turn the visual overlay on or off. Squat counting continues when the overlay is hidden."
            ), onContinue = { dontShowAgain ->
                rulesPreferences.setDontShowAgain(
                    exerciseId = "squat", dontShowAgain = dontShowAgain
                )
                showRulesDialog = false
            })

        return
    }

    // Camera resources and all exercise UI exist only after the rules gate.
    SquatCameraContent(onBack = onBack)
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun SquatCameraContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    var poseStatus by remember {
        mutableStateOf("Waiting for camera…")
    }

    var warningMessage by remember {
        mutableStateOf<String?>(null)
    }

    var resetMessage by remember {
        mutableStateOf(false)
    }

    // Show the verification-success popup only when verification first succeeds.
    var verificationSuccessMessage by remember {
        mutableStateOf(false)
    }
    var verificationSuccessToastKey by remember {
        mutableIntStateOf(0)
    }

    // Used to restart the success-message timer on every reset.
    var resetToastKey by remember {
        mutableIntStateOf(0)
    }

    val squatDetector = remember {
        SquatExerciseDetector()
    }
    val livenessEvaluator = remember {
        LivenessChallengeEvaluator(
            config = LivenessConfig(
                minimumVisibility = 0.45f,
                requiredStableFrames = 4,
                minimumFrameIntervalMs = 30L,
                challengeTimeoutMs = 15_000L,
                maxAttempts = 3
            )
        )
    }

    var livenessState by remember {
        mutableStateOf(livenessEvaluator.state)
    }
    var squatState by remember {
        mutableStateOf(SquatState())
    }


    val skeletonColor = when {
        !squatState.isPoseReliable -> Color(0xFFFF5252) // Red: unreliable pose

        squatState.phase == SquatPhase.WAITING_FOR_STANDING -> Color(0xFFFFC107) // Yellow: waiting for valid start

        squatState.phase == SquatPhase.SQUATTING -> Color(0xFFFFC107) // Yellow: squat in progress

        else -> Color(0xFF7CF5B4) // Green: reliable standing pose
    }

    var poseLandmarks by remember {
        mutableStateOf<List<PoseOverlayLandmark>>(emptyList())
    }
    var isHudVisible by remember {
        mutableStateOf(false)
    }
    var isSkeletonVisible by remember {
        mutableStateOf(false)
    }
    var showSquatHelp by remember {
        mutableStateOf(false)
    }
    val mainHandler = remember {
        Handler(Looper.getMainLooper())
    }

    val analysisExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraGranted = granted

        cameraError = if (!granted) {
            "Camera permission is required for squat detection."
        } else {
            null
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    LaunchedEffect(livenessEvaluator) {
        livenessEvaluator.start(android.os.SystemClock.uptimeMillis())
        livenessState = livenessEvaluator.state
    }
    // Delay warnings to ignore brief pose-detection interruptions.
    LaunchedEffect(poseStatus) {
        val isWarningStatus = poseStatus == LEGS_WARNING || poseStatus == NO_PERSON_WARNING

        if (isWarningStatus) {
            delay(800.milliseconds)

            // This effect is cancelled if poseStatus changes
            // while the delay is running.
            if (poseStatus == LEGS_WARNING || poseStatus == NO_PERSON_WARNING) {
                warningMessage = poseStatus
            }
        } else {
            warningMessage = null
        }
    }

    // Hide the reset-success message after two seconds.
    // Changing resetToastKey restarts the timer on each reset.
    LaunchedEffect(resetToastKey) {
        if (resetMessage) {
            delay(2000.milliseconds)
            resetMessage = false
        }
    }

    // Hide the verification-success popup after two seconds.
    // A new key restarts the timer for a later successful verification.
    LaunchedEffect(verificationSuccessToastKey) {
        if (verificationSuccessMessage) {
            delay(3000.milliseconds)
            verificationSuccessMessage = false
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!cameraGranted) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                cameraError ?: "MusclePlay needs camera access to detect your squats."
            )

            Button(
                onClick = {
                    cameraError = null
                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }) {
                Text("Grant camera permission")
            }

            Button(onClick = onBack) {
                Text("Back")
            }
        }

        return
    }

    DisposableEffect(previewView, lifecycleOwner) {
        var poseHelper: PoseLandmarkerHelper? = null
        var cameraProvider: ProcessCameraProvider? = null

        try {
            poseHelper = PoseLandmarkerHelper(
                context = context.applicationContext,

                onPoseDetected = { pose ->
                    mainHandler.post {
                        val nowMs = SystemClock.uptimeMillis()

                        if (pose == null) {
                            livenessState = livenessEvaluator.onPoseLost()
                            squatState = squatDetector.onPoseLost().toSquatUiState()
                            poseStatus = NO_PERSON_WARNING
                        } else {
                            // Repetition counting is allowed only when the evaluator explicitly permits it.
                            if (livenessState.status == LivenessStatus.VERIFIED && livenessState.canCountRepetitions) {
                                val updatedState = squatDetector.processPose(pose).toSquatUiState()

                                squatState = updatedState
                                poseStatus = when {
                                    !updatedState.isPoseReliable -> LEGS_WARNING
                                    updatedState.phase == SquatPhase.SQUATTING -> "Squat detected — stand up to complete the rep"

                                    else -> "Pose detected — get ready to squat"
                                }
                            } else {
                                when (livenessState.status) {
                                    LivenessStatus.NOT_STARTED,
                                    LivenessStatus.CHALLENGE_REQUIRED,
                                    LivenessStatus.CHALLENGE_IN_PROGRESS,
                                        -> {
                                        val previousStatus = livenessState.status
                                        livenessState = livenessEvaluator.process(
                                            pose.toLivenessPose(), nowMs
                                        )

                                        // Trigger the popup only on the transition to successful verification.
                                        val verificationJustSucceeded =
                                            previousStatus != LivenessStatus.VERIFIED && livenessState.status == LivenessStatus.VERIFIED && livenessState.canCountRepetitions

                                        if (verificationJustSucceeded) {
                                            verificationSuccessMessage = true
                                            verificationSuccessToastKey++
                                        }

                                        poseStatus = livenessState.instruction
                                    }

                                    LivenessStatus.PAUSED,
                                    LivenessStatus.FAILED,
                                        -> {
                                        poseStatus = livenessState.instruction
                                    }

                                    LivenessStatus.VERIFIED -> {
                                        // Defensive fallback: VERIFIED without permission to count.
                                        poseStatus = "Preparing verification..."
                                    }
                                }

                                // Never send verification frames to the repetition counter.
                                squatState = squatDetector.onPoseLost().toSquatUiState()
                            }
                        }
                    }
                },

                onError = { message ->
                    mainHandler.post {
                        cameraError = message
                    }
                },
                onLandmarksDetected = { landmarks ->
                    mainHandler.post {
                        poseLandmarks = landmarks.orEmpty()
                    }
                },
            )

            val providerFuture = ProcessCameraProvider.getInstance(context)

            providerFuture.addListener(
                {
                    try {
                        val provider = providerFuture.get()
                        cameraProvider = provider

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val analysis = ImageAnalysis.Builder().setOutputImageFormat(
                            ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
                        ).setBackpressureStrategy(
                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        ).build()

                        analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                            try {
                                val bitmap = imageProxy.toRgbaBitmap()

                                val mpImage = BitmapImageBuilder(bitmap).build()

                                poseHelper.detect(
                                    mpImage, SystemClock.uptimeMillis()
                                )
                            } catch (exception: Exception) {
                                Log.e(
                                    "SquatCamera", "Frame processing failed", exception
                                )
                            } finally {
                                imageProxy.close()
                            }
                        }

                        provider.unbindAll()

                        provider.bindToLifecycle(
                            lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis
                        )
                    } catch (exception: Exception) {
                        mainHandler.post {
                            cameraError = exception.message ?: "Unable to start camera"
                        }
                    }
                }, ContextCompat.getMainExecutor(context)
            )
        } catch (exception: Exception) {
            cameraError = exception.message ?: "Unable to initialize pose detection"
        }

        onDispose {
            cameraProvider?.unbindAll()
            poseHelper?.close()
            analysisExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Camera preview.
        AndroidView(
            factory = { previewView }, modifier = Modifier.fillMaxSize()
        )


        // Verification overlay: progress indicator, challenge instructions, and retry action.
        val isVerified =
            livenessState.status == LivenessStatus.VERIFIED && livenessState.canCountRepetitions
        val isVerificationActive =
            livenessState.status == LivenessStatus.NOT_STARTED || livenessState.status == LivenessStatus.CHALLENGE_REQUIRED || livenessState.status == LivenessStatus.CHALLENGE_IN_PROGRESS || livenessState.status == LivenessStatus.PAUSED || livenessState.status == LivenessStatus.FAILED
        val shouldShowSkeleton = isVerificationActive || isSkeletonVisible
        val canRetry =
            livenessState.status == LivenessStatus.PAUSED || livenessState.status == LivenessStatus.FAILED


// Layer 2: Live MediaPipe skeleton
        if (shouldShowSkeleton) {
            PoseSkeletonOverlay(
                landmarks = poseLandmarks,
                modifier = Modifier.matchParentSize(),
                mirrorHorizontally = true,
                rotateLandmarks = true,
                minimumVisibility = 0.5f,
                skeletonColor = skeletonColor
            )
        }
        if (!isVerified) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp)
                    .fillMaxWidth().background(Color(0xCC101820), RoundedCornerShape(16.dp)).border(
                        width = 1.dp, color = Color(0x557CF5B4), shape = RoundedCornerShape(16.dp)
                    ).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isVerificationActive) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(38.dp),
                        color = Color(0xFF7CF5B4),
                        strokeWidth = 3.dp
                    )

                    Text(
                        text = if (livenessState.status == LivenessStatus.CHALLENGE_IN_PROGRESS) "Verifying your movement..." else "Preparing verification...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = livenessState.instruction,
                        color = Color(0xFFE0E7EB),
                        fontSize = 18.sp
                    )

                    livenessState.reason?.let { reason ->
                        Text(
                            text = reason, color = Color(0xFFFFD180), fontSize = 12.sp
                        )
                    }
                }

                if (canRetry) {
                    Text(
                        text = if (livenessState.status == LivenessStatus.FAILED) {
                            "Verification failed"
                        } else {
                            "Verification paused"
                        }, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = livenessState.reason ?: livenessState.instruction,
                        color = Color(0xFFE0E7EB),
                        fontSize = 14.sp
                    )

                    Button(
                        onClick = {
                            livenessEvaluator.restart(SystemClock.uptimeMillis())
                            livenessState = livenessEvaluator.state
                            squatState = squatDetector.reset().toSquatUiState()
                            poseStatus = livenessState.instruction
                            warningMessage = null
                            isSkeletonVisible = true
                        }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7CF5B4), contentColor = Color(0xFF101820)
                        )
                    ) {
                        Text("Revalidate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Verification-success popup. It disappears automatically after two seconds.
        if (verificationSuccessMessage) {
            Row(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp).background(
                    color = Color(0xEE163D2D), shape = RoundedCornerShape(12.dp)
                ).border(
                    width = 1.dp, color = Color(0xFF7CF5B4), shape = RoundedCornerShape(12.dp)
                ).padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✓",
                    color = Color(0xFF7CF5B4),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Verification Successful!",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Start exercising!", color = Color(0xFFB8D8D0), fontSize = 13.sp
                    )
                }
            }
        }

        // Reset-success popup.
        // IMPORTANT: This must remain inside the Box.
        if (resetMessage) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp).background(
                    color = Color(0xEE163D2D), shape = RoundedCornerShape(12.dp)
                ).border(
                    width = 1.dp, color = Color(0xFF7CF5B4), shape = RoundedCornerShape(12.dp)
                ).padding(
                    horizontal = 16.dp, vertical = 12.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✓",
                    color = Color(0xFF7CF5B4),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Reps reset successfully!",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Warning popup.
        // Shown only after a warning persists for 800 ms.
        warningMessage?.let { message ->
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(
                    top = 8.dp, start = 8.dp, end = 100.dp
                ).background(
                    color = Color(0xEE332719), shape = RoundedCornerShape(12.dp)
                ).border(
                    width = 1.dp, color = Color(0xFFFFB74D), shape = RoundedCornerShape(12.dp)
                ).padding(
                    horizontal = 14.dp, vertical = 10.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠", color = Color(0xFFFFB74D), fontSize = 18.sp
                )

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Always-visible reps counter at the top-right.
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(
                color = Color(0xFF101820), shape = RoundedCornerShape(18.dp)
            ).padding(
                horizontal = 24.dp, vertical = 12.dp
            ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "REPS",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFB8D8D0)
            )

            Text(
                text = squatState.repetitions.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF7CF5B4)
            )

            Spacer(modifier = Modifier.padding(top = 4.dp))

            Button(
                onClick = {
                    showSquatHelp = true
                },
                modifier = Modifier.size(35.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF34434E), contentColor = Color.White
                )
            ) {
                Text(
                    text = "?", fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            }
        }
        if (showSquatHelp) {
            ExerciseHelpDialog(
                content = ExerciseHelpContents.squat, onDismiss = {
                    showSquatHelp = false
                })
        }
        // Bottom exercise Options panel.
        if (isVerified && isHudVisible) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(
                    horizontal = 12.dp, vertical = 12.dp
                ).background(
                    color = Color(0xFF101820), shape = RoundedCornerShape(18.dp)
                ).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Knee angle.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Knee angle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB8D8D0)
                    )

                    Text(text = squatState.kneeAngleDegrees?.let { "${it.toInt()}°" } ?: "--",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White)
                }

                // Current squat phase.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Squat Phase",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB8D8D0)
                    )

                    Text(
                        text = squatState.phase.name.replace('_', ' ').lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF7CF5B4)
                    )
                }


                // Hide Options and Back buttons.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isHudVisible = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7CF5B4), contentColor = Color(0xFF101820)
                        )
                    ) {
                        Text("Hide Options")
                    }

                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF34434E), contentColor = Color.White
                        )
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        isSkeletonVisible = !isSkeletonVisible
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF24313B), contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isSkeletonVisible) {
                            "Hide Skeleton"
                        } else {
                            "Show Skeleton"
                        }
                    )
                }

                // Reset button.
                Button(
                    onClick = {
                        squatState = squatDetector.reset().toSquatUiState()
                        poseStatus = "Counter reset — stand upright to begin"
                        cameraError = null

                        // Show the success popup and restart its timer.
                        resetMessage = true
                        resetToastKey++
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF24313B), contentColor = Color.White
                    )
                ) {
                    Text("Reset")
                }
            }
        } else if (isVerified) {
            Button(
                onClick = {
                    isHudVisible = true
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7CF5B4), contentColor = Color(0xFF101820)
                )
            ) {
                Text("Show Options")
            }
        }
    }
}

private fun ExerciseState.toSquatUiState(): SquatState {
    return SquatState(
        repetitions = repetitions,
        phase = when (phase) {
            ExercisePhase.WAITING_FOR_START -> SquatPhase.WAITING_FOR_STANDING

            ExercisePhase.READY -> SquatPhase.STANDING

            ExercisePhase.ACTIVE -> SquatPhase.SQUATTING

            ExercisePhase.COMPLETED,
            ExercisePhase.UNKNOWN,
                -> SquatPhase.WAITING_FOR_STANDING
        },
        kneeAngleDegrees = primaryAngleDegrees,
        isPoseReliable = isPoseReliable,
    )
}

private fun ImageProxy.toRgbaBitmap(): Bitmap {
    require(planes.isNotEmpty()) {
        "Camera frame has no image planes"
    }

    val plane = planes[0]
    val buffer = plane.buffer.duplicate()
    buffer.rewind()

    val expectedRowBytes = width * 4

    // Fast path: tightly packed RGBA pixels can be copied directly.
    if (plane.pixelStride == 4 && plane.rowStride == expectedRowBytes && buffer.remaining() >= height * expectedRowBytes) {
        return createBitmap(width, height).apply {
            copyPixelsFromBuffer(buffer)
        }
    }

    // Fallback for padded rows.
    val rgba = ByteArray(buffer.remaining())
    buffer.get(rgba)

    val pixels = IntArray(width * height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * plane.rowStride + x * plane.pixelStride

            if (index + 3 < rgba.size) {
                val red = rgba[index].toInt() and 0xFF
                val green = rgba[index + 1].toInt() and 0xFF
                val blue = rgba[index + 2].toInt() and 0xFF
                val alpha = rgba[index + 3].toInt() and 0xFF

                pixels[y * width + x] = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
            }
        }
    }

    return createBitmap(width, height).apply {
        setPixels(pixels, 0, width, 0, 0, width, height)
    }
}
