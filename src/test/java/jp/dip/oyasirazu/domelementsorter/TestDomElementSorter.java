package jp.dip.oyasirazu.domelementsorter;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathException;

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
    private static final String SORT_CONDITION_PATH =
        "src/test/resource/SortCondition.xml";
    private static final String SORT_CONDITION_OUTPUT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<xml>\n<notarget>\n<b>test.</b>\n<c>masg.</c>\n<a>gast.</a>\n</notarget>\n<target>\n<a>gast.</a>\n<b>test.</b>\n<c>masg.</c>\n</target>\n</xml>";
    private static final String NODE_COMPARATOR_PATH =
        "src/test/resource/TagName.xml";
    private static final String NODE_COMPARATOR_OUTPUT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<xml>\n<c>masg.</c>\n<b>test.</b>\n<a>gast.</a>\n</xml>";
    private static final String EXCLUDE_CONDITION_PATH =
        "src/test/resource/ExcludeCondition.xml";
    private static final String EXCLUDE_CONDITION_OUTPUT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<xml>\n<target>\n<a>gast.</a>\n<b>test.</b>\n<c>masg.</c>\n</target>\n</xml>";
    private static final String EXCLUDE_USE_XPATH_PATH =
        "src/test/resource/ExcludeUseXPath.xml";
    private static final String EXCLUDE_USE_XPATH_OUTPUT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<xml>\n<target>\n<a>gast.</a>\n<b>test.</b>\n<c>masg.</c>\n</target>\n</xml>";
    private static final String NO_RECURSIVE_PATH =
        "src/test/resource/NoRecursive.xml";
    private static final String NO_RECURSIVE_OUTPUT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<root>\n<parenta>\n<childb>b</childb>\n<childc>c</childc>\n<childa>a</childa>\n</parenta>\n<parentb>\n<childb>b</childb>\n<childc>c</childc>\n<childa>a</childa>\n</parentb>\n</root>";

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

    @Test
    public void testSortUseXPath() throws XPathException, SAXException, ParserConfigurationException, TransformerException, IOException {
        Document document = DOMElementSorter.Util.createDocument(EXCLUDE_USE_XPATH_PATH);
        DOMElementSorter.sort(document, null, "//*/@id|/xml/notarget");
        String result = DOMElementSorter.Util.documentToString(document);

        assertThat(result, is(EXCLUDE_USE_XPATH_OUTPUT));
    }

    @Test
    public void testSortWithDetailConfigSortCondition() throws SAXException, ParserConfigurationException, TransformerException, IOException {
        Document document = DOMElementSorter.Util.createDocument(SORT_CONDITION_PATH);
        DOMElementSorter.sort(
                document,
                (node) -> {
                    // タグ名「notarget」下はソートしない
                    if (node.getNodeName().equals("notarget")) {
                        return false;
                    } else {
                        return true;
                    }
                },
                DOMElementSorter.NODE_COMPARATOR_DEFAULT,
                DOMElementSorter.EXCLUDE_TARGET_CONDITION_DEFAULT);
        String result = DOMElementSorter.Util.documentToString(document);

        assertThat(result, is(SORT_CONDITION_OUTPUT));
    }

    @Test
    public void testSortWithDetailConfigNodeComparator() throws SAXException, ParserConfigurationException, TransformerException, IOException {
        Document document = DOMElementSorter.Util.createDocument(NODE_COMPARATOR_PATH);
        DOMElementSorter.sort(
                document,
                DOMElementSorter.SORT_CONDITION_DEFAULT,
                (n1, n2) -> {
                    // タグ名の降順でソート
                    return n2.getNodeName().compareTo(n1.getNodeName());
                },
                DOMElementSorter.EXCLUDE_TARGET_CONDITION_DEFAULT);
        String result = DOMElementSorter.Util.documentToString(document);

        int x=1+1;

        assertThat(result, is(NODE_COMPARATOR_OUTPUT));
    }

    @Test
    public void testSortWithDetailConfigExcludeCondition() throws SAXException, ParserConfigurationException, TransformerException, IOException {
        Document document = DOMElementSorter.Util.createDocument(EXCLUDE_CONDITION_PATH);
        DOMElementSorter.sort(
                document,
                DOMElementSorter.SORT_CONDITION_DEFAULT,
                DOMElementSorter.NODE_COMPARATOR_DEFAULT,
                (node) -> {
                    // タグ名「notarget」下は出力しない
                    if (node.getNodeName().equals("notarget")) {
                        return true;
                    } else {
                        return false;
                    }
                });
        String result = DOMElementSorter.Util.documentToString(document);

        assertThat(result, is(EXCLUDE_CONDITION_OUTPUT));
    }

    @Test
    public void testSortWithNoRecursive() throws SAXException, ParserConfigurationException, TransformerException, IOException {
        Document document = DOMElementSorter.Util.createDocument(NO_RECURSIVE_PATH);

        // ルートエレメントを指定することで、
        // ルートエレメントの子要素のみソートして、
        // 孫以降をソートさせないようにする。
        DOMElementSorter.sort(
                document.getDocumentElement(),
                false,
                DOMElementSorter.SORT_CONDITION_DEFAULT,
                DOMElementSorter.NODE_COMPARATOR_DEFAULT,
                DOMElementSorter.EXCLUDE_TARGET_CONDITION_DEFAULT);
        String result = DOMElementSorter.Util.documentToString(document);

        assertThat(result, is(NO_RECURSIVE_OUTPUT));
    }
}
