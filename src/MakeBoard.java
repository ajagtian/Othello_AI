import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
/**
 * 
 * @author akshayjagtiani
 *
 */
public class MakeBoard {
	public static void main(String[] args) {
		//	take the text from the file as it is.
		String fileText = getInputStringFromFile("input.txt");
		String [] lines = fileText.split("\n");
		final int SIZE = 8;
		char task = '2',me='X';
		double timeleft = 0;
		char[][] state = new char[SIZE][SIZE];						/*	the given instance of the game whose next move is to be found					*/
		int height = 1;

		try {
			// process the text from file into information
			task = lines[0].trim().charAt(0);						/*	the task 1 - greedy |2 - minimax |3 - alphaBeta |4 - Competition				*/
			me = lines[1].trim().charAt(0);							/*	My player alias X or O => max player											*/
			if(task == '4') {
				timeleft = Double.parseDouble(lines[2].trim());
			}
			if(task != '4') {
				height = Integer.parseInt(lines[2].trim());			/*	depth - cutoff depth															*/
			}
			//	init the given board state
			for (int i = 0 ; i < SIZE ; i++) {
				for (int j = 0 ; j <  8 ;j++) {
					state[i][j] = lines[i+3].trim().charAt(j);
				}
			}
		}
		catch(StringIndexOutOfBoundsException e) {
			System.out.println("Wrong input file");	
		}
		catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Wrong input file");
		}
		catch (NumberFormatException e) {
			System.out.println("Wrong input file");
		}
		//	init the given positional heuristic matrix
		final double [][] positionalHeuristic = {
												{99,-8,8,6,6,8,-8,99},
												{-8,-24,-4,-3,-3,-4,-24,-8},
												{8,-4,7,4,4,7,-4,8},
												{6,-3,4,0,0,4,-3,6},
												{6,-3,4,0,0,4,-3,6},
												{8,-4,7,4,4,7,-4,8},
												{-8,-24,-4,-3,-3,-4,-24,-8},
												{99,-8,8,6,6,8,-8,99}
				  						  };
		
