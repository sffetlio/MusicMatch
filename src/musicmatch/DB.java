package musicmatch;

import java.sql.*;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public final class DB implements Runnable {
	
	// Cache with saved song's IDs (these songs are in db)
	// This cashe is used for faster check if one song is already processed
	private static HashMap<String,Integer> savedSongsCache = new HashMap();
	private static HashMap<String,Integer> savedAlbumsCache = new HashMap();
	private static HashMap<String,Integer> savedArtistsCache = new HashMap();
	private static HashMap<String,Integer> savedTraitsCache = new HashMap(1500);

	// queue for db thread. if more than queue size than waiting will occur
	private static ArrayBlockingQueue<Song> dbQueue = new ArrayBlockingQueue(100);
	
	private PreparedStatement psSongUpdate;
	private PreparedStatement psSongSave;
	private PreparedStatement psAlbumSave;
	private PreparedStatement psTraitSave;
	private PreparedStatement psArtistSave;
	private PreparedStatement psLinkSongTrait;
	private PreparedStatement psLinkSimilarSongs;
	
	public DB() {
		try {
			Connection con = DriverManager.getConnection(Config.dataBaseUrl, Config.username, Config.password);
			
//			PreparedStatement emptyTables = con.prepareStatement("TRUNCATE `album`;");
//			emptyTables.executeUpdate();
//			emptyTables = con.prepareStatement("TRUNCATE `artist`;");
//			emptyTables.executeUpdate();
//			emptyTables = con.prepareStatement("TRUNCATE `similar_song`;");
//			emptyTables.executeUpdate();
//			emptyTables = con.prepareStatement("TRUNCATE `song`;");
//			emptyTables.executeUpdate();
//			emptyTables = con.prepareStatement("TRUNCATE `song_trait`;");
//			emptyTables.executeUpdate();
//			emptyTables = con.prepareStatement("TRUNCATE `trait`;");
//			emptyTables.executeUpdate();
//			emptyTables.close();
			
			ResultSet result;
			try (Statement st = con.createStatement()) {
				result = st.executeQuery("select id, link from song");
				while (result.next()) {
					savedSongsCache.put(result.getString("link"), result.getInt("id"));
				}
				Log.log("loaded "+savedSongsCache.size()+" saved songs");
				result = st.executeQuery("select id, link from song where album_id = 0");
				while (result.next()) {
					Song song = new Song();
					song.setId(result.getInt("id"));
					song.setLink(result.getString("link"));
					MusicMatch.songsToProcess.add(song);
				}
				Log.log("loaded "+MusicMatch.songsToProcess.size()+" songs to process");
				result = st.executeQuery("select id, name, artist_id from album");
				while (result.next()) {
					savedAlbumsCache.put(result.getString("artist_id")+result.getString("name"), result.getInt("id"));
				}
				Log.log("loaded "+savedAlbumsCache.size()+" albums");
				result = st.executeQuery("select id, link from artist");
				while (result.next()) {
					savedArtistsCache.put(result.getString("link"), result.getInt("id"));
				}
				Log.log("loaded "+savedArtistsCache.size()+" artists");
				result = st.executeQuery("select id, name from trait");
				while (result.next()) {
					savedTraitsCache.put(result.getString("name"), result.getInt("id"));
				}
				Log.log("loaded "+savedTraitsCache.size()+" traits");
			}
			result.close();
			
			psSongUpdate = con.prepareStatement(
				"update song set "
				+ "title = ?,"
				+ "link = ?,"
				+ "artist_id = ?,"
				+ "album_id = ?,"
				+ "lyrics = '',"
				+ "pandora_id = '' "
				+ "where id = ?",
				Statement.RETURN_GENERATED_KEYS
			);
			
			psSongSave = con.prepareStatement(
				"insert into song ("
				+ "title,"
				+ "link,"
				+ "artist_id,"
				+ "album_id,"
				+ "lyrics,"
				+ "pandora_id"
				+ ") values ("
				+ "?,"
				+ "?,"
				+ "?,"
				+ "?,"
				+ "'',"
				+ "''"
				+ ")",
				Statement.RETURN_GENERATED_KEYS
			);
			
			psAlbumSave = con.prepareStatement(
				"insert into album ("
				+ "name,"
				+ "artist_id,"
				+ "link,"
				+ "img,"
				+ "pandora_id"
				+ ") values ("
				+ "?," 
				+ "?,"
				+ "?,"
				+ "?,"
				+ "''"
				+ ")",
				Statement.RETURN_GENERATED_KEYS
			);
			
			psTraitSave = con.prepareStatement(
				"insert into trait ("
				+ "name,"
				+ "pandora_id"
				+ ") values ("
				+ "?,"
				+ "''"
				+ ")",
				Statement.RETURN_GENERATED_KEYS
			);
			
			psArtistSave = con.prepareStatement(
				"insert into artist ("
				+ "name,"
				+ "link,"
				+ "pandora_id"
				+ ") values ("
				+ "?," 
				+ "?,"
				+ "''"
				+ ")",
				Statement.RETURN_GENERATED_KEYS
			);
			
			psLinkSongTrait = con.prepareStatement(
				"insert into song_trait ("
				+ "song_id,"
				+ "trait_id"
				+ ") values ("
				+ "?," 
				+ "?"
				+ ")"
			);
			
			psLinkSimilarSongs = con.prepareStatement(
				"insert into similar_song ("
				+ "song1,"
				+ "song2"
				+ ") values ("
				+ "?," 
				+ "?"
				+ ")"
			);
		
        } catch (SQLException ex) {
            Log.logWarning(ex);
        }
	}

	public int saveSong(Song song, int artistId, int albumId) {
		try {
			Integer songId = savedSongsCache.get(song.getLink());
			if(songId != null && songId > 0 && artistId > 0 && albumId > 0){
//				return songId;
//			}
			// if song has id, so it is in db. So we need to update it.
//			if (song.getId() > 0) {
				psSongUpdate.setString(1, song.getName());
				psSongUpdate.setString(2, song.getLink());
				psSongUpdate.setInt(3, artistId);
				psSongUpdate.setInt(4, albumId);
//				psSongUpdate.setInt(5, song.getId());
				psSongUpdate.setInt(5, songId);
				psSongUpdate.executeUpdate();
				psSongUpdate.clearParameters();
				
				return songId;
			}else{
				// else if id is 0 insert it
				
				psSongSave.setString(1, song.getName());
				psSongSave.setString(2, song.getLink());
				psSongSave.setInt(3, artistId);
				psSongSave.setInt(4, albumId);
				psSongSave.executeUpdate();
				
				ResultSet generatedKeys = psSongSave.getGeneratedKeys();
				psSongSave.clearParameters();
				
				if (generatedKeys.next()) {
					int id = generatedKeys.getInt(1);
					generatedKeys.close();
					savedSongsCache.put(song.getLink(),id);
					return id;
				}else{
					return -1;
				}
			}
		} catch (SQLException ex) {
			Log.logWarning(ex);
		}
		return -1;
	}
	
	public int saveAlbum(Album album, int artistId) {
		if(album == null){
			return 0;
		}
		try {
			Integer albumId = savedAlbumsCache.get(artistId+album.getName());
			if(albumId != null && albumId > 0){
				return albumId;
			}else{
				psAlbumSave.setString(1, album.getName());
				psAlbumSave.setInt(2, artistId);
				psAlbumSave.setString(3, album.getLink());
				psAlbumSave.setString(4, album.getImg());
				psAlbumSave.executeUpdate();
				
				ResultSet generatedKeys = psAlbumSave.getGeneratedKeys();
				psAlbumSave.clearParameters();
				
				if (generatedKeys.next()) {
					albumId = generatedKeys.getInt(1);
					generatedKeys.close();
					savedAlbumsCache.put(artistId+album.getName(), albumId);
					return albumId;
				}else{
					return -1;
				}
			}
			
		} catch (SQLException ex) {
			Log.logWarning(ex);
		}
		return -1;
	}
	
	public int saveTrait(Trait trait) {
		try {
			Integer traitId = savedTraitsCache.get(trait.getName());
			if(traitId != null && traitId > 0){
				return traitId;
			}else{
				psTraitSave.setString(1, trait.getName());
				psTraitSave.executeUpdate();
				
				ResultSet generatedKeys = psTraitSave.getGeneratedKeys();
				psTraitSave.clearParameters();
				
				if (generatedKeys.next()) {
					traitId = generatedKeys.getInt(1);
					generatedKeys.close();
					savedTraitsCache.put(trait.getName(), traitId);
					return traitId;
				}else{
					return -1;
				}
			}
			
		} catch (SQLException ex) {
			Log.logWarning(ex);
		}
		return -1;
	}
	
	public int saveArtist(Artist artist) {
		if(artist == null){
			return 0;
		}
		try {
			Integer artistId = savedArtistsCache.get(artist.getLink());
			if(artistId != null && artistId > 0){
				return artistId;
			}else{
				psArtistSave.setString(1, artist.getName());
				psArtistSave.setString(2, artist.getLink());
				psArtistSave.executeUpdate();
				
				ResultSet generatedKeys = psArtistSave.getGeneratedKeys();
				psArtistSave.clearParameters();
				
				if (generatedKeys.next()) {
					artistId = generatedKeys.getInt(1);
					generatedKeys.close();
					savedArtistsCache.put(artist.getLink(), artistId);
					return artistId;
				}else{
					return -1;
				}
			}
			
		} catch (SQLException ex) {
			Log.logWarning(ex);
		}
		return -1;
	}
	
	public static synchronized void addSong(Song song){
		try {
			dbQueue.put(song);
		} catch (InterruptedException ex) {
			Log.logWarning(ex);
		}
	}
	
	public int addSongToProcess(Song song){
		Integer songId = savedSongsCache.get(song.getLink());
		if(songId != null && songId > 0){
			return songId;
		}
		songId = saveSong(song, 0, 0);
//		addSong(song);
		MusicMatch.songsToProcess.add(song);
		return songId;
	}
	
	public static synchronized boolean isSaved(Album album){
		return savedAlbumsCache.containsKey(album.getLink());
	}
	
	public static synchronized boolean isSaved(Artist artist){
		return savedArtistsCache.containsKey(artist.getLink());
	}
	
	private void linkSongTrait(Song song, Trait[] traits) {
		try {
			for (int i = 0; i < traits.length; i++) {
				psLinkSongTrait.setInt(1, song.getId());
				psLinkSongTrait.setInt(2, traits[i].getId());
				psLinkSongTrait.executeUpdate();
				psLinkSongTrait.clearParameters();
			}
			
		} catch (SQLException ex) {
			Log.logWarning(ex);
		}
	}

	private void linkSimilarSongs(Song song, Song[] similarSongs) {
		try {
			for (int i = 0; i < similarSongs.length; i++) {
				psLinkSimilarSongs.setInt(1, song.getId());
				psLinkSimilarSongs.setInt(2, similarSongs[i].getId());
				psLinkSimilarSongs.executeUpdate();
				psLinkSimilarSongs.clearParameters();
			}
		} catch (SQLException ex) {
			Log.logWarning(ex);
		}
	}
	
	@Override
	public void run() {
		try {
//			while(!terminating || dbQueue.size() > 0){
			while(true){
				long start = System.currentTimeMillis();
				
				if(!MusicMatch.working && MusicMatch.stoppedThreads == Config.processThreads && dbQueue.size() == 0){
					break;
				}
				
				Song song = dbQueue.take();
				
				long end = System.currentTimeMillis();
				long waiting = end-start;
				start = System.currentTimeMillis();
				
				int artistId = saveArtist(song.getArtist());
				
				int albumId = saveAlbum(song.getAlbum(), artistId);
				
				song.setId(saveSong(song, artistId, albumId));
				
				Trait[] traits = song.getFeatures();
				
				if(traits != null){
					for (int i = 0; i < traits.length; i++) {
						traits[i].setId(saveTrait(traits[i]));
					}
				
					linkSongTrait(song, traits);
				}
				
				Song[] similarSongs = song.getSimilar();
				
				if(similarSongs != null){
					for (int i = 0; i < similarSongs.length; i++) {
	//					Integer songId = savedSongsCache.get(similarSongs[i].getLink());
	//					if(songId != null && songId > 0){
	//						similarSongs[i].setId(songId);
	//					}else{
							similarSongs[i].setId(addSongToProcess(similarSongs[i]));
	//					}
					}
				
					linkSimilarSongs(song, similarSongs);
				}
				
				if(song.getArtist() != null){
					Song[] artistSongs = song.getArtist().getSongs();
					if(artistSongs != null){// && !isSaved(song.getArtist())
						for (int i = 0; i < artistSongs.length; i++) {
							addSongToProcess(artistSongs[i]);
						}
					}
				}
				
				end = System.currentTimeMillis();
				Log.log(
					"Proc/DB queue: "+MusicMatch.songsToProcess.size()+"/"+dbQueue.size()+
					System.getProperty("line.separator")+
					"Wait/Exec: "+waiting+"/"+(end-start)+" ms."
				);
			}
		} catch (InterruptedException ex) {
			Log.logWarning(ex);
		}
		Log.log("finished db");
		Log.log("left for proc: "+MusicMatch.songsToProcess.size());
	}
	
}
