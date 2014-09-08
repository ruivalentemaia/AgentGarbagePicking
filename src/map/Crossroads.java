package map;

public class Crossroads {
	private int id;
	private Road left;
	private Road up;
	private Road right;
	private Road down;
	private Point center;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Road getR1() {
		return left;
	}
	
	public void setR1(Road r1) {
		this.left = r1;
	}
	
	public Road getR2() {
		return up;
	}
	
	public void setR2(Road r2) {
		this.up = r2;
	}

	public Road getR3() {
		return right;
	}

	public void setR3(Road r3) {
		this.right = r3;
	}

	public Road getR4() {
		return down;
	}

	public void setR4(Road r4) {
		this.down = r4;
	}
	
	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	/*
	 * A crossroads is an object that represents the point where
	 * four roads encounter themselves. It's basically a connection
	 * between roads then. The constructor receives 4 Roads to build
	 * the Crossroads object.
	 */
	public Crossroads(Point center, Road one, Road two, Road three, Road four){
		this.center = center;
		this.left = one;
		this.up = two;
		this.right = three;
		this.down = four;
	}
	
	/*
	 * Constructor for the Crossroads object with only the center as
	 * parameter. This constructor exists because there was a necessity
	 * to add posteriorly each one of the 4 roads.
	 */
	public Crossroads(int id, Point center) {
		this.setId(id);
		this.center = center;
	}
	
	/*
	 * Print Crossroads info.
	 */
	public void printInfo() {
		System.out.println("\n");
		System.out.println("Crossroads " + this.id);
		System.out.println("Roads: ");
		left.printInfo();
		up.printInfo();
		right.printInfo();
		down.printInfo();
	}
}
