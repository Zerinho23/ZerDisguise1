# ZerDisguise — Plugin de Disfraces con Menú GUI Interactivo

Plugin modificado de LibsDisguises con un menú gráfico interactivo para servidores Paper/Spigot 1.20.4.

---

## Descarga

**[⬇ ZerDisguise.jar — Última versión](https://github.com/Zerinho23/ZerDisguise1/releases/latest)**

---

## Funciones principales

### Menú GUI Interactivo
Abre un inventario visual de 54 slots con todas las opciones de disfraz.

**Comando:** `/disguisemenu` (alias: `/dmenu`, `/disgmenu`)

**Permiso:** `libsdisguises.menu` (solo operadores por defecto)

---

### Categorías del menú

| Categoría | Descripción |
|-----------|-------------|
| 🧟 **Mobs** | Todos los tipos de criaturas del juego, paginado |
| 👤 **Jugadores** | Jugadores en línea (cabeza real) + jugadores offline por nombre |
| 🖼 **Objetos / Misc** | Entidades especiales: marcos, cristales, proyectiles, bloques cayendo... |
| 🧭 **Mi Disfraz** | Ver y gestionar tu disfraz activo |

---

### Quitar disfraz desde el menú
El botón **Quitar Disfraz** aparece siempre en el menú principal:
- 🔴 **Rojo (Barrera)** — si tienes un disfraz activo → clic para quitarlo
- ⚪ **Gris** — si no tienes ningún disfraz puesto

---

### Disfrazarte como jugador offline
1. Abre el menú `/disguisemenu`
2. Entra a **Jugadores**
3. Haz clic en **"Jugador Offline"** (ícono de libro)
4. Escribe el nombre del jugador en el chat (tienes 30 segundos)
5. Escribe `cancelar` para salir

---

## Instalación

1. Descarga `ZerDisguise.jar` desde la sección de [Releases](https://github.com/Zerinho23/ZerDisguise1/releases/latest)
2. Coloca el archivo en la carpeta `plugins/` de tu servidor
3. Reinicia el servidor
4. Usa `/disguisemenu` en el juego

---

## Requisitos

- Paper o Spigot **1.20.4**
- Java 21
- Permisos de operador (`/op`) para usar el menú

---

## Otros comandos disponibles (LibsDisguises base)

| Comando | Descripción |
|---------|-------------|
| `/disguise <tipo>` | Disfrazarte manualmente |
| `/undisguise` | Quitar disfraz |
| `/disguiseplayer <nombre>` | Disfraz de jugador por nombre |
| `/disguiseviewself` | Ver/ocultar tu propio disfraz |

---

## Compilar desde el código fuente

```bash
./gradlew :shaded:shadowJar :shaded:compileShadedFiles
# El JAR se genera en: shaded/build/libs/ZerDisguise.jar
```

---

## Créditos

- Base: [LibsDisguises](https://github.com/libraryaddict/LibsDisguises) por libraryaddict
- Menú GUI y modificaciones: ZerDisguise por Zerinho23

