package agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import map.GarbageContainer;
import map.Point;
import map.Road;
import ai.Goal;
import ai.Path;
import ai.Plan;
import units.Truck;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class TruckAgent extends Agent{
	
	private static final long serialVersionUID = 2394071613389642100L;
	
	private Truck truck;
	
	public Truck getTruck() {
		return truck;
	}
	
	public void setTruck(Truck t){
		this.truck = t;
	}
	
	public AID getAIDFromTruckName(Truck t){
		return getAID();
	}
	
	protected void setup() {
		Object[] args = getArguments();
		this.truck = (Truck) args[0];
		System.out.println("My name is " + this.getAID().getName() + " and I'm active now.");
		
		addBehaviour(new readyBehaviour(this));
		
		addBehaviour(new receiveOptimalPlan(this, this.truck));
	}
	
	
	/**
	 * 
	 * @author ruivalentemaia
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
			MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
			MessageTemplate m2 = MessageTemplate.MatchOntology("InitialMessage");
			MessageTemplate m3 = MessageTemplate.MatchContent("Ready?");
			
			MessageTemplate m1_and_m2 = MessageTemplate.and(m1, m2);
			MessageTemplate m12_and_m3 = MessageTemplate.and(m1_and_m2, m3);
			
			ACLMessage msg = receive(m12_and_m3);
			
			if(msg != null){
				System.out.println("Truck " + this.getAgent().getLocalName() + ": Received the " + msg.getContent() + " from " + msg.getSender().getLocalName() + ".");
				
				ACLMessage reply = msg.createReply();
				
				AID sender = msg.getSender();
				if(sender != null) reply.addReceiver(msg.getSender());
				
				String replyWith = msg.getReplyWith();
				if(replyWith != null) reply.setContent(replyWith);
				else reply.setContent("Yes. I'm ready.");
				
				System.out.println(getAID().getLocalName() + ": Sent the \"Yes. I'm ready.\" reply to " + msg.getSender().getLocalName() + ".");
				reply.setOntology("InitialMessage");
				
				send(reply);
				
				finished = true;
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
	
	/**
	 * 
	 * @author ruivalentemaia
	 *
	 */
	class receiveOptimalPlan extends SimpleBehaviour {
		
		private static final long serialVersionUID = 6200150863055431193L;
		
		private boolean finished = false;
		private Truck truck;
		private String tempFilePath = System.getProperty("user.dir") + "/temp";
		
		public receiveOptimalPlan(Agent a, Truck t){
			super(a);
			this.truck = t;
		}

		public Truck getTruck() {
			return truck;
		}

		public void setTruck(Truck truck) {
			this.truck = truck;
		}
		
		public String getTempFilePath() {
			return tempFilePath;
		}

		public void setTempFilePath(String tempFilePath) {
			this.tempFilePath = tempFilePath;
		}
		
		/**
		 * 
		 * @param filename
		 * @return
		 * @throws ParserConfigurationException
		 * @throws SAXException
		 * @throws IOException
		 */
		public Plan importPlanFromXML(String filename) throws ParserConfigurationException, SAXException, IOException {
			File fXmlFile = new File(this.tempFilePath + "/" + filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			doc.getDocumentElement().normalize();
			
			String truck = doc.getElementsByTagName("truck").item(0).getTextContent();
			
			NodeList nList = doc.getElementsByTagName("assignment");
			HashMap<GarbageContainer, Double> map = new HashMap<GarbageContainer, Double>();
			for(int temp = 0; temp < nList.getLength(); temp++){
				Node nNode = nList.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					Element gcElement = (Element) eElement.getElementsByTagName("garbageContainer").item(0);
					int gcId = Integer.parseInt(gcElement.getTextContent());
					
					Element amountToCollect = (Element) eElement.getElementsByTagName("amountToCollect").item(0);
					double amount = Double.parseDouble(amountToCollect.getTextContent());
					
					map.put(this.truck.getCompleteCityMap().selectGCFromId(gcId), amount);
				}
			}
			
			return new Plan(this.truck.getCompleteCityMap().selectTruckFromName(truck), map);
		}
		
		
		public List<Goal> buildGoalsList(Plan p) {
			List<Goal> goals = new ArrayList<Goal>();
			List<GarbageContainer> garbageContainers = p.getAllGarbageContainers();
			
			/*
			 * checks if there is extra garbage that the TruckAgent needs to collect
			 * an nth time because it can't do it all at the first time.
			 */
			//TODO
			double totalSum = 0;
			
			//build a list of startPoints
			List<Point> startPoints = new ArrayList<Point>();
			startPoints.add(this.truck.getStartPosition());
			Iterator<GarbageContainer> gcIt = garbageContainers.iterator();
			while(gcIt.hasNext()){
				GarbageContainer gc = gcIt.next();
				Road gcRoad = this.truck.getCompleteCityMap().selectRoadFromGarbageContainer(gc.getPosition());
				startPoints.add(this.truck.getCompleteCityMap().selectPointFromRoad(gcRoad, gc.getPosition()));
			}
			
			//build a list of endPoints
			List<Point> endPoints = new ArrayList<Point>();
			gcIt = garbageContainers.iterator();
			while(gcIt.hasNext()){
				GarbageContainer gc = gcIt.next();
				Road gcRoad = this.truck.getCompleteCityMap().selectRoadFromGarbageContainer(gc.getPosition());
				endPoints.add(this.truck.getCompleteCityMap().selectPointFromRoad(gcRoad, gc.getPosition()));
			}
			endPoints.add(this.truck.getStartPosition());
			
			//build a list of Goals.
			Iterator<Point> itPoint = startPoints.iterator();
			int counter = 0;
			while(itPoint.hasNext()){
				Point startPoint = itPoint.next();
				Point endPoint = endPoints.get(counter);
				Goal g = new Goal(counter+1,startPoint, endPoint);
				Path path = new Path(counter+1);
				g.setBestPath(path);
				counter++;
				goals.add(g);
			}
			
			return goals;
		}
		

		@Override
		public void action() {
			MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
			MessageTemplate m2 = MessageTemplate.MatchOntology("PlanOntology");
			
			MessageTemplate m1_and_m2 = MessageTemplate.and(m1, m2);
			
			ACLMessage msg = receive(m1_and_m2);
			
			if(msg != null){
				Plan plan = null;
				String filename = "";
				try {
					filename = msg.getContent();
					if(filename != "") plan = this.importPlanFromXML(filename);
				} catch (ParserConfigurationException | SAXException | IOException e) {
					e.printStackTrace();
				}
				
				if(plan != null){
					Truck t = null;
					try {
						t = new Truck(this.truck.getId(), this.truck.getTruckName(), this.truck.getGarbageType());
						t.setCompleteCityMap(this.truck.getCompleteCityMap());
					} catch (ParserConfigurationException | SAXException
							| IOException e) {
						e.printStackTrace();
					}
					
					if(t != null) {
						Point startingPoint = t.selectStartingPoint();
						t.setStartPosition(startingPoint);
						t.setCurrentPosition(startingPoint);
						t.setGoals(this.buildGoalsList(plan));
						t.buildTotalPathPlanning(2);
						
						this.truck = t;
						
						System.out.println(getAID().getLocalName() + ": Received and approved the optimal plan from " + msg.getSender().getLocalName() + ".");
						
						finished = true;
					}
				}
			}
			else{
				block();
			}
		}

		@Override
		public boolean done() {
			return finished;
		}
		
	}
}


