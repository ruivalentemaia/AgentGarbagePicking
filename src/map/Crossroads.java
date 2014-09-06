package map;

public class Crossroads {
	private int id;
	private Road r1;
	private Road r2;
	private Road r3;
	private Road r4;
	private Point center;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Road getR1() {
		return r1;
	}
	
	public void setR1(Road r1) {
		this.r1 = r1;
	}
	
	public Road getR2() {
		return r2;
	}
	
	public void setR2(Road r2) {
		this.r2 = r2;
	}

	public Road getR3() {
		return r3;
	}

	public void setR3(Road r3) {
		this.r3 = r3;
	}

	public Road getR4() {
		return r4;
	}

	public void setR4(Road r4) {
		this.r4 = r4;
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
		this.r1 = one;
		this.r2 = two;
		this.r3 = three;
		this.r4 = four;
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
		r1.printInfo();
		r2.printInfo();
		r3.printInfo();
		r4.printInfo();
	}
}
