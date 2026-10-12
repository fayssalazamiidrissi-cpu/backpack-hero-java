# Backpack Hero — clone en Java

Projet de L3 (Programmation orientée objet), réalisé en binôme sur environ 3 mois.

Clone du jeu roguelike *Backpack Hero* : le héros explore un donjon de 3 étages,
affronte des ennemis, ramasse des objets, range son sac à dos, croise des marchands
et des healers, et doit atteindre la sortie du 3ᵉ étage.

## Technologies
Java, bibliothèque graphique Zen (`lib/zen-6.0.jar`), architecture MVC
(`model` / `view` / `controller`), Javadoc.

## Lancer le jeu
Le projet est configuré pour Java 25 (il utilise les variables anonymes `_`).
```bash
ant jar
java -jar BackpackHero.jar
```
Le mode d'emploi du jeu est dans `docs/user.pdf`.

## Structure
- `src/fr/uge/backpack/model` : héros, sac à dos, objets, ennemis, donjon et salles
- `src/fr/uge/backpack/view` : affichage et gestion des images
- `src/fr/uge/backpack/controller` : boucle de jeu et gestion de l'inventaire
- `src/Items.txt`, `src/Monstres.txt` : données des objets et des monstres

## Ma part
Interface graphique, gestion du donjon et de la map.

## Remarque
Projet pédagogique à but non lucratif. Les images du jeu viennent du jeu original
*Backpack Hero* et appartiennent à ses créateurs.
