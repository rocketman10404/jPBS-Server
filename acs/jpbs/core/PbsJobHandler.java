package acs.jpbs.core;

import java.util.ArrayList;
import java.util.List;

import acs.jpbs.attrib.PbsResource;
import acs.jpbs.enums.PbsJobState;
import acs.jpbs.serverUtils.PbsEnvironment;
import acs.jpbs.utils.Utils;

public class PbsJobHandler implements IPbsObject {
	
	private PbsJob ref;

	public PbsJobHandler(int _id, String _q) {
		PbsQueue qObj = PbsServer.getInstance().getQueue(_q);
		if(qObj != null) {
			this.ref = qObj.getJob(_id);
		}
		if(this.ref == null) {
			this.ref = new PbsJob(_id, _q);
			if(qObj == null) {
				qObj = new PbsQueue(_q, PbsServer.getInstance());
				PbsServer.getInstance().addQueue(qObj);
			}
			qObj.addJob(this.ref);
		}
	}
	
	public void getChildren() { return;	}
	
	public PbsJob getJob() {
		return this.ref;
	}
	
	public void parseRawData(List<String> rawData) {
		if(rawData == null || rawData.isEmpty()) return;
		String[] rawArr;
		List<String[]> rawResourceList = new ArrayList<String[]>();
		
		for(String rawLine : rawData) {
			rawArr = rawLine.split("=");
			if(rawArr.length != 2) continue;
			rawArr[0] = rawArr[0].trim();
			
			if(rawArr[0].equals("Job_Name")) this.ref.jobName = rawArr[1].trim();
			else if(rawArr[0].equals("Job_Owner")) this.ref.jobOwner = rawArr[1].trim();
			else if(rawArr[0].equals("job_state")) this.ref.state = PbsJobState.getStateByChar(rawArr[1].trim().charAt(0));
			else if(rawArr[0].equals("Error_Path")) this.ref.errorPath = Utils.constructURI(rawArr[1].trim());
			else if(rawArr[0].equals("Output_Path")) this.ref.outputPath = Utils.constructURI(rawArr[1].trim());
			else if(rawArr[0].equals("ctime")) this.ref.ctime = Utils.dateHelper(rawArr[1].trim());
			else if(rawArr[0].equals("Priority")) this.ref.priority = Integer.parseInt(rawArr[1].trim());
			else if(rawArr[0].equals("qtime")) this.ref.qtime = Utils.dateHelper(rawArr[1].trim());
			else if(rawArr[0].equals("comment")) this.ref.comment = rawArr[1].trim();
			else if(rawArr[0].startsWith("Resource_List")) rawResourceList.add(rawArr);
		}
		
		this.ref.resourceList = PbsResource.processResource(rawResourceList);
	}
	
	public void updateSelf() {
		this.parseRawData(PbsEnvironment.retrieveQstatOutput(new String[]{"-f ",Integer.toString(this.ref.id)}));
	}
	
	public void updateChildren(List<String> rawData) { return ; }
}
