package com.chris.lucene_action;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import junit.framework.TestCase;

public class PhraseQueryTest extends TestCase{
	private Directory dir;
	private IndexSearcher searcher;
	
	protected void setUp() throws  Exception{
		dir = new RAMDirectory();
		IndexWriter writer = new IndexWriter(dir, new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
		
		Document doc = new Document();
//		doc.add(field);
	}

}
