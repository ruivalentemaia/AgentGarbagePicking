package agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import units.Planner;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class PlannerAgent extends Agent {
	
	private static final long serialVersionUID = -8343519877395972050L;
	
	private Planner planner;
	
	public Planner getPlanner() {
		return planner;
	}

	public void setPlanner(Planner planner) {
		this.planner = planner;
	}
	
	protected void setup() {
		Object args[] = getArguments();
		this.planner = (Planner) args[0];
		System.out.println("My name is " + getAID().getName() + " and I'm active now.");
		this.buildInitialMessageForTrucks();
	}
	
	/**
	 * 
	 */
	private void buildInitialMessageForTrucks() {
		
		final List<String> trucksTemp = new ArrayList<String>();
		Iterator<String> itTruck = this.planner.getTruckAgents().iterator();
		while(itTruck.hasNext()){
			String t = itTruck.next();
			trucksTemp.add(t);
		}
		
		addBehaviour(new SimpleBehaviour(this) {
			
			private static final long serialVersionUID = 3729878192721244478L;
			private boolean finished = false;
			
			@Override
			public void action() {
				ACLMessage initialMsg = new ACLMessage(ACLMessage.INFORM);
				Iterator<String> itTruck = trucksTemp.iterator();
				while(itTruck.hasNext()){
					
					AMSAgentDescription [] agents = null;
			        try {
			            SearchConstraints c = new SearchConstraints();
			            c.setMaxResults ( new Long(-1) );
			            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
			        }
			        catch (Exception e) { e.printStackTrace();}
					
			        String t = itTruck.next();
			        AID myID = getAID();
			        System.out.println("\n");
			        for (int i=0; i<agents.length;i++) {
			            AID agentID = agents[i].getName();
			            if(agentID.getName().equals(t)) {
			            	initialMsg.addReceiver(agentID);
			            	System.out.println("PlannerAgent: Ready? message to " + t);
			            }
			            else continue;
			        }
				}
		
				initialMsg.setContent("Ready?");
				initialMsg.setOntology("InitialMessage");
				send(initialMsg);
				
				finished = true;
			}

			@Override
			public boolean done() {
				return finished;
			}
		});
	}

	
}
