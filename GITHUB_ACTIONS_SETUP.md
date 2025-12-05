# Configuration GitHub Actions - Guide Étape par Étape

## ✅ Étape 1 : Vérifier que le code est sur GitHub

### Si vous n'avez pas encore poussé le code :

```bash
# Vérifier l'état Git
git status

# Ajouter tous les fichiers
git add .

# Commit
git commit -m "Add Docker, CI/CD workflows and tests"

# Vérifier le remote
git remote -v

# Si pas de remote, ajouter votre repository GitHub
git remote add origin https://github.com/rihabamor/votre-repo-name.git

# Pousser vers GitHub
git push -u origin main
```

## 🔐 Étape 2 : Configurer les Secrets GitHub

### Pour activer le build Docker automatique :

1. **Aller dans votre repository GitHub**
   - Cliquez sur **Settings** (en haut à droite du repository)

2. **Aller dans Secrets**
   - Dans le menu de gauche, cliquez sur **Secrets and variables**
   - Puis cliquez sur **Actions**

3. **Ajouter les secrets**
   - Cliquez sur **New repository secret**

   **Secret 1 : DOCKER_USERNAME**
   - Name: `DOCKER_USERNAME`
   - Secret: Votre nom d'utilisateur Docker Hub
   - Cliquez sur **Add secret**

   **Secret 2 : DOCKER_PASSWORD**
   - Name: `DOCKER_PASSWORD`
   - Secret: Votre token Docker Hub (voir ci-dessous)
   - Cliquez sur **Add secret**

### Comment obtenir un token Docker Hub :

1. Aller sur [hub.docker.com](https://hub.docker.com/)
2. Se connecter avec votre compte
3. Cliquer sur votre profil (en haut à droite)
4. Aller dans **Account Settings**
5. Cliquer sur **Security** dans le menu de gauche
6. Cliquer sur **New Access Token**
7. Donner un nom (ex: "GitHub Actions")
8. Copier le token généré
9. **⚠️ Important** : Le token ne sera affiché qu'une seule fois, copiez-le immédiatement !

## 🚀 Étape 3 : Déclencher les Workflows

### Option A : Push automatique (Recommandé)

Les workflows se déclenchent automatiquement quand vous :
- Faites un push sur la branche `main` ou `develop`
- Créez une Pull Request vers `main` ou `develop`

```bash
# Faire un changement et pousser
git add .
git commit -m "Test CI/CD"
git push origin main
```

### Option B : Déclenchement manuel

1. Aller dans l'onglet **Actions**
2. Cliquer sur le workflow "Docker Build and Push"
3. Cliquer sur **Run workflow** (bouton en haut à droite)
4. Sélectionner la branche
5. Cliquer sur **Run workflow**

## 📊 Étape 4 : Voir les Résultats

### Dans l'onglet Actions :

1. **Voir l'historique**
   - Tous les workflows exécutés apparaissent dans la liste
   - Cliquez sur un workflow pour voir les détails

2. **Voir les logs**
   - Cliquez sur un workflow
   - Cliquez sur un job (ex: "Run Tests")
   - Voir les logs étape par étape

3. **Vérifier les résultats**
   - ✅ Vert = Succès
   - ❌ Rouge = Échec
   - 🟡 Jaune = En cours

## 🔍 Étape 5 : Vérifier que tout fonctionne

### Checklist :

- [ ] Code poussé vers GitHub
- [ ] Secrets GitHub configurés (DOCKER_USERNAME, DOCKER_PASSWORD)
- [ ] Workflows visibles dans l'onglet Actions
- [ ] Workflow "CI/CD Pipeline" s'exécute automatiquement
- [ ] Tests passent (job "Run Tests" = ✅)
- [ ] Build réussit (job "Build Application" = ✅)
- [ ] Image Docker buildée (job "Build Docker Image" = ✅)

## 🐛 Dépannage

### Le workflow ne se déclenche pas :

1. Vérifier que les fichiers sont dans `.github/workflows/`
2. Vérifier la syntaxe YAML (pas d'erreurs)
3. Faire un nouveau push

### Erreur "DOCKER_USERNAME not found" :

1. Vérifier que les secrets sont bien configurés
2. Vérifier l'orthographe exacte : `DOCKER_USERNAME` et `DOCKER_PASSWORD`
3. Les secrets sont sensibles à la casse !

### Les tests échouent :

1. Vérifier les logs dans l'onglet Actions
2. Vérifier que MySQL est bien configuré dans le workflow
3. Vérifier que les dépendances Maven sont correctes

### Le build Docker échoue :

1. Vérifier que les secrets Docker Hub sont corrects
2. Vérifier que le Dockerfile est présent
3. Vérifier les logs pour voir l'erreur exacte

## 📝 Prochaines Étapes

Une fois que tout fonctionne :

1. **Créer une Pull Request** pour tester le workflow
2. **Voir l'image Docker** sur Docker Hub (si configuré)
3. **Automatiser le déploiement** (optionnel)

## 🎯 Résumé Rapide

1. ✅ Pousser le code vers GitHub
2. ✅ Configurer les secrets (DOCKER_USERNAME, DOCKER_PASSWORD)
3. ✅ Faire un push ou déclencher manuellement
4. ✅ Vérifier les résultats dans l'onglet Actions

Voilà ! Votre CI/CD est maintenant configuré ! 🚀

