package jp.dip.oyasirazu.domelementsorter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * DOMElementSorter は、
 * DOM エレメントをソートするためのユーティリティクラスです。
 */
public final class DOMElementSorter {

    /**
     * デフォルトのノード比較用クラス。
     */
    public static final NodeComparator NODE_COMPARATOR_DEFAULT =
            new NodeComparator() {
        @Override
        public int compare(final Node n1, final Node n2) {
            // タグ名でソート
            return n1.getNodeName().compareTo(n2.getNodeName());
        }
    };

    /**
     * デフォルトの出力対象外ノード判定用クラス。
     */
    public static final ExcludeTargetCondition
            EXCLUDE_TARGET_CONDITION_DEFAULT = new ExcludeTargetCondition() {
        @Override
        public boolean isExcludeTarget(final Node node) {
            // 除外するノードなし
            return false;
        }
    };

    /**
     * デフォルトのソート対象ノード判定用クラス。
     */
    public static final SortTargetCondition SORT_CONDITION_DEFAULT =
            new SortTargetCondition() {
        @Override
        public boolean isSortTarget(final Node node) {
            return true;
        }
    };

    /**
     * constructor.
     */
    private DOMElementSorter() { }

    /**
     * 指定された Document の要素を再帰的にソートする。
     *
     * <ul>
     * <li>ソート条件：タグ名</li>
     * <li>ソート対象ノード：すべて</li>
     * <li>出力対象外ノード：なし</li>
     * </ul>
     *
     * @param document ソート対象 Document
     */
    public static void sort(final Document document) {
        sort(document, true, SORT_CONDITION_DEFAULT,
                NODE_COMPARATOR_DEFAULT,
                EXCLUDE_TARGET_CONDITION_DEFAULT);
    }

    /**
     * 指定された Document を再帰的にソートする。
     *
     * @param documente ソート対象 Document
     * @param useValues ソートに使用するノードを表す XPath 式のリスト
     *                  index が小さければ小さいほどソートの優先順位が高い。
     * @param excludeXPath 出力対象外ノードを表す XPath 式
     * @throws XPathExpressionException XPath 処理失敗時
     */
    public static void sort(final Document document,
            final List<String> useValues,
            final String excludeXPath) throws XPathExpressionException {

        // useValues を使用して NodeComparatorXPath を作る。
        NodeComparator nc = null;
        if (useValues != null) {
            nc = new NodeComparatorXPath(useValues);
        }

        if (nc == null) {
            nc = NODE_COMPARATOR_DEFAULT;
        }

        // excludeXPath が指定されている場合、
        // XPath 式で取得できるノードを削除する。
        if (excludeXPath != null
                && excludeXPath != "") {
            DOMElementSorter.Util.removeNodes(document, excludeXPath);
        }

        sort(document, true, SORT_CONDITION_DEFAULT,
                nc,
                EXCLUDE_TARGET_CONDITION_DEFAULT);
    }

    /**
     * 指定された Node の要素を再帰的にソートする。
     *
     * @param node ソート対象 Node
     * @param  sortTargetCondition ソート対象ノード判定クラス
     * @param comparator ソートのための比較クラス
     * @param excludeTargetCondition 出力対象ノード判定クラス
     */
    public static void sort(final Node node,
            final SortTargetCondition sortTargetCondition,
            final Comparator<Node> comparator,
            final ExcludeTargetCondition excludeTargetCondition) {

        sort(node, true, sortTargetCondition,
                comparator, excludeTargetCondition);
    }

    /**
     * 指定された Node の要素を再帰的にソートする。
     *
     * @param node ソート対象 Node
     * @param isRecursion 再帰フラグ
     *        (true:再帰的にソートする, false:再帰的にソートしない)
     * @param  sortTargetCondition ソート対象ノード判定クラス
     * @param comparator ソートのための比較クラス
     * @param excludeTargetCondition 出力対象ノード判定クラス
     */
    public static void sort(final Node node,
            final boolean isRecursion,
            final SortTargetCondition sortTargetCondition,
            final Comparator<Node> comparator,
            final ExcludeTargetCondition excludeTargetCondition) {

        // 子ノード情報取得
        NodeList nodes = node.getChildNodes();
        int size = nodes.getLength();

        // 除外フラグが立っていない、かつ、
        // 再帰フラグが立って入れば、再帰する
        if (!excludeTargetCondition.isExcludeTarget(node) && isRecursion) {
            for (int i = 0; i < size; i++) {
                sort(nodes.item(i), isRecursion, sortTargetCondition,
                        comparator, excludeTargetCondition);
            }
        }

        // ソートターゲットでなければ何もしない
        if (!sortTargetCondition.isSortTarget(node)) {
            return;
        }

        // NodeList から ArrayList に入れ替える
        ArrayList<Node> nodeList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            nodeList.add(nodes.item(i));
        }

        // ArrayList<Node> をソートし、
        // append し直すことで子ノードのソートを行う
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
     * isSortTarget(Node node) の戻り値が
     * true であるノードの子要素をソートする。
     */
    public interface SortTargetCondition {
        /**
         * ソート対象かどうかを判定する。
         *
         * @param node 判定するノード
         * @return 判定結果(true:対象である, false:対象でない)
         */
        boolean isSortTarget(final Node node);
    }

