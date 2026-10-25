package com.muscleplay.app.feature.games.bossbattle

data class BossBattleConfig(
    val durationSeconds: Int = 60,
    val bossMaxHealth: Int = 100,
    val damagePerRep: Int = 10,
    val comboEvery: Int = 5,
    val comboBonusDamage: Int = 5,
    val pointsPerRep: Int = 100,
    val perfectRepBonus: Int = 25
) {
    init {
        require(durationSeconds > 0)
        require(bossMaxHealth > 0)
        require(damagePerRep > 0)
        require(comboEvery > 0)
        require(comboBonusDamage >= 0)
        require(pointsPerRep >= 0)
        require(perfectRepBonus >= 0)
    }
}

enum class BossBattleStatus { READY, RUNNING, WON, TIME_UP }

data class BossBattleState(
    val status: BossBattleStatus = BossBattleStatus.READY,
    val elapsedSeconds: Int = 0,
    val bossHealth: Int,
    val score: Int = 0,
    val repetitions: Int = 0,
    val currentCombo: Int = 0,
    val bestCombo: Int = 0,
    val lastDamage: Int = 0
)

class FitnessBossBattleEngine(
    private val config: BossBattleConfig = BossBattleConfig()
) {
    private var current = BossBattleState(bossHealth = config.bossMaxHealth)

    val state: BossBattleState get() = current
    val remainingSeconds: Int
        get() = (config.durationSeconds - current.elapsedSeconds).coerceAtLeast(0)

    fun start(): BossBattleState {
        if (current.status == BossBattleStatus.READY) {
            current = current.copy(status = BossBattleStatus.RUNNING)
        }
        return current
    }

    /** Call once for each newly completed, validated repetition—not on every camera frame. */
    fun onValidRep(perfectForm: Boolean = false): BossBattleState {
        if (current.status != BossBattleStatus.RUNNING) return current

        val combo = current.currentCombo + 1
        val bonus = if (combo % config.comboEvery == 0) config.comboBonusDamage else 0
        val damage = config.damagePerRep + bonus
        val health = (current.bossHealth - damage).coerceAtLeast(0)
        val points = config.pointsPerRep + if (perfectForm) config.perfectRepBonus else 0

        current = current.copy(
            status = if (health == 0) BossBattleStatus.WON else BossBattleStatus.RUNNING,
            bossHealth = health,
            score = current.score + points,
            repetitions = current.repetitions + 1,
            currentCombo = combo,
            bestCombo = maxOf(current.bestCombo, combo),
            lastDamage = damage
        )
        return current
    }

    /** Call once per elapsed second while the round is RUNNING. */
    fun tickSecond(): BossBattleState {
        if (current.status != BossBattleStatus.RUNNING) return current
        val elapsed = current.elapsedSeconds + 1
        current = current.copy(
            elapsedSeconds = elapsed,
            status = if (elapsed >= config.durationSeconds) BossBattleStatus.TIME_UP
            else BossBattleStatus.RUNNING
        )
        return current
    }

    fun breakCombo(): BossBattleState {
        if (current.status == BossBattleStatus.RUNNING) {
            current = current.copy(currentCombo = 0)
        }
        return current
    }

    fun reset(): BossBattleState {
        current = BossBattleState(bossHealth = config.bossMaxHealth)
        return current
    }
}
