package acs.jpbs.serverUtils;

import java.util.List;

import acs.jpbs.server.core.IPbsObject;

public class jPBSUpdater extends Thread {
	public static enum Method {
		UPDATE_SELF(1),
		UPDATE_CHILDREN(2),
		UPDATE_ALL(3);
		private int id;
		Method(int _id) { this.id = _id; }
	}
	
	private IPbsObject root = null;
	private List<String> rawData = null;
	private Method updateMethod = Method.UPDATE_SELF;
	
	public jPBSUpdater(IPbsObject obj) {
		this.root = obj;
	}
	
	public jPBSUpdater(IPbsObject obj, Method how) {
		this(obj);
		this.updateMethod = how;
	}
	
	public void setRawData(List<String> useThis) {
		this.rawData = useThis;
	}
	
	public void run() {
		jPBSUpdateManager.addToQueue(this);
		
		if(this.updateMethod != Method.UPDATE_CHILDREN) {
			if(this.rawData == null) this.root.updateSelf();
			else this.root.parseRawData(rawData);
		}
		if(this.updateMethod != Method.UPDATE_SELF) this.root.getChildren();
		
		jPBSUpdateManager.subtractFromQueue(this);
	}
}
