
# Machine Learning durch Computerspiele lernen

## 1. Projektidee und Ziel

Ich möchte praktisch lernen, wie Machine Learning funktioniert, indem ich einem Computer beibringe, ein Computerspiel selbstständig zu spielen und zu gewinnen.

Anstatt nur theoretische Beispiele oder Datensätze zu verwenden, soll ein Agent durch eigenes Ausprobieren lernen:

- Er beobachtet den aktuellen Spielzustand.
- Er entscheidet, welche Aktion er ausführen möchte.
- Er erhält eine Rückmeldung, ob die Aktion gut oder schlecht war.
- Er verbessert seine Strategie durch wiederholtes Spielen.

Das Projekt soll schrittweise aufgebaut werden. Als erstes Spiel eignet sich **Snake**, weil es relativ einfach ist und trotzdem interessante Entscheidungen erfordert.

Langfristig soll die Architektur auch für andere Spiele wie Flappy Bird oder einfache Jump'n'Run-Spiele funktionieren.

**Leitprinzip:** Möglichst viel selbst implementieren. Bibliotheken werden nur dort eingesetzt, wo sie nichts vom Lerneffekt wegnehmen (z.B. Oberfläche, Tests, Diagramme) oder bewusst als Vergleich dienen.

---

## 2. Welche Machine-Learning-Verfahren verwenden wir?

Das zentrale Verfahren ist **Reinforcement Learning (RL)**, auf Deutsch bestärkendes Lernen. Dabei lernt ein Agent durch Interaktion mit einer Umgebung.

Ergänzend nutzen wir **Supervised Learning** (überwachtes Lernen) in Form von *Imitation Learning*: Ein neuronales Netz lernt, einen vorhandenen Spieler nachzuahmen. Damit lernen wir die Grundlagen neuronaler Netze (Loss, Gradientenabstieg, Overfitting) an einem einfacheren Problem, bevor wir sie im Reinforcement Learning einsetzen.

### Die wichtigsten Bestandteile

| Begriff | Bedeutung |
|---|---|
| Agent | Das lernende Programm |
| Environment | Die Spielumgebung |
| Observation | Informationen über den aktuellen Spielzustand |
| Action | Eine Aktion des Agenten |
| Reward | Belohnung oder Bestrafung für eine Aktion |
| Policy | Strategie, nach der der Agent Aktionen auswählt |
| Episode | Ein vollständiger Spieldurchlauf |

### Beispiel mit Snake

#### Aktionen

Die Aktionen sind **relativ zur aktuellen Bewegungsrichtung**:

- geradeaus
- links abbiegen
- rechts abbiegen

Dadurch kann der Agent nie direkt in sich selbst zurücklaufen, und der Aktionsraum bleibt klein (3 Aktionen).

#### Observation: die wichtigste Designentscheidung

Wie der Agent die Welt „sieht“, entscheidet oft mehr über den Lernerfolg als der Algorithmus selbst.

Würde man dem Agenten die vollständige Information geben (Kopfposition, Essensposition, alle Körperteile), gäbe es astronomisch viele Zustände. Eine Q-Table würde nie ausreichend gefüllt, der Agent würde praktisch nichts lernen.

Für Q-Learning verwenden wir deshalb **11 kompakte Merkmale** (jeweils ja/nein):

| Gruppe | Merkmale | Anzahl |
|---|---|---|
| Gefahr | Gefahr geradeaus, Gefahr links, Gefahr rechts | 3 |
| Richtung | aktuell nach oben / unten / links / rechts | 4 |
| Essen | Essen liegt oben / unten / links / rechts vom Kopf | 4 |

Das ergibt maximal 2¹¹ = 2048 Zustände – eine gut handhabbare Q-Table.

Später (DQN) können wir mit reichhaltigeren Observations experimentieren, z.B. Abstände statt Ja/Nein oder ein Ausschnitt des Spielfelds.

#### Reward-System

| Ereignis | Reward |
|---|---|
| Essen erreicht | +10 |
| Kollision mit Wand oder eigenem Körper | −10 |
| Normaler Schritt | 0 (oder leicht negativ, z.B. −0.01) |

**Achtung, bekannte Falle:** Eine positive Belohnung fürs reine Überleben führt dazu, dass die Schlange lernt, endlos im Kreis zu laufen, statt Essen zu suchen. Deshalb:

