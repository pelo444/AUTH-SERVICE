#!/bin/bash
BASE_URL="http://localhost:8080"
COOKIE_FILE="cookies.txt"

if [ -z "$1" ]; then
  echo "使い方: ./stage2_register.sh <token>"
  exit 1
fi

TOKEN=$1

echo "============================"
echo "① token検証"
echo "============================"
curl -s -X GET "$BASE_URL/api/users/verify?token=$TOKEN"
echo ""

sleep 1

echo "============================"
echo "② 本登録"
echo "============================"
curl -s -X POST $BASE_URL/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\",\"username\":\"testuser\",\"personId\":\"test001\",\"password\":\"Test1234ab\"}"
echo ""

sleep 1

echo "============================"
echo "③ ログイン"
echo "============================"
curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"personId":"test001","password":"Test1234ab"}' \
  -c $COOKIE_FILE
echo ""

sleep 1

echo "============================"
echo "④ ログイン状態確認"
echo "============================"
curl -s -X GET $BASE_URL/api/auth/status \
  -b $COOKIE_FILE
echo ""

sleep 1

echo "============================"
echo "⑤ username変更"
echo "============================"
curl -s -X PUT $BASE_URL/api/users/username \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser2"}' \
  -b $COOKIE_FILE
echo ""

sleep 1

echo "============================"
echo "⑥ パスワード変更"
echo "============================"
curl -s -X PUT $BASE_URL/api/users/password \
  -H "Content-Type: application/json" \
  -d '{"password":"NewPass5678ab"}' \
  -b $COOKIE_FILE
echo ""

sleep 1

echo "============================"
echo "⑦ ログアウト"
echo "============================"
curl -s -X POST $BASE_URL/api/auth/logout \
  -b $COOKIE_FILE -c $COOKIE_FILE
echo ""

sleep 1

echo "============================"
echo "⑧ 再ログイン（新パスワード）"
echo "============================"
curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"personId":"test001","password":"NewPass5678ab"}' \
  -c $COOKIE_FILE
echo ""

sleep 1

echo "============================"
echo "⑨ ユーザー削除"
echo "============================"
curl -s -X DELETE $BASE_URL/api/users \
  -b $COOKIE_FILE
echo ""

echo "============================"
echo "テスト完了"
echo "============================"

