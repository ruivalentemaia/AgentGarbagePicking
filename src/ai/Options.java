package ai;

public class Options {
	private boolean allTrucksStartingSamePosition;
	
	public boolean isAllTrucksStartingSamePosition() {
		return allTrucksStartingSamePosition;
	}

	public void setAllTrucksStartingSamePosition(
			boolean allTrucksStartingSamePosition) {
		this.allTrucksStartingSamePosition = allTrucksStartingSamePosition;
	}

	public Options() {
		this.setAllTrucksStartingSamePosition(true);
	}
}
