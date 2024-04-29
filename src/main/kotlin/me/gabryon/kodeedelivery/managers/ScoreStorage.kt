package me.gabryon.kodeedelivery.managers

import godot.FileAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.gabryon.kodeedelivery.utility.fileAccess

/**
 * Utility class responsible to keep the maximum user score in memory
 * and into the device storage.
 */
object ScoreStorage {

    const val MINIMUM_SCORE_FOR_PRIZE = 10000
    private const val SAVE_FILE_PATH = "user://kodee-delivery.save"

    /**
     * Current user score.
     * It updates the maximum user score
     * whenever it has reached a higher value than before.
     */
    var userScore = 0
        set(value) {
            if (value > maximumUserScore) {
                maximumUserScore = value
            }
            field = value
        }

    var maximumUserScore = loadMaximumScoreFromStorage()
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
    fun saveUserScoreToDevice() {
        /**
         * The user score is saved in a binary file containing only one
         * 32-bit integer (since we do not keep track of **any** information).
         */
        CoroutineScope(Dispatchers.IO).launch {
            fileAccess(SAVE_FILE_PATH, FileAccess.ModeFlags.WRITE_READ) {
                store32(maximumUserScore.toLong())
            }
        }
    }

    /**
     * Retrieves the maximum score from the storage.
     *
     * @return The maximum score stored in the storage, or 0 if no score was found.
     */
    private fun loadMaximumScoreFromStorage(): Int {
        /**
         * As mentioned in [saveUserScoreToDevice], the save file is just
         * a 32-bit integer number.
         * We read the number from the memory if the file
         * exists, otherwise we return zero.
         */
        return fileAccess(SAVE_FILE_PATH, FileAccess.ModeFlags.READ) {
            get32().toInt()
        } ?: 0
    }
}