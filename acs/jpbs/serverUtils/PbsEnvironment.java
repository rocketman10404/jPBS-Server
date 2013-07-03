package acs.jpbs.serverUtils;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import acs.jpbs.utils.Logger;
import acs.jpbs.utils.Utils;

public class PbsEnvironment {
	private static String os = null;
	public static File qstat;
	public static File qmgr;
	public static String localHost;
	static {
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			Logger.logException("Unable to retreive hostname", e);
			localHost = "localhost";
		}
	}
	
	private PbsEnvironment() { }
	
	private static String getOsName() {
		if(os == null) os = System.getProperty("os.name");
		return os;
	}
	
	public static boolean initEnv() {
		String sysPathString = System.getenv("PATH");
		List<String> sysPaths;
		String qstatName;
		String qmgrName;
		
		if(isWindows()) {
			qstatName = "qstat.exe";
			qmgrName = "qmgr.exe";
			sysPaths = Arrays.asList(sysPathString.split(";"));
		} else {
			qstatName = "qstat";
			qmgrName = "qmgr";
			sysPaths = Arrays.asList(sysPathString.split(":"));
		}
		
		boolean qstatFound = false;
		boolean qmgrFound = false;
		File qstatCheck;
		File qmgrCheck;
		for(String pathString : sysPaths) {
			qstatCheck = new File(pathString, qstatName);
			if(qstatCheck.canExecute()) {
				qstat = qstatCheck;
				qstatFound = true;
			}
			if(qstatFound) {
				qmgrCheck = new File(pathString, qmgrName);
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
	
	public static boolean isLinux() {
		return getOsName().startsWith("Linux");
	}
	
	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}
	
	private static List<String> retrieveCmdOutput(String args[], String longLineDelimiter) {
		List<String> outputLines = new ArrayList<String>();
		List<String> cmd = new ArrayList<String>();
		if(isWindows()) {
			cmd.add("cmd");
			cmd.add("/c");
		}
		cmd.addAll(Arrays.asList(args));
		
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			StdOutHandler result = new StdOutHandler(p, outputLines);
			result.start();
			p.waitFor();
		} catch(Exception e) {
			Logger.logException("Exception occurred", e);
		}
		
		List<Integer> toRemove = new ArrayList<Integer>();
		for(int i=outputLines.size()-1; i>=0; i--) {
			if(outputLines.get(i).startsWith(longLineDelimiter) && i>0) {
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
		if(isWindows()) {
			StringBuilder joinedCmd = new StringBuilder();
			joinedCmd.append("\"\"").append(qmgr.getPath()).append("\" -c \"").append(Utils.join(args, " ")).append("\"");
			return retrieveCmdOutput(new String[]{joinedCmd.toString()}, "\t\t");
		} else {
			return retrieveCmdOutput(new String[]{qmgr.getPath(), "-c", Utils.join(args, " ")}, "\t\t");
		}
		
	}
	
	public static List<String> retrieveQstatOutput(String args[]) {
		if(isWindows()) {
			StringBuilder joinedCmd = new StringBuilder();
			joinedCmd.append("\"\"").append(qstat.getPath()).append("\" ").append(Utils.join(args, " ")).append("\"");
			return retrieveCmdOutput(new String[]{joinedCmd.toString()}, "\t");
		} else {
			return retrieveCmdOutput(Utils.concat(new String[]{qstat.getPath()}, args), "\t");
		}
	}
}
