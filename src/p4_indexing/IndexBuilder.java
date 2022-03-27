package p4_indexing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class IndexBuilder {

	private File inFile;
	private Map<String, Set<Integer>> resultIndex;
	private int numCharPage;

	public IndexBuilder(File inFile, int numCharPage) {
		this.inFile = inFile;
		this.numCharPage = numCharPage;
		
		this.resultIndex = new HashMap<>();
	}

	// do all the work
	public void run() {
		
		int pageNumber = 1;
		int numCharOnPage = 0;
		try(Scanner fileScanner = new Scanner(inFile)) {
			while(fileScanner.hasNext()) {
				String word = fileScanner.next().toLowerCase().trim();
				int wordLen = word.length();
				
				if(wordLen + numCharOnPage > this.numCharPage) {
					pageNumber++;
					numCharOnPage = 0;
				}
				
				if(!resultIndex.containsKey(word)) {
					resultIndex.put(word, new TreeSet<>());
				}
				
				resultIndex.get(word).add(pageNumber);
				numCharOnPage += wordLen;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, Set<Integer>> getResultIndex() {
		return resultIndex;
	}
}