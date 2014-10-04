package ai;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Options {
	private boolean allTrucksStartingSamePosition;
	private boolean allowPlannerAgentToCreateTrucks;
	private boolean activeConsolePrinting;
	
	private String optionsFilePath = System.getProperty("user.dir") + "/config/options";
	private String optionsFile;
	
	public boolean isAllTrucksStartingSamePosition() {
		return allTrucksStartingSamePosition;
	}

	public void setAllTrucksStartingSamePosition(
			boolean allTrucksStartingSamePosition) {
		this.allTrucksStartingSamePosition = allTrucksStartingSamePosition;
	}

	public boolean isAllowPlannerAgentToCreateTrucks() {
		return allowPlannerAgentToCreateTrucks;
	}

	public void setAllowPlannerAgentToCreateTrucks(
			boolean allowPlannerAgentToCreateTrucks) {
		this.allowPlannerAgentToCreateTrucks = allowPlannerAgentToCreateTrucks;
	}

	public boolean isActiveConsolePrinting() {
		return activeConsolePrinting;
	}

	public void setActiveConsolePrinting(boolean activeConsolePrinting) {
		this.activeConsolePrinting = activeConsolePrinting;
	}
	
	public String getOptionsFilePath(){
		return this.optionsFilePath;
	}
	
	public void setOptionsFilePath(String filePath){
		this.optionsFilePath = filePath;
	}
	
	
	public String getOptionsFile() {
		return optionsFile;
	}

	public void setOptionsFile(String optionsFile) {
		this.optionsFile = optionsFile;
	}

	/*
	 * Export Options to XML file. 
	 */
	public void export(String filename) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		
		Element rootElement = doc.createElement("options");
		doc.appendChild(rootElement);
		
		Element trucksSamePos = doc.createElement("trucksSamePos");
		trucksSamePos.appendChild(doc.createTextNode(Boolean.toString(this.allTrucksStartingSamePosition)));
		rootElement.appendChild(trucksSamePos);
		
		Element allowPlannerAgentToCreateTrucks = doc.createElement("allowPlannerAgentToCreateTrucks");
		allowPlannerAgentToCreateTrucks.appendChild(doc.createTextNode(Boolean.toString(this.allowPlannerAgentToCreateTrucks)));
		rootElement.appendChild(allowPlannerAgentToCreateTrucks);
		
		Element activeConsolePrinting = doc.createElement("activeConsolePrinting");
		activeConsolePrinting.appendChild(doc.createTextNode(Boolean.toString(this.activeConsolePrinting)));
		rootElement.appendChild(activeConsolePrinting);
		
		File f = new File(this.optionsFilePath);
		f.setExecutable(true);
		f.setReadable(true);
		f.setWritable(true);
		File file = new File(f.toString() + "/" + filename);
	
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		
		transformer.transform(source, result);
	}
	
	/*
	 * Imports Options from a file.
	 */
	public void importOptions(String filename) throws ParserConfigurationException, SAXException, IOException{
		this.optionsFile = filename;
		
		File fXmlFile = new File(this.optionsFilePath + "/" + filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		
		doc.getDocumentElement().normalize();
		
		String trucksSamePos = doc.getElementsByTagName("trucksSamePos").item(0).getTextContent();
		this.allTrucksStartingSamePosition = Boolean.parseBoolean(trucksSamePos);
		
		String allowPlannerAgentToCreateTrucks = doc.getElementsByTagName("allowPlannerAgentToCreateTrucks").item(0).getTextContent();
		this.allowPlannerAgentToCreateTrucks = Boolean.parseBoolean(allowPlannerAgentToCreateTrucks);
		
		String activeConsolePrinting = doc.getElementsByTagName("activeConsolePrinting").item(0).getTextContent();
		this.activeConsolePrinting = Boolean.parseBoolean(activeConsolePrinting);
	}
	
	
	/*
	 * 
	 */
	public Options() {
		this.setAllTrucksStartingSamePosition(true);
		this.setAllowPlannerAgentToCreateTrucks(true);
		this.setActiveConsolePrinting(false);
	}
}
