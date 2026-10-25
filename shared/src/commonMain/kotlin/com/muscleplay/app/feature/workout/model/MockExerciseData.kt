package com.muscleplay.app.feature.workout.model

enum class ExerciseDifficulty(val label: String) {
    ALL("All"),
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

enum class ExerciseEquipment(val label: String) {
    ALL("All"),
    BODYWEIGHT("Bodyweight"),
    DUMBBELL("Dumbbell"),
    BARBELL("Barbell"),
    CABLE("Cable"),
    MACHINE("Machine")
}

data class Exercise(
    val id: String,
    val name: String,
    val primaryMuscleGroup: String,
    val secondaryMuscleGroups: List<String>,
    val difficulty: ExerciseDifficulty,
    val equipment: ExerciseEquipment,
    val movementPattern: String,
    val supportsAiTracking: Boolean,
    val description: String,
    val steps: List<String>,
    val defaultSets: Int = 3,
    val defaultTargetReps: Int = 10,
    val defaultWeightKg: Double = 0.0
)

data class SetLog(
    val setNumber: Int,
    val targetWeightKg: Double,
    val targetReps: Int,
    var actualWeightKg: Double,
    var actualReps: Int,
    var isCompleted: Boolean = false
)

data class SessionExercise(
    val exercise: Exercise,
    val sets: MutableList<SetLog>
)

data class ActiveWorkoutSession(
    val id: String,
    val routineTitle: String,
    val exercises: List<SessionExercise>,
    var currentExerciseIndex: Int = 0,
    var startTimeSeconds: Long = 0L
)

data class WorkoutSummaryData(
    val workoutTitle: String,
    val durationSeconds: Long,
    val totalVolumeKg: Double,
    val totalReps: Int,
    val completedSetsCount: Int,
    val totalSetsCount: Int,
    val exercises: List<SessionExercise>
)

val mockExercisesList = listOf(
    Exercise(
        id = "squat",
        name = "Bodyweight Squat",
        primaryMuscleGroup = "Quadriceps",
        secondaryMuscleGroups = listOf("Glutes", "Hamstrings", "Core"),
        difficulty = ExerciseDifficulty.BEGINNER,
        equipment = ExerciseEquipment.BODYWEIGHT,
        movementPattern = "Squat",
        supportsAiTracking = true,
        description = "Fundamental lower body exercise that targets quadriceps, glutes, and core stability.",
        steps = listOf(
            "Stand with feet shoulder-width apart, toes pointed slightly outward.",
            "Brace your core and initiate movement by bending at hips and knees.",
            "Lower down until thighs are parallel to the ground while keeping chest lifted.",
            "Drive through heels to return forcefully to starting position."
        ),
        defaultSets = 3,
        defaultTargetReps = 12,
        defaultWeightKg = 0.0
    ),
    Exercise(
        id = "pushup",
        name = "Standard Push-ups",
        primaryMuscleGroup = "Chest",
        secondaryMuscleGroups = listOf("Triceps", "Front Delts", "Core"),
        difficulty = ExerciseDifficulty.BEGINNER,
        equipment = ExerciseEquipment.BODYWEIGHT,
        movementPattern = "Push",
        supportsAiTracking = true,
        description = "Classic upper body compound movement for developing chest, triceps, and core strength.",
        steps = listOf(
            "Place hands slightly wider than shoulder-width in high plank position.",
            "Keep body in a straight line from head to heels.",
            "Lower chest toward floor by bending elbows at a 45-degree angle.",
            "Push firmly away from floor back to starting plank position."
        ),
        defaultSets = 3,
        defaultTargetReps = 15,
        defaultWeightKg = 0.0
    ),
    Exercise(
        id = "pullup",
        name = "Overhand Pull-ups",
        primaryMuscleGroup = "Lats",
        secondaryMuscleGroups = listOf("Biceps", "Upper Back", "Rear Delts"),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        equipment = ExerciseEquipment.BODYWEIGHT,
        movementPattern = "Pull",
        supportsAiTracking = false,
        description = "Challenging upper body pull exercise focusing on upper back width and grip strength.",
        steps = listOf(
            "Grasp pull-up bar with overhand grip slightly wider than shoulder width.",
            "Hang with arms fully extended and engage shoulder blades down and back.",
            "Pull chest up toward bar until chin clears bar height.",
            "Lower back down with control to full arm extension."
        ),
        defaultSets = 3,
        defaultTargetReps = 8,
        defaultWeightKg = 0.0
    ),
    Exercise(
        id = "db_curl",
        name = "Dumbbell Bicep Curls",
        primaryMuscleGroup = "Biceps",
        secondaryMuscleGroups = listOf("Brachialis", "Forearms"),
        difficulty = ExerciseDifficulty.BEGINNER,
        equipment = ExerciseEquipment.DUMBBELL,
        movementPattern = "Pull",
        supportsAiTracking = true,
        description = "Isolated upper arm exercise for building bicep peak and arm hypertrophy.",
        steps = listOf(
            "Stand upright holding dumbbells at sides with palms facing forward.",
            "Keep elbows close to torso and curl weights upward toward shoulders.",
            "Squeeze biceps firmly at top contraction.",
            "Slowly lower dumbbells back down to starting position."
        ),
        defaultSets = 3,
        defaultTargetReps = 12,
        defaultWeightKg = 12.5
    ),
    Exercise(
        id = "bench_press",
        name = "Barbell Bench Press",
        primaryMuscleGroup = "Chest",
        secondaryMuscleGroups = listOf("Triceps", "Anterior Delts"),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        equipment = ExerciseEquipment.BARBELL,
        movementPattern = "Push",
        supportsAiTracking = false,
        description = "Premier chest building compound movement for pushing power and muscle density.",
        steps = listOf(
            "Lie flat on bench with feet firmly planted on floor.",
            "Unrack barbell with medium-wide grip and align above upper chest.",
            "Lower barbell steadily down to mid-chest touch.",
            "Press bar powerfully upward back to starting locked position."
        ),
        defaultSets = 4,
        defaultTargetReps = 8,
        defaultWeightKg = 60.0
    ),
    Exercise(
        id = "plank",
        name = "Core Plank Hold",
        primaryMuscleGroup = "Abs / Core",
        secondaryMuscleGroups = listOf("Glutes", "Shoulders", "Lower Back"),
        difficulty = ExerciseDifficulty.BEGINNER,
        equipment = ExerciseEquipment.BODYWEIGHT,
        movementPattern = "Isometrics",
        supportsAiTracking = true,
        description = "Isometric core stability exercise strengthening abdominal wall and pelvic posture.",
        steps = listOf(
            "Rest forearms on mat with elbows positioned beneath shoulders.",
            "Extend legs straight back, resting on toes.",
            "Contract glutes and core to keep hips level in a straight line.",
            "Hold position steadily while breathing continuously."
        ),
        defaultSets = 3,
        defaultTargetReps = 30,
        defaultWeightKg = 0.0
    ),
    Exercise(
        id = "db_lunge",
        name = "Dumbbell Walking Lunges",
        primaryMuscleGroup = "Quadriceps",
        secondaryMuscleGroups = listOf("Glutes", "Hamstrings", "Calves"),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        equipment = ExerciseEquipment.DUMBBELL,
        movementPattern = "Lunge",
        supportsAiTracking = true,
        description = "Unilateral leg exercise improving leg strength, balance, and hip mobility.",
        steps = listOf(
            "Hold a dumbbell in each hand at your sides with neutral grip.",
            "Step forward with right leg and bend knees to 90 degrees.",
            "Keep torso upright and back knee hovering just off ground.",
            "Push off right foot and step forward into next lunge stride."
        ),
        defaultSets = 3,
        defaultTargetReps = 10,
        defaultWeightKg = 10.0
    ),
    Exercise(
        id = "deadlift",
        name = "Barbell Conventional Deadlift",
        primaryMuscleGroup = "Posterior Chain",
        secondaryMuscleGroups = listOf("Hamstrings", "Glutes", "Lower Back", "Traps"),
        difficulty = ExerciseDifficulty.ADVANCED,
        equipment = ExerciseEquipment.BARBELL,
        movementPattern = "Hinge",
        supportsAiTracking = false,
        description = "Ultimate full-body strength lift targeting entire posterior chain and grip.",
        steps = listOf(
            "Stand with feet hip-width apart, mid-foot directly beneath bar.",
            "Hinge at hips, grip bar shoulder-width, and pull lats tight.",
            "Drive floor away with legs while extending hips and knees in unison.",
            "Lock out upright at top, then lower bar in controlled hinge descent."
        ),
        defaultSets = 4,
        defaultTargetReps = 6,
        defaultWeightKg = 80.0
    ),
    Exercise(
        id = "db_ohp",
        name = "Dumbbell Overhead Press",
        primaryMuscleGroup = "Shoulders",
        secondaryMuscleGroups = listOf("Triceps", "Upper Chest", "Traps"),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        equipment = ExerciseEquipment.DUMBBELL,
        movementPattern = "Push",
        supportsAiTracking = true,
        description = "Vertical pushing movement developing shoulder cap width and overhead stability.",
        steps = listOf(
            "Sit or stand holding dumbbells at shoulder height with palms facing forward.",
            "Brace core and press dumbbells overhead until arms are extended.",
            "Lower dumbbells back smoothly under control to ear/shoulder level."
        ),
        defaultSets = 3,
        defaultTargetReps = 10,
        defaultWeightKg = 14.0
    ),
    Exercise(
        id = "cable_fly",
        name = "Cable Chest Crossover",
        primaryMuscleGroup = "Chest",
        secondaryMuscleGroups = listOf("Front Delts"),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        equipment = ExerciseEquipment.CABLE,
        movementPattern = "Push",
        supportsAiTracking = false,
        description = "Constant tension chest isolation movement for inner pec contraction.",
        steps = listOf(
            "Attach D-handles to high pulleys on cable machine.",
            "Step forward slightly with one foot for stability and lean forward.",
            "Bring handles together in front of torso in a wide hugging motion.",
            "Squeeze chest peak contraction then slowly return handles outward."
        ),
        defaultSets = 3,
        defaultTargetReps = 12,
        defaultWeightKg = 15.0
    )
)

private var sessionCounter = 1000

fun createDefaultSession(exercise: Exercise): ActiveWorkoutSession {
    sessionCounter++
    val sets = (1..exercise.defaultSets).map { setNum ->
        SetLog(
            setNumber = setNum,
            targetWeightKg = exercise.defaultWeightKg,
            targetReps = exercise.defaultTargetReps,
            actualWeightKg = exercise.defaultWeightKg,
            actualReps = exercise.defaultTargetReps,
            isCompleted = false
        )
    }.toMutableList()

    return ActiveWorkoutSession(
        id = "session_${exercise.id}_$sessionCounter",
        routineTitle = "${exercise.name} Focus Session",
        exercises = listOf(SessionExercise(exercise, sets)),
        currentExerciseIndex = 0,
        startTimeSeconds = 0L
    )
}