- keine positive Belohnung pro Schritt
- Episode wird abgebrochen (`truncated`), wenn die Schlange zu lange kein Essen findet, z.B. nach `100 × Länge` Schritten

Durch viele Spielversuche lernt der Agent, welche Entscheidungen langfristig erfolgreich sind.

---

## 3. Architektur des Projekts

Ein wichtiger Punkt ist die Trennung zwischen dem eigentlichen Spiel und dem lernenden Agenten.

Das Spiel soll nicht wissen müssen, welcher Algorithmus gerade spielt. Es stellt lediglich eine Schnittstelle zur Verfügung.

### Grundarchitektur

```text
+---------------------------+        +---------------------------+
|      Game Core            |        |      UI (optional)        |
|  reine Spiellogik         | <----- |  JavaFX-Renderer          |
|  kein JavaFX, kein Timer  |  liest |  menschlicher Spieler     |
+-------------+-------------+        +---------------------------+
              |
              v
+---------------------------+
|    Environment Interface  |
|                           |
|  reset()                  |
|  step(action)             |
|  observationSize()        |
|  actionCount()            |
+-------------+-------------+
              |
              v
+---------------------------+
|       Agent Interface     |
|                           |
|  chooseAction()           |
|  learn()                  |
+-------------+-------------+
              |
              v
+---------------------------+
|        Agenten            |
|                           |
|  Random / Rule-Based      |
|  Q-Learning               |
|  Imitation (eigenes NN)   |
|  Deep Q-Network           |
+---------------------------+
```

### Headless-Spiellogik

Beim Training werden Millionen von Spielschritten simuliert. Deshalb gilt:

- Die Spiellogik (`game.core`) hat **keine Abhängigkeit zu JavaFX**.
- Es gibt **keinen Timer und keinen Thread** in der Spiellogik: Ein Aufruf von `step()` ist genau ein Spielschritt.
- Der Zufallsgenerator wird mit einem **Seed** initialisiert, damit Experimente reproduzierbar sind.
- Die Oberfläche ist nur ein Betrachter: Sie liest den Spielzustand und zeichnet ihn. Für den menschlichen Spieler sorgt die UI für das Timing.

### Warum ist diese Trennung wichtig?

Dadurch können wir verschiedene Agenten mit demselben Spiel testen:

- Random Agent: spielt zufällige Aktionen
- Rule-Based Agent: spielt anhand festgelegter Regeln (Vergleichsmaßstab)
- Q-Learning Agent: lernt durch Erfahrung
- Imitation Agent: neuronales Netz, das einen anderen Spieler nachahmt
- DQN Agent: lernt durch Erfahrung mit einem neuronalen Netz
- LLM Agent: später eventuell ein Sprachmodell über LangChain4j

Das Spiel selbst muss dafür nicht verändert werden. Und Training kann ohne Oberfläche mit voller Geschwindigkeit laufen.

---

## 4. Die Schnittstelle zwischen Spiel und Machine Learning

Die Schnittstelle ist einer der wichtigsten Bestandteile der Architektur. Sie ist an das Konzept der Python-Bibliothek Gymnasium angelehnt.

### Vom Spiel zum Agenten

- aktueller Spielzustand (Observation)
- Belohnung
- Information, ob die Episode beendet ist

### Vom Agenten zum Spiel

- die gewünschte Aktion

### Typischer Ablauf

```text
1. Episode starten: reset()
        |
        v
2. Spiel liefert Observation
        |
        v
3. Agent wählt Action
        |
        v
4. Spiel führt Action aus: step(action)
        |
        v
5. Spiel liefert neue Observation, Reward, terminated/truncated
        |
        v
6. Agent lernt aus dem Ergebnis
        |
        v
7. Wiederholung bis Episodenende
```

### Spielunabhängiges Format

Damit Agenten für mehrere Spiele wiederverwendbar sind, verwendet die Schnittstelle ein allgemeines Format:

- **Observation:** `double[]` mit fester Länge (ein neuronales Netz braucht ohnehin dieses Format)
- **Action:** `int` im Bereich `0 .. actionCount() - 1`

