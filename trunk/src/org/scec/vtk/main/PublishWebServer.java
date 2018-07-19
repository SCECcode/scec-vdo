package org.scec.vtk.main;

public class PublishWebServer {

	private String title;
	private String author;
	private String server;
	private String username;
	private String password;
	private String description;
	
	public PublishWebServer()
	{
		this.setTitle("N/A");
		this.setAuthor("N/A");
		this.setServer("N/A");
		this.setUsername("N/A");
		this.setPassword("N/A");
		this.setDescription("N/A");
		
		
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
