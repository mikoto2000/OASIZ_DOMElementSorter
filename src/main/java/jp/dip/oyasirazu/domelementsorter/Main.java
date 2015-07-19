package jp.dip.oyasirazu.domelementsorter;

import java.io.IOException;
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

public final class Main {

    private Main() {}

    public static void main(final String[] args) throws SAXException,
                IOException, ParserConfigurationException,
                TransformerException,
                XPathExpressionException,
                CmdLineException{

        // オプションオブジェクト準備
        CmdOptions options = new CmdOptions();

        // パーサー準備
        CmdLineParser optionParser = new CmdLineParser(options);

        // パース
        optionParser.parseArgument(args);

        // デバッグプリント
        System.out.println(options);
        Document document = DOMElementSorter.Util.createDocument(options.getTargetFilePath().get(0));

        String useValues = options.getUseValues();
        List<String> useValueList;
        if (useValues == null) {
            useValueList = null;
        } else {
            useValueList = Arrays.asList(useValues.split(","));
        }

        String excludeXPath = options.getExcludeXPath();

        sortChildNode(document, useValueList, excludeXPath);
        String documentString =
                DOMElementSorter.Util.documentToString(document);

        System.out.println(documentString);
    }

    private static void sortChildNode(final Document document, List<String> useValues, String excludeXPath) throws XPathExpressionException {
        DOMElementSorter.sort(document, useValues, excludeXPath);
    }

    @Data
    static class CmdOptions {
        @Option(name="--useValues")
        private String useValues;

        @Option(name="--excludeXPath")
        private String excludeXPath;

        @Argument
        private List<String> targetFilePath;
    }
}
