package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import units.Truck;
import map.GarbageContainer;

public class Plan {
	
	private Truck truck;
	private HashMap<GarbageContainer, Double> assignment;

	public Truck getTruck() {
		return truck;
	}

	public void setTruck(Truck truck) {
		this.truck = truck;
	}

	public HashMap<GarbageContainer, Double> getAssignment() {
		return assignment;
	}

	public void setAssignment(HashMap<GarbageContainer, Double> assignment) {
		this.assignment = assignment;
	}
	
	public void addElementToAssignment(GarbageContainer gc, double d){
		this.assignment.put(gc, d);
	}
	
	public void removeElementFromAssignment(GarbageContainer gc){
		this.assignment.remove(gc);
	}
	
	public void changeValueToCollect(GarbageContainer gc, double newValue){
		if(this.assignment.containsKey(gc)){
			this.assignment.remove(gc);
			this.assignment.put(gc, newValue);
		}
	}
	
	public List<GarbageContainer> getAllGarbageContainers() {
		List<GarbageContainer> gcList = new ArrayList<GarbageContainer>();
		
		Iterator it = this.assignment.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pairs = (Map.Entry<GarbageContainer, Double>)it.next();
			gcList.add((GarbageContainer) pairs.getKey());
		}
		
		return gcList;
	}
	
	public Plan(Truck t, HashMap<GarbageContainer, Double> assign){
		this.truck = t;
		this.assignment = new HashMap<GarbageContainer, Double>();
		this.assignment = assign;
	}
	
}
