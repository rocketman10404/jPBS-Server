package acs.jpbs.serverUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import acs.jpbs.utils.Logger;

public class StdOutHandler extends Thread {
	Process proc = null;
	List<String> listOut = null;
	
	public StdOutHandler(Process p, List<String> l) {
		this.proc = p;
		this.listOut = l;
	}
	
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(this.proc.getInputStream()));
		try {
			String line = reader.readLine();
			while(line != null) {
				listOut.add(line);
				line = reader.readLine();
			}
		} catch (Exception e) {
			Logger.logException("An error occurred while processing input", e);
		}
	}
}
