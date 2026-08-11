# アプリ起動時のDB接続プロセス

起動ログと照らし合わせながら説明します。

---

## 全体の流れ

```
① SpringApplication.run() 開始
        ↓
② ApplicationContext の初期化
        ↓
③ Spring Data JPA のリポジトリスキャン
        ↓
④ HikariCP コネクションプールの初期化
        ↓
⑤ Hibernate による DB メタデータ取得
        ↓
⑥ JPA EntityManagerFactory の初期化
        ↓
⑦ アプリ起動完了
```

---

## 各ステップの詳細

### ① SpringApplication.run() 開始

```
Starting AuthServiceApplication v0.0.1-SNAPSHOT
using Java 25.0.2 with PID 457638
```

`AuthServiceApplication.main()` が呼ばれ、Springの起動プロセスが始まります。

---

### ② ApplicationContext の初期化

```
Root WebApplicationContext: initialization completed in 13188 ms
```

Spring の DI コンテナが初期化されます。全ての `@Component` `@Service` `@Repository` `@Controller` が検出・登録されます。この時点ではまだDB接続は発生していません。

---

### ③ Spring Data JPA のリポジトリスキャン

```
Bootstrapping Spring Data JPA repositories in DEFAULT mode.
Finished Spring Data repository scanning in 502 ms.
Found 5 JPA repository interfaces.
```

`UserRepository` `UserAuthRepository` `RegistrationTokenRepository` `SsoAppRepository` `UserSsoAppRepository` の5つが検出されます。この時点でもまだDB接続は発生していません。

---

### ④ HikariCP コネクションプールの初期化 ← ここで初めてDB接続

```
HikariPool-1 - Starting...
HikariPool-1 - Added connection oracle.jdbc.driver.T4CConnection@7686f701
HikariPool-1 - Start completed.
```

ここで初めて実際のDB接続が発生します。内部では以下の処理が行われています。

```
1. application.propertiesからDB接続情報を読み込む
   ├── URL: jdbc:oracle:thin:@adb23aiyy1_tp?TNS_ADMIN=/opt/auth-service/wallet
   ├── USERNAME: AG_PELO（環境変数から）
   └── PASSWORD: ***（環境変数から）

2. TNS_ADMINのWalletを読み込む
   ├── tnsnames.ora → 接続先ホスト・ポート・サービス名を解決
   ├── cwallet.sso → SSL認証情報
   └── truststore.jks → サーバー証明書の検証

3. Oracle ATPへのTCPS（SSL）接続を確立
   └── adb.ap-osaka-1.oraclecloud.com:1522

4. コネクションプールに接続を追加
   ├── minimum-idle: 2（最低2本を常時維持）
   └── maximum-pool-size: 5（最大5本まで拡張）
```

---

### ⑤ Hibernate による DB メタデータ取得

```
Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
Database version: 23.26
```

HikariCPが確立した接続を使ってHibernateがDBのメタデータを取得します。

```
内部で実行されるSQL（概念）：
SELECT * FROM V$VERSION  ← DBバージョンを確認
→ Oracle Database 23.26 と判明
→ OracleDialect を自動選択
```

---

### ⑥ JPA EntityManagerFactory の初期化

```
Initialized JPA EntityManagerFactory for persistence unit 'default'
```

エンティティクラスとDBテーブルのマッピングが確立されます。

```
User.java          → AG_PELO.USERS
UserAuth.java      → AG_PELO.USER_AUTHS
RegistrationToken  → AG_PELO.REGISTRATION_TOKENS
SsoApp.java        → AG_PELO.SSO_APPS
UserSsoApp.java    → AG_PELO.USER_SSO_APPS
```

`spring.jpa.hibernate.ddl-auto=none` の設定により、テーブルの自動作成・変更は行われません。

---

### ⑦ アプリ起動完了

```
Tomcat started on port 8080 (http) with context path '/'
Started AuthServiceApplication in 52.207 seconds
```

リクエストの受け付けが開始されます。

---

## コネクションプールの動作（起動後）

起動後はHikariCPが接続を管理します。

```
リクエスト到着
    ↓
HikariCPからコネクションを借りる（poolから取得）
    ↓
SQL実行（SELECT / INSERT / UPDATE / DELETE）
    ↓
コネクションをプールに返却（接続は切らない）
    ↓
次のリクエストで再利用
```

これにより毎回接続・切断のオーバーヘッドなしにDB操作ができます。

---

## ローカルとVM起動時間の差

```
ローカル（Mac）: 約2.6秒
VM（OCI）:      約52秒
```

差が大きい理由は以下の通りです。

| 原因 | 詳細 |
|---|---|
| VM性能 | MacのM1チップと比べCPU性能が低い |
| ネットワーク | VMからOracle ATPへの接続にネットワークレイテンシがある |
| JVMウォームアップ | Java 25のJITコンパイルの初期化 |
