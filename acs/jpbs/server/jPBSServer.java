package acs.jpbs.server;

import java.util.List;

import acs.jpbs.core.PbsServer;
import acs.jpbs.utils.jPBSLogger;

public class jPBSServer {
	private static jPBSServer instance = null;
	public PbsServer server = null;

	private jPBSServer() {
		if(jPBSServerEnvironment.initEnv()) {
			jPBSLogger.logInfo("Environment loaded, 'qstat' utility found at '"+jPBSServerEnvironment.qstat+"'");
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
		
		List<String> test = jPBSServerEnvironment.retrieveQstatOutput(new String[]{"-Bf"});
		for(String oLine : test) {
			jPBSLogger.logInfo(oLine);
		}
	}
}
