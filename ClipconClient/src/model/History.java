package model;

import java.util.HashMap;
import java.util.Map;

public class History {
	
	private Map<String, Contents> contentsMap = new HashMap<String, Contents>();
	
	/** 새로운 데이터가 업로드되면 히스토리에 add */
	public void addContents(Contents contents) {
		contentsMap.put(contents.getContentsPKName(), contents);
	}

	/** Data를 구분하는 고유키값과 일치하는 Contents를 return */
	public Contents getContentsByPK(String contentsPKName) {
		return contentsMap.get(contentsPKName);
	}

}
