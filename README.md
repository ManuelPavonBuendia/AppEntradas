# Eventum — App de control de acceso a eventos

# 1. Introduccion
Eventum es una aplicación Android nativa desarrollada en Kotlin para el control de acceso a eventos. Permite al personal escanear códigos QR de entradas y validarlas en tiempo real contra la base de datos de Odoo.

# 2. Requisitos del sistema

**Dispositivo:**
- Android 7.0 (API 24) o superior
- Cámara trasera con autofoco
- Conexión a internet

**Entorno de desarrollo:**
- Android Studio
- JDK 11
- Kotlin 1.9 o superior
- Gradle 8.x

---

# 3. Flujo de la aplicación

El flujo comienza cuando el empleado abre la app e inicia sesión con sus credenciales de Odoo. Una vez autenticado, selecciona el evento en el que trabaja y empieza a escanear entradas. La app lee el código QR, consulta directamente con Odoo mediante XML-RPC y muestra el resultado al empleado en pantalla.

```mermaid
sequenceDiagram
    autonumber
    actor Empleado
    participant App as App (Móvil)
    participant Odoo as Servidor (Odoo)

    Note over Empleado, Odoo: Inicio de Sesión
    Empleado->>App: Introduce sus datos
    App->>Odoo: ¿Son correctos usuario y clave?
    Odoo-->>App: Sí, acceso concedido

    Note over Empleado, Odoo: Preparación
    Empleado->>App: Elige el evento actual
    App->>Odoo: Dame la lista de eventos
    Odoo-->>App: Lista de eventos disponibles

    Note over Empleado, Odoo: Proceso de Escaneo
    Empleado->>App: Escanea el código QR del ticket
    App->>Odoo: ¿Este ticket es válido para este evento?
    Odoo-->>App: Envía información del ticket

   alt:
        Note left of App: Ticket Válido
        App->>Odoo: Marcar ticket como "Ya utilizado"
        Odoo-->>App: Confirmado
        App-->>Empleado: ✅ Acceso Válido
    else 
        Note left of App: Ya utilizado
        App-->>Empleado: ⚠️ Entrada ya utilizada
    else 
        Note left of App: Error o no existe
        App-->>Empleado: ❌ Acceso Denegado
    end

```

---

# 4. Diagrama de Navegación

El flujo de pantallas de la aplicación está diseñado para ser directo y funcional, permitiendo al empleado realizar su trabajo sin distracciones. Los componentes principales son:

*   **Login:** Es la pantalla inicial de la app. El trabajador pone sus datos y, si son correctos, la aplicación le dejará acceder a los eventos.
*   **Eventos:** En esta pantalla aparece una lista con los eventos que hay anunciados. El empleado seleccionará el evento específico de la entrada que va a escanear para cargar el contexto correcto.
*   **Escáner:** Se abrirá la cámara del móvil para leer los códigos QR. Se escanea el código de la entrada y en la parte inferior se muestra el resultado del escaneo (Válido, Ya usado o Error).
*   **Ajustes:** Es una pantalla con diferentes preferencias que podrá aplicar el empleado sobre la aplicación, como cambiar el idioma, activar el modo oscuro o cerrar sesión de forma segura.

<div align="center">

```mermaid
graph TD
    Login[<b>Login</b><br/>Pantalla de acceso] -->|Validar datos| Eventos[<b>Eventos</b><br/>Lista de selección]
    Eventos -->|Seleccionar evento| Escaner[<b>Escáner</b><br/>Cámara y resultados]
    
    Eventos --> Ajustes[<b>Ajustes</b><br/>Preferencias]
    Escaner --> Ajustes
    
    Ajustes -.->|Cerrar sesión| Login

    style Login fill:#E1D5E7,stroke:#9673A6
    style Eventos fill:#D5E8D4,stroke:#82B366
    style Escaner fill:#D5E8D4,stroke:#82B366
    style Ajustes fill:#F5F5F5,stroke:#666666
```

</div>

---

# 5. Arquitectura

La app implementa **Clean Architecture** dividida en tres capas con dependencias unidireccionales: la presentación depende del dominio, y el dominio no conoce ni la presentación ni los datos.

```mermaid
classDiagram
    class TicketRepository {
        <<interface>>
        +authenticate(username, password)
        +logout()
        +getEvents() List~Event~
        +validateTicket(code, eventId, eventName) Ticket
        +checkInTicket(ticketId)
    }
    class TicketRepositoryImpl {
        -uid Int
        -password String
        +authenticate(username, password)
        +logout()
        +getEvents() List~Event~
        +validateTicket(code, eventId, eventName) Ticket
        +checkInTicket(ticketId)
    }
    class GetEvents {
        -repository TicketRepository
        +invoke() List~Event~
    }
    class ValidateTicket {
        -repository TicketRepository
        +invoke(code, eventId, eventName) Ticket
    }
    class CheckInTicket {
        -repository TicketRepository
        +invoke(ticketId)
    }
    class Ticket {
        +id Int
        +nombre String
        +cliente String
        +evento String
        +estado EstadoTicket
    }
    class EstadoTicket {
        <<enum>>
        OPEN
        DONE
        CANCELLED
        UNKNOWN
        +fromString(value) EstadoTicket
    }

    TicketRepositoryImpl ..|> TicketRepository
    GetEvents --> TicketRepository
    ValidateTicket --> TicketRepository
    CheckInTicket --> TicketRepository
    Ticket --> EstadoTicket
```

