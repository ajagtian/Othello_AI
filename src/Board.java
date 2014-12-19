
import java.util.ArrayList;
import java.util.Collections;

/**
 * 
 * @author akshayjagtiani
 *
 */
public class Board{	
	private char [][] board;													/*	current state of the game in the form of 2x2 Matrix			*/
	private char ply;															/*	next player - Black | White									*/
	private final int SIZE = 8;													/*	board size, fixed to 8x8									*/
	private final Disk diskSet;													/*	disks - black, white, empty									*/
	private double [][] heuristicMatrix;										/*	contains HEURISTIC ON POSITION SCORE for each block			*/
	private ArrayList<double []> traverseLog = new ArrayList<double[]>();		/*	contains the traverse trajectory of the algorithm			*/
	
	public Board() {
		board = new char[SIZE][SIZE];											/*	use setter method to init									*/
		diskSet = Disk.getInstance();											/*	get instance of disk set for current game					*/
		heuristicMatrix = new double[SIZE][SIZE];								/*	use the setter method to set heuristics						*/
	}
	
	private boolean outOfBoard(int row,int col) {
		return (row < 0 || col < 0 || row >= SIZE || col >= SIZE);
	}
	
	private ArrayList<Child> findCandidateChildrenFor(char ply, char [][] _board,boolean max) {
		int _row,_col,row_lookahead,col_lookahead,row,col;
		//	clear any previous moves and move paths;
		ArrayList<Child> children = new ArrayList<Child>();
		char opponent = (ply == diskSet.getWhite())?diskSet.getBlack():diskSet.getWhite();
		//	for each block on the board, check if it can be a candidate move for the current ply
		for (_row = 0 ; _row < SIZE ; _row++) {
			for(_col = 0 ; _col < SIZE ; _col++) {
				//	if the current position is occupied, it can not be a candidate for next ply
				if(_board[_row][_col] != diskSet.getEmpty()){
					continue;
				}
				//	if current position is empty, check the adjacent blocks for compatibility in every direction, 
				//	if compatibility is found in any direction, mark the cell as compatible and break search in that direction. 
				for (row_lookahead = -1 ; row_lookahead <= 1 ; row_lookahead++) {
					for (col_lookahead = -1 ; col_lookahead <= 1 ; col_lookahead++) {
						//	next adjacent row,col
						row = _row+row_lookahead;
						col = _col+col_lookahead;
						//	no need to check outside the board or on the current position
						if(outOfBoard(row, col) || (row_lookahead == 0 && col_lookahead == 0)) {
							continue;
						}
						if(_board[row][col] == opponent){
							//	if an opponent is found on one of the adjacent boxed, check deeper
							while(true) {
								row += row_lookahead;
								col += col_lookahead;
								if(outOfBoard(row, col)) {
									break;
								}
								if(_board[row][col] == diskSet.getEmpty()) {
									break;
								}
								//	if any such condition occurs down the search path - opponent{1}opponent{0,}player{1,} 
								//	--> means current position is a candidate move
								if(_board[row][col] == ply) {
									//	possible candidate move -> including duplicates
									//	spawn a child
									if(max)
										children.add(new Child(_row, _col, row, col,Double.POSITIVE_INFINITY));
									else
										children.add(new Child(_row, _col, row, col,Double.NEGATIVE_INFINITY));
									break;
								}
							}	
						}
					}
				}
			}
		}
		return children;
	}
	
