import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class InitPlyCounter {
	public static void main(String [] args) {
		try {
			FileOutputStream fout = new FileOutputStream("_ply.txt");
			String plyCount = "1";
			fout.write(plyCount.getBytes());
			fout.close();
		} catch (FileNotFoundException e) {
			System.out.println("failed, continue to game");
		} catch (IOException e) {
			System.out.println("failed, continue to game");
		}
	}
}
