package map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Road {
	private int id;
	private String direction;
	private int length;
	private List<Point> points;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	public int getLength(){
		return length;
	}
	
	public void setLength(int length){
		this.length = length;
	}

	public int getWidth() {
		return length;
	}

	public void setWidth(int width) {
		this.length = width;
	}


	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}
	
	
	/*
	 * Constructor of a Road object with parameters
	 * id, direction, width, height and a list of points.
	 */
	public Road(int Id, String dir, int l){
		this.id = Id;
		this.direction = dir;
		this.length = l;
	}
	
	/*
	 * Print Road information.
	 */
	public void printInfo() {
		System.out.println("\n");
		System.out.println("Road " + this.id + ":");
		System.out.println("Direction: " + this.direction);
		System.out.println("Length = " + this.length);
	}
	
	
}
