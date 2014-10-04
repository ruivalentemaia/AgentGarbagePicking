package examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import map.CityMap;

import org.xml.sax.SAXException;

import agent.Planner;
import agent.Truck;

public class AgentPlannerExample {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException  {
		CityMap map = new CityMap("mapTest25402030.xml");
		map.printCityMapString();
		
		List<Truck> trucks = new ArrayList<Truck>();
		
		Truck t2 = new Truck(2, "t2", "container");
		t2.setMaxCapacity(299.0);
		t2.prepare(map);
		trucks.add(t2);
		map.getTrucks().add(t2);
		
		Truck t3 = new Truck(3, "t3", "container");
		t3.setMaxCapacity(260.0);
		t3.prepare(map);
		trucks.add(t3);
		map.getTrucks().add(t3);
		
		Truck t4 = new Truck(4, "t4", "container");
		t4.setMaxCapacity(240.0);
		t4.prepare(map);
		trucks.add(t4);
		map.getTrucks().add(t4);
		
		Truck t5 = new Truck(5, "t5", "container");
		t5.setMaxCapacity(290.0);
		t5.prepare(map);
		trucks.add(t5);
		map.getTrucks().add(t5);
		
		Truck t6 = new Truck(6, "t6", "paper");
		t6.setMaxCapacity(300.0);
		t6.prepare(map);
		trucks.add(t6);
		map.getTrucks().add(t6);
		
		Planner planner = new Planner(map, trucks);
	}
}
