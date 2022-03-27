package p4_indexing;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexWriter {
	
	private File outputFile;
	private Map<String, Set<Integer>> index;

	public IndexWriter(File outputFile, Map<String, Set<Integer>> index) {
		this.outputFile = outputFile;
		this.index = index;
	}

	// do all the work
	public void run() {
		try(
				PrintWriter out = new PrintWriter(outputFile)
		) {
			// write index to a file
			List<String> wordList = new ArrayList<>(index.keySet());
			Collections.sort(wordList);
			for(String word: wordList) {
				StringBuilder indexLineBuilder = new StringBuilder();
				indexLineBuilder.append(word + " ");
				List<Integer> pageNumbers = new ArrayList<>(index.get(word));
				Collections.sort(pageNumbers);
				for(int pageNumber: pageNumbers) {
					indexLineBuilder.append(pageNumber + ",");
				}
				String indexLine = indexLineBuilder.toString();
				if(indexLine.endsWith(",")) indexLine = indexLine.substring(0, indexLine.length() -1);
				out.println(indexLine);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}