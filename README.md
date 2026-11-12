# 小窓検索 - 選択テキストを小窓で検索

文字を選択 → メニュー → 「小窓検索」で、他のアプリを開いたまま小窓で検索できるAndroidアプリです。
修正前提の試作版です。

## 使い方
1. この中身をGitHubにそのまま上書きアップロード
2. Actions → Build APK → Run workflow → 完了後 `app-debug-apk` から APKをダウンロード
3. 古いAPKをアンインストールしてから新しいAPKをインストール
4. 初回起動で「他のアプリの上に表示」を許可する（小窓のため必須）
5. どこかのアプリで文字を長押し → 選択メニュー → 「小窓検索」

## 仕様
- 小窓は他のアプリの上に重なるフローティングウィンドウ。ドラッグで移動、×で閉じる、スクロールで全部読める
- デフォルトあり → 選んだら即そのサイトの小窓
- デフォルトなし → 「どのサイトで開く？」一覧が出る。小窓で「次回からこのサイトで開く」にチェックするとデフォルトになる
- 小窓の「サイトを変える」で、その場で別サイトに切り替え可能
- サイトは追加・編集・削除できる。URLに {query} を入れると選択文字に置き換わる

## 初期サイト
- Wikipedia: https://ja.m.wikipedia.org/wiki/{query}
- YouTube: https://www.youtube.com/results?search_query={query}
- ニコニコ動画: https://www.nicovideo.jp/search/{query}
- 5ちゃんねる: https://find.5ch.net/search?q={query} （スレッド一覧→タップでスレッドを開く）
- 爆サイ: https://www.google.com/search?q=site:bakusai.com+{query} （Google経由で爆サイのスレッドを検索）
- Google: https://www.google.com/search?q={query}

## カスタム追加の例
- 名前: Amazon / URL: https://www.amazon.co.jp/s?k={query}
- サイトで1回検索して、そのURLの検索ワード部分を {query} に置き換えるだけ

## 注意
- 銀行アプリ・コピー禁止アプリなど、一部アプリでは選択メニューに出ません（Androidの仕様）
- ログインが必要なサイトは小窓内でログイン画面が出ます
- WebViewをブロックするサイトは「ブラウザで開く」に切り替えてください
- 修正したい点があれば教えてください。ここから直していきます
