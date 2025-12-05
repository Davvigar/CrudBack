import requests
from datetime import datetime
from collections import defaultdict
from typing import Any

BASE_URL = "http://localhost:8080/api"

GLOBAL_SESSION = requests.Session()
GLOBAL_USER_INFO = {"logueado": False, "rol": None, "nombre": None}

def _limpiar_total_factura(total_str):
    try:
        if total_str is None:
            return 0.0
        total_limpio = str(total_str).replace('€', '').replace(',', '').strip()
        if not total_limpio:
            return 0.0
        return float(total_limpio)
    except (ValueError, AttributeError):
        return 0.0
        
def _normalizar_datos_desde_api(datos: Any) -> Any:
    if isinstance(datos, dict):
        new_dict = {}
        key_mapping = {
            'clienteId': 'cliente_id', 'comercialId': 'comercial_id', 'productoId': 'producto_id',
            'seccionId': 'seccion_id', 'facturaId': 'factura_id', 'passwordHash': 'password_hash',
            'fechaEmision': 'fecha_emision', 'totalIva': 'total_iva', 'precioBase': 'precio_base',
            'plazasDisponibles': 'plazas_disponibles',
        }
        
        for key, value in datos.items():
            if isinstance(value, dict) and key in ['cliente', 'comercial', 'producto', 'seccion']:
                nested_id_key_camel = key + 'Id' 
                nested_id_key_snake = key + '_id'
                nested_id = value.get(nested_id_key_camel) or value.get(nested_id_key_snake)
                if nested_id is not None:
                    new_dict[key + '_id'] = nested_id
            else:
                new_key = key_mapping.get(key, key)
                valor_normalizado = _normalizar_datos_desde_api(value)
                
                if isinstance(valor_normalizado, (int, float)) and key in ['total', 'subtotal', 'totalIva', 'total_iva', 'precioBase', 'precio_base']:
                    new_dict[new_key] = float(valor_normalizado)
                else:
                    new_dict[new_key] = valor_normalizado
        return new_dict
    elif isinstance(datos, list):
        return [_normalizar_datos_desde_api(item) for item in datos]
    return datos

def _manejar_peticion(metodo, endpoint, data = None, params = None):
    url = f"{BASE_URL}/{endpoint}"
    try:
        if metodo == 'GET':
            response = GLOBAL_SESSION.get(url, params=params)
        elif metodo == 'POST':
            response = GLOBAL_SESSION.post(url, json=data)
        elif metodo == 'PUT':
            response = GLOBAL_SESSION.put(url, json=data)
        elif metodo == 'DELETE':
            response = GLOBAL_SESSION.delete(url)
        else:
            raise ValueError(f"Método HTTP no soportado: {metodo}")
            
        response.raise_for_status()
        
        if response.text and response.status_code != 204:
            try:
                json_data = response.json()
                return _normalizar_datos_desde_api(json_data)
            except (ValueError, requests.exceptions.JSONDecodeError):
                return None
        
        return True
        
    except requests.exceptions.HTTPError:
        return None
    except (requests.exceptions.RequestException, ValueError):
        return None

#  FUNCIÓN DE LOGIN 
def login_autenticacion(username, password):
   
    global GLOBAL_USER_INFO
    endpoint = "login"
    url = f"{BASE_URL}/{endpoint}"
    
    datos_formulario = {"username": username, "password": password}
    
    GLOBAL_USER_INFO["logueado"] = False
    try:
        response = GLOBAL_SESSION.post(url, data=datos_formulario)
        
        if response.status_code == 200:
            respuesta_texto = response.text
            if ',' in respuesta_texto:
                rol, nombre = respuesta_texto.split(',', 1)
                
                GLOBAL_USER_INFO["logueado"] = True
                GLOBAL_USER_INFO["rol"] = rol.lower()
                GLOBAL_USER_INFO["nombre"] = nombre.strip()
                
                return {"username": username, "nombre": nombre.strip(), "rol": rol.lower()}
        
        return False
        
    except requests.exceptions.RequestException:
        return None
        
#  COMERCIALES (/api/comerciales) 

def obtener_comerciales():
    return _manejar_peticion('GET', 'comerciales') or []

def obtener_comercial_por_id(id):
    return _manejar_peticion('GET', f'comerciales/{id}')

def crear_comercial(datos):
    return _manejar_peticion('POST', 'comerciales', data=datos)

def actualizar_comercial(id, datos):
    return _manejar_peticion('PUT', f'comerciales/{id}', data=datos)

def eliminar_comercial(id):
    return _manejar_peticion('DELETE', f'comerciales/{id}') is True

#  CLIENTES (/api/clientes)

def obtener_clientes(comercial_id = None):
    params = {'comercialId': comercial_id} if comercial_id is not None else None
    return _manejar_peticion('GET', 'clientes', params=params) or []

