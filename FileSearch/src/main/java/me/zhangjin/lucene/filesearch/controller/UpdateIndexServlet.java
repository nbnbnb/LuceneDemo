package me.zhangjin.lucene.filesearch.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.zhangjin.lucene.filesearch.model.FileModel;
import me.zhangjin.lucene.filesearch.service.IKAnalyzer6x;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class UpdateIndexServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            updateIndex(request, response);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private void updateIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // IK分词器对象
        Analyzer analyzer = new IKAnalyzer6x();
        IndexWriterConfig icw = new IndexWriterConfig(analyzer);
        icw.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        Directory dir = null;
        IndexWriter inWriter = null;

        Path indexPath = Paths.get(request.getServletContext().getRealPath("/indexdir"));

        FieldType fileType = new FieldType();
        fileType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        fileType.setStored(true);
        fileType.setTokenized(true);
        fileType.setStoreTermVectors(true);
        fileType.setStoreTermVectorPositions(true);
        fileType.setStoreTermVectorOffsets(true);

        Date start = new Date();// 开始时间
        if (!Files.isReadable(indexPath)) {
            System.out.println(indexPath.toAbsolutePath() + "不存在或者不可读，请检查!");
            response.getWriter().write(indexPath.toAbsolutePath() + "不存在或者不可读，请检查!");
            return;
        }
        dir = FSDirectory.open(indexPath);
        inWriter = new IndexWriter(dir, icw);
        ArrayList<FileModel> fileList = (ArrayList<FileModel>) extractFile(request, response);
        // 遍历fileList,建立索引
        for (FileModel f : fileList) {
            Document doc = new Document();
            doc.add(new Field("title", f.getTitle(), fileType));
            doc.add(new Field("content", f.getContent(), fileType));
            inWriter.addDocument(doc);
        }

        inWriter.commit();
        inWriter.close();
        dir.close();

        Date end = new Date();// 结束时间
        // 打印索引耗时
        System.out.println("索引文档完成,共耗时:" + (end.getTime() - start.getTime()) + "毫秒.");

        //response.getWriter().write("索引文档完成,共耗时:" + (end.getTime() - start.getTime()) + "毫秒.");

        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    /*
     * 功能:列出WebContent/files目录下的索所有文件 参数:无 返回值:FileModel类型的列表
     */
    public static List<FileModel> extractFile(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ArrayList<FileModel> list = new ArrayList<FileModel>();
        File fileDir = new File(request.getServletContext().getRealPath("/files"));
        if (!fileDir.exists()) {
            System.out.println("文件夹路径错误!");
            response.getWriter().write("文件夹路径错误");
        }
        File[] allFiles = fileDir.listFiles();

        for (File f : allFiles) {
            FileModel sf = new FileModel(f.getName(), parserFile(f));
            list.add(sf);
        }
        return list;
    }

    /*
     * 功能:使用Tika提取文件内容 参数：文件对象 返回值: String格式的文档内容
     */
    public static String parserFile(File file) throws Exception {
        String fileContent = "";// 接收文档内容
        BodyContentHandler handler = new BodyContentHandler();
        Parser parser = new AutoDetectParser();// 自动解析器接口
        Metadata metadata = new Metadata();
        FileInputStream inputStream;
        inputStream = new FileInputStream(file);
        ParseContext context = new ParseContext();
        parser.parse(inputStream, handler, metadata, context);
        fileContent = handler.toString();

        return fileContent;
    }
}
