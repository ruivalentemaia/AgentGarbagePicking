package units;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

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

	
	/**
	 * 
	 * 
	 * 
	 *
	 * 	CONSTRUCTOR.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 *
	 * 
	 * 
	 * 
	 */

	
	/*
	 * Constructor with 3 arguments (ID, truckName and startingPoint).
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
