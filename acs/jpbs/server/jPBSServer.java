package acs.jpbs.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import acs.jpbs.core.IPbsObject;
import acs.jpbs.core.PbsJob;
import acs.jpbs.core.PbsJobHandler;
import acs.jpbs.core.PbsQueue;
import acs.jpbs.core.PbsQueueHandler;
import acs.jpbs.core.PbsServer;
import acs.jpbs.core.PbsServerHandler;
import acs.jpbs.net.jPBSClientInterface;
import acs.jpbs.net.jPBSServerInterface;
import acs.jpbs.serverUtils.PbsEnvironment;
import acs.jpbs.serverUtils.PbsUpdateManager;
import acs.jpbs.utils.Logger;

public class jPBSServer extends UnicastRemoteObject implements jPBSServerInterface {

	private static final long serialVersionUID = 7037513788004765212L;
	public static PbsServerHandler pbsServer = null;
	public final static String serviceName = "jPBS-Server";
	public static List<jPBSClientInterface> clients = new LinkedList<jPBSClientInterface>();
	private static transient final ReentrantReadWriteLock clientsLock = new ReentrantReadWriteLock(true);
	protected static transient final Lock clientsReadLock = clientsLock.readLock();
	protected static transient final Lock clientsWriteLock = clientsLock.writeLock();
	
	public jPBSServer() throws RemoteException {
		super();
		if(PbsEnvironment.initEnv()) {
			Logger.logInfo("Environment loaded, 'qstat' utility found at '"+PbsEnvironment.qstat+"'");
			Logger.logInfo("'qmgr' utility found at '"+PbsEnvironment.qmgr+"'");
		} else Logger.logError("Failed to load environment info");
		pbsServer = PbsServerHandler.getInstance();
		// Update server in the background
		PbsUpdateManager.beginUpdate((PbsServerHandler)pbsServer);
	}
	
	public void deregister(jPBSClientInterface client) throws RemoteException {
		clientsWriteLock.lock();
		try {
			if(clients.contains(client)) {
				clients.remove(client);
				Logger.logInfo("Client disconnected: "+clients.size()+" client(s) total.");
			} else {
				Logger.logError("Invalid client disconnection attempt.");
			}
		} finally {
			clientsWriteLock.unlock();
		}
	}
		
	public PbsServer getServerObject() throws RemoteException { return pbsServer.getServer(); }
	
	public PbsQueue[] getQueueArray() throws RemoteException {
		return pbsServer.getQueueArray();
	}
	
	public PbsJob[] getJobArray() throws RemoteException { 
		return pbsServer.getJobArray();
	}
	
	public void register(jPBSClientInterface newClient) throws RemoteException {
		clientsWriteLock.lock();
		try {
			clients.add(newClient);
			Logger.logInfo("New client connected: "+clients.size()+" client(s) total.");
		} finally {
			clientsWriteLock.unlock();
		}
	}
	
	public void updateClients(IPbsObject newObj) {
		clientsReadLock.lock();
		try {
			Iterator<jPBSClientInterface> itr = clients.iterator();
			while(itr.hasNext()) {
				jPBSClientInterface client = itr.next();
				try {
					if(newObj instanceof PbsJobHandler) client.updateJob((PbsJob)newObj);
					else if(newObj instanceof PbsQueueHandler) client.updateQueue((PbsQueue)newObj);
					else if(newObj instanceof PbsServerHandler) client.updateServer((PbsServer)newObj);
				} catch (Exception e) {
					Logger.logError("Unable to update client '"+client.toString()+"'");
				}
			}
		} finally {
			clientsReadLock.unlock();
		}
	}
	
	public static void main(String args[]) {		
		Logger.logInfo("Initializing jPBS Server service...");
		jPBSServerInterface service = null;
		try {
			service = new jPBSServer();
			Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			registry.bind(serviceName, service);
			Logger.logInfo("jPBS service running.");
		} catch (Exception e) {
			Logger.logException("Error initializing service", e);
		}	
	}
}