		/********************************************************************************
		 *	Set up the game...															*
		 *	->	set the disk object														*
		 *	-> 	set your player															*
		 * 	-> 	init the board object													*
		 * 	->	set the state in the board object										*
		 * 	->	find the next state using any one of the 3 algos depending on input		*
		 ********************************************************************************/
		//	set up game
		Disk diskSet = Disk.getInstance();
		diskSet.setColors('X', 'O', '*', me);
		char nextPly = me;
		Board board = new Board();
		board.setBoard(state);
		board.setHeuristicMatrix(positionalHeuristic);
		double _startTime,_endTime;
		Child _bestChild = null;
		ArrayList<String> processedLog = new ArrayList<String>();
		//	play
		if(task == '4') {
			_startTime = System.nanoTime();
			int plyCount;
			// game progress heuristic, calculated specifically for ilab server
			int [] d= {1,8,7,7,7,6,5,6,5,5,5,6,5,6,5,6,6,6,6,6,6,6,6,5,6,7,10,11,11,11,11};
			File _ply = new File("_ply.txt");
			byte c [] = new byte[(int)_ply.length()];
			try {
				FileInputStream fin = new FileInputStream(_ply);
				fin.read(c);
				try{
					plyCount = Integer.parseInt(new String(c));
					FileOutputStream fout = new FileOutputStream("_ply.txt");
					fout.write(String.valueOf(plyCount+1).getBytes());
					fin.close();fout.close();
				}
				catch(NumberFormatException e) {
					plyCount = 9;
				}
			} catch (FileNotFoundException e1) {
				plyCount = 9;
			} catch (IOException e) {
				plyCount = 9;
			}
			if(plyCount <= 30){
				if(me == 'O') {
					plyCount--;
				}
				height = d[plyCount];
			}
			else {
				height = 5;
			}
			if(timeleft <= 5 && plyCount < 25) {
				height = 2;
			}
			// compete
			try {
				_bestChild = board.X_Alpha_Beta(nextPly, board.getBoard(), height, true, Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY, new Child(-1, -1, -1, -1, Double.NEGATIVE_INFINITY));
			}
			catch(OutOfMemoryError e){
				_bestChild = board.X_Alpha_Beta(nextPly, board.getBoard(), 2, true, Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY, new Child(-1, -1, -1, -1, Double.NEGATIVE_INFINITY));
			}
			catch (Exception e) {
				_bestChild = board.X_Alpha_Beta(nextPly, board.getBoard(), 2, true, Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY, new Child(-1, -1, -1, -1, Double.NEGATIVE_INFINITY));
			}
			String log = "";
			try {
				if(_bestChild.getRow() == SIZE) {
					log = "PASS";
				}
				else if(_bestChild.getRow() == -1) {
					log = "PASS";
				}
				else {
					log = String.valueOf((char)(_bestChild.getCol() + 97)) + String.valueOf((_bestChild.getRow() + 1));
				}
				FileOutputStream fout = new FileOutputStream("output.txt");
				fout.write(log.getBytes());
				fout.close();
			} 
			catch (FileNotFoundException e) {
				System.out.println("Could not write output file"+e.getMessage());			
			} catch (IOException e) {
				System.out.println("Could not write output file"+e.getMessage());			
			}
			_endTime = System.nanoTime();
			//System.out.println((_endTime - _startTime)/1000000 + "ms");
		}	
		else {
			System.out.println("---given board---");
			board.printOnConsole(board.getBoard());
			_startTime = System.nanoTime();
			if(task == '1') {
				// greedy
				_bestChild = board.getNextBestChild(nextPly, board.getBoard());
			}
			else if(task == '2') {
				//	minimax
				try{
					_bestChild = board.getBestChild_LookDeep(nextPly, board.getBoard(), height, true,new Child(-1, -1,-1, -1,Double.NEGATIVE_INFINITY));
				}
				catch(OutOfMemoryError e){
					System.out.println("The JVM Ran out of "+e.getMessage());
				}
				catch(Exception e){
					System.out.println("The JVM Ran out of heap space"+e.getMessage());
				}
			}
			else if(task == '3') {
				// alpha beta
				try {
					_bestChild = board.getBestChild_LookDeepAndPrune(nextPly, board.getBoard(), height, true, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,new Child(-1, -1,-1, -1,Double.NEGATIVE_INFINITY));
				}
				catch(OutOfMemoryError e) {
					System.out.println("The JVM Ran out of "+e.getMessage());
				}
				catch(Exception e){
					System.out.println("The JVM Ran out of heap space"+e.getMessage());
				}
			}
			if(_bestChild != null && _bestChild.getRow() != -1 && _bestChild.getRow() != SIZE){ 
				board.setBoard(board.getChildStateForThisChild(nextPly, _bestChild.getRow(),_bestChild.getCol(), board.getBoard()));
			}	
			System.out.println("---After the move---");
			board.printOnConsole(board.getBoard());
			if (_bestChild != null){
				try{
					processedLog = processTraverseLog(board.getTraverseLog(),height,task,SIZE);
				}
				catch(OutOfMemoryError e) {
					System.out.println("Java ran out of "+e.getMessage()+" during file I/O");
				}
			}	
			//System.out.println("Traverse Log");
			//for (String log : processedLog) {
				//System.out.println(log);
			//}
			_endTime = System.nanoTime();
			System.out.println("Time taken => "+(_endTime-_startTime)/1000000+" ms");
			System.out.println("Doing File i/o");
			writeFile("output.txt",board.getBoard(),processedLog,SIZE);
		}
	}

	private static void writeFile(String file, char[][] board, ArrayList<String> processedLog, final int SIZE) {
		String _board = "";
		for (int i = 0 ; i < SIZE ; i++) {
			for(int j = 0 ; j< SIZE ; j++) {
				_board += String.valueOf(board[i][j]);
			}
			_board += "\n";
		}
		String _log  = "";
		for (String log : processedLog) {
			_log += log+"\n";
		}
		String fileDirectory = System.getProperty("user.dir");
		File output = new File(fileDirectory+"/"+file);
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(output);
		} catch (FileNotFoundException e) {
		}
		if (fout != null) {
			try {
				fout.write(_board.getBytes());
				fout.write(_log.getBytes());
				fout.close();
			} catch (IOException e) {
				System.out.println("Could not write file due to permissions"+e.getMessage());
			}
			finally {
				fout = null;
			}
		}
		else {
			System.out.println("output.txt can not be made");
		}
	}

	private static ArrayList<String> processTraverseLog(ArrayList<double[]> traverseLog,int height, char task, final int SIZE) {
		ArrayList<String> processedLog = new ArrayList<String>();
		int r = traverseLog.size();
		if(task == '2') {
			processedLog.add("Node,Depth,Value");
		}
		else if(task == '3'){
			processedLog.add("Node,Depth,Value,Alpha,Beta");
		}
		for (int i = 0 ; i < r ; i++) {
			String row = String.valueOf((int)traverseLog.get(i)[0]+1);
			if((int)traverseLog.get(i)[0] == -1) {
				row = "ot";
			}
			if((int)traverseLog.get(i)[0] == SIZE) {
				row = "ss";
			}
			String col = String.valueOf((char)(traverseLog.get(i)[1] + 97));
			if((int)traverseLog.get(i)[1] == -1) {
				col = "ro";
			}
			if((int)traverseLog.get(i)[1] == SIZE) {
				col = "pa";
			}
			String value = String.valueOf((int)traverseLog.get(i)[2]);
			if(traverseLog.get(i)[2] == Double.NEGATIVE_INFINITY) {
				value = "-Infinity";
			}
			else if(traverseLog.get(i)[2] == Double.POSITIVE_INFINITY) {
				value = "Infinity";
			}
			String depth = String.valueOf(height - (int)traverseLog.get(i)[3]);
			String alpha = String.valueOf((int)traverseLog.get(i)[4]);
			if(traverseLog.get(i)[4] == Double.NEGATIVE_INFINITY) {
				alpha = "-Infinity";
			}
			String beta = String.valueOf((int)traverseLog.get(i)[5]);
			if(traverseLog.get(i)[5] == Double.POSITIVE_INFINITY) {
				beta = "Infinity";
			}
			String log = task == '3'?col+row+","+depth+","+value+","+alpha+","+beta:col+row+","+depth+","+value;
			processedLog.add(log);
		}
		return processedLog;
	}

	private static String getInputStringFromFile(String string) {
		
		String fileDirectory = System.getProperty("user.dir");
		File file = new File(fileDirectory+"/"+string);
		FileInputStream fileStream = null;
		byte [] fileText = null;
		if (file.exists()) {
			fileText = new byte[(int)file.length()];
			try {
				fileStream = new FileInputStream(file);
				fileStream.read(fileText);
				fileStream.close();
			} 
			catch(FileNotFoundException e){
				System.out.println("ERROR: File not fount. "+e.getMessage());
			}catch (IOException e) {
				System.out.println("ERROR: problem reading file. "+e.getMessage());
			}
			finally{
				fileStream = null;
				System.gc();
			}
		}
		else{
			System.out.println("ERROR: File 'input.txt' does not exist.");
		}
		return fileText != null?new String(fileText):"";
	}	
}
