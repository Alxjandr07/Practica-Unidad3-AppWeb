# ADR-001: Selección de Patrón Arquitectónico — Monolito por Capas

## Estado

Aprobado (2026-07-16)

## Contexto

El Proyecto Fin de Curso (PFC) requiere una arquitectura de software para la gestión
de vehículos de una cooperativa de transporte. El equipo está compuesto por 3
estudiantes de la carrera de Software en la UTEQ, quienes deben entregar un sistema
funcional que demuestre las competencias de la Unidad III de la materia Aplicaciones
Web [111]: ORM, caché, patrones arquitectónicos y seguridad stateless.

Los requisitos funcionales incluyen: autenticación JWT con blacklist en Redis,
CRUD completo de vehículos con paginación y filtrado dinámico, y caché de segundo
nivel con Redis. Los requisitos no funcionales priorizan la velocidad de desarrollo,
la mantenibilidad y la simplicidad operacional dada la escala del proyecto (un solo
equipo de 3 personas desplegando en un entorno local/Docker).

El equipo debe elegir entre: (a) Monolito por Capas, (b) Arquitectura Hexagonal,
(c) Microservicios, o (d) Clean Architecture.

## Decisión

Implementaremos una **arquitectura en capas (Layered Architecture)** con la siguiente
estratificación vertical:

```
Controller (REST API)
    ↓
Service (Lógica de negocio + caché)
    ↓
Repository (Acceso a datos JPA)
    ↓
Entity (Modelo de dominio)
```

Los principios que guían esta decisión son:

1. **Separación de responsabilidades por capa**: Cada capa tiene una única
   responsabilidad. Los Controllers solo reciben y validan peticiones HTTP;
   los Services orquestan la lógica de negocio; los Repositories abstraen
   el acceso a JPA/Hibernate.

2. **Dependencia unidireccional**: Las dependencias fluyen únicamente hacia
   abajo (Controller → Service → Repository → Entity). Ninguna capa inferior
   conoce las superiores.

3. **DTOs como frontera de datos**: Se emplean registros Java (records) como
   VehiculoRequest y VehiculoResponse para desacoplar el contrato API del
   modelo de persistencia, evitando la fuga de entidades JPA al exterior.

4. **Alineación con el stack tecnológico**: Spring Boot está diseñado
   nativamente para arquitectura por capas, con anotaciones como @Service,
   @Repository, @RestController que refuerzan la separación e incrementan
   la cohesión del código (Johnson, 2003).

## Consecuencias

**Positivas:**
- Curva de aprendizaje mínima para el equipo: la estructura de paquetes
  (controller, service, repository, domain, dto, config, security) es
  intuitiva y alineada con la convención de Spring Boot.
- Desarrollo paralelo eficiente: cada integrante puede trabajar en una
  capa sin generar conflictos de合并 frecuentes.
- Testing por capas: se pueden escribir tests unitarios aislados por
  capa (Mockito en Service, @DataJpaTest en Repository).
- Despliegue simple: un único artefacto JAR ejecutable con Docker.

**Negativas:**
- Acoplamiento de implementación: cambios en el esquema de base de datos
  pueden afectar Repository, Service y Controller simultáneamente.
- Escalado limitado: si el sistema requiriera escalar un módulo
  independientemente (ej: notificaciones), el monolito fuerza escalar
  todo el sistema.
- Riesgo de "God Service": sin disciplina, los Services pueden acumular
  lógica excesiva. Se mitiga manteniendo los métodos con una única
  responsabilidad y delegando al Repository o a clases utilitarias
  (VehiculoSpecifications).

## Alternativas Consideradas

| Alternativa | Ventaja | Razón de rechazo |
|---|---|---|
| **Microservicios** | Escalado independiente, despliegue aislado | Complejidad operacional excesiva para un equipo de 3 personas. Requiere service discovery, API gateway, distribución de datos transaccionales. Las "falacias de los sistemas distribuidos" (Rotem-Gal-Oz, 2012) multiplican el esfuerzo. |
| **Arquitectura Hexagonal (Puertos y Adaptadores)** | Desacoplamiento total del framework, testabilidad extrema | Curva de aprendizaje elevada para un proyecto académico. La abstracción innecesaria de puertos para un CRUD REST incrementa la cantidad de código sin beneficio proporcional. |
| **Clean Architecture** | Independencia total del framework, reglas de negocio puras | Over-engineering para el alcance del PFC. Requeriría múltiples capas de abstracción (Use Cases, Entities, Interface Adapters) que duplican la estructura sin justificación funcional. |

## Referencias

- Buschmann, F. et al. (1996). *Pattern-Oriented Software Architecture, Volume 1:
  A System of Patterns*. John Wiley & Sons.
- Johnson, R. (2003). *Expert One-on-One J2EE Design and Development*. Wrox Press.
- Rotem-Gal-Oz, A. (2012). *Fallacies of Distributed Computing Explained*.
  https://web.archive.org/web/20170809113938/http://www.rgoarchitects.com/Files/fallacies.pdf
