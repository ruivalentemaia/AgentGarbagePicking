package agent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

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

import map.CityMap;
import map.GarbageContainer;
import map.Point;
import map.Road;
import ai.Goal;
import ai.Options;
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
	private Options options;
	
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
		
		this.options = new Options();
		try {
			this.options.importOptions("options.xml");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		
		if(options.isAgentsKnowMap()) {
			addBehaviour(new readyBehaviour(this, this.options));
			addBehaviour(new receiveOptimalPlan(this, this.truck, this.truckFilename, this.options));
		}
		
		else {
			addBehaviour(new noMapBehaviour(this, this.truck));
		}
		
	}
	
	protected void takeDown() {
		this.doDelete();
	}
	
	
	/**
	 * 
	 * @author ruivalentemaia
	 *
	 */
	class readyBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 752338739127971004L;
		private boolean finished = false;
		private Options options;
		
		public Options getOptions() {
			return options;
		}

		public void setOptions(Options options) {
			this.options = options;
		}

		public readyBehaviour(Agent a, Options options){
			super(a);
			this.setOptions(options);
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
				if(this.options.isActiveConsolePrinting())
					System.out.println("Truck " + this.getAgent().getLocalName() + ": Received the " + msg.getContent() + " from " + msg.getSender().getLocalName() + ".");
				
				ACLMessage reply = msg.createReply();
				
				AID sender = msg.getSender();
				if(sender != null) reply.addReceiver(msg.getSender());
				
				String replyWith = msg.getReplyWith();
				if(replyWith != null) reply.setContent(replyWith);
				else reply.setContent("Yes. I'm ready.");
				
				if(this.options.isActiveConsolePrinting())
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
		private Plan plan;
		private Options options;
		
		public receiveOptimalPlan(Agent a, Truck t, String filename, Options options){
			super(a);
			this.truck = t;
			this.filename = filename;
			this.state = 1;
			this.options = options;
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

		public Plan getPlan() {
			return plan;
		}

		public void setPlan(Plan plan) {
			this.plan = plan;
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
			HashMap<GarbageContainer, Boolean> cRegistry = new HashMap<GarbageContainer, Boolean>();
			for(int temp = 0; temp < nList.getLength(); temp++){
				Node nNode = nList.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					Element gcElement = (Element) eElement.getElementsByTagName("garbageContainer").item(0);
					int gcId = Integer.parseInt(gcElement.getTextContent());
					
					Element amountToCollect = (Element) eElement.getElementsByTagName("amountToCollect").item(0);
					double amount = Double.parseDouble(amountToCollect.getTextContent());
					
					map.put(this.truck.getCompleteCityMap().selectGCFromId(gcId), amount);
					cRegistry.put(this.truck.getCompleteCityMap().selectGCFromId(gcId), false);
				}
			}
			
			return new Plan(this.truck.getCompleteCityMap().selectTruckFromName(truck), map, cRegistry);
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
							t.setGarbageContainersToGoTo(plan.getAllGarbageContainers());
							this.plan = plan;
							t.buildTotalPathPlanning(2);
							
							this.truck = t;
							
							try {
								this.exportTruckInformation(this.filename, this.truck);
							} catch (ParserConfigurationException e) {
								e.printStackTrace();
							} catch (TransformerException e) {
								e.printStackTrace();
							}
							
							if(this.options.isActiveConsolePrinting())
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
			 * Iterates over the pathToBeWalked list until it finds a 
			 * position that corresponds to a position where garbage
			 * should be collected.
			 */
			case 2:
				List<Goal> goals = this.truck.getGoals();
				int pathSize = this.truck.getPathToBeWalked().size();
				int stepCounter = 0;
				Iterator<Point> pathToBeWalked = this.truck.getPathToBeWalked().iterator();
				
				while(pathToBeWalked.hasNext()){
					Point firstPoint = pathToBeWalked.next();
					pathToBeWalked.remove();
					
					this.truck.setCurrentPosition(firstPoint);
					stepCounter++;
					this.truck.getPathWalked().add(firstPoint);
					this.truck.getCompleteCityMap().updateTruckPosition(this.truck);
					
					Iterator<Goal> itGoal = goals.iterator();
					while(itGoal.hasNext()){
						Goal g = itGoal.next();
						
						if(this.truck.positionClosestToGoal(firstPoint, g)){
							this.state = 3;
							break;
						}
					}
					if(this.options.isActiveConsolePrinting())
						System.out.println(getAID().getLocalName() + " : Moved to (" + firstPoint.getX() + ", " + firstPoint.getY() + ").");
					
					if(this.state == 3) break;
					
					this.state = 4;
					break;
				}
				
				//stop condition.
				if( pathSize == stepCounter){
					this.state = 5;
				}
				break;
			
			/*
			 * Collects Garbage from a GarbageContainer.
			 */
			case 3:
				Iterator<Goal> itGoal = this.truck.getGoals().iterator();
				
				while(itGoal.hasNext()){
					Goal g = itGoal.next();
					HashMap<GarbageContainer, Double> assignment = this.plan.getAssignment();
					Iterator assignmentIt = assignment.entrySet().iterator();
					int counter = 1;
					
					while(assignmentIt.hasNext()){
						Map.Entry<GarbageContainer, Double> pairs = (Entry<GarbageContainer, Double>) assignmentIt.next();
						int truckX = this.truck.getCurrentPosition().getX();
						int truckY = this.truck.getCurrentPosition().getY();
						int gcPosX = pairs.getKey().getPosition().getX();
						int gcPosY = pairs.getKey().getPosition().getY();
						int diffPosX = Math.abs(gcPosX - g.getEndPoint().getX());
						int diffPosY = Math.abs(gcPosY - g.getEndPoint().getY());
						int diffTruckGCX = Math.abs(truckX - gcPosX);
						int diffTruckGCY = Math.abs(truckY - gcPosY);
						
						if( (truckX == gcPosX || truckX == gcPosX - 1 || truckX == gcPosX +1) &&
							(truckY == gcPosY || truckY == gcPosY - 1 || truckY == gcPosY + 1) &&
							(diffTruckGCX == 1 || diffTruckGCY == 1) && !(diffTruckGCX == 1 && diffTruckGCY == 1) &&
							(diffPosX == 1 || diffPosY == 1) && !(diffPosX == 1 && diffPosY == 1) ) {
							
							if(!this.plan.getCollectedRegistry().get(pairs.getKey())) {
								this.truck.collectGarbage(pairs.getKey(), pairs.getValue());
								this.plan.changeValueOfCollectRegistry(pairs.getKey());
								if(this.options.isActiveConsolePrinting())
									System.out.println(getAID().getLocalName() + ": Collected " + pairs.getValue() + " kg of garbage in (" + gcPosX + ", " + gcPosY + ").");
								break;
							}
						}
						counter++;
					}
					
					boolean allCollected = false;
					Iterator cRegistryIt = this.plan.getCollectedRegistry().entrySet().iterator();
					int size = this.plan.getCollectedRegistry().size();
					int counterTrue = 0;
					while(cRegistryIt.hasNext()){
						Map.Entry<GarbageContainer, Boolean> pairs = (Entry<GarbageContainer, Boolean>) cRegistryIt.next();
						if(pairs.getValue()){
							counterTrue++;
						}
					}
					
					if(counterTrue == size) allCollected = true;
					if(allCollected) break;
				}
				this.state = 4;
				break;
				
			/*
			 * Sends a message to PrinterAgent containing the updated information
			 * of the Truck.
			 */
			case 4:
				ACLMessage printMsg = new ACLMessage(ACLMessage.INFORM);
				printMsg.setOntology("Print");
				printMsg.setContent(this.truck.getId() + "," +
									this.truck.getCurrentPosition().getX() + "," +
									this.truck.getCurrentPosition().getY() + "," +
									this.truck.getGarbageType() + "," +
									this.truck.getCurrentOccupation() + "," +
									this.truck.getMaxCapacity());
				
				AMSAgentDescription [] agents = null;
		        try {
		            SearchConstraints c = new SearchConstraints();
		            c.setMaxResults ( new Long(-1) );
		            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
		        }
		        catch (Exception e) { e.printStackTrace();}
				
		        String t = "printer";
		        for (int i=0; i<agents.length;i++) {
		            AID agentID = agents[i].getName();
		            if(agentID.getLocalName().equals(t)) {
		            	printMsg.addReceiver(agentID);
		            }
		        }
		        
		        send(printMsg);
				
		        if(this.options.isActiveConsolePrinting())
		        	System.out.println(getAID().getLocalName() + " : Sent information to printerAgent.");
		        this.state = 2;
		        
				break;
				
			default:
				if(this.options.isActiveConsolePrinting())
					System.out.println(getAID().getLocalName() + ": Finished working and I'm exiting.");
				
				addBehaviour(new sendStatistics(myAgent, this.truck));
				this.finished = true;
				break;
			}
		}

		@Override
		public boolean done() {
			return this.finished;
		}
	}
	
	/**
	 * 
	 * @author ruivalentemaia
	 *
	 */
	class sendStatistics extends SimpleBehaviour {

		private static final long serialVersionUID = 5490684900734207525L;
		
		private boolean finished = false;
		private Truck truck;
		
		public Truck getTruck() {
			return truck;
		}

		public void setTruck(Truck truck) {
			this.truck = truck;
		}

		public sendStatistics(Agent a, Truck t){
			super(a);
			this.setTruck(t);
		}

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology("StatsTruck");
			
			String content = "";
			if(this.truck.getTruckName() != null){
				content += this.truck.getTruckName();
			}
			else {
				block();
			}
			
			if(this.truck.getMaxCapacity() > 0){
				content += ";" + this.truck.getMaxCapacity();
			}
			else {
				block();
			}
			
			if( (this.truck.getPathWalked().size() > 0) && (this.truck.getPathToBeWalked().size() == 0)){
				content += ";" + Integer.toString(this.truck.getPathWalked().size());
			}
			else {
				block();
			}
			
			if((this.truck.getCurrentOccupation() > 0) && (this.truck.getPathToBeWalked().size() == 0)){
				content += ";" + this.truck.getCurrentOccupation();
			}
			else {
				block();
			}
			
			AMSAgentDescription [] agents = null;
	        try {
	            SearchConstraints c = new SearchConstraints();
	            c.setMaxResults ( new Long(-1) );
	            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
	        }
	        catch (Exception e) { e.printStackTrace();}
			
	        String t = "stats";
	        for (int i=0; i<agents.length;i++) {
	            AID agentID = agents[i].getName();
	            if(agentID.getLocalName().equals(t)) {
	            	msg.addReceiver(agentID);
	            }
	            else continue;
	        }
	        
	        if(content.split(";").length == 4)
	        	msg.setContent(content);
	        
			send(msg);
			
			System.out.println(getAID().getLocalName() + " : Sent statistics about " + this.truck.getTruckName() + " to stats.");
			
			this.finished = true;
		}

		@Override
		public boolean done() {
			return finished;
		}
		
	}
	
	class noMapBehaviour extends SimpleBehaviour {

		private static final long serialVersionUID = -324071559784597197L;
		
		private boolean finished = false;
		private int state;
		private Truck truck;
		private HashMap<GarbageContainer, Double> garbageCollected;
		private CityMap fullMap;
		private int counter;
		private double totalGarbageToCollect;
		private double totalGarbageCollected;
		private boolean stuck = false;
		private int maxIterations = 500;
		
		public boolean isFinished() {
			return finished;
		}

		public void setFinished(boolean finished) {
			this.finished = finished;
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}
		
		public Truck getTruck() {
			return truck;
		}

		public void setTruck(Truck truck) {
			this.truck = truck;
		}
		
		
		public double getTotalGarbageToCollect() {
			return totalGarbageToCollect;
		}

		public void setTotalGarbageToCollect(double totalGarbageToCollect) {
			this.totalGarbageToCollect = totalGarbageToCollect;
		}

		public noMapBehaviour(Agent a, Truck t) {
			super(a);
			
			this.truck = t;
			
			List<Goal> goals = new ArrayList<Goal>();
			this.truck.setGoals(goals);
			
			List<Point> pathToBeWalked = new ArrayList<Point>();
			this.truck.setPathToBeWalked(pathToBeWalked);
			
			List<GarbageContainer> garbageContainersToGoTo = new ArrayList<GarbageContainer>();
			this.truck.setGarbageContainersToGoTo(garbageContainersToGoTo);
			
			this.truck.setCurrentPosition(this.truck.getStartPosition());
			
			this.state = 1;
			this.garbageCollected = new HashMap<GarbageContainer, Double>();
			this.counter = 0;
			this.fullMap = this.truck.getCompleteCityMap();
			this.truck.getPathWalked().add(this.truck.getCurrentPosition());
			
			//count how many GCs the TruckAgent is supposed to get to.
			List<GarbageContainer> gContainers = new CopyOnWriteArrayList<GarbageContainer> (this.fullMap.getGarbageContainers());
			synchronized(gContainers){
				Iterator<GarbageContainer> itGC = gContainers.iterator();
				while(itGC.hasNext()){
					GarbageContainer gc = itGC.next();
					if(gc.getType().equals(this.truck.getGarbageType()))
						this.counter++;
				}
			}
			
			this.totalGarbageToCollect = this.truck.getCompleteCityMap().countAllGarbage();
			this.totalGarbageCollected = 0;
		}
		
		/**
		 * 
		 * @param cX
		 * @param pX
		 * @param nX
		 * @param cY
		 * @param pY
		 * @param nY
		 */
		public void collectGarbage(int cX, int pX, int nX, int cY, int pY, int nY){
			List<GarbageContainer> gContainers = new CopyOnWriteArrayList<GarbageContainer>(this.fullMap.getGarbageContainers());
			synchronized(gContainers){
				Iterator<GarbageContainer> itGContainer = gContainers.iterator();
				while(itGContainer.hasNext()){
					GarbageContainer gCont = itGContainer.next();
					int gContX = gCont.getPosition().getX();
					int gContY = gCont.getPosition().getY();
					
					if( ( (gContX == cX) || (gContX == pX) || (gContX == nX) ) &&
						( (gContY == cY) || (gContY == pY) || (gContY == nY)) ) {
						
						if(gCont.getType().equals(this.truck.getGarbageType())) {
					
							this.truck.getCompleteCityMap().getGarbageContainers().add(gCont);
							double currentOccupation = gCont.getCurrentOccupation();
							
							if( (currentOccupation <= (this.truck.getMaxCapacity() - this.truck.getCurrentOccupation())) &&
								(currentOccupation > 0)) {
								this.truck.setCurrentOccupation(this.truck.getCurrentOccupation() + currentOccupation);
								gCont.setCurrentOccupation(0);
								if(currentOccupation > 0) {
									this.garbageCollected.put(gCont, currentOccupation);
									this.totalGarbageCollected += currentOccupation;
									System.out.println(getAID().getLocalName() + " : 1Collected " + currentOccupation 
											+ " kg of garbage in (" + gCont.getPosition().getX() 
											+ ", " + gCont.getPosition().getY() + ").");
								
									addBehaviour(new garbageCollectedBehaviour(myAgent, this.truck, currentOccupation));
								}
								
								else break;
							}
							
							else if((currentOccupation > (this.truck.getMaxCapacity() - this.truck.getCurrentOccupation())) &&
								(currentOccupation > 0)){
								double valueToTake = this.truck.getMaxCapacity() - this.truck.getCurrentOccupation();
								this.truck.setCurrentOccupation(this.truck.getCurrentOccupation() + valueToTake);
								gCont.setCurrentOccupation(gCont.getCurrentOccupation() - valueToTake);
								if(valueToTake > 0) {
									this.garbageCollected.put(gCont, valueToTake);
									this.totalGarbageCollected += valueToTake;
									System.out.println(getAID().getLocalName() + " : 2Collected " + valueToTake 
											+ " kg of garbage in (" + gCont.getPosition().getX() 
											+ ", " + gCont.getPosition().getY() + ").");
								
									addBehaviour(new garbageCollectedBehaviour(myAgent, this.truck, valueToTake));
									Object[] args = getArguments();
									if(args != null){
										this.totalGarbageCollected += (double) args[0];
									}
								}
								else break;
							}
							else break;
						}
					}
				}
			}
		}
		

		@Override
		public void action() {
			switch(this.state){
				
				/*
				 * TruckAgent is moving.
				 */
				case 1:
					/*
					 * stop condition: the total amount of garbageCollected is equal
					 * to the total amount of garbage in the map.
					 */
					while((this.totalGarbageCollected != this.totalGarbageToCollect) ||
						  (this.counter <= this.maxIterations)){
							this.counter++;
							
							if(myAgent.getQueueSize() > 0){
								this.state = 3;
								break;
							}
							
							
							int currentX = this.truck.getCurrentPosition().getX();
							int currentY = this.truck.getCurrentPosition().getY();
							int previousX = currentX - 1;
							int nextX = currentX + 1;
							int previousY = currentY - 1;
							int nextY = currentY + 1;
							
							this.collectGarbage(currentX, previousX, nextX, currentY, previousY, nextY);
							
							/*
							 * Checks where to move.
							 */
							Point up = new Point(currentX, previousY);
							Point down = new Point(currentX, nextY);
							Point left = new Point(previousX, currentY);
							Point right = new Point(nextX, currentY);
							
							boolean isUpRoad = false;
							boolean isDownRoad = false;
							boolean isLeftRoad = false;
							boolean isRightRoad = false;
							
							if(this.fullMap.checkIfPointIsRoadOrCrossroads(up)) isUpRoad = true;
							if(this.fullMap.checkIfPointIsRoadOrCrossroads(down)) isDownRoad = true;
							if(this.fullMap.checkIfPointIsRoadOrCrossroads(left)) isLeftRoad = true;
							if(this.fullMap.checkIfPointIsRoadOrCrossroads(right)) isRightRoad = true;
							
							//the TruckAgent is on a CROSSROADS.
							if(isUpRoad && isDownRoad && isLeftRoad && isRightRoad){
								Random r = new Random();
								int val = r.nextInt(4-1) + 1;
								switch(val) {
									//move to the left of the map.
									case 1:
										if(!this.truck.verifyMovement(left, stuck)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(left);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
											stuck = false;
											break;
										}
										else {
											stuck = true;
											val = 2;
										}
										
									//move up on the map.
									case 2:
										if(!this.truck.verifyMovement(up, stuck)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(up);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
											stuck = false;
											break;
										}
										else {
											stuck = true;
											val = 3;
										}
									
									//move to the right of the map.
									case 3:
										if(!this.truck.verifyMovement(right, stuck)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(right);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
											stuck = false;
											break;
										}
										else {
											stuck = true;
											val = 4;
										}
									
									//move down on the map.
									case 4:
										if(!this.truck.verifyMovement(down, stuck)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(down);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");	
											stuck = false;
											break;
										}
										else {
											stuck = true;
											val = 1;
										}
									default:
										break;
								}
							}
							
							//the TruckAgent is somewhere where it can move up or down.
							else if(isUpRoad && isDownRoad && !isLeftRoad && !isRightRoad){
								Random r = new Random();
								int val = r.nextInt(2-1) + 1;
								switch(val){
									case 1:
										if(!this.truck.verifyMovement(up, stuck)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(up);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");	
											stuck = false;
											break;
										}								
										else {
											stuck = true;
											val = 2;
										}
									case 2:
										if(!this.truck.verifyMovement(down, stuck)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(down);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
											stuck = false;
											break;
										}
										else {
											stuck = true;
											val = 1;
										}
									default:
										break;
								}
							}
							//the TruckAgent is somewhere where it can move left or right.
							else if(isLeftRoad && isRightRoad && !isUpRoad && !isDownRoad){
								Random r = new Random();
								int val = r.nextInt(2-1) + 1;
								switch(val){
									case 1:
										if(!this.truck.verifyMovement(left, stuck)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(left);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
											stuck = false;
											break;
										}
										else {
											stuck = true;
											val = 2;
										}
									case 2:
										if(!this.truck.verifyMovement(right, stuck)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(right);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
											stuck = false;
											break;
										}
										else {
											stuck = true;
											val = 1;
										}
									default:
										break;
								}
							}
							//the TruckAgent is somewhere where it can only move up.
							else if(isUpRoad && !isDownRoad && !isLeftRoad && !isRightRoad){
								if(!this.truck.verifyMovement(up, stuck)){
									this.truck.getPathWalked().add(new Point(currentX, currentY));
									this.truck.setCurrentPosition(up);
									System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
									stuck = false;
								}
								else stuck = true;
							}
							//the TruckAgent is somewhere where it can only move down.
							else if(!isUpRoad && isDownRoad && !isLeftRoad && !isRightRoad) {
								if(!this.truck.verifyMovement(down, stuck)){
									this.truck.getPathWalked().add(new Point(currentX, currentY));
									this.truck.setCurrentPosition(down);
									System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
									stuck = false;
								}
								else stuck = true;
							}
							//the TruckAgent is somewhere where it can only move left.
							else if(isLeftRoad && !isRightRoad && !isUpRoad && !isDownRoad){
								if(!this.truck.verifyMovement(right, stuck)){
									this.truck.getPathWalked().add(new Point(currentX, currentY));
									this.truck.setCurrentPosition(left);
									System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
									stuck = false;
								}
								else stuck = true;
							}
							//the TruckAgent is somewhere where it can only move right.
							else if(!isLeftRoad && isRightRoad && !isUpRoad && !isDownRoad){
								if(!this.truck.verifyMovement(right, stuck)){
									this.truck.getPathWalked().add(new Point(currentX, currentY));
									this.truck.setCurrentPosition(right);
									System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
									stuck = false;
								}
								else stuck = true;
							}
							
							else {
								stuck = true;
								System.out.println(getAID().getLocalName() + " : Stayed in (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
							}
							
							if(this.truck.checkForGC()){
								this.state = 2;
								break;
							}
							
							ACLMessage printMsg = new ACLMessage(ACLMessage.INFORM);
							printMsg.setOntology("Print");
							printMsg.setContent(this.truck.getId() + "," +
												this.truck.getCurrentPosition().getX() + "," +
												this.truck.getCurrentPosition().getY() + "," +
												this.truck.getGarbageType() + "," +
												this.truck.getCurrentOccupation() + "," +
												this.truck.getMaxCapacity());
							
							AMSAgentDescription [] agents = null;
					        try {
					            SearchConstraints c = new SearchConstraints();
					            c.setMaxResults ( new Long(-1) );
					            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
					        }
					        catch (Exception e) { e.printStackTrace();}
							
					        String t = "printer";
					        for (int i=0; i<agents.length;i++) {
					            AID agentID = agents[i].getName();
					            if(agentID.getLocalName().equals(t)) {
					            	printMsg.addReceiver(agentID);
					            }
					        }
					        
					        send(printMsg);
					        System.out.println(getAID().getLocalName() + " : Sent information to printerAgent.");
							
						}
					
						if(this.totalGarbageCollected == this.totalGarbageToCollect){
							this.state = 4;
						}
						
						if(this.counter >= this.maxIterations){
							this.state = 4;
						}
						
						break;
						
				
				/* Sends a message saying it found a GarbageContainer of this
				 * TruckAgent's garbageType.
				 */
				case 2:
					ACLMessage message = new ACLMessage(ACLMessage.INFORM);
					message.setOntology("FoundGC");
					
					AMSAgentDescription [] agentsz = null;
			        try {
			            SearchConstraints c = new SearchConstraints();
			            c.setMaxResults ( new Long(-1) );
			            agentsz = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
			        }
			        catch (Exception e) { e.printStackTrace();}
					
			        for (int i=0; i<agentsz.length;i++) {
			            AID agentID = agentsz[i].getName();
			            if(agentID.getLocalName().charAt(0) == 't' || agentID.getLocalName().equals("E")) {
			            	message.addReceiver(agentID);
			            }
			            else continue;
			        }
			        
			        GarbageContainer gc = this.truck.getClosestGC();
			        message.setContent(gc.getPosition().getX() + "," + gc.getPosition().getY() + "," +
			        				   gc.getCurrentOccupation() + "," + gc.getMaxCapacity() + "," + gc.getType());
			        
			        send(message);
			        
			        System.out.println(getAID().getLocalName() + " : Sent GC information to all trucks.");
					
			        this.state = 1;
			        
					break;
					
				/*
				 * Receive message from another Truck saying they found a 
				 * GarbageContainer of this TruckAgent's garbageType.
				 */
				case 3:
					MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					MessageTemplate m2 = MessageTemplate.MatchOntology("FoundGC");
					
					MessageTemplate m1_and_m2 = MessageTemplate.and(m1, m2);
					
					ACLMessage msg = receive(m1_and_m2);
					
					if(msg != null){
						String content = msg.getContent();
						String[] parts = content.split(",");
						
						/*
						 * gCont.getPosition().getX() 
						 * + ", " + gCont.getPosition().getY() 
						 * + "," + gCont.getCurrentOccupation() 
						 * + "," + gCont.getType())
						 */
						
						Point position = new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
						double cOccupation = Double.parseDouble(parts[2]);
						double maxCapacity = Double.parseDouble(parts[3]);
						String type = parts[4];
						int gcId = this.truck.getGarbageContainersToGoTo().size() + 1;
						
						if(type.equals(this.truck.getGarbageType())){
							System.out.print(getAID().getLocalName() + " : Received msg from " + msg.getSender().getLocalName() + " and its a GC of my type.");
							
							GarbageContainer gCont = new GarbageContainer(gcId, type, maxCapacity, cOccupation, position);
							this.truck.getGarbageContainersToGoTo().add(gCont);
							
							List<Truck> trucksOfThisType = new ArrayList<Truck>(this.truck.getCompleteCityMap().selectTruckByGarbageType(type));
						
							addBehaviour(new negotiateWhoGoesToGoal(myAgent, this.truck, gCont, trucksOfThisType));
							
							this.collectGarbage(this.truck.getCurrentPosition().getX(), 
												this.truck.getCurrentPosition().getX() - 1, 
												this.truck.getCurrentPosition().getX() + 1, 
												this.truck.getCurrentPosition().getY(), 
												this.truck.getCurrentPosition().getY() - 1, 
												this.truck.getCurrentPosition().getY() + 1);
						}
						else {
							System.out.print(getAID().getLocalName() + " : Received msg from " + msg.getSender().getLocalName() + " and its not a GC of my type.");
						}
						
						this.state = 1;
					}
					else block();
					break;
				
				case 4:
					System.out.println(getAID().getLocalName() + " : I'm finished and I'm leaving.");
					this.finished = true;
					this.state = 6;
					break;
				
				default:
					break;
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
	class negotiateWhoGoesToGoal extends SimpleBehaviour{

		private static final long serialVersionUID = -7879108505609170302L;
		
		private boolean finished = false;
		private List<Truck> trucksOfThisType;
		private List<Double> maxCapacities;
		private Truck truck;
		private int maxCapsReceived;
		private boolean amIGoing;
		private GarbageContainer gc;
		private CityMap fullMap;
		
		private int state;
		
		public List<Truck> getTrucksOfThisType() {
			return trucksOfThisType;
		}

		public void setTrucksOfThisType(List<Truck> trucksOfThisType) {
			this.trucksOfThisType = trucksOfThisType;
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

		public List<Double> getMaxCapacities() {
			return maxCapacities;
		}

		public void setMaxCapacities(List<Double> maxCapacities) {
			this.maxCapacities = maxCapacities;
		}

		public int getMaxCapsReceived() {
			return maxCapsReceived;
		}

		public void setMaxCapsReceived(int maxCapsReceived) {
			this.maxCapsReceived = maxCapsReceived;
		}

		public boolean isAmIGoing() {
			return amIGoing;
		}

		public void setAmIGoing(boolean amIGoing) {
			this.amIGoing = amIGoing;
		}

		public negotiateWhoGoesToGoal(Agent a, Truck t, GarbageContainer g, List<Truck> trucks){
			super(a);
			this.setTrucksOfThisType(new ArrayList<Truck>(trucks));
			this.truck = t;
			this.gc = g;
			this.state = 1;
			this.maxCapacities = new ArrayList<Double>();
			this.maxCapsReceived = 0;
			this.amIGoing = false;
			this.fullMap = this.truck.getCompleteCityMap();
		}
		

		@Override
		public void action() {
			switch(this.state){
				
				//sends message to all trucks in the list with the maxCapacity.
				case 1:
					ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
					msg.setOntology("WhoIsGoingNegotiation");
					
					Iterator<Truck> itTruck = this.trucksOfThisType.iterator();
					double cap = this.truck.getMaxCapacity() - this.truck.getCurrentOccupation();
					while(itTruck.hasNext()){
						Truck t = itTruck.next();
						if(!t.getTruckName().equals(this.truck)){
							AMSAgentDescription [] agents = null;
					        try {
					            SearchConstraints c = new SearchConstraints();
					            c.setMaxResults ( new Long(-1) );
					            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
					        }
					        catch (Exception e) { e.printStackTrace();}
							
					        String tName = t.getTruckName();
					        for (int i=0; i<agents.length;i++) {
					            AID agentID = agents[i].getName();
					            if(agentID.getLocalName().equals(tName) && !tName.equals(this.truck.getTruckName())) {
					            	msg.addReceiver(agentID);
					            	System.out.println(getAID().getLocalName() + " : " + agentID.getLocalName() + ", I have " + cap + " of capacity.");
					            }
					            else continue;
					        }
					        
					        msg.setContent(Double.toString(cap));
					        
					        send(msg);
						}
						
						this.state = 7;
					}
					
					break;
					
				/*
				 * Sends a message to PrinterAgent containing the updated information
				 * of the Truck.
				 */
				case 2:
					ACLMessage printMsg = new ACLMessage(ACLMessage.INFORM);
					printMsg.setOntology("Print");
					printMsg.setContent(this.truck.getId() + "," +
										this.truck.getCurrentPosition().getX() + "," +
										this.truck.getCurrentPosition().getY() + "," +
										this.truck.getGarbageType() + "," +
										this.truck.getCurrentOccupation() + "," +
										this.truck.getMaxCapacity());
					
					AMSAgentDescription [] agents = null;
			        try {
			            SearchConstraints c = new SearchConstraints();
			            c.setMaxResults ( new Long(-1) );
			            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
			        }
			        catch (Exception e) { e.printStackTrace();}
					
			        String t = "printer";
			        for (int i=0; i<agents.length;i++) {
			            AID agentID = agents[i].getName();
			            if(agentID.getLocalName().equals(t)) {
			            	printMsg.addReceiver(agentID);
			            }
			        }
			        
			        send(printMsg);
			        System.out.println(getAID().getLocalName() + " : Sent information to printerAgent.");
			        this.state = 6;
			        
					break;
				
				//receives message to all trucks in the list with the maxCapacities.
				case 3:
					MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
					MessageTemplate m2 = MessageTemplate.MatchOntology("WhoIsGoingNegotiation");
					
					MessageTemplate m1_and_m2 = MessageTemplate.and(m1, m2);
					
					ACLMessage msg2 = receive(m1_and_m2);
					
					if(msg2 != null){
						String content = msg2.getContent();
						double maxCap = Double.parseDouble(content);
						this.maxCapacities.add(maxCap);
						this.maxCapsReceived++;
					}
					else {
						block();
					}
					
					if(this.maxCapsReceived == this.trucksOfThisType.size() - 1){
						this.state = 4;
					}
					break;
					
				//makes and sends decision.
				case 4:
					//calculates max.
					Iterator<Double> itMaxCaps = this.maxCapacities.iterator();
					double max = 0;
					while(itMaxCaps.hasNext()){
						double thisMax = itMaxCaps.next();
						if(thisMax > max){
							max = thisMax;
						}
					}
					
					//retrieves the Truck that corresponds to max.
					Truck toGo = null;
					Iterator<Truck> itTruck2 = this.trucksOfThisType.iterator();
					while(itTruck2.hasNext()){
						Truck truck = itTruck2.next();
						double currCap = truck.getMaxCapacity() - truck.getCurrentOccupation();
						if(currCap == max){
							toGo = truck;
						}
					}
					
					if(toGo != null){
						ACLMessage msg3 = new ACLMessage(ACLMessage.INFORM);
						msg3.setOntology("YouAreGoing");
						
						AMSAgentDescription [] agentsz = null;
				        try {
				            SearchConstraints c = new SearchConstraints();
				            c.setMaxResults ( new Long(-1) );
				            agentsz = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
				        }
				        catch (Exception e) { e.printStackTrace();}
						
				        String tName = toGo.getTruckName();
				        for (int i=0; i<agentsz.length;i++) {
				            AID agentID = agentsz[i].getName();
				            if(agentID.getLocalName().equals(tName)) {
				            	msg3.addReceiver(agentID);
				            	System.out.println(getAID().getLocalName() + " : " + agentID.getLocalName() + ", you are going.");
				            }
				            else continue;
				        }
				        
					}
					
					break;
				
				//receives decision.
				case 5:
					MessageTemplate m11 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					MessageTemplate m21 = MessageTemplate.MatchOntology("YouAreGoing");
					
					MessageTemplate m11_and_m21 = MessageTemplate.and(m11, m21);
					
					ACLMessage msg4 = receive(m11_and_m21);
					
					if(msg4 != null) {
						this.amIGoing = true;
						this.state = 6;
					}
					else {
						block();
					}
					
					break;
				//goes to GC.
				case 6:
					if(this.amIGoing){
						GarbageContainer gc = this.gc;
						boolean achieved = false;
						
						while(!achieved){
							int gcX = gc.getPosition().getX();
							int gcY = gc.getPosition().getY();
							int currentX = this.truck.getCurrentPosition().getX();
							int currentY = this.truck.getCurrentPosition().getY();
							int nextX = currentX + 1;
							int nextY = currentY + 1;
							int previousX = currentX - 1;
							int previousY = currentY - 1;
							
							if( (gcX == nextX && gcY == currentY) || (gcX == currentX && gcY == nextY) ||
								(gcX == previousX && gcY == currentY) || (gcX == currentX && gcY == previousY) ) {
								System.out.println(getAID().getLocalName() + " : Reached (" + gcX + ", " + gcY + ").");
								achieved = true;
								break;
							}
							
							/*
							 * Checks where to move.
							 */
							Point up = new Point(currentX, previousY);
							Point down = new Point(currentX, nextY);
							Point left = new Point(previousX, currentY);
							Point right = new Point(nextX, currentY);
							
							boolean isUpRoad = false;
							boolean isDownRoad = false;
							boolean isLeftRoad = false;
							boolean isRightRoad = false;
							
							if(this.fullMap.checkIfPointIsRoadOrCrossroads(up)) isUpRoad = true;
							if(this.fullMap.checkIfPointIsRoadOrCrossroads(down)) isDownRoad = true;
							if(this.fullMap.checkIfPointIsRoadOrCrossroads(left)) isLeftRoad = true;
							if(this.fullMap.checkIfPointIsRoadOrCrossroads(right)) isRightRoad = true;
							
							//the TruckAgent is on a CROSSROADS.
							if(isUpRoad && isDownRoad && isLeftRoad && isRightRoad){
								Random r = new Random();
								int val = r.nextInt(4-1) + 1;
								switch(val) {
									//move to the left of the map.
									case 1:
										if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), left)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(left);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
										}
										else val = 2;
										break;
									
									//move up on the map.
									case 2:
										if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), up)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(up);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
										}
										else val = 3;
										break;
									
									//move to the right of the map.
									case 3:
										if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), right)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(right);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");

										}
										else val = 4;
										break;
									
									//move down on the map.
									case 4:
										if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), down)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(down);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");	
										}
										else val = 1;
										break;
									default:
										break;
								}
							}
							
							//the TruckAgent is somewhere where it can move up or down.
							else if(isUpRoad && isDownRoad){
								Random r = new Random();
								int val = r.nextInt(2-1) + 1;
								switch(val){
									case 1:
										if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), up)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(up);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");	
										}
																				
										else val = 2;
										break;
									case 2:
										if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), down)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(down);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");

										}
										else val = 1;
										break;
									default:
										break;
								}
							}
							//the TruckAgent is somewhere where it can move left or right.
							else if(isLeftRoad && isRightRoad){
								Random r = new Random();
								int val = r.nextInt(2-1) + 1;
								switch(val){
									case 1:
										if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), left)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(left);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");

										}
										else val = 2;
										break;
									case 2:
										if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), right)){
											this.truck.getPathWalked().add(new Point(currentX, currentY));
											this.truck.setCurrentPosition(right);
											System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");

										}
										else val = 1;
										break;
									default:
										break;
								}
							}
							//the TruckAgent is somewhere where it can only move up.
							else if(isUpRoad && !isDownRoad){
								if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), up)){
									this.truck.getPathWalked().add(new Point(currentX, currentY));
									this.truck.setCurrentPosition(up);
									System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
								}
							}
							//the TruckAgent is somewhere where it can only move down.
							else if(!isUpRoad && isDownRoad) {
								if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), down)){
									this.truck.getPathWalked().add(new Point(currentX, currentY));
									this.truck.setCurrentPosition(down);
									System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
								}
							}
							//the TruckAgent is somewhere where it can only move left.
							else if(isLeftRoad && !isRightRoad){
								if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), left)){
									this.truck.getPathWalked().add(new Point(currentX, currentY));
									this.truck.setCurrentPosition(left);
									System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
								}
							}
							//the TruckAgent is somewhere where it can only move right.
							else if(!isLeftRoad && isRightRoad){
								if(!this.truck.isItCloser(gc, this.truck.getCurrentPosition(), right)){
									this.truck.getPathWalked().add(new Point(currentX, currentY));
									this.truck.setCurrentPosition(right);
									System.out.println(getAID().getLocalName() + " : Moved to (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
								}
							}
							else {
								System.out.println(getAID().getLocalName() + " : Stayed in (" + this.truck.getCurrentPosition().getX() + ", " + this.truck.getCurrentPosition().getY() + ").");
							}
							this.state = 2;
							break;
						}
					}
					break;
				
				default:
					this.finished = true;
					break;
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
	class garbageCollectedBehaviour extends SimpleBehaviour {

		private static final long serialVersionUID = -1973162646840707921L;
		private boolean finished = true;
		private Truck truck;
		private double gCollected;
		private int state;
		
		public Truck getTruck() {
			return truck;
		}

		public void setTruck(Truck truck) {
			this.truck = truck;
		}

		public garbageCollectedBehaviour(Agent a, Truck truck, double garbageCollected){
			super(a);
			this.setTruck(truck);
			this.gCollected = garbageCollected;
		}
		
		@Override
		public void action() {
			switch(this.state){
				//send.
				case 1:
					ACLMessage message = new ACLMessage(ACLMessage.INFORM);
					message.setOntology("GarbageCollected");
					
					AMSAgentDescription [] agents = null;
			        try {
			            SearchConstraints c = new SearchConstraints();
			            c.setMaxResults ( new Long(-1) );
			            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
			        }
			        catch (Exception e) { e.printStackTrace();}
					
			        for (int i=0; i<agents.length;i++) {
			            AID agentID = agents[i].getName();
			            if(agentID.getLocalName().equals("E") || agentID.getLocalName().charAt(0) == 't') {
			            	message.addReceiver(agentID);
			            	System.out.println(getAID().getLocalName() + " : Send GarbageCollected message to " + agentID.getLocalName() + ".");
			            }
			            else continue;
			        }
			        
			        message.setContent(Double.toString(this.gCollected));
			        
			        send(message);
			        
			        this.state = 2;
			        
					break;
					
				//receive.
				case 2:
					MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					MessageTemplate m2 = MessageTemplate.MatchOntology("GarbageCollected");
					
					MessageTemplate m1_and_m2 = MessageTemplate.and(m1, m2);
					
					ACLMessage msg = receive(m1_and_m2);
					if(msg!=null){
						String content = msg.getContent();
						double update = Double.parseDouble(content);
						Object[] args = new Object[1];
						args[0] = update;
						this.getAgent().setArguments(args);
					}
					else{
						block();
					}
					this.state = 3;
					break;
				
				//finish.
				default:
					this.finished = true;
					break;
			}
		}

		@Override
		public boolean done() {
		
			return finished;
		}
		
	}
}


