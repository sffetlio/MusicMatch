package musicmatch;

public class Artist {
	
	private int id;
	private String name;
	private String link;
	private String pandoraId;
	private Song[] songs;

	public Artist() {
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = new String(link.replace("#!/music/artist/",""));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = new String(name);
	}

	public String getPandoraId() {
		return pandoraId;
	}

	public void setPandoraId(String pandoraId) {
		this.pandoraId = pandoraId;
	}

	public Song[] getSongs() {
		return songs;
	}

	public void setSongs(Song[] songs) {
		this.songs = songs;
	}
	
}
