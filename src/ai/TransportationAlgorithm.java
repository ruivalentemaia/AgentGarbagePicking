package ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import map.GarbageContainer;
import agent.Truck;

public class TransportationAlgorithm {
	private int id;
	private List<Truck> trucks;
	private List<GarbageContainer> garbageContainers;
	private Map<Truck, Goal> assignments;
	
	//variables needed for the Transportation Algorithm
	private double[][] valuesMatrix;
	private double[][] costsMatrix;
	private double[] demandValues;
	private double[] supplyValues;
	private double totalDemand;
	private double totalSupply;
	private double[] u;
	private double[] v;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public List<Truck> getTrucks() {
		return trucks;
	}

	public void setTrucks(List<Truck> trucks) {
		this.trucks = trucks;
	}

	public List<GarbageContainer> getGarbageContainers() {
		return garbageContainers;
	}

	public void setGarbageContainers(List<GarbageContainer> garbageContainers) {
		this.garbageContainers = garbageContainers;
	}

	public Map<Truck, Goal> getAssignments() {
		return assignments;
	}

	public void setAssignments(Map<Truck, Goal> assignments) {
		this.assignments = assignments;
	}
	
	
	public double[][] getValuesMatrix() {
		return valuesMatrix;
	}

	public void setValuesMatrix(double[][] valuesMatrix) {
		this.valuesMatrix = valuesMatrix;
	}

	public double[][] getCostsMatrix() {
		return costsMatrix;
	}

	public void setCostsMatrix(double[][] costsMatrix) {
		this.costsMatrix = costsMatrix;
	}

	public double[] getDemandValues() {
		return demandValues;
	}

	public void setDemandValues(double[] demandValues) {
		this.demandValues = demandValues;
	}

	public double[] getSupplyValues() {
		return supplyValues;
	}

	public void setSupplyValues(double[] supplyValues) {
		this.supplyValues = supplyValues;
	}

	public double getTotalDemand() {
		return totalDemand;
	}

	public void setTotalDemand(double totalDemand) {
		this.totalDemand = totalDemand;
	}

	public double getTotalSupply() {
		return totalSupply;
	}

	public void setTotalSupply(double totalSupply) {
		this.totalSupply = totalSupply;
	}

	public double[] getU() {
		return u;
	}

	public void setU(double[] u) {
		this.u = u;
	}

	public double[] getV() {
		return v;
	}

	public void setV(double[] v) {
		this.v = v;
	}

	/**
	 * 
	 * 
	 * CONSTRUCTOR.
	 * 
	 * 
	 */
	
	public TransportationAlgorithm(List<Truck> ts, List<GarbageContainer> gc) {
		this.trucks = new ArrayList<Truck>();
		this.trucks = ts;
		this.garbageContainers = new ArrayList<GarbageContainer>();
		this.garbageContainers = gc;
		this.assignments = new LinkedHashMap<Truck, Goal>();
	}
	
	
	
