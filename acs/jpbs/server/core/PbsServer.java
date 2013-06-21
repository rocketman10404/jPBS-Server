package acs.jpbs.server.core;

import java.util.ArrayList;
import java.util.List;

import acs.jbps.attrib.PbsServerLicenses;
import acs.jbps.attrib.PbsStateCount;
import acs.jbps.enums.PbsServerState;
import acs.jpbs.core.PbsQueue;
import acs.jpbs.serverUtils.jPBSEnvironment;

public class PbsServer extends acs.jpbs.core.PbsServer implements IPbsObject {

	private void parseQstatData(List<String> rawData) {
		String[] rawArr;
		List<String> rawResourcesAssigned = new ArrayList<String>();
		List<String> rawDefaultChunk = new ArrayList<String>();
		
		for(String rawLine : rawData) {
			rawArr = rawLine.split("=");
			if(rawArr.length != 2) continue;
			rawArr[0] = rawArr[0].trim();
			
			if(rawArr[0].equals("server_state")) this.state = PbsServerState.parseState(rawArr[1].trim());
			else if(rawArr[0].equals("server_host")) this.host = rawArr[1].trim();
			else if(rawArr[0].equals("scheduling")) this.scheduling = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].equals("state_count")) this.stateCount = PbsStateCount.parseStateCount(rawArr[1].trim());
			else if(rawArr[0].equals("default_queue")) {
				this.defaultQueue = new PbsQueue(rawArr[1].trim());
				this.queues.put(this.defaultQueue.getName(), this.defaultQueue);
			}
			else if(rawArr[0].equals("scheduler_iteration")) this.schedulerIteration = Integer.parseInt(rawArr[1].trim());
			else if(rawArr[0].equals("FLicenses")) this.fLicenses = Integer.parseInt(rawArr[1].trim());
			else if(rawArr[0].equals("resv_enable")) this.resvEnable = Boolean.parseBoolean(rawArr[1].trim());
			else if(rawArr[0].equals("license_count")) this.licenseCount = PbsServerLicenses.parseServerLicenses(rawArr[1].trim());
			else if(rawArr[0].equals("pbs_version")) this.version = rawArr[1].trim();
			else if(rawArr[0].startsWith("default_chunk")) rawDefaultChunk.add(rawLine);
			else if(rawArr[0].startsWith("resources_assigned")) rawResourcesAssigned.add(rawLine);
		}
	}
	
	public void updateSelf() {
		this.parseQstatData(jPBSEnvironment.retrieveQstatOutput(new String[]{"-Bf"}));
	}
	
	public void updateChildren() { }
}
