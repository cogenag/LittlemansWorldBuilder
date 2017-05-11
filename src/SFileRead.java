import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
/**
 * 
 * Reads all necessary information from a map file, including the spawn position, 
 * the shape data, the warp data. This information can then be read by the game
 * class, and distributed to other necessary classes. 
 * @version Version 1.2
 * @author Adam Cogen
 *
 */
public class SFileRead {
	private int map; //the map number
	private int spawnX; //the x value of the spawn point, as read from the file
	private int spawnY; //the x value of the spawn point, as read from the file
	private int[][] fileShapeData; //stores all of the shape data from the file. first value is the shape number, second value is the specific piece of data from that shape
	private boolean hasEdgeWarpLeft; //does this map have a left edge warp?
	private boolean hasEdgeWarpRight; //does this map have a right edge warp?
	private boolean hasEdgeWarpUp; //does this map have a up edge warp?
	private boolean hasEdgeWarpDown; //does this map have a down edge warp?
	private int edgeWarpLeft; //if there is a left edge warp, what is it? if there is not, it is the letter "n." 
	private int edgeWarpRight; //if there is a right edge warp, what is it? if there is not, it is the letter "n." 
	private int edgeWarpUp; //if there is a up edge warp, what is it? if there is not, it is the letter "n." 
	private int edgeWarpDown; //if there is a down edge warp, what is it? if there is not, it is the letter "n." 
	private int edgeWarpConstant; //a constant meant to allow for the programmer to change how far the sprite should be before an edgewarp happens. all sides are equalized within the program, so changing the value of this field will change all sides evenly.
	private int shapeCount; //how many shapes are in the file?
	private int warpCount; //how many warps are there within the map?
	private int[][] warpList; //the list of in-map-warps
	private int frameWidth; //the width of the frame
	private int frameHeight; //the height of the frame
	private String fileName; //the name of the file to read from

	/**
	 * reads all of the values from a map file at once. 
	 * these values can then be accessed by the other classes.
	 * @param mapNumber the map number to read
	 */
	public SFileRead(int mapNumber){
			map = mapNumber;
			fileName = "maps/" + map + ".txt";
			setShapeCount();
			fillMapArray();
			getEdgeWarps();
			getSpawn();
			gameDimensions();
	}
	/**
	 * read all shapes from the map file and store them in an array, fileRectangleData[shape number][specific data value index].
	 * this method also checks warp count, and fills the warp array. i put that in here since this method already reads through
	 * the whole map array. a standalone method for warp data would have to burn through all the other shapes with a for loop
	 * anyway, might as well only have to do that once.
	 */
	public void fillMapArray(){
		fileShapeData = new int[shapeCount][9];
		Scanner scan1;
		try {
			/*
			 * skip over values that are not needed for the rectangle data array, which only holds shape data
			 */
			scan1 = new Scanner(new FileReader(fileName));
			scan1.nextLine();
			scan1.nextLine();
			scan1.nextLine();
			scan1.nextLine();
			//fill the rectangle data array
			for (int i = 0; i < shapeCount; i++){
				for(int j = 0; j < 9; j++){
					fileShapeData[i][j] = scan1.nextInt();	
				}
			}
			//fill the warpList
			warpCount = scan1.nextInt();
			warpList = new int[warpCount][3];
			for (int i = 0; i < warpCount; i++){
				for (int j = 0; j < 3; j++){
					String value = scan1.next();
					if(value.equals("n")){
						warpList[i][j] = map;
					} else{
						warpList[i][j] = Integer.parseInt(value);
						//System.out.println(value);
					}
				}
			}

		} catch (FileNotFoundException fnfe){
			System.out.println("Map file not found in SFileRead class fillMapArray() method");
		}
	}

	/**
	 * read the shape count from the map file.
	 * @return the shape count
	 */
	public void setShapeCount(){
		shapeCount = 0;
		Scanner scan1;
		try {
			scan1 = new Scanner(new FileReader(fileName));
			scan1.nextLine();
			scan1.nextLine();
			scan1.nextLine();
			shapeCount = scan1.nextInt();
		} catch (FileNotFoundException fnfe) {
			System.out.println("map file not found in SFileRead class rectangleCount() method");
		}
	}

