package acs.jpbs.core;

import java.util.ArrayList;
import java.util.List;

import acs.jpbs.attrib.PbsResource;
import acs.jpbs.attrib.PbsStateCount;
import acs.jpbs.enums.PbsQueueType;
import acs.jpbs.serverUtils.PbsEnvironment;
import acs.jpbs.serverUtils.PbsUpdater;
import acs.jpbs.utils.Utils;

public class PbsQueueHandler implements IPbsObject {

	private PbsQueue ref;

	public PbsQueueHandler(String _name, PbsServer myServer) {
		this.ref = myServer.getQueue(_name);
		if(this.ref == null) this.ref = new PbsQueue(_name, myServer);
	}
	
	public void getChildren() {
		// get job list output, pass to update children
		this.updateChildren(PbsEnvironment.retrieveQstatOutput(new String[]{"-f ",this.ref.name}));
	}
	
	public PbsQueue getQueue() {
		return this.ref;
	}
		
	public static PbsQueue parseRawData(List<String> rawData) {
		if(rawData == null || rawData.isEmpty()) return null;
		
		String header = rawData.get(0);
		if(!header.startsWith("Queue ")) return null;
		PbsQueue qPtr;
		String qName = header.substring(6).trim();
		qPtr = PbsServer.getInstance().getQueue(qName);
		if(qPtr == null) {
			qPtr = new PbsQueue(qName, PbsServer.getInstance());
			PbsServer.getInstance().addQueue(qPtr);
		}
		
		String[] rawArr;
		List<String[]> rawResourcesAssigned = new ArrayList<String[]>();
		List<String[]> rawDefaultChunk = new ArrayList<String[]>();
		
		for(String rawLine : rawData) {
			rawArr = rawLine.split("=");
			if(rawArr.length != 2) continue;
			rawArr[0] = rawArr[0].trim();
			
			if(rawArr[0].equals("queue_type")) qPtr.type = PbsQueueType.parseQueueType(rawArr[1].trim());
			else if(rawArr[0].equals("state_count")) qPtr.stateCount = PbsStateCount.parseStateCount(rawArr[1].trim());
			else if(rawArr[0].equals("enabled")) qPtr.enabled = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].equals("started")) qPtr.started = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].startsWith("default_chunk")) rawDefaultChunk.add(rawArr);
			else if(rawArr[0].startsWith("resources_assigned")) rawResourcesAssigned.add(rawArr);
		}
		
		qPtr.defaultChunk = PbsResource.processResource(rawDefaultChunk);
		qPtr.resourcesAssigned = PbsResource.processResource(rawResourcesAssigned);
		return qPtr;
	}

	public void updateSelf() {
		parseRawData(PbsEnvironment.retrieveQmgrOutput(new String[]{"list queue "+this.ref.name}));
	}
	
	public void updateChildren(List<String> rawData) {
		// search through data for jobs, spawn updater threads for each
		List<String> rawJobData = new ArrayList<String>(50);
		PbsJobHandler jPtr = null;
		for(String jobs : rawData) {
			// If new job data found
			if(jobs.startsWith("Job Id: ")) {
				// If previous job data exists, initiate updater for job object, pass and clear temp data
				if(rawJobData.size() > 0 && jPtr != null) {
					this.ref.addJob(jPtr.getJob());
					PbsUpdater updateThread = new PbsUpdater((IPbsObject)jPtr);
					updateThread.setRawData(rawJobData);
					updateThread.run();
					rawJobData = new ArrayList<String>();
				}
				int jId = Utils.parseId(jobs.substring(8));
				jPtr = new PbsJobHandler(jId, this.ref.name);
			} else {
				rawJobData.add(jobs);
			}
		}
		if(rawJobData.size() > 0 && jPtr != null) {
			this.ref.addJob(jPtr.getJob());
			PbsUpdater updateThread = new PbsUpdater((IPbsObject)jPtr);
			updateThread.setRawData(rawJobData);
			updateThread.run();
		}
	}
}
