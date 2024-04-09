package me.gabryon.kodeedelivery.levels

object DebugLevel : LevelLogic {

    override val maximumCharacterSpeed: Double = -3.0
    override val pointsToNextLevel: Int = Int.MAX_VALUE

    override fun mailboxes(): Sequence<MailboxPosition> = sequence {
        while (true) {
            yield(MailboxPosition(
                hoz = MailboxHorizontalPosition.LEFT,
                ver = MailboxVerticalPosition.BOTTOM,
                distanceFromPrevious = 1.0,
            ))
            yield(MailboxPosition(
                hoz = MailboxHorizontalPosition.RIGHT,
                ver = MailboxVerticalPosition.BOTTOM,
                distanceFromPrevious = .0,
            ))
        }
    }
}