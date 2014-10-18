package musicmatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.ConcurrentLinkedQueue;

public class MusicMatch {
	
	public static volatile boolean working = true;
	public static volatile int stoppedThreads = 0;
	
	// Queue with songs to  process
	public static LinkedBlockingQueue <Song> songsToProcess = new LinkedBlockingQueue();
	
    public static void main(String[] args) {
		Log.log("Start");
		
		Thread[] songThreads = new Thread[Config.processThreads];
		Thread dbThread = new Thread(new DB());

		// if we don't have old session loaded from db
		if(songsToProcess.isEmpty()){
			Song song = new Song();
			song.setLink("/metallica/metallica/nothing-else-matters");

			songsToProcess.add(song);
		}

		dbThread.setName("DB Thread");
		dbThread.start();

		for (int i = 0; i < songThreads.length; i++) {
			songThreads[i] = new Thread(new SongThread());
			songThreads[i].setName("Song Thread "+i);
			songThreads[i].start();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String command = "";
		while(!"exit".equals(command)){
			try {
				command = br.readLine();
				switch (command) {
					case "exit":
						working = false;
						break;
					default:
						break;
				}
			} catch (IOException ex) {
				Log.logWarning(ex);
			}
		}
		System.out.println("exit");

	}
	
}
