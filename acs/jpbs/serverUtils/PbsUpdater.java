package acs.jpbs.serverUtils;

import java.util.List;

import acs.jpbs.core.IPbsObject;

public class PbsUpdater extends Thread {
	public static enum Method {
		UPDATE_SELF(),
		UPDATE_CHILDREN(),
		UPDATE_ALL();
		Method() { }
	}
	
	private IPbsObject root = null;
	private List<String> rawData = null;
	private Method updateMethod = Method.UPDATE_SELF;
	
	public PbsUpdater(IPbsObject obj) {
		this.root = obj;
	}
	
	public PbsUpdater(IPbsObject obj, Method how) {
		this(obj);
		this.updateMethod = how;
	}
	
	public void setRawData(List<String> useThis) {
		this.rawData = useThis;
	}
	
	public void run() {
		PbsUpdateManager.addToQueue(this);
		
		if(this.updateMethod != Method.UPDATE_CHILDREN) {
			if(this.rawData == null) this.root.updateSelf();
			else this.root.parseRawData(rawData);
		}
		if(this.updateMethod != Method.UPDATE_SELF) this.root.getChildren();
		
		PbsUpdateManager.subtractFromQueue(this);
	}
}
