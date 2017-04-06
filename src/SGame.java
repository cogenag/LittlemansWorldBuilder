import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Timer;

/**
 * The game class for Littleman, which handles distribution and management of all 
 * information to other classes necessary for the game to run. All information 
 * comes through the game class, and is either used there or distributed to 
 * somewhere else. All other classes are instantiated here. This class also 
 * handles such features as gravity. 
 * 
 * changelog:
 * 1.1: read in-map-warps
 * 1.2: acceleration due to gravity, more neatly organized classes, 
 * 		new climbability value (jumpable climb), improved collision
 * 		and climbability checking system, implemented warps to maps of 
 *		different dimensions
 *
 * @version Version 1.2, Wednesday, March 29, 2017
 * @author Adam Cogen
 *
 */
public class SGame implements Observer {
	private int map = 4; //current map number
	private SFileRead sFile; //the class that will read data stored in map files
	private SMap sMap; //the class that will store map data 
	private SGamePanel sPanel; //the class that will display the game and sense key presses
	private SChar sChar; //the class that will store information about the player and the sprite
	private int moveSize; //used to check collision before moving to a new location that is distance moveSize away
	private int jumpStep; //keeps track of which step of the jump animation the player is on during jumps
	private Timer jumpTimer; //the timer which will start during jumps, starting the jump animation 
	private Timer fallTimer; //the timer that will start while falling short distances. at a certain speed, a different faster timer starts
	private Timer fastFallTimer; //the faster timer, which handles falling over longer distances and at higher speed
	boolean fastFall = false; //is the fast fall timer running? if so, this will be true. otherwise, false.
	private int partialJumpHeight; // one third of the jump height, used to increment char Y on each step of jumpTimer, which has three steps.
	private int partialMoveSize; //one third of total move size
	private int edgeWarpOffsetL = 15; //used to calibrate the position of edge warp on the left side of the map
	private int edgeWarpOffsetR = 5; //used to calibrate the position of edge warp on the right side of the map
	private int edgeWarpOffsetU = 2; //used to calibrate the position of edge warp at the top of the map
	private int edgeWarpOffsetD = 22; //used to calibrate the position of edge warp at the bottom of the map
	private int leftCollisionOffset = 1; //used to fix the discrepancy between the player's x position and its left side, for collision etc.
	private int rightCollisionOffset = 8; //used to fix the discrepancy between the player's x position and its right side, for collision etc.
	private int upCollisionOffset = -22; //used to fix the discrepancy between the player's y position and its top, for collision etc.
	private int downCollisionOffset = -1; //used to fix the discrepancy between the player's y position and its bottom, for collision etc.
	/*
	 * how acceleration-due-to-gravity works:
	 * ~   gravityStart = GRAVITY_INITIAL_SPEED;
	 * ~   every fallTimer clock tick, use a while loop to 
	 *     move down 1 pixel (int)(gravityStart / GRAVITY_DIVIDER) times.
	 * ~   every iteration through the while loop, gravityStart increments
	 *     by GRAVITY_ACCELERATION, so that the value of 
	 *     (int)(gravityStart / GRAVITY_DIVIDER) becomes larger every clock tick. 
	 *     this means that for every clock tick, the player moves down 1 pixel at
	 *     time, a greater number of times (the while loops runs more times).
	 *~    gravityStart will stop incrementing once 
	 *     (int)(gravityStart / GRAVITY_DIVIDER) equals TERMINAL_VELOCITY.
	 *~    every time the player hits water, climbable, or solid ground, 
	 *     the variable gravityStart resets back to GRAVITY_INITIAL_SPEED
	 *     because their fall is broken.
	 *     FALL_TIMER_FREQUENCY and JUMP_TIMER_FREQUENCY are the time in milliseconds
	 *     between clock ticks of the fall timer and jump timer.
	 *~    the boolean field normWarpResetsGravity determines whether going through
	 *     an in-map warp resets your fall speed back to the initial value.
	 *     if this is false, you will reach terminal velocity and continue 
	 *     falling at that speed until your fall is broken, even if you reach
	 *     an in-map warp.
	 *~    the boolean field edgeWarpResetsGravity determines the same thing,
	 *     but for edge warps (e.g. 'down') instead of in-map warps. 
	 *~    to turn off acceleration-due-to-gravity, make sure that 
	 *     GRAVITY_INITIAL_SPEED / GRAVITY_DIVIDER = 2 and 
	 *     GRAVITY_ACCLERATION = 0.
	 *~    Default values for gravity-on: initial_speed = 4, divider = 2,
	 *     acceleration = .2, fall and jump timers = 120, terminal velocity = 15,
	 *     normWarpResets = false, edgeWarpResets = false.
	 *     
	 *     new notes, about fast fall timer: 
	 *     all of the above is still true, except now the values to change to adjust 
	 *     gravity are the lower set of values, the "fastFallTimer stuff."
	 *     the normal-speed fall is characterised by 120 millisecond increments.
	 *     so when the player is on the ground and they jump up then land,
	 *     the way up is drawn in 120 millisecond increments, and so is the way down.
	 *     the problem was that once the player started accelerating, the 120
	 *     milllisecond increments weren't enough for the faster falling, so the player
	 *     appeared to "lag," or move too fast, with large spatial gaps between 
	 *     each time he was redrawn. simply changing the original fallTimer 
	 *     increments to a faster time meant that a jump-then-land, on solid 
	 *     ground, looked very weird: the way up was 120 millisecond increments, and
	 *     the way down was 30 millisecond increments, so it looked too high resolution.
	 *     fastFall is a new feature which preserves the asthetic of the older,
	 *     slower fall, while still allowing for acceleration that does not cause 
	 *     the falling player to look "laggy." it does this by turning off the original
	 *     fallTimer after the player reaches a certain speed, and instead turning on 
	 *     fastFallTimer, which operates in 30 millisecond increments instead of
	 *     120 millisecond increments. 
	 */
	private static final double GRAVITY_INITIAL_SPEED = 4; //real intitial speed is (int)(GRAVITY_INITIAL_SPEED / GRAVITY_DIVIDER)
	private double gravityStart = GRAVITY_INITIAL_SPEED; //gravityStart will be incremented to increase fall speed over time
	private static final double GRAVITY_DIVIDER = 2;  //actual initial speed will be (int)(GRAVITY_INITIAL_SPEED / GRAVITY_DIVIDER)
	private static final double GRAVITY_ACCELERATION = .2; //gravityStart increments by this much with every clock tick, speeding up fall over time
	private static final int FALL_TIMER_FREQUENCY = 120; //frequency of fallTimer in milliseconds
	private static final int JUMP_TIMER_FREQUENCY = 120; //frequency of jumpTimer in milliseconds
	private boolean normWarpResetsGravity = false; //setting. does an in-map-warp reset fall speed? 
	private boolean edgeWarpResetsGravity = false; //setting. does an edge-warp reset fall speed?

