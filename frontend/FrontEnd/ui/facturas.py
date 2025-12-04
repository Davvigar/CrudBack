from customtkinter import CTkFrame, CTkButton, CTkEntry, CTkLabel 
import tkinter.messagebox as tk_messagebox
import re 
from datetime import datetime 

from components.data_table import DataTable
from components.modal_form import ModalForm
from components.detail_view import DetailView
from api import api_client

# --- FUNCIONES DE VALIDACIÓN ---
def validar_id_factura(valor):
    if not valor: return "El ID de factura es obligatorio.", False
    return "✅", True
def validar_id_entidad(valor):
    if not valor: return "El ID es obligatorio.", False
    try:
        if int(valor) <= 0: return "Debe ser un número positivo.", False
        return "✅", True
    except ValueError: return "Debe ser un número entero.", False
def validar_total(valor):
    if not valor: return "El Total es obligatorio.", False
    try:
        total = float(str(valor).replace(',', '.')) 
        if total <= 0: return "El total debe ser positivo.", False
        return "✅", True
    except ValueError: return "Debe ser un número válido (decimales permitidos).", False

# --- VISTA CON CRUD DE FACTURAS ---

class VistaFacturas(CTkFrame):
    def __init__(self, maestro, **kwargs):
        super().__init__(maestro, **kwargs)
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1) 
        self.id_seleccionado = None 
        self.factura_en_edicion = None

        self.marco_control = CTkFrame(self, fg_color="transparent")
        self.marco_control.grid(row=0, column=0, padx=10, pady=(10, 5), sticky="new")

        CTkButton(self.marco_control, text="Nuevo (C)", command=self._abrir_modal_crear_factura).pack(side="right", padx=5)
        CTkButton(self.marco_control, text="Recargar", command=self.cargar_datos_factura).pack(side="right", padx=5)

        columnas_factura = ["factura_id", "cliente_id", "comercial_id", "fecha_emision", "estado", "total"]
        self.tabla_datos = DataTable(self, columnas=columnas_factura, 
                                     al_seleccionar_item=self.al_seleccionar_fila,
                                     al_doble_clic=self._mostrar_detalles_factura)
        self.tabla_datos.grid(row=1, column=0, padx=10, pady=5, sticky="nsew")
        
        self.cargar_datos_factura()
        
        self.marco_accion = CTkFrame(self, fg_color="transparent")
        self.marco_accion.grid(row=2, column=0, padx=10, pady=5, sticky="se")
        
        CTkButton(self.marco_accion, text="Editar (U)", command=self._abrir_modal_editar_factura).pack(side="right", padx=5)
        
        CTkButton(self.marco_accion, text="Eliminar (D)", fg_color="red", 
                 hover_color="#AA0000", command=self._confirmar_y_eliminar).pack(side="right", padx=5)
        
    def cargar_datos_factura(self):
        datos = api_client.obtener_facturas()
        if datos is None:
              tk_messagebox.showerror("Error de Conexión", "No se pudieron obtener las facturas. Verifique el servidor REST.")
              self.tabla_datos.actualizar_datos([])
        else:
              self.tabla_datos.actualizar_datos(datos)

    def al_seleccionar_fila(self, id_factura):
        self.id_seleccionado = str(id_factura)
    
    def _mostrar_detalles_factura(self, factura_data):
        DetailView(self.master, f"Detalles de Factura: {factura_data.get('factura_id', 'N/A')}", 
                  factura_data)
        
    def _obtener_clientes_para_modal(self) -> list:
        clientes = api_client.obtener_clientes()
        if not clientes:
            return ["No hay clientes disponibles"]
        
        opciones = []
        self.clientes_map = {} 
        
        for c in clientes:
            cliente_id = c.get('cliente_id')
            nombre = c.get('nombre', '')
            apellidos = c.get('apellidos', '')
            
            if cliente_id is not None:
                display_name = f"ID {cliente_id}: {nombre} {apellidos}"
                opciones.append(display_name)
                self.clientes_map[display_name] = cliente_id
        
        return opciones
    
    def _obtener_comerciales_para_modal(self) -> list:
        comerciales = api_client.obtener_comerciales()
        if not comerciales:
            return ["No hay comerciales disponibles"]
        
        opciones = []
        self.comerciales_map = {} 
        
        for c in comerciales:
            comercial_id = c.get('comercial_id')
            nombre = c.get('nombre', '')
            
            if comercial_id is not None:
                display_name = f"ID {comercial_id}: {nombre}"
                opciones.append(display_name)
                self.comerciales_map[display_name] = comercial_id
        
        return opciones
    
    def _obtener_productos_para_modal(self) -> list:
        productos = api_client.obtener_productos()
        if not productos:
            return ["No hay productos disponibles"]
        
        opciones = []
        self.productos_map = {} 
        self.productos_precios = {} 
        
        for p in productos:
            producto_id = p.get('producto_id')
            nombre = p.get('nombre', '')
            try:
                precio = float(str(p.get('precio_base', 0.0)).replace(',', '.'))
            except ValueError:
                precio = 0.0
            
            if producto_id is not None:
                display_name = f"ID {producto_id}: {nombre} ({precio:.2f} €)"
                opciones.append(display_name)
                self.productos_map[display_name] = producto_id
                self.productos_precios[producto_id] = precio
        
        return opciones
    
    def _get_factura_fields(self, is_edit=False):
        opciones_clientes = self._obtener_clientes_para_modal()
        opciones_comerciales = self._obtener_comerciales_para_modal()
        opciones_productos = self._obtener_productos_para_modal()
        
        fields = [
            {
                'label': 'Cliente:',
                'options': opciones_clientes,
                'key': 'cliente_display_name'
            },
            {
                'label': 'Comercial:',
                'options': opciones_comerciales,
                'key': 'comercial_display_name'
            },
            {'label': 'Total (€):', 'validator': validar_total, 'key': 'total'},
        ]
        
        if not is_edit:
            fields.insert(0, {'label': 'ID Factura:', 'validator': validar_id_factura, 'key': 'factura_id'})
            fields.insert(3, {
                'label': 'Producto:',
                'options': opciones_productos,
                'key': 'producto_display_name'
            })
            
        return fields

    def _abrir_modal_crear_factura(self):
        ModalForm(self.master, 
                  title="Crear Nueva Factura", 
                  fields_config=self._get_factura_fields(is_edit=False), 
                  action_callback=self._crear_factura_y_guardar)

    def _abrir_modal_editar_factura(self):
        if self.id_seleccionado is None:
            tk_messagebox.showwarning("Advertencia", "Selecciona una factura de la tabla para editar.")
            return

        try:
            datos_api = api_client.obtener_factura_por_id(self.id_seleccionado)
            
            if isinstance(datos_api, list) and len(datos_api) > 0:
                datos_actuales = datos_api[0]
            elif isinstance(datos_api, dict):
                datos_actuales = datos_api
            else:
                 raise Exception("Factura no encontrada o formato de respuesta inválido.")
            
            self.factura_en_edicion = datos_actuales
            
            initial_data = datos_actuales.copy()
            
            self._obtener_clientes_para_modal()
            self._obtener_comerciales_para_modal()
            
            cliente_id = datos_actuales.get('cliente_id') or (datos_actuales.get('cliente', {}).get('cliente_id') if isinstance(datos_actuales.get('cliente'), dict) else None)
            if cliente_id:
                for display_name, cid in self.clientes_map.items():
                    if cid == cliente_id:
                        initial_data['cliente_display_name'] = display_name
                        break
            
            comercial_id = datos_actuales.get('comercial_id') or (datos_actuales.get('comercial', {}).get('comercial_id') if isinstance(datos_actuales.get('comercial'), dict) else None)
            if comercial_id:
                for display_name, cid in self.comerciales_map.items():
                    if cid == comercial_id:
                        initial_data['comercial_display_name'] = display_name
                        break

            ModalForm(self.master, 
                      title=f"Editar Factura ID: {self.id_seleccionado}", 
                      fields_config=self._get_factura_fields(is_edit=True), 
                      initial_data=initial_data, 
                      action_callback=self._actualizar_factura_y_guardar)
            
        except Exception as e:
            tk_messagebox.showerror("Error", f"No se pudo cargar la factura: {e}")


    def _crear_factura_y_guardar(self, data):
        try:
            factura_data = {
                'facturaId': data.get('factura_id', ''),
                'estado': 'pendiente',
                'fechaEmision': datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),  # Formato ISO para LocalDateTime
            }
            
            if 'cliente_display_name' in data and data['cliente_display_name']:
                cliente_display = data['cliente_display_name']
                cliente_id = self.clientes_map.get(cliente_display)
                if cliente_id:
                    factura_data['cliente'] = {'clienteId': int(cliente_id)}
            
            if 'comercial_display_name' in data and data['comercial_display_name']:
                comercial_display = data['comercial_display_name']
                comercial_id = self.comerciales_map.get(comercial_display)
                if comercial_id:
                    factura_data['comercial'] = {'comercialId': int(comercial_id)}
            
            if 'producto_display_name' in data and data['producto_display_name']:
                producto_display = data['producto_display_name']
                producto_id = self.productos_map.get(producto_display)
                if producto_id:
                    factura_data['producto'] = {'productoId': int(producto_id)}
                    if 'total' not in data or not data['total']:
                        precio = self.productos_precios.get(producto_id, 0.0)
                        factura_data['total'] = precio
                        factura_data['subtotal'] = round(precio / 1.21, 2)
                        factura_data['totalIva'] = round(precio - factura_data['subtotal'], 2)
            
            if 'total' in data and data['total']:
                total = float(str(data['total']).replace(',', '.'))
                factura_data['total'] = total
                factura_data['subtotal'] = round(total / 1.21, 2)
                factura_data['totalIva'] = round(total - factura_data['subtotal'], 2)
            
            resultado = api_client.crear_factura(factura_data) 
            
            if resultado is not None and resultado is not False:
                tk_messagebox.showinfo("Éxito", f"Factura '{data.get('factura_id', '')}' creada correctamente.")
                self.cargar_datos_factura() 
                return True
            else:
                tk_messagebox.showerror("Error de API", "El servidor rechazó la petición POST. Revise la consola.")
                return False
        except Exception as e:
            tk_messagebox.showerror("Error de API", f"Fallo al guardar la factura: {e}")
            return False

    def _actualizar_factura_y_guardar(self, data):
        if not self.factura_en_edicion:
            tk_messagebox.showerror("Error", "Error interno: Objeto de edición no cargado.")
            return False

        try:
            factura_previo = self.factura_en_edicion
            
            factura_data = {}
            
            if not hasattr(self, 'clientes_map'):
                self._obtener_clientes_para_modal()
            if not hasattr(self, 'comerciales_map'):
                self._obtener_comerciales_para_modal()
            
            if 'cliente_display_name' in data and data['cliente_display_name']:
                cliente_display = data['cliente_display_name']
                cliente_id = self.clientes_map.get(cliente_display)
                if cliente_id:
                    factura_data['cliente'] = {'clienteId': int(cliente_id)}
            else:
                cliente_id = factura_previo.get('clienteId') or factura_previo.get('cliente_id')
                if cliente_id:
                    if isinstance(cliente_id, dict):
                        cliente_id = cliente_id.get('clienteId', cliente_id.get('cliente_id'))
                    factura_data['cliente'] = {'clienteId': int(cliente_id)}
            
            if 'comercial_display_name' in data and data['comercial_display_name']:
                comercial_display = data['comercial_display_name']
                comercial_id = self.comerciales_map.get(comercial_display)
                if comercial_id:
                    factura_data['comercial'] = {'comercialId': int(comercial_id)}
            else:
                comercial_id = factura_previo.get('comercialId') or factura_previo.get('comercial_id')
                if comercial_id:
                    if isinstance(comercial_id, dict):
                        comercial_id = comercial_id.get('comercialId', comercial_id.get('comercial_id'))
                    factura_data['comercial'] = {'comercialId': int(comercial_id)}
            
            producto_id = factura_previo.get('productoId') or factura_previo.get('producto_id')
            if producto_id:
                if isinstance(producto_id, dict):
                    producto_id = producto_id.get('productoId', producto_id.get('producto_id'))
                factura_data['producto'] = {'productoId': int(producto_id)}
            
            if 'total' in data and data['total']:
                total = float(str(data['total']).replace(',', '.'))
                factura_data['total'] = total
                factura_data['subtotal'] = round(total / 1.21, 2)
                factura_data['totalIva'] = round(total - factura_data['subtotal'], 2)
            
            factura_data['facturaId'] = factura_previo.get('facturaId') or factura_previo.get('factura_id') or self.id_seleccionado
            factura_data['estado'] = factura_previo.get('estado', 'pendiente')
            factura_data['fechaEmision'] = factura_previo.get('fechaEmision') or factura_previo.get('fecha_emision')
            if 'version' in factura_previo:
                factura_data['version'] = factura_previo.get('version')
            
            if api_client.actualizar_factura(self.id_seleccionado, factura_data):
                tk_messagebox.showinfo("Éxito", f"Factura ID {self.id_seleccionado} actualizada correctamente.")
                self.cargar_datos_factura() 
                return True
            else:
                tk_messagebox.showerror("Error de API", "El servidor rechazó la petición PUT. Revise la consola.")
                return False
                
        except Exception as e:
            tk_messagebox.showerror("Error de API", f"Fallo al actualizar la factura: {e}")
            return False

    def _confirmar_y_eliminar(self):
        if self.id_seleccionado is None:
            tk_messagebox.showwarning("Advertencia", "Selecciona una factura de la tabla para eliminar.")
            return

        confirmar = tk_messagebox.askyesno(
            title="Confirmar Eliminación", 
            message=f"¿Está seguro de que desea eliminar la factura con ID: {self.id_seleccionado}?"
        )

        if confirmar:
            try:
                if api_client.eliminar_factura(self.id_seleccionado):
                    tk_messagebox.showinfo("Éxito", f"Factura ID {self.id_seleccionado} eliminada.")
                    self.id_seleccionado = None 
                    self.cargar_datos_factura() 
                else:
                    tk_messagebox.showerror("Error de API", "El servidor rechazó la eliminación. Revise la consola.")
            except Exception as e:
                tk_messagebox.showerror("Error", f"No se pudo eliminar la factura. {e}")