# MineVegas Casino – Paper-Plugin

Server-Plugin-Nachbau von MineVegas Casino (Spiele-Logik, ohne Mod/Client).
Sechs Spiele als Kisten-Menüs: Slot Machine, Roulette, Blackjack, Duck Derby, Glücksrad, Plinko.
Einsätze in Eisen-/Goldbarren, Diamanten, Smaragden und deren Blöcken.

## Bauen
1. JDK 25 und Maven installieren (Paper 26.1+ läuft auf Java 25).
2. In `pom.xml` die Property `paper.version` auf deine Paper-Version setzen.
3. `mvn package` -> `target/MineVegasCasino.jar` in den `plugins`-Ordner legen.

## Benutzen
- `/casino menu` öffnet das Kreativmenü mit allen Automaten (Klick = 1 Stück, Shift-Klick = 64, "Alle holen"). Recht: `minevegas.menu` oder `minevegas.admin`.
- `/casino give <slots|roulette|blackjack|derby|wheel|plinko> [anzahl]` gibt Automaten-Blöcke (nur Admins).
- Block platzieren, dann per Rechtsklick spielen. Admins bauen/öffnen mit Schleichen normal.
- Abbauen (Admin) gibt den Automaten-Block zurück. Explosionen und Kolben bewegen Automaten nicht.
- `/casino reload` lädt `config.yml`. Auszahlungen für Plinko, Glücksrad und Derby sind dort einstellbar.
- Rechte: `minevegas.admin` (OP), `minevegas.menu` (OP), `minevegas.play` (alle).
- Hinweis: Die echten Kreativ-Tabs im Client kann ein Server-Plugin nicht erweitern (dafür wäre ein Mod/Resource-Pack nötig). Das Menü ersetzt sie.

## Hinweise
- Alle Auszahlungen sind Gesamtauszahlung inkl. Einsatz. Die Quoten sind eigene Werte (Hausvorteil ca. 5–13 %),
  nicht die Originalwerte der Mod.
- Bruchwerte werden abgerundet; bei Einsatz 1 gibt es dort teils nichts zurück.
- Wird das Fenster mitten in der Runde geschlossen oder verlässt der Spieler den Server,
  wird die Runde sofort beendet und ausgezahlt (Blackjack: automatisch "Halten").
- Nicht enthalten: Mod-Blöcke, 3D-Modelle, Dealer-Animationen, Musik, Croupier-Villager.
