package map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import agent.Truck;

public class CityMap {
	private int id;
	private String name;
	private int width;
	private int height;
	private List<Point> points;
	private List<Road> roads;
	private List<Crossroads> crossroads;
	private List<GarbageContainer> garbageContainers;
	
	//Trucks
	private List<Truck> trucks;
	
	//XML file attributes
	private String filePath = System.getProperty("user.dir") + "/maps";
	
	//Comparators to order Crossroads
	static private Comparator<Crossroads> orderCrossroadsWidthDesc;
	static private Comparator<Crossroads> orderCrossroadsWidthAsc;
	static private Comparator<Crossroads> orderCrossroadsHeightDesc;
	static private Comparator<Crossroads> orderCrossroadsHeightAsc;
	
	static {
		orderCrossroadsWidthDesc = new Comparator<Crossroads>() {
			@Override
			public int compare(Crossroads o1, Crossroads o2) {
				if(o1.getCenter().getX() == o2.getCenter().getX())
					return 0;
				else if(o1.getCenter().getX() > o2.getCenter().getX())
					return -1;
				else return 1;
			}
		};
		
		orderCrossroadsWidthAsc = new Comparator<Crossroads>() {
			@Override
			public int compare(Crossroads o1, Crossroads o2) {
				if(o1.getCenter().getX() == o2.getCenter().getX())
					return 0;
				else if(o1.getCenter().getX() > o2.getCenter().getX())
					return 1;
				else return -1;
			}
		};
		
		orderCrossroadsHeightDesc = new Comparator<Crossroads>() {
			@Override
			public int compare(Crossroads o1, Crossroads o2) {
				if(o1.getCenter().getY() == o2.getCenter().getY())
					return 0;
				else if(o1.getCenter().getY() > o2.getCenter().getY())
					return -1;
				else return 1;
			}
		};
		
		orderCrossroadsHeightAsc = new Comparator<Crossroads>() {
			@Override
			public int compare(Crossroads o1, Crossroads o2) {
				if(o1.getCenter().getY() == o2.getCenter().getY())
					return 0;
				else if(o1.getCenter().getY() > o2.getCenter().getY())
					return 1;
				else return -1;
			}
		};
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}

	public List<Road> getRoads() {
		return roads;
	}

	public void setRoads(List<Road> roads) {
		this.roads = roads;
	}

	public List<Crossroads> getCrossroads() {
		return crossroads;
	}

	public void setCrossroads(List<Crossroads> crossroads) {
		this.crossroads = crossroads;
	}

	public List<GarbageContainer> getGarbageContainers() {
		return garbageContainers;
	}

	public void setGarbageContainers(List<GarbageContainer> garbageContainers) {
		this.garbageContainers = garbageContainers;
	}
	
	public List<Truck> getTrucks() {
		return trucks;
	}

