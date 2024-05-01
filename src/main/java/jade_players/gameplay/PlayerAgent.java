package jade_players.gameplay;

import jade_players.match_making.MatchMakerAgent;
import jade_players.match_making.PairedContestantBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.Thread.sleep;

/**
 * Агент-игрок.
 * Использует матч-мейкинг от {@link MatchMakerAgent}, чтобы выбрать оппонента, с которым играет в игру 1 на 1.
 * Соответственно, в контейнере должны присутсвовать как минимум 2 агента-игрока одного типа ({@link #getMatchCategoryName()})
 * и {@link MatchMakerAgent}, создающий матчи между ними.
 *
 * @author Marat Gumerov
 * @since 30.04.2024
 */
public abstract class PlayerAgent extends Agent {
    
    private PlayState playState;
    private AID opponent;
    
    private boolean manualTurnPause;
    
    @Override
    protected void setup() {
        playState = PlayState.searchingForOpponent;
        addBehaviour(new PairUpBehaviour());
        manualTurnPause = hasArgument("manual");
    }
    
    protected boolean hasArgument(String arg){
        return Arrays.asList(getArguments()).contains(arg);
    }
    
    protected boolean isManualTurnPause(){
        return manualTurnPause;
    }
    
    private enum PlayState{
        searchingForOpponent,
        
        playingWaiting,
        playingThinking,
        
        won,
        lost,
        ;
        
        public boolean isGameOver(){
            return this == won || this == lost;
        }
    }
    
    protected String getMatchCategoryName(){
        return this.getClass().getSimpleName();
    }
    
    protected enum TurnEffect{
        i_lost,
        i_won,
        game_continues,
    }
    
    protected record TurnResult(TurnEffect effect, String messageToOpponent){
        public static TurnResult gameContinues(){
            return new TurnResult(TurnEffect.game_continues, "");
        }
        public static TurnResult gameContinues(String messageToOpponent){
            return new TurnResult(TurnEffect.game_continues, messageToOpponent + "\n");
        }
        public static TurnResult iLost(){
            return new TurnResult(TurnEffect.i_lost, "");
        }
        public static TurnResult iLost(String messageToOpponent){
            return new TurnResult(TurnEffect.i_lost, messageToOpponent + "\n");
        }
        public static TurnResult iWon(){
            return new TurnResult(TurnEffect.i_won, "");
        }
        public static TurnResult iWon(String messageToOpponent){
            return new TurnResult(TurnEffect.i_won, messageToOpponent + "\n");
        }
    }
    
    /**
     * Обработать ход оппонента (по сообщаемой строке) и вернуть эффект, который это имеет на игру
     */
    protected abstract TurnResult processOpponentsTurn(String opponentTurnMessageContent);
    
    /**
     * Совершить собственный ход, и вернуть его результат (эффект хода на игру и сообщение для оппонента)
     */
    protected abstract TurnResult takeYourTurn(TurnResult opponentsTurnResult);
    
    /**
     * Как долго агент будет ждать, перед тем как совершить ход
     * Сделано для наглядности выполнения игры
     */
    protected double turnPauseInSeconds(){
        return 1.5;
    }
    
    
    
    private class PairUpBehaviour extends PairedContestantBehaviour {
        
        @Override
        protected String getMatchCategoryName() {
            return PlayerAgent.this.getMatchCategoryName();
        }
        
        @Override
        protected void onMatchFound(boolean isFirst, AID opponent) {
            if(myAgent instanceof PlayerAgent playerAgent){
                playerAgent.addBehaviour(new PlayBehaviour(playerAgent));
                playerAgent.opponent = opponent;
                playerAgent.playState = isFirst ? PlayState.playingThinking : PlayState.playingWaiting;
            }
            else throw new IllegalStateException();
        }
    }
    
    private class PlayBehaviour extends Behaviour {
        protected final PlayerAgent playerAgent;
        
        private PlayBehaviour(PlayerAgent playerAgent) {
            super(playerAgent);
            this.playerAgent = playerAgent;
        }
        
        private void turnPause(){
            if(isManualTurnPause()){
                try {
                    System.in.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    sleep((long) (PlayerAgent.this.turnPauseInSeconds() * 1000L));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        @Override
        public void action() {
            if(playerAgent.playState == PlayState.searchingForOpponent){
                block();
                return;
            }
            
            turnPause();
            
            //Обработать ход оппонента, если ждем его хода
            TurnResult opponentsTurnResult = TurnResult.gameContinues();
            if(playerAgent.playState == PlayState.playingWaiting){
                MessageTemplate msgTemplate = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(playerAgent.opponent)
                );
                ACLMessage msg = playerAgent.receive(msgTemplate);
                if(msg == null){
                    block();
                    return;
                }
                
                opponentsTurnResult = processOpponentsTurn(msg.getContent());
                TurnEffect opponentsTurnEffect = opponentsTurnResult.effect();
                playerAgent.playState = opponentsTurnEffect == TurnEffect.game_continues ? PlayState.playingThinking
                    : opponentsTurnEffect == TurnEffect.i_lost ? PlayState.lost
                    : PlayState.won;
            }
            
            //Сделать свой ход если пришла очередь
            TurnResult myTurnResult = playerAgent.playState.isGameOver()
                ? opponentsTurnResult
                : takeYourTurn(opponentsTurnResult);
            TurnEffect myTurnEffect = myTurnResult.effect;
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(playerAgent.opponent);
            msg.setContent(myTurnResult.messageToOpponent);
            playerAgent.send(msg);
            playerAgent.playState = myTurnEffect == TurnEffect.game_continues ? PlayState.playingWaiting
                : myTurnEffect == TurnEffect.i_lost ? PlayState.lost
                : PlayState.won;
            
            if(playState.isGameOver()){
                System.out.println(playerAgent.getLocalName() + " " + playState + "!");
            }
        }
        
        @Override
        public boolean done() {
            return playerAgent.playState.isGameOver();
        }
    }
}
