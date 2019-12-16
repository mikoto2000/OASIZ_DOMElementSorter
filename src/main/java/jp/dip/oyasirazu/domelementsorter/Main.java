package jp.dip.oyasirazu.domelementsorter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import lombok.Data;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Main.
 */
public final class Main {

    /**
     * private constructor.
     */
    private Main() {}

    /**
     * Main method.
     */
    public static void main(final String[] args) throws SAXException,
                IOException, ParserConfigurationException,
                TransformerException,
                XPathExpressionException,
                CmdLineException {

        // オプションオブジェクト準備
        CmdOptions options = new CmdOptions();

        // パーサー準備
        CmdLineParser optionParser = new CmdLineParser(options);

        // パース
        try {
            optionParser.parseArgument(args);
        } catch (CmdLineException e) {
            printUsage(optionParser);
            System.exit(1);
        }

        // ヘルプ判定
        if (options.isHelp()) {
            printUsage(optionParser);
            System.exit(0);
        }

        String outputFilePathStr = options.getOutputFilePath();
        if (outputFilePathStr == null) {
            printUsage(optionParser);
            System.exit(0);
        }

        // TODO: BufferedWriter の作り方どうにかならないものか...
        try (BufferedWriter bw = Files.newBufferedWriter(
                    Paths.get(outputFilePathStr),
                    StandardCharsets.UTF_8,
                    new StandardOpenOption[]{
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE})) {
            Document document = DOMElementSorter.Util.createDocument(
                    options.getTargetFilePath().get(0));

            String excludeXPath = options.getExcludeXPath();

            sortChildNode(document, options.getUseValues(), excludeXPath);
            String documentString =
                    DOMElementSorter.Util.documentToString(document);

            bw.write(documentString);
        }
    }

    /**
     * 指定された Document を再帰的にソートする。
     *
     * @param document ソート対象 Document
     * @param useValues ソートに使用するノードを表す XPath 式のリスト
     *                  index が小さければ小さいほどソートの優先順位が高い。
     * @param excludeXPath 出力対象外ノードを表す XPath 式
     * @throws XPathExpressionException XPath 処理失敗時
     */
    private static void sortChildNode(
            final Document document,
            final List<String> useValues,
            final String excludeXPath) throws XPathExpressionException {
        DOMElementSorter.sort(document, useValues, excludeXPath);
    }

    private static void printUsage(CmdLineParser cmdLineParser) {
        // Useage を表示
        System.out.println("Useage:\n"
                + "  Main [options] INPUT_XML\n"
                + "\n"
                + "Options:");
        cmdLineParser.printUsage(System.out);
    }

    /**
     * コマンドラインオプションを表現するクラス。
     */
    @Data
    static class CmdOptions {

        /**
         * ヘルプ表示
         */
        @Option(name = "-h", aliases = "--help", usage = "print help.")
        private boolean isHelp;

        /**
         * 出力ファイルパス。
         */
        @Option(name = "-o", metaVar = "OUTPUT_XML", usage = "output file path.")
        private String outputFilePath;

        /**
         * ソートに使用する要素を表す XPath式。
         *
         * 優先度が高い順番で、カンマ区切りで XPath 式を列挙する。
         */
        @Option(name = "--useValue", aliases = "-V", metaVar = "XPATH",usage = "XPath for sort values.")
        private ArrayList<String> useValues;

        /**
         * 出力対象外ノードを表す XPath 式。
         */
        @Option(name = "--excludeXPath", metaVar = "XPATH", usage = "XPath for exclude values.")
        private String excludeXPath;

        /**
         * ソート対象のファイルパス。
         */
        @Argument
        private List<String> targetFilePath;
    }
}
