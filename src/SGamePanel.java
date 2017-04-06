import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Observable;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The panel that displays all visual aspects of the game.
 * The actual frame that the game is played in.
 * Also detects key presses (for moving the player).
 * 
 * @author Adam Cogen
 *
 */
public class SGamePanel extends JFrame {
	private int frameWidth; //the width of the game frame
	private int frameHeight; //the height of the game frame
	private DrawPanel panel; //DrawPanel is an internal class that extends JPanel. The game is drawn here. It is near the bottom of this class.
	private String fileName; //unused field for a String file name
	private int[][] shapeData; //contains the shape data that will be used to draw the map
	private int charX; //the x position of the character, for drawing purposes
	private int charY; //the y position of the character, for drawing purposes
	private int shapeCount; //the shape count, used to initialize the shapeData array
	private int step; //which step of the walking animation is the character on? for drawing purposes
	/*
	 * the hitbox is a red box that indicates the collision area of the player. the center dot marks the 
	 * arm line, which is 1 pixel below the top of the climbability area. the bottom of the climbability 
	 * area is the same as the collision area. this is used for debugging purposes, and to program 
	 * collision detection.
	 */
	private boolean showHitBox = false; //turn hitbox on or off for debugging (see comment directly above this one)
	private int leftCollisionOffset = 1; //used to calibrate where the left side of the player is located
	private int rightCollisionOffset = 8; //used to calibrate where the right side of the player is located
	private int upCollisionOffset = -22; //used to calibrate where the top of the player is located
	private int downCollisionOffset = -1; //used to calibrate where the bottom of the player is located

