// Proyecto Gradle del backend: agrupa los dos microservicios.
// Cada uno es una app Spring Boot independiente con su propio puerto y su propia base de datos.
rootProject.name = "alarma-zaror-backend"

include("ms-usuarios")
include("ms-alertas")