def obtener_cliente_por_id(id):
    return _manejar_peticion('GET', f'clientes/{id}')

def crear_cliente(datos):
    return _manejar_peticion('POST', 'clientes', data=datos)

def actualizar_cliente(id, datos):
    return _manejar_peticion('PUT', f'clientes/{id}', data=datos)

def eliminar_cliente(id):
    return _manejar_peticion('DELETE', f'clientes/{id}') is True

#  SECCIONES (/api/secciones)

def obtener_secciones():
    return _manejar_peticion('GET', 'secciones') or []

def obtener_seccion_por_id(id):
    return _manejar_peticion('GET', f'secciones/{id}')

def crear_seccion(datos):
    return _manejar_peticion('POST', 'secciones', data=datos)

def actualizar_seccion(id, datos):
    return _manejar_peticion('PUT', f'secciones/{id}', data=datos)

def eliminar_seccion(id):
    return _manejar_peticion('DELETE', f'secciones/{id}') is True

# PRODUCTOS (/api/productos)

def obtener_productos(seccion_id = None):
    params = {'seccionId': seccion_id} if seccion_id is not None else None
    return _manejar_peticion('GET', 'productos', params=params) or []

def obtener_producto_por_id(id):
    return _manejar_peticion('GET', f'productos/{id}')

def crear_producto(datos):
    return _manejar_peticion('POST', 'productos', data=datos)

def actualizar_producto(id, datos):
    return _manejar_peticion('PUT', f'productos/{id}', data=datos)

def eliminar_producto(id):
    return _manejar_peticion('DELETE', f'productos/{id}') is True

# FACTURAS (/api/facturas)

def obtener_facturas(cliente_id = None, comercial_id = None):
    params = {}
    if cliente_id is not None: params['clienteId'] = cliente_id
    if comercial_id is not None: params['comercialId'] = comercial_id
    return _manejar_peticion('GET', 'facturas', params=params) or []

def obtener_factura_por_id(id):
    return _manejar_peticion('GET', f'facturas/{id}')

def crear_factura(datos):
    return _manejar_peticion('POST', 'facturas', data=datos)

def actualizar_factura(id, datos):
    return _manejar_peticion('PUT', f'facturas/{id}', data=datos)

def eliminar_factura(id):
    return _manejar_peticion('DELETE', f'facturas/{id}') is True

# INFORMES Y ESTADÍSTICAS

def arrancar_informe(tipo):
    if tipo not in ['clientes', 'facturas', 'completo']: return None
    return _manejar_peticion('GET', f'informes/{tipo}')

def obtener_estadisticas_api():
    resultado = _manejar_peticion('GET', 'estadisticas')
    if resultado is None:
        return {'totalRequests': 0, 'successfulRequests': 0, 'failedRequests': 0, 'averageResponseTime': 0.0}
    return resultado

def exportar_estadisticas(nombre_archivo = None):
    params = {'file': nombre_archivo} if nombre_archivo else None
    return _manejar_peticion('POST', 'estadisticas', params=params) is True

def resetear_estadisticas():
    resultado = _manejar_peticion('DELETE', f'estadisticas')
    # El servidor puede devolver True o un diccionario con el mensaje
    return resultado is not None

# Funciones de Dashboard 

def obtener_facturas_para_estadisticas(): return obtener_facturas()
def obtener_comerciales_para_estadisticas(): return obtener_comerciales()

def get_invoice_counts():
    facturas = obtener_facturas_para_estadisticas()
    counts = defaultdict(int)
    for factura in facturas: counts[factura.get('estado', 'desconocido')] += 1
    return {'pagada': counts.get('pagada', 0), 'pendiente': counts.get('pendiente', 0), 'cancelada': counts.get('cancelada', 0)}

