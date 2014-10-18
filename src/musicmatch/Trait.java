package musicmatch;

public class Trait {
	
	private int id;
	private String name;
	private String pandoraId;

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
		this.name = name;
	}

	public String getPandoraId() {
		return pandoraId;
	}

	public void setPandoraId(String pandoraId) {
		this.pandoraId = pandoraId;
	}
	
	public void compact(){
		name = null;
		pandoraId = null;
	}
	
}
