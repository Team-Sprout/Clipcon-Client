package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
	private String email;
	private String name;
	private AddressBook addressBook; // ¡÷º“∑œ
	private Group group;
	
	public User(String email, String name) {
		this.email = email;
		this.name = name;
		addressBook = null;
		group = null;
	}
}