def get_ingresos_mensuales():
    facturas = obtener_facturas_para_estadisticas()
    if not facturas:
        return [], []
    
    ingresos_por_mes = defaultdict(float)
    
    for factura in facturas:
        total_raw = factura.get('total', 0) #obtener total
        total = _limpiar_total_factura(total_raw)
        
        fecha_str = factura.get('fecha_emision')
        
        if not fecha_str or total <= 0:
            continue
        
        try:
            fecha_dt = None
            if isinstance(fecha_str, list):
                if len(fecha_str) >= 3:
                    año = fecha_str[0]
                    mes = fecha_str[1]
                    día = fecha_str[2]
                    hora = fecha_str[3] if len(fecha_str) > 3 else 0
                    minuto = fecha_str[4] if len(fecha_str) > 4 else 0
                    segundo = fecha_str[5] if len(fecha_str) > 5 else 0
                    fecha_dt = datetime(año, mes, día, hora, minuto, segundo)
            elif isinstance(fecha_str, str):
                if 'T' in fecha_str:
                    fecha_part = fecha_str.split('T')[0]
                    fecha_dt = datetime.strptime(fecha_part, "%Y-%m-%d")
                elif ' ' in fecha_str:
                    fecha_part = fecha_str.split()[0]
                    fecha_dt = datetime.strptime(fecha_part, "%Y-%m-%d")
                else:
                    fecha_dt = datetime.strptime(fecha_str, "%Y-%m-%d")
            
            if fecha_dt:
                mes = fecha_dt.strftime("%b %Y")
                ingresos_por_mes[mes] += total
                    
        except Exception:
            continue
    
    if not ingresos_por_mes:
        return [], []
    
    periodos_ordenados = sorted(ingresos_por_mes.keys(), key=lambda x: datetime.strptime(x, "%b %Y"))
    valores = [ingresos_por_mes[p] for p in periodos_ordenados]
    return periodos_ordenados, valores

def get_ranking_comerciales():
    comerciales = obtener_comerciales_para_estadisticas()
    facturas = obtener_facturas_para_estadisticas()
    nombres_comerciales = {c['comercial_id']: c['nombre'] for c in comerciales if 'comercial_id' in c}
    ingresos_por_id = defaultdict(float)
    for factura in facturas:
        comercial_id = factura.get('comercial_id')
        total = _limpiar_total_factura(str(factura.get('total', '0.00€')))
        if comercial_id in nombres_comerciales and total > 0:
            ingresos_por_id[comercial_id] += total
    ranking = []
    for c in comerciales:
        c_id = c.get('comercial_id')
        total = ingresos_por_id.get(c_id, 0.0)
        ranking.append({"nombre": c.get('nombre', "Desconocido"), "ingresos": total})
    ranking.sort(key=lambda x: x['ingresos'], reverse=True)
    return ranking

# FUNCIÓN CLIENTES POR COMERCIAL PARA ESTADISTICASS
def get_clientes_por_comercial():
    comerciales = obtener_comerciales_para_estadisticas()
    clientes = obtener_clientes()

    clientes_count_por_id = defaultdict(int)
    for cliente in clientes:
        comercial_id = cliente.get('comercial_id')
        if comercial_id:
            clientes_count_por_id[comercial_id] += 1

    ranking_clientes = []
    for c in comerciales:
        c_id = c.get('comercial_id')
        count = clientes_count_por_id.get(c_id, 0)
        ranking_clientes.append({"nombre": c.get('nombre', "Desconocido"), "clientes": count})
    
    ranking_clientes.sort(key=lambda x: x['clientes'], reverse=True)
    return ranking_clientes

# FUNCIÓN: PRODUCTOS MÁS VENDIDOS
def get_productos_mas_vendidos():
    facturas = obtener_facturas_para_estadisticas()
    productos = obtener_productos()
    
    ventas_por_producto = defaultdict(int)
    for factura in facturas:
        producto_id = factura.get('producto_id')
        if producto_id:
            ventas_por_producto[producto_id] += 1
    
    nombres_productos = {p.get('producto_id'): p.get('nombre', 'Desconocido') 
                        for p in productos if 'producto_id' in p}
    
    ranking_productos = []
    for producto_id, cantidad in ventas_por_producto.items():
        nombre = nombres_productos.get(producto_id, f"Producto {producto_id}")
        ranking_productos.append({"nombre": nombre, "ventas": cantidad})
    
    ranking_productos.sort(key=lambda x: x['ventas'], reverse=True)
    return ranking_productos[:10]  # Top 10

# FUNCIÓN: INGRESOS POR SECCIÓN
def get_ingresos_por_seccion():
    facturas = obtener_facturas_para_estadisticas()
    productos = obtener_productos()
    secciones = obtener_secciones()
    
    producto_a_seccion = {p.get('producto_id'): p.get('seccion_id') 
                         for p in productos if 'producto_id' in p}
    
    seccion_nombres = {s.get('seccion_id'): s.get('nombre', 'Desconocida')
                      for s in secciones if 'seccion_id' in s}
    
    ingresos_por_seccion = defaultdict(float)
    for factura in facturas:
        producto_id = factura.get('producto_id')
        seccion_id = producto_a_seccion.get(producto_id)
        if seccion_id:
            total = _limpiar_total_factura(factura.get('total', 0))
            ingresos_por_seccion[seccion_id] += total
    
    resultado = []
    for seccion_id, total in ingresos_por_seccion.items():
        nombre = seccion_nombres.get(seccion_id, f"Sección {seccion_id}")
        resultado.append({"nombre": nombre, "ingresos": total})
    
    resultado.sort(key=lambda x: x['ingresos'], reverse=True)
    return resultado