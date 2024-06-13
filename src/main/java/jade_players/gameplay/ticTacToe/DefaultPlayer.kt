package jade_players.gameplay.ticTacToe

import jade_players.gameplay.utils.Point

/**
 * Обычный игрок (выбирает ход рандомно)
 */
open class DefaultPlayer : TicTacToeAgent() {

    override fun chooseCell(): Point? {
        // Рандомный выбор из свободных клеток
        val chosenCell = gameField.values.filter { it.content == GameField.GameFieldState.INITIAL }.randomOrNull()
        return chosenCell?.point
    }

}