	/**
	 * read the edge warps from the file.
	 * most are just numbers, indicating
	 * the map number to warp to.
	 * 'n' represents that there is 
	 * no edge warp in a particular direction,
	 * so the player should just warp to the
	 * opposite side of the current map. 
	 * 
	 */
	public void getEdgeWarps(){
		Scanner scan1;
		try {
			scan1 = new Scanner(new FileReader(fileName));
			scan1.nextLine();
			scan1.nextLine();
			String left = scan1.next();
			if (left.equals("n")){
				hasEdgeWarpLeft = false;
				edgeWarpLeft = map;
			} else {
				hasEdgeWarpLeft = true;
				edgeWarpLeft = Integer.parseInt(left);
			}
			String right = scan1.next();
			if (right.equals("n")){
				hasEdgeWarpRight = false;
				edgeWarpRight = map;
			} else {
				hasEdgeWarpRight = true;
				edgeWarpRight = Integer.parseInt(right);
			}
			String up = scan1.next();
			if (up.equals("n")){
				hasEdgeWarpUp = false;
				edgeWarpUp = map;
			} else {
				hasEdgeWarpUp = true;
				edgeWarpUp = Integer.parseInt(up);
			}
			String down = scan1.next();
			if (down.equals("n")){
				hasEdgeWarpDown = false;
				edgeWarpDown = map;
			} else {
				hasEdgeWarpDown = true;
				edgeWarpDown = Integer.parseInt(down);
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println("no map file found in SFileRead class getEdgeWarps() method");
		}
	}
	/**
	 * read the game dimensions from the file. 
	 */
	public void gameDimensions(){
		int width = 0;
		int height = 0;
		String fileName = "maps/" + map + ".txt";
		Scanner scan1;
		try {
			scan1 = new Scanner(new FileReader(fileName));
			width = scan1.nextInt();
			height = scan1.nextInt();
			frameHeight = height;
			frameWidth = width;
		} catch (FileNotFoundException fnfe) {
			System.out.println("no map file found for map in SFileRead class gameDimensions() method");
		}
	}
	/**
	 * read the spawn point from the file.
	 */
	public void getSpawn(){
		String fileName = "maps/" + map + ".txt";
		Scanner scan1;
		try {
			scan1 = new Scanner(new FileReader(fileName));
			scan1.nextLine();
			spawnX = scan1.nextInt();
			spawnY = scan1.nextInt();
		} catch (FileNotFoundException fnfe) {
			System.out.println("no map file found in SFileRead class getSpawn() method");
		}
	}

	/**
	 * Return the X position of the spawn point
	 * @return int: X posiiton of spawn point
	 */
	public int getSpawnX(){
		return spawnX;
	}
	/**
	 * Return the Y position of the spawn point
	 * @return int: Y posiiton of spawn point
	 */
	public int getSpawnY(){
		return spawnY;
	}
	
	/**
	 * Return the height of the game / frame
	 * @return int: the height of the game / frame 
	 */
	public int getFrameHeight(){
		return frameHeight;
	}
	
	/**
	 * Return the width of the game / frame 
	 * @return int: the width of the game / frame 
	 */
	public int getFrameWidth(){
		return frameWidth;
	}
	
	/**
	 * Return the map number of the left edge warp
	 * @return int: the map number of the left edge warp
	 */
	public int getEdgeWarpLeft(){
		return edgeWarpLeft;
	}
	
	/**
	 * Return the map number of the right edge warp
	 * @return int: the map number of the right edge warp
	 */
	public int getEdgeWarpRight(){
		return edgeWarpRight;
	}
	
	/**
	 * Return the map number of the bottom edge warp
	 * @return int: the map number of the bottom edge warp
	 */
	public int getEdgeWarpDown(){
		return edgeWarpDown;
	}
	
	/**
	 * Return the map number of the top edge warp
	 * @return int: the map number of the top edge warp
	 */
	public int getEdgeWarpUp(){
		return edgeWarpUp;
	}
	
	/**
	 * Return the shape count, the number of shapes to be rendered
	 * @return int: the shape count
	 */
	public int getShapeCount(){
		return shapeCount;
	}
	
	/**
	 * Return the warp count, the number of in-map-warps on the map
	 * @return int: the warp count
	 */
	public int getWarpCount(){
		return warpCount;

	}
	
	/**
	 * Return the shape data, the 2 dimensional array that contains
	 * all information about each shape in the game
	 * @return int[][]: the shape data 
	 */
	public int[][] getRectangleData(){
		return fileShapeData;

	}
	
	/**
	 * Return the warp data, the 2 dimensional array that contains
	 * all information about each in-map-warp in the game
	 * @return int[][]: the warp data 
	 */
	public int[][] getWarpList(){
		return warpList;
	}
}
