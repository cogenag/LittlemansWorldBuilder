import java.awt.Graphics;

import javax.swing.*;

/**
 * This is going to be a launcher that can start the game,
 * open the map maker, and change various game settings, 
 * including the initial map number.
 * It is not fully implemented yet.
 * 
 * @author Adam Cogen
 *
 */
public class SLauncher extends JFrame {
	private static final int FRAME_HEIGHT = 600;
	private static final int FRAME_WIDTH = 600;
	private LPanel launchPanel;
	private JButton playButton;
	private JButton quitButton;
	private JTextField mapNumberTextBox;
	
	/**
	 * Unfinished constructor
	 */
	public SLauncher() {
		this.setSize(500, 500);
		this.setTitle("Littleman");
		launchPanel = new LPanel();
		playButton = new JButton("Play");
		quitButton = new JButton("Quit");
		mapNumberTextBox = new JTextField();
		launchPanel.add(playButton);
		launchPanel.add(quitButton);
		this.add(launchPanel);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	public static void main(String [] args){
		SLauncher launch = new SLauncher();
	}
	
	/**
	 * Extends JPanel, will be used to make any drawn images that will appear in the launcher
	 * 
	 * @author Adam Cogen
	 *
	 */
	private class LPanel extends JPanel{
		public void paintComponent(Graphics g){
			
		}
	}
	
}
