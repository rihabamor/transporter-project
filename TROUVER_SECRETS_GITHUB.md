# Comment Trouver les Secrets GitHub - Guide Visuel

## 🔍 Méthode 1 : Via Settings (Standard)

### Étapes détaillées :

1. **Dans votre repository GitHub**
   - Assurez-vous d'être sur la page principale de votre repository
   - En haut de la page, vous verrez une barre d'onglets : `Code | Issues | Pull requests | Actions | Projects | Wiki | Security | Insights | Settings`

2. **Cliquer sur "Settings"**
   - C'est le dernier onglet à droite (ou avant "Settings" s'il y a d'autres onglets)

3. **Dans le menu de gauche de Settings**
   - Faites défiler vers le bas
   - Cherchez la section **"Security"** ou **"Secrets and variables"**
   - Cliquez sur **"Secrets and variables"**
   - Puis cliquez sur **"Actions"**

## 🔍 Méthode 2 : Accès Direct via URL

Si vous ne trouvez pas dans le menu, utilisez cette URL :

```
https://github.com/rihabamor/VOTRE-REPO-NAME/settings/secrets/actions
```

Remplacez `VOTRE-REPO-NAME` par le nom exact de votre repository.

## 🔍 Méthode 3 : Via Security Tab

1. Dans votre repository, cliquez sur l'onglet **"Security"** (dans la barre d'onglets)
2. Cherchez une section **"Secrets"** ou **"Secrets and variables"**
3. Cliquez dessus

## ⚠️ Si vous ne voyez toujours pas "Secrets and variables"

### Raisons possibles :

1. **Vous n'avez pas les permissions d'admin**
   - Seuls les propriétaires et administrateurs du repository peuvent voir/gérer les secrets
   - Vérifiez vos permissions : Settings → Collaborators

2. **Vous êtes sur une organisation**
   - Les secrets peuvent être au niveau de l'organisation
   - Allez dans l'organisation → Settings → Secrets → Actions

3. **L'interface a changé**
   - GitHub met parfois à jour l'interface
   - Cherchez "Secrets" dans la barre de recherche de Settings

## 🔍 Méthode 4 : Recherche dans Settings

1. Allez dans **Settings**
2. Utilisez la barre de recherche en haut de la page Settings
3. Tapez : **"secrets"** ou **"actions secrets"**
4. Cliquez sur le résultat

## 📸 Emplacement Visuel

```
Repository GitHub
│
├── Code
├── Issues
├── Pull requests
├── Actions
├── Projects
├── Wiki
├── Security
├── Insights
└── Settings  ← Cliquez ici
    │
    ├── General
    ├── Access
    ├── Secrets and variables  ← Cherchez ici
    │   └── Actions  ← Puis ici
    ├── Actions
    └── ...
```

## ✅ Alternative : Créer les Secrets via l'Interface Actions

Si vous ne trouvez toujours pas, vous pouvez aussi :

1. Aller dans l'onglet **Actions**
2. Cliquer sur un workflow (ex: "CI/CD Pipeline")
3. Si le workflow échoue à cause de secrets manquants, GitHub affichera un lien direct pour les créer

## 🎯 Solution Rapide

**Essayez cette URL directement** (remplacez le nom du repo) :

```
https://github.com/rihabamor/transporter-project/settings/secrets/actions
```

ou

```
https://github.com/rihabamor/transporter-backend/settings/secrets/actions
```

## 💡 Astuce

Si vous êtes collaborateur (pas admin), vous ne verrez peut-être pas cette option. Dans ce cas :
- Demandez au propriétaire du repository de vous donner les droits d'admin
- Ou demandez-lui de créer les secrets pour vous

## 🔗 Liens Utiles

- [Documentation GitHub Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Gérer les secrets d'un repository](https://docs.github.com/en/actions/security-guides/encrypted-secrets#creating-encrypted-secrets-for-a-repository)

