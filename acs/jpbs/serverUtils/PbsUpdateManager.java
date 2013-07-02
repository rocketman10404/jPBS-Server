package acs.jpbs.serverUtils;

import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import acs.jpbs.core.PbsQueue;
import acs.jpbs.server.jPBSServer;
import acs.jpbs.server.core.PbsQueueHandler;
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
		PbsServerHandler sHandler = jPBSServer.pbsServer;
		PbsQueueHandler qHandler;
		int jobs = 0;
		
		Logger.logInfo("Update completed.");
		Logger.logInfo("Server name: '"+sHandler.getHostName()+"'");
		
		sHandler.queueMapReadLock.lock();
		try {
			Logger.logInfo("Queues found: "+sHandler.queues.size());
			for(Entry<String, PbsQueue> qEntry : sHandler.queues.entrySet()) {
				qHandler = (PbsQueueHandler)qEntry.getValue();
				qHandler.jobMapReadLock.lock();
				try {
					jobs += qHandler.jobs.size();
					Logger.logInfo("Queue '"+qHandler.getName()+"' has "+Integer.toString(qHandler.jobs.size())+" jobs");
				} finally {
					qHandler.jobMapReadLock.unlock();
				}
			}
		} finally {
			sHandler.queueMapReadLock.unlock();
		}
		Logger.logInfo("Total jobs found: "+jobs);
	}
}
