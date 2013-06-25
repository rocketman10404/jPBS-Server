package acs.jpbs.server;

import acs.jpbs.server.core.PbsServer;
import acs.jpbs.serverUtils.jPBSEnvironment;
import acs.jpbs.serverUtils.jPBSUpdateManager;
import acs.jpbs.serverUtils.jPBSUpdater;
import acs.jpbs.utils.jPBSLogger;

public class jPBSServer {
	private static jPBSServer instance = null;
	public PbsServer server = null;

	private jPBSServer() {
		if(jPBSEnvironment.initEnv()) {
			jPBSLogger.logInfo("Environment loaded, 'qstat' utility found at '"+jPBSEnvironment.qstat+"'");
			jPBSLogger.logInfo("'qmgr' utility found at '"+jPBSEnvironment.qmgr+"'");
		} else jPBSLogger.logError("Failed to load environment info");
	}
	
	public static jPBSServer getInstance() {
		if(instance == null) {
			instance = new jPBSServer();
		}
		return instance;
	}
	

	
	public static void main(String args[]) {
		jPBSServer me = getInstance();
		me.server = new PbsServer();
		
		// Update server in the background		
		jPBSUpdateManager.beginUpdate(me.server);
	}
}
