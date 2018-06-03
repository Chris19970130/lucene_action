package com.chris.lucene_action;

import java.io.IOException;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import com.chris.lucene_action.common.TestUtil;

import junit.framework.TestCase;

public class IndexingTest extends TestCase{
	protected String[] ids = {"1","2"};
	protected String[] unindexed = {"Netherlands","Italy"};
	protected String[] unstored = {"Amsterdam has 9999 bridge","venice has lots of canals"};
	protected String[] text = {"Amesterdam","venice"};
	
	private Directory directory;
	
	protected void setUp() throws Exception{
		directory = new RAMDirectory();
		IndexWriter writer = getWriter();
		
		for(int i = 0;i<ids.length;i++) {
			Document doc = new Document();
			doc.add(new Field("id", ids[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("country",unindexed[i],Field.Store.YES,Field.Index.NO));
			doc.add(new Field("contents",unstored[i],Field.Store.NO,Field.Index.ANALYZED));
			doc.add(new Field("city",text[i],Field.Store.YES,Field.Index.ANALYZED));
			writer.addDocument(doc);
		}
		
		writer.close();
	}

	private IndexWriter getWriter() throws IOException
	{
		return new IndexWriter(directory, new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	protected int getHitCount(String fieldName,String searchString) throws IOException{
		IndexSearcher search = new IndexSearcher(directory);
		Term t = new Term(fieldName,searchString);
		Query query = new TermQuery(t);
		int hitCount = TestUtil.hitCount(search, query);
		search.close();
		return hitCount;
	}
	
	public void testIndexWriter() throws IOException{
		IndexWriter writer = getWriter();
		assertEquals(ids.length, writer.numDocs());
		writer.close();
	}
	
	public void testIndexReader() throws IOException{
		IndexReader reader = IndexReader.open(directory);
		System.out.println(getHitCount("contents", "9999"));
		assertEquals(ids.length, reader.numDocs());
		assertEquals(ids.length, reader.maxDoc());
		reader.clone();
	}
	
	public void testDeleteBeforeOptimize() throws IOException{
		IndexWriter writer = getWriter();
		assertEquals(2, writer.numDocs());
		writer.deleteDocuments(new Term("id","1"));
		writer.commit();
		assertTrue(writer.hasDeletions());
		assertEquals(2, writer.maxDoc());
		assertEquals(1, writer.numDocs());
		writer.close();
	}
	
	public void testDeleteAfterOptimize() throws IOException{
		IndexWriter writer = getWriter();
		assertEquals(2, writer.numDocs());
		writer.deleteDocuments(new Term("id","1"));
		writer.optimize();
		writer.commit();
		assertFalse(writer.hasDeletions());
		assertEquals(1, writer.maxDoc());
		assertEquals(1, writer.numDocs());
	}
	
	public void testUpdate() throws IOException{
//		IndexWriter writer = getWriter();
		assertEquals(1, getHitCount("city", "Amesterdam"));
		IndexWriter writer = getWriter();
		Document doc = new Document();
		doc.add(new Field("id","1",Field.Store.YES,Field.Index.NOT_ANALYZED));
		doc.add(new Field("country","Netherlands",Field.Store.YES,Field.Index.NO));
		doc.add(new Field("contents","Den Haag has a lot of museums",Field.Store.NO,Field.Index.ANALYZED));
		doc.add(new Field("city","Den Haag",Field.Store.YES,Field.Index.NOT_ANALYZED));
		
		writer.updateDocument(new Term("id","1"), doc);
		writer.close();
		assertEquals(0, getHitCount("city", "Amsterdam"));
		assertEquals(1, getHitCount("city","Den Haag"));
	}
	
	public void testWriterLock() throws IOException {
		IndexWriter writer1 = new IndexWriter(directory,new SimpleAnalyzer(),IndexWriter.MaxFieldLength.UNLIMITED);
		IndexWriter writer2 = null;
		try {
			writer2 = new IndexWriter(directory,new SimpleAnalyzer(),IndexWriter.MaxFieldLength.UNLIMITED); 
			fail("we should not get this pooint");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		finally {
			writer1.close();
			assertNull(writer2);
		}
	}
}
