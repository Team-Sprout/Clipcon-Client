package model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdressBook {
	private Map<String, String> adressBook;
	
	public AdressBook() {
		adressBook = new HashMap<String, String>();
	}
	
	// 주소록 추가
	public void addAdress(String eMail, String name) {
		adressBook.put(eMail, name);
	}
	
	// 주소록 삭제
	public void deleteAdress(String eMail) {
		adressBook.remove(eMail);
	}
	
	// 주소록 검색?
	public String searchAdress(String eMail) {
		return adressBook.get(eMail);
	}
}
