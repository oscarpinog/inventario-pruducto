README del Proyecto de Microservicios

Este proyecto implementa una arquitectura de microservicios utilizando Spring Boot, Docker y PostgreSQL para gestionar productos e inventario.



🚀 Tecnologías

El proyecto ha sido desarrollado utilizando las siguientes tecnologías y herramientas:



Java 21: Lenguaje de programación principal.



Spring Boot 3.2.x: Framework para el desarrollo rápido de aplicaciones Java.



Lombok: Librería para reducir el código repetitivo (boilerplate code).



PostgreSQL: Base de datos relacional robusta.



Spring Data JPA / Hibernate: Para la persistencia de datos.



Maven: Herramienta de gestión de proyectos y construcción.



Docker / Docker Compose: Para la orquestación y despliegue de los microservicios y la base de datos.



RestTemplate: Para la comunicación entre microservicios.



Swagger / OpenAPI 3: Para la documentación interactiva de las APIs.



SLF4J: Para el manejo de logs.



⚙️ Instalación

Sigue estos pasos para configurar y ejecutar el proyecto en tu entorno local.



Prerrequisitos

Asegúrate de tener instalado lo siguiente:



Java Development Kit (JDK) 21



Maven



Git



Docker y Docker Compose



Pasos de Instalación

Clonar el Repositorio:

Descarga el código fuente del repositorio Git.



git clone <URL\_DEL\_REPOSITORIO>

cd <nombre\_del\_repositorio>



(Reemplaza <URL\_DEL\_REPOSITORIO> con la URL real de tu repositorio y <nombre\_del\_repositorio> con el nombre de la carpeta del proyecto.)



Actualizar Dependencias de Maven:

Una vez clonado, es recomendable realizar un Maven update en cada proyecto de microservicio (productos-service e inventario-service) desde tu IDE o ejecutar mvn clean install en la raíz de cada subproyecto para asegurar que todas las dependencias estén descargadas.



Construir y Levantar los Servicios con Docker Compose:

Desde la raíz del proyecto (donde se encuentra el archivo docker-compose.yml), ejecuta el siguiente comando para construir las imágenes de Docker y levantar todos los servicios:



docker compose up --build



Este comando construirá las imágenes de Docker para productos-app e inventario-app (utilizando los Dockerfiles respectivos) y levantará los contenedores de la base de datos PostgreSQL, productos-app e inventario-app.



Limpieza de Contenedores e Imágenes (Opcional)

Si necesitas detener y eliminar los contenedores y las imágenes creadas por Docker Compose, puedes usar los siguientes comandos:



docker compose down # Detiene y elimina los contenedores y redes

docker rm inventario-app productos-app postgres-db # Elimina contenedores por nombre si no se eliminaron con 'down'

docker rmi inventario-producto-inventario-app inventario-producto-productos-app postgres:15 # Elimina las imágenes



🏛️ Estructura del Proyecto

El proyecto está compuesto por dos microservicios principales:



productos-service: Encargado de la gestión de productos.



inventario-service: Encargado de la gestión del inventario, con comunicación con productos-service.



Cada microservicio contiene su propio Dockerfile y está configurado para generar un JAR ejecutable que puede ser desplegado directamente con Docker Compose.



🗄️ Configuración de la Base de Datos

El proyecto utiliza una base de datos PostgreSQL configurada a través de Docker Compose:



Nombre de la Base de Datos: appdb



Usuario: admin



Contraseña: admin123



Puerto Externo: 5432 (mapeado al puerto interno 5432 del contenedor postgres-db).



Volumen de Datos: postgres\_data para persistir los datos de la base de datos.



Las credenciales de la base de datos se inyectan en los servicios de Spring Boot a través de variables de entorno en el docker-compose.yml.



🔐 Seguridad (API Key)

Para la comunicación entre servicios y el acceso a las APIs, se utilizan API Keys:



productos-app: API\_KEY: producto



inventario-app: API\_KEY: inventario



La comunicación de inventario-app a productos-app usa la API\_KEY de productos-app.



📞 Comunicación entre Servicios

La comunicación entre el servicio de inventario y el servicio de productos se realiza mediante RestTemplate. Se utilizan headers para pasar la API\_KEY desde el servicio de inventario hacia el servicio de productos para autenticación y autorización.



PRODUCTOS\_API\_BASE\_URL: http://productos-app:8080/api (URL interna del servicio de productos dentro de la red Docker).



PRODUCTOS\_SERVICE\_API\_KEY: producto (La API Key esperada por el servicio de productos).



📄 Documentación API (Swagger / OpenAPI)

Ambos microservicios están documentados con Swagger/OpenAPI, lo que permite una exploración interactiva de sus endpoints:



API de Productos: http://localhost:8080/swagger-ui/index.html



API de Inventario: http://localhost:8081/swagger-ui/index.html



🧪 Pruebas Unitarias

El proyecto cuenta con una cobertura de pruebas unitarias mayor al 80% en las capas de controller y service.impl para ambos proyectos (inventario y producto), asegurando la calidad y el correcto funcionamiento de la lógica de negocio.



✨ Patrones y Buenas Prácticas

Se han implementado diversas buenas prácticas y patrones de diseño:



Patrón Builder: Utilizado para la construcción de objetos DTO de manera más legible y segura (ej. ProductoDto, InventarioDto).



Manejo de Excepciones: Implementación de manejo de excepciones centralizado con @ControllerAdvice para respuestas HTTP consistentes.



Logs: Uso de slf4j para un registro de información estructurado y efectivo.



Variables de Entorno: Configuración flexible de parámetros sensibles (como credenciales de base de datos y API keys) a través de variables de entorno, mejorando la seguridad y la portabilidad.



JSON API Wrappers: Creación de clases personalizadas (JsonApiData, JsonApiWrapper) para simular el formato de respuesta JSON API, proporcionando una estructura de respuesta más controlada y consistente.



Justificación de Arquitectura

Se optó por una arquitectura de tipo monolito distribuido con base de datos compartida debido a su simplicidad y facilidad de implementación en etapas tempranas del proyecto. Ambas APIs Java acceden a la misma base de datos para gestionar la información centralizada, lo cual permite un desarrollo más ágil y evita duplicidad de datos. Aunque no sigue el enfoque clásico de microservicios, esta decisión es válida considerando el contexto actual del sistema. Para mantener la integridad y coordinación entre módulos, se implementará comunicación entre los servicios mediante RestTemplate, especialmente para validar productos desde el microservicio de inventario.



Justificación Base de datos SQL - PostgreSQL

Se eligió una base de datos SQL porque el proyecto de productos e inventario requiere consistencia fuerte en las transacciones, alineándose con el teorema CAP, donde se prioriza la consistencia (C) sobre la disponibilidad (A) o la tolerancia a particiones (P). En un sistema donde el stock de productos debe reflejar fielmente cada compra o movimiento, es fundamental evitar condiciones de carrera o desincronización. Las bases de datos relacionales, como PostgreSQL o MySQL, ofrecen transacciones ACID que garantizan integridad y coherencia en todo momento, lo cual es clave para este tipo de operaciones críticas.



➕ Adicional



Ejemplo de Objeto Producto

Un ejemplo de la estructura de un objeto Producto para las operaciones de la API:



{

&nbsp; "id": 0,

&nbsp; "nombre": "ZAPATOS",

&nbsp; "descripcion": "TIPO NIKE",

&nbsp; "precio": 1000

}

