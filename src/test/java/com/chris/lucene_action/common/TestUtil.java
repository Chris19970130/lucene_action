package com.chris.lucene_action.common;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class TestUtil {
	
		public static boolean hitsIncludeTitle(IndexSearcher searcher, TopDocs hits, String title) 
				throws IOException {
			for(ScoreDoc match:hits.scoreDocs) {
				if(title.equals(searcher.doc(match.doc).get("title"))) {
					return true;
				}
			}
			System.out.println("title "+title+" is not found");
			return false;
		}

		  public static int hitCount(IndexSearcher searcher, Query query) throws IOException {
		    return searcher.search(query, 1).totalHits;
		  }

		  public static int hitCount(IndexSearcher searcher, Query query, Filter filter) throws IOException {
		    return searcher.search(query, filter, 1).totalHits;
		  }

		  public static void dumpHits(IndexSearcher searcher, TopDocs hits)
		    throws IOException {
		    if (hits.totalHits == 0) {
		      System.out.println("No hits");
		    }

		    for (ScoreDoc match : hits.scoreDocs) {
		      Document doc = searcher.doc(match.doc);
		      System.out.println(match.score + ":" + doc.get("title"));
		    }
		  }
		  
		  public static Directory getBookIndexDirectory() throws IOException {
		    // The build.xml ant script sets this property for us:
		    return FSDirectory.open(new File("D:\\MyDrivers\\project\\ecplise\\lucene_action\\index"));
		  }

		  public static void rmDir(File dir) throws IOException {
		    if (dir.exists()) {
		      File[] files = dir.listFiles();
		      for (int i = 0; i < files.length; i++) {
		        if (!files[i].delete()) {
		          throw new IOException("could not delete " + files[i]);
		        }
		      }
		      dir.delete();
		    }
		  }

}