	/********************************************************************************/
	/* To make a move																*/
	/* 1-> select a point where a disk has to be placed.							*/
	/* 2-> select the ply															*/
	/* And all the possible disks will be flipped									*/
	/********************************************************************************/
	/**
	* @param ply																	
	* @param source_row															
	* @param source_col															
	* @return a shadow of board state with the new move being played				
	*/
	public char[][] getChildStateForThisChild(char ply, int child_row,int child_col, char [][] _board) {
		int rowLookAhead,colLookAhead,row,col;
		char opponent = ply == diskSet.black?diskSet.white:diskSet.black;
		//	create a temporary lookahead board instance.
		char [][] childState = copyByValue(_board);
		//	set the current block to ply
		childState[child_row][child_col] = ply;
		//	flip the disks that get captured
		for (rowLookAhead = -1 ; rowLookAhead <= 1 ; rowLookAhead++) {
			for (colLookAhead = -1 ; colLookAhead <= 1 ; colLookAhead++) {
				row = child_row + rowLookAhead;
				col = child_col + colLookAhead;
				if(outOfBoard(row, col) || (rowLookAhead == 0 && colLookAhead == 0)) {
					continue;
				}
				if (childState[row][col] == opponent) {
					while (true) {
						row += rowLookAhead;
						col += colLookAhead;					
						if(outOfBoard(row, col)) {
							break;
						}
						if(childState[row][col] == diskSet.empty) {
							break;
						}
						if(childState[row][col] == ply) {
							//	a move has been detected
							//	traverse in the reverse direction and make all the opponent => ply
							row -= rowLookAhead;
							col -= colLookAhead;
							while(childState[row][col] == opponent) {
								childState[row][col] = ply;
								row -= rowLookAhead;
								col -= colLookAhead;
							}
							break;
						}
					}
				}
			}
		}
		// the new board instance with the move been made.
		return childState;
	}
	
	private char[][] copyByValue(char[][] _board) {
		char boardInstance[][] = new char[SIZE][SIZE];
		for (int i = 0 ; i < SIZE ; i++) {
			for (int j = 0 ; j < SIZE ; j++) {
				boardInstance[i][j] = _board[i][j];
			}
		}
		return boardInstance;
	}
	
