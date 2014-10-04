package agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
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
	
	/**
	 * 
	 */
	private void buildInitialMessageForTrucks() {
		
		final List<TruckAgent> trucksTemp = new ArrayList<TruckAgent>();
		Iterator<TruckAgent> itTruck = this.planner.getTruckAgents().iterator();
		while(itTruck.hasNext()){
			TruckAgent t = itTruck.next();
			trucksTemp.add(t);
		}
		
		addBehaviour(new SimpleBehaviour(this) {
			
			private static final long serialVersionUID = 3729878192721244478L;
			private boolean finished = false;
			
			@Override
			public void action() {
				ACLMessage initialMsg = new ACLMessage(ACLMessage.INFORM);
				Iterator<TruckAgent> itTruck = trucksTemp.iterator();
				while(itTruck.hasNext()){
					TruckAgent t = itTruck.next();
					initialMsg.addReceiver(t.getAID());
					
					System.out.println("PlannerAgent: Ready? message to " + t.getAID());
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

	protected void setup() {
		Object args[] = getArguments();
		this.planner = (Planner) args[0];
		System.out.println("My name is " + getAID().getName() + " and I'm active now.");
	}
}
