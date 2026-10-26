# Statut de l'integration SIGIP

**A dire clairement en soutenance : "Architecture preparee, implementation reelle a
realiser avec l'acces a l'environnement SIGIP."** Ne pas presenter SIGIP comme deja
integre en production : seule la structure (interface, mock, service de synchro,
endpoints) existe.

## Ce qui existe deja dans le code

- `ExternalDataSource` : interface d'abstraction pour toute source de donnees externe.
- `MockSigipDataSource` : implementation de test/demo, sans connexion reelle.
- `SigipDB2DataSource` : squelette d'implementation cible pour IBM DB2 (IBM i / AS400).
- Un service de synchronisation et des endpoints d'integration (`IntegrationController`).

## Ce qu'il manque pour une vraie connexion (non realisable sans l'environnement client)

Cette liste ne peut pas etre completee depuis un environnement de developpement
generique — elle depend entierement de l'infrastructure de l'entreprise (SEWS Cabind) :

1. **Acces reseau** au serveur IBM i hebergeant SIGIP (VPN, pare-feu, IP/port autorises).
2. **Driver DB2 IBM officiel** (`jt400.jar` ou pilote JDBC DB2 for i) — dependance
   proprietaire, pas disponible sur Maven Central publiquement dans tous les cas ;
   generalement fourni par IBM ou deja present sur le reseau interne de l'entreprise.
3. **Identifiants SIGIP** (compte technique dedie, avec des droits en lecture seule
   si possible) — a demander a l'equipe qui administre SIGIP.
4. **Correspondance exacte des colonnes** entre les tables/vues SIGIP et les colonnes
   de `ProductionRecord` (dates, lignes de production, quantites, defauts) — a valider
   avec l'equipe metier, car les noms de colonnes IBM i suivent souvent des
   conventions courtes (ex: `PRDATE`, `PRLIGN`) qui doivent etre mappees explicitement.
5. **Requetes validees avec l'equipe metier** (quelles tables, quels filtres, quelle
   fenetre temporelle de synchronisation).
6. **Test de synchronisation reel** contre un environnement SIGIP de recette.
7. **Gestion des doublons** lors de synchronisations repetees (cle naturelle a definir :
   probablement date + ligne + eventuellement un identifiant de lot SIGIP).
8. **Planification automatique** de la synchronisation (cron, frequence a definir avec
   le metier).
9. **Gestion des erreurs de connexion** (timeout reseau, indisponibilite du serveur IBM i,
   alerting en cas d'echec repete).

## Prochaine etape concrete

Une fois l'acces reseau et les identifiants SIGIP obtenus aupres de l'entreprise :
1. Ajouter la dependance du driver DB2/IBM i au `pom.xml`.
2. Completer `SigipDB2DataSource` avec la vraie chaine de connexion JDBC
   (`jdbc:as400://<host>;...`) via variables d'environnement (meme principe que
   `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` pour MySQL — ne jamais commiter ces valeurs).
3. Ecrire les requetes SQL validees avec le metier dans cette classe.
4. Tester d'abord en lecture seule sur un petit echantillon avant d'activer la
   synchronisation planifiee complete.
