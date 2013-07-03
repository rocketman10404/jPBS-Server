package acs.jpbs.serverUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import acs.jpbs.core.PbsServer;
import acs.jpbs.server.jPBSServer;
import acs.jpbs.server.core.PbsServerHandler;
import acs.jpbs.utils.Logger;

public class PbsUpdateManager {
	private static BlockingQueue<PbsUpdater> updateQueue = new ArrayBlockingQueue<PbsUpdater>(10);
	
	private PbsUpdateManager() { }
	
	public static void addToQueue(PbsUpdater newThread) {
		updateQueue.add(newThread);
	}
	
	public static void beginUpdate(PbsServerHandler head) {
		PbsUpdater updater = new PbsUpdater(head, PbsUpdater.Method.UPDATE_SELF);
		updater.start();
		
		updater = new PbsUpdater(head, PbsUpdater.Method.UPDATE_CHILDREN);
		updater.start();
	}
	
	public static void subtractFromQueue(PbsUpdater finishedThread) {
		try {
			updateQueue.remove(finishedThread);
		} catch (Exception e) {
			Logger.logException("Update Manager exception", e);
		}
		if(updateQueue.peek() == null) updateCompletedEvent();
	}
	
	private static void updateCompletedEvent() {
		PbsServer sHandler = jPBSServer.pbsServer;
		
		Logger.logInfo("Update completed.");
		Logger.logInfo("PBS Server name: '"+sHandler.getHostName()+"'");
		Logger.logInfo("Queues found: "+sHandler.getNumQueues());
		Logger.logInfo("Total jobs found: "+sHandler.getNumJobs());
	}
}
