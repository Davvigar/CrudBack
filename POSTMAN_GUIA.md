# Guía de Pruebas en Postman - CRM Backend API

## Configuración Base

**URL Base:** `http://localhost:8080` (o `http://localhost:8080/crm-backend-1.0-SNAPSHOT` según tu Application Context)

**Headers necesarios:**
- `Content-Type: application/json` (para POST y PUT)

---

## 📋 ENDPOINTS DISPONIBLES

### 1. 👥 CLIENTES (`/api/clientes`)

#### GET - Listar todos los clientes
```
GET http://localhost:8080/api/clientes
```
**Respuesta:** Lista de todos los clientes (JSON array)

#### GET - Buscar cliente por ID
```
GET http://localhost:8080/api/clientes/{id}
```
**Ejemplo:** `GET http://localhost:8080/api/clientes/1`

#### GET - Buscar clientes por comercialId
```
GET http://localhost:8080/api/clientes?comercialId={id}
```
**Ejemplo:** `GET http://localhost:8080/api/clientes?comercialId=1`

#### POST - Crear nuevo cliente
```
POST http://localhost:8080/api/clientes
Content-Type: application/json

{
  "username": "juan_perez",
  "passwordHash": "hash123",
  "nombre": "Juan",
  "apellidos": "Pérez",
  "edad": 30,
  "email": "juan@example.com",
  "telefono": "123456789",
  "direccion": "Calle Principal 123",
  "comercial": {
    "comercialId": 1
  }
}
```

#### PUT - Actualizar cliente
```
PUT http://localhost:8080/api/clientes/{id}
Content-Type: application/json

{
  "username": "juan_perez_updated",
  "nombre": "Juan Carlos",
  "apellidos": "Pérez García",
  "edad": 31,
  "email": "juan.carlos@example.com"
}
```

#### DELETE - Eliminar cliente
```
DELETE http://localhost:8080/api/clientes/{id}
```
**Ejemplo:** `DELETE http://localhost:8080/api/clientes/1`

---

### 2. 💼 COMERCIALES (`/api/comerciales`)

#### GET - Listar todos los comerciales
```
GET http://localhost:8080/api/comerciales
```

#### GET - Buscar comercial por ID
```
GET http://localhost:8080/api/comerciales/{id}
```

#### POST - Crear nuevo comercial
```
POST http://localhost:8080/api/comerciales
Content-Type: application/json

{
  "username": "comercial1",
  "passwordHash": "hash456",
  "nombre": "María",
  "email": "maria@example.com",
  "telefono": "987654321",
  "rol": "comercial"
}
```

#### PUT - Actualizar comercial
```
PUT http://localhost:8080/api/comerciales/{id}
Content-Type: application/json

{
  "nombre": "María García",
  "email": "maria.garcia@example.com"
}
```

#### DELETE - Eliminar comercial
```
DELETE http://localhost:8080/api/comerciales/{id}
```

---

### 3. 📦 PRODUCTOS (`/api/productos`)

#### GET - Listar todos los productos
```
GET http://localhost:8080/api/productos
```

#### GET - Buscar producto por ID
```
GET http://localhost:8080/api/productos/{id}
```

#### GET - Buscar productos por seccionId
```
GET http://localhost:8080/api/productos?seccionId={id}
```

#### POST - Crear nuevo producto
```
POST http://localhost:8080/api/productos
Content-Type: application/json

{
  "nombre": "Producto Test",
  "descripcion": "Descripción del producto",
  "precioBase": 99.99,
  "plazasDisponibles": 10,
  "seccion": {
    "seccionId": 1
  }
}
```

#### PUT - Actualizar producto
```
PUT http://localhost:8080/api/productos/{id}
Content-Type: application/json

{
  "nombre": "Producto Actualizado",
  "precioBase": 149.99,
  "plazasDisponibles": 5
}
```

#### DELETE - Eliminar producto
```
DELETE http://localhost:8080/api/productos/{id}
```

---

### 4. 📁 SECCIONES (`/api/secciones`)

#### GET - Listar todas las secciones
```
GET http://localhost:8080/api/secciones
```

#### GET - Buscar sección por ID
```
GET http://localhost:8080/api/secciones/{id}
```

#### POST - Crear nueva sección
```
POST http://localhost:8080/api/secciones
Content-Type: application/json

{
  "nombre": "Electrónica"
}
```

#### PUT - Actualizar sección
```
PUT http://localhost:8080/api/secciones/{id}
Content-Type: application/json

{
  "nombre": "Electrónica y Tecnología"
}
```

#### DELETE - Eliminar sección
```
DELETE http://localhost:8080/api/secciones/{id}
```

---

### 5. 🧾 FACTURAS (`/api/facturas`)

#### GET - Listar todas las facturas
```
GET http://localhost:8080/api/facturas
```

#### GET - Buscar factura por ID
```
GET http://localhost:8080/api/facturas/{id}
```
**Nota:** El ID de factura es String, no Integer

#### GET - Buscar facturas por clienteId
```
GET http://localhost:8080/api/facturas?clienteId={id}
```

