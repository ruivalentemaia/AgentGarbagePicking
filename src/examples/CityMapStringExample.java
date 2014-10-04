package examples;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import map.CityMap;

public class CityMapStringExample {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException  {
		CityMap map = new CityMap(30, 50, 20, 40);
		map.printCityMapString();
	}
}
