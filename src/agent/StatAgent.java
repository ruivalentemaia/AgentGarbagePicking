package agent;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import units.Truck;

public class StatAgent extends Agent{

	private static final long serialVersionUID = -6774614096214844234L;
	
	private List<String> trucks;
	private List<Double> maxCapacities;
	private List<Double> garbageCollected;
	private List<Integer> distancesCovered;
	
	public List<String> getTrucks() {
		return trucks;
	}

	public void setTrucks(List<String> trucks) {
		this.trucks = trucks;
	}

	public List<Double> getMaxCapacities() {
		return maxCapacities;
	}

	public void setMaxCapacities(List<Double> maxCapacities) {
		this.maxCapacities = maxCapacities;
	}

	public List<Double> getGarbageCollected() {
		return garbageCollected;
	}

	public void setGarbageCollected(List<Double> garbageCollected) {
		this.garbageCollected = garbageCollected;
	}

	public List<Integer> getDistancesCovered() {
		return distancesCovered;
	}

	public void setDistancesCovered(List<Integer> distancesCovered) {
		this.distancesCovered = distancesCovered;
	}

	protected void setup() {
		this.trucks = new ArrayList<String>();
		this.maxCapacities = new ArrayList<Double>();
		this.garbageCollected = new ArrayList<Double>();
		this.distancesCovered = new ArrayList<Integer>();
		
		System.out.println("My name is " + getAID().getName() + " and I'm active now.");
		
		addBehaviour(new getStatisticsFromTrucks(this, this.trucks, this.maxCapacities, this.garbageCollected, this.distancesCovered));
	}
	
	protected void takeDown() {
		this.doDelete();
	}

	
	/**
	 * 
	 * @author ruivalentemaia
	 *
	 */
	class getStatisticsFromTrucks extends SimpleBehaviour {
		
		private static final long serialVersionUID = 7555777417414678217L;
		
		private boolean finished = false;
		private List<Double> garbageCollected;
		private List<Integer> distancesCovered;
		private List<String> trucks;
		private List<Double> maxCapacities;
		private int state = 1;
		
		private String statsFilePath = System.getProperty("user.dir") + "/statistics";
		
		public List<Double> getGarbageCollected() {
			return garbageCollected;
		}

		public void setGarbageCollected(List<Double> garbageCollected) {
			this.garbageCollected = garbageCollected;
		}

		public List<Integer> getDistancesCovered() {
			return distancesCovered;
		}

		public void setDistancesCovered(List<Integer> distancesCovered) {
			this.distancesCovered = distancesCovered;
		}

		public List<String> getTrucks() {
			return trucks;
		}

		public void setTrucks(List<String> trucks) {
			this.trucks = trucks;
		}

		public List<Double> getMaxCapacities() {
			return maxCapacities;
		}

		public void setMaxCapacities(List<Double> maxCapacities) {
			this.maxCapacities = maxCapacities;
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

		public String getStatsFilePath() {
			return statsFilePath;
		}

		public void setStatsFilePath(String statsFilePath) {
			this.statsFilePath = statsFilePath;
		}

		public getStatisticsFromTrucks(Agent a, List<String> trucks, List<Double> maxCapacities, List<Double> gCollected, List<Integer> dCovered) {
			super(a);
			this.trucks = new ArrayList<String>();
			this.trucks = trucks;
			this.maxCapacities = new ArrayList<Double>();
			this.maxCapacities = maxCapacities;
			this.garbageCollected = new ArrayList<Double>();
			this.garbageCollected = gCollected;
			this.distancesCovered = new ArrayList<Integer>();
			this.distancesCovered = dCovered;
		}
		
		/*
		 * Calculates the stopping condition.
		 */
		public boolean saveStatistics(){
			boolean save = false;
			
			//counts the number of TruckAgents in the service.
			int counter = 0;
			AMSAgentDescription [] agents = null;
	        try {
	            SearchConstraints c = new SearchConstraints();
	            c.setMaxResults ( new Long(-1) );
	            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
	        }
	        catch (Exception e) { e.printStackTrace();}
			
	        for (int i=0; i<agents.length;i++) {
	            AID agentID = agents[i].getName();
	            if(agentID.getLocalName().charAt(0) == 't' || agentID.getLocalName().equals("E")) {
	            	counter++;
	            }
	            else continue;
	        }
	        
	        int numberOfEntries = this.trucks.size();
	        if( (numberOfEntries == counter) && (numberOfEntries == this.maxCapacities.size()) 
	         && (numberOfEntries == this.garbageCollected.size()) && (numberOfEntries == this.distancesCovered.size())) {
	        	save = true;
	        }
			
			return save;
		}
		
		@Override
		public void action() {
			
			switch(this.state) {
				case 1:
					MessageTemplate m1 = MessageTemplate.MatchOntology("StatsTruck");
					MessageTemplate m2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					
					MessageTemplate m1_and_m2 = MessageTemplate.and(m1, m2);
					
					ACLMessage msg = receive(m1_and_m2);
					
					if(msg != null && msg.getContent() != ""){
						String content = msg.getContent();
						if(content != null) {
							String[] parts = content.split(";");
							
							//Truck name.
							if(parts[0] != null){
								this.trucks.add(parts[0]);
							}
							
							//Max Capacity.
							if(parts[1] != null){
								double mCap = Double.parseDouble(parts[1]);
								this.maxCapacities.add(mCap);
							}
							
							//Distance covered.
							if(parts[2] != null){
								int distance = Integer.parseInt(parts[2]);
								this.distancesCovered.add(distance);
							}
							
							//Garbage Collected.
							if(parts[3] != null){
								double gCollected = Double.parseDouble(parts[3]);
								this.garbageCollected.add(gCollected);
							}
							
							System.out.println(getAID().getLocalName() + " : Received and treat statistics from " + msg.getSender().getLocalName());
						}
						else {
							block();
						}
					}
					else {
						block();
					}
					
					if(this.saveStatistics())
						this.state = 2;
					else this.state = 1;
					
					break;
				case 2:
					try {
						FileWriter writer = new FileWriter(this.statsFilePath + "/stats.csv");
						int size = this.trucks.size();
						String toExport = "";
						toExport += "Truck,MaxCapacity,GarbageCollected,DistanceCovered\n";
						writer.append(toExport);
						for(int i = 0; i < size; i++){
							toExport = "";
							toExport += this.trucks.get(i);
							toExport += ", " + this.maxCapacities.get(i);
							toExport += ", " + this.garbageCollected.get(i);
							toExport += ", " + this.distancesCovered.get(i);
							toExport += "\n";
							writer.append(toExport);
						}
						writer.flush();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					this.state = 3;
					break;
				case 3:
					this.finished = true;
					this.state  = 4;
					break;
				default:
					System.out.println(getAID().getLocalName() + " : I'm finished and I'm leaving.");
					break;
			}
			
		}

		@Override
		public boolean done() {
			return finished;
		}
		
	}
}
