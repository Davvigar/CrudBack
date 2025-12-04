from customtkinter import CTkToplevel, CTkFrame, CTkButton, CTkScrollableFrame, CTkLabel, CTkComboBox, set_appearance_mode
import tkinter.messagebox as tk_messagebox
from components.validate_entry import ValidateEntry 

class ModalForm(CTkToplevel):
    """
    Diálogo modal reutilizable para la creación/edición de entidades.
    Soporta tanto campos de entrada con validación como combobox.
    """
    def __init__(self, master, title, fields_config, action_callback, initial_data=None, **kwargs):
        super().__init__(master, **kwargs)
        self.master = master
        self.action_callback = action_callback
        self.fields_config = fields_config
        self.validation_fields = {} 
        self.combobox_fields = {}  # Diccionario para almacenar widgets ComboBox
        self.initial_data = initial_data # Guarda los datos iniciales
        
        self.title(title)
        self.geometry("450x550")
        self.transient(master) 
        self.grab_set()        
        
        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(0, weight=1)
        
        set_appearance_mode(master._get_appearance_mode()) 

        self._crear_interfaz()
        
        self.protocol("WM_DELETE_WINDOW", self._on_close)
        
    def _crear_interfaz(self):
        scroll_frame = CTkScrollableFrame(self, label_text="Datos de la Entidad", fg_color="transparent")
        scroll_frame.grid(row=0, column=0, sticky="nsew", padx=20, pady=(20, 10))
        scroll_frame.grid_columnconfigure(0, weight=1)

        # 2. Construir campos de entrada dinámicamente
        for i, field in enumerate(self.fields_config):
            
            # LÓGICA DE CARGA: Obtener valor inicial seguro
            initial_value = self.initial_data.get(field['key'], "") if self.initial_data else ""
            
            # === SOPORTE PARA COMBOBOX ===
            if 'options' in field:
                # Usaremos un frame contenedor para que ocupe el mismo espacio vertical
                combo_frame = CTkFrame(scroll_frame, fg_color="transparent")
                combo_frame.grid(row=i, column=0, sticky="ew", padx=10, pady=(10, 5))
                combo_frame.grid_columnconfigure(0, weight=1) # Para que el combo se expanda
                
                # 1. Etiqueta (Fila 0, Columna 0)
                CTkLabel(combo_frame, text=field['label']).grid(row=0, column=0, sticky="w")
                
                # 2. Combobox (Fila 1, Columna 0)
                combo = CTkComboBox(
                    combo_frame, 
                    values=field['options'], 
                    command=field.get('command'), 
                    state=field.get('state', 'readonly'),
                    width=300 # Igual que CTkEntry en ValidateEntry
                )
                combo.grid(row=1, column=0, sticky="ew", pady=2)
                
                # 3. Etiqueta de Feedback (Fila 2, Columna 0) - Dejamos vacía para mantener consistencia
                CTkLabel(combo_frame, text="", text_color="red").grid(row=2, column=0, sticky="w") 
                
                # Preseleccionar valor si hay initial_value
                if initial_value:
                    try:
                        # Intentar establecer el valor directamente
                        combo.set(initial_value)
                    except:
                        pass
                    
                self.combobox_fields[field['key']] = combo
                
            # === LÓGICA EXISTENTE: Campo de Validación (ValidateEntry) ===
            else:
                # Obtener parámetros adicionales del campo (como 'show' para contraseñas)
                entry_kwargs = {
                    'texto_etiqueta': field['label'], 
                    'validador': field['validator'],
                    'fg_color': "transparent",
                    'initial_value': initial_value
                }
                if 'show' in field:
                    entry_kwargs['show_char'] = field['show']
                
                entry_widget = ValidateEntry(scroll_frame, **entry_kwargs)
                entry_widget.grid(row=i, column=0, sticky="ew", padx=10, pady=(10, 5))
                
                self.validation_fields[field['key']] = entry_widget
            
        # 3. Marco para los botones de acción
        button_frame = CTkFrame(self, fg_color="transparent")
        button_frame.grid(row=1, column=0, sticky="sew", padx=20, pady=(0, 20))
        button_frame.grid_columnconfigure(0, weight=1) 

        CTkButton(button_frame, text="Cancelar", command=self._on_close, fg_color="gray").pack(side="right", padx=10)
        CTkButton(button_frame, text="Guardar", command=self._on_save, fg_color="#0085FF").pack(side="right")
        
    def _on_save(self):
        data = {}
        all_valid = True
        
        # 1. Recoger datos de campos de Validación (Entry)
        for key, entry_widget in self.validation_fields.items():
            entry_widget._al_cambiar_entrada() 
            if not entry_widget.es_valido:
                all_valid = False
            
            data[key] = entry_widget.obtener_valor()
        
        # 2. Recoger datos de campos Combobox
        for key, combo_widget in self.combobox_fields.items():
            data[key] = combo_widget.get()

        if not all_valid:
            tk_messagebox.showerror("Error de Validación", "Corrija los campos marcados en rojo antes de guardar.")
            return

        try:
            success = self.action_callback(data) 
            if success:
                self.destroy()
        except Exception as e:
            tk_messagebox.showerror("Error", f"Ocurrió un error al intentar guardar: {e}")
            
    def _on_close(self):
        self.grab_release()
        self.destroy()