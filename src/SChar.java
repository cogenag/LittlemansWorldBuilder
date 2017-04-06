/**
 * Stores all information related to the player sprite,
 * including x and y position and which step of the walking
 * animation the player is on.
 * 
 * This class has a lot unused functionality that is not implemented
 * for anything yet.
 * 
 * @version Version 1.2
 * @author Adam Cogen
 *
 */
public class SChar {
	private int xPos; //current x position of the char
	private int yPos; //current y position of the char
	private int step; //which step of the walking animation is the char on?
	private int stepMax; //what is the number of steps in the walking animation? If there is 1 step, stepMax = 0, etc.
	private int moveSize; //how large is each movement of the char? how many pixels
	
	/**
	 * initialize the char, set all necessary values.
	 * @param x starting x position of character
	 * @param y starting y position of character
	 */
	public SChar (int x, int y){
		xPos = x;
		yPos = y;
		step = 0;
		stepMax = 1;
		moveSize = 3;
	}
	/**
	 * initialize the char, set all necessary values.
	 * X and Y position are not specified, and are 
	 * (0,0) until setX(), setY, setXY, etc are
	 * called.
	 */
	public SChar(){
		xPos = 0;
		yPos = 0;
		step = 0;
		stepMax = 1;
		moveSize = 3;
	}
	/**
	 * Get current x position of the player
	 * @return int: current x position of the player
	 */
	public int getX(){
		return xPos;
	}
	/**
	 * Get current y position of the player
	 * @return int: current y position of the player
	 */
	public int getY(){
		return yPos;
	}
	/**
	 * Set x position of the player
	 * @param int x: new x position of the player
	 */
	public void setX(int x){
		xPos = x;
	}
	/**
	 * Set x position of the player
	 * @param int x: new x position of the player
	 */
	public void setY(int y){
		yPos = y;
	}
	/**
	 * increment player's x position by the number of pixels
	 * stored in moveSize
	 */
	public void incX(){
		xPos = xPos + moveSize;
	}
	/**
	 * increment player's y position by the number of pixels
	 * stored in moveSize
	 */
	public void incY(){
		yPos = yPos + moveSize;
	}
	/**
	 * decrement player's x position by the number of pixels
	 * stored in moveSize
	 */
	public void decX(){
		xPos = xPos - moveSize;
	}
	/**
	 * decrement player's y position by the number of pixels
	 * stored in moveSize
	 */
	public void decY(){
		yPos = yPos - moveSize;
	}
	/**
	 * Set the characters x and y coordinates at the same time.
	 * @param int x: the new x position
	 * @param int y: the new y position
	 */
	public void setXY(int x, int y){
		xPos = x;
		yPos = y;
	}
	/**
	 * Get which step of the walking animation the sprite is on. 
	 * @return int: which step of the walking animation the sprite is on 
	 */
	public int getStep(){
		return step;
	}
	/**
	 * Set which step of the walking animation the sprite is on. 
	 * @param int newstep: new value of step 
	 */
	public void setStep(int newstep){
		step = newstep;
	}
	/**
	 * Increment which step of the walking animation the sprite is  
	 * on. If it is at the last step of the animation, set it back
	 * to the first step.
	 */
	public void incStep(){
		if (step < stepMax){
			step++;
		} else {
			step = 0;
		}
	}
}