	private double evaluateBoardFor(char ply, char _board[][]) {
		char opponent = ply==diskSet.white?diskSet.black:diskSet.white;
		int _row,_col;
		double score = 0;
		for(_row = 0 ; _row < SIZE ; _row++) {
			for(_col = 0 ; _col < SIZE ; _col++){
				if(_board[_row][_col] == ply) {
					score += heuristicMatrix[_row][_col];	
				}
				else if(_board[_row][_col] == opponent) {
					score -= heuristicMatrix[_row][_col];
				}
			}
		}
		return score;
	}
	
	
	/****************************************************
	 *	gets the next best move in greedy manner		*
	 *	look ahead is 1									*
	 ****************************************************/
	/**
	 * 
	 * @param ply
	 * @return the best move
	 */
	public Child getNextBestChild(char ply, char [][] _board)  {
		char tempState[][];
		ArrayList<Child> children;
		Child _bestChild = null;
		double _eval;
		//	calculate the next possible moves for ply 
		//	this also sets the movePaths list
		children = findCandidateChildrenFor(ply, _board,true);
		if(children.size() == 0) {
			return null;
		}
		// make moves
		for (Child child : children) {
			//	get the state after making the move
			tempState  = getChildStateForThisChild(ply, child.getRow(), child.getCol(), _board);
			//	evaluate the board for ply
			_eval = evaluateBoardFor(ply, tempState);
			//	set the score field in the move POJO
			child.setMoveEval(_eval);
		}
		//	evaluate the best move based on criteria below
		/********************************************************************/
		/*	1-> highest score first											*/
		/*	2-> in case of a tie - lowest row first							*/
		/*	3-> in case of a tie - FCFS										*/
		/********************************************************************/
		Collections.sort(children);
		_bestChild = children.get(0);
		return _bestChild;
 	}
	public Child getBestChild_LookDeep(char ply, char [][] _board, int height, boolean max, Child parent) {
		char tempState[][];
		double _eval;
		Child forkChild = null,_bestChild;
		ArrayList<Child> children;
		Child dummy = null;
		//	find possible children for board state = '_board' for player = ply
		children = findCandidateChildrenFor(ply, _board, max);
		//	if no children found  - ply cannot make any move, try for the opponent
		if(children.size() == 0) {
			// try to find children states of opponent
			children = findCandidateChildrenFor(ply == diskSet.black?diskSet.white:diskSet.black, _board, !max);
			if(children.size() == 0) {
				Child spawnChild = null;
				int myCount = plyCount(diskSet.getMe(),_board);
				int opCount = plyCount(diskSet.getMe()==diskSet.black?diskSet.white:diskSet.black, _board);
				boolean endCase = myCount == 0 || opCount == 0;
				if(!endCase && height > 1) {
					addTraverseLog(parent, height, 0, 0);
				}	
				if(max) {
					spawnChild = new Child(SIZE, SIZE, SIZE, SIZE, Double.POSITIVE_INFINITY);
				}
				else {
					spawnChild = new Child(SIZE, SIZE, SIZE, SIZE, Double.NEGATIVE_INFINITY);
				}						
				if(height > 1) {
					forkChild = getBestChild_LookDeepAndPrune(ply ==diskSet.black?diskSet.white:diskSet.black, _board, height-1, !max, 0, 0, spawnChild);
					spawnChild.setMoveEval(forkChild.getMoveEval());
					if (max && (parent.getMoveEval() < spawnChild.getMoveEval())) {
						parent.setMoveEval(spawnChild.getMoveEval());
					}
					if (!max && (parent.getMoveEval() > spawnChild.getMoveEval())) {
						parent.setMoveEval(spawnChild.getMoveEval());
					}
				}
				else {
					_eval = evaluateBoardFor(diskSet.getMe(), _board);
					spawnChild.setMoveEval(_eval);
					//addTraverseLog(spawnChild, height-1, alpha, beta);
					if (max && (parent.getMoveEval() < spawnChild.getMoveEval())) {
						parent.setMoveEval(spawnChild.getMoveEval());
					}
					if (!max && (parent.getMoveEval() > spawnChild.getMoveEval())) {
						parent.setMoveEval(spawnChild.getMoveEval());
					}
				}
				if(endCase && parent.getRow() == -1){
					addTraverseLog(parent, height, 0, 0);
				}
				else if(endCase && parent.getRow() ==  SIZE) {
					// do nothing
				}	
				else{
					addTraverseLog(parent, height, 0, 0);
				}
				return spawnChild;
			}
			else {
				//	ply could not find the move, he passed, opponent found the move
				//	add traverse log for ply's pass
				if(max) {
					dummy = new Child(SIZE, SIZE, SIZE, SIZE, Double.POSITIVE_INFINITY);
				}
				else{
					dummy = new Child(SIZE, SIZE, SIZE, SIZE, Double.NEGATIVE_INFINITY);
				}
				children = new ArrayList<Child>();
				children.add(dummy);
			}
				
		}
		for (Child child : children) {
			addTraverseLog(parent, height,0,0);
			if(child.getRow() != SIZE){
				//	create a temporary child state with this child
				tempState = getChildStateForThisChild(ply, child.getRow(), child.getCol(), _board);
			}
			else{
				tempState = _board;
			}
			if(height > 1) {
				forkChild = getBestChild_LookDeep(ply==diskSet.black?diskSet.white:diskSet.black, tempState, height-1, !max,child);
			}
			if(height == 1) {
				_eval = evaluateBoardFor(diskSet.getMe(), tempState);
				child.setMoveEval(_eval);
				if (max && (parent.getMoveEval() < child.getMoveEval())) {
					parent.setMoveEval(child.getMoveEval());
				}
				if (!max && (parent.getMoveEval() > child.getMoveEval())) {
					parent.setMoveEval(child.getMoveEval());
				}
				addTraverseLog(child, height-1,0,0);
			}
			else {
				child.setMoveEval(forkChild.getMoveEval());
				if(max && parent.getMoveEval() < child.getMoveEval()) {
					parent.setMoveEval(child.getMoveEval()); 
				}
				if(!max && parent.getMoveEval() > child.getMoveEval()) {
					parent.setMoveEval(child.getMoveEval()); 
				}
				//addTraverseLog(child, height-1);
			}
		}
		//	evaluate the child move based on criteria below
		/********************************************************************/
		/*	1-> highest score first											*/
		/*	2	-> in case of a tie - lowest row first						*/
		/*	3		-> in case of a tie - FCFS								*/
		/********************************************************************/
		Collections.sort(children);
		if(!max) {
			Collections.reverse(children);
		}
		_bestChild = children.get(0);
		parent.setMoveEval(_bestChild.getMoveEval());
		addTraverseLog(parent, height,0,0);
		if(!outOfBoard(_bestChild.getRow(),_bestChild.getCol())) {
			_board = getChildStateForThisChild(ply, _bestChild.getRow(), _bestChild.getCol(), _board);
		}
		return _bestChild;
	}
	
