package agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ai.AStar;
import ai.Goal;
import map.CityMap;
import map.GarbageContainer;
import map.Point;
import map.Road;
import jade.core.Agent;

public class Truck extends Agent {
	private static final long serialVersionUID = 1L;
	
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
	
	
	/*
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
	 * Selects one starting point (the closest to the coordinate (0,0) of
	 * the completeCityMap attribute.
	 */
	public Point selectStartingPoint() {
		Iterator<Point> pointIt = this.completeCityMap.getPoints().iterator();
		while(pointIt.hasNext()) {
			Point p = pointIt.next();
			if(p.getType().equals("ROAD")){
				System.out.println("Starting Point for Truck " + this.truckName + " = (" + p.getX() + ", " + p.getY() + ")");
				return p;
			}
		}
		return null;
	}
	
	
	/*
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
	
	
	
	/*
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
				Point gcPos = gc.getPosition();
				Road r = this.completeCityMap.selectRoadFromGarbageContainer(gcPos);
				Point finalPos = this.completeCityMap.selectPointFromRoad(r, gcPos);
				Goal g = new Goal(goalCounter, this.startPosition, finalPos);
				this.goals.add(g);
				goalCounter++;
				this.garbageContainersToGoTo.add(gc);
				System.out.println("Goal " + g.getId() + " = (" + g.getEndPoint().getX() + ", " + g.getEndPoint().getY() + ")");
			}
		}
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
	 * 
	 */
	public void doAStar(){
		this.buildGoalsList();
		Goal goal = this.goals.get(1);
		AStar astar = new AStar(goal);
		int currentG = 1;
		double currentH = goal.euclideanDistance(goal.getStartPoint(), goal.getEndPoint());
		double neighbourH = 0;
		int iteration = 1;
		
		while( !this.currentPosEqualToFinalPos(goal)) {
			
			astar.calculateFHeuristicForOpenList(currentG);
			Point bestNode = astar.minimumFHeuristic();
			currentH = goal.euclideanDistance(this.currentPosition, goal.getEndPoint());
			
			//DEBUG
			System.out.println("Iteration " + iteration);
			iteration++;
			System.out.println("\t Best Node = (" + bestNode.getX() + ", " + bestNode.getY() + ")");
			System.out.println("\t F = " + currentH);
			
			if( (bestNode.getX() == astar.getGoal().getEndPoint().getX()) &&
				(bestNode.getY() == astar.getGoal().getEndPoint().getY()) ) {
				break;
			}
			else {
				this.pathWalked.add(this.currentPosition);
				this.currentPosition = bestNode;
				astar.getClosedList().add(bestNode);
				
				List<Point> neighbours = new ArrayList<Point>();
				neighbours = this.completeCityMap.selectNeighbourPoints(bestNode);
				
				Iterator<Point> neighboursIt = neighbours.iterator();
				
				while(neighboursIt.hasNext()){
					Point neighbour = neighboursIt.next();
					currentG = astar.getClosedList().size();
					int neighbourG = currentG + 1;
					neighbourH = goal.euclideanDistance(neighbour, goal.getEndPoint());
					
					
					if( (astar.checkPointInClosedList(neighbour)) && (neighbourG > currentG) ){
						
					}
					
					else if(astar.checkPointInOpenList(neighbour) && (neighbourG > currentG)) {
						
					}
					
					else if( !(astar.checkPointInOpenList(neighbour)) || !(astar.checkPointInClosedList(neighbour))) {
						astar.getOpenList().add(neighbour);
					}
				}
			}
		}
	}
	
	
	
	/*
	 * 
	 * 
	 * 
	 *
	 * 	CONSTRUCTOR.
	 *
	 * 
	 * 
	 * 
	 */

	
	/*
	 * Constructor with 3 arguments (ID, truckName and startingPoint).
	 */
	public Truck(int ID, String n, String gT){
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
	}

}
