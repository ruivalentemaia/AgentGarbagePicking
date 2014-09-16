package ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import agent.Truck;

public class Assignment {
	private int id;
	private List<Truck> trucks;
	private Map<Truck, Goal> assignments;
	
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public List<Truck> getTrucks() {
		return trucks;
	}

	public void setTrucks(List<Truck> trucks) {
		this.trucks = trucks;
	}

	public Map<Truck, Goal> getAssignments() {
		return assignments;
	}

	public void setAssignments(Map<Truck, Goal> assignments) {
		this.assignments = assignments;
	}
	
	
	
	/*
	 * Count total number of different goals between all the
	 * agents.
	 */
	public int countDifferentGoals() {
		int nGoals = 0;
		List<Goal> allGoals = new ArrayList<Goal>();
		
		Iterator<Truck> itTrucks = this.trucks.iterator();
		while(itTrucks.hasNext()) {
			Truck t = itTrucks.next();
			allGoals.addAll(t.getGoals());
		}
		
		SortedSet<Goal> goals = new TreeSet<Goal>(new Comparator<Goal>() {
		    @Override
		    public int compare(Goal g1, Goal g2) {
		    	if( (g1.getStartPoint().getX() == g2.getStartPoint().getX()) 
						&& (g1.getStartPoint().getY() == g2.getStartPoint().getY()) 
						&& (g1.getEndPoint().getX() == g2.getEndPoint().getX()) 
						&& (g1.getEndPoint().getY() == g2.getEndPoint().getY())){
		    		
		    	   return 0;
		       }
		       return 1;
		    }
		});
		goals.addAll(allGoals);
		
		allGoals = new ArrayList<Goal>();
		allGoals.addAll(goals);
		
		nGoals = allGoals.size();
		
		return nGoals;
	}
	
	
	/*
	 * Retrieves a list with all the different Goals from all
	 * the Trucks available in the trucks list.
	 */
	public List<Goal> getAllDifferentGoals(){
		List<Goal> allGoals = new ArrayList<Goal>();
		
		Iterator<Truck> itTrucks = this.trucks.iterator();
		while(itTrucks.hasNext()) {
			Truck t = itTrucks.next();
			allGoals.addAll(t.getGoals());
		}
		
		SortedSet<Goal> goals = new TreeSet<Goal>(new Comparator<Goal>() {
		    @Override
		    public int compare(Goal g1, Goal g2) {
		    	if( (g1.getStartPoint().getX() == g2.getStartPoint().getX()) 
						&& (g1.getStartPoint().getY() == g2.getStartPoint().getY()) 
						&& (g1.getEndPoint().getX() == g2.getEndPoint().getX()) 
						&& (g1.getEndPoint().getY() == g2.getEndPoint().getY())){
		    		
		    	   return 0;
		       }
		       return 1;
		    }
		});
		goals.addAll(allGoals);
		
		allGoals.clear();
		allGoals.addAll(goals);
		
		return allGoals;
	}
	
	
	/*
	 * Allocates lengths of all different goals in a matrix of integers
	 * to be return in the end of the method.
	 */
	public int[][] createCostMatrix(List<Goal> gs){
		int [][] costs = new int[this.trucks.size()][this.countDifferentGoals()];
		int goalCounter = 0;
		//allocates goals length to the costs matrix.
		for(int i = 0; i < costs.length; i++){
			
			Truck t = this.trucks.get(i);
			String tName = t.getTruckName();
			Iterator<Goal> itGoals = gs.iterator();
			while(itGoals.hasNext()) {
				Goal g = itGoals.next();
				if(t.hasGoal(g)){
					int index = t.getGoals().indexOf(g);
					if(index != -1) {
						Path bestPath = t.getGoals().get(index).getBestPath();
						int pathLength = bestPath.getLength();
						costs[i][goalCounter] = pathLength;
						goalCounter++;
					}
				}
			}
		}
		
		return costs;
	}
	
	
	/*
	 * Prints the costs matrix.
	 */
	public void printCostsMatrix(int[][] costs){
		System.out.println("\nCOSTS MATRIX: ");
		String debugPrinter = "     ";
		
		for(int j = 0; j < costs[0].length; j++){
			debugPrinter += "G" + (j+1) + " ";
		}
		debugPrinter += "\n";
		
		for(int i = 0; i < costs.length; i++){
			debugPrinter += "T" + (i+1) + " [ ";
			for(int j = 0; j < costs[0].length; j++){
				if(j == costs[0].length - 1){
					debugPrinter += costs[i][j];
				}
					
				else {
					debugPrinter += costs[i][j] + ", ";
				}
					
			}
			debugPrinter += " ]\n";
		}
		System.out.println(debugPrinter);
	}
	
	
	/*
	 * The Hungarian Method for assignment problems.
	 */
	public void computeHungarianMethod() {
		int [][] costs = new int[this.trucks.size()][this.countDifferentGoals()];
		List<Goal> allGoals = this.getAllDifferentGoals();
		
		//allocates all costs in a matrix.
		costs = this.createCostMatrix(allGoals);
		this.printCostsMatrix(costs);
		System.out.println();
		
	}
	
	
	/**
	 * 
	 * 
	 * CONSTRUCTOR.
	 * 
	 * 
	 */
	
	public Assignment(List<Truck> ts) {
		this.trucks = new ArrayList<Truck>();
		this.trucks = ts;
		this.assignments = new LinkedHashMap<Truck, Goal>();
	}
	
}