    /**
     * 出力対象外ノードを判定するためのインターフェース。
     *
     * isExcludeTarget(Node node) の戻り値が true であるノードは出力しない。
     */
    public interface ExcludeTargetCondition {
        /**
         * 出力対象かどうかを判定する。
         *
         * @param node 判定するノード
         * @return 判定結果(true:対象である, false:対象でない)
         */
        boolean isExcludeTarget(final Node node);
    }

    /**
     * ノードのソートに利用する Comparator。
     */
    public interface NodeComparator extends Comparator<Node> { }

    /**
     * XPath 式を利用したノード比較用クラス。
     */
    public static class NodeComparatorXPath implements NodeComparator {

        /* ソートに使用する値を探すための XPath 式リスト */
        private List<XPathExpression> xPathExpressions;

        public NodeComparatorXPath(List<String> useValues)
                throws XPathExpressionException {

            XPathFactory xpathfactory = XPathFactory.newInstance();
            XPath xpath = xpathfactory.newXPath();

            xPathExpressions = new ArrayList<XPathExpression>();

            for (String xPathStr : useValues) {
                XPathExpression expression = xpath.compile(xPathStr);
                xPathExpressions.add(expression);
            }
        }

        @Override
        public int compare(final Node n1, final Node n2) {
            // そもそもノードの種類が違う場合、種類ごとにソートさせてしまおう
            if (n1.getNodeType() != n2.getNodeType()) {
                return n1.getNodeType() - n2.getNodeType();
            }

            // タグ名でソート
            for (XPathExpression xPathExpression : xPathExpressions) {
                try {
                    Node node1 = (Node)(xPathExpression.evaluate(n1, XPathConstants.NODE));
                    if (node1 != null) {
                        Node node2 = (Node)(xPathExpression.evaluate(n2, XPathConstants.NODE));
                        if (node2 != null) {
                            int result;
                            // Element の場合は、タグ名でソート
                            // そうでない場合はテキストでソート
                            if (node1.getNodeType() == Node.ELEMENT_NODE) {
                                // Element の場合は、タグ名でソート
                                result = node1.getNodeName().compareTo(node2.getNodeName());
                            } else {
                                // Element 以外の場合はテキストでソート
                                result = node1.getNodeValue().compareTo(node2.getNodeValue());
                            }

                            // ソート順が確定したら結果をリターン
                            // ソート順が確定できなければ次の要素を使って比較を行う
                            if (result == 0) {
                                continue;
                            } else {
                                return result;
                            }
                        }
                    }
                } catch (XPathExpressionException e) {
                    // あとで考える
                }
            }
            return 0;
        }
    };
    /**
     * DOM ツリーを作るのに便利な機能を実装したユーティリティクラス。
     */
    public static final class Util {

        /**
         * constructor.
         */
        private Util() { }

        /**
         * 指定されたファイルから Document を作成する。
         *
         * @param filePath XML ファイルのパス
         * @return Document インスタンス
         * @throws SAXException SAX の一般的なエラーまたは警告発生時
         * @throws ParserConfigurationException
         *              パーサーの重大な構成エラー発生時
         * @throws IOException なんらかの入出力例外の発生時
         */
        public static Document createDocument(final String filePath)
                throws SAXException, IOException,
                       ParserConfigurationException {

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(filePath);
        }

        /**
         * Document から XML 文字列を作成する。
         *
         * @param document Document インスタンス
         * @return XML 文字列
         * @throws TransformerException 変換処理例外発生時
         */
        public static String documentToString(final Document document)
                throws TransformerException {

            StringWriter sw = new StringWriter();
            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer transformer = tfactory.newTransformer();

            transformer.transform(
                    new DOMSource(document), new StreamResult(sw));
            return sw.toString().replaceAll("\\>\\<", ">\n<")
                    .replaceAll("\\n\\s+", "\n");
        }

        /**
         * Document から XPath 式で指定したノードを削除する。
         *
         * @param document Document インスタンス
         * @param excludePath 削除するノードの XPath 式
         * @throws XPathExpressionException XPath 処理失敗時
         */
        public static void removeNodes(
                final Document document,
                final String excludeXPath) throws XPathExpressionException {
            XPathFactory xpathfactory = XPathFactory.newInstance();
            XPath xpath = xpathfactory.newXPath();

            NodeList excludeNodeList = (NodeList) xpath.evaluate(
                    excludeXPath,
                    document,
                    XPathConstants.NODESET);

            int excludeNodeListLength = excludeNodeList.getLength();
            for (int i = 0; i < excludeNodeListLength; i++) {
                Node node = excludeNodeList.item(i);
                Node parentNode = node.getParentNode();

                if (parentNode != null) {
                    parentNode.removeChild(node);
                } else {
                    if (node instanceof Attr) {
                        Attr attr = (Attr)node;
                        Element ownerElement = attr.getOwnerElement();
                        ownerElement.removeAttributeNode(attr);
                    }
                }
            }
        }
    }
}
