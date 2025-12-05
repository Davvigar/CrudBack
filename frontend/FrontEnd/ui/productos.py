from customtkinter import CTkFrame, CTkButton, CTkEntry, CTkFont
import tkinter.messagebox as tk_messagebox
import re
from typing import Optional, Dict, Any
from api import api_client

from components.data_table import DataTable
from components.modal_form import ModalForm
from components.detail_view import DetailView 

def validar_nombre_producto(valor):
    if not valor: return "El nombre es obligatorio.", False
    if len(valor) < 3: return "Mínimo 3 caracteres.", False
    return "✅", True

def validar_precio_base(valor):
    if not valor: return "El precio base es obligatorio.", False
    try:
        precio = float(str(valor).replace(',', '.'))
        if precio <= 0: return "El precio debe ser positivo.", False
        return "✅", True
    except ValueError:
        return "Debe ser un número decimal válido.", False

def validar_plazas(valor):
    if not valor: return "La cantidad es obligatoria.", False
    try:
        plazas = int(valor)
        if plazas < 0: return "No puede ser negativo.", False
        return "✅", True
    except ValueError:
        return "Debe ser un número entero.", False

def validar_seccion_id(valor):
    if not valor: return "ID de Sección es obligatorio.", False
    try:
        if int(valor) <= 0: return "Debe ser un ID positivo.", False
        return "✅", True
    except ValueError:
        return "Debe ser un número entero.", False


