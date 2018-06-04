package com.chris.lucene_action.query;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * author:徐晨坤
 * 短语查询测试用例，在建立索引时，会默认设置各个项的位置信息，可以通过PhraseQuery查询几个短语间距在特定范围（如两个短语之间相聚不超过5）的文档
 */
public class PhraseQueryTest extends TestCase{
	private Directory dir;
	private IndexSearcher searcher;

	/**
	 * 索引初始化过程，在执行其它测试方法之前会调用这个方法
	 * @throws Exception
	 */
	protected void setUp() throws  Exception{
		dir = new RAMDirectory();
		IndexWriter writer = new IndexWriter(dir, new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);

		//生成索引
		Document doc = new Document();
		doc.add(new Field("field","the quick brown fox jumped over the lazy dog",Field.Store.YES,Field.Index.ANALYZED));
		writer.addDocument(doc);

		writer.close();

		searcher = new IndexSearcher(dir);
	}

	/**
	 * 关闭索引流操作，在测试方法执行完毕会调用该方法
	 * @throws IOException
	 */
	protected void tearDown() throws IOException {
		searcher.close();
		dir.close();
	}

	/**
	 *
	 * @param phrase,用于检索的短语数组
	 * @param slop,表示短语之间相距最大间隔数
	 * @return 是否匹配成功
	 */
	private boolean matched(String[] phrase,int slop) throws IOException {
		PhraseQuery query = new PhraseQuery();
		query.setSlop(slop);
		for (String word:phrase){
			query.add(new Term("field",word));
		}

		TopDocs docs = searcher.search(query,10);
		return docs.totalHits > 0;
	}

	/**
	 * 测试用例：测试slot的使用,其中评分机制是距离越近评分越高
	 * @throws IOException
	 */
	public void testSlopComparison() throws IOException{
		String[] phrase = new String[] {"quick","fox"};
		assertFalse("exact phrase not found",matched(phrase,0));//完全匹配即quick后面紧跟fox
		assertTrue("close enough",matched(phrase,1));//最大移动一个位置就可以匹配
	}

	/**
	 * 测试phrase反序时，quick要移动到fox前面至少要移动3次
	 * @throws IOException
	 */
	public void testReverse() throws IOException{
		String[] phrase = new String[] {"fox","quick"};
		assertFalse(matched(phrase,2));
		assertTrue(matched(phrase,3));
	}

	/**
	 * 复合项短语，slot规定要按照短语数组中设置的顺序排列时最多移动多少次
	 * @throws IOException
	 */
	public void testMutiple() throws IOException{
		assertFalse(matched(new String[] {"quick","jumped","lazy"},3));
		assertTrue(matched(new String[] {"quick","jumped","lazy"},4));
		assertFalse(matched(new String[] {"lazy","jumped","quick"},7));
		assertTrue(matched(new String[] {"lazy","jumped","quick"},8));
	}

}
