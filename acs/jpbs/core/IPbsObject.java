package acs.jpbs.core;

import java.util.List;

public interface IPbsObject {
	
	void getChildren();

	void parseRawData(List<String> rawData);
	
	void updateSelf();
	
	void updateChildren(List<String> rawData);
}
