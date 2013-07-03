package acs.jpbs.server.core;

import java.util.ArrayList;
import java.util.List;

import acs.jpbs.attrib.PbsResource;
import acs.jpbs.attrib.PbsStateCount;
import acs.jpbs.enums.PbsQueueType;
import acs.jpbs.serverUtils.PbsEnvironment;
import acs.jpbs.serverUtils.PbsUpdater;
import acs.jpbs.utils.Utils;

public class PbsQueueHandler extends acs.jpbs.core.PbsQueue implements IPbsObject {

	private static final long serialVersionUID = 8184349070425307083L;

	public PbsQueueHandler(String _name, PbsServerHandler myServer) {
		super(_name, myServer);
	}
	
	public void getChildren() {
		// get job list output, pass to update children
		this.updateChildren(PbsEnvironment.retrieveQstatOutput(new String[]{"-f ",this.name}));
	}
		
	public void parseRawData(List<String> rawData) {
		if(rawData == null || rawData.isEmpty()) return;
		String[] rawArr;
		List<String[]> rawResourcesAssigned = new ArrayList<String[]>();
		List<String[]> rawDefaultChunk = new ArrayList<String[]>();
		
		for(String rawLine : rawData) {
			rawArr = rawLine.split("=");
			if(rawArr.length != 2) continue;
			rawArr[0] = rawArr[0].trim();
			
			if(rawArr[0].equals("queue_type")) this.type = PbsQueueType.parseQueueType(rawArr[1].trim());
			else if(rawArr[0].equals("state_count")) this.stateCount = PbsStateCount.parseStateCount(rawArr[1].trim());
			else if(rawArr[0].equals("enabled")) this.enabled = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].equals("started")) this.started = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].startsWith("default_chunk")) rawDefaultChunk.add(rawArr);
			else if(rawArr[0].startsWith("resources_assigned")) rawResourcesAssigned.add(rawArr);
		}
		
		this.defaultChunk = PbsResource.processResource(rawDefaultChunk);
		this.resourcesAssigned = PbsResource.processResource(rawResourcesAssigned);
	}

	public void updateSelf() {
		this.parseRawData(PbsEnvironment.retrieveQmgrOutput(new String[]{"list queue "+this.name}));
	}
	
	public void updateChildren(List<String> rawData) {
		// search through data for jobs, spawn updater threads for each
		List<String> rawJobData = new ArrayList<String>();
		PbsJobHandler jPtr = null;
		for(String jobs : rawData) {
			// If new job data found
			if(jobs.startsWith("Job Id: ")) {
				// If previous job data exists, initiate updater for job object, pass and clear temp data
				if(rawJobData.size() > 0 && jPtr != null) {
					this.addJob(jPtr);
					PbsUpdater updateThread = new PbsUpdater((IPbsObject)jPtr);
					updateThread.setRawData(rawJobData);
					updateThread.run();
					rawJobData = new ArrayList<String>();
				}
				int jId = Utils.parseId(jobs.substring(8));
				jPtr = (PbsJobHandler)this.getJob(jId);
				if(jPtr == null) jPtr = new PbsJobHandler(jId, this.name);
			} else {
				rawJobData.add(jobs);
			}
		}
		if(rawJobData.size() > 0 && jPtr != null) {
			this.addJob(jPtr);
			PbsUpdater updateThread = new PbsUpdater((IPbsObject)jPtr);
			updateThread.setRawData(rawJobData);
			updateThread.run();
		}
	}
}
