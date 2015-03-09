package jp.dip.oyasirazu.domelementsorter;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Main {

    private Main() {}

    public static void main(String[] args) throws SAXException,
                IOException, ParserConfigurationException, TransformerException {

        Document document = DOMElementSorter.Util.createDocument(args[0]);
        sortChildNode(document);
        String documentString = DOMElementSorter.Util.documentToString(document);

        System.out.println(documentString);
    }

    private static void sortChildNode(Document document) {

        DOMElementSorter.sort(document);
    }
}