Das spielspezifische Wissen (was bedeutet Index 3 der Observation? was bedeutet Aktion 1?) bleibt im jeweiligen Environment.

### Java-Schnittstelle (Java 25)

```java
public interface Environment {

    double[] reset();

    StepResult step(int action);

    int observationSize();

    int actionCount();
}
```

```java
public record StepResult(
        double[] observation,
        double reward,
        boolean terminated,
        boolean truncated
) {
    public boolean done() {
        return terminated || truncated;
    }
}
```

```java
public interface Agent {

    int chooseAction(double[] observation);

    default void learn(double[] observation, int action, StepResult result) {
        // nicht-lernende Agenten (Random, Rule-Based) tun hier nichts
    }
}
```

Bedeutung:

- `observation`: neuer Spielzustand
- `reward`: Belohnung für die letzte Aktion
- `terminated`: Spiel ist regulär beendet, beispielsweise durch eine Kollision
- `truncated`: Spiel wurde aus einem anderen Grund beendet, beispielsweise durch ein Schrittlimit

---

## 5. Technologien

### Warum Java?

Wir setzen das Projekt in Java um. Reinforcement Learning ist nicht auf Python beschränkt – und da es in Java keine gepflegte RL-Bibliothek gibt, implementieren wir die Algorithmen ohnehin selbst. Für das Lernziel ist das ein Vorteil.

### Technologie-Stack

| Aufgabe | Technologie |
|---|---|
| Programmiersprache | Java 25 (LTS) |
| Build | Maven |
| Oberfläche | JavaFX (nur als Ansicht / für menschliche Spieler) |
| Tests | JUnit 5/6 (Jupiter) |
| Q-Learning, neuronales Netz, DQN | **selbst implementiert** |
| Diagramme (Lernkurven) | JavaFX `LineChart` oder XChart; alternativ CSV-Export |
| Optional, späterer Vergleich | DJL (Deep Java Library), Smile |
| Optional, LLM-Agent | LangChain4j |

### Bewertung von Java-ML-Bibliotheken (Stand November 2026)

| Bibliothek | Status | Bewertung |
|---|---|---|
| **RL4J** | ❌ veraltet | Wurde mit 1.0.0-M2 aus Deeplearning4j entfernt (nicht mehr gepflegt). Nicht verwenden. |
| **Deeplearning4j** | ⚠️ unklar | Letztes stabiles Release 1.0.0-M2.x (2022), aktuell ein „1.0.0-rewrite“ in Übergangsphase. Für ein Lernprojekt ein instabiles Fundament. |
| **Tribuo** (Oracle) | ⚠️ wenig aktiv | Letztes Release 4.3.2 (Patch). Klassisches ML, kein Reinforcement Learning. |
| **DJL** (Amazon) | ✅ aktiv | Java-API über PyTorch / TensorFlow / ONNX Runtime. Stark bei Inferenz, Training möglich. Gut für einen späteren Vergleich „eigenes Netz vs. PyTorch“. |
| **Smile** | ✅ aktiv | Umfangreiche ML-Bibliothek für die JVM. Version 6 benötigt Java 25. Gut für Vergleiche beim überwachten Lernen. |
| **LangChain4j** | ✅ aktiv | Für LLM-Integration, kein RL-Framework. Interessant für den späteren LLM-Agenten. |

### Warum das neuronale Netz selbst schreiben?

Für Snake reicht ein kleines Netz: 11 Eingänge → eine versteckte Schicht (64–256 Neuronen) → 3 Ausgänge. Forward Pass, Backpropagation und ein Optimierer (SGD, später Adam) sind wenige hundert Zeilen Java.

Dabei verstehen wir wirklich, was ein neuronales Netz tut – statt nur eine Bibliothek aufzurufen. Später können wir das Ergebnis mit DJL vergleichen.

### Warum nicht direkt LangChain4j?

LangChain4j ist primär für die Integration von Large Language Models (LLMs) gedacht (Chatbots, Tool Calling, RAG). Es ist kein Reinforcement-Learning-Framework.

LangChain4j könnte später interessant werden, um einen LLM-Agenten zu integrieren und dessen Spielverhalten mit dem klassischen RL-Agenten zu vergleichen.

---

## 6. Welche Lernalgorithmen verwenden wir?

