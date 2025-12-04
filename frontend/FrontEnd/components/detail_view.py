from customtkinter import CTkToplevel, CTkFrame, CTkLabel, CTkScrollableFrame, CTkButton, CTkFont
import tkinter as tk

class DetailView(CTkToplevel):
    """Modal para mostrar detalles completos de una entidad"""
    
    def __init__(self, parent, titulo, datos, **kwargs):
        super().__init__(parent, **kwargs)
        self.title(titulo)
        self.geometry("600x500")
        self.transient(parent)
        self.grab_set()
        
        # Frame principal
        main_frame = CTkFrame(self)
        main_frame.pack(fill="both", expand=True, padx=20, pady=20)
        
        # Título
        title_label = CTkLabel(main_frame, text=titulo, 
                               font=CTkFont(size=20, weight="bold"))
        title_label.pack(pady=(0, 15))
        
        # Frame scrolleable para el contenido
        scroll_frame = CTkScrollableFrame(main_frame)
        scroll_frame.pack(fill="both", expand=True)
        
        # Mostrar todos los datos
        for key, value in datos.items():
            if value is not None:
                # Formatear la clave (snake_case a título)
                key_formatted = key.replace('_', ' ').title()
                
                # Frame para cada campo
                field_frame = CTkFrame(scroll_frame, fg_color="transparent")
                field_frame.pack(fill="x", pady=5)
                
                # Etiqueta de la clave
                key_label = CTkLabel(field_frame, text=f"{key_formatted}:",
                                    font=CTkFont(size=12, weight="bold"),
                                    anchor="w", width=200)
                key_label.pack(side="left", padx=10)
                
                # Valor
                if isinstance(value, (dict, list)):
                    value_str = str(value)
                elif isinstance(value, float):
                    value_str = f"{value:,.2f}"
                else:
                    value_str = str(value)
                
                value_label = CTkLabel(field_frame, text=value_str,
                                      font=CTkFont(size=12),
                                      anchor="w", wraplength=350)
                value_label.pack(side="left", padx=10, fill="x", expand=True)
        
        # Botón de cerrar
        close_btn = CTkButton(main_frame, text="Cerrar", command=self.destroy,
                             fg_color="#CC0000", hover_color="#AA0000")
        close_btn.pack(pady=(15, 0))

