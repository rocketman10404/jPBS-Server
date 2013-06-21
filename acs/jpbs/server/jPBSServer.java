package acs.jpbs.server;

import acs.jpbs.server.core.PbsServer;
import acs.jpbs.serverUtils.jPBSEnvironment;
import acs.jpbs.utils.jPBSLogger;

public class jPBSServer {
	private static jPBSServer instance = null;
	public PbsServer server = null;

	private jPBSServer() {
		if(jPBSEnvironment.initEnv()) {
			jPBSLogger.logInfo("Environment loaded, 'qstat' utility found at '"+jPBSEnvironment.qstat+"'");
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
		
		// Update server in the foreground
		me.server.updateSelf();
		me.server.debugPrint();
		
		// Update children in the background
	}
}
