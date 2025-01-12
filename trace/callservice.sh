#!/bin/bash

# Global variable to store intermediate results
result=""

json=$(jq -n --arg name "Austin" '$ARGS.named')

#https://7k5i4srqjk4r7jiu3womfcrzme0xswnw.lambda-url.us-east-2.on.aws/https://6dc5w72hof3buknubzcwwvpqne0tbjtm.lambda-url.us-east-2.on.aws/"
endpoint="https://7k5i4srqjk4r7jiu3womfcrzme0xswnw.lambda-url.us-east-2.on.aws/"
#endpoint="https://q7vop3bzlljjxu7hl6p53uw5pq0scroq.lambda-url.us-east-2.on.aws/ "

response=$(curl -s -H "Content-Type: application/json" -X POST -d "$json" "$endpoint")
# Check if response is empty
if test -z "$response"; then
  echo "Error: Empty response received"
  exit 1
fi

# # Extract the value from response using jq
# result=$(echo "$response" | jq -r '.value // empty')

# # Check if value was extracted using test command
# if test -z "$result"; then
#   echo "Error: Failed to extract value from response"
#   echo "Raw response: $response"
#   exit 1
# fi

echo "Response received:"
echo "$response"
