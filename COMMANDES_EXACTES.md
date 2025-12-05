# 🔧 Commandes Exactes pour Corriger Git

## ❌ Erreur que vous avez

```
fatal: detected dubious ownership in repository at 'C:/'
```

## ✅ Solution - Commandes Exactes

### Option 1 : Exécuter le Script (Recommandé)

```powershell
.\fix-git-error.ps1
```

### Option 2 : Commandes Manuelles

**Commande 1 : Ajouter le répertoire actuel**
```powershell
git config --global --add safe.directory "C:/Users/pc/Desktop/rihebwchayma/rihebwchayma/back"
```

**Commande 2 : Autoriser tous les répertoires**
```powershell
git config --global --add safe.directory "*"
```

**⚠️ IMPORTANT :** 
- Utilisez des **GUILLEMETS** autour du chemin ou de l'astérisque
- L'astérisque `*` doit être entre guillemets : `"*"`

### Option 3 : Commande Unique (Copier-Coller)

```powershell
git config --global --add safe.directory "*"
```

## ✅ Vérification

Après avoir exécuté la commande, testez :

```powershell
git status
```

**Si ça fonctionne** : Vous verrez la liste des fichiers modifiés ou "nothing to commit"
**Si ça ne fonctionne pas** : Vous verrez encore l'erreur "dubious ownership"

## 🔍 Dépannage

### Si la commande ne fonctionne toujours pas :

1. **Vérifier que vous êtes dans le bon répertoire** :
```powershell
pwd
# Doit afficher : C:\Users\pc\Desktop\rihebwchayma\rihebwchayma\back
```

2. **Essayer avec le chemin complet** :
```powershell
git config --global --add safe.directory "C:/Users/pc/Desktop/rihebwchayma/rihebwchayma/back"
```

3. **Vérifier la configuration** :
```powershell
git config --global --get-all safe.directory
```

4. **Si rien ne fonctionne, réinitialiser Git** :
```powershell
git init
```

## 📝 Prochaines Étapes

Une fois Git corrigé :

1. ✅ Vérifier : `git status`
2. ✅ Ajouter les fichiers : `git add .`
3. ✅ Commit : `git commit -m "Initial commit"`
4. ✅ Ajouter remote : `git remote add origin https://github.com/rihabamor/VOTRE-REPO.git`
5. ✅ Push : `git push -u origin main`

