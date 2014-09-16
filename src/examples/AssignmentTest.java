package examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import map.CityMap;

import org.xml.sax.SAXException;

import agent.Truck;
import ai.Assignment;

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
		Truck t1 = new Truck(1, "t1", "undifferentiated");
		t1.prepare(map);
		
		Truck t2 = new Truck(2, "t2", "glass");
		t2.prepare(map);
		
		Truck t3 = new Truck(3, "t3", "container");
		t3.prepare(map);
		
		Truck t4 = new Truck(4, "t4", "container");
		t4.prepare(map);
		
		List<Truck> trucks = new ArrayList<Truck>();
		trucks.add(t1);
		trucks.add(t2);
		trucks.add(t3);
		trucks.add(t4);
		
		Assignment schedule = new Assignment(trucks);
		schedule.computeHungarianMethod();
		
	}
}
