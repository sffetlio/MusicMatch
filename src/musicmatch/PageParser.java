package musicmatch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PageParser {
	
	public Document doc;
	
	PageParser(String urlStr) {
		int tries = 0;
		do{
			try {
				URL url = new URL(urlStr);
//                                System.out.println(url);

				doc = Jsoup.parse(url, 10*1000);
				//if no exeption than break the loop
				
//				FileWriter fstream = new FileWriter(Config.filePath + urlStr.replace("/" , "+") + ".txt");
//				BufferedWriter out = new BufferedWriter(fstream);
//				out.write(doc.toString());
//				//Close the output stream
//				out.close();
				
				break;
			} catch (IOException ex) {
				Log.logWarning(ex);
			}		
		}while(tries++ < 10);
	}
	
	public Trait[] getSongFeatures(){
		
		String html = doc.select("div.song_features").html();
		html = html.replace("<h2>Features of This Track</h2> ", "");
		html = html.replace("<div style=\"display: none;\">", "");
		html = html.replace("<br />", "");
		html = html.replace("  ", " ");
		
		String[] lines = html.split("\n");
		List<Trait> features = new ArrayList<>();
		String currentLine;
		for(int i = 0; i < lines.length; i++){
			currentLine = lines[i].trim();
			if(currentLine.length() == 0){
				continue;
			}
			if(currentLine.startsWith("<")){
				continue;
			}
			Trait trait = new Trait();
			trait.setName(currentLine);
			features.add(trait);
		}
		
		return features.toArray(new Trait[0]);
	}

	public Song[] getSimilarSongs() {
		
		Elements elements = doc.select("div.similar_text");
		
		List<Song> similarSongs = new ArrayList<>();
		
		for(int i = 0; i < elements.size(); i++){
			Element el = elements.get(i);
			String trackName = el.select("a.track_link").html();
			String trackLink = el.select("a.track_link").attr("href");
			String artistName = el.select("a.artist_link").html();
			String artistLink = el.select("a.artist_link").attr("href");
			
			Song song = new Song();
			song.setName(trackName);
			song.setLink(trackLink);
			
			Artist artist = new Artist();
			artist.setName(artistName);
			artist.setLink(artistLink);
			song.setArtist(artist);
			
			similarSongs.add(song);
		}
		
		return similarSongs.toArray(new Song[0]);
	}

	public Song getSongInfo() {
		Song song = new Song();
		
		Element el = doc.select("div.backstage").first();
		String id = el.attr("data-musicid");
		String songTitle = el.select("h1").html();
		
		el = doc.select("div.content_right").first();
		
		String albumName = el.select("div.album_title").attr("title");
		String albumLink = el.select("div.album_title a.album_link").attr("href");
		String artistName = el.select("div.artist_name a.artist_link").html();
		String artistLink = el.select("div.artist_name a.artist_link").attr("href");
		
//		String lyrics = el.select("div.fullLyrics").html();
//		System.out.println(lyrics);
		
		song.setName(songTitle);
		
		Artist artist = new Artist();
		artist.setName(artistName);
		artist.setLink(artistLink);
		song.setArtist(artist);
		
		Album album = new Album();
		album.setName(albumName);
		album.setLink(albumLink);
		song.setAlbum(album);
		
		return song;
	}
	
	public synchronized String getImg(Song song){
		String imgName = (song.getAlbum().getLink()+song.getAlbum().getName());
		String hashtext = md5(imgName);
			
//		if(DB.isSaved(song.getAlbum())){
//			return hashtext;
//		}
		
		File file = new File(Config.imgPath + hashtext + ".jpg");
		
		if(file.exists()){
			return hashtext;
		}
		
		String imgUrl = doc.select("img.img_cvr").attr("src");

		if(imgUrl.equals(Config.noImgPath)){
			return "";
		}
			
		try {
			URL url;
			try {
				url = new URL(imgUrl);
			} catch (MalformedURLException ex) {
				Log.logWarning(ex);
				return "";
			}
			
			BufferedImage bi = null;
			int tries = 0;
			// if connection time out, try 10 times
			do{
				try{
					bi = ImageIO.read(url);
					//if no exeption than break the loop
					break;
				} catch (IOException ex) {
					Log.logWarning(ex);
				}
			}while(tries++ < 10);
			
			ImageIO.write(bi,"jpg", file);
			
		} catch (IOException ex) {
			Log.logWarning(ex);
			return "";
		}
		return hashtext;
	}

	public Song[] getArtistSongs(Song song) {
		if(DB.isSaved(song.getArtist())){
			return null;
		}
		
		// change url
		int tries = 0;
		do{
			try {
				URL url = new URL(Config.pandoraUrl + Config.albumUrl + song.getAlbum().getLink());

				doc = Jsoup.parse(url, 10*1000);
				//if no exeption than break the loop
				break;
			} catch (IOException ex) {
				Log.logWarning(ex);
			}		
		}while(tries++ < 10);
		
		if(doc == null){
			return null;
		}
		
//		FileWriter fstream;
//		try {
//			fstream = new FileWriter(Config.filePath + song.getAlbum().getLink().replace("/" , "+") + ".txt");
//			BufferedWriter out = new BufferedWriter(fstream);
//			out.write(doc.toString());
//			//Close the output stream
//			out.close();
//		} catch (IOException ex) {
//			Logger.getLogger(PageParser.class.getName()).log(Level.SEVERE, null, ex);
//		}

		List<Song> albumSongs = new ArrayList<>();
		
		Element el = doc.select("div.discography").first();
		ListIterator<Element> songIterator = el.getElementsByClass("track_link").listIterator();
		Song newSong;
		while (songIterator.hasNext()) {
			Element songEl = songIterator.next();
			if(songEl.attr("href").equals("javascript:void(0);")){
				continue;
			}
			
			newSong = new Song();
			newSong.setName(songEl.attr("title"));
			newSong.setLink(songEl.attr("href"));
			
			albumSongs.add(newSong);
		}
		doc = null;
		songIterator = null;
		return albumSongs.toArray(new Song[0]);
	}
	
	public String md5(String str){
		String hashtext = "";
		try {
			byte[] bytesOfMessage = str.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfMessage);
			BigInteger bigInt = new BigInteger(1, thedigest);
			hashtext = bigInt.toString(16);
			// Now we need to zero pad it to get the full 32 chars.
			while(hashtext.length() < 32 ){
				hashtext = "0"+hashtext;
			}
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			Logger.getLogger(PageParser.class.getName()).log(Level.SEVERE, null, ex);
		}
		return hashtext;
	}

}
