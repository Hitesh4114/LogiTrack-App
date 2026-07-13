package com.example.data

sealed class MovementState {
    object Stationary : MovementState()
    object Walking : MovementState()
    object Driving : MovementState()

    fun name(): String = when(this) {
        is Stationary -> "Stationary (0 km/h)"
        is Walking -> "Walking (5-10 km/h)"
        is Driving -> "Driving (30-80 km/h)"
    }
}

object LocationOptimizer {

    fun calculateOptimalInterval(
        movementState: MovementState,
        batteryPct: Int,
        isOptimizationEnabled: Boolean
    ): Int {
        if (!isOptimizationEnabled) {
            return 5
        }


        val baseInterval = when (movementState) {
            is MovementState.Stationary -> 20
            is MovementState.Walking -> 10
            is MovementState.Driving -> 5
        }


        val batteryMultiplier = when {
            batteryPct >= 50 -> 1.0
            batteryPct >= 20 -> 1.5
            else -> 3.0
        }

        val calculated = (baseInterval * batteryMultiplier).toInt()
        

        return calculated.coerceIn(5, 60)
    }


    fun calculateSavingsPercentage(optimalIntervalSeconds: Int): Double {
        if (optimalIntervalSeconds <= 5) return 0.0
        

        val standardUpdates = 720.0

        val optimizedUpdates = 3600.0 / optimalIntervalSeconds
        

        val savings = (1.0 - (optimizedUpdates / standardUpdates)) * 100.0
        return savings.coerceIn(0.0, 92.0)
    }
}
