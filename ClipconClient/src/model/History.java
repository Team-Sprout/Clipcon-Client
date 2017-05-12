package model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class History {

	private Map<String, Contents> contentsMap = new HashMap<String, Contents>();

	/** Add to history when new data is uploaded */
	public void addContents(Contents contents) {
		contentsMap.put(contents.getContentsPKName(), contents);
	}

	/** Return contents that match the primary key value that distinguishes the data */
	public Contents getContentsByPK(String contentsPKName) {
		return contentsMap.get(contentsPKName);
	}

}