	//fastFallTimer stuff:
	private static final int FAST_FALL_TIMER_FREQUENCY = 30; //frequency of the fastFallTimer, for faster fall speeds
	private static final double FAST_GRAVITY_INITIAL_SPEED = 8; //real intitial speed is (int)(FAST_GRAVITY_INITIAL_SPEED / FAST_GRAVITY_DIVIDER)
	private double fastGravityStart = FAST_GRAVITY_INITIAL_SPEED; //fastGravityStart will be incremented to increase fall speed over time
	private static final double FAST_GRAVITY_DIVIDER = 6;  //real intitial speed is (int)(FAST_GRAVITY_INITIAL_SPEED / FAST_GRAVITY_DIVIDER)
	private static final double FAST_GRAVITY_ACCELERATION = .2; //fastGravityStart increments by this much with every clock tick, speeding upfall over time
	int accelCount = 1; //accelCount increments until it equals accelAt, at which point fast fall speed accelerates by FAST_GRAVITY_ACCELERATION
	int accelAt = 1; //can be chan
	
	private static final int TERMINAL_VELOCITY = 8; //terminal velocity (in pixels-per-fastFallTimer-clock-tick)

	/**
	 * The SGame class consolidates all information 
	 * from other classes and keeps track of everything.
	 * It is basically the main control center of the game. 
	 */
	public SGame(int mapNumber) {
		//map = mapNumber; //comment this to use map number from fields, uncomment to use map number from main method

		partialJumpHeight = 1;
		partialMoveSize = 1;
		jumpStep = 3;
		moveSize = 3;

		sPanel = new SGamePanel(this);
		sChar = new SChar();
		changeMap(map); //initializes necessary classes and information to load a map, put into a method for use with both constructor and warps
		setCharX(sFile.getSpawnX());
		setCharY(sFile.getSpawnY());


		/**
		 * Timer listener is involved with the 'up' direction of moveChar.
		 * Reads and increments the jumpStep variable, which keeps track of 
		 * which step of the jump (step 0, step 1, step 2, etc.) we are on.
		 * stops the jump at step 4 (there are only 3 steps). moves char's
		 * Y position in a different way depending on which step we are one,
		 * then repaints the panel after each step is complete.
		 * @author adamcogen
		 *
		 */
		class TimerListenerUp implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * since the first two steps of the jump are identical,
				 * if we just used the jumpStep variable in the switch
				 * statement, we would need to rewrite the exact same
				 * code twice for case 0 and case 1. the switchJumpStep
				 * variable's value is based upon the jumpStep value,
				 * but it is logically processed so that the same code
				 * can be used for both jumpStep == 0 and jumpStep == 1.
				 */
				int switchJumpStep = 2;
				if(jumpStep == 0 || jumpStep == 1){
					switchJumpStep = 0;
				} else if (jumpStep == 2){
					switchJumpStep = 1;
				} else if (jumpStep == 3){
					switchJumpStep = 2;
				}
				switch(switchJumpStep){
				case 0:
					if (checkCollision('d', sChar.getX(), sChar.getY() - moveSize)){
						setCharY(sChar.getY() - 3);
						if (sChar.getY() <= 0 - edgeWarpOffsetU){
							edgeWarp('u');
						}
					}
					break;
				case 1:
					jumpTimer.stop();
					setStep(0);
					checkFall();
					break;
				}
				jumpStep++;
				//				if((checkClimb() == 3) && sChar.getStep() == 1){
				//					sChar.incStep();
				//				}
				refreshChar();
				sPanel.refreshImage();
			}
		}
		jumpTimer = new Timer(JUMP_TIMER_FREQUENCY, new TimerListenerUp());

		/**
		 * Timer that during a fall after a certain velocity is reached.
		 * Handles fast falling and has a higher rate of occurrence so that
		 * the fall movement appears smoother.
		 * 
		 * @author adamcogen
		 *
		 */
		class TimerListenerDownFast implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * If the player is not on the ground, move down. Try this twice. 
				 * Repeating this step is better than moving down all at once because 
				 * it checks the ground below the player more often, preventing the 
				 * player from falling too far into the ground. 
				 */
				for (int i = 0; i < ((int) (fastGravityStart / FAST_GRAVITY_DIVIDER)); i++){
					//System.out.println((int) (gravityStart / GRAVITY_DIVIDER));
					if(!isOnGround(sChar.getX(), sChar.getY()) && checkClimb() == 0){
						setCharY(sChar.getY() + partialJumpHeight);
						if (sChar.getY() >= sPanel.getFrameHeight() + edgeWarpOffsetD){
							//if the player is off the edge of the map, edgeWarp down.
							edgeWarp('d');
						}

						/*
						 * If the player has not reached terminal velocity, increase the fall speed.
						 * accelCount and accelAt would the fall to only accelerate once per  certain
						 * number of timer ticks, but it is not currently implemented.
						 */
						if (fastGravityStart <= TERMINAL_VELOCITY * FAST_GRAVITY_DIVIDER && accelCount == accelAt){
							fastGravityStart += FAST_GRAVITY_ACCELERATION;
							accelCount = 0;
						} else {
							accelCount++;
						}
					}
				}
				/*
				 * Perform the same step one more time. If the player was in "water" or on 
				 * something climbable this whole time, none of these steps were
				 * performed, but this is addressed next.
				 */
				boolean problem = (checkClimb() == 0);
				//for(int i = 0; i < Integer.MAX_VALUE; i++);
				if (!isOnGround(sChar.getX(), sChar.getY()) && checkClimb() == 0){
					setCharY(sChar.getY() + partialJumpHeight);
					if (sChar.getY() >= sPanel.getFrameHeight() + edgeWarpOffsetD){
						edgeWarp('d');
					}
				}
				/*
				 * if the fastFallTimer gets to the else statement, it just means player isn't freefalling anymore,
				 * so stop the timer.
				 */
				else {
					fastFallTimer.stop();
					fastFall = false;
					setStep(0);
					resetGravity();
				}
				refreshChar();
				sPanel.refreshImage();
			}
		}
		fastFallTimer = new Timer(FAST_FALL_TIMER_FREQUENCY, new TimerListenerDownFast());

		/**
		 * Timer listener is involved with the 'down' direction of move().
		 * Handles short jumps, such as jumping while on solid ground.
		 * Once a fall accelerates to a certain speed, this timer stops
		 * and the fastFallTimer starts. 
		 * 
		 * @author Adam Cogen
		 *
		 */
		class TimerListenerDown implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * If the player is not on the ground, move down. Try this twice. 
				 * Repeating this step is better than moving down all at once because 
				 * it checks the ground below the player more often, preventing the 
				 * player from falling too far into the ground. 
				 */
				for (int i = 0; i < ((int) (gravityStart / GRAVITY_DIVIDER)); i++){
					//System.out.println((int) (gravityStart / GRAVITY_DIVIDER));
					if(!isOnGround(sChar.getX(), sChar.getY()) && checkClimb() == 0 && !fastFall){
						setCharY(sChar.getY() + partialJumpHeight);
						if (sChar.getY() >= sPanel.getFrameHeight() + edgeWarpOffsetD){
							//if the player is off the edge of the map, edgeWarp down.
							edgeWarp('d');
						}

						/*
						 * If the player has not reached terminal velocity, increase the fall speed
						 * by the gravity acceleration constant.
						 */
						if (gravityStart <= TERMINAL_VELOCITY * GRAVITY_DIVIDER){
							gravityStart += GRAVITY_ACCELERATION;
						}
						/*
						 * if the fall speed is greater than 3 pixels per clock tick, stop the fallTimer
						 * and start the fastFallTimer.
						 */
						if (gravityStart / GRAVITY_DIVIDER >= 3){
							fastFall = true;
							fastFallTimer.start();
							fallTimer.stop();
						}
					}
				}
				/*
				 * Perform the same step one more time. If the player was in "water" or on 
				 * something climbable this whole time, none of these steps were
				 * performed, but this is addressed next.
				 */
				boolean problem = (checkClimb() == 0);
				//for(int i = 0; i < Integer.MAX_VALUE; i++);
				if (!isOnGround(sChar.getX(), sChar.getY()) && checkClimb() == 0 && !fastFall){
					setCharY(sChar.getY() + partialJumpHeight);
					if (sChar.getY() >= sPanel.getFrameHeight() + edgeWarpOffsetD){
						edgeWarp('d');
					}
					/*
					 * If the player is in water, move it down one increment here.
					 * This way, in water, the player moves down one increment 
					 * per timer tick, rather than 3 increments if it was falling 
					 * in air. 
					 */
				} else if (!isOnGround(sChar.getX(), sChar.getY()) && checkClimb() == 2){
					setCharY(sChar.getY() + partialJumpHeight);
					if (sChar.getY() >= sPanel.getFrameHeight() + edgeWarpOffsetD){
						edgeWarp('d');
					}
					resetGravity();
					/*
					 * If for some reason the fastFall timer has started but the fallTimer
					 * is still running (this is possible by jumping in rapid succession),
					 * the fallTimer will still be stopped here, so nothing will go wrong.
					 */
				} else if (fastFall){
					fallTimer.stop();
					/*
					 * If none of the previous cases are true, the fall timer should 
					 * stop, as the player has landed on solid ground (or on something
					 * climbable).
					 */
				} else {
					fallTimer.stop();
					setStep(0);
					resetGravity();
				}
				refreshChar();
				sPanel.refreshImage();
			}
		}

		fallTimer = new Timer(FALL_TIMER_FREQUENCY, new TimerListenerDown());

		checkFall();

	}

	/**
	 * Check if the player is on the ground or on something climbable.
	 * If not, start the fall timer, unless the fastFallTimer is already
	 * running.
	 */
	public void checkFall(){
		int currentX = sChar.getX();
		int currentY = sChar.getY();
		if(!isOnGround(currentX, currentY) && checkClimb() != 1 && checkClimb() != 3){
			//System.out.println("fall timer started");
			if(!fastFall){
				fallTimer.start();
			}
		}
	}

	/**
	 * Move the player in a specified direction if 
	 * that movement is possible. This is called
	 * after an arrow key is pressed. Different 
	 * directions have different implications 
	 * (up can start the jumpTimer, start "climbing,"
	 * or start "swimming," depending on the climbability
	 * of the current location).
	 * @param dir A character specifying the direction to move.
	 * 'l' = left, 'r' = right, 'u' = up, 'd' = down.
	 */
	public void move(char dir){

		boolean moved = false; //this will be used to make it so that the legs only move when the player actually moves

		if (dir == 'r'){ //if the right arrow was pressed
			/*
			 * each direction's movements are 3 pixels, and operate on a for loop, which moves
			 * one pixel at a time. this allows for more accurate collision detection, 
			 * allowing the player to move only 1 or 2 pixels if a wall is 1 or 2 pixels 
			 * away. this prevents strange looking gaps between the player and the wall.
			 */
			for(int i = 0; i < 3; i++){
				if(checkCollision('r', sChar.getX() + partialMoveSize, sChar.getY())){
					moved = true; 
					setCharX(sChar.getX() + partialMoveSize);
				}
				//check if the player is off the map and needs to be edge-warped
				if (sChar.getX() >= sPanel.getFrameWidth() + edgeWarpOffsetR){
					edgeWarp(dir);
				}
			}

		} else if (dir == 'l'){ //if the left arrow was pressed
			//see comment in (dir == 'r') for explanation about for loop
			for(int i = 0; i < 3; i++){
				if(checkCollision('l', sChar.getX() - partialMoveSize, sChar.getY())){
					setCharX(sChar.getX() - partialMoveSize);
					moved = true; 
				}
				//check if the player is off the map and needs to be edge-warped
				if (sChar.getX() <= 0 - edgeWarpOffsetL){
					edgeWarp(dir);
				}
			}
		} else if (dir == 'u'){ //if the up arrow was pressed
			/*
			 * up option 1: you are on the ground, in water, or on jumpable climb. 
			 * there is room to jump without hitting something. start the jump timer,
			 * which will allow you to jump.
			 */
			if(( (isOnGround(sChar.getX(), sChar.getY()) && checkClimb() == 0) || checkClimb() == 2 || checkClimb() == 3) && checkCollision('u', sChar.getX(), sChar.getY() - (3 *partialJumpHeight)) && jumpStep == 3){
				//System.out.println("ya");
				moved = true;
				fallTimer.stop();
				setCharY(sChar.getY() - (3 * partialJumpHeight));
				setStep(0);
				jumpStep = 0;
				jumpTimer.start();
			} 
			/*
			 * up option 2: you are not necessarily on the ground, but there is room 
			 * to jump without hitting something, and  you can climb. Move the char
			 * up by the move size, similar to how you would move left or right,
			 * without starting the jumpTimer.
			 */
			else if((checkClimb() == 1)){
				for(int i = 0; i < 3; i++){
					if (checkCollision('u', sChar.getX(), sChar.getY() - partialJumpHeight)){
						moved = true;
						setCharY(sChar.getY() - partialJumpHeight); //non-stylized jump
						//check if the player is off the map and needs to be edge-warped
						if (sChar.getY() <= 0 - edgeWarpOffsetU){
							edgeWarp(dir);
						}
					}
				}
			} 
		} else if (dir == 'd'){ //if the down arrow was pressed
			/*
			 * If there is room to move down without colliding with something,
			 * (this happens in the air, in water, or in something climbable)
			 * then move the character down.
			 */
			for(int i = 0; i < 3; i++){
				if((checkCollision('d', sChar.getX(), sChar.getY() + partialMoveSize) && jumpStep == 3)){
					moved = true;
					setCharY(sChar.getY() + partialMoveSize);
					//check if the player is off the map and needs to be edge-warped
					if (sChar.getY() >= sPanel.getFrameHeight() + edgeWarpOffsetD){ 
						edgeWarp(dir);
					}
				}
			}
		} else if (dir == 's'){ //if right shift key was pressed
			//do nothing. this can be implemented with various debug functions if necessary
		}
		if (moved){
			sChar.incStep();
		}
		refreshChar();
		sPanel.refreshImage();
		checkClimb();

		//check if the character is on stable ground after moving
		if (jumpStep == 3){
			/*
			 * note: jumpStep will only equal 3 when the jumpTimer is not currently running.
			 * this prevents the fallTimer from starting in checkFall() while the player is
			 * still on the way up in a jump. 
			 */
			checkFall();
		}

	}

	/**
	 * This observer is notified by the sGamePanel whenever an
	 * arrow key is pressed. The move method is then called based
	 * on which arrow key was pressed. 
	 */
	@Override
	public void update(Observable o, Object arg) {
		if ((char) arg == 'l'){ //left arrow was pressed
			move('l');
		} else if ((char) arg == 'r'){ //right arrow was pressed
			move('r');
		} else if ((char) arg == 'u'){ //up arrow was pressed
			move('u');
		} else if ((char) arg == 'd'){ //down arrow was pressed
			move('d');
		} else if ((char) arg == 's'){ //shift key was pressed
			//move('s'); //this can by implemented for various debug functions if needed
		} else if ((char) arg == 'n'){ //key was released
			//no use for this yet
		}
	}
	/**
	 * Consolidate all char data; update the x, y, and step fields within the sPanel.
	 * This is called whenever a change is made to the player's x position or y position
	 * ( it is called within setCharX(), setCharY() ) so that other classes that rely on 
	 * this information (sPanel) will know that it has changed. 
	 */
	public void refreshChar(){
		sPanel.setStep(sChar.getStep());
		sPanel.setCharX(sChar.getX());
		sPanel.setCharY(sChar.getY());
	}

	/**
	 * This changes the current map to a specified map,
	 * and initializes all necessary classes and fields.
	 * This is called for in-map-warps, edge-warps,
	 * and within the constructor (in this case, 
	 * the initial game map is the parameter). 
	 * @param newMap the map to change to
	 */
	public void changeMap(int newMap){
		sFile = new SFileRead(newMap);
		sMap = new SMap(newMap);

		//initialize sMap with sFile data
		//shape data
		sMap.setShapeData(sFile.getRectangleData());
		sMap.setShapeCount(sFile.getShapeCount());
		//edgewarps
		sMap.setEdgeWarpDown(sFile.getEdgeWarpDown());
		sMap.setEdgeWarpLeft(sFile.getEdgeWarpLeft());
		sMap.setEdgeWarpRight(sFile.getEdgeWarpRight());
		sMap.setEdgeWarpUp(sFile.getEdgeWarpUp());
		//normWarps
		sMap.setWarpCount(sFile.getWarpCount());
		sMap.setWarpList(sFile.getWarpList());

		//initialize sPanel with sFile data
		//shape data
		sPanel.setShapeData(sFile.getRectangleData());
		sPanel.setShapeCount(sFile.getShapeCount());
		//game size
		sPanel.setFrameHeight(sFile.getFrameHeight());
		sPanel.setFrameWidth(sFile.getFrameWidth());
		//char step
		sPanel.setStep(sChar.getStep());
		//initialize and refresh panel
		refreshChar();
		sPanel.refreshImage();
		sPanel.refreshSize();

	}

	/**
	 * perform an edge warp in the specified direction.
	 * @param direction char representing the direction 
	 * 		  to perform the edge warp in. 
	 * 		  'l' is left, 'r' is right, 'u' is up, 'd' 
	 * 		  is down.
	 */
	public void edgeWarp(char direction){
		if (direction == 'l'){ //left
			map = sMap.getEdgeWarpLeft();
			changeMap(map);
			setCharX(sPanel.getFrameWidth() + edgeWarpOffsetR);
		} else if (direction == 'r'){ //right
			map = sMap.getEdgeWarpRight();
			changeMap(map);
			setCharX(0 - edgeWarpOffsetL);
		} else if (direction == 'u'){ //up
			map = sMap.getEdgeWarpUp();
			changeMap(map);
			setCharY(sPanel.getFrameHeight() + edgeWarpOffsetD);
		} else if (direction == 'd'){ //down
			map = sMap.getEdgeWarpDown();
			changeMap(map);
			setCharY(0 - edgeWarpOffsetU);
		}
		refreshChar();
		if(edgeWarpResetsGravity){
			resetGravity();
		}
		checkFall();
	}

	/**
	 * checks the climbability of the current position by calling
	 * SMap.getClimb(x, y). returns an int.
	 * Also handles calls to normWarp(), since climbability and in-map-warp
	 * numbers are stored in the same value in map files. 
	 * 
	 * @return the climbability of current spot or the normWarp value at that spot.
	 *		   0 = not climbable. 1 = climbable. 2 = watery. 
	 * 		   value greater than or equal to 10 represents
	 * 		   an in-map-warp, with the number (climbability value - 10).
	 * 
	 * 
	 * in-map-warp takes first priority, then jump climbable, 
	 * then ladder climbable, then water, then cantclimb.
	 * 
	 */
	public int checkClimb(){
		/*
		 * 
		 * These work by checking the climbability in lines forming a box
		 * on each side of the player. If any side has something climbable,
		 * then the player can climb. Different types of climbability
		 * take priority over others (for instance if you can climb, there
		 * is no need to sink in water). Look at the main comment for this
		 * method to see priority order.
		 * 
		 * The top of the climbable range on the character is slightly above its
		 * arms (because it has short arms, and you can't climb something with
		 * your head).
		 * 
		 * note: anywhere that -11 apppears near a sChar.getY() value, it is used 
		 * to put the top bound of climb sensing at the arm height of the player
		 * 
		 * To see the climbability box, go to the SGamePanel class and set the 
		 * boolean field showHitBox to true before starting the game. The red 
		 * dot in the middle of the character is one pixel below the top of
		 * the climbability range.
		 * 
		 */
		int priority = 0;
		int climb;
		int i;
		//left
		i = downCollisionOffset;
		while(i >= -11){ //upCollisionOffset){ 
			climb = sMap.getClimb(sChar.getX() + leftCollisionOffset, sChar.getY() + i);
			if (climb >= 10) {
				priority = climb;
			} else if(climb == 3 && (priority == 0 || priority == 2 || priority == 1)){
				priority = 3;
			} else if(climb == 1 && (priority == 0 || priority == 2)){
				priority = 1;
			} else if (climb == 2 && priority == 0){
				priority = 2;
			}
			i--;
		}
		//right
		i = downCollisionOffset;
		while(i >= -11){ //upCollisionOffset){
			climb = sMap.getClimb(sChar.getX() + rightCollisionOffset, sChar.getY() + i);
			if (climb >= 10) {
				priority = climb;
			} else if(climb == 3 && (priority == 0 || priority == 2 || priority == 1)){
				priority = 3;
			} else if(climb == 1 && (priority == 0 || priority == 2)){
				priority = 1;
			} else if (climb == 2 && priority == 0){
				priority = 2;
			}
			i--;
		}
		//up
		i = leftCollisionOffset;
		while(i <= rightCollisionOffset){
			climb = sMap.getClimb(sChar.getX() + i, sChar.getY() + -11); //upCollisionOffset);
			if (climb >= 10) {
				priority = climb;
			} else if(climb == 3 && (priority == 0 || priority == 2 || priority == 1)){
				priority = 3;
			} else if(climb == 1 && (priority == 0 || priority == 2)){
				priority = 1;
			} else if (climb == 2 && priority == 0){
				priority = 2;
			}
			i++;
		}
		//down
		i = leftCollisionOffset;
		while(i <= rightCollisionOffset){
			climb = sMap.getClimb(sChar.getX() + i, sChar.getY() + downCollisionOffset);
			//climb = sMap.getClimb(sChar.getX() + i, sChar.getY() + downCollisionOffset + 1); //check the spot below player. this makes you unable to jump on ladder climbable and water
			if (climb >= 10) {
				priority = climb;
			} else if(climb == 3 && (priority == 0 || priority == 2 || priority == 1)){
				priority = 3;
			} else if(climb == 1 && (priority == 0 || priority == 2)){
				priority = 1;
			} else if (climb == 2 && priority == 0){
				priority = 2;
			}
			i++;
		}

		if (priority >= 10){
			normWarp(priority - 10);
		}

		return priority;
	}


	/**
	 * change the player's x position, then update it 
	 * in all classes that need to know it has changed.
	 * @param newX the new x position for the player, as an int
	 */
	public void setCharX(int newX){
		sChar.setX(newX);
		refreshChar();
	}

	/**
	 * change the player's x position, then update it 
	 * in all classes that need to know it has changed.
	 * @param newY the new y position for the player, as an int
	 */
	public void setCharY(int newY){
		sChar.setY(newY);
		refreshChar();
	}

	/**
	 * change the player's step variable, then update it 
	 * in all classes that need to know it has changed.
	 * @param newStep the new step value, as an int
	 */
	public void setStep(int newStep){
		sChar.setStep(newStep);
		refreshChar();
	}

	/**
	 * Perform an in-map warp, or "normal warp", which can be placed
	 * anywhere in the map.
	 * Normal warps specify which map they lead to, and at what x and y 
	 * positions. This method reads those values one at a time from
	 * warpList[][] within the SMap class, then makes appropriate changes
	 * to the game to warp the player to the specified destination.
	 * @param warpNumber
	 */
	public void normWarp(int warpNumber){
		changeMap(sMap.getNormWarpValue(warpNumber, 0));
		setCharX(sMap.getNormWarpValue(warpNumber, 1));
		setCharY(sMap.getNormWarpValue(warpNumber, 2));
		if(normWarpResetsGravity){
			resetGravity();
		}
		checkFall();
	}

	/**
	 * this method works by checking collision at every pixel in a 
	 * straight line on the specified side. All of the lines form
	 * a line around the character. To see the collision box, 
	 * go to the SGamePanel class and set the boolean field 
	 * showHitBox to true before starting the game.
	 * If the player moves left, it is necessary to check 
	 * left collision, etc. 
	 * @param side: char representing which side to check ('l' left, 'r' right, 'u' up, 'd' down)
	 * @param x: the x value to check
	 * @param y: the y value to check
	 * @return boolean: true if the player can go there, false if they can't
	 */
	public boolean checkCollision(char side, int x, int y){
		int i = 0;
		if(side == 'l'){
			//System.out.println("left");
			i = downCollisionOffset;
			while(i >= upCollisionOffset){
				if (!sMap.getCollision(x + leftCollisionOffset + 1, y + i)){
					return false;
				}
				i--;
			}
		} else if (side == 'r'){
			//System.out.println("right");
			i = downCollisionOffset;
			while(i >= upCollisionOffset){
				if (!sMap.getCollision(x + rightCollisionOffset - 1, y + i)){
					return false;
				}
				i--;
			}
		} else if (side == 'u'){
			//System.out.println("up");
			i = leftCollisionOffset + 1;
			while(i <= rightCollisionOffset - 1){
				if (!(sMap.getCollision(x + i, y + upCollisionOffset - 1))){
					return false;
				}
				i++;
			}
		} else if (side == 'd'){
			//System.out.println("down");
			i = leftCollisionOffset + 1;
			while(i <= rightCollisionOffset - 1){
				if (!(sMap.getCollision(x + i, y + downCollisionOffset + 1))){
					return false;
				}
				i++;
			}
		}
		return true;
	}

	/**
	 * this is here to improve readability of the code, so that it is easy to see when 
	 * we are checking whether the character is on the ground or not. Calling isOnGround
	 * is a lot easier to remember and read than a call to and negation of checkCollision.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isOnGround(int x, int y){
		return !checkCollision('d', x, y);
	}

	/**
	 * When the player hits the ground, something climbable, or water, this is called.
	 * Resets necessary values so that fall acceleration, etc. is reset for the next 
	 * fall.
	 */
	public void resetGravity(){
		gravityStart = GRAVITY_INITIAL_SPEED;
		fastGravityStart = FAST_GRAVITY_INITIAL_SPEED;
		accelCount = 0;
	}

	//	/**
	//	 * This is meant to find the current position of a certain side of the player,
	//	 * taking necessary calibration into account. It is currently not used, but may 
	//	 * be useful later.
	//	 * @param edge: char, which edge to check. 'l' left, 'r' right, 'u' up, 'd' down.
	//	 * @return int: the position of that side (x position for left and right, y position for top and bottom)
	//	 */
	//	public int getCharPosition(char edge){
	//		int position = 0;
	//		if (edge == 'l'){
	//			position = sChar.getX() + leftCollisionOffset;
	//		} if (edge == 'r'){
	//			position = sChar.getX() + rightCollisionOffset;
	//		} if (edge == 'u'){
	//			position = sChar.getY() + upCollisionOffset;
	//		} if (edge == 'd'){
	//			position = sChar.getY() + downCollisionOffset;
	//		}
	//		return position;
	//	}
	
	/**
	 * Instantiate the SGame class.
	 * @param args: int indicating which map number to start the game on (this parameter is not currently implented)
	 */
	public static void main(String [] args){
		SGame game = new SGame(12);
	}
	
}

