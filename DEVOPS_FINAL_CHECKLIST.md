# ✅ Checklist DevOps - Finalisation Complète

## 📊 État des Lieux

### ✅ CE QUI EST DÉJÀ FAIT

- [x] **Docker**
  - [x] Dockerfile créé
  - [x] docker-compose.yml créé
  - [x] docker-compose.dev.yml créé
  - [x] .dockerignore créé

- [x] **Git/GitHub**
  - [x] .gitignore amélioré
  - [x] Workflows GitHub Actions créés
    - [x] ci.yml (Pipeline CI/CD)
    - [x] docker-build.yml (Build Docker)

- [x] **Documentation**
  - [x] DOCKER_GUIDE.md
  - [x] GITHUB_GUIDE.md
  - [x] DEVOPS_GUIDE_FR.md
  - [x] README.md mis à jour

### ❌ CE QUI RESTE À FAIRE

- [ ] Corriger l'erreur Git (safe.directory)
- [ ] Pousser le code vers GitHub
- [ ] Configurer les secrets GitHub (Docker Hub)
- [ ] Tester Docker localement
- [ ] Vérifier les workflows GitHub Actions
- [ ] Tester le déploiement

---

## 🚀 GUIDE ÉTAPE PAR ÉTAPE

### ÉTAPE 1 : Corriger l'Erreur Git ⚠️

**Problème** : Erreur "dubious ownership"

**Solution** :

```powershell
# Dans PowerShell, exécutez :
git config --global --add safe.directory "*"
```

**Vérification** :
```powershell
git status
```
✅ Si ça fonctionne sans erreur, passez à l'étape 2.

---

### ÉTAPE 2 : Initialiser Git (si pas déjà fait)

```powershell
# Vérifier si Git est initialisé
git status

# Si erreur "not a git repository", alors :
git init
```

---

### ÉTAPE 3 : Configurer le Remote GitHub

**A. Trouver le nom de votre repository GitHub**

Regardez dans votre liste de repositories sur GitHub (ex: `transporter-project`, `transporter-backend`, etc.)

**B. Ajouter le remote**

```powershell
# Remplacez VOTRE-REPO-NAME par le nom exact
git remote add origin https://github.com/rihabamor/VOTRE-REPO-NAME.git

# Vérifier
git remote -v
```

**Si le remote existe déjà** :
```powershell
# Voir les remotes
git remote -v

# Si besoin, supprimer et réajouter
git remote remove origin
git remote add origin https://github.com/rihabamor/VOTRE-REPO-NAME.git
```

---

### ÉTAPE 4 : Pousser le Code vers GitHub

```powershell
# 1. Ajouter tous les fichiers
git add .

# 2. Créer un commit
git commit -m "Add Docker, CI/CD workflows, tests and DevOps configuration"

# 3. Créer/renommer la branche main
git branch -M main

# 4. Pousser vers GitHub
git push -u origin main
```

**Si GitHub demande une authentification** :
- Utilisez un **Personal Access Token** (pas votre mot de passe)
- Créer un token : GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token
- Permissions : cocher `repo`
- Utiliser ce token comme mot de passe

---

### ÉTAPE 5 : Vérifier sur GitHub

1. Aller sur votre repository GitHub
2. Vérifier que tous les fichiers sont présents :
   - ✅ Dockerfile
   - ✅ docker-compose.yml
   - ✅ .github/workflows/ci.yml
   - ✅ .github/workflows/docker-build.yml
   - ✅ Tous les autres fichiers

3. Aller dans l'onglet **Actions**
   - Vous devriez voir les workflows
   - Si un workflow s'est déclenché automatiquement, c'est bon signe !

---

### ÉTAPE 6 : Configurer les Secrets GitHub (Optionnel mais Recommandé)

**Pour activer le build Docker automatique** :

1. **Dans votre repository GitHub** :
   - Cliquez sur **Settings** (dernier onglet)
   - Dans le menu de gauche : **Secrets and variables** → **Actions**
   - Ou utilisez l'URL directe : `https://github.com/rihabamor/VOTRE-REPO-NAME/settings/secrets/actions`

2. **Ajouter 2 secrets** :

   **Secret 1 : DOCKER_USERNAME**
   - Cliquez sur **New repository secret**
   - Name: `DOCKER_USERNAME`
   - Secret: Votre nom d'utilisateur Docker Hub
   - Cliquez sur **Add secret**

   **Secret 2 : DOCKER_PASSWORD**
   - Cliquez sur **New repository secret**
   - Name: `DOCKER_PASSWORD`
   - Secret: Votre token Docker Hub