Wir beginnen bewusst einfach und steigern die Komplexität schrittweise.

### Phase 1: Q-Learning

Q-Learning ist ein klassischer Reinforcement-Learning-Algorithmus. Der Agent lernt, wie gut eine bestimmte Aktion in einem bestimmten Zustand ist. Dafür verwendet er eine Q-Table.

```text
Q(State, Action) = erwarteter zukünftiger Nutzen
```

Update-Formel:

Q(s,a) = Q(s,a) + α [r + γ max Q(s',a') - Q(s,a)]

- s: aktueller Zustand
- a: ausgeführte Aktion
- r: erhaltene Belohnung
- s': neuer Zustand
- α: Lernrate
- γ: Diskontierungsfaktor

Ist s' ein Endzustand (`terminated`), entfällt der Term `γ max Q(s',a')`.

**Exploration vs. Exploitation:** Mit der ε-greedy-Strategie wählt der Agent mit Wahrscheinlichkeit ε eine zufällige Aktion (Exploration), sonst die beste bekannte (Exploitation). ε wird im Laufe des Trainings verringert.

Für die Q-Table wird die `double[]`-Observation in einen Schlüssel umgewandelt (bei 11 Ja/Nein-Merkmalen z.B. eine Zahl zwischen 0 und 2047).

### Phase 2: Neuronales Netz mit Imitation Learning

Bevor wir ein neuronales Netz im Reinforcement Learning einsetzen, lernen wir es an einer einfacheren Aufgabe kennen:

1. Den Rule-Based Agent (oder uns selbst) viele Spiele spielen lassen und dabei Paare aus *(Observation, gewählte Aktion)* aufzeichnen.
2. Ein selbst geschriebenes neuronales Netz darauf trainieren, die Aktion vorherzusagen (Klassifikation).
3. Das Netz als Agent spielen lassen.

Dabei lernen wir: Forward Pass, Loss-Funktion, Backpropagation, Gradientenabstieg, Trainings- vs. Testdaten, Overfitting.

### Phase 3: Deep Q-Network (DQN)

Bei komplexeren Spielen wird die Anzahl möglicher Zustände sehr groß; eine Q-Table ist dann nicht mehr praktikabel. Ein DQN verwendet stattdessen ein neuronales Netz, das die Q-Werte schätzt.

Da wir das Netz aus Phase 2 bereits kennen, ist DQN im Kern „dasselbe Netz, andere Trainingsziele“ – ergänzt um:

- **Experience Replay:** Erfahrungen werden gespeichert und zufällig gemischt zum Training verwendet.
- **Target Network:** Eine zweite, langsamer aktualisierte Kopie des Netzes stabilisiert das Training.

---

## 7. Geplanter Projektverlauf

### Schritt 1: Snake-Spiellogik (headless)

Ziel: Vollständige Spiellogik ohne Oberfläche, abgesichert durch Tests.

- Spielfeld (Start mit 10×10, konfigurierbar)
- Schlange und Bewegung (relative Aktionen)
- Essen zufällig platzieren (mit Seed)
- Kollisionen erkennen
- Spielende
- JUnit-Tests für alle Regeln

Ergebnis: Ein getestetes Spiel, das per Methodenaufruf Schritt für Schritt gespielt werden kann.

### Schritt 2: Environment-Schnittstelle und Random Agent

- `Environment`, `StepResult`, `Agent` erstellen
- `SnakeEnvironment` mit den 11 Merkmalen und dem Reward-System implementieren
- Random Agent implementieren
- Trainer-Schleife: viele Episoden automatisch spielen, Punktzahl und Schritte messen

Ergebnis: Der Computer spielt Snake selbstständig (noch ohne zu lernen). Wir haben eine erste Vergleichszahl.

### Schritt 3: JavaFX-Oberfläche

- Renderer, der den Spielzustand zeichnet
- Steuerung per Tastatur für menschliche Spieler
- Modus zum Zuschauen, wie ein Agent spielt (mit einstellbarer Geschwindigkeit)

Ergebnis: Ein Mensch kann Snake spielen, und wir können Agenten beim Spielen beobachten.

### Schritt 4: Rule-Based Agent

- Einfache Strategie: auf das Essen zugehen, Gefahren vermeiden

