package jade_players.gameplay;

import jade_players.gameplay.utils.RandomUtils;

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
    protected TurnResult processOpponentsTurn(String opponentTurnMessageContent) {
        int opponentCount = Integer.parseInt(opponentTurnMessageContent);
        return isWinningState(opponentCount) ? TurnResult.iLost() : TurnResult.gameContinues();
    }
    
    @Override
    protected TurnResult takeYourTurn(TurnResult opponentsTurnResult) {
        int inc = RandomUtils.random.nextInt(1, 4);
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
