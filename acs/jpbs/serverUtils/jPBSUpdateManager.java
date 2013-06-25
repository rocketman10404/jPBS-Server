package acs.jpbs.serverUtils;

import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import acs.jpbs.server.jPBSServer;
import acs.jpbs.server.core.PbsQueue;
import acs.jpbs.server.core.PbsServer;
import acs.jpbs.utils.jPBSLogger;

public class jPBSUpdateManager {
	private static BlockingQueue<jPBSUpdater> updateQueue = new ArrayBlockingQueue<jPBSUpdater>(10);
	
	private jPBSUpdateManager() { }
	
	public static void addToQueue(jPBSUpdater newThread) {
		updateQueue.add(newThread);
	}
	
	public static void beginUpdate(PbsServer head) {
		jPBSUpdater updater = new jPBSUpdater(head, jPBSUpdater.Method.UPDATE_SELF);
		updater.start();
		
		updater = new jPBSUpdater(head, jPBSUpdater.Method.UPDATE_CHILDREN);
		updater.start();
	}
	
	public static void subtractFromQueue(jPBSUpdater finishedThread) {
		try {
			updateQueue.remove(finishedThread);
		} catch (Exception e) {
			jPBSLogger.logException("Update Manager exception", e);
		}
		if(updateQueue.peek() == null) updateCompletedEvent();
	}
	
	private static void updateCompletedEvent() {
		jPBSLogger.logInfo("Update completed.");
		jPBSLogger.logInfo("Server name: '"+jPBSServer.getInstance().server.getHostName()+"'");
		jPBSLogger.logInfo("Queues found: "+jPBSServer.getInstance().server.queues.size());
		int jobs = 0;
		for(Entry<String, PbsQueue> qEntry : jPBSServer.getInstance().server.queues.entrySet()) {
			jobs += qEntry.getValue().jobs.size();
		}
		jPBSLogger.logInfo("Jobs found: "+jobs);
	}
}
