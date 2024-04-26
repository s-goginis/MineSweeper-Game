import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

// The main board for Minesweeper, displaying grid of square cells
public class MinesweeperBoard extends JPanel implements MouseListener {
	private static final int BOARD_SIZE = 15;
	private static final int NUMBER_OF_MINES = 30;

	// Color of flagged squares
	private static final Color LIGHT_RED = new Color(255, 85, 85);    

	private static final int HIDDEN = 0; 
	private static final int REVEALED = 1;
	private static final int FLAGGED_AS_MINED = 2;

	// tells the state of the cell in the given row and column: HIDDEN, REVEALED,or FLAGGED_AS_MINED.
	private int[][] state;  

	// Displays a message to the user
	private JLabel message;  
	private boolean[][] mines;

	// Constructor 
	public MinesweeperBoard() {
		mines = new boolean[BOARD_SIZE][BOARD_SIZE];
		setPreferredSize( new Dimension( 30 * BOARD_SIZE, 30 * BOARD_SIZE ) );
		// Arrange for this board to respond to mouse clicks.
		addMouseListener(this);  
		// Create and configure the message label.
		message = new JLabel();  
		message.setBackground(Color.LIGHT_GRAY);
		message.setOpaque(true);
		message.setForeground(Color.RED);
		message.setFont(new Font("SansSerif", Font.BOLD, 18));
		message.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		setFont(new Font("Serif", Font.BOLD, 16));
		newGame();  // Start the first game.
	}

	// Used by the main program to get reference to the message label,
	public JLabel getMessageLabel() {
		return message;
	}


	// Called to start a new game.
	public void newGame() {
		message.setText("Flags Left:  " + NUMBER_OF_MINES + "/" + NUMBER_OF_MINES);
		state = new int[BOARD_SIZE][BOARD_SIZE];
		mines = new boolean[BOARD_SIZE][BOARD_SIZE];
		for (int i = 0; i < BOARD_SIZE * 2; i++) {
			int row = (int) (Math.random() * BOARD_SIZE);
			int col = (int) (Math.random() * BOARD_SIZE);
			if (!mines[row][col]) {
				mines[row][col] = true;
			}
			else {
				i--;
			}
		}
		repaint();
	}


