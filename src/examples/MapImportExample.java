package examples;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import map.CityMap;

public class MapImportExample {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException  {
		CityMap map = new CityMap("randomMapTest.xml");
		map.printCityMapString();
	}
}
