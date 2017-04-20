package model;

import java.util.Map;

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
	private String name;
	private Map<String, String> userList;
	// √ ¥Î¿⁄ List
	private History history;
}
