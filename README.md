📦 Microservicio de Ventas - Grupo Cordillera
Este microservicio es el corazón transaccional de la Plataforma de Monitoreo Cordillera. Se encarga de procesar las ventas, validar la existencia de productos y sucursales, y coordinar con el microservicio de inventario bajo un modelo de alta disponibilidad y resiliencia.

🛠️ Herramientas Necesarias (Pre-requisitos)
Para ejecutar este proyecto de forma exitosa en tu entorno local, debes contar con el siguiente "Kit de Desarrollo":

1. Entorno de Ejecución y Construcción
   Java JDK 17: Es el lenguaje base. Asegúrate de tener configurada la variable de entorno JAVA_HOME.

Apache Maven 3.8+: Se encarga de descargar las librerías (Spring, Resilience4j, OpenFeign) y construir el archivo .jar.

2. Gestión de Datos
   PostgreSQL 14+: Motor de base de datos donde se almacenan las ventas.

pgAdmin 4 o DBeaver: Herramientas gráficas para que puedas ver las tablas, ejecutar SQL y verificar que los datos se guardaron correctamente.

3. Entorno de Desarrollo (IDE)
   IntelliJ IDEA (Recomendado): Es el mejor para Spring Boot. También puedes usar VS Code con las extensiones de "Spring Boot Extension Pack".

4. Herramientas de Pruebas y Monitoreo
   Postman: Indispensable. La usaremos para enviar peticiones JSON al microservicio y simular las compras del frontend.

Navegador Web (Chrome/Edge): Para acceder a los endpoints de consulta rápida y ver los JSON de respuesta.

🏗️ Configuración del Entorno
Paso 1: Base de Datos
Abre pgAdmin 4.

Crea un nuevo servidor (si no tienes uno) y una base de datos llamada db_ventas.

El microservicio creará las tablas automáticamente al iniciar gracias a hibernate.ddl-auto=update.

Paso 2: Configuración del Proyecto
En el archivo src/main/resources/application.properties, ajusta tus credenciales de PostgreSQL:

Properties
spring.datasource.url=jdbc:postgresql://localhost:5432/db_ventas
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
🚀 Ejecución del Microservicio
Abre el proyecto en IntelliJ IDEA.

Espera a que Maven descargue las dependencias (verás una barra de progreso abajo a la derecha).

Busca la clase VentasApplication.java.

Haz clic derecho y selecciona "Run 'VentasApplication'".

Verifica en la consola que diga: Started VentasApplication in X seconds (JVM running for X). El puerto por defecto es el 8081.

🧪 Guía de Pruebas con Postman
Para probar que todo funciona, abre Postman y crea una nueva petición:

1. Crear una Venta (Simulación de compra)
   Método: POST

URL: http://localhost:8081/api/ventas

Body: Selecciona raw y tipo JSON.

Contenido:

JSON
{
"productoId": 1,
"sucursalId": 1,
"cantidad": 2,
"origen": "WEB",
"montoTotal": 45000
}
Nota: Si el micro de Stock (8085) está apagado, verás cómo el Circuit Breaker entra en acción y te devuelve un mensaje de error controlado en lugar de un error 500 de Java.

2. Consultar Historial de Ventas
   Método: GET

URL: http://localhost:8081/api/ventas

Resultado: Deberías ver una lista con todas las ventas que has realizado, incluyendo el ID de transacción generado por la base de datos.

🛡️ Notas de Arquitectura (Explicación para la entrega)
Circuit Breaker: Si el microservicio de Stock falla, este micro de Ventas no se "cae". Usa un Fallback que devuelve un error amigable.

Feign Client: No usamos URLs quemadas de forma ruda; usamos interfaces limpias para hablar con los otros servicios.

Integración: Recuerda que para que el Frontend vea los nombres de los productos, los microservicios de Productos (8082) y Sucursales (8084) deben estar encendidos