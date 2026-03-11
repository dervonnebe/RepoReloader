# RepoReloader

Überwacht GitHub-Releases und lädt neue Plugin-Versionen automatisch herunter - kein manuelles Kopieren mehr.

---

## Wie es funktioniert

RepoReloader fragt in konfigurierbaren Intervallen die GitHub-API nach neuen Releases ab. Wird eine neuere Version gefunden, wird das JAR automatisch als `PluginName.jar.update` in den `plugins/`-Ordner geladen. Beim nächsten Server-Neustart kann ein Update-Loader (z. B. [Plugman](https://dev.bukkit.org/projects/plugman)) die Datei automatisch einsetzen.

---

## Anforderungen

| | |
|---|---|
| Server | Paper / Folia 1.21+ |
| Java | 21+ |
| GitHub Token | Für private Repos erforderlich |

---

## Installation

1. JAR in `plugins/` legen und Server starten
2. `plugins/RepoReloader/config.yml` öffnen
3. GitHub-Token und Repositories eintragen (siehe unten)
4. `/rr check` zum Testen

---

## Konfiguration

```yaml
# Token unter https://github.com/settings/tokens/new erstellen
# Private Repos: Scope "Contents" (Read-only) erforderlich
# Öffentliche Repos: kein Scope nötig
github-token: "ghp_..."

check-interval-minutes: 30   # globales Intervall

repositories:
  - owner: "dervonnebe"
    repo: "MeinPlugin"
    local-filename: "MeinPlugin.jar"
    # check-interval-minutes: 10   # pro Repo überschreibbar
```

Alle Nachrichten sind im **MiniMessage-Format** editierbar (`messages.*` in `config.yml`).

---

## Befehl

| Befehl | Beschreibung |
|---|---|
| `/rr check` | Alle Repos sofort prüfen |
| `/rr check owner/repo` | Ein bestimmtes Repo sofort prüfen |

---

## Berechtigungen

| Node | Standard | Beschreibung |
|---|---|---|
| `reporeloader.admin.updates` | OP | Update-Benachrichtigungen empfangen |
| `reporeloader.admin.force` | OP | `/rr check` ausführen |

---

## Build

```bash
./gradlew clean build
# → build/libs/RepoReloader-<version>.jar
```
