from customtkinter import CTkFrame, CTkScrollbar, CTkEntry, CTkLabel
from tkinter import ttk 
import tkinter as tk

class DataTable(CTkFrame):
    # Componente reutilizable para mostrar datos tabulares (Requisito DataTabel).
    def __init__(self, maestro, columnas, al_seleccionar_item=None, al_doble_clic=None, mostrar_busqueda=True, **kwargs):
        super().__init__(maestro, **kwargs)
        self.columnas = columnas
        self.al_seleccionar_item = al_seleccionar_item
        self.al_doble_clic = al_doble_clic  # Callback para doble clic
        self.datos = []
        self.datos_filtrados = []  # Datos filtrados por búsqueda
        self.orden_columna = {}  # Almacena el orden actual de cada columna (True=ascendente, False=descendente)
        self.mostrar_busqueda = mostrar_busqueda
        
        self.grid_columnconfigure(0, weight=1)
        self.grid_rowconfigure(1 if mostrar_busqueda else 0, weight=1) # La tabla debe expandirse

        if mostrar_busqueda:
            self._crear_busqueda()
        self._crear_vista_tabla()

    def _crear_vista_tabla(self):
        # Configura el Frame contenedor y el Treeview
        self.marco_tabla = CTkFrame(self)
        row_tabla = 1 if self.mostrar_busqueda else 0
        self.marco_tabla.grid(row=row_tabla, column=0, sticky="nsew", padx=5, pady=5)
        self.marco_tabla.grid_rowconfigure(0, weight=1)
        self.marco_tabla.grid_columnconfigure(0, weight=1)

        # Inicializa Treeview con las columnas definidas
        self.arbol = ttk.Treeview(self.marco_tabla, columns=self.columnas, show='headings')
        
        # Configura las cabeceras y comandos de ordenación
        for col in self.columnas:
            self.arbol.heading(col, text=col.replace('_', ' ').title(), 
                               command=lambda c=col: self._ordenar_datos(c))
            self.arbol.column(col, width=150, anchor=tk.W)

        # Configura la barra de desplazamiento y la enlaza al Treeview
        self.barra_desplazamiento = CTkScrollbar(self.marco_tabla, command=self.arbol.yview)
        self.arbol.configure(yscrollcommand=self.barra_desplazamiento.set)

        # Empaqueta la tabla y la barra de desplazamiento
        self.barra_desplazamiento.grid(row=0, column=1, sticky="ns")
        self.arbol.grid(row=0, column=0, sticky="nsew")

        # Conecta el evento de selección al método handler
        self.arbol.bind('<<TreeviewSelect>>', self._al_seleccionar)
        # Conecta el evento de doble clic
        if self.al_doble_clic:
            self.arbol.bind('<Double-1>', self._al_doble_clic)

    def _crear_busqueda(self):
        # Crea el campo de búsqueda
        self.busqueda_frame = CTkFrame(self, fg_color="transparent")
        self.busqueda_frame.grid(row=0, column=0, padx=5, pady=5, sticky="ew")
        self.busqueda_frame.grid_columnconfigure(1, weight=1)
        
        CTkLabel(self.busqueda_frame, text="🔍 Buscar:", font=("Arial", 12)).grid(row=0, column=0, padx=5)
        self.entry_busqueda = CTkEntry(self.busqueda_frame, placeholder_text="Buscar en todas las columnas...")
        self.entry_busqueda.grid(row=0, column=1, padx=5, sticky="ew")
        self.entry_busqueda.bind('<KeyRelease>', self._filtrar_datos)
    
    def _filtrar_datos(self, evento=None):
        # Filtra los datos según el texto de búsqueda
        texto_busqueda = self.entry_busqueda.get().lower()
        if not texto_busqueda:
            self.datos_filtrados = self.datos.copy()
        else:
            self.datos_filtrados = []
            for item in self.datos:
                # Buscar en todos los valores del item
                encontrado = False
                for col in self.columnas:
                    valor = str(item.get(col, "")).lower()
                    if texto_busqueda in valor:
                        encontrado = True
                        break
                if encontrado:
                    self.datos_filtrados.append(item)
        
        # Refrescar la tabla con datos filtrados
        self._refrescar_tabla_con_datos(self.datos_filtrados)
    
    def _al_seleccionar(self, evento):
        # Maneja la selección de fila y devuelve el ID (primera columna).
        item_seleccionado = self.arbol.focus()
        if item_seleccionado and self.al_seleccionar_item:
            valores = self.arbol.item(item_seleccionado, 'values')
            
            # Devuelve el valor de la primera columna (ID), forzado a string.
            if valores:
                self.al_seleccionar_item(str(valores[0]))
    
    def _al_doble_clic(self, evento):
        # Maneja el doble clic en una fila
        item_seleccionado = self.arbol.focus()
        if item_seleccionado and self.al_doble_clic:
            valores = self.arbol.item(item_seleccionado, 'values')
            # Obtener el item completo de los datos filtrados
            if valores:
                id_buscado = str(valores[0])
                # Buscar el item completo en los datos filtrados
                for item in self.datos_filtrados:
                    if str(item.get(self.columnas[0], "")) == id_buscado:
                        self.al_doble_clic(item)
                        break 

    def actualizar_datos(self, nuevos_datos):
        # Limpia la tabla y la rellena con los nuevos datos.
        self.datos = nuevos_datos or []
        self.datos_filtrados = self.datos.copy()
        # Limpiar búsqueda si existe
        if self.mostrar_busqueda and hasattr(self, 'entry_busqueda'):
            self.entry_busqueda.delete(0, tk.END)
        self._refrescar_tabla_con_datos(self.datos_filtrados)
    
    def _refrescar_tabla_con_datos(self, datos_a_mostrar):
        # Refresca la tabla con los datos proporcionados
        for item in self.arbol.get_children():
            self.arbol.delete(item)

        for item in datos_a_mostrar:
            # Inserta solo los valores que coinciden con las columnas
            valores_a_insertar = []
            for col in self.columnas:
                valor = item.get(col, "")
                # Convertir None a string vacío y formatear valores
                if valor is None:
                    valor = ""
                elif isinstance(valor, (int, float)):
                    # Formatear números con 2 decimales si es necesario
                    if isinstance(valor, float) and valor % 1 != 0:
                        valor = f"{valor:.2f}"
                    else:
                        valor = str(valor)
                else:
                    valor = str(valor)
                valores_a_insertar.append(valor)
            self.arbol.insert('', tk.END, values=valores_a_insertar)

    def _ordenar_datos(self, columna):
        # Ordena los datos por la columna seleccionada (alterna entre ascendente y descendente).
        if not self.datos:
            return
        
        # Determinar el orden (alternar entre ascendente y descendente)
        orden_ascendente = not self.orden_columna.get(columna, True)
        self.orden_columna[columna] = orden_ascendente
        
        # Función auxiliar para convertir valores a comparables
        def obtener_valor_para_ordenar(item):
            valor = item.get(columna, "")
            if valor == "" or valor is None:
                return "" if orden_ascendente else "zzz"  # Valores vacíos al final o al principio
            
            # Intentar convertir a número si es posible
            try:
                if isinstance(valor, (int, float)):
                    return float(valor)
                # Intentar convertir string a número
                valor_str = str(valor).replace(',', '.').strip()
                if valor_str.replace('.', '').replace('-', '').isdigit():
                    return float(valor_str)
            except (ValueError, AttributeError):
                pass
            
            # Si es una fecha en formato ISO o similar, convertirla
            try:
                from datetime import datetime
                if isinstance(valor, str) and ('T' in valor or '-' in valor):
                    # Intentar parsear como fecha
                    fecha = datetime.fromisoformat(valor.replace('Z', '+00:00'))
                    return fecha.timestamp()
            except (ValueError, AttributeError):
                pass
            
            # Por defecto, tratar como string
            return str(valor).lower()
        
        # Ordenar los datos filtrados
        datos_a_ordenar = self.datos_filtrados if self.datos_filtrados else self.datos
        datos_a_ordenar.sort(key=obtener_valor_para_ordenar, reverse=not orden_ascendente)
        
        # Actualizar la tabla con los datos ordenados
        self._refrescar_tabla_con_datos(datos_a_ordenar)
        
        # Actualizar el indicador de orden en la cabecera
        indicador = " ▲" if orden_ascendente else " ▼"
        texto_cabecera = columna.replace('_', ' ').title() + indicador
        self.arbol.heading(columna, text=texto_cabecera)
    
    def _refrescar_tabla(self):
        # Refresca la tabla con los datos actuales (sin recargar desde la API)
        datos_a_mostrar = self.datos_filtrados if self.datos_filtrados else self.datos
        self._refrescar_tabla_con_datos(datos_a_mostrar)