# Solución al Error "Session/EntityManager is closed"

## Diagnóstico

Si sigues obteniendo el error `Session/EntityManager is closed`, verifica:

### 1. Verifica que el filtro esté configurado
El `TransactionFilter` debe estar definido en `web.xml`. Revisa que el archivo contenga:
```xml
<filter>
    <filter-name>TransactionFilter</filter-name>
    <filter-class>org.david.crm.filter.TransactionFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>TransactionFilter</filter-name>
    <url-pattern>/api/*</url-pattern>
</filter-mapping>
```

### 2. Verifica los logs de Tomcat
Busca mensajes que indiquen que el filtro se está ejecutando. Si no aparece nada, el filtro no se está cargando.

### 3. Recompila y reinicia
1. Haz `Build` → `Rebuild Project` en IntelliJ
2. O desde Maven: `mvn clean compile`
3. Reinicia Tomcat completamente (detén y vuelve a iniciar)

### 4. Verifica el orden de filtros
Los filtros con `@WebFilter` (como `RateLimitFilter`) pueden ejecutarse en cualquier orden.
El `TransactionFilter` debe ejecutarse PRIMERO porque está definido en `web.xml`.

### 5. Si aún falla
Verifica que no haya errores de compilación o que el filtro esté siendo cargado correctamente.

## Solución alternativa: Verificar que el filtro se ejecute

Si el filtro no se está ejecutando, puede ser porque:
- Hay un error en la clase que impide su carga
- El `web.xml` no se está cargando correctamente
- Hay conflictos con otros filtros

## Próximos pasos

Si después de verificar todo lo anterior sigue fallando, comparte:
1. Los logs completos de Tomcat al iniciar
2. Cualquier error en la consola de IntelliJ
3. La traza completa del error

