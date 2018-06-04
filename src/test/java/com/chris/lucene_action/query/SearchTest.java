package com.chris.lucene_action.query;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.chris.lucene_action.common.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;

public class SearchTest extends TestCase{
	  public void testTerm() throws Exception {
		    Directory dir = TestUtil.getBookIndexDirectory(); //A
		    IndexSearcher searcher = new IndexSearcher(dir);  //B

		    Term t = new Term("subject", "ant");
		    Query query = new TermQuery(t);
		    TopDocs docs = searcher.search(query, 10);
		    assertEquals("Ant in Action",                //C
		                 1, docs.totalHits);                         //C

		    t = new Term("subject", "junit");
		    docs = searcher.search(new TermQuery(t), 10);
		    assertEquals("Ant in Action, " +                                 //D
		                 "JUnit in Action, Second Edition",                  //D
		                 2, docs.totalHits);                                 //D

		    searcher.close();
		    dir.close();
		  }
	  
	  public void testQueryParser() throws Exception{
		  Directory dir = TestUtil.getBookIndexDirectory();
		  IndexSearcher searcher = new IndexSearcher(dir);
		  QueryParser parser = new QueryParser(Version.LUCENE_30, "contents", new SimpleAnalyzer());
		  
		  Query query = parser.parse("+JUNIT +ANT -MOCK");
		  TopDocs docs = searcher.search(query, 10);
		  
		  assertEquals(1, docs.totalHits);
		  Document d = searcher.doc(docs.scoreDocs[0].doc);
		  assertEquals("Ant in Action", d.get("title"));
		  
		  searcher.close();
		  dir.close();
		  
	  }
	  
	  public void NearRealTimeTest() throws Exception{
		  Directory dir = new RAMDirectory();
		  IndexWriter writer = new IndexWriter(dir, new SimpleAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
		  for(int i=0;i<10;i++) {
			  Document doc = new Document();
			  Field field1 = new Field("id", ""+i, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
			  Field field2 = new Field("name","chris",Field.Store.NO,Field.Index.ANALYZED);
			  doc.add(field1);
			  doc.add(field2);
			  writer.addDocument(doc);
		  }
		  
		  IndexReader reader = writer.getReader();
		  IndexSearcher searcher = new IndexSearcher(reader);
		  
		  QueryParser parser = new QueryParser(Version.LUCENE_30, "name", new SimpleAnalyzer());
		  Query query = new TermQuery(new Term("id","0"));
		  TopDocs docs = searcher.search(query, 1);
		  assertEquals(1, docs.totalHits);
		  
		  writer.deleteDocuments(query);
		  Document doc = new Document();
		  doc.add(new Field("id","10",Field.Store.NO,Field.Index.NOT_ANALYZED_NO_NORMS));
		  doc.add(new Field("name","chenkun",Field.Store.NO,Field.Index.ANALYZED));
		  writer.addDocument(doc);
		  
		  IndexReader newReader = reader.reopen();
		  if(reader != newReader) {
			  reader.close();
			  reader = newReader;
			  searcher = new IndexSearcher(reader);
		  }
		  query = parser.parse("chris chenkun");
		  docs = searcher.search(query, 10);
		  assertEquals(10, docs.totalHits);
		  reader.close();
		  writer.close();
	  }
	  
	  public void testExplain() throws Exception{
		  Directory dir = TestUtil.getBookIndexDirectory();
		  IndexSearcher searcher = new IndexSearcher(dir);
		  QueryParser parser = new QueryParser(Version.LUCENE_30, "contents", new SimpleAnalyzer());
		  
		  Query query = parser.parse("junit");
		  
		  TopDocs docs = searcher.search(query, 10);
		  for(ScoreDoc doc:docs.scoreDocs) {
			  Explanation explanation = searcher.explain(query, doc.doc);
			  System.out.println("--------------------");
			  System.out.println(searcher.doc(doc.doc).get("title"));
			  System.out.println(explanation.toString());
		  }
		  
		  searcher.close();
		  dir.close();
	  }
	  
	  public void testTermRangeQuery() throws Exception{
		  Directory dir = TestUtil.getBookIndexDirectory();
		  IndexSearcher searcher = new IndexSearcher(dir);
		  TermRangeQuery query = new TermRangeQuery("title2", "d", "j", true, true);
		  
		  TopDocs docs = searcher.search(query, 100);
		  assertEquals(3, docs.totalHits);
		  
		  searcher.close();
		  dir.close();
	  }
	  
	  public void testNumericRangeQuery() throws Exception{
		  Directory dir = TestUtil.getBookIndexDirectory();
		  IndexSearcher searcher = new IndexSearcher(dir);
		  NumericRangeQuery query = NumericRangeQuery.newIntRange("pubmonth", 
  												200605, 200609, true, true);
		  
		  TopDocs docs = searcher.search(query, 100);
		  assertEquals(1, docs.totalHits);
		  
		  searcher.close();
		  dir.close();
	  }
	  
	  public void testPrefixQuery() throws Exception{
		  Directory dir = TestUtil.getBookIndexDirectory();
		  IndexSearcher searcher = new IndexSearcher(dir);
		  Term term = new Term("category", "/technology/computers/programming");
		  PrefixQuery query = new PrefixQuery(term);
		  TermQuery query2 = new TermQuery(term);
		  
		  TopDocs docs = searcher.search(query, 100);
		  TopDocs docs2 = searcher.search(query2, 10);

		  assertTrue(docs.totalHits > docs2.totalHits);
		  
		  searcher.close();
		  dir.close();
	  }
	  
	  public void testBooleanQuery() throws Exception{
		  TermQuery searchingBooks = new TermQuery(new Term("subject","search"));
		  Query books2010 = NumericRangeQuery.newIntRange("pubmonth", 201001, 201012, true, true);
		  
		  BooleanQuery booleanQuery = new BooleanQuery();
		  booleanQuery.add(searchingBooks, BooleanClause.Occur.MUST);
		  booleanQuery.add(books2010,BooleanClause.Occur.MUST);
		  
		  Directory dir = TestUtil.getBookIndexDirectory();
		  IndexSearcher searcher = new IndexSearcher(dir);
		  
		  TopDocs docs = searcher.search(booleanQuery, 10);
		  
		  assertEquals("Lucene in Action, Second Edition", searcher.doc(docs.scoreDocs[0].doc).get("title"));
		  
		  assertTrue(TestUtil.hitsIncludeTitle(searcher, docs, "Lucene in Action, Second Edition"));
		  dir.close();
		  searcher.close();
	  }


}
