package acs.jpbs.serverUtils;

import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import acs.jpbs.core.PbsQueue;
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
		Logger.logInfo("Update completed.");
		Logger.logInfo("Server name: '"+jPBSServer.getInstance().server.getHostName()+"'");
		Logger.logInfo("Queues found: "+jPBSServer.getInstance().server.queues.size());
		int jobs = 0;
		for(Entry<String, PbsQueue> qEntry : jPBSServer.getInstance().server.queues.entrySet()) {
			jobs += qEntry.getValue().jobs.size();
			Logger.logInfo("Queue '"+qEntry.getValue().getName()+"' has "+Integer.toString(qEntry.getValue().jobs.size())+" jobs");
		}
		Logger.logInfo("Total jobs found: "+jobs);
	}
}