class VistaProductos(CTkFrame):
    
    def __init__(self, maestro, **kwargs):
        super().__init__(maestro, **kwargs)
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1) 
        
        self.id_seleccionado: Optional[int] = None 
        self.producto_en_edicion: Optional[Dict[str, Any]] = None
        self.secciones_map = {}

        self._inicializar_controles()
        self.cargar_datos_producto()
        
    def _inicializar_controles(self):
        
        self.marco_control = CTkFrame(self, fg_color="transparent")
        self.marco_control.grid(row=0, column=0, padx=10, pady=(10, 5), sticky="new")

        # Solo admin puede crear productos
        if api_client.GLOBAL_USER_INFO.get("rol") == "pseudoadmin":
            CTkButton(self.marco_control, text="Nuevo (C)", command=self._abrir_modal_crear_producto).pack(side="right", padx=5)
        CTkButton(self.marco_control, text="Recargar", command=self.cargar_datos_producto).pack(side="right", padx=5)


        columnas_producto = ["producto_id", "nombre", "precio_base", "plazas_disponibles", "seccion_id"] 
        self.tabla_datos = DataTable(self, columnas=columnas_producto, 
                                     al_seleccionar_item=self.al_seleccionar_fila,
                                     al_doble_clic=self._mostrar_detalles_producto)
        self.tabla_datos.grid(row=1, column=0, padx=10, pady=5, sticky="nsew")
        
        self.marco_accion = CTkFrame(self, fg_color="transparent")
        self.marco_accion.grid(row=2, column=0, padx=10, pady=5, sticky="se")
        
        # Solo admin puede editar/eliminar productos
        if api_client.GLOBAL_USER_INFO.get("rol") == "pseudoadmin":
            CTkButton(self.marco_accion, text="Editar (U)", command=self._abrir_modal_editar_producto).pack(side="right", padx=5)
            CTkButton(self.marco_accion, text="Eliminar (D)", fg_color="red", 
                     hover_color="#AA0000", command=self._confirmar_y_eliminar).pack(side="right", padx=5)
        

    def cargar_datos_producto(self):
        datos = api_client.obtener_productos()
        if datos is None:
            tk_messagebox.showerror("Error de Conexión", "No se pudieron obtener los productos. Verifique el servidor REST.")
            self.tabla_datos.actualizar_datos([])
        else:
            self.tabla_datos.actualizar_datos(datos)

    def al_seleccionar_fila(self, id_producto):
        try:
            self.id_seleccionado = int(id_producto)
        except (ValueError, TypeError):
            self.id_seleccionado = None
    
    def _mostrar_detalles_producto(self, producto_data):
        DetailView(self.master, f"Detalles de Producto: {producto_data.get('nombre', 'N/A')}", 
                  producto_data)

    def _obtener_secciones_para_modal(self) -> list:
        secciones = api_client.obtener_secciones()
        if not secciones:
            return ["No hay secciones disponibles"]
        
        opciones = []
        self.secciones_map = {}  
        
        for s in secciones:
            seccion_id = s.get('seccion_id')
            nombre = s.get('nombre', '')
            
            if seccion_id is not None:
                display_name = f"ID {seccion_id}: {nombre}"
                opciones.append(display_name)
                self.secciones_map[display_name] = seccion_id
        
        return opciones
    
    def _get_producto_fields(self):
        opciones_secciones = self._obtener_secciones_para_modal()
        
        return [
            {'label': 'Nombre:', 'validator': validar_nombre_producto, 'key': 'nombre'},
            {'label': 'Precio Base:', 'validator': validar_precio_base, 'key': 'precio_base'},
            {'label': 'Plazas Disp.:', 'validator': validar_plazas, 'key': 'plazas_disponibles'},
            {
                'label': 'Sección:',
                'options': opciones_secciones,
                'key': 'seccion_display_name'
            },
        ]

    def _abrir_modal_crear_producto(self):
        ModalForm(self.master,
                  title="Crear Nuevo Producto",
                  fields_config=self._get_producto_fields(),
                  action_callback=self._crear_producto_y_guardar)

    def _abrir_modal_editar_producto(self):
        if self.id_seleccionado is None:
            tk_messagebox.showwarning("Advertencia", "Selecciona un producto de la tabla para editar.")
            return

        try:
            datos_actuales = api_client.obtener_producto_por_id(self.id_seleccionado)
            
            if datos_actuales is None or isinstance(datos_actuales, list):
                raise Exception("Producto no encontrado o formato de respuesta inválido.")
            
            self.producto_en_edicion = datos_actuales
            
            initial_data = datos_actuales.copy()
            
            self._obtener_secciones_para_modal()
            
            seccion_id = datos_actuales.get('seccion_id') or (datos_actuales.get('seccion', {}).get('seccion_id') if isinstance(datos_actuales.get('seccion'), dict) else None)
            if seccion_id:
                for display_name, sid in self.secciones_map.items():
                    if sid == seccion_id:
                        initial_data['seccion_display_name'] = display_name
                        break
            
            ModalForm(self.master,
                      title=f"Editar Producto ID: {self.id_seleccionado}",
                      fields_config=self._get_producto_fields(),
                      initial_data=initial_data,
                      action_callback=self._actualizar_producto_y_guardar)
            
        except Exception as e:
            tk_messagebox.showerror("Error", f"No se pudo cargar el producto: {e}")


    def _crear_producto_y_guardar(self, data):
        try:
            data['precio_base'] = float(data['precio_base'])
            data['plazas_disponibles'] = int(data['plazas_disponibles'])
            data['descripcion'] = "Descripción por defecto." 
            
            if 'seccion_display_name' in data and data['seccion_display_name']:
                seccion_display = data['seccion_display_name']
                seccion_id = self.secciones_map.get(seccion_display)
                if seccion_id:
                    data['seccion'] = {"seccionId": int(seccion_id)}
                else:
                    tk_messagebox.showerror("Error", "Sección no válida seleccionada.")
                    return False
            else:
                tk_messagebox.showerror("Error", "Debe seleccionar una sección.")
                return False

            resultado = api_client.crear_producto(data)
            
            if resultado is not None and resultado is not False:
                tk_messagebox.showinfo("Éxito", f"Producto '{data['nombre']}' CREADO en la BD.")
                self.cargar_datos_producto()
                return True
            else:
                tk_messagebox.showerror("Error de API", "La API REST no pudo crear el producto. Revise la consola.")
                return False

        except Exception as e:
            tk_messagebox.showerror("Error de Aplicación", f"Fallo interno al preparar el producto: {e}")
            return False

    def _actualizar_producto_y_guardar(self, data):
        if not self.producto_en_edicion:
            tk_messagebox.showerror("Error", "Error interno: El producto no fue cargado para edición.")
            return False
        try:
            producto_previo = self.producto_en_edicion
            
            if not hasattr(self, 'secciones_map') or not self.secciones_map:
                self._obtener_secciones_para_modal()
            
            data_final = {
                'nombre': data.get('nombre'),
                'precio_base': float(data.get('precio_base')),
                'plazas_disponibles': int(data.get('plazas_disponibles')),
            }
            
            data_final['productoId'] = producto_previo.get('producto_id')
            data_final['descripcion'] = producto_previo.get('descripcion') or "Descripción por defecto."
            
            if 'seccion_display_name' in data and data['seccion_display_name']:
                seccion_display = data['seccion_display_name']
                seccion_id = self.secciones_map.get(seccion_display)
                if seccion_id:
                    data_final['seccion'] = {"seccionId": int(seccion_id)}
                else:
                    tk_messagebox.showerror("Error", "Sección no válida seleccionada.")
                    return False
            else:
                seccion_id = producto_previo.get('seccion_id') or (producto_previo.get('seccion', {}).get('seccion_id') if isinstance(producto_previo.get('seccion'), dict) else None)
                if seccion_id:
                    if isinstance(seccion_id, dict):
                        seccion_id = seccion_id.get('seccion_id')
                    data_final['seccion'] = {"seccionId": int(seccion_id)}
            
            if api_client.actualizar_producto(self.id_seleccionado, data_final):
                tk_messagebox.showinfo("Éxito", f"Producto ID {self.id_seleccionado} actualizado.")
                self.cargar_datos_producto()
                return True
            else:
                tk_messagebox.showerror("Error de API", "Fallo al actualizar. El servidor rechazó la petición.")
                return False
                
        except Exception as e:
            tk_messagebox.showerror("Error de API", f"Fallo al actualizar el producto: {e}")
            return False
            
    def _confirmar_y_eliminar(self):
        if self.id_seleccionado is None:
            tk_messagebox.showwarning("Advertencia", "Selecciona un producto para eliminar.")
            return

        confirmar = tk_messagebox.askyesno(
            title="Confirmar Eliminación",
            message=f"¿Está seguro de que desea eliminar el producto con ID: {self.id_seleccionado}?"
        )

        if confirmar:
            try:
                if api_client.eliminar_producto(self.id_seleccionado):
                    tk_messagebox.showinfo("Éxito", f"Producto ID {self.id_seleccionado} eliminado.")
                    self.id_seleccionado = None
                    self.cargar_datos_producto()
                else:
                    tk_messagebox.showerror("Error de API", "El servidor rechazó la eliminación.")
            except Exception as e:
                tk_messagebox.showerror("Error", f"No se pudo eliminar el producto. {e}")