	// Draws the board, using all the state variables
	protected void paintComponent(Graphics g) {
		double squareWidth = (double)getWidth() / BOARD_SIZE;
		double squareHeight = (double)getHeight() / BOARD_SIZE;
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				int y1 = (int) (row * squareHeight);
				int x1 = (int) (col * squareWidth);
				int y2 = (int) ((row + 1) * squareHeight);
				int x2 = (int) ((col + 1) * squareWidth);
				int width = x2 - x1;
				int height = y2 - y1;

				if (state[row][col] == HIDDEN) {
					g.setColor(Color.GRAY);
				}
				else if (state[row][col] == FLAGGED_AS_MINED) {
					g.setColor(LIGHT_RED);
				}
				else {
					g.setColor(Color.LIGHT_GRAY);
				}
				g.fillRect(x1, y1, width, height);
				g.setColor(Color.BLACK);
				g.drawRect(x1, y1, width, height);
				if (state[row][col] == REVEALED) {
					if (mines[row][col]) {
						g.setColor(LIGHT_RED);
						g.fillRect(x1,y1,width,height);
						g.setColor(Color.BLACK);
						g.drawString("*",x1+9,y1+24);
					}
				
					if (!mines[row][col] && countMines(row,col) > 0) {
						g.setColor(Color.BLACK);
						g.drawString("" + countMines(row,col),x1+6,y1+21);
					}
				}
			}
		}
	}

	// Called when the user clicks the mouse to initiate action
	public void mousePressed(MouseEvent evt) {
		int row;
		int col;
		
		// figure out where the user clicked
		double squareWidth = (double)getWidth() / BOARD_SIZE;
		double squareHeight = (double)getHeight() / BOARD_SIZE;
     	row = (int)( evt.getY() / squareHeight );
     	col = (int)( evt.getX() / squareWidth );

     	// check if user right-clicked or shift-clicked, is so, then flag
     	if (evt.getButton() == MouseEvent.BUTTON3 || evt.isShiftDown()) {
     		if (state[row][col] == FLAGGED_AS_MINED) {
     			state[row][col] = HIDDEN;
     		}
     		else if (state[row][col] == HIDDEN && countFlagged() < NUMBER_OF_MINES) {
     			state[row][col] = FLAGGED_AS_MINED;
     		}
     		
     		message.setText("Flags Left:  " + (NUMBER_OF_MINES - countFlagged()) + "/" + NUMBER_OF_MINES);
     	}
     	
     	// a normal left click
     	else {
     		state[row][col] = REVEALED;
     		message.setText("Flags Left:  " + (NUMBER_OF_MINES - countFlagged()) + "/" + NUMBER_OF_MINES);
     		if (countMines(row,col) == 0) {
     			unhide(row,col);
     		}
     		if (mines[row][col]) {
     			for (int i = 0; i < BOARD_SIZE; i++) {
     				for (int j = 0; j < BOARD_SIZE; j++) {
     					state[i][j] = REVEALED;
     				}
     			message.setText("GAME OVER!");
     			}
     		}
     		else if (countRevealed() + countFlagged() == BOARD_SIZE * BOARD_SIZE) {
     			message.setText("YOU WON!");
     		}
     	}

     	// Redraw board to show effects of user click
     	repaint();
	}

	// Flooding
	public void unhide(int row, int col) {
		ArrayList<Location> adj = getAdjacentSquares(row,col);
		for (Location e: adj) {
			if (countMines(e.getRow(),e.getCol()) == 0 && state[e.getRow()][e.getCol()] == HIDDEN) {
				state[e.getRow()][e.getCol()] = REVEALED;
				unhide(e.getRow(),e.getCol());
			}
			state[e.getRow()][e.getCol()] = REVEALED;
		}
	}

	// Counts the number of mines next to a given space
	public int countMines(int row, int col) {
		int count = 0;

		for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if(i >= 0 && i < BOARD_SIZE && j >= 0 && j < BOARD_SIZE) {
                    if(mines[i][j]) {
                        count++;
                    }
                }
            }
        }
        return count;
	}
	

	// Counts the number of hidden mines left
	public int countRevealed() {
		int count = 0;
		
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (state[i][j] == REVEALED) {
					count++;
				}
			}
		}
		return count;
	}

	//Counts the number of flagged mines
	public int countFlagged() {
		int count  = 0;
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (state[i][j] == FLAGGED_AS_MINED) {
					count++;
				}
			}
		}
		return count;
	}

	//Gets the adjacent squares that are not mines
	public ArrayList<Location> getAdjacentSquares(int row, int col) {
		ArrayList<Location> adj = new ArrayList<Location>();
		if (row > 0) {
			adj.add(new Location(row-1,col));
		}
		if (col > 0) {
			adj.add(new Location(row,col-1));
		}
		if (row < BOARD_SIZE-1) {
			adj.add(new Location(row+1,col));
		}
		if (col < BOARD_SIZE-1) {
			adj.add(new Location(row,col+1));
		}
		if (row > 0 && col > 0) {
			adj.add(new Location(row-1,col-1));
		}
		if (row < BOARD_SIZE-1 && col < BOARD_SIZE-1) {
			adj.add(new Location(row+1,col+1));
		}
		if (row > 0 && col < BOARD_SIZE-1) {
			adj.add(new Location(row-1,col+1));
     	}
		if (row < BOARD_SIZE-1 && col > 0) {
			adj.add(new Location(row+1,col-1));
		}
		adj.removeIf(e -> mines[e.getRow()][e.getCol()]);
		return adj;
	}

	// required methods for a MouseListener

	public void mouseReleased(MouseEvent evt) { }
	public void mouseClicked(MouseEvent evt) { }
	public void mouseEntered(MouseEvent evt) { }
	public void mouseExited(MouseEvent evt) { }
   
}
