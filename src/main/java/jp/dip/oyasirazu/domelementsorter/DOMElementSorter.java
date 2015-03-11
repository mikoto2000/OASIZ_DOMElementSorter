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
    private static void sort(final Node node,
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
    }
}
