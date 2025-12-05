# Guide DevOps - Git, GitHub et Docker

## ✅ Intégration Complète Réussie !

J'ai intégré tous les outils DevOps nécessaires pour votre projet. Voici ce qui a été configuré :

## 📦 Fichiers Créés

### Docker
- ✅ `Dockerfile` - Image Docker pour l'application
- ✅ `docker-compose.yml` - Configuration complète (app + MySQL)
- ✅ `docker-compose.dev.yml` - Configuration pour développement
- ✅ `.dockerignore` - Fichiers à exclure du build Docker

### GitHub
- ✅ `.gitignore` - Amélioré avec exclusions Docker et secrets
- ✅ `.github/workflows/ci.yml` - Pipeline CI/CD automatique
- ✅ `.github/workflows/docker-build.yml` - Build Docker automatisé

### Documentation
- ✅ `DOCKER_GUIDE.md` - Guide complet Docker
- ✅ `GITHUB_GUIDE.md` - Guide complet GitHub
- ✅ `DEVOPS_GUIDE_FR.md` - Ce fichier (résumé en français)

## 🐳 Docker - Démarrage Rapide

### Option 1 : Tout en un (Recommandé)

```bash
# Lancer l'application + base de données
docker-compose up -d

# Voir les logs
docker-compose logs -f app

# Arrêter
docker-compose down
```

L'application sera accessible sur : **http://localhost:8080**

### Option 2 : Build manuel

```bash
# Construire l'image
docker build -t transporteur:latest .

# Lancer
docker run -d -p 8080:8080 transporteur:latest
```

## 🔄 GitHub - Configuration

### 1. Initialiser Git (si pas déjà fait)

```bash
git init
git add .
git commit -m "Initial commit avec Docker et CI/CD"
```

### 2. Connecter à GitHub

```bash
# Ajouter votre repository GitHub
git remote add origin https://github.com/votre-username/transporteur.git

# Pousser le code
git branch -M main
git push -u origin main
```

### 3. Configurer les Secrets GitHub

Pour activer le build Docker automatique :

1. Aller sur GitHub → Votre repository → `Settings` → `Secrets and variables` → `Actions`
2. Ajouter ces secrets :
   - `DOCKER_USERNAME` : Votre nom d'utilisateur Docker Hub
   - `DOCKER_PASSWORD` : Votre token Docker Hub

**Comment obtenir un token Docker Hub :**
- Aller sur [hub.docker.com](https://hub.docker.com/)
- `Account Settings` → `Security` → `New Access Token`
- Copier le token

## 🚀 GitHub Actions CI/CD

### Workflows Automatiques

Une fois configuré, à chaque push sur `main` ou `develop` :

1. ✅ **Tests** : Exécution automatique de tous les tests
2. ✅ **Build** : Compilation de l'application
3. ✅ **Docker** : Build et push de l'image Docker (sur `main`)

### Voir les Workflows

1. Aller sur votre repository GitHub
2. Cliquer sur l'onglet **Actions**
3. Voir l'historique des exécutions

## 📋 Commandes Essentielles

### Docker

```bash
# Démarrer
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter
docker-compose stop

# Redémarrer
docker-compose restart

# Reconstruire
docker-compose build --no-cache
docker-compose up -d

# Supprimer tout
docker-compose down -v
```

### Git

```bash
# Voir les changements
git status

# Ajouter les fichiers
git add .

# Commit
git commit -m "Description"

# Push vers GitHub
git push origin main

# Créer une branche
git checkout -b feature/nouvelle-fonctionnalite
```

## 🔧 Configuration

### Variables d'Environnement Docker

Modifiez `docker-compose.yml` pour changer :
- Ports
- Mots de passe
- Configuration de la base de données

### Workflows GitHub

Les workflows sont dans `.github/workflows/` :
- `ci.yml` : Pipeline principal
- `docker-build.yml` : Build Docker manuel

## ✅ Checklist d'Intégration

### Docker
- [x] Dockerfile créé
- [x] docker-compose.yml créé
- [x] .dockerignore créé
- [ ] Tester : `docker-compose up -d`
- [ ] Vérifier : http://localhost:8080

### GitHub
- [x] .gitignore amélioré
- [x] Workflows GitHub Actions créés
- [ ] Repository GitHub créé
- [ ] Code poussé vers GitHub
- [ ] Secrets GitHub configurés (Docker Hub)

### CI/CD
- [x] Pipeline de tests configuré
- [x] Build automatique configuré
- [x] Build Docker automatique configuré
- [ ] Tester : Push sur GitHub et vérifier Actions

## 🐛 Dépannage

### Docker ne démarre pas

```bash
# Vérifier les logs
docker-compose logs

# Vérifier que les ports sont libres
netstat -an | grep 8080

# Reconstruire
docker-compose build --no-cache
docker-compose up -d
```

### GitHub Actions ne fonctionne pas

1. Vérifier que les secrets sont configurés
2. Vérifier les logs dans l'onglet Actions
3. Vérifier que les workflows sont dans `.github/workflows/`

### Erreur de connexion à la base de données

```bash
# Vérifier que MySQL est démarré
docker-compose ps

# Attendre 30-60 secondes pour le démarrage complet
```

## 📚 Documentation Complète

Pour plus de détails, consultez :
- **DOCKER_GUIDE.md** : Guide complet Docker
- **GITHUB_GUIDE.md** : Guide complet GitHub et CI/CD

## 🎯 Prochaines Étapes

1. **Tester Docker localement** :
   ```bash
   docker-compose up -d
   ```

2. **Créer le repository GitHub** et pousser le code

3. **Configurer les secrets GitHub** pour Docker Hub

4. **Tester le CI/CD** en faisant un push

5. **Déployer en production** avec Docker

## 🚢 Déploiement Production

### Avec Docker Compose

```bash
# Sur le serveur
git clone https://github.com/votre-username/transporteur.git
cd transporteur
docker-compose up -d
```

### Avec Docker Hub

```bash
# Pull l'image depuis Docker Hub
docker pull votre-username/transporteur:latest

# Run
docker run -d -p 8080:8080 votre-username/transporteur:latest
```

## ✨ Résumé

Tous les outils DevOps sont maintenant intégrés :
- ✅ **Docker** : Containerisation complète
- ✅ **Git** : Gestion de version
- ✅ **GitHub** : Repository et collaboration
- ✅ **CI/CD** : Automatisation des tests et builds

Vous êtes prêt pour le développement et le déploiement ! 🚀

