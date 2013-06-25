package acs.jpbs.server.core;

import java.util.List;

public class PbsJob extends acs.jpbs.core.PbsJob implements IPbsObject {
	
	public void getChildren() {
		return;
	}
	
	public void parseRawData(List<String> rawData) { }
	
	public void updateSelf() { }
	
	public void updateChildren(List<String> rawData) { }
}
