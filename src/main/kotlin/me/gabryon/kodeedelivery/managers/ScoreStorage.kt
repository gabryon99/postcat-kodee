package me.gabryon.kodeedelivery.managers

object ScoreStorage {

    const val MINIMUM_SCORE_FOR_PRIZE = 10000

    var userScore = 0
        set(value) {
            if (value > maximumUserScore) {
                maximumUserScore = value
            }
            field = value
        }

    var maximumUserScore = 0
        private set

    /**
     * Resets the current user score to zero.
     */
    fun resetScore() {
        userScore = 0
    }

    /**
     * Serializes the maximum user score in device memory.
     */
    fun saveScoreToDeviceStorage() {
        TODO("Serialize the maximum user score in device memory")
    }
}