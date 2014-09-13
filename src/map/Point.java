package map;

public class Point {
	private int x;
	private int y;
	private String type;
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		if ( (type.equals("HOUSE")) || (type.equals("ROAD")) 
		  || (type.equals("CROSSROADS")) || (type.equals("GARBAGE_CONTAINER")) )
			this.type = type;
		else this.type = "HOUSE";
		
	}

	/*
	 * Constructs a Point with an x and a y integers as parameters.
	 */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/*
	 * Checks if a parameter Point b is equal to this point.
	 */
	public boolean isEqual(Point b){
		if( (this.getX() == b.getX()) && (this.getY() == b.getY()))
			return true;
		else return false;
	}
}
