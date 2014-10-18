package musicmatch;

public class SongThread implements Runnable {
	
	@Override
    public void run(){
		PageParser pp = null;
		while(MusicMatch.working){
			if(pp != null){
				pp.doc = null;
				pp = null;
			}
			try {
				Song song = MusicMatch.songsToProcess.take();

				Log.log(Thread.currentThread().getName()+" processing: "+song.getLink());
				
				pp = new PageParser(Config.pandoraUrl + Config.songUrl + song.getLink());
				
				if(pp.doc == null){
					continue;
				}

				Song currentSong = pp.getSongInfo();
				currentSong.setLink(song.getLink());

				currentSong.setId(song.getId());

				Trait[] features = pp.getSongFeatures();
				currentSong.setFeatures(features);

				Song[] similarSongs = pp.getSimilarSongs();
				currentSong.setSimilar(similarSongs);

				String imgHash = pp.getImg(currentSong);
				currentSong.getAlbum().setImg(imgHash);
				
				if(!currentSong.getAlbum().getLink().equals("")){
					Song[] artistSongs = pp.getArtistSongs(currentSong);

					currentSong.getArtist().setSongs(artistSongs);
				}
				DB.addSong(currentSong);
				
			} catch (Exception ex) {
				Log.logSevere(Thread.currentThread().getName()+" EXEPTION");
				Log.logSevere(ex);
			}
		}
		Log.log(Thread.currentThread().getName()+" stopped");
		MusicMatch.stoppedThreads++;
    }
	
}