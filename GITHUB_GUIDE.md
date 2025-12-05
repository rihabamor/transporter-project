# Guide GitHub - CI/CD et Workflows

Ce guide explique comment utiliser GitHub pour la gestion du code source et l'automatisation CI/CD.

## 📋 Prérequis

- Compte GitHub
- Git installé localement
- Accès au repository GitHub

## 🔧 Configuration Initiale

### 1. Initialiser Git (si pas déjà fait)

```bash
git init
git add .
git commit -m "Initial commit"
```

### 2. Connecter au Repository GitHub

```bash
# Ajouter le remote
git remote add origin https://github.com/votre-username/transporteur.git

# Ou avec SSH
git remote add origin git@github.com:votre-username/transporteur.git

# Vérifier
git remote -v
```

### 3. Premier Push

```bash
git branch -M main
git push -u origin main
```

## 🔄 Workflow Git Standard

### Branches Recommandées

- `main` : Code de production
- `develop` : Code de développement
- `feature/*` : Nouvelles fonctionnalités
- `hotfix/*` : Corrections urgentes
- `release/*` : Préparation de release

### Commandes Git Essentielles

```bash
# Créer une nouvelle branche
git checkout -b feature/nouvelle-fonctionnalite

# Ajouter des fichiers
git add .

# Commit
git commit -m "Description des changements"

# Push vers GitHub
git push origin feature/nouvelle-fonctionnalite

# Créer une Pull Request sur GitHub
# (via l'interface web GitHub)

# Revenir sur main
git checkout main

# Mettre à jour depuis GitHub
git pull origin main
```

## 🚀 GitHub Actions CI/CD

### Workflows Configurés

Le projet contient deux workflows GitHub Actions :

#### 1. `ci.yml` - Pipeline CI/CD Complet

**Déclencheurs :**
- Push sur `main` ou `develop`
- Pull Request vers `main` ou `develop`

**Jobs :**
1. **Test** : Exécute tous les tests unitaires et d'intégration
2. **Build** : Compile l'application
3. **Docker Build** : Construit et pousse l'image Docker (uniquement sur `main`)

**Fichier :** `.github/workflows/ci.yml`

#### 2. `docker-build.yml` - Build Docker Manuel

**Déclencheurs :**
- Push de tags (v*)
- Déclenchement manuel

**Job :**
- Build et push de l'image Docker avec différents tags

**Fichier :** `.github/workflows/docker-build.yml`

### Configuration des Secrets GitHub

Pour utiliser les workflows Docker, configurez ces secrets dans GitHub :

1. Aller dans : `Settings` → `Secrets and variables` → `Actions`
2. Ajouter les secrets suivants :

```
DOCKER_USERNAME=votre-username-dockerhub
DOCKER_PASSWORD=votre-token-dockerhub
```

**Comment obtenir un token Docker Hub :**
1. Aller sur [Docker Hub](https://hub.docker.com/)
2. `Account Settings` → `Security` → `New Access Token`
3. Copier le token et l'ajouter comme secret `DOCKER_PASSWORD`

### Voir les Workflows en Action

1. Aller sur votre repository GitHub
2. Cliquer sur l'onglet `Actions`
3. Voir l'historique des exécutions
4. Cliquer sur une exécution pour voir les détails

## 📝 Bonnes Pratiques Git

### Messages de Commit

Utilisez des messages clairs et descriptifs :

```bash
# ✅ Bon
git commit -m "feat: Ajouter l'authentification JWT"
git commit -m "fix: Corriger le bug de connexion"
git commit -m "test: Ajouter des tests pour PaymentService"

# ❌ Éviter
git commit -m "fix"
git commit -m "update"
```

### Convention de Nommage

**Branches :**
- `feature/authentification-jwt`
- `bugfix/correction-paiement`
- `hotfix/erreur-critique`
- `release/v1.0.0`

**Tags :**
- `v1.0.0`
- `v1.1.0`
- `v2.0.0`

### Pull Requests

1. Créer une branche pour votre fonctionnalité
2. Pousser la branche vers GitHub
3. Créer une Pull Request sur GitHub
4. Ajouter une description claire
5. Demander une review
6. Une fois approuvée, merger dans `main` ou `develop`

## 🔍 Vérifier l'État du Repository

```bash
# Voir les changements
git status

# Voir l'historique
git log --oneline

# Voir les branches
git branch -a

# Voir les remotes
git remote -v
```

## 🐛 Dépannage

### Conflits de Merge

```bash
# Récupérer les dernières modifications
git fetch origin
git merge origin/main

# Résoudre les conflits dans les fichiers
# Puis :
git add .
git commit -m "Résolution des conflits"
```

### Annuler un Commit Local

```bash
# Annuler le dernier commit (garder les changements)
git reset --soft HEAD~1

# Annuler le dernier commit (supprimer les changements)
git reset --hard HEAD~1
```

### Forcer un Push (⚠️ Attention)

```bash
# Seulement si vous êtes sûr
git push --force origin main
```

## 📊 GitHub Features Utiles

### Issues

Utilisez les Issues pour :
- Tracker les bugs
- Proposer des fonctionnalités
- Discuter des améliorations

### Projects

Organisez votre travail avec les GitHub Projects (Kanban board).

### Releases

Créer des releases pour marquer les versions :

```bash
# Créer un tag
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin v1.0.0

# Sur GitHub, créer une release depuis le tag
```

## 🔐 Sécurité

### .gitignore

Assurez-vous que `.gitignore` contient :
- Fichiers de configuration sensibles
- Mots de passe
- Clés API
- Fichiers de build

### Secrets

**Ne jamais commiter :**
- Mots de passe
- Clés API
- Tokens JWT secrets
- Certificats

Utilisez les GitHub Secrets pour les workflows.

## 🚢 Déploiement Automatique

### Avec GitHub Actions

Les workflows peuvent être étendus pour :
- Déployer automatiquement après les tests
- Notifier l'équipe
- Créer des releases automatiques

### Exemple d'Extension

Ajouter dans `ci.yml` :

```yaml
deploy:
  name: Deploy to Production
  runs-on: ubuntu-latest
  needs: [test, build, docker-build]
  if: github.ref == 'refs/heads/main'
  steps:
    - name: Deploy
      run: |
        # Vos commandes de déploiement
```

## 📚 Ressources

- [Git Documentation](https://git-scm.com/doc)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Git Flow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)
- [Conventional Commits](https://www.conventionalcommits.org/)

## ✅ Checklist

- [ ] Repository GitHub créé
- [ ] Code poussé vers GitHub
- [ ] Secrets GitHub configurés (Docker Hub)
- [ ] Workflows GitHub Actions fonctionnels
- [ ] Branches configurées (main, develop)
- [ ] .gitignore à jour
- [ ] README.md à jour

