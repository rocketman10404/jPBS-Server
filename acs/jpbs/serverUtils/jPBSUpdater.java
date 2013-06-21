package acs.jpbs.serverUtils;

import java.util.List;

import acs.jpbs.server.core.IPbsObject;

public class jPBSUpdater extends Thread {
	private IPbsObject root = null;
	private boolean updateSelf = false;
	private List<String> rawData = null;
	
	public jPBSUpdater(IPbsObject obj) {
		this.root = obj;
	}
	
	public jPBSUpdater(IPbsObject obj, boolean self) {
		this(obj);
		this.updateSelf = self;
	}
	
	public void setRawData(List<String> useThis) {
		this.rawData = useThis;
	}
	
	public void run() {
		if(this.updateSelf) {
			if(this.rawData == null) this.root.updateSelf();
			else this.root.parseQstatData(rawData);
		}
		this.root.getChildren();
	}
}
