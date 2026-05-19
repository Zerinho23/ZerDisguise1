# LibsDisguises

Uno de los mejores plugins de disfraces para servidores Minecraft. Permite que jugadores y entidades se disfracen como cualquier mob, jugador u objeto del juego, con un nivel de personalización altísimo gracias a su sistema de "watchers" por entidad.

---

## Versiones compatibles

- **Minecraft:** 1.17 — 1.21.x (Paper, Spigot, Folia)
- **Dependencia requerida:** [PacketEvents](https://www.spigotmc.org/resources/packetevents-api.80279/)
- **Java:** 17 o superior

> ⚠️ No hay soporte para versiones anteriores a 1.12.

---

## Instalación

1. Descarga **PacketEvents** y colócalo en tu carpeta `plugins/`
2. Descarga **LibsDisguises** desde [SpigotMC](https://www.spigotmc.org/resources/32453/) o desde [Jenkins](https://ci.lib.co.nz/job/LibsDisguises/)
3. Coloca el JAR en `plugins/`
4. Reinicia el servidor
5. (Opcional) Edita `plugins/LibsDisguises/config.yml` según tus preferencias

---

## ¿Qué puede hacer este plugin?

### Disfraces disponibles
El plugin soporta más de **130 tipos de entidades**, incluyendo todos los mobs modernos:

| Categoría | Ejemplos |
|---|---|
| Mobs pasivos | Vaca, Cerdo, Oveja, Pollo, Conejo, Armadillo, Camello... |
| Mobs neutrales | Lobo, Abeja, Enderman, Gólem de Hierro... |
| Mobs hostiles | Creeper, Esqueleto, Zombie, Breeze, Warden, Creaking... |
| Jefes | Dragón del End, Wither, Elder Guardian |
| Jugadores | Cualquier skin de jugador real o personalizado via MineSkin |
| Objetos/misc | Bloque cayendo, Marco de objeto, Estante de armadura, TNT... |
| Entidades moduladas | Soporte para mobs de mods (Modded Living / Modded Misc) |

### Personalización por entidad
Cada disfraz tiene propiedades únicas modificables, como por ejemplo:
- **Creeper:** activado/desactivado, tamaño de explosión
- **Oveja:** color de lana, esquilada o no
- **Caballo:** color, marcas, silla, equipamiento
- **Lobo:** domado, enojado, collar de color
- **Villager:** profesión, nivel, tipo de bioma
- **Display entities:** texto, bloques, objetos con transformaciones
- Y muchas más según el mob

---

## Comandos

| Comando | Descripción |
|---|---|
| `/disguise <tipo> [opciones]` | Te disfrazas a ti mismo |
| `/disguiseplayer <jugador> <tipo> [opciones]` | Disfrazas a otro jugador |
| `/disguiseentity <tipo> [opciones]` | Disfrazas la entidad que estás mirando |
| `/disguiseradius <radio> <tipo> [opciones]` | Disfrazas todas las entidades en un radio |
| `/disguiseselector <selector> <tipo> [opciones]` | Disfrazas entidades por selector (`@a`, `@e`, etc.) |
| `/undisguise` | Quitas tu propio disfraz |
| `/undisguiseplayer <jugador>` | Quitas el disfraz de otro jugador |
| `/undisguiseentity` | Quitas el disfraz de la entidad que miras |
| `/undisguiseradius <radio>` | Quitas disfraces en un radio |
| `/undisguiseselector <selector>` | Quitas disfraces por selector |
| `/disguisemodify <opciones>` | Modificas tu disfraz activo |
| `/disguisemodifyplayer <jugador> <opciones>` | Modificas el disfraz activo de otro jugador |
| `/disguiseanimate <animación>` | Ejecutas una animación en tu disfraz |
| `/disguiseviewself` | Activas/desactivas ver tu propio disfraz |
| `/libsdisguises reload` | Recargas la configuración del plugin |

**Aliases útiles:** `/d`, `/dis`, `/u`, `/und`

---

## Permisos principales

| Permiso | Descripción | Default |
|---|---|---|
| `libsdisguises.disguise.*` | Acceso a todos los disfraces | op |
| `libsdisguises.disguise.mob.<tipo>` | Disfraz de un mob específico | op |
| `libsdisguises.disguise.player` | Disfrazarse como jugador | op |
| `libsdisguises.disguise.misc.<tipo>` | Disfraz de entidad misc | op |
| `libsdisguises.undisguise` | Quitarse el propio disfraz | true |
| `libsdisguises.undisguise.others` | Quitar el disfraz a otros | op |
| `libsdisguises.disguiseradius` | Usar comando de radio | op |
| `libsdisguises.seethrough` | Ver a través de los disfraces | false |
| `libsdisguises.viewself` | Ver el propio disfraz | true |

---

## Disfraces personalizados

Puedes guardar disfraces con nombre en `configs/disguises.yml` y asignarlos vía permisos:

```yaml
Disguises:
  zombie_rapido: "Zombie setBabyZombie true setSpeed 2.0"
  rey_fantasma: "Player setGameProfile Notch setSkin skull"
```

Luego se usan con `/disguise zombie_rapido` si el jugador tiene el permiso correspondiente.

---

## API para desarrolladores

Agrega la dependencia a tu proyecto:

### Gradle
```groovy
repositories {
    maven { url "https://mvn.lib.co.nz/public" }
}

dependencies {
    implementation group: 'me.libraryaddict.disguises', name: 'libsdisguises', version: '11.0.18'
}
```

### Maven
```xml
<repository>
    <id>libsdisguises-public</id>
    <url>https://mvn.lib.co.nz/public/</url>
</repository>

<dependency>
    <groupId>me.libraryaddict.disguises</groupId>
    <artifactId>libsdisguises</artifactId>
    <version>11.0.18</version>
    <scope>provided</scope>
</dependency>
```

### Ejemplo básico
```java
// Disfrazar a un jugador como Creeper
MobDisguise disguise = new MobDisguise(DisguiseType.CREEPER);
DisguiseAPI.disguiseToAll(player, disguise);

// Disfrazar como otro jugador con skin
PlayerDisguise playerDisguise = new PlayerDisguise("Notch");
DisguiseAPI.disguiseToAll(player, playerDisguise);

// Quitar disfraz
DisguiseAPI.undisguiseToAll(player);
```

### Eventos disponibles
```java
@EventHandler
public void onDisguise(DisguiseEvent event) {
    Player player = (Player) event.getEntity();
    Disguise disguise = event.getDisguise();
    // event.setCancelled(true); para cancelar
}

@EventHandler
public void onUndisguise(UndisguiseEvent event) { ... }

@EventHandler
public void onInteract(DisguiseInteractEvent event) { ... }
```

---

## Links útiles

- [Página en SpigotMC](https://www.spigotmc.org/resources/32453/)
- [JavaDocs (API)](https://libraryaddict.github.io/LibsDisguises/javadoc/)
- [Descargas Jenkins](https://ci.lib.co.nz/job/LibsDisguises/)
- [PacketEvents (dependencia)](https://www.spigotmc.org/resources/packetevents-api.80279/)

---

## Reportar un bug

Antes de abrir un issue, asegúrate de:

1. Incluir el **stack trace completo** del error (o descripción detallada del comportamiento incorrecto)
2. Describir **paso a paso** cómo reproducirlo
3. Indicar **versión de Minecraft, LibsDisguises y PacketEvents**
4. Verificar que no hay errores al cargar el plugin (`/plugins`)
5. Confirmar que estás usando la versión más reciente antes de reportar

---

## Nota importante

Este proyecto **no autoriza** modificar ni eludir el código que limita funciones a clientes de pago. Tampoco autoriza publicar dicho código modificado.
