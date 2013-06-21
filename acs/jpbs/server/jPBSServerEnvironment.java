package acs.jpbs.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import acs.jpbs.serverUtils.StdOutHandler;
import acs.jpbs.utils.jPBSLogger;

public class jPBSServerEnvironment {
	public static File qstat;
	
	private jPBSServerEnvironment() { }
	
	public static boolean initEnv() {
		String sysPathString = System.getenv("PATH");
		List<String> sysPaths = Arrays.asList(sysPathString.split(";"));
		
		File qstatCheck;
		for(String pathString : sysPaths) {
			qstatCheck = new File(pathString, "qstat.exe");
			if(qstatCheck.canExecute()) {
				qstat = qstatCheck;
				return true;
			}
		}
		qstat = null;
		return false;
	}
	
	public static List<String> retrieveQstatOutput(String args[]) {
		List<String> outputLines = new ArrayList<String>();
		List<String> cmd = new ArrayList<String>();
		cmd.add("cmd");
		cmd.add("/c");
		cmd.add(qstat.getPath());
		cmd.addAll(Arrays.asList(args));
		
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			StdOutHandler result = new StdOutHandler(p, outputLines);
			result.start();
			p.waitFor();
		} catch(Exception e) {
			jPBSLogger.logException("Exception occurred", e);
		}
		
		List<Integer> toRemove = new ArrayList<Integer>();
		for(int i=outputLines.size()-1; i>=0; i--) {
			if(outputLines.get(i).startsWith("\t") && i>0) {
				outputLines.set(i-1, outputLines.get(i-1)+outputLines.get(i).trim());
				toRemove.add(i);
			} else {
				outputLines.set(i, outputLines.get(i).trim());
			}
		}
		
		for(Integer badLine : toRemove) outputLines.remove(badLine.intValue());
				
		return outputLines;
	}
}
