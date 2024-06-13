package jade_players.gameplay.ticTacToe

import jade_players.gameplay.ticTacToe.GameField.Companion.FIELD_SIZE
import jade_players.gameplay.utils.Direction
import jade_players.gameplay.utils.Grid
import jade_players.gameplay.utils.Point

/**
 * Сильный игрок (выбирает наилучшие свои ходы и блокирует наилучшие ходы противника)
 */
class StrongPlayer : DefaultPlayer() {

    /**
     * Первый ход
     */
    private var isFirstMove = true

    override fun chooseCell(): Point? {
        // Забрать центр
        val pickUpCenter = pickUpCenter()
        pickUpCenter?.let {
            return it
        }

        // Получить свой и противника наилучший ход
        val bestMyMove = morePerspectiveMove(mySymbol, enemySymbol)
        val bestEnemyMove = morePerspectiveMove(enemySymbol, mySymbol)
        if (bestMyMove != null && bestEnemyMove != null) {

            // Если нам требуется меньше ходов, то выбираем свой ход,
            // если кол-во ходов противника до победы меньше, то выбираем наилучший ход противника, тем самым мешая ему
            if (bestMyMove.first <= bestEnemyMove.first) {
                return bestMyMove.second
            } else {
                return bestEnemyMove.second
            }
        }
        bestMyMove?.let {
            return it.second
        }
        bestEnemyMove?.let {
            return it.second
        }

        // Если не сработал ни один из ходов, то ходим рандомно
        return super.chooseCell()
    }

    /**
     * Взятие центра игрового поля
     */
    private fun pickUpCenter() : Point? {
        var centerPoint = Point(FIELD_SIZE / 2, FIELD_SIZE / 2)

        // Если игрок ходит первым или центр еще не взят, то выбрать центр
        if((isFirstMove && isFirstPlayer) || gameField[centerPoint]?.content == GameField.GameFieldState.INITIAL) {
            isFirstMove = false
            return Point(FIELD_SIZE / 2, FIELD_SIZE / 2)
        }
        return null
    }

    /**
     * Наиболее перспективный ход
     * @param forWhom для кого перспективный (символ)
     * @param enemy противник (символ)
     * @return минимальное кол-во ходов для победы и наилучший ход (Точка)
     */
    private fun morePerspectiveMove(forWhom : GameField.GameFieldState, enemy : GameField.GameFieldState) : Pair<Int, Point>? {
        // Получить все ячейки игрока для которого перспективно
        val myCells = gameField.values.filter { it.content == forWhom }

        // Минимальное кол-во ходов до выигрыша
        var minMovesToWin = Int.MAX_VALUE

        // Лучший ход (ячейка)
        var bestMove : Grid.Cell<GameField.GameFieldState>? = null

        myCells.forEach { cell ->

            // Получить все линии пересекающие ячейку
            val allLinesForCell = getAllLinesForCell(cell)
            allLinesForCell.forEach { (_, u) ->

                // Получить информацию о линии
                val information = informationAboutLine(u, enemy)

                // Если в линии содержится потенциально наиболее перспективный ход, то запомнить его
                information?.let { info ->
                    if (info.first < minMovesToWin) {
                        minMovesToWin = info.first
                        bestMove = info.second
                    }
                }
            }
        }
        return if (bestMove != null) Pair(minMovesToWin, bestMove!!.point) else null
    }

    /**
     * Получение всех линий, которые пересекают ячейку
     * @param currentCell ячейка
     */
    private fun getAllLinesForCell(currentCell : Grid.Cell<GameField.GameFieldState>) : Map<LineType, List<Grid.Cell<GameField.GameFieldState>>> {
        val leftUp = currentCell.getCellsInDirection(Direction.up_left, ROW_LENGTH_TO_WIN).drop(1)
        val rightUp = currentCell.getCellsInDirection(Direction.up_right, ROW_LENGTH_TO_WIN).drop(1)
        val leftDown = currentCell.getCellsInDirection(Direction.down_left, ROW_LENGTH_TO_WIN).drop(1)
        val rightDown = currentCell.getCellsInDirection(Direction.down_right, ROW_LENGTH_TO_WIN).drop(1)
        val left = currentCell.getCellsInDirection(Direction.left, ROW_LENGTH_TO_WIN).drop(1)
        val right = currentCell.getCellsInDirection(Direction.right, ROW_LENGTH_TO_WIN).drop(1)
        val up = currentCell.getCellsInDirection(Direction.up, ROW_LENGTH_TO_WIN).drop(1)
        val down = currentCell.getCellsInDirection(Direction.down, ROW_LENGTH_TO_WIN).drop(1)
        val horizontal = left.reversed() + currentCell + right
        val vertical = up.reversed() + currentCell + down
        val leftDiagonal = leftUp.reversed() + currentCell + rightDown
        val rightDiagonal = rightUp.reversed() + currentCell + leftDown
        return mapOf(
            LineType.HORIZONTAL to horizontal,
            LineType.VERTICAL to vertical,
            LineType.LEFT_DIAGONAL to leftDiagonal,
            LineType.RIGHT_DIAGONAL to rightDiagonal
        )
    }

    /**
     * Получить информацию о перспективном ходе в линии
     * @param line линия
     * @param enemy противник (символ)
     * @return минимальное кол-во ходов для победы и наилучший ход (ячейка)
     */
    private fun informationAboutLine(line : List<Grid.Cell<GameField.GameFieldState>>, enemy: GameField.GameFieldState) : Pair<Int, Grid.Cell<GameField.GameFieldState>>? {
        // Минимальное количество ходов до победы
        var minMoveLeftToWin = Int.MAX_VALUE

        // Наилучший ход (ячейка)
        var nextCell : Grid.Cell<GameField.GameFieldState>? = null

        // Найти наиболее перспективную последовательность, где больше всего своих символов
        for (i in 0 until line.count()) {
            var moveLeftToWin = 0
            var canWin = true
            var tmpNextCell : Grid.Cell<GameField.GameFieldState>? = null
            if (i + ROW_LENGTH_TO_WIN - 1 >= line.count()) {
                break;
            }

            // Проверка последовательности на пригодность для наилучшего хода
            for (j in i until i + ROW_LENGTH_TO_WIN) {
                if (line[j].content == GameField.GameFieldState.INITIAL) {
                    tmpNextCell = line[j]
                    moveLeftToWin++
                } else if (line[j].content == enemy) {
                    canWin = false
                    break
                }
            }

            if (canWin && moveLeftToWin < minMoveLeftToWin) {
                nextCell = tmpNextCell
                minMoveLeftToWin = moveLeftToWin
            }
        }
        return if (minMoveLeftToWin != Int.MAX_VALUE && nextCell != null) Pair(minMoveLeftToWin, nextCell) else null
    }

    /**
     * Тип линии
     */
    enum class LineType {
        HORIZONTAL,
        VERTICAL,
        LEFT_DIAGONAL,
        RIGHT_DIAGONAL
    }
}