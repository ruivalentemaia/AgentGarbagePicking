package agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ai.Options;
import units.Truck;
import map.CityMap;
import map.Point;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PrinterAgent extends Agent{

	private static final long serialVersionUID = -3764121063294481538L;
	private CityMap cityMap;
	
	public CityMap getCityMap() {
		return cityMap;
	}

	public void setCityMap(CityMap cityMap) {
		this.cityMap = cityMap;
	}

	protected void setup() {
		Object[] args = getArguments();
		this.cityMap = (CityMap) args[0];
		System.out.println("My name is " + getAID().getName() + " and I'm active now.");
		
		addBehaviour(new receiveTruckInformation(this, this.cityMap));
	}
	
	protected void takeDown() {
		this.doDelete();
	}
	
	/**
	 * 
	 * @author ruivalentemaia
	 *
	 */
	class receiveTruckInformation extends SimpleBehaviour {
		private static final long serialVersionUID = 267032967465024888L;
		
		private boolean finished = false;
		private CityMap cityMap;
		private String cityMapString;
		private int nMessages;
		private List<AID> senders;
		private Options options;
		
		public CityMap getCityMap() {
			return cityMap;
		}

		public void setCityMap(CityMap cityMap) {
			this.cityMap = cityMap;
		}

		public String getCityMapString() {
			return cityMapString;
		}

		public void setCityMapString(String cityMapString) {
			this.cityMapString = cityMapString;
		}

		public int getnMessages() {
			return nMessages;
		}

		public void setnMessages(int nMessages) {
			this.nMessages = nMessages;
		}

		public Options getOptions() {
			return options;
		}

		public void setOptions(Options options) {
			this.options = options;
		}

		public receiveTruckInformation(Agent a, CityMap cityMap){
			super(a);
			this.cityMap = cityMap;
			this.cityMapString = "";
			this.nMessages = 0;
			this.senders = new ArrayList<AID>();
			this.options = new Options();
			
			try {
				this.options.importOptions("options.xml");
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void action() {
			this.nMessages = 0;
			
			if(this.getAgent().getCurQueueSize() > 0) {
				MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				MessageTemplate m2 = MessageTemplate.MatchOntology("Print");
				
				MessageTemplate m1_and_m2 = MessageTemplate.and(m1, m2);
				
				ACLMessage msg = receive(m1_and_m2);
				
				if(msg != null){
					String content = msg.getContent();
					String[] parts = content.split(",");
					int id = Integer.parseInt(parts[0]);
					int cX = Integer.parseInt(parts[1]);
					int cY = Integer.parseInt(parts[2]);
					String gType = parts[3];
					double cOccupation = Double.parseDouble(parts[4]);
					double maxOccupation = Double.parseDouble(parts[5]);
					
					Truck t = this.cityMap.selectTruckFromId(id);
					t.setCurrentPosition(new Point(cX, cY));
					t.setGarbageType(gType);
					t.setCurrentOccupation(cOccupation);
					t.setMaxCapacity(maxOccupation);
					
					this.cityMap.updateTruck(t);
					
					String cityMapString = this.cityMap.getCityMapString();
					this.cityMapString = cityMapString;
					
					if(this.options.isActiveConsolePrinting())
						System.out.println(getAID().getLocalName() + " : Received Truck Information from " + msg.getSender().getLocalName() + ".");
					
					if(this.senders.size() > 0){
						if(!this.senders.contains(msg.getSender())) {
							this.senders.add(msg.getSender());
							this.nMessages++;
						}
					}
					else {
						this.senders.add(msg.getSender());
						this.nMessages++;
					}
					
					if(this.senders.size() == this.nMessages){
						try {
							Runtime.getRuntime().exec("clear");
						} catch (IOException e) {
							e.printStackTrace();
						}
						this.getAgent().doWait(500);
						System.out.println(this.cityMapString);
						this.senders = new ArrayList<AID>();
					}
				}
				else {
					block();
				}
			}
			else {
				block();
			}
		}

		@Override
		public boolean done() {
			return finished;
		}
		
	}

}
