package musicmatch;

public class Config {
	
	static final int processThreads = 12;
	static final String pandoraUrl = "http://pandora.com/content/";
	static final String songUrl = "music/song";
	static final String artistUrl = "music/artist";
	static final String albumUrl = "music/album";
	
	// db config
	static final String host = "localhost";
	static final String port = "3306";
	static final String table = "music_match";
	static final String username = "";
	static final String password = "";
	static final String dataBaseUrl = "jdbc:mysql://"+host+":"+port+"/"+table;
	
	static final String imgPath = "D:\\mmImg\\";
	static final String noImgPath = "http://www.pandora.com/images/no_album_art.jpg";
	static final String filePath = "D:\\mmFiles\\";
}
