package acs.jpbs.server;

import acs.jpbs.core.PbsServer;
import acs.jpbs.server.core.PbsServerHandler;
import acs.jpbs.serverUtils.PbsEnvironment;
import acs.jpbs.serverUtils.PbsUpdateManager;
import acs.jpbs.utils.Logger;

public class jPBSServer {
	private static jPBSServer instance = null;
	public PbsServer server = null;

	private jPBSServer() {
		if(PbsEnvironment.initEnv()) {
			Logger.logInfo("Environment loaded, 'qstat' utility found at '"+PbsEnvironment.qstat+"'");
			Logger.logInfo("'qmgr' utility found at '"+PbsEnvironment.qmgr+"'");
		} else Logger.logError("Failed to load environment info");
	}
	
	public static jPBSServer getInstance() {
		if(instance == null) {
			instance = new jPBSServer();
		}
		return instance;
	}
	

	
	public static void main(String args[]) {
		jPBSServer me = getInstance();
		me.server = new PbsServerHandler();
		
		// Update server in the background		
		PbsUpdateManager.beginUpdate((PbsServerHandler)me.server);
	}
}