#### GET - Buscar facturas por comercialId
```
GET http://localhost:8080/api/facturas?comercialId={id}
```

#### POST - Crear nueva factura
```
POST http://localhost:8080/api/facturas
Content-Type: application/json

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

#### PUT - Actualizar factura
```
PUT http://localhost:8080/api/facturas/{id}
Content-Type: application/json

{
  "estado": "pagada",
  "total": 121.00
}
```

#### DELETE - Eliminar factura
```
DELETE http://localhost:8080/api/facturas/{id}
```

---

### 6. 📊 ESTADÍSTICAS (`/api/estadisticas`)

#### GET - Obtener estadísticas
```
GET http://localhost:8080/api/estadisticas
```
**Respuesta:**
```json
{
  "totalRequests": 150,
  "successfulRequests": 140,
  "failedRequests": 10,
  "logsWritten": 145,
  "averageResponseTime": 125.50
}
```

#### DELETE - Resetear estadísticas
```
DELETE http://localhost:8080/api/estadisticas
```

#### POST - Exportar estadísticas
```
POST http://localhost:8080/api/estadisticas?file=mis_estadisticas.txt
```

---

### 7. 📄 INFORMES (`/api/informes`)

**Nota:** Estos endpoints generan informes de forma asíncrona. La respuesta inmediata indica que el proceso ha iniciado.

#### GET - Generar informe de clientes
```
GET http://localhost:8080/api/informes/clientes
```
**Respuesta:** `{"message": "Generando informe de clientes en segundo plano...", "status": "processing"}`

#### GET - Generar informe de facturas
```
GET http://localhost:8080/api/informes/facturas
```
**Respuesta:** `{"message": "Generando informe de facturas en segundo plano...", "status": "processing"}`

#### GET - Generar informe completo
```
GET http://localhost:8080/api/informes/completo
```
**Respuesta:** `{"message": "Generando informe completo en segundo plano...", "status": "processing"}`

---

## 🚀 EJEMPLOS DE USO EN POSTMAN

### Configurar una colección en Postman:

1. **Crear nueva colección:** "CRM Backend API"

2. **Variables de colección:**
   - `base_url`: `http://localhost:8080` (o tu URL con context)
   - `base_url_with_context`: `http://localhost:8080/crm-backend-1.0-SNAPSHOT` (si aplica)

3. **Usar las variables:**
   ```
   {{base_url}}/api/clientes
   ```

### Estructura recomendada de carpetas:

```
📁 CRM Backend API
  📁 Clientes
    📄 GET - Listar clientes
    📄 GET - Obtener cliente por ID
    📄 POST - Crear cliente
    📄 PUT - Actualizar cliente
    📄 DELETE - Eliminar cliente
  📁 Comerciales
    ...
  📁 Productos
    ...
  📁 Secciones
    ...
  📁 Facturas
    ...
  📁 Estadísticas
    ...
  📁 Informes
    ...
```

---

## 📝 NOTAS IMPORTANTES

1. **CORS:** Asegúrate de que el filtro CORS esté configurado si pruebas desde un navegador.

2. **Rate Limiting:** Hay un límite de 100 peticiones por minuto por cliente.

3. **Version (Optimistic Locking):** Las entidades tienen un campo `version` que se maneja automáticamente. No lo incluyas en las peticiones POST.

4. **Relaciones:** Para crear relaciones, solo necesitas enviar el objeto con el ID:
   ```json
   {
     "comercial": {
       "comercialId": 1
     }
   }
   ```

5. **IDs:**
   - Clientes, Comerciales, Productos, Secciones: `Integer`
   - Facturas: `String`

---

## 🧪 EJEMPLO COMPLETO: Crear un Cliente con Comercial

### Paso 1: Obtener un comercial existente
```
GET http://localhost:8080/api/comerciales
```
**Respuesta:** Anota el `comercialId` (ej: 1)

### Paso 2: Crear un cliente asociado a ese comercial
```
POST http://localhost:8080/api/clientes
Content-Type: application/json

{
  "username": "test_cliente",
  "passwordHash": "test123",
  "nombre": "Test",
  "apellidos": "Cliente",
  "edad": 25,
  "email": "test@example.com",
  "telefono": "555-1234",
  "direccion": "Calle Test 456",
  "comercial": {
    "comercialId": 1
  }
}
```

### Paso 3: Verificar que se creó
```
GET http://localhost:8080/api/clientes
```

---

## ✅ CÓDIGOS DE RESPUESTA

- `200 OK` - Operación exitosa
- `201 Created` - Recurso creado exitosamente (POST)
- `400 Bad Request` - Error en los datos enviados
- `404 Not Found` - Recurso no encontrado
- `500 Internal Server Error` - Error del servidor

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Error 404
- Verifica que Tomcat esté corriendo
- Verifica el Application Context en IntelliJ
- Prueba primero: `http://localhost:8080/test.html`

### Error de conexión
- Verifica que MySQL esté corriendo
- Verifica las credenciales en `persistence.xml`

### Error de formato JSON
- Asegúrate de que el header `Content-Type: application/json` esté configurado
- Valida el JSON antes de enviarlo (usa un validador JSON online)

