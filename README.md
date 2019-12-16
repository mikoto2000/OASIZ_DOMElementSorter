OASIZ_DOMElementSorter
======================

XML element sort tool.

Usage:
------

```sh
Useage:
  Main [options] INPUT_XML

Options:
 --excludeXPath XPATH  : XPath for exclude values.
 --useValue (-V) XPATH : XPath for sort values.
 -h (--help)           : print help.
 -o OUTPUT_XML         : output file path.
```

```sh
java -jar OASIZ_DOMElementSorter-x.x.x.jar --excludeXPath EXCLUDE_NODE_XPATH --useValue SORT_VALUE1 --useValue SORT_VALUE2 -o OUTPUT_FILE INPUT_FILE

# 1. `--excludeXPath`: 属性 UUID, TIMESTAMP を削除
# 2. `-V .`: タグ名でソート
# 3. `-V ./NAME/text()`: タグ名が同一なら NAME の値でソート
# 4. `-o test.xml`: ソート結果を test.xml へ出力
# 5. `input.xml`: インプット XML ファイル
java -jar OASIZ_DOMElementSorter-x.x.x.jar \
         --excludeXPath "//*/@UUID|//*/@TIMESTAMP" \
         -V . \
         -V ./NAME/text() \
         -o test.xml \
         input.xml
```


Requirements:
-------------

- java version "1.8.0_112" or later.


License:
--------

Copyright (C) 2019 mikoto2000

This software is released under the MIT License, see LICENSE

このソフトウェアは MIT ライセンスの下で公開されています。 LICENSE を参照してください。


Author:
-------

mikoto2000 <mikoto2000@gmail.com>


