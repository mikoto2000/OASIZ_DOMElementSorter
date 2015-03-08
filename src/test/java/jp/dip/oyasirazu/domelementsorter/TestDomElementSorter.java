package jp.dip.oyasirazu.domelementsorter;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * TestDomElementSorter
 */
public class TestDomElementSorter {
    private static final String EMPTY_XML_PATH =
        "src/test/resource/EmptyXml.xml";
    private static final String EMPTY_XML_OUTPUT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<xml/>";
    private static final String TAG_NAME_PATH =
        "src/test/resource/TagName.xml";
    private static final String TAG_NAME_OUTPUT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<xml>\n<a>gast.</a>\n<b>test.</b>\n<c>masg.</c>\n</xml>";

    @Test
    public void testSortEmptyXml() throws SAXException, ParserConfigurationException, TransformerException, IOException {
        Document document = DOMElementSorter.Util.createDocument(EMPTY_XML_PATH);
        DOMElementSorter.sort(document);
        String result = DOMElementSorter.Util.documentToString(document);

        assertThat(result, is(EMPTY_XML_OUTPUT));
    }

    @Test
    public void testSortTagName() throws SAXException, ParserConfigurationException, TransformerException, IOException {
        Document document = DOMElementSorter.Util.createDocument(TAG_NAME_PATH);
        DOMElementSorter.sort(document);
        String result = DOMElementSorter.Util.documentToString(document);

        assertThat(result, is(TAG_NAME_OUTPUT));
    }
}
