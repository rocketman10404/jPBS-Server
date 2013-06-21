package acs.jpbs.serverUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import acs.jpbs.utils.jPBSLogger;
import acs.jpbs.utils.jPBSUtils;

public class jPBSEnvironment {
	public static File qstat;
	public static File qmgr;
	
	private jPBSEnvironment() { }
	
	public static boolean initEnv() {
		String sysPathString = System.getenv("PATH");
		List<String> sysPaths = Arrays.asList(sysPathString.split(";"));
		
		boolean qstatFound = false;
		boolean qmgrFound = false;
		File qstatCheck;
		File qmgrCheck;
		for(String pathString : sysPaths) {
			qstatCheck = new File(pathString, "qstat.exe");
			if(qstatCheck.canExecute()) {
				qstat = qstatCheck;
				qstatFound = true;
			}
			if(qstatFound) {
				qmgrCheck = new File(pathString, "qmgr.exe");
				if(qmgrCheck.canExecute()) {
					qmgr = qmgrCheck;
					qmgrFound = true;
				}
			}
			if(qstatFound && qmgrFound) return true;
		}
		qstat = null;
		qmgr = null;
		return false;
	}
	
	private static List<String> retrieveCmdOutput(String args[]) {
		List<String> outputLines = new ArrayList<String>();
		List<String> cmd = new ArrayList<String>();
		cmd.add("cmd");
		cmd.add("/c");
		cmd.addAll(Arrays.asList(args));
		
		for(String cmds : cmd) System.out.println(cmds + " ; ");
		
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
	
	public static List<String> retrieveQmgrOutput(String args[]) {
		return retrieveCmdOutput(jPBSUtils.concat(new String[]{qmgr.getPath(), "-c"}, args));
	}
	
	public static List<String> retrieveQstatOutput(String args[]) {
		return retrieveCmdOutput(jPBSUtils.concat(new String[]{qstat.getPath()}, args));
	}
}
