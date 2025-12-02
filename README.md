# CRM Backend - API REST con Jakarta EE

Backend del sistema CRM para multinacional de formación profesional desarrollado con Jakarta EE, JPA y MySQL. Proporciona una API REST para gestionar comerciales, clientes (estudiantes), secciones (áreas de formación), productos (cursos de grados superiores) y facturas (matrículas).

## Requisitos Previos

- Java 21 o superior
- Maven 3.6 o superior
- MySQL 8.0 o superior
- Servidor de aplicaciones compatible con Jakarta EE 10 (Tomcat 10+, GlassFish 7+, etc.)

## Configuración de la Base de Datos

1. Asegúrate de que MySQL esté ejecutándose
2. Ejecuta el script SQL para crear la base de datos con datos de ejemplo:
   ```bash
   mysql -u root -p < database_setup.sql
   ```
   
   O desde MySQL Workbench o cualquier cliente MySQL, ejecuta el contenido del archivo `database_setup.sql`

### Script SQL de Inicialización

El archivo `database_setup.sql` contiene:
- Creación de la base de datos `crudProject`
- Creación de todas las tablas con sus relaciones:
  - `secciones` (4 áreas de formación: Salud, Emergencias, Business, Tech)
  - `comerciales` (5 comerciales, incluyendo 1 administrador)
  - `clientes` (10 estudiantes potenciales)
  - `productos` (23 cursos de grados superiores distribuidos en las áreas)
  - `facturas` (15 matrículas de ejemplo con estados variados)
- Datos de ejemplo medianamente completos para probar la aplicación

**Cursos incluidos:**
- **Salud**: Enfermería, Farmacia, Técnico de Rayos, Dietética, Higiene Bucodental, Anatomía Patológica, Laboratorio Clínico
- **Emergencias**: Emergencias Sanitarias, Protección Civil, Coordinación de Emergencias, Prevención de Riesgos
- **Business**: Administración y Finanzas, Comercio Internacional, Marketing, Gestión de Ventas, Asistencia a la Dirección, Transporte y Logística
- **Tech**: DAM, ASIR, DAW, Ciberseguridad, IA y Big Data, Animación 3D y Videojuegos

**Nota:** El script elimina las tablas existentes si ya existen, así que úsalo con precaución en producción.

## Configuración del Proyecto

### 1. Configurar la conexión a la base de datos

Edita el archivo `src/main/resources/META-INF/persistence.xml` y ajusta las siguientes propiedades según tu configuración:

```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/crudProject?useSSL=false&amp;serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="root"/>
<property name="jakarta.persistence.jdbc.password" value="tu_contraseña"/>
```

### 2. Compilar el proyecto

```bash
mvn clean compile
```

### 3. Empaquetar el proyecto

```bash
mvn clean package
```

Esto generará un archivo WAR en `target/crm-backend-1.0-SNAPSHOT.war`

## Ejecución

### Opción 1: Tomcat 10+

1. Descarga e instala Tomcat 10 o superior
2. Copia el archivo WAR a la carpeta `webapps` de Tomcat:
   ```bash
   cp target/crm-backend-1.0-SNAPSHOT.war $CATALINA_HOME/webapps/crm.war
   ```
3. Inicia Tomcat:
   ```bash
   $CATALINA_HOME/bin/startup.sh  # Linux/Mac
   $CATALINA_HOME/bin/startup.bat # Windows
   ```
4. La API estará disponible en: `http://localhost:8080/crm/api/`

### Opción 2: GlassFish 7+

1. Descarga e instala GlassFish 7 o superior
2. Inicia GlassFish:
   ```bash
   asadmin start-domain
   ```
3. Despliega la aplicación:
   ```bash
   asadmin deploy target/crm-backend-1.0-SNAPSHOT.war
   ```
4. La API estará disponible en: `http://localhost:8080/crm-backend-1.0-SNAPSHOT/api/`

### Opción 3: Maven con plugin de Tomcat (desarrollo)

```bash
mvn clean package
mvn org.apache.tomcat.maven:tomcat7-maven-plugin:run
```

## Endpoints de la API

### Comerciales

- `GET /api/comerciales` - Listar todos los comerciales
- `GET /api/comerciales/{id}` - Obtener un comercial por ID
- `POST /api/comerciales` - Crear un nuevo comercial
- `PUT /api/comerciales/{id}` - Actualizar un comercial
- `DELETE /api/comerciales/{id}` - Eliminar un comercial

### Clientes

- `GET /api/clientes` - Listar todos los clientes
- `GET /api/clientes?comercialId={id}` - Listar clientes por comercial
- `GET /api/clientes/{id}` - Obtener un cliente por ID
- `POST /api/clientes` - Crear un nuevo cliente
- `PUT /api/clientes/{id}` - Actualizar un cliente
- `DELETE /api/clientes/{id}` - Eliminar un cliente

### Secciones

- `GET /api/secciones` - Listar todas las secciones
- `GET /api/secciones/{id}` - Obtener una sección por ID
- `POST /api/secciones` - Crear una nueva sección
- `PUT /api/secciones/{id}` - Actualizar una sección
- `DELETE /api/secciones/{id}` - Eliminar una sección

### Productos

- `GET /api/productos` - Listar todos los productos
- `GET /api/productos?seccionId={id}` - Listar productos por sección
- `GET /api/productos/{id}` - Obtener un producto por ID
- `POST /api/productos` - Crear un nuevo producto
- `PUT /api/productos/{id}` - Actualizar un producto
- `DELETE /api/productos/{id}` - Eliminar un producto

