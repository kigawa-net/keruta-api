# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がこのリポジトリでコードを扱う際のガイダンスを提供します。

## コマンド

### ビルドとテスト
```bash
# プロジェクト全体をビルド
./gradlew build

# Spring Bootアプリケーションを起動
./gradlew bootRun

# 全てのテストを実行
./gradlew test

# 詳細な出力でテストを実行
./gradlew test --continue
```

### コード品質
```bash
# コードスタイルをチェック (KtLint)
./gradlew ktlintCheck

# コードを自動フォーマット
./gradlew ktlintFormat

# クリーンビルド
./gradlew clean
```

### OpenAPI コード生成
```bash
# OpenAPIからコード生成 (自動でコンパイル前に実行されます)
./gradlew openApiGenerate
```

## アーキテクチャ

Kerutaは分散タスク実行システムのバックエンドAPIです。

### プロジェクト構造
- `net.kigawa.keruta.api` - REST APIコントローラー（エントリーポイント）
- `net.kigawa.keruta.core` - ビジネスロジックとドメインモデル
- `net.kigawa.keruta.infra.security` - セキュリティ設定とJWT認証
- `net.kigawa.keruta.infra.app` - Kubernetesインテグレーションとジョブオーケストレーション

### 主要技術スタック
- **フレームワーク**: Spring Boot 3.2.0 + Spring Security + Spring Data MongoDB
- **言語**: Kotlin 1.9.25 (Java 21対応)
- **データベース**: MongoDB (開発環境), Keycloak用のPostgreSQL
- **コンテナ化**: Docker Compose (MongoDB, PostgreSQL, Keycloak, API)
- **認証**: JWT + Keycloak
- **非同期処理**: Kotlin Coroutines
- **コード生成**: OpenAPI Generator (kotlin-spring)

### 開発環境
- MongoDBは`docker-compose.yml`で起動 (ポート27017)
- Keycloakは`http://localhost:8180`で起動 (admin/admin)
- APIは`http://localhost:8080`で起動

### OpenAPI仕様
- OpenAPI仕様: `src/main/resources/openapi.yaml`
- 生成されたコード: `build/generated/src/main/kotlin`
- APIパッケージ: `net.kigawa.keruta.api.generated`
- モデルパッケージ: `net.kigawa.keruta.model.generated`

### 重要な設定
- JVMクラッシュ回避のため`-XX:TieredStopAtLevel=1`を設定
- KtLintは生成されたファイルを除外
- MongoDB接続設定はDocker環境用の環境変数で管理