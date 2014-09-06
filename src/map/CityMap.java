package map;

import java.util.ArrayList;
import java.util.List;
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
	 * Create one Road with random dimensions, id and direction.
	 */
	public Road randomizeRoad(Point startingPoint) {
		//generates a random id between 100 and 1.
		int id = 0;
		Random rId = new Random();
		id = rId.nextInt(100-1) + 1;
		
		//generates number between 1 and 3 to define direction of the road.
		String direction = "";
		int dir = 0;
		Random rDir = new Random();
		dir = rDir.nextInt(3-1) + 1;
		switch(dir) {
			//left
			case 1:
				direction = "<";
				break;
			//right	
			case 2:
				direction = ">";
				break;
			//both	
			case 3:
				direction = "=";
				break;
			default:
				direction = "";
				break;
		}
		
		//generates a width between the 1 and this maps's height divided by a hundred.
		int length = 0;
		Random rWidth = new Random();
		length = rWidth.nextInt( (this.height / 10) - 1) + 1;
		
		return new Road(id, direction, length);
	}
	
	/*
	 * Returns a randomly generated center Point to be used
	 * in the constructor of a Crossroads object.
	 */
	public Point randomizeCrossroadsCenter() {
		int x = 0, y = 0;
		Random rX = new Random();
		Random rY = new Random();
		x = rX.nextInt(this.width - 1) + 1;
		y = rY.nextInt(this.height - 1) + 1;
		return new Point(x,y);
	}
	
	
	/*
	 * This constructor does not take any parameters, hence it
	 * generates random values for the attributes of the CityMap
	 * object.
	 */
	public CityMap() {
		//generates a random id for the CityMap object, between 1 and 100.
		Random id = new Random();
		int lower = 1;
		int higher = 100;
		this.id = id.nextInt(higher-lower) + lower;
		
		this.name = "cityMap" + this.id;
		
		//generates random width between 100 and 400.
		Random width = new Random();
		lower = 100;
		higher = 400;
		this.width = width.nextInt(higher-lower) + lower;
		
		//generates random height between 100 and 400.
		Random height = new Random();
		lower = 100;
		higher = 400;
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
		Crossroads firstCrossroads = new Crossroads(1, this.randomizeCrossroadsCenter());
		this.crossroads = new ArrayList<Crossroads>();
		this.crossroads.add(firstCrossroads);
	}
	
}
