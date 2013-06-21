package acs.jpbs.server.core;

import java.util.List;

public interface IPbsObject {
	
	void getChildren();

	void parseQstatData(List<String> rawData);
	
	void updateSelf();
	
	void updateChildren(List<String> rawData);
}
