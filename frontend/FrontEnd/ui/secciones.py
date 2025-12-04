from customtkinter import CTkFrame, CTkButton, CTkEntry, CTkFont
import tkinter.messagebox as tk_messagebox
import re
from typing import Optional, Dict, Any
from api import api_client

# Importa componentes de tabla y modal
from components.data_table import DataTable
from components.modal_form import ModalForm
from components.detail_view import DetailView 

# --- FUNCIONES DE VALIDACIÓN ---
def validar_nombre_seccion(valor):
    if not valor: return "El nombre de la sección es obligatorio.", False
    if len(valor) < 3: return "Mínimo 3 caracteres.", False
    return "✅", True


# ====================================================================
# --- VISTA COMPLETA CON CRUD DE SECCIONES ---
# ====================================================================

class VistaSecciones(CTkFrame):
    # Frame que contiene la tabla de secciones y los controles CRUD.
    
    def __init__(self, maestro, **kwargs):
        super().__init__(maestro, **kwargs)
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1, weight=1) 
        
        self.id_seleccionado: Optional[int] = None 
        self.seccion_en_edicion: Optional[Dict[str, Any]] = None

        self._inicializar_controles()
        self.cargar_datos_seccion()
        
    def _inicializar_controles(self):
        # Define el layout, input de búsqueda y botones.
        
        self.marco_control = CTkFrame(self, fg_color="transparent")
        self.marco_control.grid(row=0, column=0, padx=10, pady=(10, 5), sticky="new")

        # Botones CRUD (Nuevo y Recargar)
        CTkButton(self.marco_control, text="Nuevo (C)", command=self._abrir_modal_crear_seccion).pack(side="right", padx=5)
        CTkButton(self.marco_control, text="Recargar", command=self.cargar_datos_seccion).pack(side="right", padx=5)

        # Inicialización de la Tabla de Datos
        columnas_seccion = ["seccion_id", "nombre"] # Solo ID y Nombre
        self.tabla_datos = DataTable(self, columnas=columnas_seccion, 
                                     al_seleccionar_item=self.al_seleccionar_fila,
                                     al_doble_clic=self._mostrar_detalles_seccion)
        self.tabla_datos.grid(row=1, column=0, padx=10, pady=5, sticky="nsew")
        
        # Marco de Acciones inferiores (Editar/Eliminar)
        self.marco_accion = CTkFrame(self, fg_color="transparent")
        self.marco_accion.grid(row=2, column=0, padx=10, pady=5, sticky="se")
        
        CTkButton(self.marco_accion, text="Editar (U)", command=self._abrir_modal_editar_seccion).pack(side="right", padx=5)
        CTkButton(self.marco_accion, text="Eliminar (D)", fg_color="red", 
                 hover_color="#AA0000", command=self._confirmar_y_eliminar).pack(side="right", padx=5)
        
    # --- FUNCIONES DE LECTURA Y SELECCIÓN ---

    def cargar_datos_seccion(self):
        datos = api_client.obtener_secciones()
        if datos is None:
            tk_messagebox.showerror("Error de Conexión", "No se pudieron obtener las secciones. Verifique el servidor REST.")
            self.tabla_datos.actualizar_datos([])
        else:
            self.tabla_datos.actualizar_datos(datos)

    def al_seleccionar_fila(self, id_seccion):
        try:
            self.id_seleccionado = int(id_seccion)
        except (ValueError, TypeError):
            self.id_seleccionado = None
            
    
    def _mostrar_detalles_seccion(self, seccion_data):
        # Muestra los detalles completos de una sección
        DetailView(self.master, f"Detalles de Sección: {seccion_data.get('nombre', 'N/A')}", 
                  seccion_data)

    def _get_seccion_fields(self):
        # Solo necesitamos el nombre
        return [
            {'label': 'Nombre de la Sección:', 'validator': validar_nombre_seccion, 'key': 'nombre'},
        ]
        
    # --- FUNCIONES CRUD ---

    def _abrir_modal_crear_seccion(self):
        ModalForm(self.master,
                  title="Crear Nueva Sección",
                  fields_config=self._get_seccion_fields(),
                  action_callback=self._crear_seccion_y_guardar)

    def _abrir_modal_editar_seccion(self):
        if self.id_seleccionado is None:
            tk_messagebox.showwarning("Advertencia", "Selecciona una sección de la tabla para editar.")
            return

        try:
            datos_actuales = api_client.obtener_seccion_por_id(self.id_seleccionado)
            
            if datos_actuales is None or isinstance(datos_actuales, list):
                raise Exception("Sección no encontrada o formato de respuesta inválido.")
            
            self.seccion_en_edicion = datos_actuales
            
            ModalForm(self.master,
                      title=f"Editar Sección ID: {self.id_seleccionado}",
                      fields_config=self._get_seccion_fields(),
                      initial_data=datos_actuales,
                      action_callback=self._actualizar_seccion_y_guardar)
            
        except Exception as e:
            tk_messagebox.showerror("Error", f"No se pudo cargar la sección: {e}")


    def _crear_seccion_y_guardar(self, data):
        try:
            # No se necesitan campos adicionales (username, password, FKs) para Secciones
            resultado = api_client.crear_seccion(data)
            
            if resultado is not None and resultado is not False:
                tk_messagebox.showinfo("Éxito", f"Sección '{data['nombre']}' CREADA en la BD.")
                self.cargar_datos_seccion()
                return True
            else:
                tk_messagebox.showerror("Error de API", "La API REST no pudo crear la sección. Revise la consola.")
                return False

        except Exception as e:
            tk_messagebox.showerror("Error de Aplicación", f"Fallo interno al preparar la sección: {e}")
            return False

    def _actualizar_seccion_y_guardar(self, data):
        if not self.seccion_en_edicion:
            tk_messagebox.showerror("Error", "Error interno: La sección no fue cargada para edición.")
            return False
        try:
            seccion_previo = self.seccion_en_edicion
            
            data_final = {
                'nombre': data.get('nombre'),
            }
            
            # Incluye el ID para la actualización en Java/JPA
            if seccion_previo.get('seccion_id') is not None:
                data_final['seccionId'] = seccion_previo.get('seccion_id')
                
            if api_client.actualizar_seccion(self.id_seleccionado, data_final):
                tk_messagebox.showinfo("Éxito", f"Sección ID {self.id_seleccionado} actualizada.")
                self.cargar_datos_seccion()
                return True
            else:
                tk_messagebox.showerror("Error de API", "Fallo al actualizar. El servidor rechazó la petición.")
                return False
                
        except Exception as e:
            tk_messagebox.showerror("Error de API", f"Fallo al actualizar la sección: {e}")
            return False
            
    def _confirmar_y_eliminar(self):
        if self.id_seleccionado is None:
            tk_messagebox.showwarning("Advertencia", "Selecciona una sección para eliminar.")
            return

        confirmar = tk_messagebox.askyesno(
            title="Confirmar Eliminación",
            message=f"¿Está seguro de que desea eliminar la sección con ID: {self.id_seleccionado}?"
        )

        if confirmar:
            try:
                if api_client.eliminar_seccion(self.id_seleccionado):
                    tk_messagebox.showinfo("Éxito", f"Sección ID {self.id_seleccionado} eliminada.")
                    self.id_seleccionado = None
                    self.cargar_datos_seccion()
                else:
                    tk_messagebox.showerror("Error de API", "El servidor rechazó la eliminación.")
            except Exception as e:
                tk_messagebox.showerror("Error", f"No se pudo eliminar la sección. {e}")


