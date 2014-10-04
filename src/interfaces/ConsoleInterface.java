package interfaces;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import map.CityMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import agent.Planner;
import agent.PlannerAgent;
import agent.Truck;
import ai.Options;

public class ConsoleInterface {
	
	private char lastOption;
	private List<Truck> trucks;
	private CityMap map;
	
	private String trucksFilePath = System.getProperty("user.dir") + "/config/trucks";
	
	public char getLastOption() {
		return lastOption;
	}


	public void setLastOption(char lastOption) {
		this.lastOption = lastOption;
	}

	
	public List<Truck> getTrucks() {
		return trucks;
	}


	public void setTrucks(List<Truck> trucks) {
		this.trucks = trucks;
	}


	public String getTrucksFilePath() {
		return trucksFilePath;
	}


	public void setTrucksFilePath(String trucksFilePath) {
		this.trucksFilePath = trucksFilePath;
	}

	public CityMap getMap() {
		return map;
	}


	public void setMap(CityMap map) {
		this.map = map;
	}


	/**
	 * 
	 */
	private void printMainMenu() {
		String cText = "";
		cText += "\nWelcome to the CityGarbagePicking simulator.\n";
		cText += "Choose one of the options below to configure the system or to run a simulation of this agent-based software.";
		cText += "\n\n";
		cText += "\n1) Start CityGarbagePicking";
		cText += "\n2) Choose the CityMap";
		cText += "\n3) Configure Trucks";
		cText += "\n4) Options";
		cText += "\n5) Exit";
		cText += "\n";
		cText += "\nChoose: ";
		
		System.out.println(cText);
	}
	
	
	/**
	 * 
	 * @param options
	 */
	private void printOptionsSubMenu(Options options) {
		String cText = "";
		cText += "\nSelect which Option you want to change: ";
		cText += "\n1) All Trucks Starting in the same position : " + Boolean.toString(options.isAllTrucksStartingSamePosition());
		cText += "\n2) Allow PlannerAgent to create Trucks : " + Boolean.toString(options.isAllowPlannerAgentToCreateTrucks());
		cText += "\n3) Active Console Printing : " + Boolean.toString(options.isActiveConsolePrinting());
		cText += "\n\n";
		cText += "\nBack to Previous Menu (type b or B).";
		cText += "\nSelect option to be changed: ";
		
		System.out.println(cText);
	}
	
	
	/**
	 * 
	 */
	private void printConfigureTruckSubMenu(){
		String cText = "";
		cText += "\nSelect your action:";
		cText += "\n1)Add Truck";
		cText += "\n2)Edit Truck information";
		cText += "\n3)Remove Truck";
		cText += "\n\n";
		cText += "\nBack to Previous Menu (type b or B).";
		cText += "\nSelect option: ";
		
		System.out.println(cText);
	}
	
	
	/**
	 * 
	 * @return
	 */
	private int buildMainMenu() {
		
		this.printMainMenu();
		
		Scanner in = new Scanner(System.in);
		int i = in.nextInt();
		if(i >= 1 && i <= 5)
			return i;
		else return -1;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private char buildOptionsSubMenu() throws ParserConfigurationException, SAXException, IOException {
		char option = ' ';
		
		Options options = new Options();
		options.importOptions("options.xml");
		this.printOptionsSubMenu(options);
		
		option = (char) System.in.read();
		
		return option;
	}

	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private char buildConfigureTruckSubMenu() throws IOException {
		char option = ' ';
		
		this.printConfigureTruckSubMenu();
		
		option = (char) System.in.read();
		
		return option;
		
	}
	
	
	/**
	 * 
	 * @param t
	 * @return
	 */
	private int searchTruckInList(Truck t){
		Iterator<Truck> truckIt = this.trucks.iterator();
		int counter = 0;
		while(truckIt.hasNext()){
			Truck t1 = truckIt.next();
			if(t1.getId() == t.getId()){
				return counter;
			}
			counter++;
		}
		return -1;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private Truck buildAddTruck() throws ParserConfigurationException, SAXException, IOException{
		
		int lastId = -1;
		if(this.trucks.size() > 0) lastId = this.trucks.size() + 1;
		else lastId = 1;
		
		Scanner in = new Scanner(new InputStreamReader(System.in));
		String cText = "";
		cText += "\n\n\nAdd new Truck:";
		cText += "\n";
		cText += "\nTruck Name: " + "t" + lastId;
		System.out.println(cText);
		cText = "";
		
		String tName = "";
		tName = in.nextLine();
		tName = "t" + lastId;
		
		cText = "\nGarbage Type: ";
		System.out.println(cText);
		cText = "";
		String tGType = in.nextLine();
		
		cText = "\nMaximum Capacity: ";
		System.out.println(cText);
		cText = "";
		double maxC = in.nextDouble();
		
		Truck t;
		if( (lastId != -1) && (tName != null) && (tGType != null) && (maxC > 0)){
			t = new Truck(lastId, tName, tGType);
			t.setMaxCapacity(maxC);
		}
		else {
			t = new Truck(1, "t1", "paper");
			t.setMaxCapacity(400);
		}
		
		return t;
		
	}
	
	
	/**
	 * 
	 * @return
	 */
	private int buildListTrucks() {
		String trucksInfo = "";
		trucksInfo += "\nTrucks List: ";
		trucksInfo += "\n\n";
		int counter = 1;
		Iterator<Truck> itTruck = this.trucks.iterator();
		while(itTruck.hasNext()){
			Truck t = itTruck.next();
			trucksInfo += "\n" + counter + ") " + t.getId() + ", " 
						 	+ t.getTruckName() + ", " + t.getGarbageType() 
						 	+ ", " + t.getMaxCapacity();
			counter++;
		}
		System.out.println(trucksInfo);
		
		trucksInfo = "";
		trucksInfo += "\nWhich Truck do you want to edit ? (insert the id - first field)";
		System.out.println(trucksInfo);
		
		Scanner in = new Scanner(System.in);
		int id = in.nextInt();
		
		if(id > 0 && id < counter)
			return id;
		else return -1;
	}
	
	
	/**
	 * 
	 * @param truckID
	 */
	private void buildEditField(int truckID){
		
		Iterator<Truck> truckIt = this.trucks.iterator();
		Truck toEdit = null;
		int field = -1;
		while(truckIt.hasNext()){
			Truck t = truckIt.next();
			if(t.getId() == truckID){
				toEdit = t;
			}
		}
		
		if(toEdit != null) {
			String truckInfo = "";
			truckInfo = "Information of the selected Truck: ";
			truckInfo += "\n\n";
			truckInfo += "\n1) Id = " + toEdit.getId();
			truckInfo += "\n2) Name = " + toEdit.getTruckName();
			truckInfo += "\n3) Garbage Type = " + toEdit.getGarbageType();
			truckInfo += "\n4) Max. Capacity = " + toEdit.getMaxCapacity();
			truckInfo += "\n\n";
			truckInfo += "\n(If you want to go back to the Main Menu, please insert a number higher than 4)";
			truckInfo += "\n\nWhich is the field you want to edit (insert the number): ";
			System.out.println(truckInfo);
			Scanner in = new Scanner(System.in);
			field = in.nextInt();
		}
		
		String truckInfo = "";
		if(field != -1){
			switch(field){
				case 1:
					truckInfo += "\n\n\nEdit Truck ID: ";
					truckInfo += "\n\n";
					truckInfo += "\nPrevious ID: " + toEdit.getId();
					truckInfo += "\nNew ID: ";
					System.out.println(truckInfo);
					Scanner in = new Scanner(System.in);
					int newID = in.nextInt();
					if(newID > 0) toEdit.setId(newID);
					else toEdit.setId(this.trucks.size());
					this.trucks.get(this.searchTruckInList(toEdit)).setId(toEdit.getId());;
					break;
				case 2:
					truckInfo += "\n\n\nEdit Truck name: ";
					truckInfo += "\n\n";
					truckInfo += "\nPrevious Name: " + toEdit.getTruckName();
					truckInfo += "\nNew Name: ";
					System.out.println(truckInfo);
					Scanner in2 = new Scanner(System.in);
					String newName = in2.nextLine();
					if(!newName.equals("")) toEdit.setTruckName(newName);
					else toEdit.setTruckName("t"+toEdit.getId());
					this.trucks.get(this.searchTruckInList(toEdit)).setTruckName(toEdit.getTruckName());;
					break;
				case 3:
					truckInfo += "\n\n\nEdit Garbage Type: ";
					truckInfo += "\n\n";
					truckInfo += "\nPrevious Garbage Type: " + toEdit.getGarbageType();
					truckInfo += "\nNew Garbage Type: ";
					System.out.println(truckInfo);
					Scanner in3 = new Scanner(System.in);
					String newGC = in3.nextLine();
					if(!newGC.equals("")) toEdit.setGarbageType(newGC);
					else toEdit.setGarbageType("undifferentiated");
					this.trucks.get(this.searchTruckInList(toEdit)).setGarbageType(toEdit.getGarbageType());;
					break;
				case 4:
					truckInfo += "\n\n\nEdit Maximum Capacity: ";
					truckInfo += "\n\n";
					truckInfo += "\nPrevious Max. Capacity: " + toEdit.getGarbageType();
					truckInfo += "\nNew Max. Capacity: ";
					System.out.println(truckInfo);
					Scanner in4 = new Scanner(System.in);
					double newMC = in4.nextDouble();
					if(newMC > 0) toEdit.setMaxCapacity(newMC);
					else toEdit.setMaxCapacity(1000);
					this.trucks.get(this.searchTruckInList(toEdit)).setMaxCapacity(toEdit.getMaxCapacity());
					break;
				default:
					break;
			}
		}
	
	}
	
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean buildRemoveTruck() throws IOException{
		String trucksInfo = "";
		trucksInfo += "\nTrucks List: ";
		trucksInfo += "\n\n";
		int counter = 1;
		Iterator<Truck> itTruck = this.trucks.iterator();
		while(itTruck.hasNext()){
			Truck t = itTruck.next();
			trucksInfo += "\n" + counter + ") " + t.getId() + ", " 
						 	+ t.getTruckName() + ", " + t.getGarbageType() 
						 	+ ", " + t.getMaxCapacity();
			counter++;
		}
		System.out.println(trucksInfo);
		
		trucksInfo = "";
		trucksInfo += "\nWhich Truck do you want to remove ? (insert the id - first field)";
		System.out.println(trucksInfo);
		
		Scanner in = new Scanner(System.in);
		int id = in.nextInt();
		
		trucksInfo = "";
		if(id > 0 && id < counter) {
			trucksInfo = "\n\n\nAre you sure you want to remove Truck " + id + " ? (y/n)";
			System.out.println(trucksInfo);
			char confirm = (char) System.in.read();
			if(confirm == 'y') {
				itTruck = this.trucks.iterator();
				while(itTruck.hasNext()) {
					Truck t = itTruck.next();
					if(t.getId() == id){
						itTruck.remove();
						System.out.println("\nTruck " + t.getId() + " successfully removed.");
						break;
					}
				}
			}
			return true;
		}
		return false;
	}
	
	
	/*
	 * 
	 */
	private char buildChooseCityMapSubMenu() {
		char option = ' ';
		
		String cText = "";
		cText += "\nYou can load a CityMap from a file or generate a new one.";
		cText += "\n\n1)Load from file.";
		cText += "\n2)Generate new CityMap.";
		cText += "\n\nWhich action do you want to do ?";
		cText += "\nPress b to go back.";
		System.out.println(cText);
		Scanner in = new Scanner(System.in);
		option = (char) in.next().charAt(0);
		
		return option;
	}
	
	/**
	 * 
	 * @param fileName
	 */
	private String buildChooseCityMap() {
		String cText = "";
		cText += "\nChoose the CityMap (by its name): ";
		System.out.println(cText);
		this.listFiles(new File(System.getProperty("user.dir") + "/config/maps"));
		cText = "\n\n";
		cText += "\nInsert the name of the map file: ";
		System.out.println(cText);
		Scanner in = new Scanner(System.in);
		String name = in.nextLine();
		if(name != null) {
			File f = new File(System.getProperty("user.dir") + "/config/maps");
			if(this.findFile(name, f))
				return name;
		}
		return "";
	}
	
	/*
	 * 
	 */
	private void listFiles(File file) {
		 File[] list = file.listFiles();
		 int counter = 1;
	     if(list!=null)
	     for (File fil : list){
	       System.out.println(counter + ") " + fil.getName());
	       counter++;
	     }
	}
	
	/**
	 * 
	 * @param name
	 * @param file
	 */
	private boolean findFile(String name,File file) {
        File[] list = file.listFiles();
        if(list!=null)
        for (File fil : list){
            if (fil.isDirectory())
                findFile(name,fil);
            else if (name.equalsIgnoreCase(fil.getName())) {
            	System.out.println("\nFound in " + fil.getParentFile() + "/" + name);
            	return true;
            }
        }
        return false;
    }
	
	/**
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IOException
	 * @throws SAXException 
	 */
	private boolean generateCityMap() throws ParserConfigurationException, TransformerException, IOException, SAXException {
		boolean done = false;
		String cText = "";
		cText += "\n\nGenerate a new City Map. Insert width and height:";
		cText += "\n\nMin. Width = ";
		System.out.println(cText);
		Scanner in = new Scanner (System.in);
		int minWidth = in.nextInt();
		
		
		cText = "";
		cText += "\nMax. Width = ";
		System.out.println(cText);
		in = new Scanner(System.in);
		int maxWidth = in.nextInt();
		
		cText = "";
		cText += "\nMin. Height = ";
		System.out.println(cText);
		in = new Scanner (System.in);
		int minHeight = in.nextInt();
		
		cText = "";
		cText = "\nMax. Height = ";
		System.out.println(cText);
		in = new Scanner(System.in);
		int maxHeight = in.nextInt();
		
		if( (minWidth > 0) && (maxWidth > 0) && (minHeight > 0) && (maxHeight > 0)) {
			done = true;
			this.map = new CityMap(minWidth, maxWidth, minHeight, maxHeight);
			
			cText = "";
			cText += "\n\nSave new generated map as: ";
			System.out.println(cText);
			in = new Scanner(System.in);
			String filename = in.nextLine();
			this.map.exportMapToXML(filename);
		}
		
		return done;
	}
	
	
	private void start() throws ParserConfigurationException, SAXException, IOException, ControllerException {
		if( (this.trucks.size() > 0) && (this.map != null) ) {
			
			this.map.printCityMapString();
			
			Iterator<Truck> itTruck = this.trucks.iterator();
			while(itTruck.hasNext()){
				Truck t = itTruck.next();
				t.prepare(this.map);
				this.map.getTrucks().add(t);
			}
			
			Planner planner = new Planner(this.map, this.trucks);
			
			//creates JADE AgentContainer.
			Profile p = new ProfileImpl();
			p.setParameter(Profile.MAIN_PORT, "8888");
			Runtime rt = Runtime.instance();
			AgentContainer ac = rt.createMainContainer(p);
			
			//adds PlannerAgent to the AgentContainer
			try {
				Object[] args = new Object[1];
				args[0] = planner;
				AgentController aController = ac.createNewAgent("planner", agent.PlannerAgent.class.getName(), args);
				aController.start();
			} catch(jade.wrapper.StaleProxyException e) {
				System.err.println("Error launching " + planner.getClass().getName());
			}
			
			//adds all Trucks to the AgentContainer
			itTruck = this.trucks.iterator();
			while(itTruck.hasNext()){
				Truck t = itTruck.next();
				try {
					Object[] args = new Object[1];
					args[0] = t;
					AgentController aController = ac.createNewAgent(t.getTruckName(), agent.TruckAgent.class.getName(), args);
					aController.start();
				} catch (StaleProxyException e) {
					System.err.println("Error launching " + agent.TruckAgent.class.getName());
				}
			}
			
		}
		else return;
	}
	
	
	/**
	 * 
	 * @param input
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	private boolean treatOptionsSubMenuInput(char input) throws ParserConfigurationException, SAXException, IOException, TransformerException{
		Options options = new Options();
		options.importOptions("options.xml");
		boolean done = false;
		
		switch(input){
			//All Trucks Starting in the same position
			case '1':
				if(options.isAllTrucksStartingSamePosition())
					options.setAllTrucksStartingSamePosition(false);
				else options.setAllTrucksStartingSamePosition(true);
				options.export("options.xml");
				break;
			
			//Allow PlannerAgent to create Trucks
			case '2':
				if(options.isAllowPlannerAgentToCreateTrucks())
					options.setAllowPlannerAgentToCreateTrucks(false);
				else options.setAllowPlannerAgentToCreateTrucks(true);
				options.export("options.xml");
				break;
				
			//Active Console Printing
			case '3':
				if(options.isActiveConsolePrinting())
					options.setActiveConsolePrinting(false);
				else options.setActiveConsolePrinting(true);
				options.export("options.xml");
				break;
			
			case 'b':
				done = true;
				break;
			
			case 'B':
				done = true;
				break;
			
			//Other input.
			default:
				System.out.println("\nPlease select a valid option.");
				break;
		}
		return done;
	}
	
	
	/**
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws SAXException 
	 */
	private boolean treatConfigureTruckSubMenu(char input) throws IOException, ParserConfigurationException, TransformerException, SAXException {
		boolean done = false;
		
		switch(input){
			//Add Truck.
			case '1':
				Truck t = this.buildAddTruck();
				this.trucks.add(t);
				done = true;
				break;
			
			//Edit Truck information.
			case '2':
				int truckToEdit = this.buildListTrucks();
				if(truckToEdit != -1){
					this.buildEditField(truckToEdit);
				}
				done = true;
				break;
				
			//Remove Truck.
			case '3':
				done = this.buildRemoveTruck();
				this.exportTrucks("trucks.xml");
				if(done) break;
			
			case 'b':
				done = true;
				break;
			
			case 'B':
				done = true;
				break;
				
			default: 
				break;
		}
		
		return done;
	}
	
	
	private boolean treatChooseCityMapSubMenu(char optionSelected) throws ParserConfigurationException, SAXException, IOException, TransformerException{
		boolean done = false;
		
		switch(optionSelected) {
			//Load
			case '1':
				String mapName = this.buildChooseCityMap();
				if(mapName != "") this.map = new CityMap(mapName);
				break;
			
			//Generate
			case '2':
				done = this.generateCityMap();
				if(done) break;
				break;
		
			case 'b':
				done = true;
				break;
		
			case 'B':
				done = true;
				break;
			
			default:
				break;
		}
		
		return done;
	}
	
	
	/**
	 * 
	 * @param mainMenuOption
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 * @throws ControllerException 
	 */
	private int treatMainMenuSelectedOption(int mainMenuOption) throws IOException, ParserConfigurationException, SAXException, TransformerException, ControllerException{
			switch(mainMenuOption){
			
			//start
			case 1:
				this.lastOption = '1';
				this.start();
				break;
		
			//choose the CityMap
			case 2:
				this.lastOption = '2';
				char optionSelectedChooseCityMap = this.buildChooseCityMapSubMenu();
				boolean doneCityMap = this.treatChooseCityMapSubMenu(optionSelectedChooseCityMap);
				if(doneCityMap) break;
				break;
			//configure Trucks
			case 3:
				this.lastOption = '3';
				this.importTrucks("trucks.xml");
				char optionSelectedTruck = this.buildConfigureTruckSubMenu();
				boolean doneTruck = this.treatConfigureTruckSubMenu(optionSelectedTruck);
				if(doneTruck) break;
				break;
			
			//Options
			case 4:
				this.lastOption = '4';
				char optionSelectedOptions = this.buildOptionsSubMenu();
				boolean doneOptions = this.treatOptionsSubMenuInput(optionSelectedOptions);
				if(doneOptions) break;
				
			//Exit
			case 5:
				this.exportTrucks("trucks.xml");
				this.lastOption = '5';
				break;
				
			//other input
			default:
				this.lastOption = Integer.toString(mainMenuOption).charAt(0);
				System.out.println("\n\nPlease select a valid option.");
				mainMenuOption = 5;
				break;
		}
		
		return mainMenuOption;
	}
	
	
	/*
	 * 
	 * 
	 * 
	 * 	
	 * 
	 * 
	 * 
	 * 		XML EXPORT/IMPORT
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	/**
	 * 
	 * @param filename
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public void exportTrucks(String filename) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		
		Element rootElement = doc.createElement("trucks");
		doc.appendChild(rootElement);
		
		Iterator<Truck> itTruck = this.trucks.iterator();
		while(itTruck.hasNext()){
			Truck t = itTruck.next();
			Element truck = doc.createElement("truck");
			
			Element id = doc.createElement("id");
			id.appendChild(doc.createTextNode(Integer.toString(t.getId())));
			truck.appendChild(id);
			
			Element name = doc.createElement("name");
			name.appendChild(doc.createTextNode(t.getTruckName()));
			truck.appendChild(name);
			
			Element gcType = doc.createElement("garbageType");
			gcType.appendChild(doc.createTextNode(t.getGarbageType()));
			truck.appendChild(gcType);
			
			Element maxCapacity = doc.createElement("maxCapacity");
			maxCapacity.appendChild(doc.createTextNode(Double.toString(t.getMaxCapacity())));
			truck.appendChild(maxCapacity);
			
			rootElement.appendChild(truck);
		}
		
		File f = new File(this.trucksFilePath);
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
	
	/**
	 * 
	 * @param filename
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void importTrucks(String filename) throws ParserConfigurationException, SAXException, IOException{
		File fXmlFile = new File(this.trucksFilePath + "/" + filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		
		doc.getDocumentElement().normalize();
		
		NodeList nList = doc.getElementsByTagName("truck");
		
		for(int temp = 0; temp < nList.getLength(); temp++){
			Node nNode = nList.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				
				int id = Integer.parseInt(eElement.getElementsByTagName("id").item(0).getTextContent());
				String name = eElement.getElementsByTagName("name").item(0).getTextContent();
				String gcType = eElement.getElementsByTagName("garbageType").item(0).getTextContent();
				double maxCapacity = Double.parseDouble(eElement.getElementsByTagName("maxCapacity").item(0).getTextContent());
			
				Truck t = new Truck(id, name, gcType);
				t.setMaxCapacity(maxCapacity);
				
				int pos = this.searchTruckInList(t);
				if(pos == -1) this.trucks.add(t);
			}
		}
	}
	
	
	/**
	 * 
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws ControllerException 
	 */
	public ConsoleInterface() throws IOException, ParserConfigurationException, TransformerException, SAXException, ControllerException {
		Options options = new Options();
		options.export("options.xml");
		
		this.trucks = new ArrayList<Truck>();
		
		int mainMenuOption = 0;
		
		do {
			mainMenuOption = this.buildMainMenu();
			mainMenuOption = this.treatMainMenuSelectedOption(mainMenuOption);
			if(mainMenuOption >= 1 && mainMenuOption <= 4)
				mainMenuOption = this.treatMainMenuSelectedOption(mainMenuOption);
		} while(mainMenuOption != 5);
	}
}