Ergebnis: Ein Vergleichsmaßstab, den der lernende Agent schlagen soll.

### Schritt 5: Q-Learning

- Q-Table implementieren
- Lernrate, Diskontierungsfaktor, ε-greedy mit abnehmendem ε
- Training über viele Episoden
- Q-Table speichern und laden

Ergebnis: Der Agent verbessert seine Spielstrategie durch Erfahrung.

### Schritt 6: Training analysieren und visualisieren

Wir messen über den Trainingsverlauf:

- durchschnittliche Punktzahl (gleitender Durchschnitt)
- maximale Punktzahl
- durchschnittliche Episodenlänge
- Anteil der Episoden mit Abbruch durch Schrittlimit

Die Werte werden als CSV gespeichert und als Lernkurve angezeigt (JavaFX `LineChart` oder XChart).

Beispiel:

```text
Episode 1:    Score 0
Episode 100:  Score 1
Episode 500:  Score 3
Episode 1000: Score 7
Episode 5000: Score 15
```

Die Werte sind nur illustrative Beispiele, keine garantierten Ergebnisse.

Außerdem: Hyperparameter und Seed jedes Experiments protokollieren, damit Ergebnisse vergleichbar und wiederholbar sind.

### Schritt 7: Eigenes neuronales Netz und Imitation Learning

- Kleines neuronales Netz selbst implementieren (Forward Pass, Backpropagation, SGD)
- Spiele des Rule-Based Agents aufzeichnen
- Netz trainieren und als Agent spielen lassen
- Trainings- und Testgenauigkeit vergleichen

Ergebnis: Wir verstehen, wie ein neuronales Netz lernt.

### Schritt 8: Deep Q-Network

- DQN mit dem eigenen Netz implementieren
- Experience Replay
- Target Network
- Ergebnisse mit Q-Learning und Rule-Based Agent vergleichen
- Optional: dasselbe mit DJL umsetzen und vergleichen

Ergebnis: Ein Agent, der mit größeren Zustandsräumen umgehen kann.

### Schritt 9: Weitere Spiele

- Flappy Bird (interessant: kontinuierlicher Zustand, muss für Q-Learning diskretisiert werden)
- Pong
- einfache Jump'n'Run-Spiele
- eventuell ein eigenes kleines Strategiespiel

Die Agenten können wiederverwendet werden. Nur das jeweilige Environment (Observation, Aktionen, Rewards) muss neu definiert werden.

### Schritt 10 (optional): LLM-Agent

Siehe Abschnitt 10.

---

## 8. Mögliche Projektstruktur

```text
LearnSnake/
│
├── pom.xml
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── <basispaket>/
│   │
│   │           ├── game/
│   │           │   └── snake/
│   │           │       ├── SnakeGame.java          (reine Logik, kein JavaFX)
│   │           │       └── SnakeEnvironment.java   (Observation, Reward)
│   │
│   │           ├── environment/
│   │           │   ├── Environment.java
│   │           │   └── StepResult.java
│   │
│   │           ├── agent/
│   │           │   ├── Agent.java
│   │           │   ├── RandomAgent.java
│   │           │   ├── RuleBasedSnakeAgent.java
│   │           │   ├── QLearningAgent.java
│   │           │   └── DqnAgent.java
│   │
│   │           ├── nn/
│   │           │   └── NeuralNetwork.java          (selbst implementiert)
│   │
│   │           ├── training/
│   │           │   ├── Trainer.java
│   │           │   └── TrainingStatistics.java
│   │
│   │           ├── ui/
│   │           │   └── SnakeView.java              (JavaFX)
│   │
│   │           └── Main.java
│   │
│   └── test/
│       └── java/
│
└── README.md
```

Diese Struktur ist ein Vorschlag und kann im Laufe des Projekts angepasst werden.

---

## 9. Wichtige Konzepte, die ich dabei lernen möchte

Die Themen werden jeweils dann eingeführt, wenn sie im Projekt praktisch benötigt werden:

