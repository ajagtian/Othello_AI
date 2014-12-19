public class Child implements Comparable<Child>{	
	private int row;
	private int col;
	private int parent_row;
	private int parent_col;
	private double moveEval;
	
	Child() {
		
	}
	
	Child(int row,int col, int p_row, int p_col,double eval) {
		this.row = row;
		this.col = col;
		this.parent_row = p_row;
		this.parent_col = p_col;
		this.moveEval = eval;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public int getParent_row() {
		return parent_row;
	}
	public void setParent_row(int parent_row) {
		this.parent_row = parent_row;
	}
	public int getParent_col() {
		return parent_col;
	}
	public void setParent_col(int parent_col) {
		this.parent_col = parent_col;
	}
	public double getMoveEval() {
		return moveEval;
	}
	public void setMoveEval(double moveEval) {
		this.moveEval = moveEval;
	}
	//@Override
	public String toString() {
		return "Child [row=" + row + ", col=" + col + ", parent_row="
				+ parent_row + ", parent_col=" + parent_col + ", moveEval="
				+ moveEval + "]";
	}

	//@Override
	public int compareTo(Child o) {
		if (this.moveEval > o.getMoveEval()) {
			return -1;
		}
		else if(this.moveEval < o.getMoveEval()) {
			return 1;
		}
		else {
			if (this.row < o.getRow()) {
				return -1;
			}
			else if (this.row > o.getRow()) {
				return 1;
			}
			else {
				if(this.col < o.getCol()) {
					return -1;
				}
				else if(this.col > o.getCol()) {
					return 1;
				}
				else {
					return 0;
				}	
			}
		}	
	}	
}