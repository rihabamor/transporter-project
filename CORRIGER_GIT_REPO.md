# 🔧 Corriger le Problème Git - Repository au Mauvais Endroit

## ❌ Problème Identifié

Git voit **TOUS les fichiers du disque C:/** au lieu de voir uniquement les fichiers de votre projet.

Cela signifie que le repository Git a été initialisé à la **racine C:/** au lieu d'être dans votre dossier de projet.

## ✅ Solution

### Option 1 : Exécuter le Script (Recommandé)

```powershell
.\fix-git-repo.ps1
```

### Option 2 : Réinitialiser Git Manuellement

**Étape 1 : Vérifier où est le .git**

```powershell
# Vérifier dans le répertoire actuel
Test-Path .git

# Vérifier à la racine (ne devrait pas exister)
Test-Path C:\.git
```

**Étape 2 : Initialiser Git dans le bon répertoire**

```powershell
# S'assurer d'être dans le bon répertoire
cd C:\Users\pc\Desktop\rihebwchayma\rihebwchayma\back

# Initialiser Git ici
git init
```

**Étape 3 : Vérifier que ça fonctionne**

```powershell
git status
```

**Maintenant, vous devriez voir uniquement les fichiers de votre projet !**

## 🎯 Commandes Complètes

```powershell
# 1. Aller dans le répertoire du projet
cd C:\Users\pc\Desktop\rihebwchayma\rihebwchayma\back

# 2. Initialiser Git (si pas déjà fait dans ce dossier)
git init

# 3. Vérifier
git status

# 4. Ajouter les fichiers du projet
git add .

# 5. Commit
git commit -m "Initial commit with Docker, CI/CD and tests"

# 6. Ajouter le remote GitHub
git remote add origin https://github.com/rihabamor/VOTRE-REPO-NAME.git

# 7. Pousser
git branch -M main
git push -u origin main
```

## ⚠️ Si le .git est à la racine C:/

**ATTENTION : Ne supprimez PAS le .git à la racine si vous avez d'autres projets qui l'utilisent !**

Si vous voulez le supprimer quand même :

```powershell
# ⚠️ ATTENTION : Cela supprimera le repository Git à la racine
Remove-Item C:\.git -Recurse -Force
```

**Mais il est préférable de simplement initialiser un nouveau repository dans votre dossier de projet.**

## ✅ Vérification

Après correction, `git status` devrait montrer :
- ✅ Uniquement les fichiers de votre projet
- ✅ Pas de fichiers Windows, Program Files, etc.

## 📝 Prochaines Étapes

Une fois Git corrigé :

1. ✅ `git add .` - Ajouter les fichiers du projet
2. ✅ `git commit -m "..."` - Créer un commit
3. ✅ `git remote add origin ...` - Ajouter GitHub
4. ✅ `git push -u origin main` - Pousser vers GitHub

