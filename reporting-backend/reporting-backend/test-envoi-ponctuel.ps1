# Test envoi ponctuel de rapport à un email spécifique

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  TEST ENVOI PONCTUEL DE RAPPORT" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
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
}

# 2. Lister les rapports disponibles
Write-Host "Rapports disponibles:" -ForegroundColor Yellow
$reports = Invoke-RestMethod -Uri "http://localhost:8080/api/reports?page=0&size=5" -Headers $headers

if ($reports.totalElements -eq 0) {
    Write-Host "  [ATTENTION] Aucun rapport disponible" -ForegroundColor Red
    Write-Host "  Generez d'abord un rapport depuis l'interface" -ForegroundColor Yellow
    exit 1
}

foreach ($r in $reports.content) {
    Write-Host "  - ID: $($r.id) | $($r.title) | $($r.periodStart) -> $($r.periodEnd)" -ForegroundColor White
}

# 3. Prendre le premier rapport
$reportId = $reports.content[0].id
$reportTitle = $reports.content[0].title

Write-Host ""
Write-Host "Test avec rapport ID: $reportId - $reportTitle" -ForegroundColor Cyan
Write-Host ""

# 4. Demander email destinataire
$emailDest = Read-Host "Email destinataire (ex: fatimabenritounia37@gmail.com)"
$nomDest = Read-Host "Nom destinataire (optionnel, Entree pour skip)"

# 5. Envoyer
Write-Host ""
Write-Host "Envoi en cours..." -ForegroundColor Yellow

try {
    $uri = "http://localhost:8080/api/reports/$reportId/send-to?email=$emailDest"
    
    if ($nomDest -and $nomDest.Trim() -ne "") {
        $uri += "&recipientName=$([System.Uri]::EscapeDataString($nomDest))"
    }
    
    $response = Invoke-RestMethod -Uri $uri -Method POST -Headers $headers
    
    Write-Host ""
    Write-Host "[OK] $($response.message)" -ForegroundColor Green
    Write-Host ""
    Write-Host "Verifiez l'email $emailDest (et le dossier SPAM)" -ForegroundColor Cyan
}
catch {
    Write-Host ""
    Write-Host "[ERREUR] Echec envoi" -ForegroundColor Red
    
    if ($_.ErrorDetails.Message) {
        $errorObj = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "Message: $($errorObj.message)" -ForegroundColor Red
    }
    else {
        Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
