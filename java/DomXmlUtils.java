package com.uv.wm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 2017/11/17 16:50
 * 使用Dom方式来处理xml文件,依赖xml-apis.jar
 *
 * @author owen pan
 */
public class DomXmlUtil {
    private static Logger logger = LoggerFactory.getLogger(DomXmlUtil.class);
    private DocumentBuilderFactory dbf;
    private DocumentBuilder db;

    public static DomXmlUtil newInstance() {
        DomXmlUtil domXmlUtil=new DomXmlUtil();
        domXmlUtil.dbf = DocumentBuilderFactory.newInstance();
        // 如果创建的解析器在解析XML文档时必须删除元素内容中的空格，则为true，否则为false
        domXmlUtil.dbf.setIgnoringElementContentWhitespace(true);
        try {
            // 创建解析器，解析XML文档
            domXmlUtil.db = domXmlUtil.dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
        return domXmlUtil;
    }

    public <T> T parseXml(File file,BaseDomXmlParser<T> domXmlParser) {
        try {
            Document doc = db.parse(file);
            return domXmlParser.parse(doc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public <T> T parseXml(String uri,BaseDomXmlParser<T> domXmlParser) {
        try {
            Document doc = db.parse(uri);
            return domXmlParser.parse(doc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static class DomXmlFinder{
        private Logger logger = LoggerFactory.getLogger(DomXmlFinder.class);
        /**
         * 选择Node ，exp：/father/son[@id='001']
         * @param express 表达式
         * @param doc document对象
         * @return Node
         */
        private Object selectNode(String express, Document doc, QName type){
            Object result = null;
            //创建XPath工厂
            XPathFactory xpathFactory = XPathFactory.newInstance();
            //创建XPath对象
            XPath xpath = xpathFactory.newXPath();
            try {
                result =  xpath.evaluate(express, doc.getDocumentElement(), type);
            } catch (XPathExpressionException e) {
                logger.error(e.getMessage(), e);
            }
            return result;
        }
        public Node selectSingleNode(String express, Document doc) {
            return (Node) selectNode(express,doc, XPathConstants.NODE);
        }

        public NodeList selectNodeList(String express, Document doc) {
            return (NodeList) selectNode(express,doc, XPathConstants.NODESET);
        }
    }

    public  static abstract class BaseDomXmlParser<T> extends DomXmlFinder {
        protected abstract T parse(Document doc);
    }



    // 修改
    public BaseTransformerFactoryDomSaver modifyXml(File file,BaseDomXmlUpdater domXmlUpdater) {
        try {
            Document xmldoc = db.parse(file);
            domXmlUpdater.update(xmldoc);
            return new BaseTransformerFactoryDomSaver(xmldoc,file);
        } catch (Exception e) {
           logger.error(e.getMessage(),e);
           return null;
        }
    }



    abstract class BaseDomXmlUpdater extends DomXmlFinder{
        protected abstract void update(Document doc);
    }

    class BaseTransformerFactoryDomSaver {
        private Document document;
        private File file;

        public BaseTransformerFactoryDomSaver(Document document, File file) {
            this.document = document;
            this.file = file;
        }

        public void save() {
            // 保存
            try {
                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer former = factory.newTransformer();
                former.setParameter("indent-number", 2);
                Properties prop=new Properties();
                prop.setProperty( OutputKeys.METHOD, "xml" );
                prop.setProperty(OutputKeys.INDENT,"yes");
                prop.setProperty("{http://xml.apache.org/xalan}indent-amount", "4");

                former.setOutputProperties(prop);
                former.transform(new DOMSource(document), new StreamResult(file));
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
    }

    public BaseTransformerFactoryDomSaver deleteNodeFromXml(File file,BaseDomXmlDeleter baseDomXmlDeleter) {
        try {
            Document xmldoc = db.parse(file);
            baseDomXmlDeleter.delete(xmldoc);
            return new BaseTransformerFactoryDomSaver(xmldoc,file);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    abstract class BaseDomXmlDeleter extends DomXmlFinder{
        protected abstract void delete(Document doc);
    }

    public BaseTransformerFactoryDomSaver createXml(File file, BaseDomXmlUpdater baseDomXmlUpdater) {
        try {
            // 创建Document对象
            Document xmldoc = db.parse(file);
            baseDomXmlUpdater.update(xmldoc);
            if(!file.createNewFile()){
                throw new RuntimeException("create Xml file fail,Xml file has exists");
            }
            return new BaseTransformerFactoryDomSaver(xmldoc,file);
        } catch (Exception e) {
           logger.error(e.getMessage(),e);
            return null;
        }
    }

    public static void main(String[] args) {
        Object re= DomXmlUtil.newInstance().parseXml(new File( "/broadcast.xml"),new BaseDomXmlParser<List<String>>() {
            @Override
            protected List<String> parse(Document doc) {
                List<String> list=new ArrayList<>();
                Element root = doc.getDocumentElement();
                NodeList items = root.getElementsByTagName("broadcast-item");
                for (int i = 0; i < items.getLength(); i++) {
                    Element item = (Element) items.item(i);
                    if (item.hasAttributes()) {
                        list.add(Boolean.parseBoolean(item.getAttribute("enable"))+ item.getAttribute("url"));
                    }
                }
                return null;
            }
        });
        System.out.println(re);
    }

    
}

