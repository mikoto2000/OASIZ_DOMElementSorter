package jp.dip.oyasirazu.domelementsorter;

import java.io.IOException;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * DOMElementSorter
 */
public class DOMElementSorter {

    public static final NodeComparator NODE_COMPARATOR_DEFAULT =  new NodeComparator() {
        @Override
        public int compare(Node n1, Node n2) {
            // タグ名でソート
            return n1.getNodeName().compareTo(n2.getNodeName());
        }
    };

    public static final ExcludeTargetCondition EXCLUDE_TARGET_CONDITION_DEFAULT =  new ExcludeTargetCondition() {
        @Override
        public boolean isExcludeTarget(Node node) {
            // 除外するノードなし
            return false;
        }
    };
    public static final SortTargetCondition SORT_CONDITION_DEFAULT =  new SortTargetCondition() {

        @Override
        public boolean isSortTarget(Node node) {
            return true;
        }
    };

    private DOMElementSorter() {}

    public static void sort(Document document) {
        sort(document, true, SORT_CONDITION_DEFAULT, NODE_COMPARATOR_DEFAULT, EXCLUDE_TARGET_CONDITION_DEFAULT);
    }

    public static void sort(Node node, SortTargetCondition SortTargetCondition, Comparator<Node> comparator, ExcludeTargetCondition excludeTargetCondition) {
        sort(node, true, SortTargetCondition, comparator, excludeTargetCondition);
    }

    private static void sort(Node node, boolean isRecursion, SortTargetCondition SortTargetCondition, Comparator<Node> comparator, ExcludeTargetCondition excludeTargetCondition) {
        // 子ノード情報取得
        NodeList nodes = node.getChildNodes();
        int size = nodes.getLength();

        // 除外フラグが立っていない、かつ、
        // 再帰フラグが立って入れば、再帰する
        if (!excludeTargetCondition.isExcludeTarget(node) && isRecursion) {
            for (int i = 0; i < size; i++) {
                sort(nodes.item(i), isRecursion, SortTargetCondition, comparator, excludeTargetCondition);
            }
        }

        // ソートターゲットでなければ何もしない
        if (!SortTargetCondition.isSortTarget(node)) {
            return;
        }

        // NodeList から ArrayList に入れ替える
        ArrayList<Node> nodeList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            nodeList.add(nodes.item(i));
        }

        // ArrayList<Node> をソートし、append し直すことで子ノードのソートを行う
        Collections.sort(nodeList, comparator);
        for (Node n : nodeList) {
            node.removeChild(n);

            // 除外ノードでなければ追加し直す
            if (!excludeTargetCondition.isExcludeTarget(n)) {
                node.appendChild(n);
            }
        }
    }

    /**
     * ソート対象ノードを判定するためのインターフェース。
     *
     * isSortTarget(Node node) の戻り値が true であるノードの子要素をソートする。
     */
    public interface SortTargetCondition {
        boolean isSortTarget(Node node);
    }

    /**
     * 出力対象外ノードを判定するためのインターフェース。
     *
     * isExcludeTarget(Node node) の戻り値が true であるノードは出力しない。
     */
    public interface ExcludeTargetCondition {
        boolean isExcludeTarget(Node node);
    }

    /**
     * ノードのソートに利用する Comparator。
     */
    public interface NodeComparator extends Comparator<Node> {}

    public static class Util {

        private Util() {}

        public static Document createDocument(String filePath) throws SAXException,
                    IOException, ParserConfigurationException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(filePath);
        }

        public static String documentToString(Document document) throws TransformerException {
            StringWriter sw = new StringWriter();
            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer transformer = tfactory.newTransformer();

            transformer.transform(new DOMSource(document), new StreamResult(sw));
            return sw.toString().replaceAll("\\>\\<", ">\n<").replaceAll("\\n\\s+", "\n");
        }
    }
}
