package musicmatch;

public class Song {
	
	private int id;
	private String name;
	private String link;
	private Trait[] features;
	
	private Artist artist;
	private Album album;
	
	private Song[] similar;
	
	public Song() {
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = new String(name);
	}
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = new String(link.replace("#!/music/song/",""));
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public Trait[] getFeatures() {
		return features;
	}

	public void setFeatures(Trait[] features) {
		this.features = features;
	}

	public Song[] getSimilar() {
		return similar;
	}

	public void setSimilar(Song[] similar) {
		this.similar = similar;
	}
		
//	@Override
//	public String toString() {
//		String featuresStr = "";
//		if(features != null)
//		for (int i = 0; i < features.length; i++) {
//			featuresStr += features[i].getName()+'\n';
//		}
//		String similarStr = "";
//		if(similar != null)
//		for (int i = 0; i < similar.length; i++) {
//			similarStr += similar[i].getLink()+'\n';
//		}
//		
//		return "Song{" +
//			"\nid=" + id +
//			"\ntitle=" + name +
//			"\nlink=" + link +
//			"\nartist=" + artist.getName() +
//			"\nartistLink=" + artist.getLink() +
//			"\nalbum=" + album.getName() +
//			"\nalbumLink=" + album.getLink() +
//			"\n\nFeatures: \n" +
//			featuresStr +
//			"\n\nSimilar Songs: \n" +
//			similarStr +
//			"}";
//	}

//	@Override
//	public boolean equals(Object obj) {
//		if (obj == null) {
//			return false;
//		}
//		if (getClass() != obj.getClass()) {
//			return false;
//		}
//		final Song other = (Song) obj;
//		if (!Objects.equals(this.link, other.link)) {
//			return false;
//		}
//		return true;
//	}
//
//	@Override
//	public int hashCode() {
//		int hash = 3;
//		hash = 79 * hash + Objects.hashCode(this.link);
//		return hash;
//	}
	
}
