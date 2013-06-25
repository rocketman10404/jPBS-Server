package acs.jpbs.server.core;

import java.util.ArrayList;
import java.util.List;

import acs.jbps.attrib.PbsResource;
import acs.jbps.attrib.PbsStateCount;
import acs.jbps.enums.PbsQueueType;

public class PbsQueue extends acs.jpbs.core.PbsQueue implements IPbsObject {

	public PbsQueue(String _name, PbsServer myServer) {
		super(_name, myServer);
	}
	
	public void getChildren() {
		
	}
	
	public void parseRawData(List<String> rawData) {
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

	public void updateSelf() { }
	
	public void updateChildren(List<String> rawData) { }
}
