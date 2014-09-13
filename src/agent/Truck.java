package agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ai.Goal;
import map.Point;
import jade.core.Agent;

public class Truck extends Agent {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String truckName;
	private Point startPosition;
	private Point currentPosition;
	private List<Point> pathWalked;
	private List<Point> pathToBeWalked;
	private List<Goal> goals;
	
	//complete list of the CityMap points
	private List<Point> completeCityMap;
	
	
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


	public Point getStartPosition() {
		return startPosition;
	}


	public void setStartPosition(Point startPosition) {
		this.startPosition = startPosition;
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


	public List<Point> getCompleteCityMap() {
		return completeCityMap;
	}


	public void setCompleteCityMap(List<Point> completeCityMap) {
		this.completeCityMap = completeCityMap;
	}
	
	
	/*
	 * Gets the maximum X value in the list of map points.
	 */
	public int getMaxX() {
		int max = 0;
		Iterator<Point> itMap = this.completeCityMap.iterator();
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
		Iterator<Point> itMap = this.completeCityMap.iterator();
		while(itMap.hasNext()){
			Point p = itMap.next();
			if(p.getY() > max){
				max = p.getY();
			}
		}
		return max;
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
			index = this.completeCityMap.indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
			}
			
			//test if it's a Road.
			futurePos.setType("ROAD");
			index = this.completeCityMap.indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
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
			index = this.completeCityMap.indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
			}
			
			//test if it's a Road.
			futurePos.setType("ROAD");
			index = this.completeCityMap.indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
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
			index = this.completeCityMap.indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
			}
			
			//test if it's a Road.
			futurePos.setType("ROAD");
			index = this.completeCityMap.indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
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
			index = this.completeCityMap.indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
			}
			
			//test if it's a Road.
			futurePos.setType("ROAD");
			index = this.completeCityMap.indexOf(futurePos);
			if(index != -1){
				this.currentPosition = futurePos;
				this.pathWalked.add(this.currentPosition);
				success = 1;
				return success;
			}
			
		}
		
		return success;
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
	public Truck(int ID, String n, Point sP){
		this.id = ID;
		
		if(!n.equals(""))
			this.truckName = n;
		else this.truckName = "Truck" + this.id;
		
		if( (sP.getX() >= 0) && (sP.getY() >= 0))
			this.startPosition = sP;
		else this.startPosition = new Point(0,0);
		
		this.currentPosition = startPosition;
		
		this.pathWalked = new ArrayList<Point>();
		this.pathWalked.add(this.currentPosition);
		
		this.pathToBeWalked = new ArrayList<Point>();
		
		this.goals = new ArrayList<Goal>();
		
		this.completeCityMap = new ArrayList<Point>();
	}

}
