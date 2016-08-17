package com.ide;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
	String targetroot;

	public SourceWatcher(String path) {
		watcher = new FileSystemWatcher(true, 1000, 400);
		watcher.addListener(new SourceChangeListener());
		try {
			String fullpath = new File(path).getCanonicalPath();
			System.out.println("sourcesroot = "+fullpath);
			this.root = new File(fullpath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		walk(watcher, this.root);
	}
	
	private static Set<String> supported_suffixes;
	
	static {
		supported_suffixes = new HashSet<String>();
		supported_suffixes.add(".java");
		supported_suffixes.add(".properties");
		supported_suffixes.add(".yml");
		supported_suffixes.add(".html");
		supported_suffixes.add(".js");
		supported_suffixes.add(".css");
	}

	private static String getSuffix(File file) {
		String s = file.toString();
		return s.substring(s.lastIndexOf("."));
	}
	
	private static void walk(FileSystemWatcher watcher, File path) {
		File[] files = path.listFiles();
		boolean hasSources = false;
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					walk(watcher, file);
				} else {
					if (supported_suffixes.contains(getSuffix(file))) {
						hasSources = true;
					}
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
			Set<File> toCopy = new HashSet<File>();
			for (ChangedFiles changedFiles : changeSet) {
				Set<ChangedFile> changedFilesInSet = changedFiles.getFiles();
				for (ChangedFile changedFile : changedFilesInSet) {
					// Watching dirs so may get some random stuff in here.
					// Some files we just want to copy, some we want to compile
					String suffix = getSuffix(changedFile.getFile());
					if (supported_suffixes.contains(suffix)) {
						if (changedFile.getFile().getName().endsWith(".java")) {
							toCompile.add(changedFile.getFile());
						} else {
							toCopy.add(changedFile.getFile());
						}
					}
				}
			}
			System.out.println("To compile: " + toCompile);
			System.out.println("To copy: " + toCopy);
			compile(toCompile);
			copy(toCopy);
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
		
		private void copy(Set<File> toCopy) {
			String sourcesPath = root.getAbsolutePath().toString();
			byte[] bs = new byte[5];
			int read;
			for (File f: toCopy) {
//				String contents = loadFile(f);
				String fromPath = f.getPath();
				if (fromPath.startsWith(sourcesPath)) {
					String toPath = targetroot+fromPath.substring(sourcesPath.length());
					System.out.println("Copying from "+sourcesPath+" to "+toPath);
					String dir = toPath.substring(0, toPath.lastIndexOf(File.separator));
					new File(dir).mkdirs();
					try {
						FileInputStream fis = new FileInputStream(f);
						FileOutputStream fos = new FileOutputStream(new File(toPath));
						while ((read=fis.read(bs))!=-1) {
							fos.write(bs, 0, read);
						}
						fis.close();
						fos.close();
					} catch (IOException e) {
						throw new RuntimeException("Failed to copy to " + toPath, e);
					}
				} else {
					System.out.println("Problem? "+fromPath+" not on sourcespath "+sourcesPath);
				}
				
			}
		}

		private void writeClass(String className, byte[] bytes) {
			String toPath = targetroot==null?root.getAbsolutePath():targetroot+File.separator+className.replaceAll("\\.", File.separator)+".class";
			System.out.println("Writing class "+className+" to "+toPath);
			// Ensure directories exist - might be a new file
			String dir = toPath.substring(0, toPath.lastIndexOf(File.separator));
			new File(dir).mkdirs();
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(new File(toPath));
				fos.write(bytes, 0, bytes.length);
				fos.close();
			} catch (IOException e) {
				throw new RuntimeException("Failed to write out to " + toPath, e);
			}
		}

//		private List<String> discoverLocations(String classname) {
//			List<String> collector = new ArrayList<String>();
//			System.out.print("Scanning for '" + classname + "'.");
//			find(classespath==null?root:new File(classespath), classname, collector);
//			System.out.println("found #" + collector.size() + " locations");
//			targetLocations.put(classname, collector);
//			return collector;
//		}

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

	public void setTargetRoot(String targetroot) {
		try {
			String fullpath = new File(targetroot).getCanonicalPath();
			this.targetroot = fullpath;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Targetroot = "+this.targetroot);
	}
}
