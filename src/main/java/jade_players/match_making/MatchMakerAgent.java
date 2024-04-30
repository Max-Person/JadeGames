package jade_players.match_making;

import jade_players.BehaviourUtils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Агент, составляющий матчи между другими агентами одного типа
 * Агенты-участники отправляют сообщения типа SUBSCRIBE с помощью {@link ContestantBehaviour},
 * после чего они добавляются в очереди ожидания. Когда в очереди наберется достаточно агентов участников,
 * всем им отправляется сообщение типа PROPOSE о начале матча (см. {@link ContestantBehaviour#onMatchFound})
 *
 * @author Marat Gumerov
 * @since 30.04.2024
 */
public class MatchMakerAgent extends Agent {
    public static final String MATCH_MAKER_SERVICE = "matchmaker";
    private final static int MATCH_SIZE = 2;
    
    @Override
    protected void setup() {
        addBehaviour(new MatchMakerBehaviour());
        ServiceDescription matchMakerService = new ServiceDescription();
        matchMakerService.setType(MATCH_MAKER_SERVICE);
        matchMakerService.setName(getName());
        DFAgentDescription matchMakerDescription = new DFAgentDescription();
        matchMakerDescription.addServices(matchMakerService);
        try {
            DFService.register(this, matchMakerDescription);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static class MatchMakerBehaviour extends CyclicBehaviour{
        private Map<String, Deque<AID>> contestantMap = new HashMap<>();
        
        @Override
        public void action() {
            BehaviourUtils.receiveAll(myAgent, MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE))
                .forEach(message -> {
                    contestantMap.computeIfAbsent(message.getContent(), k -> new ArrayDeque<>()).add(message.getSender());
                });
            
            contestantMap.forEach((categoryName, contestants)  -> {
                while (contestants.size() >= MATCH_SIZE){
                    List<AID> match = IntStream.range(0, MATCH_SIZE).mapToObj(i -> contestants.pollFirst()).toList();
                    match.forEach(contestantAID -> {
                        List<String> others = match.stream()
                            .filter(other -> !other.equals(contestantAID))
                            .map(AID::getName)
                            .toList();
                        int index = match.indexOf(contestantAID);
                        
                        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                        msg.addReceiver(contestantAID);
                        msg.setContent(index + "\n" + String.join("\n", others));
                        
                        myAgent.send(msg);
                    });
                    
                    System.out.println("Made match of " + categoryName
                        + " with " + match.stream().map(AID::getLocalName).toList());
                }
            });
            
            block();
        }
    }
    
}
