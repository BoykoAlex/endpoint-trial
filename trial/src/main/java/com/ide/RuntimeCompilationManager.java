package com.ide;

import org.springframework.stereotype.Component;

@Component
public class RuntimeCompilationManager {

	SourceWatcher sourceWatcher;

	public RuntimeCompilationManager() {
		String path = System.getProperty("sourcepath");
		if (path == null || path.length() == 0) {
			path = ".";
		}
		String classespath = System.getProperty("classespath");
		System.out.println("Watch root: " + path);
		sourceWatcher = new SourceWatcher(path);
		if (classespath!=null && classespath.length()!=0) {
			sourceWatcher.setClassesPath(classespath);
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
