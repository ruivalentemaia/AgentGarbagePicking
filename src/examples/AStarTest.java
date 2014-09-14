package examples;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import agent.Truck;
import ai.AStar;
import map.CityMap;
import map.Point;

public class AStarTest {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException  {
		//Exports map to an xml file.
		//CityMap map = new CityMap(25,30,20,25);
		//map.exportMapToXML("randomMapTest25302025");
		
		//Imports map from xml file.
		CityMap map = new CityMap("randomMapTest25302025.xml");
		map.getGarbageContainers().get(1).setType("undifferentiated");
		
		//Creates one Truck, allocates to it the CityMap and builds its goals list.
		Truck t1 = new Truck(1, "t1", "undifferentiated");
		t1.setCompleteCityMap(map);
		
		Point startingPoint = t1.selectStartingPoint();
		t1.setStartPosition(startingPoint);
		t1.setCurrentPosition(startingPoint);
		
		map.printCityMapString();
		t1.buildGoalsList();
		
		//
		t1.doAStar();
		
	}
}
