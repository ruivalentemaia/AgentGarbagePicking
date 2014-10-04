package agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ai.TransportationAlgorithm;
import map.CityMap;
import map.GarbageContainer;
import jade.core.Agent;

public class PlannerAgent extends Agent{

	private static final long serialVersionUID = 4733803062082964773L;
	
	private CityMap map;
	private List<Truck> trucks;
	private List<TransportationAlgorithm> transportPlan;
	
	public CityMap getMap() {
		return map;
	}

	public void setMap(CityMap map) {
		this.map = map;
	}

	public List<Truck> getTrucks() {
		return trucks;
	}

	public void setTrucks(List<Truck> trucks) {
		this.trucks = trucks;
	}

	public List<TransportationAlgorithm> getTransportPlan() {
		return transportPlan;
	}

	public void setTransportPlan(List<TransportationAlgorithm> transportPlan) {
		this.transportPlan = transportPlan;
	}
	
	
	public void adjustGarbageAndTrucksNeeds(HashMap<String, Boolean> gbgTypeAndTrucks) {
		Iterator it = gbgTypeAndTrucks.entrySet().iterator();
		boolean allSet = true;
		List<String> typesNotSet = new ArrayList<String>();
		while(it.hasNext()){
			Map.Entry<String, Boolean> pairs = (Map.Entry<String, Boolean>) it.next();
			if(!pairs.getValue()){
				allSet = false;
				typesNotSet.add(pairs.getKey());
			}
		}
		
		if(!allSet){
			
			if(this.map.getOptions().isAllowPlannerAgentToCreateTrucks()){
				Iterator<String> itTypes = typesNotSet.iterator();
				while(itTypes.hasNext()){
					String type = itTypes.next();
					int id = this.trucks.get(this.trucks.size()-1).getId() + 1;
					Truck t = new Truck(id, "t" + id, type);
					t.setMaxCapacity(250);
					t.prepare(this.map);
					this.trucks.add(t);
					this.map.getTrucks().add(t);
				}
			}
			
			else {
				Iterator<Truck> itTruck = this.trucks.iterator();
				
				while(itTruck.hasNext()){
					Truck t = itTruck.next();
					Iterator<String> itTypes = typesNotSet.iterator();
					while(itTypes.hasNext()){
						String type = itTypes.next();
						if(t.getGarbageType().equals(type)){
							itTruck.remove();
						}
					}
				}
				
				itTruck = this.map.getTrucks().iterator();
				while(itTruck.hasNext()){
					Truck t = itTruck.next();
					Iterator<String> itTypes = typesNotSet.iterator();
					while(itTypes.hasNext()){
						String type = itTypes.next();
						if(t.getGarbageType().equals(type)){
							itTruck.remove();
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * Constructor of the PlannerAgent agent.
	 * @param map A map of the city where the garbage will be collected.
	 * @param trucks A list of trucks to collect the garbage.
	 */
	public PlannerAgent(CityMap map, List<Truck> trucks) {
		this.map = map;
		this.trucks = trucks;
		this.transportPlan = new ArrayList<TransportationAlgorithm>();
		
		List<GarbageContainer> garbageContainers = new ArrayList<GarbageContainer>();
		garbageContainers = this.map.getGarbageContainers();
		
		
		//builds a list of all GarbageContainer types available
		List<String> gcTypesAvailable = new ArrayList<String>();
		Iterator<GarbageContainer> itGC = garbageContainers.iterator();
		while(itGC.hasNext()){
			GarbageContainer gc = itGC.next();
			Iterator<String> itGCTypes = gcTypesAvailable.iterator();
			boolean typeExists = false;
			
			while(itGCTypes.hasNext()){
				String type = itGCTypes.next();
				if(gc.getType().equals(type)) typeExists = true;
			}
			
			if(!typeExists) {
				gcTypesAvailable.add(gc.getType());
			}
		}
		
		/*
		 * This agent needs to verify if there are Trucks able to collect
		 * all the types of garbage.
		 */
		HashMap<String, Boolean> gbgTypeAndTrucks = new HashMap<String, Boolean>();
		Iterator<String> itGCTypes = gcTypesAvailable.iterator();
		while(itGCTypes.hasNext()){
			String type = itGCTypes.next();
			Iterator<Truck> truckIt = this.trucks.iterator();
			boolean hasTruckToCollectIt = false;
			
			while(truckIt.hasNext()){
				Truck t = truckIt.next();
				if(t.getGarbageType().equals(type))
					hasTruckToCollectIt = true;
			}
			
			gbgTypeAndTrucks.put(type, hasTruckToCollectIt);
		}
		
		/*
		 * What if there is some GarbageType in the map that does not have any Truck
		 * able to collect it ? 
		 * If that happens and if the Options allow it, the Planner Agent will create a 
		 * new Truck for that kind of GarbageType. If the Options don't allow it, then 
		 * the GarbageContainers of that type will not be part of the problem (will be 
		 * removed from the CityMap object).
		 */
		Iterator it = gbgTypeAndTrucks.entrySet().iterator();
		boolean allSet = true;
		List<String> typesNotSet = new ArrayList<String>();
		while(it.hasNext()){
			Map.Entry<String, Boolean> pairs = (Map.Entry<String, Boolean>) it.next();
			if(!pairs.getValue()){
				allSet = false;
				typesNotSet.add(pairs.getKey());
			}
		}
		
		if(!allSet) this.adjustGarbageAndTrucksNeeds(gbgTypeAndTrucks);
		
		//creates a Transportation Algorithm for each one of the types.
		Iterator<String> itType = gcTypesAvailable.iterator();
		while(itType.hasNext()){
			String type = itType.next();
			
			List<Truck> containerTrucks = this.map.selectTruckByGarbageType(type);
			List<GarbageContainer> containersGC = map.selectGarbageContainersByType(type);
			
			TransportationAlgorithm transportation = new TransportationAlgorithm(containerTrucks, containersGC);
			transportation.performTransportationAlgorithm();
			
			this.transportPlan.add(transportation);
		}
		
	}

}
