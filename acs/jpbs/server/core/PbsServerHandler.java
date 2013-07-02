package acs.jpbs.server.core;

import java.util.ArrayList;
import java.util.List;

import acs.jpbs.attrib.PbsResource;
import acs.jpbs.attrib.PbsServerLicenses;
import acs.jpbs.attrib.PbsStateCount;
import acs.jpbs.enums.PbsServerState;
import acs.jpbs.server.core.PbsQueueHandler;
import acs.jpbs.serverUtils.PbsEnvironment;
import acs.jpbs.serverUtils.PbsUpdater;

public class PbsServerHandler extends acs.jpbs.core.PbsServer implements IPbsObject {
	
	private static PbsServerHandler instance = null;
	private static final long serialVersionUID = 1829196769843795325L;

	private PbsServerHandler() {
		super();
	}
	
	public void getChildren() {
		// get queue list output, pass to update children
		this.updateChildren(PbsEnvironment.retrieveQmgrOutput(new String[]{"list queue @hn1"}));
	}
	
	public static PbsServerHandler getInstance() {
		if(instance == null) {
			instance = new PbsServerHandler();
		}
		return instance;
	}
		
	public void parseRawData(List<String> rawData) {
		String[] rawArr;
		List<String[]> rawResourcesAssigned = new ArrayList<String[]>();
		List<String[]> rawDefaultChunk = new ArrayList<String[]>();
		
		for(String rawLine : rawData) {
			rawArr = rawLine.split("=");
			if(rawArr.length != 2) continue;
			rawArr[0] = rawArr[0].trim();
			
			if(rawArr[0].equals("server_state")) this.state = PbsServerState.parseState(rawArr[1].trim());
			else if(rawArr[0].equals("server_host")) this.host = rawArr[1].trim();
			else if(rawArr[0].equals("scheduling")) this.scheduling = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].equals("state_count")) this.stateCount = PbsStateCount.parseStateCount(rawArr[1].trim());
			else if(rawArr[0].equals("default_queue")) this.defaultQueueKey = rawArr[1].trim();
			else if(rawArr[0].equals("scheduler_iteration")) this.schedulerIteration = Integer.parseInt(rawArr[1].trim());
			else if(rawArr[0].equals("FLicenses")) this.fLicenses = Integer.parseInt(rawArr[1].trim());
			else if(rawArr[0].equals("resv_enable")) this.resvEnable = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].equals("license_count")) this.licenseCount = PbsServerLicenses.parseServerLicenses(rawArr[1].trim());
			else if(rawArr[0].equals("pbs_version")) this.version = rawArr[1].trim();
			else if(rawArr[0].startsWith("default_chunk")) rawDefaultChunk.add(rawArr);
			else if(rawArr[0].startsWith("resources_assigned")) rawResourcesAssigned.add(rawArr);
		}
		
		this.defaultChunk = PbsResource.processResource(rawDefaultChunk);
		this.resourcesAssigned = PbsResource.processResource(rawResourcesAssigned);
	}
	
	public void updateSelf() {
		this.parseRawData(PbsEnvironment.retrieveQmgrOutput(new String[]{"list server"}));
	}
	
	public void updateChildren(List<String> rawData) { 
		// search through data for queues, spawn updater threads for each
		List<String> rawQueueData = new ArrayList<String>();
		PbsQueueHandler qPtr = null;
		for(String queues : rawData) {
			// If new queue data found
			if(queues.startsWith("Queue ")) {
				// If previous queue data exists, initiate updater for queue object, pass and clear temp data
				if(rawQueueData.size() > 0 && qPtr != null) {
					this.queueMapWriteLock.lock();
					try {
						this.queues.put(qPtr.getName(), qPtr);
					} finally {
						this.queueMapWriteLock.unlock();
					}
					PbsUpdater updateThread = new PbsUpdater((IPbsObject)qPtr, PbsUpdater.Method.UPDATE_ALL);
					updateThread.setRawData(rawQueueData);
					updateThread.run();
					rawQueueData = new ArrayList<String>();
				}
				String qName = queues.substring(6);
				this.queueMapReadLock.lock();
				try {
					if(!this.queues.containsKey(qName)) {
						qPtr = new PbsQueueHandler(qName, this);
					} else {
						qPtr = (PbsQueueHandler)this.queues.get(qName);
					}
				} finally {
					this.queueMapReadLock.unlock();
				}
			} else {
				rawQueueData.add(queues);
			}
		}
		if(rawQueueData.size() > 0 && qPtr != null) {
			this.queueMapWriteLock.lock();
			try {
				this.queues.put(qPtr.getName(), qPtr);
			} finally {
				this.queueMapWriteLock.unlock();
			}
			PbsUpdater updateThread = new PbsUpdater((IPbsObject)qPtr, PbsUpdater.Method.UPDATE_ALL);
			updateThread.setRawData(rawQueueData);
			updateThread.run();
		}
	}
}
