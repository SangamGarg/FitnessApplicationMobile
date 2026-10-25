package com.muscleplay.app.feature.games.pushup

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

private const val PUSH_UP_TAG = "PushUpCamera"
private const val NO_PERSON_WARNING = "No person detected — move into view"
private const val ARMS_WARNING = "Move your shoulder, elbow and wrist into view"

private val PushUpGreen = Color(0xFF7CF5B4)
private val PushUpDark = Color(0xFF101820)
private val PushUpMuted = Color(0xFFB8D8D0)
private val PushUpYellow = Color(0xFFFFC107)
private val PushUpRed = Color(0xFFFF5252)
private val PushUpPanel = Color(0xFF24313B)
private val PushUpButton = Color(0xFF34434E)

@Composable
actual fun PushUpCameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val rulesPreferences = remember(context) {
        ExerciseRulesPreferences(context.applicationContext)
    }

    var rulesDecisionReady by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showRulesDialog = rulesPreferences.shouldShowRules("pushup")
        rulesDecisionReady = true
    }

    if (!rulesDecisionReady) return

    if (showRulesDialog) {
        ExerciseRulesDialog(
            title = "Push-up",
            rules = listOf(
                "Position the camera so your upper body and arms are visible.",
                "Use a side-on position for more reliable elbow-angle detection.",
                "Keep your shoulders, elbows and wrists visible throughout the exercise.",
                "Begin with your arms straight.",
                "Lower your body with control, then push back up to complete a repetition.",
                "Avoid rushing or forcing a movement that causes pain or discomfort.",
                "Green skeleton: your pose is detected reliably.",
                "Yellow skeleton: tracking your movement or waiting for the starting position.",
                "Red skeleton: your pose is unreliable. Adjust your position.",
                "Use Show/Hide Skeleton to control the visual overlay. Counting continues when it is hidden.",
                "Complete liveness verification before repetitions can be counted.",
                "Follow the on-screen verification instruction and keep your upper body and arms visible.",
                "If verification pauses or fails, raise your right hand above your right shoulder and hold it for about 1.5 seconds to restart verification.",
                "Alternatively, tap Revalidate when the button is displayed.",
                "Repetition counting resumes only after verification succeeds and counting is enabled."
            ),
            onContinue = { dontShowAgain ->
                rulesPreferences.setDontShowAgain(
                    exerciseId = "pushup",
                    dontShowAgain = dontShowAgain
                )
                showRulesDialog = false
            }
        )
        return
    }

    PushUpCameraContent(onBack = onBack)
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun PushUpCameraContent(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var cameraError by remember { mutableStateOf<String?>(null) }
    var poseStatus by remember { mutableStateOf("Waiting for camera…") }
    var warningMessage by remember { mutableStateOf<String?>(null) }

    var resetMessage by remember { mutableStateOf(false) }
    var resetToastKey by remember { mutableIntStateOf(0) }

    var verificationSuccessMessage by remember { mutableStateOf(false) }
    var verificationSuccessToastKey by remember { mutableIntStateOf(0) }

    // Right-hand gesture revalidation: 1.5-second hold with a brief dropout grace period.
    var rightHandRaisedSinceMs by remember { mutableStateOf<Long?>(null) }
    var rightHandLastSeenMs by remember { mutableStateOf<Long?>(null) }
    var rightHandRaisedFrames by remember { mutableIntStateOf(0) }
    var lastGestureRevalidationMs by remember { mutableStateOf(0L) }

    val detector = remember { PushUpExerciseDetector() }
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
    var livenessState by remember { mutableStateOf(livenessEvaluator.state) }

    var exerciseState by remember {
        mutableStateOf(ExerciseState())
    }

    var poseLandmarks by remember {
        mutableStateOf<List<PoseOverlayLandmark>>(emptyList())
    }

    var isHudVisible by remember { mutableStateOf(false) }
    var isSkeletonVisible by remember { mutableStateOf(false) }
    var showPushUpHelp by remember { mutableStateOf(false) }

    val skeletonColor = when {
        !exerciseState.isPoseReliable -> PushUpRed
        exerciseState.phase == ExercisePhase.WAITING_FOR_START -> PushUpYellow
        exerciseState.phase == ExercisePhase.ACTIVE -> PushUpYellow
        else -> PushUpGreen
    }

    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraGranted = granted
        cameraError = if (granted) {
            null
        } else {
            "Camera permission is required for push-up detection."
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(livenessEvaluator) {
        livenessEvaluator.start(SystemClock.uptimeMillis())
        livenessState = livenessEvaluator.state
    }

    LaunchedEffect(verificationSuccessToastKey) {
        if (verificationSuccessMessage) {
            delay(3000.milliseconds)
            verificationSuccessMessage = false
        }
    }

    LaunchedEffect(poseStatus) {
        val isWarningStatus = poseStatus == NO_PERSON_WARNING || poseStatus == ARMS_WARNING

        if (isWarningStatus) {
            delay(800.milliseconds)

            if (poseStatus == NO_PERSON_WARNING || poseStatus == ARMS_WARNING) {
                warningMessage = poseStatus
            }
        } else {
            warningMessage = null
        }
    }

    LaunchedEffect(resetToastKey) {
        if (resetMessage) {
            delay(2000.milliseconds)
            resetMessage = false
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
                cameraError ?: "MusclePlay needs camera access to detect your push-ups."
            )

            Button(
                onClick = {
                    cameraError = null
                    permissionLauncher.launch(Manifest.permission.CAMERA)
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
        var disposed = false

        try {
            poseHelper = PoseLandmarkerHelper(
                context = context.applicationContext,
                onPoseDetected = { pose ->
                    mainHandler.post {
                        if (disposed) return@post

                        val nowMs = SystemClock.uptimeMillis()
                        if (pose == null) {
                            livenessState = livenessEvaluator.onPoseLost()
                            exerciseState = detector.onPoseLost()
                            poseStatus = NO_PERSON_WARNING
                        } else if (
                            livenessState.status == LivenessStatus.VERIFIED &&
                            livenessState.canCountRepetitions
                        ) {
                            val updatedState = detector.processPose(pose)
                            exerciseState = updatedState
                            poseStatus = when {
                                !updatedState.isPoseReliable -> ARMS_WARNING
                                updatedState.phase == ExercisePhase.ACTIVE ->
                                    "Push-up detected — push back up to complete the rep"
                                else -> updatedState.feedback
                            }
                        } else {
                            when (livenessState.status) {
                                LivenessStatus.NOT_STARTED,
                                LivenessStatus.CHALLENGE_REQUIRED,
                                LivenessStatus.CHALLENGE_IN_PROGRESS -> {
                                    val previousStatus = livenessState.status
                                    livenessState = livenessEvaluator.process(
                                        pose.toLivenessPose(), nowMs
                                    )
                                    val verificationJustSucceeded =
                                        previousStatus != LivenessStatus.VERIFIED &&
                                                livenessState.status == LivenessStatus.VERIFIED &&
                                                livenessState.canCountRepetitions
                                    if (verificationJustSucceeded) {
                                        verificationSuccessMessage = true
                                        verificationSuccessToastKey++
                                    }
                                    poseStatus = livenessState.instruction
                                }
                                LivenessStatus.PAUSED,
                                LivenessStatus.FAILED -> {
                                    poseStatus = livenessState.instruction
                                }
                                LivenessStatus.VERIFIED -> {
                                    poseStatus = "Preparing verification..."
                                }
                            }
                            // Preserve the detector's existing repetition count while verifying.
                            exerciseState = detector.onPoseLost()
                        }
                    }
                },
                onError = { message ->
                    mainHandler.post {
                        if (!disposed) cameraError = message
                    }
                },
                onLandmarksDetected = { landmarks ->
                    mainHandler.post {
                        if (disposed) return@post

                        poseLandmarks = landmarks.orEmpty()
                        val currentLandmarks = landmarks.orEmpty()
                        val nowMs = SystemClock.uptimeMillis()
                        val canRetryNow =
                            livenessState.status == LivenessStatus.PAUSED ||
                                    livenessState.status == LivenessStatus.FAILED

                        if (canRetryNow) {
                            val rightShoulder = currentLandmarks.getOrNull(12)
                            val rightWrist = currentLandmarks.getOrNull(16)
                            val landmarksReliable =
                                rightShoulder != null && rightWrist != null &&
                                        rightShoulder.visibility >= 0.45f &&
                                        rightWrist.visibility >= 0.45f
                            val isRightHandRaised =
                                landmarksReliable &&
                                        rightWrist.y < rightShoulder.y - 0.05f

                            if (isRightHandRaised) {
                                rightHandLastSeenMs = nowMs
                                if (rightHandRaisedSinceMs == null) {
                                    rightHandRaisedSinceMs = nowMs
                                    rightHandRaisedFrames = 1
                                } else {
                                    rightHandRaisedFrames++
                                }

                                val heldDurationMs = nowMs - (rightHandRaisedSinceMs ?: nowMs)
                                val enoughFrames = rightHandRaisedFrames >= 15
                                val enoughTime = heldDurationMs >= 1500L
                                val cooldownFinished =
                                    nowMs - lastGestureRevalidationMs >= 2000L

                                if (enoughFrames && enoughTime && cooldownFinished) {
                                    lastGestureRevalidationMs = nowMs
                                    rightHandRaisedSinceMs = null
                                    rightHandLastSeenMs = null
                                    rightHandRaisedFrames = 0
                                    livenessEvaluator.restart(nowMs)
                                    livenessState = livenessEvaluator.state
                                    poseStatus = livenessState.instruction
                                    warningMessage = null
                                }
                            } else {
                                val lastSeen = rightHandLastSeenMs
                                // Tolerate brief landmark dropouts for up to 400 ms.
                                if (lastSeen == null || nowMs - lastSeen > 400L) {
                                    rightHandRaisedSinceMs = null
                                    rightHandLastSeenMs = null
                                    rightHandRaisedFrames = 0
                                }
                            }
                        } else {
                            rightHandRaisedSinceMs = null
                            rightHandLastSeenMs = null
                            rightHandRaisedFrames = 0
                        }
                    }
                })

            val providerFuture = ProcessCameraProvider.getInstance(context)

            providerFuture.addListener(
                {
                    if (disposed) return@addListener

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
                                    PUSH_UP_TAG, "Frame processing failed", exception
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
                            if (!disposed) {
                                cameraError = exception.message ?: "Unable to start camera"
                            }
                        }
                    }
                }, ContextCompat.getMainExecutor(context)
            )
        } catch (exception: Exception) {
            cameraError = exception.message ?: "Unable to initialize pose detection"
        }

        onDispose {
            disposed = true
            cameraProvider?.unbindAll()
            poseHelper?.close()
            analysisExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView }, modifier = Modifier.fillMaxSize()
        )

        val isVerified =
            livenessState.status == LivenessStatus.VERIFIED &&
                    livenessState.canCountRepetitions
        val canRetry =
            livenessState.status == LivenessStatus.PAUSED ||
                    livenessState.status == LivenessStatus.FAILED
        val shouldShowSkeleton = !isVerified || isSkeletonVisible

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

        // Liveness verification overlay belongs in the Compose Box, not inside LaunchedEffect.
        if (!isVerified) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .background(Color(0xCC101820), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x557CF5B4), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!canRetry) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(38.dp), color = PushUpGreen, strokeWidth = 3.dp
                    )
                    Text(
                        text = if (livenessState.status == LivenessStatus.CHALLENGE_IN_PROGRESS)
                            "Verifying your movement..." else "Preparing verification...",
                        color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = livenessState.instruction,
                        color = Color(0xFFE0E7EB), fontSize = 16.sp
                    )
                    livenessState.reason?.let { reason ->
                        Text(text = reason, color = Color(0xFFFFD180), fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = if (livenessState.status == LivenessStatus.FAILED)
                            "Verification failed" else "Verification paused",
                        color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = livenessState.reason ?: livenessState.instruction,
                        color = Color(0xFFE0E7EB), fontSize = 14.sp
                    )
                    Button(
                        onClick = {
                            val nowMs = SystemClock.uptimeMillis()
                            rightHandRaisedSinceMs = null
                            rightHandLastSeenMs = null
                            rightHandRaisedFrames = 0
                            livenessEvaluator.restart(nowMs)
                            livenessState = livenessEvaluator.state
                            poseStatus = livenessState.instruction
                            warningMessage = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PushUpGreen, contentColor = PushUpDark
                        )
                    ) { Text("Revalidate", fontWeight = FontWeight.Bold) }
                    Text(
                        text = "Raise and hold your right hand above your shoulder for 1.5 seconds to revalidate, or tap Revalidate.",
                        color = PushUpMuted, fontSize = 13.sp
                    )
                }
            }
        }

        if (verificationSuccessMessage) {
            Row(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp)
                    .background(Color(0xEE163D2D), RoundedCornerShape(12.dp))
                    .border(1.dp, PushUpGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✓", color = PushUpGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Verification Successful!", color = Color.White,
                        fontSize = 15.sp, fontWeight = FontWeight.Bold
                    )
                    Text("Start exercising!", color = PushUpMuted, fontSize = 13.sp)
                }
            }
        }

        if (resetMessage) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp).background(
                    color = Color(0xEE163D2D), shape = RoundedCornerShape(12.dp)
                ).border(
                    width = 1.dp, color = PushUpGreen, shape = RoundedCornerShape(12.dp)
                ).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✓", color = PushUpGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Reps reset successfully!",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        warningMessage?.let { message ->
            Row(
                modifier = Modifier.align(Alignment.TopStart)
                    .padding(top = 8.dp, start = 8.dp, end = 100.dp).background(
                        color = Color(0xEE332719), shape = RoundedCornerShape(12.dp)
                    ).border(
                        width = 1.dp, color = Color(0xFFFFB74D), shape = RoundedCornerShape(12.dp)
                    ).padding(horizontal = 14.dp, vertical = 10.dp),
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

        // Repetition counter and help button.
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(
                color = PushUpDark, shape = RoundedCornerShape(18.dp)
            ).padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "REPS", style = MaterialTheme.typography.labelSmall, color = PushUpMuted
            )

            Text(
                text = exerciseState.repetitions.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = PushUpGreen
            )

            Spacer(modifier = Modifier.padding(top = 4.dp))

            Button(
                onClick = { showPushUpHelp = true },
                modifier = Modifier.size(35.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PushUpButton, contentColor = Color.White
                )
            ) {
                Text(
                    text = "?", fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            }
        }

        if (showPushUpHelp) {
            // Uses the existing exercise-help content for push-ups.
            ExerciseHelpDialog(
                content = ExerciseHelpContents.pushUp, onDismiss = { showPushUpHelp = false })
        }

        // Bottom options panel, styled like the squat screen.
        if (isVerified && isHudVisible) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp).background(
                        color = PushUpDark, shape = RoundedCornerShape(18.dp)
                    ).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = statusMessageForHud(exerciseState, poseStatus),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (exerciseState.isPoseReliable) PushUpGreen
                    else PushUpYellow
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Arm angle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushUpMuted
                    )
                    Text(text = exerciseState.primaryAngleDegrees?.let { "${it.toInt()}°" } ?: "--",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Push-up Phase",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushUpMuted
                    )
                    Text(
                        text = exerciseState.phase.name.replace('_', ' ').lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = PushUpGreen
                    )
                }

                cameraError?.let { error ->
                    Text(
                        text = error, color = PushUpRed, style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isHudVisible = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PushUpGreen, contentColor = PushUpDark
                        )
                    ) {
                        Text("Hide Options")
                    }

                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PushUpButton, contentColor = Color.White
                        )
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = { isSkeletonVisible = !isSkeletonVisible },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PushUpPanel, contentColor = Color.White
                    )
                ) {
                    Text(
                        if (isSkeletonVisible) "Hide Skeleton"
                        else "Show Skeleton"
                    )
                }

                Button(
                    onClick = {
                        exerciseState = detector.reset()
                        poseStatus = "Counter reset — straighten your arms to begin"
                        cameraError = null
                        resetMessage = true
                        resetToastKey++
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PushUpPanel, contentColor = Color.White
                    )
                ) {
                    Text("Reset")
                }
            }
        } else if (isVerified) {
            Button(
                onClick = { isHudVisible = true },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PushUpGreen, contentColor = PushUpDark
                )
            ) {
                Text("Show Options")
            }
        }
    }
}

private fun statusMessageForHud(
    state: ExerciseState,
    cameraStatus: String,
): String {
    return when (cameraStatus) {
        NO_PERSON_WARNING -> NO_PERSON_WARNING
        ARMS_WARNING -> ARMS_WARNING
        else -> state.feedback
    }
}

private fun ImageProxy.toRgbaBitmap(): Bitmap {
    require(planes.isNotEmpty()) {
        "Camera frame has no image planes"
    }

    val plane = planes[0]
    val buffer = plane.buffer.duplicate()
    buffer.rewind()

    val expectedRowBytes = width * 4

    if (plane.pixelStride == 4 && plane.rowStride == expectedRowBytes && buffer.remaining() >= height * expectedRowBytes) {
        return createBitmap(width, height).apply {
            copyPixelsFromBuffer(buffer)
        }
    }

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