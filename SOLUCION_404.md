# Solución al Error 404 en Tomcat

## Problema
Después de desplegar correctamente el WAR, al acceder a `http://localhost:8080/crm-backend-1.0-SNAPSHOT/api/clientes` obtienes error 404.

## Diagnóstico del Problema

El error 404 generalmente se debe a que el **Application Context** está configurado incorrectamente en IntelliJ. Necesitas verificar:

### Paso 1: Verificar el Application Context Real

1. En IntelliJ, ve a `Run` → `Edit Configurations...`
2. Selecciona tu configuración de Tomcat
3. Ve a la pestaña **Deployment**
4. Verifica qué dice en la columna **Application context** para tu artifact `crm-backend:war exploded`

**Posibles valores:**
- `/` → La aplicación está en la raíz, accede a: `http://localhost:8080/api/clientes`
- `/crm-backend-1.0-SNAPSHOT` → Accede a: `http://localhost:8080/crm-backend-1.0-SNAPSHOT/api/clientes`
- `/crm-backend` → Accede a: `http://localhost:8080/crm-backend/api/clientes`

### Paso 2: Verificar los Logs de Tomcat

Busca en los logs de Tomcat algo como:
```
INFO ... Desplegando el directorio [C:\...\crm-backend-1.0-SNAPSHOT] de la aplicación web
```

El nombre del directorio te indica el contexto.

### Paso 3: Verificar que los Servlets se Registraron

En los logs deberías ver algo como (busca en la consola):
```
INFO ... Servlet [ClienteServlet] fue registrado
```

Si no ves esto, los servlets no se están cargando.

## Soluciones

### Solución 1: Cambiar el Application Context en IntelliJ

1. Ve a `Run` → `Edit Configurations...`
2. Selecciona tu configuración de Tomcat
3. Ve a la pestaña **Deployment**
4. En la columna **Application context**, cambia el valor:
   - Si quieres usar `/` (raíz): escribe `/`
   - Si quieres usar un nombre específico: escribe `/crm-backend-1.0-SNAPSHOT` (o el que prefieras)
5. Haz clic en `Apply` y luego `OK`
6. Reinicia Tomcat

**Después de cambiar, prueba:**
- Si configuraste `/`: `http://localhost:8080/api/clientes`
- Si configuraste `/crm-backend-1.0-SNAPSHOT`: `http://localhost:8080/crm-backend-1.0-SNAPSHOT/api/clientes`

### Solución 2: Verificar en la Página de Tomcat Manager

1. Abre: `http://localhost:8080/manager/html`
2. Si te pide usuario/contraseña:
   - Usuario: `admin` o `tomcat`
   - Contraseña: la que configuraste en `tomcat-users.xml`
3. Busca tu aplicación en la lista
4. Verifica el nombre de la aplicación (ese es tu context path)

### Solución 3: Probar con la Raíz (más simple)

La forma más simple es configurar el Application Context como `/`:

1. En **Deployment**, cambia el Application context a `/`
2. Reinicia Tomcat
3. Accede directamente a: `http://localhost:8080/api/clientes`

### Solución 4: Verificar que los Servlets se Carguen

Si después de verificar el contexto aún obtienes 404, puede ser que los servlets no se estén registrando. Para diagnosticar:

1. Verifica que los archivos `.class` estén en `target/crm-backend-1.0-SNAPSHOT/WEB-INF/classes/org/david/crm/controller/`
2. Recompila el proyecto: `mvn clean compile`
3. Verifica que `web.xml` NO tenga `<metadata-complete>true</metadata-complete>` (si lo tiene, quítalo)
4. Reinicia Tomcat

## Verificación Rápida

Prueba estas URLs en orden:

1. `http://localhost:8080/api/clientes` (si el context es `/`)
2. `http://localhost:8080/crm-backend-1.0-SNAPSHOT/api/clientes` (si el context es `/crm-backend-1.0-SNAPSHOT`)
3. `http://localhost:8080/crm-backend/api/clientes` (si el context es `/crm-backend`)

## Otras URLs de Prueba

Si alguna de las URLs anteriores funciona, prueba estas otras:

- `/api/comerciales`
- `/api/productos`
- `/api/secciones`
- `/api/facturas`
- `/api/estadisticas`
- `/api/informes/*`

## Si Nada Funciona

1. Verifica que MySQL esté corriendo
2. Verifica las credenciales en `persistence.xml`
3. Revisa los logs de Tomcat para errores específicos
4. Asegúrate de que estás usando Tomcat 10+ (no Tomcat 9 o anterior)