	public Child getBestChild_LookDeepAndPrune(char ply, char [][] _board,int height, boolean max, double alpha, double beta,Child parent) {
		//System.out.println("root"); printOnConsole(_board);
		char tempState[][] = null;
		double _eval;
		Child forkChild = null,_bestChild,dummy=null;
		ArrayList<Child> children;
		children = findCandidateChildrenFor(ply, _board,max);
		if(children.size() == 0) {
			children = findCandidateChildrenFor(ply == diskSet.black?diskSet.white:diskSet.black, _board, !max);
			if(children.size() == 0) {
				Child spawnChild = null;
				int myCount = plyCount(diskSet.getMe(),_board);
				int opCount = plyCount(diskSet.getMe()==diskSet.black?diskSet.white:diskSet.black, _board);
				boolean endCase = myCount == 0 || opCount == 0;
				if(!endCase && height > 1) {
					addTraverseLog(parent, height, alpha, beta);
				}	
				if(max) {
					spawnChild = new Child(SIZE, SIZE, SIZE, SIZE, Double.POSITIVE_INFINITY);
				}
				else {
					spawnChild = new Child(SIZE, SIZE, SIZE, SIZE, Double.NEGATIVE_INFINITY);
				}						
				if(height > 1) {
					forkChild = getBestChild_LookDeepAndPrune(ply ==diskSet.black?diskSet.white:diskSet.black, _board, height-1, !max, alpha, beta, spawnChild);
					spawnChild.setMoveEval(forkChild.getMoveEval());
					if (max && (parent.getMoveEval() < spawnChild.getMoveEval())) {
						parent.setMoveEval(spawnChild.getMoveEval());
					}
					if (!max && (parent.getMoveEval() > spawnChild.getMoveEval())) {
						parent.setMoveEval(spawnChild.getMoveEval());
					}
					if(max) {
						if(parent.getMoveEval() > alpha && parent.getMoveEval() < beta) {
							if(!endCase) {
								alpha = parent.getMoveEval();
							}
						}
					}
					else {
						if(parent.getMoveEval() < beta && parent.getMoveEval() > alpha) {
							if (!endCase) {
								beta = parent.getMoveEval();
							}
						}
					}
				}
				else {
					_eval = evaluateBoardFor(diskSet.getMe(), _board);
					spawnChild.setMoveEval(_eval);
					//addTraverseLog(spawnChild, height-1, alpha, beta);
					if (max && (parent.getMoveEval() < spawnChild.getMoveEval())) {
						parent.setMoveEval(spawnChild.getMoveEval());
					}
					if (!max && (parent.getMoveEval() > spawnChild.getMoveEval())) {
						parent.setMoveEval(spawnChild.getMoveEval());
					}
				}
				if(endCase && parent.getRow() == -1){
					addTraverseLog(parent, height, alpha, beta);
				}
				else if(endCase && parent.getRow() ==  SIZE) {
					// do nothing
				}	
				else{
					addTraverseLog(parent, height, alpha, beta);
				}
				return spawnChild;
			}
			else {
				children = new ArrayList<Child>();
				if(max) {
					dummy = new Child(SIZE, SIZE, SIZE, SIZE, Double.POSITIVE_INFINITY);
				}
				else{
					dummy = new Child(SIZE, SIZE, SIZE, SIZE, Double.NEGATIVE_INFINITY);
				}
				children.add(dummy);
			}
		}
		for (Child child :  children) {
			//if(beta <= alpha) {
				if(max && parent.getMoveEval() >= beta) {
					child.setMoveEval(Double.NEGATIVE_INFINITY);
					continue;
				}
				else if(!max && parent.getMoveEval() <= alpha) {
					child.setMoveEval(Double.POSITIVE_INFINITY);
					continue;
				}
			//}
			addTraverseLog(parent, height,alpha,beta);
			// make a move 
			if(child.getRow() != SIZE) {
				tempState = getChildStateForThisChild(ply, child.getRow(), child.getCol(), _board);
			}
			else {
				tempState = _board;
			}
			if(height > 1) {
				forkChild = getBestChild_LookDeepAndPrune(ply == diskSet.black?diskSet.white:diskSet.black, tempState,height-1, !max,alpha,beta,child);
			}
			if(height == 1){
				_eval = evaluateBoardFor(diskSet.getMe(), tempState);
				child.setMoveEval(_eval);
				if (max && (parent.getMoveEval() < child.getMoveEval())) {
					parent.setMoveEval(child.getMoveEval());
				}
				if (!max && (parent.getMoveEval() > child.getMoveEval())) {
					parent.setMoveEval(child.getMoveEval());
				}
				addTraverseLog(child, height-1,alpha,beta);
				if(max){
					if(_eval > alpha && _eval < beta) {
						alpha = child.getMoveEval();
					}
				}
				else{
					if(_eval < beta && _eval > alpha){
						beta = child.getMoveEval();
					}
				}
				
			}
			else{
				child.setMoveEval(forkChild.getMoveEval());
				if(max && parent.getMoveEval() < child.getMoveEval()) {
					parent.setMoveEval(child.getMoveEval()); 
				}
				if(!max && parent.getMoveEval() > child.getMoveEval()) {
					parent.setMoveEval(child.getMoveEval()); 
				}
				if(max){
					if(child.getMoveEval() > alpha && child.getMoveEval() < beta) {
						alpha = child.getMoveEval();
					}
				}
				else{
					if (child.getMoveEval() < beta && child.getMoveEval() > alpha) {
						beta = child.getMoveEval();
					}	
				}
			}
		}
		//	evaluate the best move based on criteria below
		/********************************************************************/
		/*	1-> highest score first											*/
		/*	2	-> in case of a tie - lowest row first						*/
		/*	3		-> in case of a tie - FCFS								*/
		/********************************************************************/
		
		Collections.sort(children);
		if(!max) {
			Collections.reverse(children);
		}
		_bestChild = children.get(0);
		parent.setMoveEval(_bestChild.getMoveEval());
		addTraverseLog(parent,height,alpha,beta);
		// Make the best move
		if(!outOfBoard(_bestChild.getRow(),_bestChild.getCol())) {
			_board = getChildStateForThisChild(ply, _bestChild.getRow(), _bestChild.getCol(), _board);
		}
		return _bestChild;		
	}
	/**
	 * 
	 * Game competition agent
	 * 
	 * @param ply
	 * @param _board
	 * @param height
	 * @param max
	 * @param alpha
	 * @param beta
	 * @param parent
	 * @return the best possible next state for ply
	 */
	public Child X_Alpha_Beta(char ply, char [][] _board,int height, boolean max, double alpha, double beta,Child parent) {
		char tempState[][] = null;
		double _eval;
		Child forkChild = null,_bestChild=null,dummy=null;
		ArrayList<Child> children;
		children = findCandidateChildrenFor(ply, _board,max);
		if(children.size() == 0) {
			// no children states found for ply, make a pass, try finding possible states other player.
			children = findCandidateChildrenFor(ply == diskSet.black?diskSet.white:diskSet.black, _board, !max);
			if(children.size() == 0) {
				_eval = evaluateBoardFor(diskSet.me, _board);
				parent.setMoveEval(_eval);
				return parent;
			}
			else {
				children = new ArrayList<Child>();
				if(max) {
					dummy = new Child(SIZE, SIZE, SIZE, SIZE, Double.POSITIVE_INFINITY);
				}
				else{
					dummy = new Child(SIZE, SIZE, SIZE, SIZE, Double.NEGATIVE_INFINITY);
				}
				children.add(dummy);
			}
		}
		for (Child child :  children) {
			if(beta <= alpha) {
				return _bestChild;
			}
			// make a move 
			if(child.getRow() != SIZE) { // not a PASS move
				tempState = getChildStateForThisChild(ply, child.getRow(), child.getCol(), _board);
			}
			else {
				tempState = _board;
			}
			if(height == 1){
				_eval = evaluateBoardFor(diskSet.getMe(), tempState);
				child.setMoveEval(_eval);
				// find best child
				if (max && (parent.getMoveEval() < child.getMoveEval())) {
					parent.setMoveEval(child.getMoveEval());
					_bestChild = child;
				}
				if (!max && (parent.getMoveEval() > child.getMoveEval())) {
					parent.setMoveEval(child.getMoveEval());
					_bestChild = child;
				}
				// set up alpha beta
				if(max){
					if(_eval > alpha) {
						alpha = child.getMoveEval();
					}
				}
				else{
					if(_eval < beta){
						beta = child.getMoveEval();
					}
				}
			}
			else if(height > 1){
				forkChild = X_Alpha_Beta(ply == diskSet.black?diskSet.white:diskSet.black, tempState,height-1, !max,alpha,beta,child);
				child.setMoveEval(forkChild.getMoveEval());
				// set up best child
				if(max && parent.getMoveEval() < child.getMoveEval()) {
					parent.setMoveEval(child.getMoveEval());
					_bestChild = child;
				}
				if(!max && parent.getMoveEval() > child.getMoveEval()) {
					parent.setMoveEval(child.getMoveEval());
					_bestChild = child;
				}
				// set alpha and beta
				if(max){
					if(child.getMoveEval() > alpha) {
						alpha = child.getMoveEval();
					}
				}
				else{
					if (child.getMoveEval() < beta) {
						beta = child.getMoveEval();
					}	
				}
			}
		}
		return _bestChild;		
	}

