package jade_players.gameplay.ticTacToe

import jade_players.gameplay.PlayerAgent
import jade_players.gameplay.ticTacToe.GameField.Companion.FIELD_SIZE
import jade_players.gameplay.utils.Direction
import jade_players.gameplay.utils.Grid.Cell
import jade_players.gameplay.utils.Point

abstract class TicTacToeAgent : PlayerAgent() {

    /**
     * Символ игрока на игровом поле
     */
    lateinit var mySymbol : GameField.GameFieldState

    /**
     * Символ опонента на игровом поле
     */
    lateinit var enemySymbol : GameField.GameFieldState

    /**
     * Игровое поле
     */
    protected val gameField : GameField = GameField(initialSymbol = GameField.GameFieldState.INITIAL)

    /**
     * Ходит первым
     */
    var isFirstPlayer = false

    protected var log = false

    override fun setup() {
        super.setup()
        log = hasArgument("log")
    }

    override fun turnPauseInSeconds(): Double = 0.5

    override fun processOpponentsTurn(opponentTurnMessageContent: String?): TurnResult {
        // Парсим ответ опонента
        val enemyPoint = opponentTurnMessageContent?.takeIf { it.isNotEmpty() }?.let {
            val parse = it.split('|')
            Point(parse.first().trim().toInt(), parse.last().trim().toInt())
        }

        // Если ход противника пустой, значит ходы закончились и получилась ничья, то есть все проиграли
        return enemyPoint?.let {
            // Синхронизируем игровые поля
            gameField[it]?.content = enemySymbol

            // Проверяем выиграл ли противник последним ходом, если да, то мы проиграли, если нет, то игра продолжается
            if (isWin(it, enemySymbol, ROW_LENGTH_TO_WIN)) {
                TurnResult.iLost()
            } else {
                TurnResult.gameContinues()
            }
        } ?: TurnResult.iLost()
    }

    override fun takeYourTurn(opponentsTurnResult: TurnResult?): TurnResult? {
        // Выбираем клетку
        val chosenCell = chooseCell()
        chosenCell?.let {
            gameField[it]?.content = mySymbol
        }

        // Печатаем прогресс
        println("Game state on " + aid.localName + "'s (${mySymbol.symbol}) turn : ")
        gameField.print()

        // Если клетка для хода не выбрана, то ничья, то есть мы проиграли
        return chosenCell?.let {

            // Проверяем, выиграли ли мы, если да, то говорим, что мы выиграли, если нет, то игра продолжается
            if (isWin(it, mySymbol, ROW_LENGTH_TO_WIN)) {
                TurnResult.iWon("${it.x}|${it.y}")
            } else {
                TurnResult.gameContinues("${it.x}|${it.y}")
            }
        } ?: TurnResult.iLost()
    }

    override fun onMatchFoundAgent(isFirst: Boolean) {
        super.onMatchFoundAgent(isFirst)

        // Устанавливаем первого игрока
        isFirstPlayer = isFirst

        // Устанавливаем первому игроку крестик, а второму нолик
        mySymbol = if (isFirst) GameField.GameFieldState.CROSS else GameField.GameFieldState.ZERO
        enemySymbol = if (mySymbol == GameField.GameFieldState.CROSS) GameField.GameFieldState.ZERO else GameField.GameFieldState.CROSS
    }

    override fun getMatchCategoryName(): String {
        return "TicTacToe"
    }

    /**
     * Проверяет, выиграл ли игрок за свой последний ход
     * @param lastPoint последний ход игрока
     * @param symbol символ игрока на игровом поле
     * @param lineSize длина последовательности, которая обеспечивает победу
     */
    protected fun isWin(lastPoint : Point, symbol : GameField.GameFieldState, lineSize : Int = FIELD_SIZE) : Boolean {
        val isLeftDiagonal = lastPoint.x == lastPoint.y
        val isRightDiagonal = FIELD_SIZE - lastPoint.x - 1 == lastPoint.y
        val currentCell = gameField[lastPoint] ?: return false
        if (isLeftDiagonal || isRightDiagonal) {
            val leftUp = currentCell.getCellsInDirection(Direction.up_left, lineSize).drop(1)
            val rightUp = currentCell.getCellsInDirection(Direction.up_right, lineSize).drop(1)
            val leftDown = currentCell.getCellsInDirection(Direction.down_left, lineSize).drop(1)
            val rightDown = currentCell.getCellsInDirection(Direction.down_right, lineSize).drop(1)
            val leftDiagonal = leftUp.reversed() + currentCell + rightDown
            val rightDiagonal = rightUp.reversed() + currentCell + leftDown
            if (
                (isLeftDiagonal && findSubList(leftDiagonal, symbol, lineSize))
                || (isRightDiagonal && findSubList(rightDiagonal, symbol, lineSize))
                || (isLeftDiagonal && isRightDiagonal
                        && (findSubList(leftDiagonal, symbol, lineSize) || findSubList(rightDiagonal, symbol, lineSize)))
            ) {
                return true
            }
        }
        val left = currentCell.getCellsInDirection(Direction.left, lineSize).drop(1)
        val right = currentCell.getCellsInDirection(Direction.right, lineSize).drop(1)
        val up = currentCell.getCellsInDirection(Direction.up, lineSize).drop(1)
        val down = currentCell.getCellsInDirection(Direction.down, lineSize).drop(1)
        val horizontal = left.reversed() + currentCell + right
        val vertical = up.reversed() + currentCell + down
        return findSubList(horizontal, symbol, lineSize) || findSubList(vertical, symbol, lineSize)
    }

    /**
     * Функция для выбора следующей клетки (ход игрока)
     */
    abstract fun chooseCell() : Point?

    /**
     * Поиск подмассива определенной длинны в массиве
     */
    protected fun findSubList(list : List<Cell<GameField.GameFieldState>>, symbol : GameField.GameFieldState, lineSize : Int) : Boolean {
        var counter = 0
        list.forEach {
            if (it.content == symbol) {
                counter++
            } else {
                counter = 0
            }
            if (counter == lineSize) {
                return true
            }
        }
        return false
    }

    companion object {
        /**
         * Длина последовательности необходимая для победы
         */
        const val ROW_LENGTH_TO_WIN = 3
    }
}