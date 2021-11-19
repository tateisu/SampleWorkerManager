# SampleWorkerManager
AndroidXのWorkerManagerを使うサンプル

## Android JetPack App Startup を使った初期化
- GlobalStateInitializer

## Android HetPack Room を使ったアプリ内DB
- AppDatabase
- RItem

## WorkerManagerを使ったバックグラウンド実行
- ItemWorker
- 処理中に(サイレントですが)通知アイコンがでます。
- 複数の WorkRequestを同時に実行しないよう多重起動防止があります。
- (実装なし) WorkRequestにConstraint を追加することでネットワーク状況やバッテリー残量に応じてリクエストを保留することができます。

## メイン画面
- 画面上部のボタンを押すとデータを追加します。
- 画面の残りの部分のリストビューには現在のデータが表示されます。LiveDataで自動更新されます。
- 追加したデータは ItemWorker で随時処理されます。

## クラッシュ等で処理が中断された場合の検出
- データが処理されてる間にIDEから再度実行開始してみてください。
- RItemはstartTokenを持ち、 GlobalState.instanceToken(初期化時刻+プロセスID)と比較することで過去のプロセスで追加されたデータが中断されたことを検出します。
