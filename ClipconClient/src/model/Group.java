package model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Group {
	private String primaryKey;
	@Setter
	private List<User> userList;
	private History history = new History();

	public Group(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void addContents(Contents contents) {
		history.addContents(contents);
	}

	public Contents getContents(String key) {
		return history.getContentsByPK(key);
	}
}
