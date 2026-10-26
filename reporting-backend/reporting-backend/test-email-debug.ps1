# Script de Debug - Test Email

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  DEBUG TEST EMAIL - SEWS REPORTING" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 1. Login
Write-Host "1. Connexion admin..." -ForegroundColor Yellow
$loginBody = @{
    email = "admin@sews.com"
    password = "Admin@123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "   [OK] Connecte" -ForegroundColor Green
}
catch {
    Write-Host "   [ERREUR] Login echoue: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. Vérifier config SMTP actuelle
Write-Host ""
Write-Host "2. Verification config SMTP en base..." -ForegroundColor Yellow

try {
    $smtpConfig = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/settings/smtp" -Headers $headers
    
    Write-Host "   Host: $($smtpConfig.host)" -ForegroundColor Gray
    Write-Host "   Port: $($smtpConfig.port)" -ForegroundColor Gray
    Write-Host "   Username: $($smtpConfig.username)" -ForegroundColor Gray
    Write-Host "   From Email: $($smtpConfig.fromEmail)" -ForegroundColor Gray
    Write-Host "   From Name: $($smtpConfig.fromName)" -ForegroundColor Gray
    
    if ([string]::IsNullOrWhiteSpace($smtpConfig.host)) {
        Write-Host "   [ATTENTION] Host SMTP vide!" -ForegroundColor Red
    }
    if ([string]::IsNullOrWhiteSpace($smtpConfig.username)) {
        Write-Host "   [ATTENTION] Username vide!" -ForegroundColor Red
    }
}
catch {
    Write-Host "   [ERREUR] Impossible de recuperer config SMTP" -ForegroundColor Red
}

# 3. Demander les infos à l'utilisateur
Write-Host ""
Write-Host "3. Configuration SMTP pour Gmail..." -ForegroundColor Yellow
Write-Host ""

$emailDest = Read-Host "   Votre email de destination (pour recevoir le test)"
Write-Host ""
Write-Host "   Pour Gmail, vous devez creer un MOT DE PASSE D'APPLICATION :" -ForegroundColor Cyan
Write-Host "   1. Allez sur https://myaccount.google.com/security" -ForegroundColor White
Write-Host "   2. Activez 'Validation en 2 etapes'" -ForegroundColor White
Write-Host "   3. Allez dans 'Mots de passe des applications'" -ForegroundColor White
Write-Host "   4. Creez un nouveau mot de passe pour 'Reporting System'" -ForegroundColor White
Write-Host "   5. Copiez le mot de passe genere (format: xxxx xxxx xxxx xxxx)" -ForegroundColor White
Write-Host ""

$gmailUser = Read-Host "   Votre email Gmail (expediteur)"
$gmailPass = Read-Host "   Mot de passe d'application Gmail" -AsSecureString
$gmailPassPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($gmailPass)
)

# 4. Enregistrer la config SMTP
Write-Host ""
Write-Host "4. Enregistrement config SMTP..." -ForegroundColor Yellow

$smtpData = @{
    host = "smtp.gmail.com"
    port = "587"
    username = $gmailUser
    password = $gmailPassPlain
    fromEmail = $gmailUser
    fromName = "SEWS Reporting System"
} | ConvertTo-Json

try {
    $saveResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/settings/smtp" -Method PUT -Headers $headers -Body $smtpData
    Write-Host "   [OK] Configuration enregistree" -ForegroundColor Green
}
catch {
    Write-Host "   [ERREUR] Echec enregistrement: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 5. Test d'envoi
Write-Host ""
Write-Host "5. Envoi email de test..." -ForegroundColor Yellow
Write-Host "   Destinataire: $emailDest" -ForegroundColor Gray

try {
    $testUri = "http://localhost:8080/api/admin/settings/smtp/test"
    $testResponse = Invoke-RestMethod -Uri $testUri -Headers $headers -Method POST
    
    Write-Host ""
    Write-Host "   [OK] Email envoye avec succes!" -ForegroundColor Green
    Write-Host ""
    Write-Host "   Verifiez votre boite email: $emailDest" -ForegroundColor Cyan
    Write-Host "   (peut prendre 1-2 minutes)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "   Si vous ne recevez rien:" -ForegroundColor Yellow
    Write-Host "   - Verifiez le dossier SPAM" -ForegroundColor White
    Write-Host "   - Verifiez que le mot de passe d'application est correct" -ForegroundColor White
    Write-Host "   - Verifiez que la validation en 2 etapes est activee" -ForegroundColor White
}
catch {
    $errorMsg = $_.Exception.Message
    Write-Host ""
    Write-Host "   [ERREUR] Echec envoi email" -ForegroundColor Red
    Write-Host ""
    Write-Host "   Details de l'erreur:" -ForegroundColor Yellow
    Write-Host "   $errorMsg" -ForegroundColor Red
    Write-Host ""
    
    if ($errorMsg -like "*Authentication failed*" -or $errorMsg -like "*535*") {
        Write-Host "   CAUSE: Identifiants incorrects" -ForegroundColor Yellow
        Write-Host "   SOLUTION:" -ForegroundColor Cyan
        Write-Host "   - Utilisez un MOT DE PASSE D'APPLICATION (pas votre mot de passe Gmail normal)" -ForegroundColor White
        Write-Host "   - Verifiez que la validation en 2 etapes est activee" -ForegroundColor White
    }
    elseif ($errorMsg -like "*Connection*" -or $errorMsg -like "*timeout*") {
        Write-Host "   CAUSE: Impossible de se connecter au serveur SMTP" -ForegroundColor Yellow
        Write-Host "   SOLUTION:" -ForegroundColor Cyan
        Write-Host "   - Verifiez votre connexion Internet" -ForegroundColor White
        Write-Host "   - Verifiez que le port 587 n'est pas bloque par un firewall" -ForegroundColor White
    }
    
    Write-Host ""
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  FIN DU TEST" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
