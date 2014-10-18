package musicmatch;

public class Album {
	
	private int id;
	private String name;
	private String link;
	private String img;
	private String pandoraId;

	public Album() {
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
//		if(link.startsWith("/")){
//			this.link = link.replaceFirst("/", "");
//		}else{
			this.link = new String(link.replace("#!/music/album/",""));
//		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = new String(name);
	}
	
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getPandoraId() {
		return pandoraId;
	}

	public void setPandoraId(String pandoraId) {
		this.pandoraId = pandoraId;
	}
}