---

## 5.1. Estructura de paquetes

```mermaid
graph TD
    root[AppEntradas]:::rootStyle --> core
    root --> data
    root --> domain
    root --> injection
    root --> presentation

    classDef rootStyle fill:#534AB7,color:#fff,stroke-width:2px
    classDef default fill:#EEEDFE,color:#26215C,stroke:#534AB7
```

### 5.1.1 core

Contiene todo lo que es configuración global y valores que no cambian. Las constantes de conexión con Odoo, los nombres de los campos, los mensajes de error y las excepciones propias viven aquí para que cualquier parte de la app pueda acceder a ellos sin duplicar strings.

<div align="center">

```mermaid
graph TD
     core[core]:::layerStyle
     core --> c1[constants]
     core --> c2[exceptions]
     c1 --> c3[AppConstants.kt]:::folderStyle
     c2 --> c4[AppExceptions.kt]:::folderStyle
     
    classDef layerStyle fill:#534AB7,color:#fff,stroke:#3F3795,stroke-width:2px
    classDef folderStyle fill:white,color:#26215C,stroke:#534AB7,stroke-dasharray: 5 5
    classDef default fill:#EEEDFE,color:#26215C,stroke:#534AB7
```

</div>

### 5.1.2 domain

Es el núcleo de la app. No depende de Android, Odoo, ni ninguna librería externa. Contiene:

- **model** — los objetos con los que trabaja la app (`Event`, `Ticket`, `EstadoTicket`)
- **repository** — la interfaz `TicketRepository` que define las operaciones disponibles sin saber cómo se implementan
- **usecase** — las acciones de negocio (`GetEvents`, `ValidateTicket`, `CheckInTicket`), cada una con un único método `invoke`

<div align="center">

```mermaid
graph TD
    domain[domain]:::layerStyle
    
    domain --> model[model]
    domain --> repo[repository] 
    domain --> uc[usecase]
    model --> estado[EstadoTicket.kt]:::folderStyle
    model --> event[Event.kt]:::folderStyle
    model --> ticket[Ticket.kt]:::folderStyle

    repo --> ticketrepo[TicketRepository.kt]:::folderStyle

    uc --> cheking[CheckInTicket.kt]:::folderStyle
    uc --> getevent[GetEvents.kt]:::folderStyle
    uc --> valticket[ValidateTicket.kt]:::folderStyle

    classDef layerStyle fill:#534AB7,color:#fff,stroke:#3F3795,stroke-width:2px
    classDef folderStyle fill:white,color:#26215C,stroke:#534AB7,stroke-dasharray: 5 5
    classDef default fill:#FFFFFF,color:#26215C,stroke:#534AB7
```

</div>


### 5.1.3 data

Contiene `TicketRepositoryImpl`, la única clase que sabe cómo hablar con Odoo. Implementa la interfaz `TicketRepository` y gestiona la sesión XML-RPC. Guarda el `uid` y la `password` en memoria como variables privadas que se obtienen al autenticarse y se limpian al hacer logout.

<div align="center">

```mermaid
graph TD
    pres[data]:::layerStyle
    
    pres --> repository[repository]
    repository --> ticket[TicketRepositoryImpl]:::folderStyle

    classDef layerStyle fill:#534AB7,color:#fff,stroke:#3F3795,stroke-width:2px
    classDef folderStyle fill:white,color:#26215C,stroke:#534AB7,stroke-dasharray: 5 5
    classDef default fill:#EEEDFE,color:#26215C,stroke:#534AB7
```
</div>

### 5.1.4 injection

Contiene el módulo Hilt `AppModule` que provee el repositorio como singleton. Al ser singleton, se garantiza que solo existe una instancia del repositorio en toda la app, lo que es necesario porque guarda el estado de sesión en memoria.

<div align="center">

```mermaid
graph TD
    infra[Infraestructura]:::layerStyle
    infra --> inj[injection]
    inj --> i1[AppModule.kt]:::folderStyle

    classDef layerStyle fill:#534AB7,color:#fff,stroke:#3F3795,stroke-width:2px
    classDef folderStyle fill:white,color:#26215C,stroke:#534AB7,stroke-dasharray: 5 5
    classDef default fill:#EEEDFE,color:#26215C,stroke:#534AB7
```

</div>

### 5.1.5 presentation

