package units;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ai.GreedyPathSearch;
import ai.Goal;
import ai.Options;
import ai.Path;
import map.CityMap;
import map.Crossroads;
import map.GarbageContainer;
import map.Point;
import map.Road;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Truck {
	
	private int id;
	private String truckName;
	private String garbageType;
	private Point startPosition;
	private Point currentPosition;
	private double currentOccupation;
	private double maxCapacity;
	private List<Point> pathWalked;
	private List<Point> pathToBeWalked;
	private List<Goal> goals;
	private List<GarbageContainer> garbageContainersToGoTo;
	
	//complete list of the CityMap points
	private CityMap completeCityMap;
	
	private Options options;
	
	private String tempFilePath = System.getProperty("user.dir") + "/temp";
	
	//Comparators for arrays.
	static private Comparator<Goal> orderId;
	
	static {
		orderId = new Comparator<Goal>() {
			@Override
			public int compare(Goal o1, Goal o2) {
				if(o1.getId() == o2.getId())
					return 0;
				else if(o1.getId() > o2.getId()){
					return 1;
				}
				else return -1;
			}
			
		};
	}
	
	
	/**
	 *
	 * 
	 * 
	 * 
	 * 	GETS AND SETS FOR ALL THE ATTRIBUTES.
	 * 
	 * 
	 * 
	 * 
	 */
	
	public int getId() {
		return id;
	}
	
	
	public void setId(int id) {
		this.id = id;
	}


	public String getTruckName() {
		return truckName;
	}


	public void setTruckName(String truckName) {
		this.truckName = truckName;
	}


	public String getGarbageType() {
		return garbageType;
	}


	public void setGarbageType(String garbageType) {
		this.garbageType = garbageType;
	}


	public Point getStartPosition() {
		return startPosition;
	}


	public void setStartPosition(Point startPosition) {
		this.startPosition = startPosition;
	}


	public double getCurrentOccupation() {
		return currentOccupation;
	}


	public void setCurrentOccupation(double currentOccupation) {
		this.currentOccupation = currentOccupation;
	}


	public double getMaxCapacity() {
		return maxCapacity;
	}


	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}


	public Point getCurrentPosition() {
		return currentPosition;
	}


	public void setCurrentPosition(Point currentPosition) {
		this.currentPosition = currentPosition;
	}


	public List<Point> getPathWalked() {
		return pathWalked;
	}


	public void setPathWalked(List<Point> pathWalked) {
		this.pathWalked = pathWalked;
	}


	public List<Point> getPathToBeWalked() {
		return pathToBeWalked;
	}


	public void setPathToBeWalked(List<Point> pathToBeWalked) {
		this.pathToBeWalked = pathToBeWalked;
	}
	
	
	public List<Goal> getGoals() {
		return goals;
	}


	public void setGoals(List<Goal> goals) {
		this.goals = goals;
	}


	public List<GarbageContainer> getGarbageContainersToGoTo() {
		return garbageContainersToGoTo;
	}


	public void setGarbageContainersToGoTo(List<GarbageContainer> garbageContainersToGoTo) {
		this.garbageContainersToGoTo = garbageContainersToGoTo;
	}


	public CityMap getCompleteCityMap() {
		return completeCityMap;
	}


	public void setCompleteCityMap(CityMap completeCityMap) {
		this.completeCityMap = completeCityMap;
	}
	
	public String getTempFilePath() {
		return tempFilePath;
	}


	public void setTempFilePath(String tempFilePath) {
		this.tempFilePath = tempFilePath;
	}
	
	public Options getOptions() {
		return this.options;
	}
	
	public void setOptions(Options options){
		this.options = options;
	}


	/*
	 * Orders by Id an array of Goals.
	 */
	public void orderGoalById(Goal[] g){
		Arrays.sort(g, orderId);
	}
	
	
	/*
	 * Gets the maximum X value in the list of map points.
	 */
	public int getMaxX() {
		int max = 0;
		Iterator<Point> itMap = this.completeCityMap.getPoints().iterator();
		while(itMap.hasNext()){
			Point p = itMap.next();
			if(p.getX() > max){
				max = p.getX();
			}
		}
		return max;
	}
	
	/*
	 * Gets the maximum Y value in the list of map points.
	 */
	public int getMaxY() {
		int max = 0;
		Iterator<Point> itMap = this.completeCityMap.getPoints().iterator();
		while(itMap.hasNext()){
			Point p = itMap.next();
			if(p.getY() > max){
				max = p.getY();
			}
		}
		return max;
	}
	
	
	/*
	 * Checks if this Truck can start in the position that is to be
	 * assigned as startingPosition.
	 */
	private boolean canStartInThisPosition(Point p) {
		boolean itCan = true;
		Iterator<Truck> itTruck = this.completeCityMap.getTrucks().iterator();
		while(itTruck.hasNext()){
			Truck t = itTruck.next();
			if( (t.getStartPosition().getX() == p.getX()) &&
				(t.getStartPosition().getY() == p.getY())) {
				itCan = false;
				break;
			}
		}
		return itCan;
	}
	
	
	/*
	 * Selects one starting point (the closest to the coordinate (0,0) of
	 * the completeCityMap attribute.
	 */
	public Point selectStartingPoint() {
		Iterator<Point> pointIt = this.completeCityMap.getPoints().iterator();
		double minDistance = 10000000;
		double currentDistance = 0;
		Point selectedPoint = new Point(0,0);
		
		if(this.completeCityMap.getOptions().isAllTrucksStartingSamePosition()){
			while(pointIt.hasNext()) {
				Point p = pointIt.next();
				currentDistance = Math.sqrt(Math.pow(p.getX() - 0, 2) + Math.pow(p.getY() - 0, 2));
				if( (currentDistance < minDistance) && (p.getType().equals("ROAD"))){
					minDistance = currentDistance;
					selectedPoint = p;
				}
			}
		}
		
		else {
			while(pointIt.hasNext()) {
				Point p = pointIt.next();
				if(this.canStartInThisPosition(p)){
					currentDistance = Math.sqrt(Math.pow(p.getX() - 0, 2) + Math.pow(p.getY() - 0, 2));
					if( (currentDistance < minDistance) && (p.getType().equals("ROAD"))){
						minDistance = currentDistance;
						selectedPoint = p;
					}
				}
			}
		}
		
		return selectedPoint;
	}
	
	
	/*
	 * Checks if this Truck has one Goal passed as parameter.
	 */
	public boolean hasGoal(Goal g){
		boolean hasGoal = false;
		Iterator<Goal> itGoals = this.goals.iterator();
		while(itGoals.hasNext()){
			Goal goal = itGoals.next();
			if(g.checkGoalEquality(g, goal)){
				hasGoal = true;
			}
		}
		return hasGoal;
	}
	
	
	/*
	 * Remove repeated Goals from the goals list.
	 */
	public void removeRepeatedGoals() {
		SortedSet<Goal> goals = new TreeSet<Goal>(new Comparator<Goal>() {
		    @Override
		    public int compare(Goal g1, Goal g2) {
		        if ( (g1.getStartPoint().getX() == g2.getStartPoint().getX()) &&
		        		 (g1.getStartPoint().getY() == g2.getStartPoint().getY()) &&
		        		 (g1.getEndPoint().getX() == g2.getEndPoint().getX()) &&
		        		 (g1.getEndPoint().getY() == g2.getEndPoint().getY())) {
		        	return 0;
		        }
		        return 1;
		    }
		});
		goals.addAll(this.goals);
		
		this.goals.clear();
		this.goals.addAll(goals);
	}
	
	
	/*
	 * Checks if a given position is the one closest to a given goal.
	 */
	public boolean positionClosestToGoal(Point p, Goal g){
		if( ((p.getX() == g.getEndPoint().getX()) || (p.getX() == g.getEndPoint().getX() - 1) || (p.getX() == g.getEndPoint().getX() + 1)) &&
			((p.getY() == g.getEndPoint().getY() || (p.getY() == g.getEndPoint().getY() - 1) || (p.getY() == g.getEndPoint().getY() + 1))) ) {
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 	TRUCK MOVEMENTS METHODS.
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	/*
	 * Collects Garbage from one GarbageContainer
	 */
	public void collectGarbage(GarbageContainer gc, double quantity){
		Iterator<GarbageContainer> itGC = this.garbageContainersToGoTo.iterator();
		while(itGC.hasNext()){
			GarbageContainer gContainer = itGC.next();
			if(gc.getPosition().getX() == gContainer.getPosition().getX() &&
			   gc.getPosition().getY() == gContainer.getPosition().getY()){
				gc.setCurrentOccupation(gc.getCurrentOccupation() - quantity);
				this.setCurrentOccupation(this.getCurrentOccupation() + quantity);
				gContainer.setCurrentOccupation(gContainer.getCurrentOccupation() - quantity);
			}
		}
	}
	
	/*
	 * This truck moves one position to the left.
	 * Returns 1 if the movement was completed and -1 if it failed.
	 */
	public int moveLeft(){
		int success = -1;
		int index = -1;
		
		//if it's 0, the Truck can't move left.
		if( (this.currentPosition.getX() > 0)) {
			Point futurePos = new Point(this.currentPosition.getX() - 1, this.currentPosition.getY());
			
			//test if it's a Crossroads.
			futurePos.setType("CROSSROADS");
			index = this.completeCityMap.getPoints().indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
			}
			
			/*
			 * tests if it's a Road and tests for the directions of it
			 * to validate the movement.
			 */
			Road prev = this.completeCityMap.selectRoadFromPoint(this.currentPosition);
			Road next = this.completeCityMap.selectRoadFromPoint(futurePos);
			try {
				if( (prev.getDirection().equals(next.getDirection())) || (next.getDirection().equals("both"))){
					futurePos.setType("ROAD");
					index = this.completeCityMap.getPoints().indexOf(futurePos);
					if(index != -1){
						this.currentPosition = futurePos;
						this.pathWalked.add(this.currentPosition);
						success = 1;
						return success;
					}
				}
			} catch(NullPointerException e){
				e.printStackTrace();
			}
			
		}
		
		return success;
	}
	
	
	/*
	 * This Truck moves one position to the right.
	 * Returns 1 if the movement was completed and -1 if the movement failed.
	 */
	public int moveRight(){
		int success = -1;
		int index = -1;
		
		//if it's 0, the Truck can't move left.
		if( (this.currentPosition.getX() < this.getMaxX())) {
			Point futurePos = new Point(this.currentPosition.getX() + 1, this.currentPosition.getY());
			
			//test if it's a Crossroads.
			futurePos.setType("CROSSROADS");
			index = this.completeCityMap.getPoints().indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
			}
			
			/*
			 * tests if it's a Road and tests for the directions of it
			 * to validate the movement.
			 */
			Road prev = this.completeCityMap.selectRoadFromPoint(this.currentPosition);
			Road next = this.completeCityMap.selectRoadFromPoint(futurePos);
			try {
				if( (prev.getDirection().equals(next.getDirection())) || (next.getDirection().equals("both"))){
					futurePos.setType("ROAD");
					index = this.completeCityMap.getPoints().indexOf(futurePos);
					if(index != -1){
						this.currentPosition = futurePos;
						this.pathWalked.add(this.currentPosition);
						success = 1;
						return success;
					}
				}
			} catch(NullPointerException e){
				e.printStackTrace();
			}
			
		}
		
		return success;
	}
	
	
	/*
	 * This Truck moves one position Up in the map.
	 * Returns 1 if the movement was completed and -1 if it failed.
	 */
	public int moveUp() {
		int success = -1;
		int index = -1;
		
		//if it's 0, the Truck can't move left.
		if( (this.currentPosition.getY() > 0)) {
			Point futurePos = new Point(this.currentPosition.getX(), this.currentPosition.getY() - 1);
			
			//test if it's a Crossroads.
			futurePos.setType("CROSSROADS");
			index = this.completeCityMap.getPoints().indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
			}
			
			/*
			 * tests if it's a Road and tests for the directions of it
			 * to validate the movement.
			 */
			Road prev = this.completeCityMap.selectRoadFromPoint(this.currentPosition);
			Road next = this.completeCityMap.selectRoadFromPoint(futurePos);
			try {
				if( (prev.getDirection().equals(next.getDirection())) || (next.getDirection().equals("both"))){
					futurePos.setType("ROAD");
					index = this.completeCityMap.getPoints().indexOf(futurePos);
					if(index != -1){
						this.currentPosition = futurePos;
						this.pathWalked.add(this.currentPosition);
						success = 1;
						return success;
					}
				}
			} catch(NullPointerException e){
				e.printStackTrace();
			}
			
		}
		
		return success;
	}
	
	
	/*
	 * This Truck moves one position Up in the map.
	 * Returns 1 if the movement was completed and -1 if it failed.
	 */
	public int moveDown() {
		int success = -1;
		int index = -1;
		
		//if it's 0, the Truck can't move left.
		if( (this.currentPosition.getY() < this.getMaxY())) {
			Point futurePos = new Point(this.currentPosition.getX(), this.currentPosition.getY() + 1);
			
			//test if it's a Crossroads.
			futurePos.setType("CROSSROADS");
			index = this.completeCityMap.getPoints().indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
			}
			
			/*
			 * tests if it's a Road and tests for the directions of it
			 * to validate the movement.
			 */
			Road prev = this.completeCityMap.selectRoadFromPoint(this.currentPosition);
			Road next = this.completeCityMap.selectRoadFromPoint(futurePos);
			try {
				if( (prev.getDirection().equals(next.getDirection())) || (next.getDirection().equals("both"))){
					futurePos.setType("ROAD");
					index = this.completeCityMap.getPoints().indexOf(futurePos);
					if(index != -1){
						this.currentPosition = futurePos;
						this.pathWalked.add(this.currentPosition);
						success = 1;
						return success;
					}
				}
			} catch(NullPointerException e){
				e.printStackTrace();
			}
			
		}
		
		return success;
	}
	
	
	
	/**
	 * 
	 * 
	 * 
	 * 	PATH PLANNING.
	 * 
	 * 
	 * 
	 */
	
	
	/*
	 * Fills the list of Goals with new Goals where the type of the garbage
	 * in the GarbageContainer used is the same as the type of garbage that this
	 * Truck can transport.
	 */
	public void buildGoalsList() {
		Iterator<GarbageContainer> gcIt = this.completeCityMap.getGarbageContainers().iterator();
		int goalCounter = 1;
		while(gcIt.hasNext()){
			GarbageContainer gc = gcIt.next();
			if(gc.getType().equals(this.getGarbageType())){
				//going.
				Point gcPos = gc.getPosition();
				Road r = this.completeCityMap.selectRoadFromGarbageContainer(gcPos);
				Point finalPos = this.completeCityMap.selectPointFromRoad(r, gcPos);
				Goal g = new Goal(goalCounter, this.startPosition, finalPos);
				Path p = new Path(goalCounter);
				g.setBestPath(p);
				this.goals.add(g);
				goalCounter++;
				this.garbageContainersToGoTo.add(gc);
				
				//coming back to deliver garbage.
				if(gc.getCurrentOccupation() == this.getMaxCapacity()){
					Goal gBack = new Goal(goalCounter, finalPos, this.startPosition);
					Path path = new Path(goalCounter);
					gBack.setBestPath(path);
					this.goals.add(gBack);
					goalCounter++;
				}
				
				else if(gc.getCurrentOccupation() > this.getMaxCapacity()){
					Goal gBack = new Goal(goalCounter, finalPos, this.startPosition);
					Path path = new Path(goalCounter);
					gBack.setBestPath(path);
					this.goals.add(gBack);
					goalCounter++;
				}
				
				else if(gc.getCurrentOccupation() < this.getMaxCapacity()) {
					if(this.options.isActiveConsolePrinting())
						System.out.println("TRUCK can go to the next Goal !");
				}
			}
		}
		this.removeRepeatedGoals();
	}
	
	
	/*
	 * Checks if the current position is the same as the final position
	 * of a parameter passed Goal.
	 */
	private boolean currentPosEqualToFinalPos(Goal g){
		if( (this.currentPosition.getX() == g.getEndPoint().getX()) && (this.currentPosition.getY() == g.getEndPoint().getY())) {
			return true;
		}
		else return false;
	}
	
	
	/*
	 * Performs Greedy path search for one Goal g, passed as parameter
	 */
	public void doGreedyPathSearch(Goal g, int sizeOfCurrentPath, int whichTimeWasThisDone){
		if(whichTimeWasThisDone == 1)
			this.buildGoalsList();
		GreedyPathSearch greedy = new GreedyPathSearch(g);
		double currentH = g.euclideanDistance(g.getStartPoint(), g.getEndPoint());
		int iteration = 1;
		int nth = 0;
		List<Point> blacklist = new ArrayList<Point>();
		
		while( !this.currentPosEqualToFinalPos(g)) {
			currentH = g.euclideanDistance(this.currentPosition, g.getEndPoint());
			
			greedy.calculateFHeuristicForOpenList();
			Point bestNode = new Point(0,0);
			
			if(nth == 0)
				bestNode = greedy.minimumFHeuristic();
			else {
				bestNode = greedy.getMinimumValue(nth);
				nth = 0;
			}
			
			/*
			 * Checks if bestNode is in the blacklist.
			 */
			Iterator<Point> blacklistIt = blacklist.iterator();
			while(blacklistIt.hasNext()){
				Point black = blacklistIt.next();
				if( (black.getX() == bestNode.getX()) && (black.getY() == bestNode.getY())) {
					Random rN = new Random();
					nth = rN.nextInt(4);
					bestNode = greedy.getMinimumValue(nth);
					break;
				}
			}
			
			Point fivePositionsAgo = new Point(0,0);
			if(iteration > 5){
				fivePositionsAgo = this.pathToBeWalked.get(this.pathToBeWalked.size() - 5);
			}
			
			this.pathToBeWalked.add(this.currentPosition);
			
			if( (bestNode.getX() == greedy.getGoal().getEndPoint().getX()) &&
				(bestNode.getY() == greedy.getGoal().getEndPoint().getY()) ) {
				
				if(this.options.isActiveConsolePrinting())
					System.out.println("Path Planning complete for Goal " + g.getId() + " of Truck " + this.getTruckName() + " complete.");
				
				this.currentPosition = bestNode;
				this.pathToBeWalked.add(this.currentPosition);
				break;
			}
			else {
				this.currentPosition = bestNode;
				greedy.getClosedList().add(bestNode);
				
				List<Point> neighbours = new ArrayList<Point>();
				neighbours = this.completeCityMap.selectNeighbourPoints(bestNode);
				
				Iterator<Point> neighboursIt = neighbours.iterator();
				
				while(neighboursIt.hasNext()){
					Point neighbour = neighboursIt.next();
					
					if( !(greedy.checkPointInOpenList(neighbour)) || !(greedy.checkPointInClosedList(neighbour))) {
						greedy.getOpenList().add(neighbour);
					}
				}
			}
			
			//loop detector.
			if(fivePositionsAgo.equals(this.currentPosition)){
				
				//search for the last Crossroads crossed.
				Iterator<Point> itPathWalked = this.pathToBeWalked.iterator();
				Crossroads lastCrossroadCrossed = new Crossroads(1, new Point(0,0));
				int skip = 0;
				while(itPathWalked.hasNext()){
					Point p = itPathWalked.next();
					if(p.getType().equals("CROSSROADS")){
						lastCrossroadCrossed.setCenter(new Point(p.getX(), p.getY()));
						Iterator<Crossroads> itCross = this.completeCityMap.getCrossroads().iterator();
						while(itCross.hasNext()){
							Crossroads c = itCross.next();
							if( (c.getCenter().getX() == lastCrossroadCrossed.getCenter().getX()) &&
								(c.getCenter().getY() == lastCrossroadCrossed.getCenter().getY())){
								lastCrossroadCrossed = c;
							}
						}
					}
				}
				
				/*
				 * Counts how many times the lastCrossroadCrossed
				 * comes in the list.
				 */
				itPathWalked = this.pathToBeWalked.iterator();
				while(itPathWalked.hasNext()){
					Point p = itPathWalked.next();
					if( (p.getX() == lastCrossroadCrossed.getCenter().getX()) &&
							(p.getY() == lastCrossroadCrossed.getCenter().getY()) &&
							(p.getType().equals("CROSSROADS"))){
						skip++;
					}
				}
				
				/* Removes everything that was inserted after the 
				 * lastCrossroadCrossed in pathToBeWalked.
				 */
				itPathWalked = this.pathToBeWalked.iterator();
				int skipCopy = skip;
				skip = 0;
				boolean remove = false;
				while(itPathWalked.hasNext()){
					Point p = itPathWalked.next();
					
					if(remove) {
						itPathWalked.remove();
						blacklist.add(p);
					}
					
					if( (p.getX() == lastCrossroadCrossed.getCenter().getX()) &&
						(p.getY() == lastCrossroadCrossed.getCenter().getY()) &&
						(p.getType().equals("CROSSROADS"))){
						skip++;
						if(skip == skipCopy)
							remove = true;
					}
					
				}
				
				greedy.backtrack(lastCrossroadCrossed);
				this.currentPosition = new Point(lastCrossroadCrossed.getCenter().getX(),
												 lastCrossroadCrossed.getCenter().getY());
				this.currentPosition.setType("CROSSROADS");
				nth++;
			}
			iteration++;
		}
		
		int goalIndex = this.goals.indexOf(g);
		Path path = g.getBestPath();
		path.setPoints(this.pathToBeWalked);
		path.setLength(this.pathToBeWalked.size()-sizeOfCurrentPath);
		g.setBestPath(path);
		this.goals.get(goalIndex).setBestPath(path);		
	}
	
	
	/*
	 * Does the GreedyPathSearch for each one of the goals available
	 * in the goals List.
	 */
	public void buildTotalPathPlanning(int whichTime) {
		Iterator<Goal> itGoal = this.goals.iterator();
		Goal[] goalsTemp = new Goal[this.goals.size()];
		int counter = 0;
		
		if(this.options.isActiveConsolePrinting())
			System.out.println("\n");
		
		while(itGoal.hasNext()){
			Goal g = itGoal.next();
			goalsTemp[counter] = g;
			counter++;
			
			if(this.options.isActiveConsolePrinting())
				System.out.println("Goal " + g.getId() + " = (" + g.getEndPoint().getX() + ", " + g.getEndPoint().getY() + ")");
		}
		
		this.orderGoalById(goalsTemp);
		
		for(int i = 0; i < goalsTemp.length; i++){
			this.doGreedyPathSearch(goalsTemp[i], this.pathToBeWalked.size(), whichTime);
		}
	}
	
	/*
	 * Prints the pathToBeWalked by this Truck.
	 */
	public void printPathToBeWalked(){
		Iterator<Point> itPath = this.pathToBeWalked.iterator();
		String toPrint = "\nPath to be walked by Truck " + this.truckName + ": \n";
		toPrint+= "[ ";
		while(itPath.hasNext()){
			Point p = itPath.next();
			if(itPath.hasNext()){
				toPrint += "(" + p.getX() + ", " + p.getY() + "), ";
			}
			else toPrint += "(" + p.getX() + ", " + p.getY() + ")";
		}
		toPrint += "]";
		System.out.println(toPrint);
	}
	
	
	/*
	 * Sets everything the Truck needs to be abldoGreedyPathSearche to start collecting
	 * garbage. Should be the last method to be called on the Truck
	 * object before the Garbage Picking process starts.
	 */
	public void prepare(CityMap map) {
		this.setCompleteCityMap(map);
		
		Point startingPoint = this.selectStartingPoint();
		this.setStartPosition(startingPoint);
		this.setCurrentPosition(startingPoint);
		
		this.buildGoalsList();
		this.buildTotalPathPlanning(1);
		
		if(this.options.isActiveConsolePrinting())
			this.printPathToBeWalked();
	}

	
	
	/*
	 *
	 * 
	 * 
	 * 
	 * 
	 * 	XML EXPORT/IMPORT
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	/**
	 * 
	 * @param filename
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public String exportTruckInformation(String filename) throws ParserConfigurationException, TransformerException{
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
		id.appendChild(doc.createTextNode(Integer.toString(this.id)));
		rootElement.appendChild(id);
		
		Element truckName = doc.createElement("name");
		truckName.appendChild(doc.createTextNode(this.truckName));
		rootElement.appendChild(truckName);
		
		Element garbageType = doc.createElement("garbageType");
		garbageType.appendChild(doc.createTextNode(this.garbageType));
		rootElement.appendChild(garbageType);
		
		Element startPosition = doc.createElement("startPosition");
		startPosition.setAttribute("type", this.getStartPosition().getType());
		startPosition.setAttribute("x", Integer.toString(this.getStartPosition().getX()));
		startPosition.setAttribute("y", Integer.toString(this.getStartPosition().getY()));
		rootElement.appendChild(startPosition);
		
		Element currentPosition = doc.createElement("currentPosition");
		currentPosition.setAttribute("type", this.getCurrentPosition().getType());
		currentPosition.setAttribute("x", Integer.toString(this.getCurrentPosition().getX()));
		currentPosition.setAttribute("y", Integer.toString(this.getCurrentPosition().getY()));
		rootElement.appendChild(currentPosition);
		
		Element currentOccupation = doc.createElement("currentOccupation");
		currentOccupation.appendChild(doc.createTextNode(Double.toString(this.currentOccupation)));
		rootElement.appendChild(currentOccupation);
		
		Element maxCapacity = doc.createElement("maxCapacity");
		maxCapacity.appendChild(doc.createTextNode(Double.toString(this.maxCapacity)));
		rootElement.appendChild(maxCapacity);
		
		Element pathWalked = doc.createElement("pathWalked");
		Iterator<Point> itPathWalked = this.pathWalked.iterator();
		while(itPathWalked.hasNext()){
			Point p = itPathWalked.next();
			
			Element point = doc.createElement("pathWalkedPoint");
			point.setAttribute("x", Integer.toString(p.getX()));
			point.setAttribute("y", Integer.toString(p.getY()));
			pathWalked.appendChild(point);
		}
		rootElement.appendChild(pathWalked);
		
		Element pathToBeWalked = doc.createElement("pathToBeWalked");
		Iterator<Point> itPathToBeWalked = this.pathToBeWalked.iterator();
		while(itPathToBeWalked.hasNext()){
			Point p = itPathToBeWalked.next();
			
			Element point = doc.createElement("pathToBeWalkedPoint");
			point.setAttribute("x", Integer.toString(p.getX()));
			point.setAttribute("y", Integer.toString(p.getY()));
			pathToBeWalked.appendChild(point);
		}
		rootElement.appendChild(pathToBeWalked);
		
		Element goals = doc.createElement("goals");
		Iterator<Goal> itGoal = this.goals.iterator();
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
		
		Element garbageContainersToGo = doc.createElement("garbageContainersToGo");
		
		Iterator<GarbageContainer> itGC = this.garbageContainersToGoTo.iterator();
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
		
		Element cityMap = doc.createElement("map");
		cityMap.appendChild(doc.createTextNode(this.getCompleteCityMap().getMapsFileName()));
		rootElement.appendChild(cityMap);
		
		Element options = doc.createElement("options");
		options.appendChild(doc.createTextNode(this.getOptions().getOptionsFile()));
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
				
		return filename;
	}
	
	
	public void importFromXML(String filename) throws ParserConfigurationException, SAXException, IOException{
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
		this.pathWalked = new ArrayList<Point>();
		this.pathToBeWalked = new ArrayList<Point>();
		this.goals = new ArrayList<Goal>();
		this.garbageContainersToGoTo = new ArrayList<GarbageContainer>();
		
		File fXmlFile = new File(this.tempFilePath + "/" + filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		
		doc.getDocumentElement().normalize();
		
		String id = doc.getElementsByTagName("id").item(0).getTextContent();
		if(id != null) this.id = Integer.parseInt(id);
		
		String truckName = doc.getElementsByTagName("name").item(0).getTextContent();
		if(truckName != null) this.truckName = truckName;
		
		String garbageType = doc.getElementsByTagName("garbageType").item(0).getTextContent();
		if(garbageType != null) this.garbageType = garbageType;
		
		Element startPoint = (Element) doc.getElementsByTagName("startPosition").item(0);
		String sPtype = startPoint.getAttribute("type");
		int sPx = -1;
		sPx = Integer.parseInt(startPoint.getAttribute("x"));
		int sPy = -1;
		sPy = Integer.parseInt(startPoint.getAttribute("y"));
		
		if(sPx != -1 && sPy != -1){
			this.startPosition = new Point(sPx,sPy);
			this.startPosition.setType(sPtype);
		}
		
		Element currentPosition = (Element) doc.getElementsByTagName("currentPosition").item(0);
		String cPtype = "";
		cPtype = currentPosition.getAttribute("type");
		int cPx = -1;
		cPx = Integer.parseInt(currentPosition.getAttribute("x"));
		int cPy = -1;
		cPy = Integer.parseInt(currentPosition.getAttribute("y"));
		
		if(cPx != -1 && cPy != -1) {
			this.currentPosition = new Point(cPx, cPy);
			this.currentPosition.setType(cPtype);
		}
		
		
		String cOccupation = doc.getElementsByTagName("currentOccupation").item(0).getTextContent();
		if(cOccupation !=  null) this.currentOccupation = Double.parseDouble(cOccupation);
		
		String maxCapacity = doc.getElementsByTagName("maxCapacity").item(0).getTextContent();
		if(maxCapacity != null) this.maxCapacity = Double.parseDouble(maxCapacity);
		
		Element pathWalked = (Element) doc.getElementsByTagName("pathWalked").item(0);
		if(pathWalked != null) {
			NodeList nList = pathWalked.getElementsByTagName("point");
			for(int temp = 0; temp < nList.getLength(); temp++){
				Node nNode = nList.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					String pX = eElement.getAttribute("x");
					String pY = eElement.getAttribute("y");
					
					if(pX != null && pY != null) 
						this.pathWalked.add(new Point(Integer.parseInt(pX), Integer.parseInt(pY)));
				}
			}
		}
		
		
		Node pathToBeWalkedNode = doc.getElementsByTagName("pathToBeWalked").item(0);
		Element pathToBeWalked = (Element) pathToBeWalkedNode;
		NodeList nList = pathToBeWalked.getElementsByTagName("point");
		for(int temp = 0; temp < nList.getLength(); temp++){
			Node nNode = nList.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				
				String pX = eElement.getAttribute("x");
				String pY = eElement.getAttribute("y");
				
				if(pX != null && pY != null) 
					this.pathToBeWalked.add(new Point(Integer.parseInt(pX), Integer.parseInt(pY)));
			}
		}
		
		nList = doc.getElementsByTagName("goal");
		for(int temp = 0; temp < nList.getLength(); temp++){
			Node nNode = nList.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				
				String goalIdStr = eElement.getElementsByTagName("id").item(0).getTextContent();
				int goalId = -1;
				if(goalIdStr != null) goalId = Integer.parseInt(goalIdStr);
				
				Element goalStartPoint = (Element) eElement.getElementsByTagName("startPoint").item(0);
				int gSPx = -1;
				gSPx = Integer.parseInt(goalStartPoint.getAttribute("x"));
				int gSPy = -1;
				gSPy = Integer.parseInt(goalStartPoint.getAttribute("y"));
				
				Element goalEndPoint = (Element) eElement.getElementsByTagName("endPoint").item(0);
				int gEPx = -1;
				gEPx = Integer.parseInt(goalEndPoint.getAttribute("x"));
				int gEPy = -1;
				gEPy = Integer.parseInt(goalEndPoint.getAttribute("y"));
				
				Goal g = null;
				if(goalId != -1 && gSPx != -1 && gSPy != -1 && gEPx != -1 && gEPy != -1)
					g = new Goal(goalId, new Point(gSPx, gSPy), new Point(gEPx, gEPy));
			
				Element bestPath = (Element) eElement.getElementsByTagName("bestPath").item(0);
				
				String bestPathId = "";
				bestPathId = bestPath.getElementsByTagName("id").item(0).getTextContent();
				int bPId = -1;
				if(bestPathId != "") bPId = Integer.parseInt(bestPathId);
				
				String bestPathLength = "";
				bestPathLength = bestPath.getElementsByTagName("length").item(0).getTextContent();
				int bPL = -1;
				if(bestPathLength != "") bPL = Integer.parseInt(bestPathLength);
				
				List<Point> bestPathPoints = new ArrayList<Point>();
				NodeList nListPoints = bestPath.getElementsByTagName("point");
				for(int temp2 = 0; temp2 < nListPoints.getLength(); temp2++){
					Node nNodePoints = nListPoints.item(temp);
					
					if (nNodePoints.getNodeType() == Node.ELEMENT_NODE) {
						Element pElement = (Element) nNodePoints;
						
						String pXStr = pElement.getAttribute("x");
						String pYStr = pElement.getAttribute("y");
						int pX = -1;
						int pY = -1;
						
						if(pXStr != null && pYStr != null){
							pX = Integer.parseInt(pXStr);
							pY = Integer.parseInt(pYStr);
							bestPathPoints.add(new Point(pX, pY));
						}
					}
				}
				
				if(g != null) {
					g.setBestPath(new Path(bPId));
					g.getBestPath().setLength(bPL);
					g.getBestPath().setPoints(bestPathPoints);
					
					this.goals.add(g);
				}
			}
		}
		
		nList = doc.getElementsByTagName("garbageContainer");
		for(int temp = 0; temp < nList.getLength(); temp++){
			Node nNode = nList.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				
				String gcIdStr = eElement.getElementsByTagName("id").item(0).getTextContent();
				int gcId = -1;
				if(gcIdStr != null) gcId = Integer.parseInt(gcIdStr);
				
				String gcType = "";
				gcType = eElement.getElementsByTagName("type").item(0).getTextContent();
				
				String gcCurrentOccStr = eElement.getElementsByTagName("currentOccupation").item(0).getTextContent();
				double gcCurrentOccupation = -1;
				if(gcCurrentOccStr != null) gcCurrentOccupation = Double.parseDouble(gcCurrentOccStr);
				
				String gcMaxCapStr = eElement.getElementsByTagName("maxCapacity").item(0).getTextContent();
				double gcMaxCapacity = -1;
				if(gcMaxCapStr != null) gcMaxCapacity = Double.parseDouble(gcMaxCapStr);
				
				Element gcPos = (Element) eElement.getElementsByTagName("position").item(0);
				int gcPosx = -1;
				gcPosx = Integer.parseInt(gcPos.getAttribute("x"));
				int gcPosy = -1;
				gcPosy = Integer.parseInt(gcPos.getAttribute("y"));
				
				
				if(gcId != -1 && gcType != "" && gcMaxCapacity != -1 && gcCurrentOccupation != - 1 && gcPosx != -1 && gcPosy != -1){
					GarbageContainer gc = new GarbageContainer(gcId, gcType, gcMaxCapacity, gcCurrentOccupation, new Point(gcPosx, gcPosy));
					this.garbageContainersToGoTo.add(gc);
				}
			}
		}
		try {
			String map = doc.getElementsByTagName("map").item(0).getTextContent();
			if(map != null) this.completeCityMap = new CityMap(map);
		} catch(NullPointerException e){
			e.printStackTrace();
		}
		
		
		String options = "";
		options = doc.getElementsByTagName("options").item(0).getTextContent();
		if(options != "") {
			this.options = new Options();
			this.options.importOptions(options);
		}
	}
	
	/*
	 * 
	 * 
	 * 
	 *
	 * 
	 * 
	 * 
	 * CONSTRUCTOR.
	 *
	 *
	 *
	 * 
	 * 
	 * 
	 */

	
	/**
	 * Constructor with 3 arguments (ID, truckName and startingPoint).
	 * @param ID
	 * @param n
	 * @param gT
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Truck(int ID, String n, String gT) throws ParserConfigurationException, SAXException, IOException{
		this.id = ID;
		
		if(!n.equals(""))
			this.truckName = n;
		else this.truckName = "Truck" + this.id;
		
		if(!gT.equals(""))
				this.garbageType = gT;
		else this.garbageType = "undifferentiated";
		
		this.pathWalked = new ArrayList<Point>();
		
		this.pathToBeWalked = new ArrayList<Point>();
		
		this.goals = new ArrayList<Goal>();
		
		this.garbageContainersToGoTo = new ArrayList<GarbageContainer>();
		
		this.options = new Options();
		this.options.importOptions("options.xml");
	}

}
