# ZerDisguise

Plugin de disfraces para servidores Minecraft basado en LibsDisguises v11.0.18. Permite que jugadores y entidades se disfracen como cualquier mob, jugador u objeto del juego — ahora con **menú gráfico interactivo** para facilitar el uso.

---

## ✨ Novedad: Menú GUI Interactivo

Esta versión añade un menú gráfico completo accesible con un solo comando. No hace falta conocer los comandos de texto.

### Uso
```
/disguisemenu
/dmenu
/disgmenu
```

### Menús disponibles
| Menú | Descripción |
|---|---|
| 🐾 **Mobs** | Todos los tipos de mobs paginados (Zombie, Creeper, etc.) |
| 🎭 **Misceláneos** | Entidades especiales (Armor Stand, Item Frame, TNT...) |
| 👤 **Jugadores** | Cabezas de jugadores en línea para disfrazarse como ellos |
| 🔮 **Mi Disfraz** | Ver tu disfraz activo y quitarlo fácilmente |

### Permiso requerido
```
libsdisguises.menu   (por defecto: solo ops)
```

---

## Versiones compatibles

- **Minecraft:** 1.17 — 1.21.x (Paper, Spigot, Folia)
- **Dependencia requerida:** [PacketEvents](https://www.spigotmc.org/resources/packetevents-api.80279/)
- **Java:** 17 o superior

> ⚠️ No hay soporte para versiones anteriores a 1.12.

---

## Instalación

1. Descarga **PacketEvents** y colócalo en tu carpeta `plugins/`
2. Descarga el JAR desde la sección [Releases](https://github.com/Zerinho23/ZerDisguise1/releases)
3. Coloca el JAR en `plugins/`
4. Reinicia el servidor
5. (Opcional) Edita `plugins/LibsDisguises/config.yml` según tus preferencias

---

## Comandos

| Comando | Descripción |
|---|---|
| `/disguisemenu` | Abre el menú gráfico de disfraces (nuevo) |
| `/disguise <tipo> [opciones]` | Te disfrazas a ti mismo |
| `/disguiseplayer <jugador> <tipo>` | Disfrazas a otro jugador |
| `/disguiseentity <tipo>` | Disfrazas la entidad que estás mirando |
| `/disguiseradius <radio> <tipo>` | Disfrazas entidades en un radio |
| `/undisguise` | Quitas tu propio disfraz |
| `/undisguiseplayer <jugador>` | Quitas el disfraz de otro jugador |
| `/disguisemodify <opciones>` | Modificas tu disfraz activo |
| `/disguiseviewself` | Activas/desactivas ver tu propio disfraz |
| `/libsdisguises reload` | Recargas la configuración |

**Aliases:** `/d`, `/dis`, `/u`, `/und`, `/dmenu`, `/disgmenu`

---

## Permisos principales

| Permiso | Descripción | Default |
|---|---|---|
| `libsdisguises.menu` | Acceso al menú GUI | op |
| `libsdisguises.disguise.*` | Todos los disfraces | op |
| `libsdisguises.disguise.mob.<tipo>` | Disfraz de un mob específico | op |
| `libsdisguises.disguise.player` | Disfrazarse como jugador | op |
| `libsdisguises.disguise.misc.<tipo>` | Disfraz de entidad misc | op |
| `libsdisguises.undisguise` | Quitarse el propio disfraz | true |
| `libsdisguises.undisguise.others` | Quitar el disfraz a otros | op |
| `libsdisguises.viewself` | Ver el propio disfraz | true |
| `libsdisguises.seethrough` | Ver a través de los disfraces | false |

---

## ¿Qué tipos de disfraz existen?

| Categoría | Ejemplos |
|---|---|
| Mobs pasivos | Vaca, Cerdo, Oveja, Pollo, Conejo, Armadillo, Camello... |
| Mobs neutrales | Lobo, Abeja, Enderman, Gólem de Hierro... |
| Mobs hostiles | Creeper, Esqueleto, Zombie, Breeze, Warden, Creaking... |
| Jefes | Dragón del End, Wither, Elder Guardian |
| Jugadores | Cualquier skin de jugador real o personalizado |
| Misceláneos | Bloque cayendo, Marco de objeto, Estante de armadura, TNT... |

---

## Disfraces personalizados (archivo)

Puedes guardar disfraces con nombre en `configs/disguises.yml`:

```yaml
Disguises:
  zombie_rapido: "Zombie setBabyZombie true setSpeed 2.0"
  rey_fantasma: "Player setGameProfile Notch setSkin skull"
```

Se usan con `/disguise zombie_rapido` si el jugador tiene el permiso correspondiente.

---

## API para desarrolladores

### Gradle
```groovy
repositories {
    maven { url "https://mvn.lib.co.nz/public" }
}
dependencies {
    implementation group: "me.libraryaddict.disguises", name: "libsdisguises", version: "11.0.18"
}
```

### Ejemplo básico
```java
// Disfrazar como Creeper
MobDisguise disguise = new MobDisguise(DisguiseType.CREEPER);
DisguiseAPI.disguiseToAll(player, disguise);

// Disfrazar como jugador con skin
PlayerDisguise playerDisguise = new PlayerDisguise("Notch");
DisguiseAPI.disguiseToAll(player, playerDisguise);

// Quitar disfraz
DisguiseAPI.undisguiseToAll(player);
```

---

## Links útiles

- [Releases (descargas)](https://github.com/Zerinho23/ZerDisguise1/releases)
- [Página en SpigotMC](https://www.spigotmc.org/resources/32453/)
- [JavaDocs (API)](https://libraryaddict.github.io/LibsDisguises/javadoc/)
- [PacketEvents (dependencia)](https://www.spigotmc.org/resources/packetevents-api.80279/)

---

## Reportar un bug

Antes de abrir un issue:

1. Incluye el **stack trace completo** del error
2. Describe **paso a paso** cómo reproducirlo
3. Indica **versión de Minecraft, plugin y PacketEvents**
4. Confirma que estás usando la versión más reciente

