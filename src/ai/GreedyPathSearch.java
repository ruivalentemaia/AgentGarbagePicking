package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import map.Crossroads;
import map.Point;

public class GreedyPathSearch {
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
	 * Calculates F = H for all the Points in the openList.
	 */
	public void calculateFHeuristicForOpenList() {
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
	 * Orders the fHeuristic HashMap by key.
	 */
	private double[] orderFHeuristic() {
		Map<Double, Point> map = new TreeMap<Double, Point>(this.fHeuristic);
		double[] heuristics = new double[map.size()];
		int counter = 0;
		for(Entry<Double, Point> entry : map.entrySet()){
			heuristics[counter] = entry.getKey();
			counter++;
		}
		return heuristics;
	}
	
	/*
	 * Get nth minimum value in the fHeuristics map.
	 */
	public Point getMinimumValue(int n){
		double[] heuristics = this.orderFHeuristic();
		double minimumValue = heuristics[n];
		return this.fHeuristic.get(minimumValue);
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
	 * Backtracks the path when the path found is looping in a local minimum.
	 */
	public void backtrack(Crossroads toCrossroad){
		Point center = toCrossroad.getCenter();
		center.setType("CROSSROADS");
		double lowerHeuristic = 100000;
		
		/*
		 * Removes all the entries in the fHeuristic Hashmap
		 * which keys (heuristic values) are lower than the one
		 * of the last Crossroads.
		 */
		for(Entry<Double, Point> entry : this.fHeuristic.entrySet()){
			if( (center.getX() == entry.getValue().getX()) && 
				(center.getY() == entry.getValue().getY()) &&
				(center.getType().equals(entry.getValue().getType()))) {
					lowerHeuristic = entry.getKey();
					break;
			}
		}
		
		Iterator it = this.fHeuristic.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Double, Point> entry = (Entry<Double, Point>) it.next();
			if(entry.getKey() < lowerHeuristic){
				it.remove();
			}
		}
		
		/*
		 * Removes all elements inserted in the openList after the
		 * Crossroads toCrossroad to which we want to backtrack.
		 */
		Iterator<Point> itOL = this.openList.iterator();
		boolean toRemove = false;
		while(itOL.hasNext()){
			Point p = itOL.next();
			if( (p.getX() == center.getX()) && (p.getY() == center.getY()) && (p.getType().equals(center.getType()))) {
				toRemove = true;
			}
			if(toRemove){
				itOL.remove();
			}
		}
		
		/*
		 * Removes all elements inserted in the openList after the
		 * Crossroads toCrossroad to which we want to backtrack.
		 */
		Iterator<Point> itCL = this.closedList.iterator();
		toRemove = false;
		while(itCL.hasNext()){
			Point p = itCL.next();
			if( (p.getX() == center.getX()) && (p.getY() == center.getY()) && (p.getType().equals(center.getType()))) {
				toRemove = true;
			}
			if(toRemove){
				itCL.remove();
			}
		}
		
	}
	
	
	/*
	 * Greedy algorithm constructor.
	 */
	public GreedyPathSearch(Goal goal){
		this.openList = new ArrayList<Point>();
		this.closedList = new ArrayList<Point>();
		this.goal = goal;
		this.fHeuristic = new HashMap<Double, Point>();
		this.openList.add(goal.getStartPoint());
	}
}
