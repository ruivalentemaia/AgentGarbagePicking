package agent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

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
		private Plan plan;
		
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
				Iterator<Point> pathToBeWalked = this.truck.getPathToBeWalked().iterator();
				
				while(pathToBeWalked.hasNext()){
					Point firstPoint = pathToBeWalked.next();
					pathToBeWalked.remove();
					
					this.truck.setCurrentPosition(firstPoint);
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
					
					System.out.println(getAID().getLocalName() + ": Moved to (" + firstPoint.getX() + ", " + firstPoint.getY() + ").");
					if(this.state == 3) break;
				}
				
				//stop condition.
				if(this.truck.getPathToBeWalked().isEmpty()){
					System.out.println(getAID().getLocalName() + ": Finished working and I'm exiting.");
					this.state = 4;
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
				this.state = 2;
				break;
			default:
				finished = true;
				break;
			}
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
		
		
		public noMapBehaviour(Truck t) {
			this.truck = t;
			this.state = 1;
		}

		@Override
		public void action() {
			switch(this.state){
				/*
				 * TruckAgent is moving.
				 */
				case 1:
					HashMap<GarbageContainer, Double> garbageCollected = new HashMap<GarbageContainer, Double>();
					CityMap unknownMap = new CityMap();
					CityMap fullMap = this.truck.getCompleteCityMap();
					
					this.truck.setCompleteCityMap(unknownMap);
					this.truck.getCompleteCityMap().getPoints().add(this.truck.getCurrentPosition());
					
					//count how many GCs the TruckAgent is supposed to get to.
					Iterator<GarbageContainer> itGC = fullMap.getGarbageContainers().iterator();
					int counter = 0;
					while(itGC.hasNext()){
						GarbageContainer gc = itGC.next();
						if(gc.getType().equals(this.truck.getGarbageType()))
							counter++;
					}
					
					/*
					 * stop condition: the number of GCs hit is equal to the number 
					 * of GCs that the TruckAgent is supposed to hit.
					 */
					while(garbageCollected.size() < counter){
						int currentX = this.truck.getCurrentPosition().getX();
						int currentY = this.truck.getCurrentPosition().getY();
						
						int previousX = currentX - 1;
						int nextX = currentX + 1;
						int previousY = currentY - 1;
						int nextY = currentY + 1;
						
						//checks if there is some GarbageContainer around this position.
						Iterator<GarbageContainer> itGContainer = fullMap.getGarbageContainers().iterator();
						while(itGContainer.hasNext()){
							
							GarbageContainer gCont = itGContainer.next();
							int gContX = gCont.getPosition().getX();
							int gContY = gCont.getPosition().getY();
							
							if( ( (gContX == currentX) || (gContX == previousX) || (gContX == nextX) ) &&
								( (gContY == currentY) || (gContY == previousY) || (gContY == nextY)) ) {
								
								if(gCont.getType().equals(this.truck.getGarbageType())) {
							
									this.truck.getCompleteCityMap().getGarbageContainers().add(gCont);
									double currentOccupation = gCont.getCurrentOccupation();
									if(currentOccupation <= (this.truck.getMaxCapacity() - this.truck.getCurrentOccupation())){
										this.truck.setCurrentOccupation(this.truck.getCurrentOccupation() + currentOccupation);
										gCont.setCurrentOccupation(0);
										garbageCollected.put(gCont, currentOccupation);
									}
									else {
										double valueToTake = this.truck.getMaxCapacity() - this.truck.getCurrentOccupation();
										this.truck.setCurrentOccupation(this.truck.getCurrentOccupation() + valueToTake);
										gCont.setCurrentOccupation(gCont.getCurrentOccupation() - valueToTake);
										garbageCollected.put(gCont, valueToTake);
									}
								}
								
								/*
								 * sends message to all Trucks of this GarbageType saying
								 * it found one.
								 */
								else {
									//TODO
								}
							}
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
						
						if(fullMap.checkIfPointIsRoadOrCrossroads(up)) isUpRoad = true;
						if(fullMap.checkIfPointIsRoadOrCrossroads(down)) isDownRoad = true;
						if(fullMap.checkIfPointIsRoadOrCrossroads(left)) isLeftRoad = true;
						if(fullMap.checkIfPointIsRoadOrCrossroads(right)) isRightRoad = true;
						
						//the TruckAgent is on a CROSSROADS.
						if(isUpRoad && isDownRoad && isLeftRoad && isRightRoad){
							Random r = new Random();
							int val = r.nextInt(4-1) + 1;
							switch(val) {
								//move to the left of the map.
								case 1:
									if(!this.truck.hasPointBeenWalked(left)){
										this.truck.getPathWalked().add(new Point(currentX, currentY));
										this.truck.setCurrentPosition(left);
									}
									else val = 2;
									break;
								
								//move up on the map.
								case 2:
									if(!this.truck.hasPointBeenWalked(up)){
										this.truck.getPathWalked().add(new Point(currentX, currentY));
										this.truck.setCurrentPosition(up);
									}
									else val = 3;
									break;
								
								//move to the right of the map.
								case 3:
									if(!this.truck.hasPointBeenWalked(right)){
										this.truck.getPathWalked().add(new Point(currentX, currentY));
										this.truck.setCurrentPosition(right);
									}
									else val = 4;
									break;
								
								//move down on the map.
								case 4:
									if(!this.truck.hasPointBeenWalked(down)){
										this.truck.getPathWalked().add(new Point(currentX, currentY));
										this.truck.setCurrentPosition(down);
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
									if(!this.truck.hasPointBeenWalked(up)){
										this.truck.getPathWalked().add(new Point(currentX, currentY));
										this.truck.setCurrentPosition(up);
									}
									else val = 2;
									break;
								case 2:
									if(!this.truck.hasPointBeenWalked(down)){
										this.truck.getPathWalked().add(new Point(currentX, currentY));
										this.truck.setCurrentPosition(down);
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
									if(!this.truck.hasPointBeenWalked(left)){
										this.truck.getPathWalked().add(new Point(currentX, currentY));
										this.truck.setCurrentPosition(left);
									}
									else val = 2;
									break;
								case 2:
									if(!this.truck.hasPointBeenWalked(right)){
										this.truck.getPathWalked().add(new Point(currentX, currentY));
										this.truck.setCurrentPosition(right);
									}
									else val = 1;
									break;
								default:
									break;
							}
						}
						//the TruckAgent is somewhere where it can only move up.
						else if(isUpRoad && !isDownRoad){
							if(!this.truck.hasPointBeenWalked(up)){
								this.truck.getPathWalked().add(new Point(currentX, currentY));
								this.truck.setCurrentPosition(up);
							}
						}
						//the TruckAgent is somewhere where it can only move down.
						else if(!isUpRoad && isDownRoad) {
							if(!this.truck.hasPointBeenWalked(down)){
								this.truck.getPathWalked().add(new Point(currentX, currentY));
								this.truck.setCurrentPosition(down);
							}
						}
						//the TruckAgent is somewhere where it can only move left.
						else if(isLeftRoad && !isRightRoad){
							if(!this.truck.hasPointBeenWalked(left)){
								this.truck.getPathWalked().add(new Point(currentX, currentY));
								this.truck.setCurrentPosition(left);
							}
						}
						//the TruckAgent is somewhere where it can only move right.
						else if(!isLeftRoad && isRightRoad){
							if(!this.truck.hasPointBeenWalked(right)){
								this.truck.getPathWalked().add(new Point(currentX, currentY));
								this.truck.setCurrentPosition(right);
							}
						}
						
						
					}
					break;
				
				/*
				 * Receive message from another Truck saying they found a 
				 * GarbageContainer of this TruckAgent's garbageType.
				 */
				case 2:
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
}


