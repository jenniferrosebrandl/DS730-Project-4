package p4_indexing;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Main {
	public static final String IN_FILE_EXTENSION = ".txt";
	public static final String OUT_FILE_EXTENSION = ".result.txt";

	// do all the work
	public static void run(File inFolder, File outFolder, int numCharPage) {
		
		File[] files = inFolder.listFiles((file)->{
			return file.exists() && file.isFile() && checkExtention(file, "txt");
		});
		
		// first make sure the output folder exists and is a directory
		if(!outFolder.exists()) {
			outFolder.mkdir();
		}
		if(!outFolder.exists() || !outFolder.isDirectory()) {
			String msg = "Could not create out directory";
			System.err.println(msg);
			throw new RuntimeException(msg);
		}
		
		// clean the output folder
		Arrays.stream(outFolder.listFiles()).forEach(file->file.delete());
		
		long startTime = System.nanoTime();
		
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
		
		long finishTime = System.nanoTime();
		System.out.printf("Total Running time is %.4f (ms)\n", (finishTime-startTime)/1.0e6);
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
			
			run(inFolder, outFolder, numCharPage);
		} catch (Exception e) {
			System.err.println("Usage!!");
		}
	}
}