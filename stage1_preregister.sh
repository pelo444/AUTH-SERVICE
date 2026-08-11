#!/bin/bash
BASE_URL="http://localhost:8080"

echo "============================"
echo "仮登録（メール送信）"
echo "============================"
curl -s -X POST $BASE_URL/api/users/preregister \
  -H "Content-Type: application/json" \
  -d '{"email":"yousukeyaegashi97@gmail.com"}'
echo ""
echo "メールに届いたtokenを確認して stage2_register.sh を実行してください。"

