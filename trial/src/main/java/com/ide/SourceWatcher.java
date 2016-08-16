package com.ide;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.compiler.runner.javac.CompilationMessage;
import org.springframework.compiler.runner.javac.CompilationResult;
import org.springframework.compiler.runner.javac.CompiledClassDefinition;
import org.springframework.compiler.runner.javac.RuntimeJavaCompiler;
import org.springframework.compiler.watcher.ChangedFile;
import org.springframework.compiler.watcher.ChangedFiles;
import org.springframework.compiler.watcher.FileChangeListener;
import org.springframework.compiler.watcher.FileSystemWatcher;

public class SourceWatcher {

	FileSystemWatcher watcher;
	File root;

	public SourceWatcher(String path) {
		watcher = new FileSystemWatcher(true, 1000, 400);
		watcher.addListener(new SourceChangeListener());
		this.root = new File(path);
		walk(watcher, this.root);
	}

	private static void walk(FileSystemWatcher watcher, File path) {
		File[] files = path.listFiles();
		boolean hasSources = false;
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					walk(watcher, file);
				} else if (file.getName().endsWith(".java")) {
					hasSources = true;
				}
			}
		}
		if (hasSources) {
			System.out.println("Watching " + path);
			watcher.addSourceFolder(path);
		}
	}

	public void start() {
		watcher.start();
	}

	public void stop() {
		watcher.stop();
	}

	class SourceChangeListener implements FileChangeListener {

		@Override
		public void onChange(Set<ChangedFiles> changeSet) {
			Set<File> toCompile = new HashSet<File>();
			for (ChangedFiles changedFiles : changeSet) {
				Set<ChangedFile> changedFilesInSet = changedFiles.getFiles();
				for (ChangedFile changedFile : changedFilesInSet) {
					// System.out.println("Change in "+changedFile.getFile());
					// Watching a directory we may sometimes see .class files
					// come in here...
					if (changedFile.getFile().getName().endsWith(".java")) {
						toCompile.add(changedFile.getFile());
					}
				}
			}
			System.out.println("To compile: " + toCompile);
			compile(toCompile);
		}

		public String loadFile(File f) {
			try {
				FileInputStream fis = new FileInputStream(f);
				DataInputStream dis = new DataInputStream(fis);
				StringBuilder content = new StringBuilder();
				String line = dis.readLine();
				while (line != null) {
					content.append(line).append(System.lineSeparator());
					line = dis.readLine();
				}
				dis.close();
				return content.toString();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private String toShortName(String name) {
			return name.substring(0, name.lastIndexOf("."));
		}

		Map<String, List<String>> targetLocations = new HashMap<String, List<String>>();

		private void compile(Set<File> toCompile) {
			for (File f : toCompile) {
				String sourceCode = loadFile(f);
				RuntimeJavaCompiler rjc = new RuntimeJavaCompiler();
				CompilationResult cr = rjc.compile(toShortName(f.getName()), sourceCode);
				System.out.println("Messages for compiling: " + f.getName());
				for (CompilationMessage message : cr.getCompilationMessages()) {
					System.out.println(message);
				}
				List<CompiledClassDefinition> cds = cr.getCompileClassDefinitions();
				for (CompiledClassDefinition cd : cds) {
					System.out.println("Compiling '" + f.getName() + "' produced '" + cd.getClassName() + "' bytes:#"
							+ cd.getBytes().length);
					writeClass(cd.getClassName(), cd.getBytes());
				}
			}
		}

		private void writeClass(String className, byte[] bytes) {
			List<String> dumpLocations = targetLocations.get(className);
			if (dumpLocations == null) {
				dumpLocations = discoverLocations(className);
			}
			for (String location : dumpLocations) {
				FileOutputStream fos;
				try {
					System.out.println("Writing class '" + className + "' out to: " + location);
					fos = new FileOutputStream(new File(location));
					fos.write(bytes, 0, bytes.length);
					fos.close();
				} catch (IOException e) {
					throw new RuntimeException("Failed to write out to " + location, e);
				}

			}
		}

		private List<String> discoverLocations(String classname) {
			List<String> collector = new ArrayList<String>();
			System.out.print("Scanning for '" + classname + "'.");
			find(root, classname, collector);
			System.out.println("found #" + collector.size() + " locations");
			targetLocations.put(classname, collector);
			return collector;
		}

		private void find(File path, String classname, List<String> collector) {
			System.out.print(".");
			if (path.isDirectory()) {
				File[] files = path.listFiles();
				for (File file : files) {
					find(file, classname, collector);
				}
			} else {
				// classname will be something like aaa.bbb.ccc.Ddd
				String pathName = path.getPath().replaceAll("/", ".");
				if (pathName.contains(classname + ".class")) {
					// System.out.println(">>>"+path);
					collector.add(path.toString());
				}
			}
		}

	}
}
