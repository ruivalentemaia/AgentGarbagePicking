package ai;

import java.util.ArrayList;
import java.util.List;

import map.Point;

public class Goal {
	private int id;
	private Point startPoint;
	private Point endPoint;
	private List<Path> possiblePaths;
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

	public List<Path> getPossiblePaths() {
		return possiblePaths;
	}


	public void setPossiblePaths(List<Path> possiblePaths) {
		this.possiblePaths = possiblePaths;
	}


	public Path getBestPath() {
		return bestPath;
	}


	public void setBestPath(Path bestPath) {
		this.bestPath = bestPath;
	}


	/*
	 * Constructor for the Goal object with 3 parameters (corresponding to
	 * id, startPoint, endPoint).
	 */
	public Goal(int ID, Point sP, Point eP){
		this.id = ID;
		this.setStartPoint(sP);
		this.setEndPoint(eP);
		
		this.possiblePaths = new ArrayList<Path>();
	}
}
