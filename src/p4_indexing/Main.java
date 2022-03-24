package p4_indexing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Main {
	public static final String IN_FILE_EXTENSION = ".txt";

	private File inFolder;
	private File outFolder;
	private int numCharPage;

	public Main(File inFolder, File outFolder, int numCharPage) {
		this.inFolder = inFolder;
		this.outFolder = outFolder;
		this.numCharPage = numCharPage;
	}

	// do all the work
	public void run() {
		File inDir = this.inFolder;
		
		File[] files = inDir.listFiles((file)->{
			return file.exists() && file.isFile() && checkExtention(file, "txt");
		});
		
		// first make sure the output folder exists and is a directory
		if(!this.outFolder.exists()) {
			this.outFolder.mkdir();
		}
		if(!this.outFolder.exists() || !this.outFolder.isDirectory()) {
			String msg = "Could not create out directory";
			System.err.println(msg);
			throw new RuntimeException(msg);
		}
		
		// clean the output folder
		Arrays.stream(this.outFolder.listFiles()).forEach(file->file.delete());
		
		long startTime = System.nanoTime();
		for(File file: files) {
			processFile(file);
		}
		long finishTime = System.nanoTime();
		System.out.printf("Total Running time is %.4f (ms)\n", (finishTime-startTime)/1.0e6);
	}

	private boolean checkExtention(File file, String string) {
		return file.getName().endsWith(IN_FILE_EXTENSION);
	}

	private String outputFileName(File inputFile) {
		String path =  inputFile.getName();
		return path.substring(0, path.length() - IN_FILE_EXTENSION.length()) + ".results";
	}
	
	private Map<String, List<Integer>> buildIndex(Scanner in) {
		Map<String, Set<Integer>> index = new HashMap<>();
		
		int pageNumber = 1;
		int numCharOnPage = 0;
		
		while(in.hasNext()) {
			String word = in.next().toLowerCase().trim();
			int wordLen = word.length();
			
			if(wordLen + numCharOnPage > this.numCharPage) {
				pageNumber++;
				numCharOnPage = 0;
			}
			
			if(!index.containsKey(word)) {
				index.put(word, new TreeSet<>());
			}
			
			index.get(word).add(pageNumber);
			numCharOnPage += wordLen;
		}
		
		Map<String, List<Integer>> indexAsList = new HashMap<>();
		for(String word: index.keySet()) {
			ArrayList<Integer> pageList = new ArrayList<>(index.get(word));
			indexAsList.put(word, pageList);
		}
		return indexAsList;
	}
	
	private void processFile(File inFile) {
		System.out.println("processing " + inFile.getName());
		
		try(
				Scanner in = new Scanner(inFile);
				PrintWriter out = new PrintWriter(
						new File(this.outFolder.getAbsolutePath(), outputFileName(inFile)))
		) {
			// build index
			Map<String, List<Integer>> index = buildIndex(in);

			// write index to a file
			List<String> wordList = new ArrayList<>(index.keySet());
			Collections.sort(wordList);
			for(String word: wordList) {
				StringBuilder indexLineBuilder = new StringBuilder();
				indexLineBuilder.append(word + " ");
				for(int pageNumber: index.get(word)) {
					indexLineBuilder.append(pageNumber + ",");
				}
				String indexLine = indexLineBuilder.toString();
				if(indexLine.endsWith(",")) indexLine = indexLine.substring(0, indexLine.length() -1);
				out.println(indexLine);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			File inFolder = new File(args[0]);
			File outFolder = new File(args[1]);
			int numCharPage = Integer.parseInt(args[2]);
			
			if(!inFolder.isDirectory()) {
				throw new Exception();
			}
			
			(new Main(inFolder, outFolder, numCharPage)).run();
		} catch (Exception e) {
			System.err.println("Usage!!");
		}
	}
}