# Script pour corriger l'erreur Git "dubious ownership"

Write-Host "=== Correction de l'erreur Git ===" -ForegroundColor Green
Write-Host ""

# Méthode 1 : Ajouter le répertoire actuel
$currentDir = (Get-Location).Path
Write-Host "Répertoire actuel: $currentDir" -ForegroundColor Yellow
Write-Host "Ajout du répertoire comme safe directory..." -ForegroundColor Cyan
git config --global --add safe.directory $currentDir

# Méthode 2 : Autoriser tous les répertoires (plus permissif)
Write-Host "Autorisation de tous les répertoires..." -ForegroundColor Cyan
git config --global --add safe.directory "*"

Write-Host ""
Write-Host "=== Test de git status ===" -ForegroundColor Green
$result = git status 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ SUCCÈS ! Git fonctionne maintenant." -ForegroundColor Green
    Write-Host ""
    Write-Host "Résultat:" -ForegroundColor Yellow
    Write-Host $result
} else {
    Write-Host "❌ Erreur toujours présente:" -ForegroundColor Red
    Write-Host $result
    Write-Host ""
    Write-Host "Essayez cette commande manuellement:" -ForegroundColor Yellow
    Write-Host "git config --global --add safe.directory 'C:/Users/pc/Desktop/rihebwchayma/rihebwchayma/back'" -ForegroundColor White
}

Write-Host ""
Write-Host "=== Terminé ===" -ForegroundColor Green

