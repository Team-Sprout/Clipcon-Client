package model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Group {
	private String primaryKey;
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
