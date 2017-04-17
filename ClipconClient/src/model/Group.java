package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Group {
	private String groupName;
	private String groupPK;
	// 참여자 List
	// 초대자 List
	private History history;
}
