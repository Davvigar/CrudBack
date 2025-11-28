# Configuración de Tomcat en IntelliJ IDEA

## Requisitos Previos

1. ✅ Tu proyecto ya está configurado como **WAR** (`packaging>war</packaging>` en `pom.xml`)
2. ✅ Ya tienes la estructura `webapp/WEB-INF/` con `web.xml` y `beans.xml`
3. ✅ El proyecto usa Jakarta EE 10 (Tomcat 10+ requerido)

## Pasos para Configurar Tomcat en IntelliJ

### 1. Instalar el Plugin de Tomcat (si no está instalado)

1. Ve a `File` → `Settings` (o `Ctrl+Alt+S`)
2. Ve a `Plugins`
3. Busca "Tomcat and TomEE Integration"
4. Instálalo y reinicia IntelliJ

### 2. Descargar Tomcat 10+

- Descarga Tomcat 10.1.x o superior desde: https://tomcat.apache.org/
- Descomprime en una carpeta (ej: `C:\apache-tomcat-10.1.x`)

### 3. Configurar Tomcat en IntelliJ

1. Ve a `Run` → `Edit Configurations...` (o haz clic en la lista desplegable junto al botón Run)
2. Haz clic en el `+` (Add New Configuration)
3. Selecciona `Tomcat Server` → `Local`
4. Configura:
   - **Name**: `Tomcat 10 - CRM Backend`
   - **Application server**: Haz clic en `Configure...` y selecciona la carpeta de tu instalación de Tomcat
   - **Open browser**: Marca si quieres que abra el navegador automáticamente
   - **URL**: `http://localhost:8080/crm-backend-1.0-SNAPSHOT/` (o el nombre de tu WAR)

### 4. Configurar el Deployment (WAR Exploded)

1. En la misma ventana de configuración, ve a la pestaña **Deployment**
2. Haz clic en `+` → `Artifact...`
3. Selecciona: **`crm-backend:war exploded`**
   - ⚠️ **IMPORTANTE**: Selecciona la versión **exploded**, no el `.war` empaquetado
4. En **Application context**, escribe: `/crm-backend-1.0-SNAPSHOT` (o `/` si quieres la raíz)
5. Haz clic en `OK`

### 5. Configurar el Build

1. En la pestaña **Server**, verifica que:
   - **On 'Update' action**: `Update classes and resources`
   - **On frame deactivation**: `Update classes and resources`
2. Esto permite hot-reload durante el desarrollo

### 6. Asegúrate de que Maven esté configurado

1. Ve a `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven`
2. Verifica que Maven esté correctamente configurado

### 7. Compilar el Proyecto

Antes de ejecutar, compila el proyecto:
- `Build` → `Build Project` (o `Ctrl+F9`)
- O desde la terminal: `mvn clean compile`

### 8. Ejecutar

1. Selecciona la configuración "Tomcat 10 - CRM Backend" en la lista desplegable
2. Haz clic en el botón **Run** (▶️) o presiona `Shift+F10`

## Verificación

Una vez que Tomcat inicie, deberías poder acceder a:
- `http://localhost:8080/crm-backend-1.0-SNAPSHOT/api/clientes`
- `http://localhost:8080/crm-backend-1.0-SNAPSHOT/api/comerciales`
- etc.

## Solución de Problemas

### Error: "No artifacts marked for deployment"
- Ve a `File` → `Project Structure` (`Ctrl+Alt+Shift+S`)
- Ve a `Artifacts`
- Asegúrate de que existe `crm-backend:war exploded`
- Si no existe, haz clic en `+` → `Web Application: Exploded` → `From Modules...`
- Selecciona tu módulo Maven

### Error: "Artifact is not deployed"
- Asegúrate de que el packaging en `pom.xml` es `<packaging>war</packaging>`
- Compila el proyecto con `mvn clean compile`
- Reimporta el proyecto Maven: click derecho en `pom.xml` → `Maven` → `Reload Project`

### Error de CDI/Weld
- Verifica que `src/main/webapp/WEB-INF/beans.xml` existe
- Verifica que el listener de Weld está en `web.xml`

## Notas Importantes

- **WAR Exploded** es mejor para desarrollo porque permite hot-reload
- El **WAR empaquetado** (`.war`) es mejor para producción
- Asegúrate de que MySQL esté corriendo antes de iniciar Tomcat
- Verifica las credenciales de la base de datos en `persistence.xml`

