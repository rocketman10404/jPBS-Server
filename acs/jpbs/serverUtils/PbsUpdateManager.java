package acs.jpbs.serverUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import acs.jpbs.core.PbsServer;
import acs.jpbs.core.PbsServerHandler;
import acs.jpbs.server.jPBSServer;
import acs.jpbs.utils.Logger;

public class PbsUpdateManager {
	private static BlockingQueue<PbsUpdater> updateQueue = new ArrayBlockingQueue<PbsUpdater>(5);
	
	private PbsUpdateManager() { }
	
	public static void addToQueue(PbsUpdater newThread) {
		try {
			updateQueue.put(newThread);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void beginFullUpdate() {
		new PbsUpdater(PbsServerHandler.getInstance(), PbsUpdater.Method.UPDATE_SELF).start();
		new PbsUpdater(null, PbsUpdater.Method.UPDATE_ALL_QUEUES).start();
		new PbsUpdater(null, PbsUpdater.Method.UPDATE_ALL_JOBS).start();
	}
	
	public static void subtractFromQueue(PbsUpdater finishedThread) {
		try {
			updateQueue.remove(finishedThread);
		} catch (Exception e) {
			Logger.logException("Update Manager exception", e);
		}
		if(updateQueue.peek() == null) updateCompletedEvent();
	}
	
	public static void updateAllJobs() {
		List<String> rawData = PbsEnvironment.retrieveQstatOutput(new String[]{"-f"});
		List<String> rawJobData = new ArrayList<String>(50);
		for(String jobs : rawData) {
			if(jobs.startsWith("Job Id: ")) {
				if(rawJobData.size() > 0) {
					PbsUpdater updateThread = new PbsUpdater(null, PbsUpdater.Method.UPDATE_JOB);
					updateThread.setRawData(rawJobData);
					updateThread.start();
					rawJobData = new ArrayList<String>();
				}
			}
			rawJobData.add(jobs);
		}
		if(rawJobData.size() > 0) {
			PbsUpdater updateThread = new PbsUpdater(null, PbsUpdater.Method.UPDATE_JOB);
			updateThread.setRawData(rawJobData);
			updateThread.start();
		}
	}
	
	public static void updateAllQueues() {
		List<String> rawData = PbsEnvironment.retrieveQmgrOutput(new String[]{"list queue @"+jPBSServer.pbsHost});
		List<String> rawQueueData = new ArrayList<String>(15);
		for(String queues : rawData) {
			if(queues.startsWith("Queue ")) {
				if(rawQueueData.size() > 0) {
					PbsUpdater updateThread = new PbsUpdater(null, PbsUpdater.Method.UPDATE_QUEUE);
					updateThread.setRawData(rawQueueData);
					updateThread.start();
					rawQueueData = new ArrayList<String>();
				}
			}
			rawQueueData.add(queues);
		}
		if(rawQueueData.size() > 0) {
			PbsUpdater updateThread = new PbsUpdater(null, PbsUpdater.Method.UPDATE_QUEUE);
			updateThread.setRawData(rawQueueData);
			updateThread.start();
		}
	}
	
	private static void updateCompletedEvent() {
		PbsServer sHandler = jPBSServer.pbsServer.getServer();
		
		Logger.logInfo("Update completed.");
		Logger.logInfo("PBS Server name: '"+sHandler.getHostName()+"'");
		Logger.logInfo("Queues found: "+sHandler.getNumQueues());
		Logger.logInfo("Total jobs found: "+sHandler.getNumJobs());
	}
}
