package ai;

import java.util.ArrayList;
import java.util.List;

import map.Point;

public class Goal {
	private int id;
	private Point startPoint;
	private Point endPoint;
	//private List<Path> possiblePaths;
	private Path bestPath;
	
	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public Point getStartPoint() {
		return startPoint;
	}


	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}


	public Point getEndPoint() {
		return endPoint;
	}


	public void setEndPoint(Point endPoint) {
		this.endPoint = endPoint;
	}


	public Path getBestPath() {
		return bestPath;
	}


	public void setBestPath(Path bestPath) {
		this.bestPath = bestPath;
	}
	
	
	/*
	 * Calculates Euclidean distance between two points passed as parameter.
	 */
	public double euclideanDistance(Point start, Point destination){
		double h = Math.sqrt(Math.pow((start.getX() - destination.getX()), 2) + Math.pow(start.getY()-destination.getY(), 2));
		return h;
	}
	

	/*
	 * Constructor for the Goal object with 3 parameters (corresponding to
	 * id, startPoint, endPoint).
	 */
	public Goal(int ID, Point sP, Point eP){
		this.id = ID;
		this.setStartPoint(sP);
		this.setEndPoint(eP);
		
	}
}
