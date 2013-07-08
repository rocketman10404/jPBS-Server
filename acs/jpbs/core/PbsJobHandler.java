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
	
	public static PbsJob parseRawData(List<String> rawData) {
		if(rawData == null || rawData.isEmpty()) return null;
		
		String header = rawData.get(0);
		if(!header.startsWith("Job Id: ")) return null;
		int jId = Utils.parseId(header.substring(8));
		PbsJob jTemp = new PbsJob(jId, "");
		
		String[] rawArr;
		List<String[]> rawResourceList = new ArrayList<String[]>(50);
		
		for(String rawLine : rawData) {
			rawArr = rawLine.split("=");
			if(rawArr.length != 2) continue;
			rawArr[0] = rawArr[0].trim();
			
			if(rawArr[0].equals("Job_Name")) jTemp.jobName = rawArr[1].trim();
			else if(rawArr[0].equals("Job_Owner")) jTemp.jobOwner = rawArr[1].trim();
			else if(rawArr[0].equals("job_state")) jTemp.state = PbsJobState.getStateByChar(rawArr[1].trim().charAt(0));
			else if(rawArr[0].equals("queue")) jTemp.queueKey = rawArr[1].trim();
			else if(rawArr[0].equals("Error_Path")) jTemp.errorPath = Utils.constructURI(rawArr[1].trim());
			else if(rawArr[0].equals("Output_Path")) jTemp.outputPath = Utils.constructURI(rawArr[1].trim());
			else if(rawArr[0].equals("ctime")) jTemp.ctime = Utils.dateHelper(rawArr[1].trim());
			else if(rawArr[0].equals("Priority")) jTemp.priority = Integer.parseInt(rawArr[1].trim());
			else if(rawArr[0].equals("qtime")) jTemp.qtime = Utils.dateHelper(rawArr[1].trim());
			else if(rawArr[0].equals("comment")) jTemp.comment = rawArr[1].trim();
			else if(rawArr[0].startsWith("Resource_List")) rawResourceList.add(rawArr);
		}
		
		jTemp.resourceList = PbsResource.processResource(rawResourceList);
		PbsQueue qPtr = PbsServer.getInstance().getQueueSafe(jTemp.queueKey);
		PbsJob jPtr;
		jPtr = qPtr.getJob(jId);
		if(jPtr == null) {
			jPtr = jTemp;
			qPtr.addJob(jPtr);
			System.out.println("Job "+jId+" created.");
		} else {
			jPtr.makeCopy(jTemp);
			System.out.println("Job "+jId+" copied.");
		}
		return jPtr;
	}
	
	public void updateSelf() {
		parseRawData(PbsEnvironment.retrieveQstatOutput(new String[]{"-f ",Integer.toString(this.ref.id)}));
	}
	
	public void updateChildren(List<String> rawData) { return ; }
}
