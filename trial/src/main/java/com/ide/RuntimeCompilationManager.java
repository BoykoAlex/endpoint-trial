package com.ide;

import org.springframework.stereotype.Component;

@Component
public class RuntimeCompilationManager {

	SourceWatcher sourceWatcher;

	public RuntimeCompilationManager() {
		String sourceroot = System.getProperty("sourceroot");
		if (sourceroot == null || sourceroot.length() == 0) {
			sourceroot = ".";
		}
		String targetroot = System.getProperty("targetroot");
		sourceWatcher = new SourceWatcher(sourceroot);
		if (targetroot!=null && targetroot.length()!=0) {
			sourceWatcher.setTargetRoot(targetroot);
		}
		sourceWatcher.start();
	}

	public void stop() {
		System.out.println("Shutting down the compilation monitor/manager");
		sourceWatcher.stop();
	}

	// public static void main(String[] args) throws InterruptedException {
	// // args[0] - folder to watch for changes
	// SourceWatcher sourceWatcher = new SourceWatcher(args[0]);
	// sourceWatcher.start();
	// Thread.sleep(10000);
	// sourceWatcher.stop();
	// System.out.println("Finished");
	// }
}
