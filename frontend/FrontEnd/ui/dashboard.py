from customtkinter import CTkFrame, CTkButton, CTkLabel, CTkToplevel, CTkFont
import tkinter.messagebox as tk_messagebox

# Importaciones de las vistas modulares
from .clientes import VistaClientes
from .comerciales import VistaComerciales
from .facturas import VistaFacturas
from .secciones import VistaSecciones
from .productos import VistaProductos

from components.vistadashboard import VistaDashboard 


class VentanaDashboard(CTkToplevel):
    def __init__(self, maestro, username="Comercial Demo", **kwargs):
        super().__init__(maestro, **kwargs)
        self.maestro = maestro
        self.username = username 
        
        self.transient(maestro)
        self.title("XTART CRM - Dashboard Principal")
        self.geometry("1100x700")
        self.minsize(800, 600)
        
        self.vistas_cargadas = {} 
        
        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(1, weight=1)
        
        self.crear_diseno()
        self.cambiar_vista("Dashboard")
        
        self.protocol("WM_DELETE_WINDOW", self._al_cerrar)
        self.maestro.bind('<F1>', lambda event: self._abrir_ayuda()) 

    def crear_diseno(self):
        self.lateral_frame = CTkFrame(self, fg_color="#1F1F1F", width=250, corner_radius=0) 
        self.lateral_frame.grid(row=0, column=0, sticky="nsew")
        self.lateral_frame.grid_columnconfigure(0, weight=1)
        self.lateral_frame.grid_rowconfigure(7, weight=1)
        
        CTkLabel(self.lateral_frame, 
                 text="XTART CRM", 
                 font=CTkFont(family="Roboto", size=28, weight="bold"), 
                 text_color="#F0F0F0").grid(row=0, column=0, padx=20, pady=(30, 10))
        
        self.crear_boton_nav("Dashboard", 1)
        self.crear_boton_nav("Clientes", 2)
        self.crear_boton_nav("Comerciales", 3)
        self.crear_boton_nav("Facturas", 4)
        self.crear_boton_nav("Productos", 5)
        self.crear_boton_nav("Secciones", 6)
        
        CTkButton(self.lateral_frame, 
                  text="Cerrar Sesión", 
                  command=self._al_cerrar,
                  fg_color="#CC0000", # Rojo más vivo
                  hover_color="#AA0000", 
                  font=CTkFont(family="Roboto", size=15, weight="bold"), 
                  height=40).grid(row=7, column=0, padx=20, pady=30, sticky="s")
      
        self.main_content_frame = CTkFrame(self, fg_color="#F8F8F8", corner_radius=0) 
        self.main_content_frame.grid(row=0, column=1, sticky="nsew", padx=20, pady=20)
        
        self.main_content_frame.grid_rowconfigure(0, weight=0)
        self.main_content_frame.grid_rowconfigure(1, weight=1) 
        self.main_content_frame.grid_columnconfigure(0, weight=1)

        title_bar_frame = CTkFrame(self.main_content_frame, fg_color="transparent")
        title_bar_frame.grid(row=0, column=0, sticky="ew", padx=10, pady=(0, 10)) 
        title_bar_frame.grid_columnconfigure(0, weight=1) 
        title_bar_frame.grid_columnconfigure(1, weight=1) 
        
        self.section_title = CTkLabel(title_bar_frame, text="",
                                      font=CTkFont(family="Roboto", size=30, weight="bold"),
                                      text_color="#0D0D0D") 
        self.section_title.grid(row=0, column=0, sticky="w")
        
        self.welcome_label = CTkLabel(title_bar_frame, 
                                      text=f"Bienvenido, {self.username.split()[0]}", 
                                      font=CTkFont(family="Roboto", size=18, weight="normal"),
                                      text_color="#5E81AC")
        self.welcome_label.grid(row=0, column=1, sticky="e")
        
        self.current_view_container = CTkFrame(self.main_content_frame, fg_color="#EAEAEA", corner_radius=10) 
        self.current_view_container.grid(row=1, column=0, sticky="nsew", padx=10, pady=(0, 10))
        
        self.current_view_container.grid_columnconfigure(0, weight=1)
        self.current_view_container.grid_rowconfigure(0, weight=1)

    # --- METODOS DE NAVEGACION Y UTILIDAD ---
    
    def crear_boton_nav(self, texto, fila):
        btn = CTkButton(self.lateral_frame, 
                        text=texto, 
                        command=lambda t=texto: self.cambiar_vista(t),
                        fg_color="#0085FF", 
                        hover_color="#006BBF", 
                        font=CTkFont(family="Roboto", size=15, weight="bold"), 
                        height=40,
                        corner_radius=10)
        btn.grid(row=fila, column=0, padx=20, pady=10, sticky="ew")
        return btn
        
    def cambiar_vista(self, nombre_vista):
        
        for widget in self.current_view_container.winfo_children():
            widget.destroy() 
        
        self.section_title.configure(text=nombre_vista.upper())
        
        vista = None
        if nombre_vista == "Dashboard":
            self.welcome_label.grid()
            self.cargar_vista_dashboard()
            return
        else:
            self.welcome_label.grid_remove()
            
        if nombre_vista == "Clientes":
            vista = VistaClientes(self.current_view_container, fg_color="transparent")
        elif nombre_vista == "Comerciales":
            vista = VistaComerciales(self.current_view_container, fg_color="transparent")
        elif nombre_vista == "Facturas":
            vista = VistaFacturas(self.current_view_container, fg_color="transparent")
        elif nombre_vista == "Secciones":
            vista = VistaSecciones(self.current_view_container, fg_color="transparent")
        elif nombre_vista == "Productos":
            vista = VistaProductos(self.current_view_container, fg_color="transparent")
            
        if vista:
            vista.grid(row=0, column=0, sticky="nsew")
        
    def cargar_vista_dashboard(self):
        dashboard_view = VistaDashboard(self.current_view_container, fg_color="transparent")
        dashboard_view.grid(row=0, column=0, sticky="nsew", padx=0, pady=0)
        
    def _al_cerrar(self):
        self.destroy()
        self.maestro.deiconify()
        
    def _abrir_ayuda(self, event=None):
        informacion_ayuda = ("GUÍA RÁPIDA - CRM XTART\n"
                              "• Utilice la tecla TAB para navegar.\n"
                              "• Atajo de teclado: F1 para Ayuda Contextual.\n"
                              "• Use el menú lateral para navegar entre secciones.")
        tk_messagebox.showinfo(title="Ayuda Contextual (F1)", message=informacion_ayuda)