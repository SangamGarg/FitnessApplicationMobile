package com.muscleplay.app.feature.workouts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkoutRepositoryTest {

    @Test
    fun testInitialState() {
        val repository = InMemoryWorkoutRepository()
        val state = repository.workoutState.value

        assertEquals("Push-ups", state.exerciseName)
        assertEquals(0, state.completedReps)
        assertEquals(10, state.targetReps)
        assertFalse(state.isCompleted)
    }

    @Test
    fun testCompleteRepIncrementsReps() {
        val repository = InMemoryWorkoutRepository()
        
        repository.completeRep()
        var state = repository.workoutState.value
        assertEquals(1, state.completedReps)
        assertFalse(state.isCompleted)

        // Complete up to target (10)
        repeat(9) {
            repository.completeRep()
        }
        
        state = repository.workoutState.value
        assertEquals(10, state.completedReps)
        assertTrue(state.isCompleted)
    }

    @Test
    fun testCompleteRepDoesNotExceedTarget() {
        val repository = InMemoryWorkoutRepository()
        
        // Complete up to 10
        repeat(10) {
            repository.completeRep()
        }
        
        var state = repository.workoutState.value
        assertEquals(10, state.completedReps)
        assertTrue(state.isCompleted)

        // Try to complete one more rep when already completed
        repository.completeRep()
        state = repository.workoutState.value
        assertEquals(10, state.completedReps)
        assertTrue(state.isCompleted)
    }

    @Test
    fun testResetWorkout() {
        val repository = InMemoryWorkoutRepository()
        
        repository.completeRep()
        repository.completeRep()
        
        var state = repository.workoutState.value
        assertEquals(2, state.completedReps)
        assertFalse(state.isCompleted)

        repository.resetWorkout()
        
        state = repository.workoutState.value
        assertEquals(0, state.completedReps)
        assertFalse(state.isCompleted)
    }
}