	public void setTrucks(List<Truck> trucks) {
		this.trucks = trucks;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/*
	 * Orders an array of Crossroads by each Crossroads' width
	 * in descending order (higher width first).
	 */
	public void orderCrossroadsWidthDesc(Crossroads[] crossroads){
		Arrays.sort(crossroads, orderCrossroadsWidthDesc);
	}
	
	
	/*
	 * Orders an array of Crossroads by each Crossroads' width
	 * in ascending order (lower width first).
	 */
	public void orderCrossroadsWidthAsc(Crossroads[] crossroads) {
		Arrays.sort(crossroads, orderCrossroadsWidthAsc);
	}
	
	
	/*
	 * Orders an array of Crossroads by Crossroads' height
	 * in descending order (higher height first).
	 */
	public void orderCrossroadsHeightDesc(Crossroads[] crossroads){
		Arrays.sort(crossroads, orderCrossroadsHeightDesc);
	}
	
	
	/*
	 * Orders an array of Crossroads by Crossroads' height
	 * in ascending order (lower height first).
	 */
	public void orderCrossroadsHeightAsc(Crossroads[] crossroads) {
		Arrays.sort(crossroads, orderCrossroadsHeightAsc);
	}
	
	
	/*
	 * Sorts the points List by ascending x value.
	 */
	private void sortPointsByAscendingX(){
		Collections.sort(this.points, new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return o1.getX() - o2.getX();
			}
		});
	}
	
	
	/*
	 * Converts an ArrayList of Crossroads to an array of Crossroads.
	 */
	private Crossroads[] convertArrayListCrossroadsToArray(List<Crossroads> crossroads) {
		Crossroads[] crossroadsArray = new Crossroads[crossroads.size()];
		return crossroads.toArray(crossroadsArray);
	}
	
	
	/*
	 * Converts an array of Crossroads to an ArrayList of the same type.
	 */
	private List<Crossroads> convertArrayCrossroadsToArrayList(Crossroads[] crossroads) {
		List<Crossroads> crossroadsList = new ArrayList<Crossroads>();
		for(int i = 0; i < crossroads.length; i++) {
			crossroadsList.add(crossroads[i]);
		}
		return crossroadsList;
	}
	
	
	/*
	 * Removes the duplicates from the Crossroads ArrayList.
	 */
	private void removeCrossroadsDuplicates() {
		ArrayList<Crossroads> newCrossroads = new ArrayList<Crossroads>();
		HashSet<Crossroads> hs = new HashSet<Crossroads>();
		hs.addAll(this.crossroads);
		newCrossroads.addAll(hs);
		this.crossroads.clear();
		this.crossroads.addAll(newCrossroads);
	}
	
	
	/*
	 * Gets the Road to which a given Point parameter's object belongs to.
	 */
	public Road selectRoadFromPoint(Point p){
		Iterator<Road> itRoad = this.roads.iterator();
		while(itRoad.hasNext()) {
			Road r = itRoad.next();
			List<Point> roadPoints = new ArrayList<Point>();
			roadPoints.addAll(r.getPoints());
			Iterator<Point> roadPointsIt = roadPoints.iterator();
			while(roadPointsIt.hasNext()){
				Point n = roadPointsIt.next();
				if( (n.getX() == p.getX()) && (n.getY() == p.getY())){
					return r;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Gets the Road to which a GarbageContainer is closest to.
	 */
	public Road selectRoadFromGarbageContainer(Point gcPosition){
		Iterator<Road> itRoad = this.roads.iterator();
		while(itRoad.hasNext()) {
			Road r = itRoad.next();
			List<Point> roadPoints = new ArrayList<Point>();
			roadPoints.addAll(r.getPoints());
			Iterator<Point> roadPointsIt = roadPoints.iterator();
			while(roadPointsIt.hasNext()){
				Point n = roadPointsIt.next();
				
				//checks if the gc is on the left.
				if( ( (gcPosition.getX() + 1) == n.getX()) && (gcPosition.getY() == n.getY())){
					return r;
				}
				
				//checks if the gc is on the right of this road point.
				else if( ( (gcPosition.getX() - 1) == n.getX()) && (gcPosition.getY() == n.getY())) {
					return r;
				}
				
				//checks if the gc is down of this road point.
				else if( (gcPosition.getX() == n.getX()) && ((gcPosition.getY() + 1) == n.getY())) {
					return r;
				}
				
				//checks if the gc is up of this road point.
				else if( (gcPosition.getX() == n.getX()) && ((gcPosition.getY() - 1) == n.getY())) {
					return r;
				}
			}
		}
		return null;
	}
	
	
	/*
	 * Gets the point of a computed Road to which a GarbageContainer parameter
	 * is closest to.
	 */
	public Point selectPointFromRoad(Road r, Point gcPosition) {
		Iterator<Point> itPoints = r.getPoints().iterator();
		while(itPoints.hasNext()){
			Point n = itPoints.next();
			
			//checks if the gc is on the left.
			if( ( (gcPosition.getX() + 1) == n.getX()) && (gcPosition.getY() == n.getY())){
				return n;
			}
			
			//checks if the gc is on the right of this road point.
			else if( ( (gcPosition.getX() - 1) == n.getX()) && (gcPosition.getY() == n.getY())) {
				return n;
			}
			
			//checks if the gc is down of this road point.
			else if( (gcPosition.getX() == n.getX()) && ((gcPosition.getY() + 1) == n.getY())) {
				return n;
			}
			
			//checks if the gc is up of this road point.
			else if( (gcPosition.getX() == n.getX()) && ((gcPosition.getY() - 1) == n.getY())) {
				return n;
			}
		}
		return null;
	}
	
	
	/*
	 * Selects the neighbours of the parameter Point p and
	 * returns them in a list.
	 */
	public List<Point> selectNeighbourPoints(Point p){
		List<Point> neighbours = new ArrayList<Point>();
		String pType = p.getType();
		
		if(pType.equals("CROSSROADS")){
			
			//left neighbour
			int xNeighbour = p.getX() - 1;
			int yNeighbour = p.getY();
			String neighbourType = "ROAD";
			Point neighbourLeft = new Point(xNeighbour, yNeighbour);
			neighbourLeft.setType(neighbourType);
			neighbours.add(neighbourLeft);
			
			//up neighbour
			xNeighbour = p.getX();
			yNeighbour = p.getY() - 1;
			Point neighbourUp = new Point(xNeighbour, yNeighbour);
			neighbourUp.setType(neighbourType);
			neighbours.add(neighbourUp);
			
			//right neighbour
			xNeighbour = p.getX() + 1;
			yNeighbour = p.getY();
			Point neighbourRight = new Point(xNeighbour, yNeighbour);
			neighbourRight.setType(neighbourType);
			neighbours.add(neighbourRight);
			
			//down neighbour
			xNeighbour = p.getX();
			yNeighbour = p.getY() + 1;
			Point neighbourDown = new Point(xNeighbour, yNeighbour);
			neighbourDown.setType(neighbourType);
			neighbours.add(neighbourDown);
			
		}
		else if(pType.equals("ROAD")){
			Road r = this.selectRoadFromPoint(p);
			String direction = r.getDirection();
			
			if(direction.equals("both")){
				Point rightTestPoint = new Point(p.getX() + 1, p.getY());
				Point leftTestPoint = new Point(p.getX() - 1, p.getY());
				Point upTestPoint = new Point(p.getX(), p.getY() - 1);
				Point downTestPoint = new Point(p.getX(), p.getY() + 1);
				
				Iterator<Point> itPoints = this.points.iterator();
				while(itPoints.hasNext()){
					Point t = itPoints.next();
					if( (t.getX() == rightTestPoint.getX()) && (t.getY() == rightTestPoint.getY()) ){
						if( (t.getType().equals("ROAD")) || (t.getType().equals("CROSSROADS"))){
							neighbours.add(t);
						}
					}
					else if( (t.getX() == leftTestPoint.getX()) && (t.getY() == leftTestPoint.getY()) ){
						if( (t.getType().equals("ROAD")) || (t.getType().equals("CROSSROADS"))){
							neighbours.add(t);
						}
					}
					else if( (t.getX() == upTestPoint.getX()) && (t.getY() == upTestPoint.getY()) ){
						if( (t.getType().equals("ROAD")) || (t.getType().equals("CROSSROADS"))){
							neighbours.add(t);
						}
					}
					else if( (t.getX() == downTestPoint.getX()) && (t.getY() == downTestPoint.getY()) ){
						if( (t.getType().equals("ROAD")) || (t.getType().equals("CROSSROADS"))){
							neighbours.add(t);
						}
					}
				}
			}
			//to the right or up.
			else if(direction.equals("->")){
				Point rightTestPoint = new Point(p.getX() + 1, p.getY());
				Point upTestPoint = new Point(p.getX(), p.getY() - 1);
				Iterator<Point> itPoints = this.points.iterator();
				while(itPoints.hasNext()){
					Point t = itPoints.next();
					if( (t.getX() == rightTestPoint.getX()) && (t.getY() == rightTestPoint.getY()) ){
						if( (t.getType().equals("ROAD")) || (t.getType().equals("CROSSROADS"))){
							neighbours.add(t);
						}
					}
					else if( (t.getX() == upTestPoint.getX()) && (t.getY() == upTestPoint.getY()) ){
						if( (t.getType().equals("ROAD")) || (t.getType().equals("CROSSROADS"))){
							neighbours.add(t);
						}
					}
				}
			}
			//to the left or down.
			else if(direction.equals("<-")){
				Point leftTestPoint = new Point(p.getX() - 1, p.getY());
				Point downTestPoint = new Point(p.getX(), p.getY() + 1);
				Iterator<Point> itPoints = this.points.iterator();
				while(itPoints.hasNext()){
					Point t = itPoints.next();
					if( (t.getX() == leftTestPoint.getX()) && (t.getY() == leftTestPoint.getY()) ){
						if( (t.getType().equals("ROAD")) || (t.getType().equals("CROSSROADS"))){
							neighbours.add(t);
						}
					}
					else if( (t.getX() == downTestPoint.getX()) && (t.getY() == downTestPoint.getY()) ){
						if( (t.getType().equals("ROAD")) || (t.getType().equals("CROSSROADS"))){
							neighbours.add(t);
						}
					}
				}
			}
		}
		return neighbours;
	}
	
	
	/*
	 * 
	 * 
	 * 		CROSSROADS GENERATION
	 * 
	 * 
	 */
	
	/*
	 * Returns a randomly generated center Point to be used
	 * in the constructor of a Crossroads object.
	 */
	private Point randomizeCrossroadsCenter(int distanceToKeep) {
		int x = 0, y = 0;
		Random rX = new Random();
		Random rY = new Random();
		x = rX.nextInt(this.width - distanceToKeep) + distanceToKeep;
		y = rY.nextInt(this.height - distanceToKeep) + distanceToKeep;
		return new Point(x,y);
	}
	
	
	/*
	 * Checks if it's possible to add crossroads to the upper border.
	 * If it is, then it'll keep adding crossroads up till the minimum
	 * distanceToKeep is passed.
	 */
	private int generateCrossroadsUp(Crossroads c, int distanceToKeep, int minRoadLength){
		int counter = 0;
		int currentHeight = c.getCenter().getY();
		int emptySpace = currentHeight - distanceToKeep;
		while( (emptySpace - minRoadLength - distanceToKeep) > 0) {
			Crossroads cNew = new Crossroads(c.getId() + 1, 
									new Point(c.getCenter().getX(), 
										currentHeight - minRoadLength - 1));
			
			this.crossroads.add(cNew);
			currentHeight = cNew.getCenter().getY();
			emptySpace = currentHeight - distanceToKeep;
			counter++;
		}
		return counter;
	}
	
	
	/*
	 * Checks if it's possible to add crossroads to the lower border.
	 * If it is, then it'll keep adding crossroads down till the minimum
	 * distanceToKeep is passed.
	 */
	private int generateCrossroadsDown(Crossroads c, int distanceToKeep, int minRoadLength){
		int counter = 0;
		int currentHeight = c.getCenter().getY();
		int emptySpace = (this.getHeight() - currentHeight) - distanceToKeep;
		while( (emptySpace - minRoadLength-distanceToKeep) > 0) {
			Crossroads cNew = new Crossroads(c.getId() + 1, 
								new Point(c.getCenter().getX(), 
										currentHeight + minRoadLength + 1));
			
			this.crossroads.add(cNew);
			currentHeight = cNew.getCenter().getY();
			emptySpace = (this.getHeight() - currentHeight) - distanceToKeep;
		}
		return counter;
	}
	
	
	/*
	 * Generates all possible Crossroads to the right (until the end of the right side of the map)
	 * for each one of the previously generated crossroads, allocated in tempCrossroads
	 * ArrayList.
	 */
	private int generateCrossroadsRight(List<Crossroads> tempCrossroads, int distanceToKeep, int minRoadLength) {
		int currentWidth = 0;
		int emptySpace = 0;
		int currentId = 0;
		int counter = 0;
		Iterator<Crossroads> it = tempCrossroads.iterator();
		while(it.hasNext()){
			Crossroads c = it.next();
			currentWidth = c.getCenter().getX();
			emptySpace = (this.getWidth() - currentWidth) - distanceToKeep;
			currentId = tempCrossroads.size();
			while( (emptySpace - minRoadLength) > 0){
				Crossroads cNew = new Crossroads(currentId++,
									new Point(currentWidth + minRoadLength + 1,
										c.getCenter().getY()));
				this.crossroads.add(cNew);
				currentWidth = cNew.getCenter().getX();
				emptySpace = (this.getWidth() - currentWidth) - distanceToKeep;
				counter++;
			}
		}
		return counter;
	}
	
	
	/*
	 * Generates all possible Crossroads to the left (until the end of the left side of the map)
	 * for each one of the previously generated crossroads, allocated in tempCrossroads
	 * ArrayList.
	 */
	private int generateCrossroadsLeft(List<Crossroads> tempCrossroads, int distanceToKeep, int minRoadLength) {
		int currentWidth = 0;
		int emptySpace = 0;
		int currentId = 0;
		int counter = 0;
		Iterator<Crossroads> it = tempCrossroads.iterator();
		while(it.hasNext()){
			Crossroads c = it.next();
			currentWidth = c.getCenter().getX();
			emptySpace = currentWidth - distanceToKeep;
			currentId = tempCrossroads.size();
			while( (emptySpace - minRoadLength) > 0){
				Crossroads cNew = new Crossroads(currentId++,
									new Point(currentWidth - minRoadLength - 1,
										c.getCenter().getY()));
				this.crossroads.add(cNew);
				currentWidth = cNew.getCenter().getX();
				emptySpace = currentWidth - distanceToKeep;
				counter++;
			}
		}
		return counter;
	}
	
	
	/*
	 * From the information of the Crossroads passed as parameter,
	 * it creates other Crossroads. The Crossroads are created until
	 * the stopping rule (remaining distance to the borders of the map)
	 * is fulfilled.
	 */
	private void createRemainingCrossroads(Crossroads first, int distanceToKeep, int maxRoadLength) {
		Random rMinDistance = new Random();
		List<Crossroads> tempCrossroads = new ArrayList<Crossroads>();
		int minRoadLength = rMinDistance.nextInt( (this.height/5) - 2) + 2;
		
		//generates all possible Crossroads up and down first.
		this.generateCrossroadsUp(first, distanceToKeep, minRoadLength);
		this.generateCrossroadsDown(first, distanceToKeep, minRoadLength);
		
		//copies all the current Crossroads to the tempCrossroads ArrayList.
		for(Crossroads c : this.crossroads){
			tempCrossroads.add(c);
		}
		
		/*
		 * generates all possible Crossroads for the right and left
		 * for each one of the Crossroads in the tempCrossroads ArrayList.
		 */
		this.generateCrossroadsRight(tempCrossroads, distanceToKeep, minRoadLength);
		this.generateCrossroadsLeft(tempCrossroads, distanceToKeep, minRoadLength);
	}
	
	
	
	/*
	 * 
	 * 
	 * 		ROADS GENERATION
	 * 
	 * 
	 */
	
	
	/*
	 * Builds an array of Crossroads where all the Crossroads have the same
	 * height in the map, where height is passed as parameter to the method.
	 */
	private Crossroads[] retrieveCrossroadsWithSameHeight(Crossroads[] tempCross, int height) {
		
		//counts elements with that same height first.
		int counter = 0;
		for(int i = 0; i < tempCross.length; i++){
			if(tempCross[i].getCenter().getY() == height)
				counter++;
		}
		
		//then declares the array
		Crossroads[] crossroads = new Crossroads[counter];
		
		//then fills it with the objects with the height parameter.
		counter = 0;
		for(int i = 0; i < tempCross.length; i++){
			if(tempCross[i].getCenter().getY() == height) {
				crossroads[counter] = tempCross[i];
				counter++;
			}
			else continue;
		}
		
		return crossroads;
	}
	
	
	/*
	 * Builds an array of Crossroads where all the Crossroads have the same
	 * width in the map, where width is passed as parameter to the method.
	 */
	private Crossroads[] retrieveCrossroadsWithSameWidth(Crossroads[] tempCross, int width) {
		//counts elements with that same height first.
		int counter = 0;
		for(int i = 0; i < tempCross.length; i++){
			if(tempCross[i].getCenter().getX() == width)
				counter++;
		}
		
		//then declares the array
		Crossroads[] crossroads = new Crossroads[counter];
		
		//then fills it with the objects with the height parameter.
		counter = 0;
		for(int i = 0; i < tempCross.length; i++){
			if(tempCross[i].getCenter().getX() == width) {
				crossroads[counter] = tempCross[i];
				counter++;
			}
			else continue;
		}
		
		return crossroads;
	}
	
	/*
	 * Retrieves the Road in the roads ArrayList with the highest id.
	 */
	private Road retrieveRoadHighestId() {
		Road r = null;
		Iterator<Road> it = this.roads.iterator();
		int max = 0;
		while(it.hasNext()){
			Road r1 = it.next();
			if(r1.getId() > max){
				r = r1;
			}
		}
		return r;
	}
	
	
	/*
	 * Creates the left and right Road objects for each one
	 * of the previously created Crossroads.
	 */
	private void generateRoadsLeftAndRight() {
		List<Crossroads> newCrossroads = new ArrayList<Crossroads>();
		Crossroads[] crossroadsArray = this.convertArrayListCrossroadsToArray(this.crossroads);
		this.orderCrossroadsWidthDesc(crossroadsArray);
		
		int currentHeight = crossroadsArray[0].getCenter().getY();
		int currentWidth = crossroadsArray[0].getCenter().getX();
		int counterNumberDifferentWidth = this.retrieveCrossroadsWithSameWidth(crossroadsArray, currentWidth).length;
		int counterNumberDifferentHeight = this.retrieveCrossroadsWithSameHeight(crossroadsArray, currentHeight).length;
		int counterRoadId = 1;
		
		/*
		 * Generate Roads left and allocates them in each of
		 * the Crossroads where they belong.
		 */
		for(int i = 0; i < counterNumberDifferentHeight; i++){
			
			Crossroads[] tempCrossroads = this.retrieveCrossroadsWithSameHeight
												(crossroadsArray, currentHeight);
			currentHeight = crossroadsArray[counterNumberDifferentWidth].getCenter().getY();
			this.convertArrayCrossroadsToArrayList(crossroadsArray);
			
			for(int j = 0; j < tempCrossroads.length; j++) {
				if(j == 0) {
					int length = (tempCrossroads[j].getCenter().getX() - 
								 tempCrossroads[j+1].getCenter().getX()) - 1;
					
					//left Road.
					Road left = new Road(counterRoadId, "both", length);
					
					//Road points
					List<Point> roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = new Point(tempCrossroads[j].getCenter().getX() - k,
											tempCrossroads[j].getCenter().getY());
						if( (p.isEqual(tempCrossroads[j].getCenter())) ||
							(p.isEqual(tempCrossroads[j+1].getCenter())))
							continue;
						else roadPoints.add(p);
					}
					
					left.setPoints(roadPoints);
					tempCrossroads[j].setR1(left);
					
					this.roads.add(left);
					counterRoadId++;
					
					//right Road.
					length = (this.width - tempCrossroads[j].getCenter().getX()) - 1;
					Road right = new Road(counterRoadId, "both", length);
					
					roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = null;
						if( (tempCrossroads[j].getCenter().getX()+k) < this.width) {
							p = new Point(tempCrossroads[j].getCenter().getX() + k,
									tempCrossroads[j].getCenter().getY());
							if( (p.isEqual(tempCrossroads[j].getCenter())) ||
								(p.isEqual(tempCrossroads[j+1].getCenter())))
								continue;
							else roadPoints.add(p);
						}
					}
					right.setPoints(roadPoints);
					tempCrossroads[j].setR3(right);
					
					this.roads.add(right);
					counterRoadId++;
					
					newCrossroads.add(tempCrossroads[j]);
				}
				
				else if((j != 0) && (j != tempCrossroads.length - 1)){
					
					int length = (tempCrossroads[j].getCenter().getX() - 
							 	 tempCrossroads[j+1].getCenter().getX()) - 1;
					
					//left Road.
					Road left = new Road(counterRoadId, "both", length);
					
					//Road points
					List<Point> roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = new Point(tempCrossroads[j].getCenter().getX() - k,
											tempCrossroads[j].getCenter().getY());
						if( (p.isEqual(tempCrossroads[j].getCenter())) ||
							(p.isEqual(tempCrossroads[j+1].getCenter())) ||
							(p.isEqual(tempCrossroads[j-1].getCenter())))
							continue;
						else roadPoints.add(p);
					}
					left.setPoints(roadPoints);
					tempCrossroads[j].setR1(left);
					
					//right Road.
					Road right = tempCrossroads[j-1].getR1();
					
					roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = null;
						if( (tempCrossroads[j].getCenter().getX()+k) < this.width) {
							p = new Point(tempCrossroads[j].getCenter().getX() + k,
									tempCrossroads[j].getCenter().getY());
							if( (p.isEqual(tempCrossroads[j].getCenter())) ||
								(p.isEqual(tempCrossroads[j+1].getCenter())) ||
								(p.isEqual(tempCrossroads[j-1].getCenter())))
								continue;
							else roadPoints.add(p);
						}
					}
					right.setPoints(roadPoints);
					tempCrossroads[j].setR3(right);
					
					this.roads.add(left);
					counterRoadId++;
					
					newCrossroads.add(tempCrossroads[j]);
				}
				
				else {
					int length = tempCrossroads[j].getCenter().getX();
					
					//left Road.
					Road left = new Road(counterRoadId, "both", length);
					List<Point> roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = new Point(tempCrossroads[j].getCenter().getX() - k,
											tempCrossroads[j].getCenter().getY());
						if( (p.isEqual(tempCrossroads[j].getCenter())) ||
							(p.isEqual(tempCrossroads[j-1].getCenter())))
							continue;
						else roadPoints.add(p);
					}
					left.setPoints(roadPoints);
					tempCrossroads[j].setR1(left);
					
					//right Road.
					Road right = tempCrossroads[j-1].getR1();
					roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = null;
						if( (tempCrossroads[j].getCenter().getX()+k) < this.width) {
							p = new Point(tempCrossroads[j].getCenter().getX() + k,
									tempCrossroads[j].getCenter().getY());
							if( (p.isEqual(tempCrossroads[j].getCenter())) ||
								(p.isEqual(tempCrossroads[j-1].getCenter())))
								continue;
							else roadPoints.add(p);
						}
					}
					right.setPoints(roadPoints);
					tempCrossroads[j].setR3(right);
					
					this.roads.add(left);
					counterRoadId++;
					
					newCrossroads.add(tempCrossroads[j]);
				}
			}

			if((counterNumberDifferentWidth + tempCrossroads.length) >= crossroadsArray.length) {
				int diff = Math.abs(counterNumberDifferentWidth - crossroadsArray.length);
				if(diff < counterNumberDifferentWidth)
					counterNumberDifferentWidth -= diff - 1;
				else {
					counterNumberDifferentWidth = crossroadsArray.length-1;
				}
			}
			else {
				counterNumberDifferentWidth += tempCrossroads.length;
			}
			currentHeight = crossroadsArray[counterNumberDifferentWidth].getCenter().getY();
		}
		this.setCrossroads(newCrossroads);
	}
	
	
	/*
	 * Generates the up and down roads for each one of the
	 * previously generated Crossroads.
	 */
	private void generateRoadsUpAndDown() {
		this.removeCrossroadsDuplicates();
		Crossroads[] crossroadsArray = this.convertArrayListCrossroadsToArray(this.crossroads);
		this.orderCrossroadsHeightDesc(crossroadsArray);
		
		int currentHeight = crossroadsArray[0].getCenter().getY();
		int currentWidth = crossroadsArray[0].getCenter().getX();
		
		int counterNumberDifferentWidth = this.retrieveCrossroadsWithSameHeight(crossroadsArray, currentHeight).length;
		
		int counterRoadId = this.retrieveRoadHighestId().getId() + 1;
		int index = 0;
		
		for(int i = 0; i < counterNumberDifferentWidth; i++){
			
			Crossroads[] tempCrossroads = this.retrieveCrossroadsWithSameWidth
					(crossroadsArray, currentWidth);
			currentWidth = crossroadsArray[index].getCenter().getX();
			this.convertArrayCrossroadsToArrayList(crossroadsArray);
			
			for(int j = 0; j < tempCrossroads.length; j++) {
				
				//element with the highest height.
				if(j == 0) {
					int length = (tempCrossroads[j].getCenter().getY() - 
							 tempCrossroads[j+1].getCenter().getY()) - 1;
					
					//Up Road.
					Road up = new Road(counterRoadId, "both", length);
					
					//Road points
					List<Point> roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = new Point(tempCrossroads[j].getCenter().getX(),
											tempCrossroads[j].getCenter().getY() - k);
						if( (p.isEqual(tempCrossroads[j].getCenter())) ||
							(p.isEqual(tempCrossroads[j+1].getCenter())))
							continue;
						else roadPoints.add(p);
					}
					
					up.setPoints(roadPoints);
					tempCrossroads[j].setR2(up);
					this.roads.add(up);
					counterRoadId++;
					
					//Down Road.
					length = (this.height - tempCrossroads[j].getCenter().getY()) - 1;
					Road down = new Road(counterRoadId, "both", length);
					
					roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = null;
						if( (tempCrossroads[j].getCenter().getY()+k) < this.height) {
							p = new Point(tempCrossroads[j].getCenter().getX(),
									tempCrossroads[j].getCenter().getY() + k);
							if( (p.isEqual(tempCrossroads[j].getCenter())) ||
								(p.isEqual(tempCrossroads[j+1].getCenter())))
								continue;
							else roadPoints.add(p);
						}
					}
					down.setPoints(roadPoints);
					tempCrossroads[j].setR4(down);
					this.roads.add(down);
					counterRoadId++;
				}
				
				//elements in the middle.
				else if ( (j != 0) && (j != tempCrossroads.length - 1)) {
					
					int length = (tempCrossroads[j].getCenter().getY() - 
						 	 tempCrossroads[j+1].getCenter().getY()) - 1;
				
					//Up Road.
					Road up = new Road(counterRoadId, "both", length);
					
					//Road points
					List<Point> roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = new Point(tempCrossroads[j].getCenter().getX(),
											tempCrossroads[j].getCenter().getY() - k);
						if( (p.isEqual(tempCrossroads[j].getCenter())) ||
							(p.isEqual(tempCrossroads[j+1].getCenter())) ||
							(p.isEqual(tempCrossroads[j-1].getCenter())))
							continue;
						else roadPoints.add(p);
					}
					up.setPoints(roadPoints);
					tempCrossroads[j].setR2(up);
					
					//Down Road.
					Road down = tempCrossroads[j-1].getR2();
					
					roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = null;
						if( (tempCrossroads[j].getCenter().getY()+k) < this.height) {
							p = new Point(tempCrossroads[j].getCenter().getX(),
									tempCrossroads[j].getCenter().getY() + k);
							if( (p.isEqual(tempCrossroads[j].getCenter())) ||
								(p.isEqual(tempCrossroads[j+1].getCenter())) ||
								(p.isEqual(tempCrossroads[j-1].getCenter())))
								continue;
							else roadPoints.add(p);
						}
					}
					down.setPoints(roadPoints);
					tempCrossroads[j].setR4(down);
					
					this.roads.add(down);
					counterRoadId++;
				}
				
				//element with the lowest height.
				else {
					int length = tempCrossroads[j].getCenter().getY();
					
					//Up Road.
					Road up = new Road(counterRoadId, "both", length);
					List<Point> roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = new Point(tempCrossroads[j].getCenter().getX(),
											tempCrossroads[j].getCenter().getY() - k);
						if( (p.isEqual(tempCrossroads[j].getCenter())) ||
							(p.isEqual(tempCrossroads[j-1].getCenter())))
							continue;
						else roadPoints.add(p);
					}
					up.setPoints(roadPoints);
					tempCrossroads[j].setR2(up);
					
					//Down Road.
					Road down = tempCrossroads[j-1].getR2();
					roadPoints = new ArrayList<Point>();
					for(int k = 1; k <= length; k++){
						Point p = null;
						if( (tempCrossroads[j].getCenter().getY()+k) < this.height) {
							p = new Point(tempCrossroads[j].getCenter().getX(),
										  tempCrossroads[j].getCenter().getY() + k);
							if( (p.isEqual(tempCrossroads[j].getCenter())) ||
								(p.isEqual(tempCrossroads[j-1].getCenter())))
								continue;
							else roadPoints.add(p);
						}
					}
					down.setPoints(roadPoints);
					tempCrossroads[j].setR4(down);
					
					this.roads.add(down);
					counterRoadId++;
				}
			}
			
			index++;
			currentWidth = crossroadsArray[index].getCenter().getX();
		}
		this.setCrossroads(this.convertArrayCrossroadsToArrayList(crossroadsArray));
	}
	
	
	/*
	 * Generates all the Roads between the generated Crossroads.
	 */
	private void generateAllRoads() {
		this.generateRoadsLeftAndRight();
		this.generateRoadsUpAndDown();
	}
	
	
	/*
	 * 
	 * 
	 * 
	 * 		GARBAGE CONTAINERS GENERATION
	 * 
	 * 
	 * 
	 */
	
	/*
	 * Method to generate one Garbage Container, assigning values
	 * randomly to all its attributes.
	 */
	private GarbageContainer generateGarbageContainer() {
		int GCid = 0;
		String GCType = "";
		
		if(this.garbageContainers.size() == 0){
			GCid = 1;
		}
		else {
			GCid = this.garbageContainers.size() + 1;
		}
		
		Random rType = new Random();
		int type = rType.nextInt(4-1) + 1; //there are 4 different types.
		switch(type) {
			//paper
			case 1:
				GCType = "paper";
				break;
			//glass
			case 2:
				GCType = "glass";
				break;
			//container
			case 3:
				GCType = "container";
				break;
			default:
				GCType = "undifferentiated";
				break;
		}
		
		Random rMaxCapacity = new Random();
		int GCMaxCapacity = rMaxCapacity.nextInt(400-50) + 50; //min 50 kg, max 400 kg.
		
		Random rCurrentOccupation = new Random();
		int GCCurrentOccupation = rCurrentOccupation.nextInt(GCMaxCapacity - 5) + 5;
		
		int xPos = 0;
		int yPos = 0;
		
		Random rIdRoad = new Random();
		int roadId = rIdRoad.nextInt(this.roads.size() - 1) + 1;
		Road chosenRoad = this.roads.get(roadId);
		
		//identify which is the road direction.
		Point a = chosenRoad.getPoints().get(chosenRoad.getPoints().size() - 1);
		Point b = chosenRoad.getPoints().get(chosenRoad.getPoints().size() - 2);
		
		// 0 for roads that go up or down and 1 for roads that go left or right.
		int direction = 0;
		if( (a.getX() == b.getX()) && (a.getY() != b.getY())) {
			direction = 0;
		}
		else {
			direction = 1;
		}
		
		Random rRoadPoint = new Random();
		int roadPoint = rRoadPoint.nextInt(chosenRoad.getPoints().size() - 1) + 1;
		Random rSide = new Random();
		int side = rSide.nextInt(2-1) + 1;
		
		Point p = chosenRoad.getPoints().get(roadPoint);
		//road that goes up or down.
		if(direction == 0){
			//goes to the left.
			if(side == 1) {
				xPos = p.getX()-1;
				yPos = p.getY();
			}
			else{
				xPos = p.getX() + 1;
				yPos = p.getY();
			}
		}
		else {
			if(side == 1) {
				xPos = p.getX();
				yPos = p.getY() - 1;
			}
			else {
				xPos = p.getX();
				yPos = p.getY() + 1;
			}
		}
		
		return new GarbageContainer(GCid, GCType, GCMaxCapacity, GCCurrentOccupation,new Point(xPos, yPos));
	}
	
	
	/*
	 * Generate GarbageContainer objects depending on the size of the map.
	 */
	private void generateAllGarbageContainers() {
		Random rNumberGC = new Random();
		int numberGC = rNumberGC.nextInt(this.height/3 - this.height/5) + this.height/5;
		for(int i = 0; i < numberGC; i++){
			GarbageContainer gc = this.generateGarbageContainer();
			this.garbageContainers.add(gc);
		}
	}
	
	
	/*
	 * 
	 * 
	 * 
	 * 		XML MAP FILE EXPORT/IMPORT
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	/*
	 * Exports the current map to an XML file.
	 * Structure:
	 * <map>
	 * 	<id></id>
	 * 	<name></name>
	 * 	<width></width>
	 * 	<height></height>
	 * 	<crossroads>
	 * 		<crossroad>
	 * 			<id></id>
	 * 			<center x="" y="">
	 * 			<roadLeft>
	 * 				<id></id>
	 * 				<direction></direction>
	 * 				<length></length>
	 * 				<point x="" y="">
	 * 				<point x="" y="">
	 * 				<point x="" y="">
	 * 			</roadLeft>
	 * 			<roadUp>
	 * 				<id></id>
	 * 				<direction></direction>
	 * 				<length></length>
	 * 				<point x="" y="">
	 * 				<point x="" y="">
	 * 				<point x="" y="">
	 * 			</roadUp>
	 * 			<roadRight>
	 * 				<id></id>
	 * 				<direction></direction>
	 * 				<length></length>
	 * 				<point x="" y="">
	 * 				<point x="" y="">
	 * 				<point x="" y="">
	 * 			</roadRight>
	 * 			<roadDown>
	 * 				<id></id>
	 * 				<direction></direction>
	 * 				<length></length>
	 * 				<point x="" y="">
	 * 				<point x="" y="">
	 * 				<point x="" y="">
	 *			</roadDown>
	 *		</crossroad>
	 *	</crossroads>
	 *	<garbagecontainers>
	 *		<garbagecontainer>
	 *			<id></id>
	 *			<type></type>
	 *			<maxCapacity></maxCapacity>
	 *			<currentOccupation></currentOccupation>
	 *			<position x="" y="" />
	 *		</garbagecontainer>
	 *	</garbagecontainers>
	 *</map>
	 */
	public void exportMapToXML(String filename) throws ParserConfigurationException, TransformerException, IOException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("map");
		doc.appendChild(rootElement);
		
		Element id = doc.createElement("id");
		id.appendChild(doc.createTextNode(Integer.toString(this.getId())));
		rootElement.appendChild(id);
		
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(this.getName()));
		rootElement.appendChild(name);
		
		Element width = doc.createElement("width");
		width.appendChild(doc.createTextNode(Integer.toString(this.getWidth())));
		rootElement.appendChild(width);
		
		Element height = doc.createElement("height");
		height.appendChild(doc.createTextNode(Integer.toString(this.getHeight())));
		rootElement.appendChild(height);
		
		Element crossroadsSet = doc.createElement("crossroads");
		
		Iterator<Crossroads> itCrossroads = this.crossroads.iterator();
		while(itCrossroads.hasNext()) {
			Crossroads c = itCrossroads.next();
			Element crossroads = doc.createElement("crossroad");
			
			//Crossroads - id
			Element crossId = doc.createElement("id");
			crossId.appendChild(doc.createTextNode(Integer.toString(c.getId())));
			crossroads.appendChild(crossId);
			
			//Crossroads - center.
			Element center = doc.createElement("center");
			Attr xCenter = doc.createAttribute("x");
			xCenter.setValue(Integer.toString(c.getCenter().getX()));
			Attr yCenter = doc.createAttribute("y");
			yCenter.setValue(Integer.toString(c.getCenter().getY()));
			center.setAttributeNode(xCenter);
			center.setAttributeNode(yCenter);
			crossroads.appendChild(center);
			
			//Crossroads - road left.
			Element left = doc.createElement("roadLeft");
			Element leftId = doc.createElement("id");
			leftId.appendChild(doc.createTextNode(Integer.toString(c.getR1().getId())));
			left.appendChild(leftId);
			Element leftDir = doc.createElement("direction");
			leftDir.appendChild(doc.createTextNode(c.getR1().getDirection()));
			left.appendChild(leftDir);
			Element leftLength = doc.createElement("length");
			leftLength.appendChild(doc.createTextNode(Integer.toString(c.getR1().getLength())));
			left.appendChild(leftLength);
			Element leftPointsTag = doc.createElement("points");
			
			Iterator<Point> leftPoints = c.getR1().getPoints().iterator();
			while(leftPoints.hasNext()){
				Point p = leftPoints.next();
				Element point = doc.createElement("point");
				Attr xP = doc.createAttribute("x");
				xP.setValue(Integer.toString(p.getX()));
				point.setAttributeNode(xP);
				Attr yP = doc.createAttribute("y");
				yP.setValue(Integer.toString(p.getY()));
				point.setAttributeNode(yP);
				leftPointsTag.appendChild(point);
				left.appendChild(leftPointsTag);
			}
			crossroads.appendChild(left);
			
			//Crossroads - road up.
			Element up = doc.createElement("roadUp");
			Element upId = doc.createElement("id");
			upId.appendChild(doc.createTextNode(Integer.toString(c.getR2().getId())));
			up.appendChild(upId);
			Element upDir = doc.createElement("direction");
			upDir.appendChild(doc.createTextNode(c.getR2().getDirection()));
			up.appendChild(upDir);
			Element upLength = doc.createElement("length");
			upLength.appendChild(doc.createTextNode(Integer.toString(c.getR2().getLength())));
			up.appendChild(upLength);
			Element upPointsTag = doc.createElement("points");
			
			Iterator<Point> upPoints = c.getR2().getPoints().iterator();
			while(upPoints.hasNext()){
				Point p = upPoints.next();
				Element point = doc.createElement("point");
				Attr xP = doc.createAttribute("x");
				xP.setValue(Integer.toString(p.getX()));
				point.setAttributeNode(xP);
				Attr yP = doc.createAttribute("y");
				yP.setValue(Integer.toString(p.getY()));
				point.setAttributeNode(yP);
				upPointsTag.appendChild(point);
				up.appendChild(upPointsTag);
			}
			crossroads.appendChild(up);
			
			//Crossroads - road right.
			Element right = doc.createElement("roadRight");
			Element rightId = doc.createElement("id");
			rightId.appendChild(doc.createTextNode(Integer.toString(c.getR3().getId())));
			right.appendChild(rightId);
			Element rightDir = doc.createElement("direction");
			rightDir.appendChild(doc.createTextNode(c.getR3().getDirection()));
			right.appendChild(rightDir);
			Element rightLength = doc.createElement("length");
			rightLength.appendChild(doc.createTextNode(Integer.toString(c.getR3().getLength())));
			right.appendChild(rightLength);
			Element rightPointsTag = doc.createElement("points");
			
			Iterator<Point> rightPoints = c.getR3().getPoints().iterator();
			while(rightPoints.hasNext()){
				Point p = rightPoints.next();
				Element point = doc.createElement("point");
				Attr xP = doc.createAttribute("x");
				xP.setValue(Integer.toString(p.getX()));
				point.setAttributeNode(xP);
				Attr yP = doc.createAttribute("y");
				yP.setValue(Integer.toString(p.getY()));
				point.setAttributeNode(yP);
				rightPointsTag.appendChild(point);
				right.appendChild(rightPointsTag);
			}
			crossroads.appendChild(right);
			
			//Crossroads - road down.
			Element down = doc.createElement("roadDown");
			Element downId = doc.createElement("id");
			downId.appendChild(doc.createTextNode(Integer.toString(c.getR4().getId())));
			down.appendChild(downId);
			Element downDir = doc.createElement("direction");
			downDir.appendChild(doc.createTextNode(c.getR4().getDirection()));
			down.appendChild(downDir);
			Element downLength = doc.createElement("length");
			downLength.appendChild(doc.createTextNode(Integer.toString(c.getR4().getLength())));
			down.appendChild(downLength);
			Element downPointsTag = doc.createElement("points");
			
			Iterator<Point> downPoints = c.getR4().getPoints().iterator();
			while(downPoints.hasNext()){
				Point p = downPoints.next();
				Element point = doc.createElement("point");
				Attr xP = doc.createAttribute("x");
				xP.setValue(Integer.toString(p.getX()));
				point.setAttributeNode(xP);
				Attr yP = doc.createAttribute("y");
				yP.setValue(Integer.toString(p.getY()));
				point.setAttributeNode(yP);
				downPointsTag.appendChild(point);
				down.appendChild(downPointsTag);
			}
			crossroads.appendChild(down);
			
			crossroadsSet.appendChild(crossroads);
		}
		rootElement.appendChild(crossroadsSet);
		
		Element garbageContainersSet = doc.createElement("garbagecontainers");
		Iterator<GarbageContainer> itGC = this.garbageContainers.iterator();
		while(itGC.hasNext()){
			GarbageContainer gc = itGC.next();
			
			Element garbageContainer = doc.createElement("garbagecontainer");
			
			Element gcId = doc.createElement("id");
			gcId.appendChild(doc.createTextNode(Integer.toString(gc.getId())));
			garbageContainer.appendChild(gcId);
			
			Element gcType = doc.createElement("type");
			gcType.appendChild(doc.createTextNode(gc.getType()));
			garbageContainer.appendChild(gcType);
			
			Element gcMaxCap = doc.createElement("maxCapacity");
			gcMaxCap.appendChild(doc.createTextNode(Double.toString(gc.getMaxCapacity())));
			garbageContainer.appendChild(gcMaxCap);
			
			Element gcCurrentOccupation = doc.createElement("currentOccupation");
			gcCurrentOccupation.appendChild(doc.createTextNode(Double.toString(gc.getCurrentOccupation())));
			garbageContainer.appendChild(gcCurrentOccupation);
			
			Element gcPosition = doc.createElement("position");
			Attr xP = doc.createAttribute("x");
			xP.setValue(Integer.toString(gc.getPosition().getX()));
			gcPosition.setAttributeNode(xP);
			
			Attr yP = doc.createAttribute("y");
			yP.setValue(Integer.toString(gc.getPosition().getY()));
			gcPosition.setAttributeNode(yP);
			
			garbageContainer.appendChild(gcPosition);
			
			garbageContainersSet.appendChild(garbageContainer);
		}
		rootElement.appendChild(garbageContainersSet);
		
		File f = new File(System.getProperty("user.dir") + "/maps");
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
	 * Constructs CityMap from information in an XML file.
	 */
	public CityMap(String filename) throws ParserConfigurationException, SAXException, IOException{
		
		this.crossroads = new ArrayList<Crossroads>();
		this.roads = new ArrayList<Road>();
		this.garbageContainers = new ArrayList<GarbageContainer>();
		this.trucks = new ArrayList<Truck>();
		
		File fXmlFile = new File(this.filePath + "/" + filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		
		doc.getDocumentElement().normalize();
		
		String id = doc.getElementsByTagName("id").item(0).getTextContent();
		
		if(!id.equals(null))
			this.id = Integer.parseInt(id.toString());
		else return;
		
		String name = doc.getElementsByTagName("name").item(0).getTextContent();
		
		if(!name.equals(null))
			this.name = name;
		else return;
		
		String width = doc.getElementsByTagName("width").item(0).getTextContent();
		
		if(!width.equals(null))
			this.width = Integer.parseInt(width.toString());
		else return;
		
		String height = doc.getElementsByTagName("height").item(0).getTextContent();
		
		if(!height.equals(null))
			this.height = Integer.parseInt(height.toString());
		else return;
		
		NodeList nList = doc.getElementsByTagName("crossroad");
		
		for(int temp = 0; temp < nList.getLength(); temp++){
			Node nNode = nList.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				
				Element eElement = (Element) nNode;
				
				String crossroadsId = eElement.getElementsByTagName("id").item(0).getTextContent();
				
				if(crossroadsId.equals(null)) return;
				
				Element crossroadsCenter = (Element) eElement.getElementsByTagName("center").item(0);
				String centerX = crossroadsCenter.getAttribute("x");
				String centerY = crossroadsCenter.getAttribute("y");
				
				if( (centerX.equals(null)) || (centerY.equals(null))) return;
				
				Crossroads c = new Crossroads(Integer.parseInt(crossroadsId.toString()), 
											  new Point(Integer.parseInt(centerX), 
													  	Integer.parseInt(centerY)));
				
				/*
				 * Left Road for the Crossroads c.
				 */
				Element roadLeft = (Element) eElement.getElementsByTagName("roadLeft").item(0);
				
				NodeList roadLeftList = roadLeft.getChildNodes();
				String roadLeftId = roadLeftList.item(0).getTextContent();
				
				String roadLeftDir = roadLeftList.item(1).getTextContent();
				
				String roadLeftLength = roadLeftList.item(2).getTextContent();
				
				Road left = new Road(Integer.parseInt(roadLeftId.toString()),
									 roadLeftDir.toString(),
									 Integer.parseInt(roadLeftLength.toString()));
				
				List<Point> leftRoadPoints = new ArrayList<Point>();
				
				for(int i = 0; i < roadLeftList.getLength(); i++){
					Node pNode = roadLeftList.item(i);
					
					if (pNode.getNodeType() == Node.ELEMENT_NODE) {
						Element points = (Element) pNode;
						NodeList nl = points.getChildNodes();
						
						for(int j = 0; j < nl.getLength(); j++){
							Node point = nl.item(j);
							
							if(nl.item(j).getNodeType() == Node.ELEMENT_NODE){
								Element pointElement = (Element) point;
								
								String xStr = pointElement.getAttribute("x");
								String yStr = pointElement.getAttribute("y");
								
								int x = Integer.parseInt(xStr);
								int y = Integer.parseInt(yStr);
								
								Point p = new Point(x,y);
								leftRoadPoints.add(p);
							}
							
							
						}
					}
				}
				
				left.setPoints(leftRoadPoints);
				c.setR1(left);
				
				if(!this.roads.contains(left))
					this.roads.add(left);
				
				
				/*
				 * Up Road for the Crossroads c.
				 */
				Element roadUp = (Element) eElement.getElementsByTagName("roadUp").item(0);
				
				NodeList roadUpList = roadUp.getChildNodes();
				String roadUpId = roadUpList.item(0).getTextContent();
				
				String roadUpDir = roadUpList.item(1).getTextContent();
				
				String roadUpLength = roadUpList.item(2).getTextContent();
				
				Road up = new Road(Integer.parseInt(roadUpId.toString()),
									 roadUpDir.toString(),
									 Integer.parseInt(roadUpLength.toString()));
				
				List<Point> upRoadPoints = new ArrayList<Point>();
				
				for(int i = 0; i < roadUpList.getLength(); i++){
					Node pNode = roadUpList.item(i);
					
					if (pNode.getNodeType() == Node.ELEMENT_NODE) {
						Element points = (Element) pNode;
						NodeList nl = points.getChildNodes();
						
						for(int j = 0; j < nl.getLength(); j++){
							Node point = nl.item(j);
							
							if(nl.item(j).getNodeType() == Node.ELEMENT_NODE){
								Element pointElement = (Element) point;
								
								String xStr = pointElement.getAttribute("x");
								String yStr = pointElement.getAttribute("y").toString();
								
								int x = Integer.parseInt(xStr);
								int y = Integer.parseInt(yStr);
								
								Point p = new Point(x,y);
								upRoadPoints.add(p);
							}
						}
					}
				}
				
				up.setPoints(upRoadPoints);
				c.setR2(up);
				
				if(!this.roads.contains(up))
					this.roads.add(up);
				
				
				/*
				 * Right Road for the Crossroads c.
				 */
				Element roadRight = (Element) eElement.getElementsByTagName("roadRight").item(0);
				
				NodeList roadRightList = roadRight.getChildNodes();
				String roadRightId = roadRightList.item(0).getTextContent();
				
				String roadRightDir = roadRightList.item(1).getTextContent();
				
				String roadRightLength = roadRightList.item(2).getTextContent();
				
				Road right = new Road(Integer.parseInt(roadRightId.toString()),
									 roadRightDir.toString(),
									 Integer.parseInt(roadRightLength.toString()));
				
				List<Point> rightRoadPoints = new ArrayList<Point>();
				
				for(int i = 0; i < roadRightList.getLength(); i++){
					Node pNode = roadRightList.item(i);
					
					if (pNode.getNodeType() == Node.ELEMENT_NODE) {
						Element points = (Element) pNode;
						NodeList nl = points.getChildNodes();
						for(int j = 0; j < nl.getLength(); j++){
							Node point = nl.item(j);
							
							if(nl.item(j).getNodeType() == Node.ELEMENT_NODE){
								Element pointElement = (Element) point;
								
								String xStr = pointElement.getAttribute("x");
								String yStr = pointElement.getAttribute("y");
								
								int x = Integer.parseInt(xStr);
								int y = Integer.parseInt(yStr);
								
								Point p = new Point(x,y);
								rightRoadPoints.add(p);
							}
						}
					}
				}
				right.setPoints(rightRoadPoints);
				c.setR3(right);
				
				if(!this.roads.contains(right))
					this.roads.add(right);
				
				
				/*
				 * Down Road for the Crossroads c.
				 */
				Element roadDown = (Element) eElement.getElementsByTagName("roadDown").item(0);
				
				NodeList roadDownList = roadDown.getChildNodes();
				String roadDownId = roadDownList.item(0).getTextContent();
				
				String roadDownDir = roadDownList.item(1).getTextContent();
				
				String roadDownLength = roadDownList.item(2).getTextContent();
				
				Road down = new Road(Integer.parseInt(roadDownId.toString()),
									 roadDownDir.toString(),
									 Integer.parseInt(roadDownLength.toString()));
				
				List<Point> downRoadPoints = new ArrayList<Point>();
				
				for(int i = 0; i < roadDownList.getLength(); i++){
					Node pNode = roadDownList.item(i);
					
					if (pNode.getNodeType() == Node.ELEMENT_NODE) {
						Element points = (Element) pNode;
						NodeList nl = points.getChildNodes();
						for(int j = 0; j < nl.getLength(); j++){
							Node point = nl.item(j);
							
							if(nl.item(j).getNodeType() == Node.ELEMENT_NODE){
								Element pointElement = (Element) point;
								
								String xStr = pointElement.getAttribute("x");
								String yStr = pointElement.getAttribute("y");
								
								int x = Integer.parseInt(xStr);
								int y = Integer.parseInt(yStr);
								
								Point p = new Point(x,y);
								downRoadPoints.add(p);
							}
						}
					}
				}
				
				down.setPoints(downRoadPoints);
				c.setR4(down);
				
				if(!this.roads.contains(down))
					this.roads.add(down);
				
				this.crossroads.add(c);
			}
		}
		
		/*
		 * Garbage Containers.
		 */
		nList = doc.getElementsByTagName("garbagecontainer");
		
		for(int temp = 0; temp < nList.getLength(); temp++){
			Node nNode = nList.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				
				Element eElement = (Element) nNode;
				
				String gcId = eElement.getElementsByTagName("id").item(0).getTextContent();
				
				String gcType = eElement.getElementsByTagName("type").item(0).getTextContent();
				
				String gcMaxCapacity = eElement.getElementsByTagName("maxCapacity").item(0).getTextContent();
				
				String gcCurrentOccupation = eElement.getElementsByTagName("currentOccupation").item(0).getTextContent();
				
				Element gcPosition = (Element) eElement.getElementsByTagName("position").item(0);
				int x = Integer.parseInt(gcPosition.getAttribute("x").toString());
				int y = Integer.parseInt(gcPosition.getAttribute("y").toString());
				
				Point position = new Point(x,y);
				
				GarbageContainer gc = new GarbageContainer(Integer.parseInt(gcId),
														   gcType,
														   Double.parseDouble(gcMaxCapacity),
														   Double.parseDouble(gcCurrentOccupation),
														   position);
				this.garbageContainers.add(gc);
			}
		}
		
		//allocates all points in the points ArrayList
		this.allocatePoints();
		this.allocatePointTypeValuesInRoads();
	}
	
	
	
	/*
	 * 
	 * 
	 * 	POINT ALLOCATION.
	 * 
	 * 
	 */
	
	/*
	 *	Allocates all created points (in Crossroads, Roads and GarbageContainers Lists)
	 * to the Points List. 
	 */
	private void allocatePoints() {
		this.points = new ArrayList<Point>();
		
		//fills the points arraylist with points.
		for(int i = 0; i < this.width; i++) {
			for(int j = 0; j < this.height; j++){
				Point p = new Point(i,j);
				boolean foundOtherType = false;
				
				Iterator<Crossroads> itCrossroads = this.crossroads.iterator();
				while(itCrossroads.hasNext()){
					Crossroads c = itCrossroads.next();
					if( (i == c.getCenter().getX()) && (j == c.getCenter().getY())){
						p.setType("CROSSROADS");
						foundOtherType = true;
						break;
					}
				}
				
				Iterator<Road> itRoad = this.roads.iterator();
				while(itRoad.hasNext()) {
					Road r = itRoad.next();
					List<Point> roadPoints = new ArrayList<Point>();
					roadPoints.addAll(r.getPoints());
					
					Iterator<Point> roadPointsIt = roadPoints.iterator();
					while(roadPointsIt.hasNext()) {
						Point roadPoint = roadPointsIt.next();
						if( (i == roadPoint.getX()) && (j == roadPoint.getY())){
							p.setType("ROAD");
							foundOtherType = true;
							break;
						}
					}
				}
				
				Iterator<GarbageContainer> itGC = this.garbageContainers.iterator();
				while(itGC.hasNext()){
					GarbageContainer gc = itGC.next();
					if( (i == gc.getPosition().getX()) && (j == gc.getPosition().getY())){
						p.setType("GARBAGE_CONTAINER");
						foundOtherType = true;
						break;
					}
				}
				
				if(!foundOtherType)
					p.setType("HOUSE");
				this.points.add(p);
			}
		}
		
		this.sortPointsByAscendingX();
	}
	
	/*
	 * Allocates types values in the roads arraylist.
	 */
	private void allocatePointTypeValuesInRoads(){
		Iterator<Road> itRoads = this.roads.iterator();
		while(itRoads.hasNext()) {
			Road r = itRoads.next();
			Iterator<Point> itRoadPoints = r.getPoints().iterator();
			while(itRoadPoints.hasNext()){
				Point rP = itRoadPoints.next();
				Iterator<Point> itPoints = this.points.iterator();
				while(itPoints.hasNext()) {
					Point p = itPoints.next();
					if( (p.getX() == rP.getX()) && (p.getY() == rP.getY())){
						rP.setType(p.getType());
					}
				}
			}
		}
	}
	
	
	/*
	 * 
	 *
	 * 
	 * 		RANDOM CITYMAP CONSTRUCTOR
	 * 				AND
	 * 		CITYMAP INFORMATION PRINTING
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	/*
	 * This constructor does not take any parameters, hence it
	 * generates random values for the attributes of the CityMap
	 * object.
	 */
	public CityMap(int minWidth, int maxWidth, int minHeight, int maxHeight) {
		//generates a random id for the CityMap object, between 1 and 100.
		Random id = new Random();
		int lower = 1;
		int higher = 100;
		this.id = id.nextInt(higher-lower) + lower;
		
		this.name = "cityMap" + this.id;
		
		//generates random width between 100 and 400.
		Random width = new Random();
		lower = minWidth;
		higher = maxWidth;
		this.width = width.nextInt(higher-lower) + lower;
		
		//generates random height between 100 and 400.
		Random height = new Random();
		lower = minHeight;
		higher = maxHeight;
		this.height = height.nextInt(higher-lower) + lower;
		
		//create the first Crossroads.
		Random rDistanceToKeep = new Random();
		int distanceToKeep = rDistanceToKeep.nextInt(5-2) + 2;
		Crossroads firstCrossroads = new Crossroads(1, 
									this.randomizeCrossroadsCenter(distanceToKeep));
		this.crossroads = new ArrayList<Crossroads>();
		this.crossroads.add(firstCrossroads);
		
		//create the remaining necessary Crossroads.
		Random rMaxRoadLength = new Random();
		int maxRoadLength = rMaxRoadLength.nextInt(this.height/3 - 4) + 4;
		this.createRemainingCrossroads(firstCrossroads, distanceToKeep, maxRoadLength);
		
		//create all the Roads.
		this.roads = new ArrayList<Road>();
		this.generateAllRoads();
		
		//create GarbageContainer objects.
		this.garbageContainers = new ArrayList<GarbageContainer>();
		this.generateAllGarbageContainers();
		
		//allocates all points in the points ArrayList
		this.allocatePoints();
		this.allocatePointTypeValuesInRoads();
		
		//Trucks.
		this.trucks = new ArrayList<Truck>();
	}
	
	
	/*
	 * Prints the CityMap in a String sequence.
	 */
	public void printCityMapString() {
		String[][] cityMap = new String[this.width][this.height];
		
		//allocates in the cityMap the housing area (that for now is all the area)
		for(int i = 0; i < cityMap.length; i++){
			for(int j = 0; j < cityMap[0].length; j++){
				cityMap[i][j] = "o";
			}
		}
		
		//allocates in the cityMap String the crossroads centers.
		Iterator<Crossroads> it = this.crossroads.iterator();
		int x = 0, y = 0;
		while(it.hasNext()) {
			Crossroads c = it.next();
			x = c.getCenter().getX();
			y = c.getCenter().getY();
			cityMap[x][y] = "c";
			
			Road left = c.getR1();
			Iterator<Point> itPoints = left.getPoints().iterator();
			while(itPoints.hasNext()){
				Point p = itPoints.next();
				cityMap[p.getX()][p.getY()] = "r";
			}
			
			
			Road up = c.getR2();
			itPoints = up.getPoints().iterator();
			while(itPoints.hasNext()){
				Point p = itPoints.next();
				cityMap[p.getX()][p.getY()] = "r";
			}
			
			Road right = c.getR3();
			itPoints = right.getPoints().iterator();
			while(itPoints.hasNext()){
				Point p = itPoints.next();
				cityMap[p.getX()][p.getY()] = "r";
			}
			
			
			Road down = c.getR4();
			itPoints = down.getPoints().iterator();
			while(itPoints.hasNext()){
				Point p = itPoints.next();
				cityMap[p.getX()][p.getY()] = "r";
			}
		}
		
		Iterator<GarbageContainer> itGC = this.garbageContainers.iterator();
		while(itGC.hasNext()){
			GarbageContainer gc = itGC.next();
			cityMap[gc.getPosition().getX()][gc.getPosition().getY()] = "G";
		}
		
		String debugPrinter = "";
		
		for(int i = 0; i < cityMap[0].length; i++){
			for(int j = 0; j < cityMap.length; j++){
				debugPrinter += cityMap[j][i] + " ";
			}
			debugPrinter += "\n";
		}
		
		System.out.println(debugPrinter);
		System.out.println();
	}
	
}
