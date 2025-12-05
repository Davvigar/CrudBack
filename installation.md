# Instalación y puesta en marcha

Guía concisa para levantar base de datos, backend y frontend.

## Requisitos
- Java 21+ y Maven 3.6+
- MySQL 8.0+
- Tomcat 10+ (u otro contenedor Jakarta EE 10)
- Python 3.11+ y pip

## 1) Base de datos
```bash
mysql -u root -p < database_setup.sql
```
Verifica credenciales en `src/main/resources/META-INF/persistence.xml` (`jakarta.persistence.jdbc.user/password`).

## 2) Backend
```bash
mvn clean package
```
Desplegar el WAR:
```bash
cp target/crm-backend-1.0-SNAPSHOT.war $CATALINA_HOME/webapps/crm.war
$CATALINA_HOME/bin/startup.sh   # startup.bat en Windows
```
API: `http://localhost:8080/crm/api/`

## 3) Frontend (escritorio)
```bash
cd frontend/FrontEnd
pip install -r requirements.txt
python main.py
```
Consume la API en `http://localhost:8080/crm/api/`. Si cambias host/puerto, ajusta `frontend/FrontEnd/api/api_client.py`.

## Notas rápidas
- Arranca MySQL antes del backend.
- Para redeploy: `mvn clean package` y copia del WAR de nuevo.
- Si usas otro contenedor EE10, despliega el WAR según su CLI/console.

