package examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import map.CityMap;
import map.GarbageContainer;

import org.xml.sax.SAXException;

import units.Truck;
import ai.TransportationAlgorithm;

public class AssignmentTest {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException  {
		
		//CityMap map = new CityMap(25,40, 20, 30);
		//map.exportMapToXML("mapTest25402030.xml");
		
		//Imports map from xml file.
		CityMap map = new CityMap("mapTest25402030.xml");
		map.printCityMapString();
		//map.getGarbageContainers().get(1).setType("undifferentiated");
		//map.getGarbageContainers().get(2).setType("glass");
		
		//Creates one Truck, allocates to it the CityMap and builds its goals list.
		List<Truck> trucks = new ArrayList<Truck>();
		Truck t1 = new Truck(1, "t1", "undifferentiated");
		t1.setMaxCapacity(330.0);
		t1.prepare(map);
		trucks.add(t1);
		map.getTrucks().add(t1);
		
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
		
		map.setTrucks(trucks);
		
		List<Truck> containerTrucks = map.selectTruckByGarbageType("container");
		List<GarbageContainer> containersGC = map.selectGarbageContainersByType("container");
		
		TransportationAlgorithm transportation = new TransportationAlgorithm(containerTrucks, containersGC);
		transportation.performTransportationAlgorithm();
	}
}
