from customtkinter import CTkFrame, CTkEntry, CTkLabel, StringVar

class ValidateEntry(CTkFrame):
    def __init__(self, maestro, texto_etiqueta="Campo", validador=None, initial_value="", show=None, **kwargs): 
        super().__init__(maestro, **kwargs)
        
        self.validador = validador
        self.__es_valido = False 
        
        self.var_entrada = StringVar(value=initial_value) 
        
        self.grid_columnconfigure(0, weight=1)
        
        CTkLabel(self, text=texto_etiqueta).grid(row=0, column=0, sticky="w", padx=5)
        
        entry_kwargs = {"textvariable": self.var_entrada, "width": 300}
        if show:
            entry_kwargs["show"] = show
        self.entrada = CTkEntry(self, **entry_kwargs) 
        self.entrada.grid(row=1, column=0, sticky="ew", padx=5, pady=2)
        
        self.var_entrada.trace_add("write", self._al_cambiar_entrada) 
        
        self.etiqueta_feedback = CTkLabel(self, text="", text_color="red")
        self.etiqueta_feedback.grid(row=2, column=0, sticky="w", padx=5)
        
        self._al_cambiar_entrada() 

    def _al_cambiar_entrada(self, *args):
        valor = self.var_entrada.get()
        
        if self.validador:
            mensaje, es_valido = self.validador(valor)
            self.__es_valido = es_valido
            
            self.etiqueta_feedback.configure(text=mensaje)
            
            if self.__es_valido or not valor:
                self.entrada.configure(border_color="#909090")
            else:
                self.entrada.configure(border_color="red")

    def obtener_valor(self):
        return self.var_entrada.get()

    @property 
    def es_valido(self):
        self._al_cambiar_entrada() 
        return self.__es_valido
    
    @es_valido.setter
    def es_valido(self, value):
        self.__es_valido = value