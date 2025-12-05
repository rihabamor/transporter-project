# Guide Docker - Application Transporteur

Ce guide explique comment utiliser Docker pour déployer l'application Transporteur.

## 📋 Prérequis

- Docker installé (version 20.10 ou supérieure)
- Docker Compose installé (version 2.0 ou supérieure)

Vérifier l'installation :
```bash
docker --version
docker-compose --version
```

## 🚀 Démarrage Rapide

### Option 1 : Docker Compose (Recommandé)

Cette méthode lance l'application ET la base de données MySQL automatiquement.

```bash
# Construire et démarrer tous les services
docker-compose up -d

# Voir les logs
docker-compose logs -f app

# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes (⚠️ supprime les données)
docker-compose down -v
```

L'application sera accessible sur : `http://localhost:8080`

### Option 2 : Docker uniquement (sans Compose)

Si vous avez déjà une base de données MySQL :

```bash
# Construire l'image
docker build -t transporteur:latest .

# Lancer le conteneur
docker run -d \
  --name transporteur-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/the_transporter \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=yourpassword \
  transporteur:latest

# Voir les logs
docker logs -f transporteur-app
```

## 🛠️ Commandes Utiles

### Docker Compose

```bash
# Démarrer en arrière-plan
docker-compose up -d

# Démarrer et voir les logs
docker-compose up

# Arrêter
docker-compose stop

# Redémarrer
docker-compose restart

# Voir les logs
docker-compose logs -f app
docker-compose logs -f mysql

# Voir le statut
docker-compose ps

# Reconstruire l'image
docker-compose build --no-cache

# Supprimer tout
docker-compose down -v
```

### Docker

```bash
# Construire l'image
docker build -t transporteur:latest .

# Lister les images
docker images

# Lancer un conteneur
docker run -d -p 8080:8080 --name transporteur-app transporteur:latest

# Voir les logs
docker logs -f transporteur-app

# Arrêter
docker stop transporteur-app

# Supprimer
docker rm transporteur-app

# Entrer dans le conteneur
docker exec -it transporteur-app sh
```

## 🔧 Configuration

### Variables d'Environnement

Vous pouvez personnaliser la configuration via des variables d'environnement :

```bash
# Créer un fichier .env
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/the_transporter
SPRING_DATASOURCE_USERNAME=transporteur
SPRING_DATASOURCE_PASSWORD=transporteur123
JWT_SECRET=MySuperSecretKeyForJWTGeneration1234567890
JWT_EXPIRATION=86400000
```

Puis lancer :
```bash
docker-compose --env-file .env up
```

### Modifier docker-compose.yml

Pour changer le port, la base de données, etc., éditez `docker-compose.yml`.

## 🗄️ Base de Données

### Accéder à MySQL

```bash
# Via Docker Compose
docker-compose exec mysql mysql -u transporteur -p transporteur123 the_transporter

# Ou directement
docker exec -it transporteur-mysql mysql -u transporteur -p transporteur123 the_transporter
```

### Sauvegarder la base de données

```bash
docker-compose exec mysql mysqldump -u transporteur -ptransporteur123 the_transporter > backup.sql
```

### Restaurer la base de données

```bash
docker-compose exec -T mysql mysql -u transporteur -ptransporteur123 the_transporter < backup.sql
```

## 🐛 Dépannage

### L'application ne démarre pas

1. Vérifier les logs :
   ```bash
   docker-compose logs app
   ```

2. Vérifier que MySQL est prêt :
   ```bash
   docker-compose ps
   ```

3. Vérifier les ports :
   ```bash
   netstat -an | grep 8080
   ```

### Erreur de connexion à la base de données

1. Vérifier que MySQL est démarré :
   ```bash
   docker-compose ps mysql
   ```

2. Vérifier les variables d'environnement dans `docker-compose.yml`

3. Attendre que MySQL soit complètement démarré (peut prendre 30-60 secondes)

### Reconstruire complètement

```bash
# Arrêter et supprimer
docker-compose down -v

# Supprimer l'image
docker rmi transporteur:latest

# Reconstruire
docker-compose build --no-cache
docker-compose up -d
```

## 📊 Monitoring

### Health Check

L'application expose un endpoint de santé :
```bash
curl http://localhost:8080/actuator/health
```

### Voir les ressources utilisées

```bash
docker stats transporteur-app transporteur-mysql
```

## 🔒 Sécurité

### Production

Pour la production, modifiez :
1. Les mots de passe dans `docker-compose.yml`
2. Utilisez des secrets Docker ou des variables d'environnement
3. Configurez un reverse proxy (nginx)
4. Activez HTTPS

### Exemple pour production

```yaml
environment:
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
  JWT_SECRET: ${JWT_SECRET}
```

## 🚢 Déploiement

### Docker Hub

```bash
# Tag l'image
docker tag transporteur:latest votre-username/transporteur:latest

# Push vers Docker Hub
docker push votre-username/transporteur:latest
```

### Pull et run depuis Docker Hub

```bash
docker pull votre-username/transporteur:latest
docker run -d -p 8080:8080 votre-username/transporteur:latest
```

## 📝 Notes

- Les données MySQL sont persistées dans un volume Docker
- L'application redémarre automatiquement en cas d'erreur
- Les health checks vérifient l'état des services

## 🔗 Liens Utiles

- [Documentation Docker](https://docs.docker.com/)
- [Documentation Docker Compose](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)

