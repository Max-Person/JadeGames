package jade_players;


import jade.tools.rma.rma;
import jade.tools.sniffer.Sniffer;
import jade_players.gameplay.battleship.BattleshipAgent;
import jade_players.match_making.MatchMakerAgent;
import jade_players.gameplay.SamplePlayerAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.*;

import java.io.IOException;
import java.util.*;

/**
 * Этот класс и общий код взят и переработан с
 * <a href="https://startjade.gitlab.io/">https://startjade.gitlab.io/</a>
 * - см. туда, для доп информации
 */
public class Principal {

	private static HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();// container's name - container's ref
	private static List<AgentController> agentList;// agents's ref


	/************************************
	 * 1) Network and platform parameters
	 ***********************************/

	/**
	 * IP (or host) of the main container
	 */
	private static final String PLATFORM_IP = "127.0.0.1"; 

	/**
	 * Port to use to contact the AMS
	 */
	private static final int PLATFORM_PORT=8888;

	/**
	 * ID (name) of the platform instance
	 */
	private static final String PLATFORM_ID="MP";



	/**
	 * Main 
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		List<Object> parameters;

		//Whe should create the Platform and the GateKeeper, whether the platform is distributed or not

		//1) Create the platform (Main container (DF+AMS) + containers + monitoring agents : RMA and SNIFFER)
		ContainerController mainContainer = createEmptyPlatform();

		//2) create agents and add them to the platform.
		agentList = createAgents(mainContainer);

		try {
			System.out.println("The system is paused -- this action is only here to let you activate the sniffer on the agents, if you want (see documentation)");
			System.out.println("Press enter in the console to start the agents");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//4) launch agents
		startAgents(agentList);

	}


	/**********************************************
	 * 
	 * Methods used to create an empty platform
	 * 
	 **********************************************/

	/**
	 * Create an empty platform composed of 1 main container and several containers.
	 *
	 * @return a ref to the platform and update the containerList
	 */
	private static ContainerController createEmptyPlatform(){

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(PLATFORM_IP, PLATFORM_PORT, PLATFORM_ID);
		System.out.println("Launching a main-container..."+pMain);
		ContainerController mainContainerRef = rt.createMainContainer(pMain); //DF and AMS are include

		// 2) create the containers
//		containerList.putAll(createContainers(rt));

		// 3) create monitoring agents : rma agent, used to debug and monitor the platform; sniffer agent, to monitor communications; 
		createMonitoringAgents(mainContainerRef);

		System.out.println("Plaform ok");
		return mainContainerRef;

	}



	/**
	 * create the monitoring agents (rma+sniffer) on the main-container given in parameter and launch them.
	 * <ul>
	 * <li> RMA agent's is used to control, debug and monitor the platform;
	 * <li> Sniffer agent is used to monitor communications
	 * </ul>
	 * @param mc the main-container's reference
	 */
	private static void createMonitoringAgents(ContainerController mc) {

		System.out.println("Launching the rma agent on the main container ...");
		AgentController rmaAgent;

		try {
			rmaAgent = mc.createNewAgent("rma", rma.class.getName(), new Object[0]);
			rmaAgent.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("Launching of rma agent failed");
		}

		System.out.println("Launching  Sniffer agent on the main container...");
		AgentController snifferAgent = null;

		try {
			snifferAgent = mc.createNewAgent("sniffur", Sniffer.class.getName(), new Object[0]);
			snifferAgent.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("launching of sniffer agent failed");

		}		
	}



	/**********************************************
	 * 
	 * Methods used to create the agents and to start them
	 * 
	 **********************************************/


	/**
	 *  Creates the agents and add them to the agentList. The agents are NOT started.
	 * @return the agentList
	 */

	private static List<AgentController> createAgents(ContainerController containerController) {
		System.out.println("\n --- \n Launching agents... \n --- \n");
		List<AgentController> agentList=new ArrayList<AgentController>();
		
		createOneAgent(
			containerController,
			"MM",
			MatchMakerAgent.class.getName(),
			agentList,
			new Object[0]
		);
		
		createOneAgent(
			containerController,
			"BIBA",
			BattleshipAgent.class.getName(),
			agentList,
			new Object[]{"log"}
		);
		
		createOneAgent(
			containerController,
			"BOBA",
			BattleshipAgent.class.getName(),
			agentList,
			new Object[0]
		);
		

		System.out.println("Agents launched...");
		return agentList;
	}

	/**
	 * Create one agent agentName of class className wit parameters  agentOptionnalParameters on container c
	 * @param container containerObject
	 * @param agentName name of the agent
	 * @param className class of the agent
	 * @param agentList list that store the agents'references 
	 * @param agentOptionnalParameters agent's initial parameters that can be retrieved through the getArgument() method in the agent's setup(). Should be null if no params
	 */
	private static void createOneAgent(
		ContainerController container,
		String agentName,
		String className,
		List<AgentController> agentList,
		Object[] agentOptionnalParameters
	) {
		try {						
			AgentController	ag = container.createNewAgent(agentName, className, agentOptionnalParameters);
			agentList.add(ag);
			try {
				System.out.println(agentName+" launched on "+container.getContainerName());
			} catch (ControllerException e) {
				e.printStackTrace();
			}
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start the agents
	 * @param agentList
	 */
	private static void startAgents(List<AgentController> agentList){

		System.out.println("Starting agents...");


		for(final AgentController ac: agentList){
			try {
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("Agents started...");
	}

}







