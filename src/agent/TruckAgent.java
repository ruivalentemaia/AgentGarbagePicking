package agent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
	private String truckFilename;
	
	public Truck getTruck() {
		return truck;
	}
	
	public void setTruck(Truck t){
		this.truck = t;
	}
	
	public String getTruckFilename() {
		return truckFilename;
	}

	public void setTruckFilename(String truckFilename) {
		this.truckFilename = truckFilename;
	}

	public AID getAIDFromTruckName(Truck t){
		return getAID();
	}
	
	protected void setup() {
		Object[] args = getArguments();
		this.truck = (Truck) args[0];
		this.truckFilename = "truck" + this.truck.getTruckName() + ".xml";
		
		System.out.println("My name is " + this.getAID().getName() + " and I'm active now.");
		
		addBehaviour(new readyBehaviour(this));
		
		addBehaviour(new receiveOptimalPlan(this, this.truck, this.truckFilename));
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
		private String filename;
		private int state;
		
		public receiveOptimalPlan(Agent a, Truck t, String filename){
			super(a);
			this.truck = t;
			this.filename = filename;
			this.state = 1;
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
		
		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
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
		
		
		public void exportTruckInformation(String filename, Truck t) throws ParserConfigurationException, TransformerException{
			/*
			 * private int id;
			 * private String truckName;
			 * private String garbageType;
			 * private Point startPosition;
			 * private Point currentPosition;
			 * private double currentOccupation;
			 * private double maxCapacity;
			 * private List<Point> pathWalked;
			 * private List<Point> pathToBeWalked;
			 * private List<Goal> goals;
			 * private List<GarbageContainer> garbageContainersToGoTo;
			 * //complete list of the CityMap points
			 * private CityMap completeCityMap;
			 * private Options options;
			 */
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement("truck");
			doc.appendChild(rootElement);
			
			Element id = doc.createElement("id");
			id.appendChild(doc.createTextNode(Integer.toString(t.getId())));
			rootElement.appendChild(id);
			
			Element truckName = doc.createElement("name");
			truckName.appendChild(doc.createTextNode(t.getTruckName()));
			rootElement.appendChild(truckName);
			
			Element garbageType = doc.createElement("garbageType");
			garbageType.appendChild(doc.createTextNode(t.getGarbageType()));
			rootElement.appendChild(garbageType);
			
			Element startPosition = doc.createElement("startPosition");
			startPosition.setAttribute("type", t.getStartPosition().getType());
			startPosition.setAttribute("x", Integer.toString(t.getStartPosition().getX()));
			startPosition.setAttribute("y", Integer.toString(t.getStartPosition().getY()));
			rootElement.appendChild(startPosition);
			
			Element currentPosition = doc.createElement("currentPosition");
			currentPosition.setAttribute("type", t.getCurrentPosition().getType());
			currentPosition.setAttribute("x", Integer.toString(t.getCurrentPosition().getX()));
			currentPosition.setAttribute("y", Integer.toString(t.getCurrentPosition().getY()));
			rootElement.appendChild(currentPosition);
			
			Element currentOccupation = doc.createElement("currentOccupation");
			currentOccupation.appendChild(doc.createTextNode(Double.toString(t.getCurrentOccupation())));
			rootElement.appendChild(currentOccupation);
			
			Element maxCapacity = doc.createElement("maxCapacity");
			maxCapacity.appendChild(doc.createTextNode(Double.toString(t.getMaxCapacity())));
			rootElement.appendChild(maxCapacity);
			
			if(t.getPathWalked().size() > 0) {
				Element pathWalked = doc.createElement("pathWalked");
				Iterator<Point> itPathWalked = t.getPathWalked().iterator();
				while(itPathWalked.hasNext()){
					Point p = itPathWalked.next();
					
					Element point = doc.createElement("pathWalkedPoint");
					point.setAttribute("x", Integer.toString(p.getX()));
					point.setAttribute("y", Integer.toString(p.getY()));
					pathWalked.appendChild(point);
				}
				rootElement.appendChild(pathWalked);
			}
			
			Element pathToBeWalked = doc.createElement("pathToBeWalked");
			Iterator<Point> itPathToBeWalked = t.getPathToBeWalked().iterator();
			while(itPathToBeWalked.hasNext()){
				Point p = itPathToBeWalked.next();
				
				Element point = doc.createElement("pathToBeWalkedPoint");
				point.setAttribute("x", Integer.toString(p.getX()));
				point.setAttribute("y", Integer.toString(p.getY()));
				pathToBeWalked.appendChild(point);
			}
			rootElement.appendChild(pathToBeWalked);
			
			Element goals = doc.createElement("goals");
			Iterator<Goal> itGoal = t.getGoals().iterator();
			while(itGoal.hasNext()){
				Goal g = itGoal.next();
				
				Element goal = doc.createElement("goal");
				
				Element goalId = doc.createElement("id");
				goalId.appendChild(doc.createTextNode(Integer.toString(g.getId())));
				goal.appendChild(goalId);
				
				Element startPoint = doc.createElement("startPoint");
				startPoint.setAttribute("x", Integer.toString(g.getStartPoint().getX()));
				startPoint.setAttribute("y", Integer.toString(g.getStartPoint().getY()));
				goal.appendChild(startPoint);
				
				Element endPoint = doc.createElement("endPoint");
				endPoint.setAttribute("x", Integer.toString(g.getEndPoint().getX()));
				endPoint.setAttribute("y", Integer.toString(g.getEndPoint().getY()));
				goal.appendChild(endPoint);
				
				Element bestPath = doc.createElement("bestPath");
				
				Element bestPathId = doc.createElement("id");
				bestPathId.appendChild(doc.createTextNode(Integer.toString(g.getBestPath().getId())));
				bestPath.appendChild(bestPathId);
				
				Element bestPathLength = doc.createElement("length");
				bestPathLength.appendChild(doc.createTextNode(Integer.toString(g.getBestPath().getLength())));
				bestPath.appendChild(bestPathLength);
				
				Element bestPathPoints = doc.createElement("points");
				
				Iterator<Point> itBPPoint = g.getBestPath().getPoints().iterator();
				while(itBPPoint.hasNext()){
					Point bpPoint = itBPPoint.next();
					
					Element bpPointElem = doc.createElement("point");
					bpPointElem.setAttribute("x", Integer.toString(bpPoint.getX()));
					bpPointElem.setAttribute("y", Integer.toString(bpPoint.getY()));
					bestPathPoints.appendChild(bpPointElem);
				}
				bestPath.appendChild(bestPathPoints);
				
				goal.appendChild(bestPath);
				
				goals.appendChild(goal);
			}
			rootElement.appendChild(goals);
			
			if(t.getGarbageContainersToGoTo().size() > 0) {
				Element garbageContainersToGo = doc.createElement("garbageContainersToGo");
				Iterator<GarbageContainer> itGC = t.getGarbageContainersToGoTo().iterator();
				while(itGC.hasNext()){
					GarbageContainer gc = itGC.next();
					
					Element garbageContainer = doc.createElement("garbageContainer");
					
					Element gcId = doc.createElement("id");
					gcId.appendChild(doc.createTextNode(Integer.toString(gc.getId())));
					garbageContainer.appendChild(gcId);
					
					Element gcType = doc.createElement("type");
					gcType.appendChild(doc.createTextNode(gc.getType()));
					garbageContainer.appendChild(gcType);
					
					Element gcCurrentOccupation = doc.createElement("currentOccupation");
					gcCurrentOccupation.appendChild(doc.createTextNode(Double.toString(gc.getCurrentOccupation())));
					garbageContainer.appendChild(gcCurrentOccupation);
					
					Element gcMaxCapacity = doc.createElement("maxCapacity");
					gcMaxCapacity.appendChild(doc.createTextNode(Double.toString(gc.getMaxCapacity())));
					garbageContainer.appendChild(gcMaxCapacity);
					
					Element gcPosition = doc.createElement("position");
					gcPosition.setAttribute("x", Integer.toString(gc.getPosition().getX()));
					gcPosition.setAttribute("y", Integer.toString(gc.getPosition().getY()));
					garbageContainer.appendChild(gcPosition);
					
					garbageContainersToGo.appendChild(garbageContainer);
				}
				rootElement.appendChild(garbageContainersToGo);
			}
			
			Element cityMap = doc.createElement("map");
			String cityMapFilename = t.getCompleteCityMap().getMapsFileName();
			if(cityMapFilename != null){
				cityMap.appendChild(doc.createTextNode(t.getCompleteCityMap().getMapsFileName()));
				rootElement.appendChild(cityMap);
			}
			
			Element options = doc.createElement("options");
			options.appendChild(doc.createTextNode(t.getOptions().getOptionsFile()));
			rootElement.appendChild(options);
			
			File f = new File(this.tempFilePath);
			f.setExecutable(true);
			f.setReadable(true);
			f.setWritable(true);
			File file = new File(f.toString() + "/" + filename);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			
			transformer.transform(source, result);
		}
		

		@Override
		public void action() {
			switch(this.state){
			/*
			 * Receives Plan, processes again the goals of this Truck,
			 * exports its information to an XML file and goes to the next
			 * state.
			 */
			case 1:
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
							t.getCompleteCityMap().setMapsFileName(this.filename);
							t.setMaxCapacity(this.truck.getMaxCapacity());
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
							
							try {
								this.exportTruckInformation(this.filename, this.truck);
							} catch (ParserConfigurationException e) {
								e.printStackTrace();
							} catch (TransformerException e) {
								e.printStackTrace();
							}
							
							System.out.println(getAID().getLocalName() + ": Received and approved the optimal plan from " + msg.getSender().getLocalName() + ".");
							this.state = 2;
						}
					}
				}
				else{
					block();
				}
				break;
				
				/*
				 * 
				 */
				case 2:
					this.state = 3;
					break;
				default:
					System.out.println(getAID().getLocalName() + " is exiting.");
					finished = true;
					break;
			}
			
			
		}

		@Override
		public boolean done() {
			return finished;
		}
	}
}


