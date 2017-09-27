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

	/** Add to history when new data is uploaded */
	public void addContents(Contents contents) {
		history.addContents(contents);
	}

	/** Return contents that match the primary key value that distinguishes the data */
	public Contents getContents(String key) {
		return history.getContentsByPK(key);
	}
}
