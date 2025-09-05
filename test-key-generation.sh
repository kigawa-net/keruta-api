#!/bin/bash

# Test script for Git key generation endpoint
echo "Testing Git Key Generation Endpoint"
echo "===================================="

# Check if application is running
echo "1. Checking if application is running on port 8080..."
if curl -s -f http://localhost:8080/api/v1/health > /dev/null 2>&1; then
    echo "✓ Application is running"
else
    echo "✗ Application is not running on port 8080"
    echo "   Please start the application first with: ./gradlew bootRun"
    exit 1
fi

echo ""
echo "2. Testing the /api/v1/git/keys/generate endpoint..."
echo ""

# Test the key generation endpoint
response=$(curl -s -w "%{http_code}" -X POST \
  http://localhost:8080/api/v1/git/keys/generate \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-key-'$(date +%s)'",
    "keyType": "SSH",
    "keySize": 2048,
    "algorithm": "ssh-rsa"
  }' 2>&1)

http_code="${response: -3}"
response_body="${response%???}"

echo "HTTP Status Code: $http_code"
echo "Response:"
echo "$response_body" | jq . 2>/dev/null || echo "$response_body"

case $http_code in
    200)
        echo ""
        echo "✓ SUCCESS: Key generation endpoint is working correctly!"
        ;;
    404)
        echo ""
        echo "✗ ERROR: Endpoint not found (404)"
        echo "   This confirms the routing issue mentioned in the error log."
        echo "   The endpoint exists in code but Spring cannot route to it."
        ;;
    500)
        echo ""
        echo "⚠ WARNING: Internal server error (500)"
        echo "   The endpoint is reachable but there may be an implementation issue."
        ;;
    *)
        echo ""
        echo "✗ ERROR: Unexpected HTTP status code: $http_code"
        ;;
esac

echo ""
echo "3. Testing with GET request (should fail)..."
get_response=$(curl -s -w "%{http_code}" http://localhost:8080/api/v1/git/keys/generate 2>&1)
get_http_code="${get_response: -3}"
get_response_body="${get_response%???}"

echo "GET Request HTTP Status Code: $get_http_code"
if [[ $get_http_code == "405" ]]; then
    echo "✓ Correctly returns 405 Method Not Allowed for GET request"
elif [[ $get_http_code == "404" ]]; then
    echo "✗ Returns 404 for GET request (this suggests a routing problem)"
else
    echo "⚠ Unexpected response for GET request: $get_http_code"
fi

echo ""
echo "Test completed."