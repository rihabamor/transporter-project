# Script pour corriger le repository Git (initialisé au mauvais endroit)

Write-Host "=== Correction du Repository Git ===" -ForegroundColor Green
Write-Host ""

$currentDir = (Get-Location).Path
Write-Host "Répertoire actuel: $currentDir" -ForegroundColor Yellow
Write-Host ""

# Vérifier si .git existe dans le répertoire actuel
if (Test-Path .git) {
    Write-Host "✅ .git trouvé dans le répertoire actuel" -ForegroundColor Green
    Write-Host "Le repository est au bon endroit." -ForegroundColor Green
} else {
    Write-Host "⚠️ Pas de .git dans le répertoire actuel" -ForegroundColor Yellow
    Write-Host "Initialisation d'un nouveau repository Git..." -ForegroundColor Cyan
    
    # Initialiser Git dans le bon répertoire
    git init
    
    Write-Host "✅ Repository Git initialisé dans: $currentDir" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== Vérification ===" -ForegroundColor Green
Write-Host "Test de git status (devrait montrer uniquement les fichiers du projet)..." -ForegroundColor Cyan
Write-Host ""

# Test avec git status mais limité au répertoire actuel
$status = git status --porcelain 2>&1 | Select-Object -First 20

if ($status) {
    Write-Host "Fichiers détectés:" -ForegroundColor Yellow
    $status | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host "Aucun fichier modifié détecté" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Prochaines Étapes ===" -ForegroundColor Green
Write-Host "1. Ajouter les fichiers du projet: git add ." -ForegroundColor White
Write-Host "2. Créer un commit: git commit -m 'Initial commit'" -ForegroundColor White
Write-Host "3. Ajouter le remote: git remote add origin https://github.com/rihabamor/VOTRE-REPO.git" -ForegroundColor White
Write-Host "4. Pousser: git push -u origin main" -ForegroundColor White

