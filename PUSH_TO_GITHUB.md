# Guide : Pousser le Code vers GitHub

## 🔧 Étape 1 : Corriger l'Erreur Git

L'erreur que vous avez vue est due à une sécurité Git. Corrigeons-la :

### Option A : Exécuter le script (Recommandé)

```powershell
# Exécuter le script de correction
.\fix-git.ps1
```

### Option B : Commandes manuelles

```powershell
# Ajouter le répertoire actuel comme safe
git config --global --add safe.directory "C:/Users/pc/Desktop/rihebwchayma/rihebwchayma/back"

# Ou autoriser tous les répertoires (moins sécurisé mais fonctionne)
git config --global --add safe.directory "*"
```

## 📋 Étape 2 : Vérifier l'État Git

```powershell
# Vérifier l'état
git status

# Vérifier si un remote est configuré
git remote -v
```

## 🔗 Étape 3 : Configurer le Remote GitHub

Si vous n'avez pas encore de remote configuré :

```powershell
# Ajouter votre repository GitHub
git remote add origin https://github.com/rihabamor/VOTRE-REPO-NAME.git

# Vérifier
git remote -v
```

**Remplacez `VOTRE-REPO-NAME` par le nom exact de votre repository GitHub.**

## 📤 Étape 4 : Pousser le Code

```powershell
# Ajouter tous les fichiers
git add .

# Commit
git commit -m "Add Docker, CI/CD workflows, tests and DevOps configuration"

# Pousser vers GitHub
git push -u origin main
```

Si vous avez une branche différente (ex: `master`) :

```powershell
# Vérifier votre branche
git branch

# Si vous êtes sur master, renommez ou poussez master
git push -u origin master
```

## 🔐 Étape 5 : Authentification GitHub

Si GitHub demande une authentification :

### Option A : Token Personnel (Recommandé)

1. Aller sur GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Générer un nouveau token avec les permissions `repo`
3. Utiliser le token comme mot de passe lors du push

### Option B : GitHub CLI

```powershell
# Installer GitHub CLI (si pas déjà installé)
# Puis :
gh auth login
```

## ✅ Vérification

Après le push, vérifiez sur GitHub :

1. Aller sur votre repository GitHub
2. Vérifier que tous les fichiers sont présents
3. Aller dans l'onglet **Actions**
4. Vous devriez voir les workflows

## 🐛 Dépannage

### Erreur : "fatal: not a git repository"

```powershell
# Initialiser Git
git init
```

### Erreur : "remote origin already exists"

```powershell
# Voir les remotes
git remote -v

# Supprimer l'ancien remote
git remote remove origin

# Ajouter le nouveau
git remote add origin https://github.com/rihabamor/VOTRE-REPO-NAME.git
```

### Erreur : "Permission denied"

- Vérifier que vous avez les droits sur le repository
- Utiliser un token personnel au lieu du mot de passe
- Vérifier l'URL du repository

### Erreur : "branch 'main' does not exist"

```powershell
# Créer la branche main
git branch -M main

# Ou utiliser master
git push -u origin master
```

## 📝 Checklist Complète

- [ ] Erreur Git corrigée (safe.directory)
- [ ] Git initialisé (`git init`)
- [ ] Remote GitHub configuré (`git remote add origin`)
- [ ] Fichiers ajoutés (`git add .`)
- [ ] Commit créé (`git commit`)
- [ ] Code poussé (`git push`)
- [ ] Vérifié sur GitHub
- [ ] Onglet Actions visible

## 🎯 Commandes Rapides (Copier-Coller)

```powershell
# Correction + Configuration complète
git config --global --add safe.directory "*"
git init
git add .
git commit -m "Add Docker, CI/CD workflows, tests and DevOps configuration"
git remote add origin https://github.com/rihabamor/VOTRE-REPO-NAME.git
git branch -M main
git push -u origin main
```

**N'oubliez pas de remplacer `VOTRE-REPO-NAME` !**

