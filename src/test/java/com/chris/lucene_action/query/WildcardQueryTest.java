package com.chris.lucene_action.query;

import junit.framework.TestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

/**
 * 通配符查询通用类WildcardQuery和FuzzyQuery测试类
 */
public class WildcardQueryTest extends TestCase {
    private Directory directory;

    @Override
    protected void setUp() throws Exception {
        directory = new RAMDirectory();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * 创建索引方法
     * @param fields
     * @throws IOException
     */
    private void indexSingleFieldDocs(Field[] fields) throws IOException {
        IndexWriter writer = new IndexWriter(directory,new WhitespaceAnalyzer(),IndexWriter.MaxFieldLength.UNLIMITED);
        for (Field field:fields){
            Document doc = new Document();
            doc.add(field);
            writer.addDocument(doc);
        }

        writer.optimize();
        writer.close();
    }

    /**
     * 测试WildcardQuery的测试用例
     * WildcardQuery使用通配符如：？ * 等进行查询
     * @throws IOException
     */
    public void testWildcardQuery() throws IOException{
        indexSingleFieldDocs(new Field[] {
                new Field("contents","wild",Field.Store.YES,Field.Index.ANALYZED),
                new Field("contents","child",Field.Store.YES,Field.Index.ANALYZED),
                new Field("contents","mild",Field.Store.YES,Field.Index.ANALYZED),
                new Field("contents","mildew",Field.Store.YES,Field.Index.ANALYZED)
        });

        IndexSearcher searcher = new IndexSearcher(directory);
        Query query = new WildcardQuery(new Term("contents","?ild*"));
        TopDocs docs = searcher.search(query,10);

        assertEquals(3,docs.totalHits);
        assertEquals("same score",docs.scoreDocs[0].score,docs.scoreDocs[1].score,0.0);
        assertEquals("same score",docs.scoreDocs[1].score,docs.scoreDocs[2].score,0.0);
        searcher.close();
    }

    /**
     * 测试FuzzyQuery的测试用例
     * FuzzyQuery采用了编辑距离算法：计算从一个字符串到另一个字符串所需的最小插入、删除和替换的字母个数
     * FuzzyQuery通过一个阈值来控制匹配，并不是单纯依据编辑距离，域值由编辑距离/字符串长度得到的一个因子，值越小，匹配评分越高
     * FuzzyQuery会尽可能枚举索引的每一个项，性能比较低
     * @throws IOException
     */
    public void testFuzzyQuery() throws IOException{
        indexSingleFieldDocs(new Field[] {
                new Field("contents","wuzzy",Field.Store.YES,Field.Index.ANALYZED),
                new Field("contents","fuzzy",Field.Store.YES,Field.Index.ANALYZED),
        });

        IndexSearcher searcher = new IndexSearcher(directory);
        Query query = new FuzzyQuery(new Term("contents","wuzza"));
        TopDocs docs = searcher.search(query,10);

        assertEquals(2,docs.totalHits);
        assertTrue(docs.scoreDocs[0].score != docs.scoreDocs[1].score);

        searcher.close();
    }
}