	public void printOnConsole(char [][] _board) {
		int i,j;
		for (i = 0 ; i < SIZE ; i++) {
			for (j = 0 ; j < SIZE ; j++) {
				System.out.print(_board[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	/**
	 * 
	 * @param ply
	 * @param _board
	 * @return the count of ply on the _board
	 */
	public int plyCount(char ply, char[][] _board) {
		int i,j,count;
		count = 0;
		for (i = 0 ; i < SIZE ; i++) {
			for (j = 0 ; j< SIZE ; j++) {
				count = _board[i][j] == ply?count+1:count;
			}
		}
		return count;
	}
	/**
	 * Add a step in traverse log
	 * @param child
	 * @param height
	 * @param alpha
	 * @param beta
	 */
	private void addTraverseLog(Child child, int height, double alpha, double beta) { 
		double  [] childAttr = {child.getRow(),child.getCol(),child.getMoveEval(),height,alpha,beta};
		traverseLog.add(childAttr);
	}
	/**********************************************************
	 *	Setters and Getters of Class attributes				  * 	
	 **********************************************************/
	public char[][] getBoard() {
		return board;
	}
	public void setBoard(char[][] board) {
		this.board = board;
	}
	public char getPly() {
		return ply;
	}
	public void setPly(char ply) {
		this.ply = ply;
	}
	public double[][] getHeuristicMatrix() {
		return heuristicMatrix;
	}

	public void setHeuristicMatrix(double[][] heuristicMatrix) {
		this.heuristicMatrix = heuristicMatrix;
	}
	public ArrayList<double[]> getTraverseLog() {
		return traverseLog;
	}
	public void setTraverseLog(ArrayList<double[]> traverseLog) {
		this.traverseLog = traverseLog;
	}
}