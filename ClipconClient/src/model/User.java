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
}