3. **Comment obtenir le token Docker Hub** :
   - Aller sur [hub.docker.com](https://hub.docker.com/)
   - Se connecter
   - Profil → **Account Settings** → **Security**
   - **New Access Token**
   - Donner un nom (ex: "GitHub Actions")
   - Copier le token (affiché une seule fois !)

**Note** : Si vous ne configurez pas les secrets, les workflows fonctionneront mais le build Docker échouera. Les tests et le build Maven fonctionneront quand même.

---

### ÉTAPE 7 : Tester Docker Localement 🐳

**A. Tester le build Docker**

```powershell
# Construire l'image
docker build -t transporteur:latest .
```

✅ Si ça fonctionne sans erreur, continuez.

**B. Tester Docker Compose**

```powershell
# Démarrer l'application + MySQL
docker-compose up -d

# Voir les logs
docker-compose logs -f app

# Vérifier que l'application démarre
# Attendre 30-60 secondes, puis ouvrir : http://localhost:8080
```

**Vérification** :
- Ouvrir un navigateur : `http://localhost:8080`
- Ou tester : `curl http://localhost:8080/actuator/health`

**Arrêter** :
```powershell
docker-compose down
```

✅ Si l'application démarre correctement, Docker fonctionne !

---

### ÉTAPE 8 : Vérifier les Workflows GitHub Actions

**A. Déclencher un workflow**

**Option 1 : Automatique**
- Faire un nouveau commit et push :
```powershell
git add .
git commit -m "Test CI/CD"
git push origin main
```

**Option 2 : Manuel**
- Aller dans l'onglet **Actions** sur GitHub
- Cliquer sur "CI/CD Pipeline"
- Cliquer sur **Run workflow** (bouton en haut à droite)
- Sélectionner la branche `main`
- Cliquer sur **Run workflow**

**B. Vérifier les résultats**

1. Dans l'onglet **Actions**, vous verrez le workflow s'exécuter
2. Cliquez sur le workflow pour voir les détails
3. Vérifiez les jobs :
   - ✅ **Run Tests** : Doit être vert (succès)
   - ✅ **Build Application** : Doit être vert (succès)
   - ⚠️ **Build Docker Image** : Peut échouer si secrets non configurés (normal)

**Si les tests échouent** :
- Cliquez sur le job pour voir les logs
- Vérifiez les erreurs dans les logs
- Corrigez et recommencez

---

### ÉTAPE 9 : Vérification Finale ✅

**Checklist complète** :

- [ ] Erreur Git corrigée
- [ ] Code poussé vers GitHub
- [ ] Tous les fichiers présents sur GitHub
- [ ] Onglet Actions visible
- [ ] Docker fonctionne localement
- [ ] Workflows GitHub Actions fonctionnent
- [ ] Tests passent dans GitHub Actions
- [ ] Build réussit dans GitHub Actions
- [ ] Secrets GitHub configurés (optionnel)
- [ ] Image Docker buildée (si secrets configurés)

---

## 🎯 RÉSUMÉ DES COMMANDES (Copier-Coller)

```powershell
# ÉTAPE 1 : Corriger Git
git config --global --add safe.directory "*"

# ÉTAPE 2 : Vérifier Git
git status

# ÉTAPE 3 : Initialiser (si besoin)
git init

# ÉTAPE 4 : Ajouter remote (remplacez VOTRE-REPO-NAME)
git remote add origin https://github.com/rihabamor/VOTRE-REPO-NAME.git

# ÉTAPE 5 : Pousser le code
git add .
git commit -m "Add Docker, CI/CD workflows, tests and DevOps configuration"
git branch -M main
git push -u origin main

# ÉTAPE 6 : Tester Docker
docker build -t transporteur:latest .
docker-compose up -d
# Attendre, puis ouvrir http://localhost:8080
docker-compose down
```

---

## 🐛 DÉPANNAGE RAPIDE

### Erreur Git "dubious ownership"
```powershell
git config --global --add safe.directory "*"
```

### Erreur "remote origin already exists"
```powershell
git remote remove origin
git remote add origin https://github.com/rihabamor/VOTRE-REPO-NAME.git
```

### Erreur "Permission denied" lors du push
- Utiliser un Personal Access Token au lieu du mot de passe
- Vérifier que vous avez les droits sur le repository

### Docker ne démarre pas
```powershell
# Vérifier les logs
docker-compose logs

# Vérifier les ports
netstat -an | findstr 8080

# Reconstruire
docker-compose build --no-cache
docker-compose up -d
```

### Workflows GitHub Actions ne se déclenchent pas
- Vérifier que les fichiers sont dans `.github/workflows/`
- Faire un nouveau push
- Vérifier la syntaxe YAML

---

## 📚 DOCUMENTATION

Consultez ces guides pour plus de détails :

- **DOCKER_GUIDE.md** - Guide complet Docker
- **GITHUB_GUIDE.md** - Guide complet GitHub
- **GITHUB_ACTIONS_SETUP.md** - Configuration GitHub Actions
- **PUSH_TO_GITHUB.md** - Guide pour pousser le code

---

## ✅ FINALISATION

Une fois toutes les étapes complétées :

1. ✅ Votre code est sur GitHub
2. ✅ Docker fonctionne localement
3. ✅ CI/CD est configuré et fonctionne
4. ✅ Les tests s'exécutent automatiquement
5. ✅ L'application peut être déployée avec Docker

**🎉 Félicitations ! Votre pipeline DevOps est complet !**

