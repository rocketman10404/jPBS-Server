package acs.jpbs.core;

import java.util.ArrayList;
import java.util.List;

import acs.jpbs.attrib.PbsResource;
import acs.jpbs.attrib.PbsServerLicenses;
import acs.jpbs.attrib.PbsStateCount;
import acs.jpbs.core.PbsQueueHandler;
import acs.jpbs.core.PbsServer;
import acs.jpbs.enums.PbsServerState;
import acs.jpbs.serverUtils.PbsEnvironment;
import acs.jpbs.serverUtils.PbsUpdater;

public class PbsServerHandler implements IPbsObject {
	
	private static PbsServerHandler instance = null;
	private static PbsServer ref;

	private PbsServerHandler() {
		ref = PbsServer.getInstance();
	}
	
	public void getChildren() {
		// get queue list output, pass to update children
		this.updateChildren(PbsEnvironment.retrieveQmgrOutput(new String[]{"list queue @hn1"}));
	}
	
	public PbsJob[] getJobArray() {
		return ref.getJobArray();
	}
	
	public PbsQueue[] getQueueArray() {
		return ref.getQueueArray();
	}
	
	public PbsServer getServer() {
		return ref;
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
			
			if(rawArr[0].equals("server_state")) ref.state = PbsServerState.parseState(rawArr[1].trim());
			else if(rawArr[0].equals("server_host")) ref.host = rawArr[1].trim();
			else if(rawArr[0].equals("scheduling")) ref.scheduling = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].equals("state_count")) ref.stateCount = PbsStateCount.parseStateCount(rawArr[1].trim());
			else if(rawArr[0].equals("default_queue")) ref.defaultQueueKey = rawArr[1].trim();
			else if(rawArr[0].equals("scheduler_iteration")) ref.schedulerIteration = Integer.parseInt(rawArr[1].trim());
			else if(rawArr[0].equals("FLicenses")) ref.fLicenses = Integer.parseInt(rawArr[1].trim());
			else if(rawArr[0].equals("resv_enable")) ref.resvEnable = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].equals("license_count")) ref.licenseCount = PbsServerLicenses.parseServerLicenses(rawArr[1].trim());
			else if(rawArr[0].equals("pbs_version")) ref.version = rawArr[1].trim();
			else if(rawArr[0].startsWith("default_chunk")) rawDefaultChunk.add(rawArr);
			else if(rawArr[0].startsWith("resources_assigned")) rawResourcesAssigned.add(rawArr);
		}
		
		ref.defaultChunk = PbsResource.processResource(rawDefaultChunk);
		ref.resourcesAssigned = PbsResource.processResource(rawResourcesAssigned);
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
					ref.addQueue(qPtr.getQueue());
					PbsUpdater updateThread = new PbsUpdater((IPbsObject)qPtr, PbsUpdater.Method.UPDATE_ALL);
					updateThread.setRawData(rawQueueData);
					updateThread.run();
					rawQueueData = new ArrayList<String>();
				}
				String qName = queues.substring(6);
				qPtr = new PbsQueueHandler(qName, ref);
			} else {
				rawQueueData.add(queues);
			}
		}
		if(rawQueueData.size() > 0 && qPtr != null) {
			ref.addQueue(qPtr.getQueue());
			PbsUpdater updateThread = new PbsUpdater((IPbsObject)qPtr, PbsUpdater.Method.UPDATE_ALL);
			updateThread.setRawData(rawQueueData);
			updateThread.run();
		}
	}
}
