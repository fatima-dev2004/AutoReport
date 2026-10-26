# Test Email Automatique - Utilise la config existante

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  TEST EMAIL AUTOMATIQUE" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 1. Login
$loginBody = @{
    email = "admin@sews.com"
    password = "Admin@123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. Voir config actuelle
Write-Host "Configuration SMTP actuelle:" -ForegroundColor Yellow
$smtpConfig = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/settings/smtp" -Headers $headers

Write-Host "  Host: $($smtpConfig.host)" -ForegroundColor White
Write-Host "  Port: $($smtpConfig.port)" -ForegroundColor White
Write-Host "  Username: $($smtpConfig.username)" -ForegroundColor White
Write-Host "  From: $($smtpConfig.fromEmail)" -ForegroundColor White
Write-Host ""

# 3. Vérifier si config valide
if ([string]::IsNullOrWhiteSpace($smtpConfig.host) -or [string]::IsNullOrWhiteSpace($smtpConfig.username)) {
    Write-Host "[ERREUR] Configuration SMTP incomplete!" -ForegroundColor Red
    Write-Host "Allez sur la page Parametres pour configurer SMTP" -ForegroundColor Yellow
    exit 1
}

# 4. Test envoi (à l'adresse admin)
Write-Host "Test envoi email..." -ForegroundColor Yellow
Write-Host "Destinataire: admin@sews.com" -ForegroundColor Gray
Write-Host ""

try {
    $testResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/settings/smtp/test" -Headers $headers -Method POST
    
    Write-Host "[OK] Email envoye!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Message du serveur: $($testResponse.message)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Verifiez votre boite email (et le dossier SPAM)" -ForegroundColor Yellow
}
catch {
    Write-Host "[ERREUR] Echec envoi" -ForegroundColor Red
    Write-Host ""
    
    $statusCode = $_.Exception.Response.StatusCode.Value__
    $errorBody = $_.ErrorDetails.Message
    
    Write-Host "Code erreur: $statusCode" -ForegroundColor Red
    
    if ($errorBody) {
        $errorObj = $errorBody | ConvertFrom-Json
        Write-Host "Message: $($errorObj.message)" -ForegroundColor Red
    }
    else {
        Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "Causes possibles:" -ForegroundColor Yellow
    Write-Host "1. Mot de passe Gmail incorrect (utilisez MOT DE PASSE D'APPLICATION)" -ForegroundColor White
    Write-Host "2. Validation en 2 etapes non activee sur Gmail" -ForegroundColor White
    Write-Host "3. Port 587 bloque par firewall" -ForegroundColor White
    Write-Host ""
    Write-Host "Comment obtenir mot de passe d'application Gmail:" -ForegroundColor Cyan
    Write-Host "https://myaccount.google.com/security -> Mots de passe des applications" -ForegroundColor Gray
}

Write-Host ""
