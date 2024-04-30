package jade_players;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Вспомогательные функции для поведений
 *
 * @author Marat Gumerov
 * @since 30.04.2024
 */
public abstract class BehaviourUtils {
    
    public static List<ACLMessage> receiveAll(Agent agent, MessageTemplate messageTemplate){
        return Optional.ofNullable(agent.receive(messageTemplate, 100)).orElseGet(ArrayList::new);
    }
    
    public static Optional<ACLMessage> receive(Agent agent, MessageTemplate messageTemplate){
        return Optional.ofNullable(agent.receive(messageTemplate));
    }
    
    public static Optional<ACLMessage> receiveAndProcess(
        Agent agent,
        MessageTemplate messageTemplate,
        Consumer<ACLMessage> process
    ){
        Optional<ACLMessage> message = receive(agent, messageTemplate);
        message.ifPresent(process);
        return message;
    }
    
    
    public static List<DFAgentDescription> findAgentsByServiceType(Agent agent, String serviceType){
        ServiceDescription matchMakerService = new ServiceDescription();
        matchMakerService.setType(serviceType);
        DFAgentDescription matchMakerDescription = new DFAgentDescription();
        matchMakerDescription.addServices(matchMakerService);
        
        try {
            return Arrays.stream(DFService.search(agent, matchMakerDescription)).toList();
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Optional<DFAgentDescription> findAgentByServiceType(Agent agent, String serviceType){
        return findAgentsByServiceType(agent, serviceType).stream().findFirst();
    }
}