### Facturas

- `GET /api/facturas` - Listar todas las facturas
- `GET /api/facturas?clienteId={id}` - Listar facturas por cliente
- `GET /api/facturas?comercialId={id}` - Listar facturas por comercial
- `GET /api/facturas/{id}` - Obtener una factura por ID
- `POST /api/facturas` - Crear una nueva factura
- `PUT /api/facturas/{id}` - Actualizar una factura
- `DELETE /api/facturas/{id}` - Eliminar una factura

## Formato de Datos

Todas las peticiones y respuestas utilizan JSON. Ejemplo de creación de un comercial:

```json
{
  "username": "comercial1",
  "passwordHash": "hash123",
  "nombre": "Juan",
  "email": "juan@example.com",
  "telefono": "123456789",
  "rol": "comercial"
}
```

Ejemplo de creación de un cliente:

```json
{
  "username": "cliente1",
  "passwordHash": "hash123",
  "nombre": "María",
  "apellidos": "García",
  "edad": 30,
  "email": "maria@example.com",
  "telefono": "987654321",
  "direccion": "Calle Principal 123",
  "comercial": {
    "comercialId": 1
  }
}
```

Ejemplo de creación de una factura:

```json
{
  "facturaId": "FAC-2024-001",
  "cliente": {
    "clienteId": 1
  },
  "comercial": {
    "comercialId": 1
  },
  "producto": {
    "productoId": 1
  },
  "estado": "pendiente",
  "subtotal": 100.00,
  "totalIva": 21.00,
  "total": 121.00
}
```

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   └── org/david/crm/
│   │       ├── config/          # Configuración (EntityManagerProducer)
│   │       ├── controller/      # Servlets (endpoints REST)
│   │       ├── filter/          # Filtros (rate limiting, estadísticas)
│   │       ├── model/           # Entidades JPA
│   │       ├── repository/      # Capa de repositorios
│   │       ├── service/         # Capa de servicios
│   │       └── util/            # Utilidades (JsonUtil)
│   ├── resources/
│   │   └── META-INF/
│   │       └── persistence.xml  # Configuración JPA
│   └── webapp/
│       └── WEB-INF/
│           └── web.xml          # Configuración web
└── test/
```

## Tecnologías Utilizadas

- **Jakarta EE 10**: API Servlet, CDI, JPA
- **Hibernate 6.4**: Implementación de JPA
- **MySQL Connector**: Driver para MySQL
- **Jackson**: Serialización/deserialización JSON
- **Maven**: Gestión de dependencias y construcción

## Notas Importantes

- Las transacciones se gestionan mediante `@Transactional` en los servicios
- Los repositorios utilizan `Optional` para manejar valores nulos de forma segura
- La aplicación utiliza CDI para la inyección de dependencias
- Se implementa programación funcional con Streams y lambdas donde es apropiado

## Funcionalidades de Concurrencia y Paralelismo

Este proyecto implementa técnicas avanzadas de concurrencia y paralelismo:

### Características Implementadas

- **ExecutorService**: Generación de informes asíncrona con pool de hilos
- **CompletableFuture**: Procesamiento paralelo de múltiples informes
- **Threads dedicados**: 
  - `ClienteReportThread` (extends Thread) para informes
  - Hilo con clase anónima en `RateLimiter` para limpiar ventanas
  - Hilo con lambda en `ApiStatistics` para exportar estadísticas
- **Recursos Atómicos**: AtomicInteger y AtomicLong para contadores thread-safe
- **Rate Limiting**: Control de 100 peticiones por minuto por cliente
- **Locking Optimista**: Prevención de Lost Updates con `@Version` en entidades
- **Logging Asíncrono**: Escritura de logs en segundo plano
- **Sincronización**: Uso de `synchronized` y `ReentrantLock` para secciones críticas

### Endpoints de Concurrencia

- `GET /api/informes/clientes` - Genera informe de clientes en segundo plano
- `GET /api/informes/facturas` - Genera informe de facturas en segundo plano
- `GET /api/informes/completo` - Genera informe completo en paralelo
- `GET /api/estadisticas` - Obtiene estadísticas de la API (recursos atómicos)
- `DELETE /api/estadisticas` - Resetea las estadísticas
- `POST /api/estadisticas?file=archivo.txt` - Exporta estadísticas usando un hilo creado con lambda

### Documentación Técnica

Ver `DOCUMENTACION_CONCURRENCIA.md` para documentación detallada sobre:
- Diseño de concurrencia
- Diagramas de flujo
- Problemas resueltos (Lost Updates, Race Conditions, Rate Limiting)
- Impacto en el rendimiento

## Solución de Problemas

### Error de conexión a la base de datos

- Verifica que MySQL esté ejecutándose
- Comprueba las credenciales en `persistence.xml`
- Asegúrate de que la base de datos `crudProject` existe

### Error 404 en los endpoints

- Verifica que la aplicación se haya desplegado correctamente
- Comprueba la URL base (puede variar según el servidor)
- Revisa los logs del servidor de aplicaciones

### Errores de compilación

- Asegúrate de tener Java 21 instalado
- Ejecuta `mvn clean install` para descargar todas las dependencias
- Verifica que todas las dependencias estén correctamente definidas en `pom.xml`

## Desarrollo

Para desarrollo local, puedes usar:

```bash
mvn clean compile
mvn org.apache.tomcat.maven:tomcat7-maven-plugin:run
```

O configurar tu IDE (IntelliJ IDEA, Eclipse) para ejecutar en modo debug.

