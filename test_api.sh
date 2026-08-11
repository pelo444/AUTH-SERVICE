#!/bin/bash

BASE_URL="http://localhost:8080"
COOKIE_FILE="cookies.txt"
LOG_FILE="api_test.log"

# ログファイル初期化
> $LOG_FILE
> $COOKIE_FILE

echo "============================" | tee -a $LOG_FILE
echo "① 新規ユーザー登録" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE
curl -s -X POST $BASE_URL/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","personId":"test001","password":"Test1234ab"}' \
  | tee -a $LOG_FILE
echo "" | tee -a $LOG_FILE

sleep 1

echo "============================" | tee -a $LOG_FILE
echo "② ログイン" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE
curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"personId":"test001","password":"Test1234ab"}' \
  -c $COOKIE_FILE \
  | tee -a $LOG_FILE
echo "" | tee -a $LOG_FILE

sleep 1

echo "============================" | tee -a $LOG_FILE
echo "③ ログイン状態確認" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE
curl -s -X GET $BASE_URL/api/auth/status \
  -b $COOKIE_FILE \
  | tee -a $LOG_FILE
echo "" | tee -a $LOG_FILE

sleep 1

echo "============================" | tee -a $LOG_FILE
echo "④ username変更" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE
curl -s -X PUT $BASE_URL/api/users/username \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser2"}' \
  -b $COOKIE_FILE \
  | tee -a $LOG_FILE
echo "" | tee -a $LOG_FILE

sleep 1

echo "============================" | tee -a $LOG_FILE
echo "⑤ パスワード変更" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE
curl -s -X PUT $BASE_URL/api/users/password \
  -H "Content-Type: application/json" \
  -d '{"password":"NewPass5678ab"}' \
  -b $COOKIE_FILE \
  | tee -a $LOG_FILE
echo "" | tee -a $LOG_FILE

sleep 1

echo "============================" | tee -a $LOG_FILE
echo "⑥ ログアウト" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE
curl -s -X POST $BASE_URL/api/auth/logout \
  -b $COOKIE_FILE -c $COOKIE_FILE \
  | tee -a $LOG_FILE
echo "" | tee -a $LOG_FILE

sleep 1

echo "============================" | tee -a $LOG_FILE
echo "⑦ ログアウト後の状態確認" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE
curl -s -X GET $BASE_URL/api/auth/status \
  -b $COOKIE_FILE \
  | tee -a $LOG_FILE
echo "" | tee -a $LOG_FILE

sleep 1

echo "============================" | tee -a $LOG_FILE
echo "⑧ 再ログイン（新パスワードで）" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE
curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"personId":"test001","password":"NewPass5678ab"}' \
  -c $COOKIE_FILE \
  | tee -a $LOG_FILE
echo "" | tee -a $LOG_FILE

sleep 1

echo "============================" | tee -a $LOG_FILE
echo "⑨ ユーザー削除" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE
curl -s -X DELETE $BASE_URL/api/users \
  -b $COOKIE_FILE \
  | tee -a $LOG_FILE
echo "" | tee -a $LOG_FILE

echo "============================" | tee -a $LOG_FILE
echo "テスト完了。結果は $LOG_FILE を確認してください。" | tee -a $LOG_FILE
echo "============================" | tee -a $LOG_FILE

