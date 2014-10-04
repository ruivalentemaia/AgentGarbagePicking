package agent;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TruckAgent extends Agent{
	
	private static final long serialVersionUID = 2394071613389642100L;
	
	private Truck truck;
	
	public Truck getTruck() {
		return truck;
	}
	
	public void setTruck(Truck t){
		this.truck = t;
	}
	
	protected void setup() {
		Object[] args = getArguments();
		this.truck = (Truck) args[0];
		System.out.println("My name is " + this.getAID().getName() + " and I'm active now.");
		addBehaviour(new readyBehaviour(this));
	}
	
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 		MESSAGES
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	class readyBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 752338739127971004L;
		private boolean finished = false;
		
		public readyBehaviour(Agent a){
			super(a);
		}

		@Override
		public void action() {
			MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			MessageTemplate m2 = MessageTemplate.MatchOntology("InitialMessage");
			MessageTemplate m3 = MessageTemplate.MatchContent("Ready?");
			
			MessageTemplate m1_and_m2 = MessageTemplate.and(m1, m2);
			MessageTemplate m12_and_m3 = MessageTemplate.and(m1_and_m2, m3);
			
			ACLMessage msg = receive(m12_and_m3);
			if(msg != null){
				System.out.println("Truck " + this.getAgent().getName() + ": Received the " + msg.getContent() + " from " + msg.getSender().getName() + ".");
				
			}
		}

		@Override
		public boolean done() {
			return finished;
		}
		
	}
}


