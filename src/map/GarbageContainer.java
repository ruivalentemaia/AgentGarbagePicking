package map;

public class GarbageContainer {
	private int id;
	private String type; //4 different types: paper, glass, container and undifferentiated.
	private double maxCapacity; //in kg.
	private double currentOccupation; //in kg.
	private Point position;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public double getCurrentOccupation() {
		return currentOccupation;
	}

	public void setCurrentOccupation(double currentOccupation) {
		this.currentOccupation = currentOccupation;
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}
	
	/*
	 * Constructor of a garbage container.
	 */
	public GarbageContainer(int Id, String t, double maxC, double currC, Point p) {
		this.id = Id;
		this.type = t;
		this.maxCapacity = maxC;
		this.currentOccupation = currC;
		this.position = p;
	}
	
}
