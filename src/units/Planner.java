package units;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import agent.TruckAgent;
import ai.TransportationAlgorithm;
import map.CityMap;
import map.GarbageContainer;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class Planner{
	
	private CityMap map;
	private List<Truck> trucks;
	private List<TransportationAlgorithm> transportPlan;
	private List<String> truckAgents;
	
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
	
	
	/*
	 * 
	 * 
	 * 
	 * 
	 * 		AUXILIARY METHODS.
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	public List<String> getTruckAgents() {
		return truckAgents;
	}

	public void setTruckAgents(List<String> truckAgents) {
		this.truckAgents = truckAgents;
	}

	/**
	 * 
	 * @param gbgTypeAndTrucks
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void adjustGarbageAndTrucksNeeds(HashMap<String, Boolean> gbgTypeAndTrucks) throws ParserConfigurationException, SAXException, IOException {
		boolean allSet = true;
		List<String> typesNotSet = new ArrayList<String>();
		
		Iterator it = gbgTypeAndTrucks.entrySet().iterator();
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
	
	
	
	/*
	 * 
	 * 
	 * 
	 * 
	 *		CONSTRUCTOR 
	 * 
	 * 
	 * 
	 */
	
	
	/**
	 * Constructor of the PlannerAgent agent. It generates an optimal solution
	 * for each set of Trucks with the same garbage types through a Transportation
	 * algorithm, depending on their position and on their maximum capacity
	 * to carry garbage.s
	 * @param map A map of the city where the garbage will be collected.
	 * @param trucks A list of trucks to collect the garbage.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public Planner(CityMap map, List<Truck> trucks) throws ParserConfigurationException, SAXException, IOException {
		this.map = map;
		this.trucks = trucks;
		this.transportPlan = new ArrayList<TransportationAlgorithm>();
		this.truckAgents = new ArrayList<String>();
		
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
			
			Truck e = transportation.getTrucks().get(transportation.getTrucks().size() - 1);
			if(e.getTruckName().contains("E")){
				this.trucks.add(e);
				this.map.getTrucks().add(e);
			}
			
			int numberTrucksInTransportation = transportation.getTrucks().size();
			int numberTrucksHere = containerTrucks.size();
			
			if(numberTrucksInTransportation == numberTrucksHere) {
				this.transportPlan.add(transportation);
			}
			
			else {
				containerTrucks = transportation.getTrucks();
				transportation = new TransportationAlgorithm(containerTrucks, containersGC);
				transportation.performTransportationAlgorithm();
				this.transportPlan.add(transportation);
			}
		}
	}

}
