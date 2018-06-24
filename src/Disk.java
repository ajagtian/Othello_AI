public class Disk {
	/*
	 * Peanut butter
	 */
	public char black = 'X';
	public char white = 'O';
	public char empty = '*';
	public char me = 'X';
	private static Disk instance = new Disk();
	
	private Disk() {
		
	}
	
	public static Disk getInstance() {
		return instance;
	}
	
	public void setColors(char b, char w, char e, char m) {
		black = b;
		white = w;
		empty = e;
		me = m;
	}

	public char getBlack() {
		return black;
	}

	public char getWhite() {
		return white;
	}

	public char getEmpty() {
		return empty;
	}

	public char getMe() {
		return me;
	}	
}
