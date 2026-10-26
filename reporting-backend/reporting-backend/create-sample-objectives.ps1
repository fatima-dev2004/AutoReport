# Script PowerShell pour créer des objectifs de test

# 1. Login pour obtenir le token
$loginBody = @{
    email = "admin@sews.com"
    password = "Admin@123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -Body $loginBody `
    -ContentType "application/json"

$token = $loginResponse.token
Write-Host "✅ Login réussi. Token obtenu." -ForegroundColor Green

# 2. Créer plusieurs objectifs de test
$objectives = @(
    @{
        line = "Ligne A"
        periodType = "MONTHLY"
        startDate = "2026-08-01"
        endDate = "2026-08-31"
        targetQuantity = 50000
        targetQualityRate = 95.0
        maxDefects = 2500
        active = $true
        notes = "Objectif production Août 2026"
    },
    @{
        line = "Ligne B"
        periodType = "MONTHLY"
        startDate = "2026-08-01"
        endDate = "2026-08-31"
        targetQuantity = 45000
        targetQualityRate = 93.0
        maxDefects = 3150
        active = $true
        notes = "Objectif production Août 2026"
    },
    @{
        line = "Ligne C"
        periodType = "MONTHLY"
        startDate = "2026-08-01"
        endDate = "2026-08-31"
        targetQuantity = 40000
        targetQualityRate = 94.0
        maxDefects = 2400
        active = $true
        notes = "Objectif production Août 2026"
    },
    @{
        line = "Ligne A"
        periodType = "WEEKLY"
        startDate = "2026-08-03"
        endDate = "2026-08-09"
        targetQuantity = 12500
        targetQualityRate = 95.5
        maxDefects = 563
        active = $true
        notes = "Objectif hebdomadaire semaine 1"
    },
    @{
        line = "Ligne B"
        periodType = "WEEKLY"
        startDate = "2026-08-03"
        endDate = "2026-08-09"
        targetQuantity = 11000
        targetQualityRate = 93.5
        maxDefects = 715
        active = $true
        notes = "Objectif hebdomadaire semaine 1"
    }
)

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

foreach ($objective in $objectives) {
    $body = $objective | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/objectives" `
            -Method POST `
            -Headers $headers `
            -Body $body
        
        Write-Host "✅ Objectif créé: $($objective.line) - $($objective.periodType)" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Erreur création objectif $($objective.line): $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Récupération de la liste des objectifs..." -ForegroundColor Cyan

try {
    $uri = "http://localhost:8080/api/objectives?page=0" + "&" + "size=20"
    $listResponse = Invoke-RestMethod -Uri $uri -Headers $headers
    
    Write-Host "Total objectifs: $($listResponse.totalElements)" -ForegroundColor Green
    Write-Host ""
    Write-Host "Objectifs créés:" -ForegroundColor Yellow
    
    foreach ($obj in $listResponse.content) {
        $line = $obj.line
        $period = $obj.periodType
        $qty = $obj.targetQuantity
        $quality = $obj.targetQualityRate
        Write-Host "  - ID: $($obj.id) - $line - $period - $qty unites - Qualite: $quality%" -ForegroundColor White
    }
}
catch {
    Write-Host "Erreur récupération liste: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "Script termine! Rafraichissez la page /objectives dans le navigateur." -ForegroundColor Green
