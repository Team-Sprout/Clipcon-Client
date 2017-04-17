package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Group {
	private String primaryKey;
	private String name;
	// 참여자 List
	// 초대자 List
	private History history;
}