| Konzept | Schritt |
|---|---|
| Was ist Reinforcement Learning? Agent, Environment, Episode | 2 |
| Was ist eine Reward Function? Reward Shaping und seine Fallen | 2 |
| Zustandsrepräsentation (Feature Engineering) | 2 |
| Baseline / Vergleichsmaßstab | 2, 4 |
| Was ist eine Policy? | 4, 5 |
| Exploration vs. Exploitation | 5 |
| Q-Table und Q-Learning | 5 |
| Wie bewertet man den Lernerfolg eines Agenten? | 6 |
| Reproduzierbarkeit und Hyperparameter | 6 |
| Was ist ein neuronales Netz? Forward Pass, Backpropagation | 7 |
| Supervised Learning, Loss, Gradientenabstieg, Overfitting | 7 |
| Deep Q-Network | 8 |
| Experience Replay, Target Network | 8 |
| Diskretisierung kontinuierlicher Zustände | 9 |

---

## 10. Langfristige Erweiterung: Vergleich mit einem LLM-Agenten

Eine interessante spätere Erweiterung wäre ein Vergleich zwischen einem klassischen Reinforcement-Learning-Agenten und einem Large Language Model, z.B. über LangChain4j.

Mögliche Fragestellungen:

- Kann ein LLM Snake spielen?
- Wie gut kann es Spielzustände interpretieren?
- Kann es aus Feedback lernen?
- Wie unterscheidet sich seine Strategie von Q-Learning?
- Wie viele Spielversuche benötigen die unterschiedlichen Ansätze?
- Welche Methode ist effizienter (Rechenzeit, Kosten)?

Ein LLM ist nicht automatisch ein Reinforcement-Learning-Agent. Es wäre zunächst ein anderer Ansatz, der über dieselbe Environment-Schnittstelle mit dem Spiel kommunizieren kann.

---

## 11. Unsere grundlegenden Architekturentscheidungen

| Thema | Entscheidung |
|---|---|
| Programmiersprache | Java 25 (LTS) |
| Build-System | Maven |
| Erstes Spiel | Snake (Start mit 10×10-Feld) |
| Spiellogik | headless, ohne JavaFX, schrittbasiert, mit Seed |
| Grafische Oberfläche | JavaFX, nur als Ansicht |
| Schnittstelle | Eigene Environment-Schnittstelle nach Gymnasium-Vorbild |
| Observation / Action | `double[]` / `int` (spielunabhängig) |
| Aktionen Snake | relativ: geradeaus, links, rechts |
| Observation Snake | 11 Ja/Nein-Merkmale (Gefahr, Richtung, Essen) |
| ML-Verfahren | Reinforcement Learning, ergänzt um Imitation Learning |
| Algorithmen | Q-Learning → eigenes NN (Imitation) → DQN |
| Neuronales Netz | selbst implementiert |
| ML-Bibliotheken | keine im Kern; DJL / Smile optional als Vergleich |
| Nicht verwenden | RL4J (veraltet), Deeplearning4j (unklarer Status) |
| LangChain4j | Optionale spätere Erweiterung |

---

## 12. Aktueller Stand

Die Schrittnummern beziehen sich auf Abschnitt 7.

| Schritt | Inhalt | Status |
|---|---|---|
| 1 | Snake-Spiellogik (headless) mit JUnit-Tests, Maven-Projekt mit Java 25 | ✅ erledigt |
| 2 | Environment-Schnittstelle, `SnakeEnvironment`, Random Agent, Trainer-Schleife | ✅ erledigt |
| 3 | JavaFX-Oberfläche | ⏭️ als Nächstes |
| 4 | Rule-Based Agent | offen |
| 5 | Q-Learning | offen |
| 6 | Training analysieren und visualisieren | offen |
| 7 | Eigenes neuronales Netz und Imitation Learning | offen |
| 8 | Deep Q-Network | offen |
| 9 | Weitere Spiele | offen |
| 10 | LLM-Agent (optional) | offen |

### Messwerte

| Agent | Feld | Episoden | Score avg | Score max | Schritte avg |
|---|---|---|---|---|---|
| Random | 10×10 | 10'000 | 0.17 | 4 | 20.1 |

Diese Werte sind die untere Vergleichslinie: Jeder lernende Agent muss deutlich besser sein.

**Unser Ziel:** Am Ende soll ein Agent Snake selbstständig spielen und durch Training immer besser werden. Dabei soll die Architektur so gestaltet sein, dass wir später weitere Spiele und Lernalgorithmen hinzufügen können.
