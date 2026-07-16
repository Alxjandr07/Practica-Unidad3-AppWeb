# ADR-002: Selección de Framework para el Frontend — Angular vs React

## Estado

Aprobado (2026-07-16)

## Contexto

El PFC requiere un frontend单页应用 (SPA) que consuma la API REST del backend
Spring Boot. Los requisitos funcionales del cliente incluyen: formulario de login,
listado paginado de vehículos con filtros dinámicos, formularios de creación/edición
con validación, y confirmación de logout (invalidación de token).

El equipo debe seleccionar entre los frameworks principales del ecosistema JavaScript
actual, evaluando factores como: compatibilidad con el stack existente (Java/Spring),
curva de aprendizaje del equipo, madurez del ecosistema, soporte para Angular 17+
standalone components y TypeScript ecosistema.

## Decisión

El equipo selecciona **Angular 17+** como framework frontend para el PFC.

Los criterios de evaluación cuantitativos son:

| Criterio | Angular 17+ | React 18+ | Vue 3+ |
|---|---|---|---|
| Tipo | Framework integral | Biblioteca + ecosistema | Framework progresivo |
| Lenguaje | TypeScript nativo | JavaScript/TS opt-in | TypeScript opcional |
| Inyección de dependencias | Sistema DI nativo | Context API / Bibliotecas | provide/inject |
| Formularios reactivos | FormsModule + Validators | React Hook Form / Formik | VeeValidate |
| Componentes standalone | ✅ desde v15 | ✅ (funciones) | ✅ (SFC) |
| Router integrado | ✅ @angular/router | ❌ (React Router, 3ra party) | ✅ vue-router |
| HttpClient tipado | ✅ HttpClient | ❌ (fetch/axios, 3ra party) | ✅ axios |
| CLI oficial | ✅ Angular CLI | ❌ (Create React App deprecado) | ✅ create-vue |
| Testing integrado | ✅ Karma + Jasmine / Jest | ❌ (Jest, 3ra party) | ❌ (Vitest, 3ra party) |

**Justificación técnica:**

1. **Alineación con el backend**: Angular está construido en TypeScript de forma
   nativa, lo que permite compartit contratos de datos (interfaces DTO) entre
   frontend y backend con mínima fricción. Los records Java del backend
   (VehiculoRequest, VehiculoResponse) mapean directamente a interfaces TypeScript.

2. **Angular CLI y estructura de proyecto**: Angular CLI genera una estructura
   de proyecto estandarizada con tests, linting, y build optimizado desde el
   primer `ng new`. Esto reduce la configuración manual y alinea con la
   convención de "convention over configuration" que el equipo ya aplica en
   Spring Boot (Johnson, 2003).

3. **Formularios reactivos**: Angular提供了 un sistema robusto de formularios
   reactivos con validación declarativa, que se alinea directamente con la
   validación Jakarta Validation del backend (Bean Validation). Cada campo del
   formulario puede tener validadores que reflejan las restricciones del
   VehiculoRequest (@NotBlank, @Size, @Min).

4. **Dependency Injection nativo**: El sistema de DI de Angular permite
   inyectar servicios HTTP, guards de autenticación y interceptores de
   cookies HttpOnly de forma declarativa, manteniendo una arquitectura
   limpia por componentes.

5. **Madurez y soporte a largo plazo**: Angular tiene soporte LTS de Google
   con ciclos de actualización predecibles (6 meses), lo que garantiza
   estabilidad para el proyecto.

## Consecuencias

**Positivas:**
- El equipo puede definir interfaces TypeScript que espejen los DTOs del
  backend, garantizando type-safety end-to-end.
- Angular incluye router, HTTP client, forms y testing out-of-the-box,
  eliminando la necesidad de evaluar y seleccionar bibliotecas de terceros.
- Los interceptores HTTP de Angular facilitan el envío automático de cookies
  HttpOnly y el manejo de respuestas 401 para redirigir al login.

**Negativas:**
- Curva de aprendizaje inicial más pronunciada que React para conceptos
  como módulos, inyección de dependencias y RxJS (observables).
- Bundle size mayor que React para aplicaciones pequeñas (aunque
  con Ivy y tree-shaking se ha reducido significativamente).
- Menor flexibilidad en la arquitectura interna: Angular impone una
  estructura más rígida comparada con React.

## Alternativas Consideradas

| Alternativa | Ventaja | Razón de rechazo |
|---|---|---|
| **React 18+** | Ecosistema más grande, menor bundle size, mayor flexibilidad, más ofertas laborales | No incluye router, forms, HTTP client ni testing de forma nativa. Requiere evaluar y combinar múltiples bibliotecas de terceros (React Router, React Hook Form, Axios, Testing Library). La ausencia de un CLI oficial (Create React App está deprecado) genera inconsistencia en la estructura del proyecto. |
| **Vue 3+** | Curva de aprendizaje suave, composición API elegante | Ecosistema más pequeño que Angular/React para enterprise. VeeValidate y Vue Router son soluciones de terceros. Menor disponibilidad de talento en el mercado ecuatoriano comparado con Angular (Stack Overflow Developer Survey, 2024). |
| **Next.js (React SSR)** | Server-side rendering, SEO optimizado | Complejidad innecesaria: el PFC es una SPA interna, no requiere SEO. Agrega la complejidad de Node.js al stack de despliegue. |

## Referencias

- Stack Overflow. (2024). *Stack Overflow Developer Survey 2024: Technology*.
  https://survey.stackoverflow.co/2024/
- Google. (2024). *Angular v17 Documentation — Standalone Components*.
  https://angular.io/guide/standalone-components
- Johnson, R. (2003). *Expert One-on-One J2EE Design and Development*. Wrox Press.