Contiene los Fragments, ViewModels y Adapters de cada pantalla. Cada pantalla tiene su propio ViewModel que expone su estado mediante `LiveData`. Los Fragments observan ese estado y actualizan la UI. Ningún Fragment contiene lógica de negocio.

<div align="center">

```mermaid
graph TD
    pres[representation]:::layerStyle
    
    pres --> event[event]
    event --> eventadt[EventAdapter.kt]:::folderStyle
    event --> eventlist[EventListFragment.kt]:::folderStyle
    event --> eventview[EventViewAdapter.kt]:::folderStyle

    pres --> login[login]
    login --> loginfrag[EventListFragment.kt]:::folderStyle
    login --> loginview[EventViewAdapter.kt]:::folderStyle

    pres --> scanner[scanner]
    scanner --> scannerfrag[ScannerFragment.kt]:::folderStyle
    scanner --> scannerview[ScannerViewModel.kt]:::folderStyle

    pres --> setting[settings]
    setting --> settingsfrag[SettingsFragment.kt]:::folderStyle
    setting--> settingsview[SettingsViewModel.kt]:::folderStyle

    classDef layerStyle fill:#534AB7,color:#fff,stroke:#3F3795,stroke-width:2px
    classDef folderStyle fill:white,color:#26215C,stroke:#534AB7,stroke-dasharray: 5 5
    classDef default fill:#EEEDFE,color:#26215C,stroke:#534AB7
```

</div>

---

## 6. Comunicación con Odoo

La app usa **XML-RPC**, el protocolo estándar de integración de Odoo. Se usan dos endpoints:

| Endpoint | Uso |
|---|---|
| `/xmlrpc/2/common` | Autenticación de usuarios |
| `/xmlrpc/2/object` | Operaciones sobre modelos de datos |

Los modelos y métodos usados son:

| Modelo | Método | Descripción |
|---|---|---|
| `event.event` | `search_read` | Obtener eventos en estado "Announced" |
| `event.registration` | `search_read` | Buscar ticket por barcode y evento |
| `event.registration` | `action_set_done` | Marcar ticket como utilizado |

---

## 7. Gestión de errores

La app define excepciones propias que extienden `RuntimeException`:

| Excepción | Cuándo se lanza |
|---|---|
| `CredencialesInvalidasException` | Las credenciales introducidas no son válidas |
| `ConexionException` | Fallo en la comunicación con Odoo |
| `TicketNotFoundException` | No se encuentra el ticket en Odoo |

Los ViewModels siempre capturan por tipo, nunca por mensaje:

```kotlin
catch (e: CredencialesInvalidasException) {
    _state.postValue(Error(AppConstants.ERROR_CREDENCIALES))
} catch (e: ConexionException) {
    _state.postValue(Error(AppConstants.ERROR_CONEXION))
} catch (e: Exception) {
    _state.postValue(Error(AppConstants.ERROR_DESCONOCIDO))
}
```

---

## 8. Dependencias principales

| Librería | Versión | Uso |
|---|---|---|
| Hilt | 2.51.1 | Inyección de dependencias |
| Navigation Component | 2.8.4 | Navegación y SafeArgs |
| CameraX | 1.3.2 | Acceso a cámara |
| ML Kit Barcode Scanning | 17.2.0 | Lectura de QR |
| Coil | 2.6.0 | Carga de imágenes |
| Apache XML-RPC | 3.1.3 | Comunicación con Odoo |
| Lifecycle ViewModel | 2.7.0 | Gestión ciclo de vida |
| Coroutines | 1.7.3 | Programación asíncrona |
| JUnit 4 | — | Tests unitarios |
| Mockito Kotlin | 5.4.0 | Mocks en tests |
| Coroutines Test | 1.7.3 | Tests con corrutinas |

---

## 9. Configuración de la conexión

Para apuntar a otra instancia de Odoo modifica estas dos constantes en `AppConstants.kt`:

```kotlin
const val URL_ODOO = "https://edu-pruebaeventos.odoo.com"
const val DB_NAME  = "edu-pruebaeventos"
```

El usuario de Odoo necesita:
- Lectura sobre `event.event` y `event.registration`
- Ejecución de `action_set_done` sobre `event.registration`

---

## 10. Tests

La estrategia de tests cubre las capas de dominio y presentación con JUnit 4 y Mockito.

| Clase | Casos cubiertos |
|---|---|
| `EstadoTicketTest` | Conversión de cada string a su enum |
| `GetEventsTest` | Lista correcta, lista vacía, error |
| `ValidateTicketTest` | Ticket válido, null, error |
| `CheckInTicketTest` | Check-in correcto, error |
| `LoginViewModelTest` | Campos vacíos, credenciales inválidas, error conexión, éxito |
| `EventListViewModelTest` | Lista eventos, lista vacía, error |
| `ScannerViewModelTest` | Ticket válido, usado, cancelado, no encontrado, error, revalidación |
