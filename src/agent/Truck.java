package agent;

import java.util.ArrayList;
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
	
	
	/*
	 *
	 * 
	 * 	GETS AND SETS FOR ALL THE ATTRIBUTES.
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
	}

}
