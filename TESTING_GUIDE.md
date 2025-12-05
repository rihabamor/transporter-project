# Guide des Tests Unitaires et d'Intégration

Ce document explique comment exécuter les tests unitaires et d'intégration du projet.

## 📋 Structure des Tests

Le projet contient deux types de tests :

### Tests Unitaires
- **Services** : `src/test/java/com/transporteur/service/`
  - `AuthServiceTest.java`
  - `MissionServiceTest.java`
  - `PaymentServiceTest.java`
  - `TransporteurServiceTest.java`
- **Security** : `src/test/java/com/transporteur/security/`
  - `JwtUtilTest.java`
- **Controllers** : `src/test/java/com/transporteur/controller/`
  - `AuthControllerTest.java`
  - `MissionControllerTest.java`
  - `PaymentControllerTest.java`

### Tests d'Intégration
- **Integration Tests** : `src/test/java/com/transporteur/integration/`
  - `AuthIntegrationTest.java`
  - `MissionIntegrationTest.java`

## 🚀 Commandes pour Exécuter les Tests

### Exécuter tous les tests
```bash
mvn test
```

### Exécuter uniquement les tests unitaires
```bash
mvn test -Dtest="*Test"
```

### Exécuter uniquement les tests d'intégration
```bash
mvn test -Dtest="*IntegrationTest"
```

### Exécuter un test spécifique
```bash
# Test unitaire spécifique
mvn test -Dtest="AuthServiceTest"

# Test d'intégration spécifique
mvn test -Dtest="AuthIntegrationTest"
```

### Exécuter les tests avec affichage détaillé
```bash
mvn test -X
```

### Exécuter les tests et générer un rapport
```bash
mvn test surefire-report:report
```

### Exécuter les tests en ignorant les échecs (pour voir tous les résultats)
```bash
mvn test -Dmaven.test.failure.ignore=true
```

## 📊 Vérifier que les Tests sont Intégrés Correctement

### 1. Vérifier la compilation
```bash
mvn clean compile test-compile
```

### 2. Lister tous les tests disponibles
```bash
mvn test -Dtest="*Test" -DfailIfNoTests=false
```

### 3. Exécuter les tests et vérifier le rapport
Après l'exécution, consultez le rapport dans :
```
target/surefire-reports/
```

## 🔧 Configuration des Tests

### Fichier de configuration de test
Les tests utilisent une base de données H2 en mémoire configurée dans :
```
src/test/resources/application-test.properties
```

### Dépendances de test
Les dépendances suivantes sont configurées dans `pom.xml` :
- `spring-boot-starter-test` : Contient JUnit 5, Mockito, AssertJ
- `h2` : Base de données en mémoire pour les tests

## 📝 Exemples de Tests

### Test Unitaire (Service)
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private CompteRepository compteRepository;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    void testRegister_Success() {
        // Test implementation
    }
}
```

### Test d'Intégration
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testRegister_Client_Success() throws Exception {
        // Test implementation
    }
}
```

## ✅ Vérification de l'Intégration

Pour vérifier que les tests sont bien intégrés :

1. **Vérifier la compilation** :
   ```bash
   mvn clean compile test-compile
   ```

2. **Exécuter tous les tests** :
   ```bash
   mvn test
   ```

3. **Vérifier le rapport** :
   - Ouvrir `target/surefire-reports/index.html` dans un navigateur
   - Vérifier que tous les tests passent

4. **Vérifier la couverture** (optionnel) :
   ```bash
   mvn test jacoco:report
   ```

## 🐛 Dépannage

### Problème : Tests ne se lancent pas
- Vérifier que Maven est installé : `mvn --version`
- Nettoyer le projet : `mvn clean`
- Recompiler : `mvn compile test-compile`

### Problème : Erreurs de dépendances
- Télécharger les dépendances : `mvn dependency:resolve`
- Nettoyer le cache Maven : `mvn clean install -U`

### Problème : Base de données H2
- Vérifier que la dépendance H2 est dans `pom.xml`
- Vérifier `application-test.properties`

## 📈 Statistiques des Tests

Après exécution, vous pouvez voir :
- Nombre de tests exécutés
- Nombre de tests réussis
- Nombre de tests échoués
- Temps d'exécution

Dans le rapport Maven ou dans la console.

## 🎯 Prochaines Étapes

1. Exécuter tous les tests : `mvn test`
2. Vérifier les résultats dans `target/surefire-reports/`
3. Corriger les éventuels échecs
4. Ajouter plus de tests si nécessaire

