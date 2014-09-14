package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import map.Point;

public class AStar {
	private List<Point> openList;
	private List<Point> closedList;
	private Goal goal;
	private Map<Double, Point> fHeuristic;
	
	public List<Point> getOpenList() {
		return openList;
	}
	
	public void setOpenList(List<Point> openList) {
		this.openList = openList;
	}

	public List<Point> getClosedList() {
		return closedList;
	}

	public void setClosedList(List<Point> closedList) {
		this.closedList = closedList;
	}

	public Goal getGoal() {
		return goal;
	}

	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	
	
	public Map<Double, Point> getfHeuristic() {
		return fHeuristic;
	}

	public void setfHeuristic(Map<Double, Point> fHeuristic) {
		this.fHeuristic = fHeuristic;
	}

	
	/*
	 * Calculates F = H + G for all the Points in the openList.
	 */
	public void calculateFHeuristicForOpenList(int g) {
		Iterator<Point> openListIt = this.openList.iterator();
		while(openListIt.hasNext()){
			Point p = openListIt.next();
			double h = this.goal.euclideanDistance(p, this.goal.getEndPoint());
			double f = h;
			this.fHeuristic.put(f, p);
		}
	}
	
	
	/*
	 * Calculates the minimum value in the fHeuristic list.
	 */
	public Point minimumFHeuristic() {
		Iterator<Entry<Double, Point>> fHeuristicIt = this.fHeuristic.entrySet().iterator();
		double min = 100000000;
		double d = 0;
		Point finalPoint = new Point(0,0);
		while(fHeuristicIt.hasNext()) {
			Map.Entry<Double,Point> heu = fHeuristicIt.next();
			Point p = heu.getValue();
			d = heu.getKey();
			if(d < min) {
				min = d;
				finalPoint = p; 
			}
		}
		return finalPoint;
	}
	
	
	/*
	 * Checks if a parameter Point is in the openList.
	 */
	public boolean checkPointInOpenList(Point p){
		boolean exists = false;
		Iterator<Point> itOpen = this.openList.iterator();
		while(itOpen.hasNext()){
			Point n = itOpen.next();
			if( (n.getX() == p.getX()) && (n.getY() == p.getY())) {
				exists = true;
				return exists;
			}
		}
		return exists;
	}
	
	
	/*
	 * Checks if a parameter Point is in the closedList.
	 */
	public boolean checkPointInClosedList(Point p){
		boolean exists = false;
		Iterator<Point> itClosed = this.closedList.iterator();
		while(itClosed.hasNext()){
			Point n = itClosed.next();
			if( (n.getX() == p.getX()) && (n.getY() == p.getY())) {
				exists = true;
				return exists;
			}
		}
		return exists;
	}
	
	
	/*
	 * A* constructor.
	 */
	public AStar(Goal goal){
		this.openList = new ArrayList<Point>();
		this.closedList = new ArrayList<Point>();
		this.goal = goal;
		this.fHeuristic = new HashMap<Double, Point>();
		this.openList.add(goal.getStartPoint());
	}
}
