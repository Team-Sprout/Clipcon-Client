package model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class History {
	
	private Map<String, Contents> contentsMap = new HashMap<String, Contents>();
	
	/** �깉濡쒖슫 �뜲�씠�꽣媛� �뾽濡쒕뱶�릺硫� �엳�뒪�넗由ъ뿉 add */
	public void addContents(Contents contents) {
		contentsMap.put(contents.getContentsPKName(), contents);
	}

	/** Data瑜� 援щ텇�븯�뒗 怨좎쑀�궎媛믨낵 �씪移섑븯�뒗 Contents瑜� return */
	public Contents getContentsByPK(String contentsPKName) {
		return contentsMap.get(contentsPKName);
	}

}