	/**
	 * 
	 * 
	 * 	
	 * 	TRANSPORTATION ALGORITHM STEPS.
	 * 
	 * 
	 * 
	 */
	
	
	/*
	 * Initializes the problem:
	 * 1 - calculates total demand.
	 * 2 - calculates total supply.costs
	 * 3 - checks if they're equal. If yes, proceeds normally. If not, adds a line
	 * or a column, depending on the case.
	 * 4 - Fills the costsMatrix.
	 */
	private void initialize() {
		int nAgents = this.trucks.size();
		int nGarbageContainers = this.garbageContainers.size();
		
		this.demandValues = new double[nGarbageContainers];
		this.supplyValues = new double[nAgents];
		
		//calculates total demand
		Iterator<GarbageContainer> itGC = this.garbageContainers.iterator();
		int index = 0;
		double totalDemand = 0;
		while(itGC.hasNext()) {
			GarbageContainer gc = itGC.next();
			this.demandValues[index] = gc.getCurrentOccupation();
			totalDemand += gc.getCurrentOccupation();
			index++;
		}
		this.totalDemand = totalDemand;
		
		//calculates total supply
		Iterator<Truck> itTruck = this.trucks.iterator();
		index = 0;
		double totalSupply = 0;
		while(itTruck.hasNext()){
			Truck t = itTruck.next();
			this.supplyValues[index] = t.getMaxCapacity();
			totalSupply += t.getMaxCapacity();
			index++;
		}
		this.totalSupply = totalSupply;
		
		/*
		 * If the supply and demand are not equal, then we enter in a special case
		 * of a Transportation problem that we'll have to solve by adding one column
		 * or one row to the Transportation matrix.
		 */
		if(this.totalSupply == this.totalDemand){
			
			System.out.println("\n Total Supply: " + this.totalSupply);
			System.out.println(" Total Demand: " + this.totalDemand);
			
			this.costsMatrix = new double[nAgents][nGarbageContainers];
			itTruck = this.trucks.iterator();
			int truckIndex = 0;
			while(itTruck.hasNext()){
				Truck t = itTruck.next();
				Iterator<Goal> itGoals = t.getGoals().iterator();
				int goalIndex = 0;
				while(itGoals.hasNext()){
					Goal g = itGoals.next();
					if(g.getBestPath().getLength() != 0) {
						this.costsMatrix[truckIndex][goalIndex] = g.getBestPath().getLength();
						goalIndex++;
					}
				}
				truckIndex++;
			}
		}
		
		//artificial row with unitary costs equal to 0.
		else if(this.totalDemand > this.totalSupply){
			
			System.out.println("\n Total Supply: " + this.totalSupply);
			System.out.println(" Total Demand: " + this.totalDemand);
			
			int numberNecessarySteps = 0;
			while(!(this.totalDemand == this.totalSupply)) {
				double difference = this.totalDemand - this.totalSupply;
				
				double[] supplyValuesCopy = new double[this.supplyValues.length + 1];
				for(int i = 0; i < this.supplyValues.length; i++){
					supplyValuesCopy[i] = this.supplyValues[i];
				}
				supplyValuesCopy[supplyValuesCopy.length - 1] = difference;
				
				this.supplyValues = new double[supplyValuesCopy.length];
				this.supplyValues = supplyValuesCopy;
				this.totalSupply += difference;
				numberNecessarySteps++;
			}
			
			
			this.costsMatrix = new double[nAgents + numberNecessarySteps][nGarbageContainers];
			itTruck = this.trucks.iterator();
			int truckIndex = 0;
			while(itTruck.hasNext()){
				Truck t = itTruck.next();
				Iterator<Goal> itGoals = t.getGoals().iterator();
				int goalIndex = 0;
				while(itGoals.hasNext()){
					Goal g = itGoals.next();
					if(g.getBestPath().getLength() != 0) {
						this.costsMatrix[truckIndex][goalIndex] = g.getBestPath().getLength();
						goalIndex++;
					}
				}
				truckIndex++;
			}
			
			//last row
			for(int j = 0; j < numberNecessarySteps; j++) {
				for(int i = 0; i < nGarbageContainers; i++){
					this.costsMatrix[nAgents-j][i] = 0;
				}
			}
			
		}
		
		//artificial column with unitary costs equal to 0.
		else {
			
			System.out.println("\n Total Supply: " + this.totalSupply);
			System.out.println(" Total Demand: " + this.totalDemand);
			
			int numberNecessarySteps = 0;
			while(!(this.totalDemand == this.totalSupply)) {
				double difference = this.totalSupply - this.totalDemand;
				
				double[] demandValuesCopy = new double[this.demandValues.length + 1];
				for(int i = 0; i < this.demandValues.length; i++){
					demandValuesCopy[i] = this.demandValues[i];
				}
				demandValuesCopy[demandValuesCopy.length - 1] = difference;
				
				this.demandValues = new double[demandValuesCopy.length];
				this.demandValues = demandValuesCopy;
				this.totalDemand += difference;
				numberNecessarySteps++;
			}
			
			
			this.costsMatrix = new double[nAgents][nGarbageContainers+numberNecessarySteps];
			itTruck = this.trucks.iterator();
			int truckIndex = 0;
			while(itTruck.hasNext()){
				Truck t = itTruck.next();
				Iterator<Goal> itGoals = t.getGoals().iterator();
				int goalIndex = 0;
				while(itGoals.hasNext()){
					Goal g = itGoals.next();
					if(g.getBestPath().getLength() != 0) {
						this.costsMatrix[truckIndex][goalIndex] = g.getBestPath().getLength();
						goalIndex++;
					}
				}
				truckIndex++;
			}
			
			//for last column.
			for(int i = 0; i < nAgents; i++){
				for(int j = 0; j < numberNecessarySteps; j++) {
					this.costsMatrix[i][nGarbageContainers-j] = 0;
				}
			}
		}
	}
	
	
	/*
	 * Applies the Northwest-corner rule to fill the valuesMatrix.
	 */
	private void northwestCornerRule() {
		this.valuesMatrix = new double[this.supplyValues.length][this.demandValues.length];
		
		double[] supplyValuesCopy = new double[this.supplyValues.length];
		double[] demandValuesCopy = new double[this.demandValues.length];
		
		for(int i = 0; i < this.supplyValues.length; i++){
			supplyValuesCopy[i] = this.supplyValues[i];
		}
		
		for(int i = 0; i < this.demandValues.length; i++){
			demandValuesCopy[i] = this.demandValues[i];
		}
		
		for(int i = 0; i < supplyValuesCopy.length; i++){
			for(int j = 0; j < demandValuesCopy.length; j++){
				if(supplyValuesCopy[i] > demandValuesCopy[j]){
					this.valuesMatrix[i][j] = demandValuesCopy[j];
					supplyValuesCopy[i] = supplyValuesCopy[i] - demandValuesCopy[j];
					demandValuesCopy[j] = 0;
				}
				else if(demandValuesCopy[j] > supplyValuesCopy[i]){
					this.valuesMatrix[i][j] = supplyValuesCopy[i];
					demandValuesCopy[j] = demandValuesCopy[j] - supplyValuesCopy[i];
					supplyValuesCopy[i] = 0;
				}
				else {
					this.valuesMatrix[i][j] = supplyValuesCopy[i];
					supplyValuesCopy[i] = 0;
					demandValuesCopy[j] = 0;
				}
			}
		}
	}
	
	
	/*
	 * Checks for Degeneracy (another special case of the Transportation algorithm).
	 */
	private boolean checkDegeneracy() {
		boolean degenerated = false;
		int countBasicVariables = this.supplyValues.length + this.demandValues.length - 1;
		
		//count positive values
		int countPositiveValues = 0;
		for(int i = 0; i < this.valuesMatrix.length; i++){
			for(int j = 0; j < this.valuesMatrix[i].length; j++){
				if(this.valuesMatrix[i][j] > 0){
					countPositiveValues++;
				}
			}
		}
		
		if(countBasicVariables > countPositiveValues)
			degenerated = true;
		
		return degenerated;
	}
	
	
	/*
	 * Corrects Degeneracy.
	 */
	private void correctDegeneracy() {
		int countBasicVariables = this.supplyValues.length + this.demandValues.length - 1;
		
		//count positive values
		int countPositiveValues = 0;
		for(int i = 0; i < this.valuesMatrix.length; i++){
			for(int j = 0; j < this.valuesMatrix[i].length; j++){
				if(this.valuesMatrix[i][j] > 0){
					countPositiveValues++;
				}
			}
		}
		//TODO: the rest of the Degeneracy check.
	}
	
	
	/*
	 * Checks if a variable in position [i][j] is basic.
	 */
	private boolean isBasic(int i, int j){
		if(this.valuesMatrix[i][j] > 0)
			return true;
		return false;
	}
	
	
	/*
	 * Calculate u and v values.
	 */
	private void computeUsAndVs(){
		this.u = new double[this.supplyValues.length];
		this.v = new double[this.demandValues.length];
		
		boolean[] uCalculated = new boolean[this.u.length];
		boolean[] vCalculated = new boolean[this.v.length];
		
		this.u[0] = 0;
		uCalculated[0] = true;
		
		for(int i = 1; i < uCalculated.length; i++){
			uCalculated[i] = false;
		}
		
		for(int j = 0; j < vCalculated.length; j++){
			vCalculated[j] = false;
		}
		
		for(int i = 0; i < this.valuesMatrix.length; i++) {
			for(int j = 0; j < this.valuesMatrix[0].length; j++){
				if(this.isBasic(i, j)){
					if((!vCalculated[j]) && (!uCalculated[i])) continue;
					
					else if( (!vCalculated[j]) && (uCalculated[i])){
						this.v[j] = this.costsMatrix[i][j] - this.u[i];
						vCalculated[j] = true;
					}
					else if((vCalculated[j]) && (!uCalculated[i])){
						this.u[i] = this.costsMatrix[i][j] - this.v[j];
						uCalculated[i] = true;
					}
					else if((!vCalculated[i]) && (uCalculated[j])){
						this.v[i] = this.costsMatrix[i][j] - this.u[j];
						vCalculated[i] = true;
					}
					else if((vCalculated[i]) && (!uCalculated[j])){
						this.u[j] = this.costsMatrix[i][j] - this.v[i];
						uCalculated[j] = true;
					}
					else continue;
				}
				else continue;
			}
			
		}
		
		this.printUs();
		this.printVs();
	}
	
	
	/*
	 * Calculates deltas for the current solution.
	 */
	private double[] computeDeltas() {
		int nonBasicVariableCounter = 0;
		for(int i = 0; i < this.valuesMatrix.length; i++){
			for(int j = 0; j < this.valuesMatrix[0].length; j++){
				if(!this.isBasic(i, j)){
					nonBasicVariableCounter++;
				}
			}
		}
		
		double[] deltas = new double[nonBasicVariableCounter];
		int deltaCounter = 0;
		for(int i = 0; i < this.valuesMatrix.length; i++){
			for(int j = 0; j < this.valuesMatrix[0].length; j++){
				if(!this.isBasic(i, j)){
					deltas[deltaCounter] = this.costsMatrix[i][j] - this.u[i] - this.v[j];
					deltaCounter++;
				}
			}
		}
		
		return deltas;
	}
	
	
	/*
	 * Checks if a parameter-passed array of deltas corresponds to the
	 * optimal solution.
	 */
	private boolean isOptimalSolution(double[] deltas){
		boolean optimal = true;
		for(int i = 0; i < deltas.length; i++){
			if(deltas[i] <= 0){
				optimal = false;
				break;
			}
		}
		return optimal;
	}
	
	
	/*
	 * Gets the minimum value available in the deltas matrix, passed as
	 * parameter.
	 */
	private double getMinimumDelta(double[] deltas){
		double minimum = 100000;
		for(int i = 0; i < deltas.length; i++){
			if(deltas[i] < minimum){
				minimum = deltas[i];
			}
		}
		return minimum;
	}
	
	
	/*
	 * Builds a boolean matrix that in each position has a true value
	 * if its value in valueMatrix corresponds to the lowest delta and a false value
	 * if it doesn't.
	 */
	private boolean[][] buildBooleanDeltaMatrix(double delta){
		boolean[][] booleanDeltaMatrix = new boolean[this.valuesMatrix.length][this.valuesMatrix[0].length];
		
		int nonBasicVariableCounter = 0;
		for(int i = 0; i < this.valuesMatrix.length; i++){
			for(int j = 0; j < this.valuesMatrix[0].length; j++){
				if(!this.isBasic(i, j)){
					nonBasicVariableCounter++;
				}
			}
		}
		
		double[] deltas = new double[nonBasicVariableCounter];
		int deltaCounter = 0;
		boolean stop = false;
		for(int i = 0; i < this.valuesMatrix.length; i++){
			for(int j = 0; j < this.valuesMatrix[0].length; j++){
				if(!this.isBasic(i, j)){
					deltas[deltaCounter] = this.costsMatrix[i][j] - this.u[i] - this.v[j];
					
					if( (deltas[deltaCounter] < 0) && (deltas[deltaCounter] == delta)){
						booleanDeltaMatrix[i][j] = true;
						stop = true;
						break;
					}
					else booleanDeltaMatrix[i][j] = false;
					
					deltaCounter++;
				}
			}
			
			if(stop){
				break;
			}
		}
		
		return booleanDeltaMatrix;
	}
	
	
	/*
	 * Builds a matrix of -1 and 1s called thetaMatrix. The -1 represents
	 * the values that have -%theta to be calculated and the 1 represents
	 * the values that have +%theta to be calculated. All the values that
	 * have 0s in the thetaMatrix are values that will go to the next
	 * iteration unchanged.
	 */
	private int[][] buildThetaMatrix(boolean[][] deltaMatrix){
		int[][] thetaMatrix = new int[deltaMatrix.length][deltaMatrix[0].length];
		
		int indexColumn = -1;
		int indexLine = -1;
		for(int i = 0; i < deltaMatrix.length; i++){
			for(int j = 0; j < deltaMatrix[i].length; j++){
				if(deltaMatrix[i][j]){
					thetaMatrix[i][j] = 1;
					indexLine = i;
					indexColumn = j;
					break;
				}
			}
		}
		
		/*
		 * Put -1 up or down that column
		 */
		
		//if it's not on the first line.
		if(indexLine != 0) {
			
			//if it's not on the last line
			if(indexLine != thetaMatrix.length - 1){
				if(this.isBasic(indexLine + 1, indexColumn)){
					thetaMatrix[indexLine + 1][indexColumn] = -1;
				}
				else if(this.isBasic(indexLine-1, indexColumn)){
					thetaMatrix[indexLine-1][indexColumn] = -1;
				}
				else {
					for(int i = 1; i < thetaMatrix.length-1; i++){
						if(this.isBasic(i, indexColumn)){
							thetaMatrix[i][indexColumn] = -1;
							break;
						}
						else continue;
					}
				}
			}
			
			//if it is on the last line
			else {
				if(this.isBasic(indexLine-1, indexColumn)){
					thetaMatrix[indexLine-1][indexColumn] = -1;
				}
				else {
					for(int i = 1; i < thetaMatrix.length-1; i++){
						if(this.isBasic(i, indexColumn)){
							thetaMatrix[i][indexColumn] = -1;
							break;
						}
						else continue;
					}
				}
			}
		}
		
		//if it's on the first line.
		else {
			if(this.isBasic(indexLine+1, indexColumn)){
				thetaMatrix[indexLine+1][indexColumn] = -1;
			}
			else {
				for(int i = 1; i < thetaMatrix.length-1; i++){
					if(this.isBasic(i, indexColumn)){
						thetaMatrix[i][indexColumn] = -1;
						break;
					}
					else continue;
				}
			}
		}
		
		/*
		 * Put -1 on the right or left column to the one with the 1.
		 */
		int indexLineLastOne = -1;
		int indexColumnLastOne = -1;
		//if it's not on the first column.
		if(indexColumn != 0){
			
			//if it's not on the last column
			if(indexColumn != thetaMatrix[0].length-1){
				if(this.isBasic(indexLine, indexColumn + 1)){
					thetaMatrix[indexLine][indexColumn + 1] = -1;
					indexLineLastOne = indexLine;
					indexColumnLastOne = indexColumn+1;
				}
				
				else if(this.isBasic(indexLine, indexColumn -1)){
					thetaMatrix[indexLine][indexColumn - 1] = -1;
					indexLineLastOne = indexLine;
					indexColumnLastOne = indexColumn-1;
				}
				
				else {
					for(int i = 1; i < thetaMatrix[0].length-1; i++){
						if(this.isBasic(indexLine, i)){
							thetaMatrix[indexLine][i] = -1;
							indexLineLastOne = indexLine;
							indexColumnLastOne = i;
							break;
						}
						else continue;
					}
				}
			}
			
			//if it's on the last column.
			else {
				if(this.isBasic(indexLine, indexColumn-1)){
					thetaMatrix[indexLine][indexColumn-1] = -1;
					indexLineLastOne = indexLine;
					indexColumnLastOne = indexColumn-1;
				}
				else {
					for(int i = 1; i < thetaMatrix[0].length-1; i++){
						if(this.isBasic(indexLine, i)){
							thetaMatrix[indexLine][i] = -1;
							indexLineLastOne = indexLine;
							indexColumnLastOne = i;
							break;
						}
						else continue;
					}
				}
			}
		}
		
		//if it's on the first column
		else {
			if(this.isBasic(indexLine, indexColumn+1)){
				thetaMatrix[indexLine][indexColumn+1] = -1;
				indexLineLastOne = indexLine;
				indexColumnLastOne = indexColumn+1;
			}
			else {
				for(int i = 2; i < thetaMatrix[0].length-1; i++){
					if(this.isBasic(indexLine, i)){
						thetaMatrix[indexLine][i] = -1;
						indexLineLastOne = indexLine;
						indexColumnLastOne = i;
						break;
					}
					else continue;
				}
			}
		}
		
		/*
		 * Last one.
		 */
		if(indexLineLastOne != 0){
			
			//if it's not on the last line.
			if(indexLineLastOne != thetaMatrix.length-1){
				if(this.isBasic(indexLineLastOne-1, indexColumnLastOne)){
					thetaMatrix[indexLineLastOne-1][indexColumnLastOne] = 1;
				}
				
				else if(this.isBasic(indexLineLastOne+1, indexColumnLastOne)){
					thetaMatrix[indexLineLastOne+1][indexColumnLastOne] = 1;
				}
				
				else {
					for(int i = 1; i < thetaMatrix.length; i++){
						if(this.isBasic(i,indexColumnLastOne)){
							thetaMatrix[i][indexColumnLastOne] = 1;
							break;
						}
					}
				}
			}
			
			//if it's on the last line.
			else {
				if(this.isBasic(indexLineLastOne-1, indexColumnLastOne)){
					thetaMatrix[indexLineLastOne-1][indexColumnLastOne] = 1;
				}
				else {
					for(int i = 1; i < thetaMatrix.length-1; i++){
						if(this.isBasic(i,indexColumnLastOne)){
							thetaMatrix[i][indexColumnLastOne] = 1;
							break;
						}
					}
				}
			}
		}
		
		else {
			if(this.isBasic(indexLineLastOne+1, indexColumnLastOne)){
				thetaMatrix[indexLineLastOne+1][indexColumnLastOne] = 1;
			}
			else {
				for(int i = 1; i < thetaMatrix.length; i++){
					if(this.isBasic(i,indexColumnLastOne)){
						thetaMatrix[i][indexColumnLastOne] = 1;
						break;
					}
				}
			}
		}
		
		
		return thetaMatrix;
	}
	
	
	/*
	 * Prints the costsMatrix.
	 */
	public void printCostsMatrix() {
		System.out.println("\nCOSTS MATRIX: ");
		String debugPrinter = "";
		for(int i = 0; i < this.costsMatrix.length; i++){
			debugPrinter += " [";
			for(int j = 0; j < this.costsMatrix[0].length; j++){
				if(j == this.costsMatrix[0].length - 1)
					debugPrinter += this.costsMatrix[i][j];
				else debugPrinter += this.costsMatrix[i][j] + ", ";
			}
			debugPrinter += "]\n";
		}
		System.out.println(debugPrinter);
	}
	
	
	/*
	 * Prints the valuesMatrix.
	 */
	public void printValuesMatrix() {
		System.out.println("\nVALUES MATRIX: ");
		String debugPrinter = "";
		for(int i = 0; i < this.valuesMatrix.length; i++){
			debugPrinter += " [";
			for(int j = 0; j < this.valuesMatrix[0].length; j++){
				if(j == this.valuesMatrix[0].length - 1)
					debugPrinter += this.valuesMatrix[i][j];
				else debugPrinter += this.valuesMatrix[i][j] + ", ";
			}
			debugPrinter += "]\n";
		}
		System.out.println(debugPrinter);
	}
	
	
	/*
	 * Prints the supply values array.
	 */
	public void printSupplyValues() {
		System.out.println("\nSUPPLY VALUES ARRAY: ");
		String debugPrinter = "";
		debugPrinter += " [";
		for(int i = 0; i < this.supplyValues.length; i++){
			if(i == this.supplyValues.length - 1)
				debugPrinter += this.supplyValues[i];
			else debugPrinter += this.supplyValues[i] + ", ";
		}
		debugPrinter += " ]";
		System.out.println(debugPrinter);
	}
	
	
	/*
	 * Prints the demand values array.
	 */
	public void printDemandValues() {
		System.out.println("\nDEMAND VALUES ARRAY: ");
		String debugPrinter = "";
		debugPrinter += " [";
		for(int i = 0; i < this.demandValues.length; i++){
			if(i == this.demandValues.length - 1)
				debugPrinter += this.demandValues[i];
			else debugPrinter += this.demandValues[i] + ", ";
		}
		debugPrinter += " ]";
		System.out.println(debugPrinter);
	}
	
	
	/*
	 * Print u's.
	 */
	public void printUs(){
		System.out.println("\nU's ARRAY: ");
		String debugPrinter = "";
		debugPrinter += " [";
		for(int i = 0; i < this.u.length; i++){
			if(i == this.u.length - 1)
				debugPrinter += this.u[i];
			else debugPrinter += this.u[i] + ", ";
		}
		debugPrinter += " ]";
		System.out.println(debugPrinter);
	}
	
	
	/*
	 * Print v's.
	 */
	public void printVs(){
		System.out.println("\nV's ARRAY: ");
		String debugPrinter = "";
		debugPrinter += " [";
		for(int i = 0; i < this.v.length; i++){
			if(i == this.v.length - 1)
				debugPrinter += this.v[i];
			else debugPrinter += this.v[i] + ", ";
		}
		debugPrinter += " ]";
		System.out.println(debugPrinter);
	}
	
	
	/*
	 * Prints the deltas, passed as parameter.
	 */
	public void printDeltas(double[] deltas){
		System.out.println("\nDELTAS's ARRAY: ");
		String debugPrinter = "";
		debugPrinter += " [";
		for(int i = 0; i < deltas.length; i++){
			if(i == deltas.length - 1)
				debugPrinter += deltas[i];
			else debugPrinter += deltas[i] + ", ";
		}
		debugPrinter += " ]";
		System.out.println(debugPrinter);
	}
	
	
	/*
	 * Print Iteration Header.
	 */
	public void printHeader(int it){
		System.out.println("\n--------------------------\t\t ITERATION " + it + " \t\t--------------------------");
	}
	
	
	/*
	 * Performs the Transportation algorithm.
	 */
	public void performTransportationAlgorithm() {
		this.initialize();
		this.northwestCornerRule();
		if(this.checkDegeneracy()){
			this.correctDegeneracy();
		}
		boolean optimal = false;
		int iteration = 1;
		
		while(!optimal){
			
			this.printHeader(iteration);
			this.printSupplyValues();
			this.printDemandValues();
			this.printCostsMatrix();
			this.printValuesMatrix();
			
			this.computeUsAndVs();
			double[] deltas = this.computeDeltas();
			this.printDeltas(deltas);
			
			boolean[][] booleanDeltas = this.buildBooleanDeltaMatrix(this.getMinimumDelta(deltas));
			int[][] thetasMatrix = this.buildThetaMatrix(booleanDeltas);
			
			optimal = this.isOptimalSolution(deltas);
			iteration++;
		}
	}
	
}
