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
	//private String name;
	private List<User> userList;
	private History history;
	
	public Group(String primaryKey) {
		this.primaryKey = primaryKey;
	}
}
