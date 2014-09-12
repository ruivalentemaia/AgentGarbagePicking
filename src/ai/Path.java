package ai;

import java.util.ArrayList;
import java.util.List;

import map.Point;

public class Path {
	private int id;
	private List<Point> points;
	
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}
	
	
	/*
	 * Adds Point p to the points ArrayList.
	 */
	public void addPoint(Point p) {
		this.points.add(p);
	}
	
	
	/*
	 * Removes Point p to the points ArrayList.
	 */
	public void removePoint(Point p){
		this.points.remove(p);
	}
	
	
	/*
	 * Constructor of the Path object with only 1 argument (ID).
	 */
	public Path(int ID) {
		this.id = ID;
		this.points = new ArrayList<Point>();
	}
}
