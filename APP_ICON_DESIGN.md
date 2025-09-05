# 💕 Love App - Icon Design

## Design-Konzept

Das neue App-Icon basiert auf dem wunderschönen HTML/CSS Design-Konzept und wurde für Android optimiert.

### Hauptelemente:
- 🌸 **Rosa Gradient-Hintergrund**: Sanfter Gradient von hellem Pink (#fbcfe8) zu dunklerem Pink (#f472b6)
- 💖 **Zentrales Herz**: Weißes, leicht rotiertes Herz (-15°) als Hauptelement
- 💌 **Liebesnotiz**: Kleines weißes Rechteck mit rosa Herz (symbolisiert tägliche Nachrichten)
- 📅 **Timeline-Linie**: Dünne weiße Linie mit Punkten am unteren Rand (symbolisiert Timeline-Feature)

### Dateien:
- `ic_app_heart.xml` - Quadratisches Icon (für normale Launcher)
- `ic_app_heart_round.xml` - Rundes Icon (für runde Launcher)
- `app_icon_design.svg` - Original SVG Design-Vorlage

### Farben:
- **Hauptrosa**: #f472b6
- **Hellrosa**: #fbcfe8  
- **Weiß**: #FFFFFF (für Herz und Details)
- **Transparenz**: 0.6-0.9 für subtile Overlay-Effekte

### Design-Prinzipien:
1. **Romantisch & Warm**: Rosa Farbpalette vermittelt Liebe und Wärme
2. **Klar & Minimalistisch**: Einfache Formen, die auch in kleinen Größen gut erkennbar sind
3. **Symbolisch**: Jedes Element repräsentiert ein App-Feature
   - Herz = Liebe/Hauptzweck
   - Notiz = Tägliche Nachrichten
   - Timeline = Erinnerungen/Meilensteine
4. **Modern**: Abgerundete Ecken und Schatten für moderne Android-Ästhetik

## Verwendung:
Das Icon wird automatisch im AndroidManifest.xml verwendet:
```xml
android:icon="@drawable/ic_app_heart"
android:roundIcon="@drawable/ic_app_heart_round"
```

## Anpassungen für verschiedene Launcher:
- **Standard Launcher**: Verwendet `ic_app_heart.xml` (quadratisch mit abgerundeten Ecken)
- **Runde Launcher**: Verwendet `ic_app_heart_round.xml` (kreisförmig)
- **Adaptive Icons**: Beide Icons sind 108dp x 108dp für optimale Skalierung

---
*Design basiert auf dem ursprünglichen HTML-Konzept - romantisch, warm und bedeutungsvoll.*