package acs.jpbs.serverUtils;

import java.util.List;

import acs.jpbs.core.IPbsObject;
import acs.jpbs.core.PbsJobHandler;
import acs.jpbs.core.PbsQueueHandler;
import acs.jpbs.core.PbsServerHandler;

public class PbsUpdater extends Thread {
	public static enum Method {
		UPDATE_ALL_QUEUES(),
		UPDATE_ALL_JOBS(),
		UPDATE_SELF(),
		UPDATE_SELF_AND_CHILDREN(),
		UPDATE_QUEUE(),
		UPDATE_JOB();
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
	
	private void parseRawData(List<String> rawData) {
		if(this.root instanceof PbsServerHandler) PbsServerHandler.parseRawData(rawData);
		else if(this.root instanceof PbsQueueHandler) PbsQueueHandler.parseRawData(rawData);
		else if(this.root instanceof PbsJobHandler) PbsJobHandler.parseRawData(rawData);
	}
	
	public void setRawData(List<String> useThis) {
		this.rawData = useThis;
	}
	
	public void run() {
		PbsUpdateManager.addToQueue(this);
		
		switch(this.updateMethod) {
		case UPDATE_SELF:
			if(this.rawData == null) this.root.updateSelf();
			else this.parseRawData(rawData);
			break;
		case UPDATE_SELF_AND_CHILDREN:
			if(this.rawData == null) this.root.updateSelf();
			else this.parseRawData(rawData);
			this.root.getChildren();
			break;
		case UPDATE_ALL_QUEUES:
			PbsUpdateManager.updateAllQueues();
			break;
		case UPDATE_ALL_JOBS:
			PbsUpdateManager.updateAllJobs();
			break;
		case UPDATE_QUEUE:
			if(this.rawData == null) break;
			PbsQueueHandler.parseRawData(rawData);
			break;
		case UPDATE_JOB:
			if(this.rawData == null) break;
			PbsJobHandler.parseRawData(rawData);
			break;
		default:
			break;		
		}
		
		PbsUpdateManager.subtractFromQueue(this);
	}
}
