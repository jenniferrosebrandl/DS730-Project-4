package p4_indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
	public static final String IN_FILE_EXTENSION = ".txt";
	public static final String OUT_FILE_EXTENSION = ".result.txt";

	// do all the work in Series
	public static void runSerially(File inFolder, File outFolder, int numCharPage) throws IOException {
		
		File[] files = inFolder.listFiles((file)->{
			return file.exists() && file.isFile() && checkExtention(file, "txt");
		});
		
		// first make sure the output folder exists and is a directory
		if(!outFolder.exists()) {
			Files.createDirectories(outFolder.toPath());
		}
		if(!outFolder.exists() || !outFolder.isDirectory()) {
			String msg = "Could not create out directory";
			System.err.println(msg);
			throw new RuntimeException(msg);
		}
		
		// clean the output folder
		Arrays.stream(outFolder.listFiles()).forEach(file->file.delete());
		
		// first build and write my indexes
		for(File file: files) {
			// create a new indexBuilder
			IndexBuilder builder = new IndexBuilder(file, numCharPage);
			// run the index builder
			builder.run();
			Map<String, Set<Integer>> index = builder.getResultIndex();
			
			// save the index
			String inputFileName =  file.getName();
			String outputFileName =  
					inputFileName.substring(0, inputFileName.length() - IN_FILE_EXTENSION.length()) + 
					OUT_FILE_EXTENSION;
			File outputFile = new File(outFolder, outputFileName);
			(new IndexWriter(outputFile, index)).run();
		}
	}

	// do individual files in parallel
	public static void runFilesInParallel(File inFolder, File outFolder, int numCharPage) throws IOException {
		
		File[] files = inFolder.listFiles((file)->{
			return file.exists() && file.isFile() && checkExtention(file, "txt");
		});
		
		// first make sure the output folder exists and is a directory
		if(!outFolder.exists()) {
			Files.createDirectories(outFolder.toPath());
		}
		if(!outFolder.exists() || !outFolder.isDirectory()) {
			String msg = "Could not create out directory";
			System.err.println(msg);
			throw new RuntimeException(msg);
		}
		
		// clean the output folder
		Arrays.stream(outFolder.listFiles()).forEach(file->file.delete());
		
		// first build and write my indexes, in separate threads
		List<Thread> threadList = new ArrayList<>();
		for(File file: files) {
			Thread th = new Thread() {
				@Override
				public void run() {
					// create a new indexBuilder
					IndexBuilder builder = new IndexBuilder(file, numCharPage);
					// run the index builder
					builder.run();
					Map<String, Set<Integer>> index = builder.getResultIndex();
					
					// save the index
					String inputFileName =  file.getName();
					String outputFileName =  
							inputFileName.substring(0, inputFileName.length() - IN_FILE_EXTENSION.length()) + 
							OUT_FILE_EXTENSION;
					File outputFile = new File(outFolder, outputFileName);
					(new IndexWriter(outputFile, index)).run();
				}
				
			};
			
			threadList.add(th);
			th.start();
		}
		
		// wait for all threads to complete
		for(Thread th: threadList) {
			try {
				th.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static boolean checkExtention(File file, String string) {
		return file.getName().endsWith(IN_FILE_EXTENSION);
	}

	public static void main(String[] args) {
		try {
			File inFolder = new File(args[0]);
			File outFolder = new File(args[1]);
			int numCharPage = Integer.parseInt(args[2]);
			
			if(!inFolder.isDirectory()) {
				throw new Exception();
			}
			
			long serialStartTime = System.nanoTime();
			File serialOutFolder = new File(outFolder, "serial");
			runSerially(inFolder, serialOutFolder, numCharPage);
			long serialFinishTime = System.nanoTime();
			System.out.printf(
					"Total Running time (serial) is %.4f (ms)\n", 
					(serialFinishTime-serialStartTime)/1.0e6);
			
			long parallelStartTime = System.nanoTime();
			File filesInParallelOutFolder = new File(outFolder, "filesInParallel");
			runFilesInParallel(inFolder, filesInParallelOutFolder, numCharPage);
			long parallelFinishTime = System.nanoTime();
			System.out.printf(
					"Total Running time (files in parallel) is %.4f (ms)\n", 
					(parallelFinishTime-parallelStartTime)/1.0e6);
			
		} catch (Exception e) {
			System.err.println("Usage!!");
		}
	}
}