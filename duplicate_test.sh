#!/bin/bash
BASE_URL="http://localhost:8080"

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "使い方: ./duplicate_test.sh <token1> <token2>"
  echo "事前に仮登録を2回実行してtokenを2つ用意してください"
  exit 1
fi

TOKEN1=$1
TOKEN2=$2

echo "============================"
echo "① 1回目の本登録"
echo "============================"
curl -s -X POST $BASE_URL/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN1\",\"username\":\"testuser\",\"personId\":\"test001\",\"password\":\"Test1234ab\"}"
echo ""

sleep 1

echo "============================"
echo "② 同一PERSON_IDで2回目の登録（エラーになるはず）"
echo "============================"
curl -s -X POST $BASE_URL/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN2\",\"username\":\"testuser\",\"personId\":\"test001\",\"password\":\"Test1234ab\"}"
echo ""

echo "============================"
echo "テスト完了（②でエラーになっていれば成功）"
echo "============================"

