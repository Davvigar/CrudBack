from customtkinter import *
from PIL import Image
import tkinter.messagebox as tk_messagebox
from api.api_client import login_autenticacion, crear_cliente, obtener_comerciales
from components.modal_form import ModalForm
from components.validate_entry import ValidateEntry
import re 


class LoginPage(CTkFrame):

    def __init__(self, master, open_dashboard_callback, **kwargs):
        super().__init__(master, **kwargs)
        self.master = master
        self.open_dashboard_callback = open_dashboard_callback
         
        self.configure(fg_color="white") 
        
        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure((0, 1), weight=1)
        
        self._construir_interfaz()
        self.master.bind('<F1>', lambda event: self._abrir_ayuda())
    
    def _construir_interfaz(self):
        
        self.bg_img = CTkImage(dark_image=Image.open("assets/b1.jpg"), size=(500, 500))
        bg_lab = CTkLabel(self, image=self.bg_img, text="")
        bg_lab.grid(row=0, column=0, sticky="nsew", padx=20, pady=20)
        
        form_frame = CTkFrame(self, fg_color="#D9D9D9", corner_radius=20, width=300)
        form_frame.grid(row=0, column=1, padx=(10, 30), pady=115, sticky="nsew")

        content_frame = CTkFrame(form_frame, fg_color="#D9D9D9")
        content_frame.pack(expand=True, padx=30, pady=30)
        content_frame.grid_columnconfigure(0, weight=1)

        self._construir_widgets_formulario(content_frame)

    def _construir_widgets_formulario(self, parent_frame):
        # Título
        title = CTkLabel(parent_frame, text="BIENVENIDO", text_color="black", font=("Roboto", 30, "bold"))
        title.grid(row=0, column=0, sticky="n", pady=10)

        entry_style = {"text_color": "white", "fg_color": "black", 
                        "placeholder_text_color": "white", "font": ("", 16, "bold"), 
                        "width": 250, "corner_radius": 15, "height": 45}

        self.usrname_entry = CTkEntry(parent_frame, placeholder_text="Usuario", **entry_style)
        self.usrname_entry.grid(row=1, column=0, sticky="ew", pady=(15, 5))

        self.passwd_entry = CTkEntry(parent_frame, placeholder_text="Contraseña", show="*", **entry_style)
        self.passwd_entry.grid(row=2, column=0, sticky="ew", pady=(5, 20))

        button_container = CTkFrame(parent_frame, fg_color="#D9D9D9")
        button_container.grid(row=3, column=0, sticky="ew")

        cr_acc = CTkLabel(button_container, text="Crear Cuenta!", text_color="black", 
                            cursor="hand2", font=("", 15))
        cr_acc.pack(side="left")
        cr_acc.bind("<Button-1>", lambda e: self._abrir_formulario_registro())

        self.l_btn = CTkButton(button_container, text="Login", font=("", 15, "bold"), 
                                height=40, width=80, fg_color="#0085FF", cursor="hand2",
                                corner_radius=15, command=self._handle_login)
        self.l_btn.pack(side="right")
        
    def _handle_login(self):
        username = self.usrname_entry.get()
        password = self.passwd_entry.get()

        if not username or not password:
            tk_messagebox.showerror(title="Error", message="Usuario y contraseña obligatorios.")
            return

        resultado = login_autenticacion(username, password)
        
        if resultado:
            nombre_usuario = resultado.get("nombre", username)
            rol = resultado.get("rol", "usuario")
            tk_messagebox.showinfo(title="Login Exitoso", message=f"Bienvenido, {nombre_usuario}.")
            self.open_dashboard_callback(nombre_usuario)
        elif resultado is False:
            tk_messagebox.showerror(title="Error", message="Usuario o contraseña incorrectos.")
        else:
            if username == "admin" and password == "1234":
                nombre_simulado = "Administrador (Simulación)"
                tk_messagebox.showinfo(title="Modo Simulación Activo",
                                        message="Conexión fallida al servidor. Bienvenido, Administrador (Simulación).")
                self.open_dashboard_callback(nombre_simulado)
            else:
                tk_messagebox.showerror(title="Error de Conexión",
                                         message="No se pudo conectar al servidor. Verifique que el backend esté ejecutándose.")
        
        self.passwd_entry.delete(0, END)

    def _abrir_formulario_registro(self):
        
        def validar_username(valor):
            if not valor: return "Campo obligatorio.", False
            if len(valor) < 3: return "Mínimo 3 caracteres.", False
            if not re.match(r"^[a-zA-Z0-9_]+$", valor): return "Solo letras, números y guiones bajos.", False
            return "✅", True
        
        def validar_password(valor):
            if not valor: return "Campo obligatorio.", False
            if len(valor) < 4: return "Mínimo 4 caracteres.", False
            return "✅", True
        
        def validar_nombre(valor):
            if not valor: return "Campo obligatorio.", False
            if len(valor) < 2: return "Mínimo 2 caracteres.", False
            return "✅", True
        
        def validar_email(valor):
            if not valor: return "El email es obligatorio.", False
            if not re.match(r"[^@]+@[^@]+\.[^@]+", valor): return "Formato de email incorrecto.", False
            return "✅", True
        
        def validar_telefono(valor):
            if not valor: return "El teléfono es obligatorio.", False
            if not re.match(r"[\d\s\-\.]{6,}", valor): return "Formato de teléfono incorrecto (mín. 6 dígitos).", False
            return "✅", True
        
        def validar_edad(valor):
            if not valor: return "La edad es obligatoria.", False
            try:
                edad = int(valor)
                if not (18 <= edad <= 120): return "Edad debe ser entre 18 y 120.", False
                return "✅", True
            except ValueError:
                return "Debe ser un número entero.", False
        
        def validar_direccion(valor):
            if not valor: return "La dirección es obligatoria.", False
            if len(valor) < 5: return "Dirección demasiado corta.", False
            return "✅", True
        
        comerciales = obtener_comerciales()
        comerciales_opciones = ["Ninguno"] + [f"{c.get('nombre', '')} (ID: {c.get('comercial_id', '')})" for c in comerciales] if comerciales else ["Ninguno"]
        
        def validar_comercial(valor):
            return "✅", True
        
        fields_config = [
            {'key': 'username', 'label': 'Usuario *', 'validator': validar_username},
            {'key': 'passwordHash', 'label': 'Contraseña *', 'validator': validar_password, 'show': '*'},
            {'key': 'nombre', 'label': 'Nombre *', 'validator': validar_nombre},
            {'key': 'apellidos', 'label': 'Apellidos *', 'validator': validar_nombre},
            {'key': 'edad', 'label': 'Edad *', 'validator': validar_edad},
            {'key': 'email', 'label': 'Email *', 'validator': validar_email},
            {'key': 'telefono', 'label': 'Teléfono *', 'validator': validar_telefono},
            {'key': 'direccion', 'label': 'Dirección *', 'validator': validar_direccion},
        ]
        
        def guardar_cliente(data):
            datos_cliente = {
                'username': data.get('username', ''),
                'passwordHash': data.get('passwordHash', ''), 
                'nombre': data.get('nombre', ''),
                'apellidos': data.get('apellidos', ''),
                'edad': int(data.get('edad', 0)) if data.get('edad') else 0,
                'email': data.get('email', ''),
                'telefono': data.get('telefono', ''),
                'direccion': data.get('direccion', ''),
                'comercial': None 
            }
            
            resultado = crear_cliente(datos_cliente)
            if resultado:
                tk_messagebox.showinfo("Éxito", "Cliente registrado correctamente. Ya puedes iniciar sesión.")
                return True
            else:
                tk_messagebox.showerror("Error", "No se pudo registrar el cliente. Verifique los datos.")
                return False
        
        modal = ModalForm(
            master=self.master,
            title="Registro de Nuevo Cliente",
            fields_config=fields_config,
            action_callback=guardar_cliente
        )
        modal.focus()
    
    def _abrir_ayuda(self):
        informacion_ayuda = ("GUÍA RÁPIDA - CRM XTART\n"
                              "• Puedes utilizar la tecla TAB para desplazarse mejor entre campos\n"
                              "• Pueds abrir la ayuda con el F1\n"
                              "• Para una simulacion, usar: usuario=admin, contraseña=1234")
        tk_messagebox.showinfo(title="Ayuda (F1)", message=informacion_ayuda)