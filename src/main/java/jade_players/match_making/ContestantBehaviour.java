package jade_players.match_making;

import jade_players.BehaviourUtils;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.List;

/**
 * Поведение агента участника матчей. Работает с {@link MatchMakerAgent} для составления матчей
 * Наследники должны переопределить {@link #getMatchCategoryName()} - категорию матчей, внутри которых они группируются,
 * и {@link #onMatchFound} - что делать, когда найден матч
 *
 * @author Marat Gumerov
 * @since 30.04.2024
 */
public abstract class ContestantBehaviour extends Behaviour {
    private enum State {
        notRequested,
        waiting,
        found,
    }
    
    private State state = State.notRequested;
    
    @Override
    public void action() {
        if (state == State.waiting) {
            BehaviourUtils.receive(myAgent, MessageTemplate.MatchPerformative(ACLMessage.PROPOSE))
                .ifPresent(matchMessage -> {
                    List<String> content = List.of(matchMessage.getContent().split("\n"));
                    int index = Integer.parseInt(content.get(0));
                    List<AID> others = content.subList(1, content.size())
                        .stream()
                        .map(name -> new AID(name, AID.ISGUID))
                        .toList();
                    onMatchFound(index, others);
                    state = State.found;
                });
        }
        
        if (state == State.notRequested) {
            BehaviourUtils.findAgentByServiceType(myAgent, MatchMakerAgent.MATCH_MAKER_SERVICE)
                .ifPresent(matchMaker -> {
                    ACLMessage subscribeMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
                    subscribeMessage.setContent(getMatchCategoryName());
                    subscribeMessage.addReceiver(matchMaker.getName());
                    myAgent.send(subscribeMessage);
                    state = State.waiting;
                });
        }
        
        if (state != State.notRequested) {
            block();
        }
    }
    
    /**
     * Имя категории матчей, в которой работает текущий участник
     */
    protected abstract String getMatchCategoryName();
    
    /**
     * Действия при нахождении матча
     * @param myIndex - индекс текущего участника среди всех остальных
     * @param otherContestants - AID остальных участников матча
     */
    protected abstract void onMatchFound(int myIndex, List<AID> otherContestants);
    
    
    @Override
    public boolean done() {
        return state == State.found;
    }
}
