package examples;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import map.CityMap;

public class MapExportExample {
	public static void main(String[] args)  {
		CityMap map = new CityMap(40, 80, 30, 50);
		map.printCityMapString();
		try {
			map.exportMapToXML("randomMapTest.xml");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
