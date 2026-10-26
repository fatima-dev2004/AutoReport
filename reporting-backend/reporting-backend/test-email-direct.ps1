# Test envoi email SMTP
# Remplacez YOUR_TOKEN par votre token JWT

$token = Read-Host "Entrez votre token JWT"

Write-Host "Test 1: Configuration SMTP..." -ForegroundColor Yellow
curl -X GET "http://localhost:8080/api/admin/settings" `
  -H "Authorization: Bearer $token"

Write-Host "`nTest 2: Test SMTP..." -ForegroundColor Yellow
curl -X POST "http://localhost:8080/api/admin/settings/test-email" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{"email":"fatimabenritounia37@gmail.com"}'

Write-Host "`nTest terminé!" -ForegroundColor Green
