package acs.jpbs.server.core;

import java.util.ArrayList;
import java.util.List;

import acs.jbps.attrib.PbsResource;
import acs.jbps.enums.PbsJobState;
import acs.jpbs.core.PbsQueue;
import acs.jpbs.serverUtils.PbsEnvironment;
import acs.jpbs.utils.Utils;

public class PbsJobHandler extends acs.jpbs.core.PbsJob implements IPbsObject {
	
	public PbsJobHandler(int _id, PbsQueue _q) {
		super(_id, _q);
	}
	
	public void getChildren() { return;	}
	
	public void parseRawData(List<String> rawData) {
		if(rawData == null || rawData.isEmpty()) return;
		String[] rawArr;
		List<String[]> rawResourceList = new ArrayList<String[]>();
		
		for(String rawLine : rawData) {
			rawArr = rawLine.split("=");
			if(rawArr.length != 2) continue;
			rawArr[0] = rawArr[0].trim();
			
			if(rawArr[0].equals("Job_Name")) this.jobName = rawArr[1].trim();
			else if(rawArr[0].equals("Job_Owner")) this.jobOwner = rawArr[1].trim();
			else if(rawArr[0].equals("job_state")) this.state = PbsJobState.getStateByChar(rawArr[1].trim().charAt(0));
			else if(rawArr[0].equals("Error_Path")) this.errorPath = Utils.constructURI(rawArr[1].trim());
			else if(rawArr[0].equals("Output_Path")) this.outputPath = Utils.constructURI(rawArr[1].trim());
			else if(rawArr[0].equals("ctime")) this.ctime = Utils.dateHelper(rawArr[1].trim());
			else if(rawArr[0].equals("Priority")) this.priority = Integer.parseInt(rawArr[1].trim());
			else if(rawArr[0].equals("qtime")) this.qtime = Utils.dateHelper(rawArr[1].trim());
			else if(rawArr[0].equals("comment")) this.comment = rawArr[1].trim();
			else if(rawArr[0].startsWith("Resource_List")) rawResourceList.add(rawArr);
		}
		
		this.resourceList = PbsResource.processResource(rawResourceList);
	}
	
	public void updateSelf() {
		this.parseRawData(PbsEnvironment.retrieveQstatOutput(new String[]{"-f ",Integer.toString(this.id)}));
	}
	
	public void updateChildren(List<String> rawData) { return ; }
}
