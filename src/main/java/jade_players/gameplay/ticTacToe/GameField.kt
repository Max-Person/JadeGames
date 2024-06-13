package jade_players.gameplay.ticTacToe

import jade_players.gameplay.utils.Grid
import java.util.function.Supplier

/**
 * Игровое поле для крестиков ноликов
 * @param initialSymbol символ инициализации ячеек
 */
class GameField(
    val initialSymbol : GameFieldState
) : Grid<GameField.GameFieldState>(FIELD_SIZE, FIELD_SIZE, Supplier { initialSymbol }) {

    fun print() {
        println("-------")
        asRows().forEach { row ->
            print("|")
            row.forEach { cell ->
                print(cell.content.symbol)
            }
            println("|")
        }
        println("-------")
    }

    /**
     * Состояние ячеек игрового поля
     */
    enum class GameFieldState(val symbol : String) {
        /**
         * Крестик
         */
        CROSS("X"),

        /**
         * Нолик
         */
        ZERO("0"),

        /**
         * Символ пустоты
         */
        INITIAL("*");
    }

    companion object {

        /**
         * Размер поля
         */
        const val FIELD_SIZE = 3
    }
}