package jade_players.gameplay;

/**
 * Игрок тестовой игры - каждый игрок увеличивает внутренний счетчик на рандомное число.
 * Кто первый дойдет от 0 до 10 - победил
 *
 * @author Marat Gumerov
 * @since 30.04.2024
 */
public class SamplePlayerAgent extends PlayerAgent{
    
    private int count = 0;
    
    @Override
    protected TurnEffect processOpponentsTurn(String opponentTurnMessageContent) {
        int opponentCount = Integer.parseInt(opponentTurnMessageContent);
        return isWinningState(opponentCount) ? TurnEffect.i_lost : TurnEffect.game_continues;
    }
    
    @Override
    protected TurnResult takeYourTurn() {
        int inc = random.nextInt(1, 4);
        count += inc;
        System.out.println(getLocalName() + " - " + count);
        return isWinningState(count)
            ? TurnResult.iWon(String.valueOf(count))
            : TurnResult.gameContinues(String.valueOf(count));
    }
    
    private boolean isWinningState(int count){
        return count >= 10;
    }
}