	/**
	 * 
	 * Instantiates the SGamePanel class and initializes necessary fields.
	 * 
	 * @param game The SGame object which this SGamePanel is instantiated by.
	 * This will be passed to the internal observable KeyPressNotifier class, 
	 * where it will be added as an observer.
	 */
	public SGamePanel(SGame game){
		panel = new DrawPanel();
		frameHeight = 0;
		frameWidth = 0;
		shapeCount = 0;
		step = 0;
		this.setResizable(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		/*
		 * Instantiate the KeyPressNotifier internal class, 
		 * which will be observable by the SGame, which this 
		 * SGamePanel object is instantiated by. 
		 * The KeyPressNotifier will be used to notify the game
		 * when a key has been pressed, since only the SGamePanel
		 * has the ability to detect key presses (since it extends 
		 * JFrame).
		 */

		KeyPressNotifier pressNotifier = new KeyPressNotifier();

		/*
		 * Pass in the SGame that this SGamePanel is instantiated by,
		 * and has as a field.
		 * This will act as an observer. 
		 */

		pressNotifier.setGame(game);

		/**
		 * Handles key presses within the game.
		 * 
		 * @author adamcogen
		 *
		 */
		class KeyPress implements KeyListener{
			/**
			 * not implemented
			 * @param e
			 */
			@Override
			public void keyTyped(KeyEvent e) {
				//nothin	
			}
			/**
			 * Handles all key presses, and calls a method within
			 * the KeyPressNotifier class (internal class within the 
			 * SGamePanel) that will then notify the SGame that a key
			 * has been pressed. From there, the SGame will perform 
			 * the appropriate functions.
			 * @param e the key that was pressed (usually one of the arrow keys, except for a possible debug function on the shift key)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT: //left key pressed
					//moveChar("left");
					pressNotifier.keyPressChange('l');
					break;
				case KeyEvent.VK_UP: //up key pressed
					//moveChar("up");
					pressNotifier.keyPressChange('u');
					break;
				case KeyEvent.VK_RIGHT: //right key pressed
					//moveChar("right");
					pressNotifier.keyPressChange('r');
					break;
				case KeyEvent.VK_DOWN: // down key pressed
					//moveChar("down");
					pressNotifier.keyPressChange('d');
					break;
				case KeyEvent.VK_SHIFT: // shift key pressed
					//moveChar("shift");
					pressNotifier.keyPressChange('s');
					break;
				}
			}
			/**
			 * This is not used for anything yet, but just in case
			 * it is eventually useful, the KeyPressNotifier class 
			 * also notifies the SGame about key releases. 
			 */
			@Override
			public void keyReleased(KeyEvent e) {
				pressNotifier.keyPressChange('n');
			}
		}
		this.addKeyListener(new KeyPress());
	}

	/**
	 * read a number from the shape array.
	 * @param rectangle the number of the shape to read
	 * @param value the index of the value to read, 0-8
	 * @return the value at the specified rectangle
	 */
	public int readNum(int rectangle, int value){
		return shapeData[rectangle][value];
	}

	/**
	 * DrawPanel extends JPanel.
	 * Render all shapes within the shape array, and draw the character, 
	 * with a different pose depending which stage of the walking 
	 * animation it is on.
	 * Some shapes are drawn before the character, and some after, as 
	 * specified by the map file, so that they will appear either
	 * in front of or behind the sprite.
	 * @author Adam Cogen
	 *
	 */
	class DrawPanel extends JPanel{
		public void paintComponent(Graphics g){
			int red = 0;
			int green = 0;
			int blue = 0;

			//draw all rectangles that go behind player, and no-collision ovals that go behind player
			for (int i = 0; i < shapeCount; i++){

				/*
				 * read each color from the file, and call a method that 
				 * corrects any colors that are outside of the range 0 to 255
				 */
				red = fixColorRange(readNum(i, 6));
				green = fixColorRange(green = readNum(i, 7));
				blue = fixColorRange(readNum(i, 8));

				//all behind rectangles
				if(readNum(i, 4) != 2 && readNum(i, 4) != 3 && readNum(i, 4) != 4 && readNum(i, 4) != 5){
					g.setColor(new Color(red, green, blue));
					g.fillRect(readNum(i, 0), readNum(i, 1), readNum(i, 2), readNum(i, 3));
				}
				//draw no-collision behind ovals
				if(readNum(i,4) == 3){
					g.setColor(new Color(red, green, blue));
					g.fillOval(readNum(i, 0), readNum(i, 1), readNum(i, 2), readNum(i, 3));
				}
			}

			//draw the character
			g.setColor(Color.black);
			if (step == 0){
				g.drawString("H", charX, charY);
				g.drawString("- -", charX - 5, charY - 6);
			} else {
				g.drawString("X", charX, charY);
				g.drawString("~ ~", charX - 5, charY - 6);
			}
			g.drawString("O", charX, charY - 5);
			g.drawString("o", charX + 2, charY - 15);

			//draw the hitbox if it is turned on
			if (showHitBox){
				g.setColor(Color.red);
				//left
				g.drawLine(charX + leftCollisionOffset, charY + downCollisionOffset, charX +leftCollisionOffset, charY + upCollisionOffset);
				//right
				g.drawLine(charX + rightCollisionOffset, charY + downCollisionOffset, charX + rightCollisionOffset, charY + upCollisionOffset);
				//up
				g.drawLine(charX + leftCollisionOffset, charY + upCollisionOffset, charX + rightCollisionOffset, charY + upCollisionOffset);
				//down
				g.drawLine(charX + leftCollisionOffset, charY + downCollisionOffset, charX + rightCollisionOffset, charY + downCollisionOffset);
				//center dot (at arm line)
				g.drawLine(charX + 4, charY + -10, charX + 5, charY + -10);
			}

			//draw no-collision rectangles and ovals that go in front of the character
			for (int i = 0; i < shapeCount; i++){

				/*
				 * read each color from the file, and call a method that 
				 * corrects any colors that are outside of the range 0 to 255
				 * (no colors should be outside of that range, but if for some
				 * reason they are, the game will correct them and will still 
				 * run).
				 */
				red = fixColorRange(readNum(i, 6));
				green = fixColorRange(green = readNum(i, 7));
				blue = fixColorRange(readNum(i, 8));

				//draw no-collision, in-front-of-character rectangles
				if(readNum(i, 4) == 2){
					g.setColor(new Color(red, green, blue));
					g.fillRect(readNum(i, 0), readNum(i, 1), readNum(i, 2), readNum(i, 3));
				}
				//draw no-collision, in-front-of-character ovals
				if(readNum(i,4) == 4){
					g.setColor(new Color(red, green, blue));
					g.fillOval(readNum(i, 0), readNum(i, 1), readNum(i, 2), readNum(i, 3));
				}
			}
		}
		
		/**
		 * Correct any values that are not within the range 0 to 255, inclusive.
		 * Values less than 0 will become 0, values greater than 255 will become 255.
		 * Meant to make sure that color RGB values are valid.
		 * 
		 * @param value The original value as an int
		 * @return int: the value adjusted so that 0 <= value <= 255
		 */
		public int fixColorRange(int value){
			if (value > 255){
				value = 255;
			} else if (value < 0){
				value = 0;
			}
			return value;
		}
	}
	
	/**
	 * Refresh the size of the panel and frame.
	 * This will be called when initializing the 
	 * game or when changing to a map of a different
	 * size than the map before it.
	 */
	public void refreshSize(){
		panel.setPreferredSize(new Dimension(frameWidth, frameHeight));
		this.add(panel);
		this.pack();
	}
	
	/**
	 * Set the shape data for the panel, which holds
	 * all information about shapes to be rendered on
	 * the map. The shapes will be drawn within the
	 * SGamePanel class. 
	 * @param data int[][]: the shape data for the current map
	 */
	public void setShapeData(int[][] data){
		shapeData = data;
	}
	
	/**
	 * Set the current height of the game / frame. 
	 * This will be called when initializing the 
	 * game or when changing to a map of a different
	 * size than the map before it.
	 * @param height the height of the game / frame for the current map
	 */
	public void setFrameHeight(int height){
		frameHeight = height;
	}
	
	/**
	 * Set the current width of the game / frame. 
	 * This will be called when initializing the 
	 * game or when changing to a map of a different
	 * size than the map before it.
	 * @param width the width of the game / frame for the current map
	 */
	public void setFrameWidth(int width){
		frameWidth = width;
	}
	
	/**
	 * Repaint the frame, thus refreshing the image displayed in-game.
	 * This will be necessary after changes in position of the player,
	 * or when drawing new shapes on the map (when initializing game
	 * or changing maps).
	 */
	public void refreshImage(){
		this.repaint();
	}
	
	/**
	 * Set the x position of the character, as it is to be drawn
	 * @param val int: the character's x position, as it is to be drawn
	 */
	public void setCharX(int val){
		charX = val;
	}
	
	/**
	 * Set the y position of the character, as it is to be drawn
	 * @param val int: the character's y position, as it is to be drawn
	 */
	public void setCharY(int val){
		charY = val;
	}
	
	/**
	 * Set the shape count, for shape rendering purposes
	 * @param val int: the shape count
	 */
	public void setShapeCount(int val){
		shapeCount = val;
	}
	
	/**
	 * Set which step of the walk animation the player is currently on, for drawing purposes
	 * @param val int: which step of the walking animation the character is on
	 */
	public void setStep(int val){
		step = val;
	}
	
	/**
	 * Return the height of the game / frame, for edge-warp detection purposes within the SGame class
	 * @returnhe int: the height of the game / frame
	 */
	public int getFrameHeight(){
		return frameHeight;
	}
	/**
	 * Return the width of the game / frame, for edge-warp detection purposes within the SGame class
	 * @return int: the width of the game / frame
	 */
	public int getFrameWidth(){
		return frameWidth;
	}
	
	/**
	 * Since Java does not support double inheritance and the SGamePanel already extends 
	 * JFrame, this class extends Observable and notifies the main Game class whenever a
	 * key is pressed or released.  The direction n represents no direction being pressed,
	 * u represents up, d represents down, l represents left, and r represents right.
	 * @author adamcogen
	 *
	 */
	public class KeyPressNotifier extends Observable {
		
		private char currentDirection; //what was the most recent key event sent to the KeyPressNotifier?
		
		/**
		 * Initialize the KeyPressNotifier. 'n' represents a key release, so this is an appropriate value
		 * to initialize the currentDirection field with.
		 */
		public KeyPressNotifier(){
			currentDirection = 'n';
		}
		
		/**
		 * This is the method that is called when a key press happens within the SGamePanel.
		 * This is where observers of the KeyPressNotifier are notified. This method passes
		 * information about which key was pressed as a parameter to the observer (the SGame
		 * class), where it can be processed.
		 * @param char: the direction that was pressed within the SGamePanel. 
		 */
		public void keyPressChange(char dir){
			if (dir == 'l'){ //left arrow pressed
				currentDirection = 'l';
			} else if (dir == 'r'){ //right arrow pressed
				currentDirection = 'r';
			} else if (dir == 'u'){ //up arrow pressed
				currentDirection = 'u';
			} else if (dir == 'd'){ //down arrow pressed
				currentDirection = 'd';
			} else if (dir == 's'){ //shift key pressed
				currentDirection = 's';
			} else if (dir == 'n'){ //no key pressed, or key released 
				currentDirection = 'n';
			}
			setChanged();
			notifyObservers(currentDirection);
			//System.out.println("ya");
		}
		
		/**
		 * Pass in an SGame, which will act as the observer for the KeyPressNotifier.
		 * @param game The SGame that will act as an observer and will process and act on key presses.
		 */
		public void setGame(SGame game){
			addObserver(game);
		}

	}
}
