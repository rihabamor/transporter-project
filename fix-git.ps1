# Script pour corriger le problème Git "dubious ownership"

Write-Host "=== Correction du problème Git ===" -ForegroundColor Green

# 1. Ajouter le répertoire actuel comme safe directory
$currentDir = (Get-Location).Path
Write-Host "Répertoire actuel: $currentDir" -ForegroundColor Yellow

# 2. Configurer Git pour accepter ce répertoire
Write-Host "`nConfiguration de Git..." -ForegroundColor Cyan
git config --global --add safe.directory $currentDir
git config --global --add safe.directory "*"

# 3. Vérifier l'état Git
Write-Host "`nVérification de l'état Git..." -ForegroundColor Cyan
git status

# 4. Vérifier si un remote est configuré
Write-Host "`nVérification des remotes..." -ForegroundColor Cyan
git remote -v

Write-Host "`n=== Terminé ===" -ForegroundColor Green
Write-Host "`nSi vous voyez toujours des erreurs, exécutez:" -ForegroundColor Yellow
Write-Host "  git config --global --add safe.directory '*'`n" -ForegroundColor White

