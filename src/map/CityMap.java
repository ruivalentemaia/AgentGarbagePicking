package map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class CityMap {
	private int id;
	private String name;
	private int width;
	private int height;
	private List<Point> points;
	private List<Road> roads;
	private List<Crossroads> crossroads;
	private List<GarbageContainer> garbageContainers;
	
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
	 * 
	 * 
	 * 						CROSSROADS GENERATION
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
	 * 							ROADS GENERATION
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
		System.out.println();
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
	 * 					GARBAGE CONTAINERS GENERATION
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
		int type = rType.nextInt(4-1) + 4; //there are 4 different types.
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
		
		//fills the points arraylist with points.
		this.points = new ArrayList<Point>();
		for(int i = 1; i <= this.width; i++) {
			for(int j = 1; j <= this.height; j++){
				Point p = new Point(i,j);
				this.points.add(p);
			}
		}
		
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
			System.out.println("Generated Crossroad with center = ("+ x + ", " + y + ")");
			cityMap[x][y] = "c";
			
			Road left = c.getR1();
			Iterator<Point> itPoints = left.getPoints().iterator();
			while(itPoints.hasNext()){
				Point p = itPoints.next();
				cityMap[p.getX()][p.getY()] = "r";
				System.out.println("\t Added Point at (" + p.getX() + ", " + p.getY() + ")");
			}
			
			
			Road up = c.getR2();
			itPoints = up.getPoints().iterator();
			while(itPoints.hasNext()){
				Point p = itPoints.next();
				cityMap[p.getX()][p.getY()] = "r";
				System.out.println("\t Added Point at (" + p.getX() + ", " + p.getY() + ")");
			}
			
			Road right = c.getR3();
			itPoints = right.getPoints().iterator();
			while(itPoints.hasNext()){
				Point p = itPoints.next();
				cityMap[p.getX()][p.getY()] = "r";
				System.out.println("\t Added Point at (" + p.getX() + ", " + p.getY() + ")");
			}
			
			
			Road down = c.getR4();
			itPoints = down.getPoints().iterator();
			while(itPoints.hasNext()){
				Point p = itPoints.next();
				cityMap[p.getX()][p.getY()] = "r";
				System.out.println("\t Added Point at (" + p.getX() + ", " + p.getY() + ")");
			}
		}
		
		Iterator<GarbageContainer> itGC = this.garbageContainers.iterator();
		System.out.println("\n Garbage Containers:");
		while(itGC.hasNext()){
			GarbageContainer gc = itGC.next();
			cityMap[gc.getPosition().getX()][gc.getPosition().getY()] = "G";
			System.out.println("\t Added GarbageContainer at (" + gc.getPosition().getX() + ", " + gc.getPosition().getY() + ")");
		}
		
		String debugPrinter = "";
		debugPrinter += "\nNumber of columns = " + cityMap.length;
		debugPrinter += "\nNumber of lines = " + cityMap[0].length;
		debugPrinter += "\n";
		
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
