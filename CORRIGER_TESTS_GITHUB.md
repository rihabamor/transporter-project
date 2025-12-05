# 🔧 Corriger les Tests qui Échouent sur GitHub Actions

## ❌ Problème Identifié

Le job **"Run Tests"** a échoué, ce qui a empêché les autres jobs de s'exécuter.

## 🔍 Étape 1 : Voir les Logs d'Erreur

### Sur GitHub :

1. **Cliquez sur le job "Run Tests"** (celui avec l'icône rouge ❌)
2. Vous verrez les étapes du job
3. **Cliquez sur l'étape qui a échoué** (généralement "Run tests")
4. **Regardez les logs** - les erreurs sont en rouge

## 🔍 Causes Possibles

### Cause 1 : Problème de Base de Données MySQL

**Symptôme** : Erreur de connexion à MySQL dans les tests

**Solution** : Vérifier que le workflow utilise H2 pour les tests, pas MySQL

### Cause 2 : Tests qui Échouent Vraiment

**Symptôme** : Un ou plusieurs tests échouent

**Solution** : Corriger les tests ou la configuration

### Cause 3 : Problème de Configuration Maven

**Symptôme** : Erreur de compilation ou de dépendances

**Solution** : Vérifier le pom.xml et les dépendances

### Cause 4 : Problème avec les Variables d'Environnement

**Symptôme** : Tests qui ne trouvent pas la configuration

**Solution** : Vérifier application-test.properties

## 🔧 Solutions

### Solution 1 : Vérifier le Workflow CI/CD

Le workflow doit utiliser H2 pour les tests, pas MySQL. Vérifions le fichier :

**Fichier** : `.github/workflows/ci.yml`

Assurez-vous que la configuration de la base de données pour les tests est correcte.

### Solution 2 : Vérifier les Tests Localement

Avant de corriger sur GitHub, testez localement :

```powershell
# Exécuter les tests localement
mvn clean test
```

Si les tests échouent localement aussi, corrigez-les d'abord.

### Solution 3 : Vérifier application-test.properties

Le fichier `src/test/resources/application-test.properties` doit être correct :

```properties
# Base de données H2 en mémoire pour les tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

## 📋 Checklist de Diagnostic

1. [ ] Cliquer sur "Run Tests" dans GitHub Actions
2. [ ] Voir les logs d'erreur
3. [ ] Identifier l'erreur exacte
4. [ ] Tester localement : `mvn clean test`
5. [ ] Corriger l'erreur
6. [ ] Pousser les corrections
7. [ ] Vérifier que le workflow passe

## 🎯 Actions Immédiates

### 1. Voir l'Erreur Exacte

Sur GitHub Actions :
- Cliquez sur "Run Tests"
- Cliquez sur l'étape qui a échoué
- **Copiez l'erreur** (les lignes en rouge)

### 2. Tester Localement

```powershell
# Dans votre terminal
mvn clean test
```

### 3. Partager l'Erreur

Une fois que vous avez l'erreur exacte, partagez-la avec moi et je vous aiderai à la corriger.

## 💡 Erreurs Communes

### Erreur : "Cannot connect to MySQL"

**Solution** : Le workflow doit utiliser H2, pas MySQL. Vérifier `ci.yml`.

### Erreur : "Test failed"

**Solution** : Voir quel test échoue et le corriger.

### Erreur : "Class not found"

**Solution** : Vérifier que toutes les dépendances sont dans `pom.xml`.

## 🔄 Après Correction

Une fois corrigé :

1. **Commit les corrections** :
```powershell
git add .
git commit -m "Fix tests for CI/CD"
git push origin main
```

2. **Vérifier sur GitHub Actions** :
   - Le workflow se déclenchera automatiquement
   - Vérifiez que "Run Tests" passe maintenant (✅ vert)

## 📝 Prochaines Étapes

1. **Cliquez sur "Run Tests"** dans GitHub Actions
2. **Voyez l'erreur exacte** dans les logs
3. **Partagez l'erreur** avec moi
4. **Je vous aiderai à la corriger**

---

**Important** : Sans voir l'erreur exacte dans les logs, je ne peux pas identifier le problème précis. Cliquez sur "Run Tests" et copiez l'erreur que vous voyez !

