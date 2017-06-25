<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xslt">
    <xsl:output method="xml" encoding="UTF-8"
            indent="yes" xalan:indent-amount="4"/>
    <xsl:template match="/">
        <!-- xml 宣言とルート要素の間の改行 -->
        <xsl:text disable-output-escaping = "yes">&#10;</xsl:text>
        <!-- xsl:output のインデント設定を行いつつ要素コピー -->
        <xsl:copy-of select="."/>
    </xsl:template>
</xsl:stylesheet>

