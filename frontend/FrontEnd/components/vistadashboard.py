import customtkinter as ctk
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
import numpy as np
from customtkinter import CTkFrame
import warnings

warnings.filterwarnings('ignore', category=RuntimeWarning, message='.*More than.*figures.*')
plt.rcParams['figure.max_open_warning'] = 0

from api.api_client import (get_ingresos_mensuales, get_ranking_comerciales, get_invoice_counts,
                            get_clientes_por_comercial, get_productos_mas_vendidos, 
                            get_ingresos_por_seccion, obtener_estadisticas_api, arrancar_informe,
                            resetear_estadisticas)
from customtkinter import CTkButton, CTkLabel
import tkinter.messagebox as tk_messagebox
import threading


CARD_COLOR = "#FFFFFF" 
TEXT_COLOR_DARK = "#0D0D0D" 
LINE_COLORS = ["#0085FF", "#FF7F50", "#3CB371", "#7B68EE"]
GRID_COLOR = "#DDDDDD" 

plt.rcParams.update({
    "figure.facecolor": CARD_COLOR,
    "axes.facecolor": CARD_COLOR,
    "axes.edgecolor": GRID_COLOR,
    "axes.labelcolor": TEXT_COLOR_DARK,
    "xtick.color": TEXT_COLOR_DARK,
    "ytick.color": TEXT_COLOR_DARK,
    "grid.color": GRID_COLOR,
    "grid.linestyle": "-",
    "font.size": 12,
    "xtick.labelsize": 11,
    "ytick.labelsize": 11,
    "legend.fontsize": 12,
    "axes.titlesize": 14
})

class VistaDashboard(CTkFrame):
    
    def __init__(self, master, **kwargs):
        super().__init__(master, **kwargs)
        
        self.grid_columnconfigure((0, 1, 2), weight=1)
        self.grid_rowconfigure(0, weight=0) 
        self.grid_rowconfigure(1, weight=2) 
        self.grid_rowconfigure(2, weight=2) 
        self.grid_rowconfigure(3, weight=2) 
        self.grid_rowconfigure(4, weight=1)

        periodos, ingresos = get_ingresos_mensuales()
        ranking = get_ranking_comerciales()
        conteo_facturas = get_invoice_counts()
        clientes_por_comercial = get_clientes_por_comercial()
        productos_mas_vendidos = get_productos_mas_vendidos()
        ingresos_por_seccion = get_ingresos_por_seccion()
        estadisticas_api = obtener_estadisticas_api() or {}
        
        if not periodos or not ingresos:
            periodos = []
            ingresos = []
        
        nombres = [d['nombre'] for d in ranking] if ranking else []
        valores = [d['ingresos'] for d in ranking] if ranking else []
        total_ingresos = sum(ingresos) if ingresos else 0.0

        
        self._add_kpi_card(self, total_ingresos, 0, 0, 2)
        self._add_estadisticas_api_card(self, estadisticas_api, 0, 2, 1)

        chart_func_line = lambda: self.create_top_chart(periodos, ingresos)
        self._add_chart_to_dashboard(self, chart_func_line, 1, 0, 3, "📈 Evolución de Ingresos Mensuales (€)", None, None)
        
        chart_func_bar = lambda: self.create_bar_chart(nombres, valores)
        self._add_chart_to_dashboard(self, chart_func_bar, 2, 0, 2, "📊 Ranking Comercial por Ingresos", None, None)
        
        chart_func_donut = lambda: self.create_invoice_status_pie(conteo_facturas)
        self._add_chart_to_dashboard(self, chart_func_donut, 2, 2, 1, "📑 Estado de Facturas", None, None)
        
        nombres_clientes = [d['nombre'] for d in clientes_por_comercial] if clientes_por_comercial else []
        valores_clientes = [d['clientes'] for d in clientes_por_comercial] if clientes_por_comercial else []
        chart_func_clientes = lambda: self.create_bar_chart(nombres_clientes, valores_clientes)
        self._add_chart_to_dashboard(self, chart_func_clientes, 3, 0, 1, "👥 Clientes por Comercial", None, None)
        
        nombres_productos = [d['nombre'] for d in productos_mas_vendidos] if productos_mas_vendidos else []
        valores_productos = [d['ventas'] for d in productos_mas_vendidos] if productos_mas_vendidos else []
        chart_func_productos = lambda: self.create_bar_chart(nombres_productos, valores_productos)
        self._add_chart_to_dashboard(self, chart_func_productos, 3, 1, 1, "🛍️ Productos Más Vendidos", None, None)
        
        nombres_secciones = [d['nombre'] for d in ingresos_por_seccion] if ingresos_por_seccion else []
        valores_secciones = [d['ingresos'] for d in ingresos_por_seccion] if ingresos_por_seccion else []
        chart_func_secciones = lambda: self.create_bar_chart(nombres_secciones, valores_secciones)
        self._add_chart_to_dashboard(self, chart_func_secciones, 3, 2, 1, "📦 Ingresos por Sección", None, None)
        
        self._add_informes_section(self, 4, 0, 3)



    def _add_kpi_card(self, parent_frame, total_ingresos, row, col, span):
        kpi_frame = ctk.CTkFrame(parent_frame, fg_color=LINE_COLORS[0], corner_radius=10)
        kpi_frame.grid(row=row, column=col, columnspan=span, sticky="nsew", padx=5, pady=5)
        kpi_frame.grid_columnconfigure(0, weight=1)
        kpi_frame.grid_rowconfigure(1, weight=1)

        ctk.CTkLabel(kpi_frame, 
                     text="INGRESOS TOTALES NETOS", 
                     text_color="white", 
                     font=ctk.CTkFont(size=12, weight="bold")
        ).grid(row=0, column=0, sticky="nw", padx=20, pady=(15, 0))

        ctk.CTkLabel(kpi_frame, 
                     text=f"{total_ingresos:,.2f} €", 
                     text_color="white", 
                     font=ctk.CTkFont(size=40, weight="bold")
        ).grid(row=1, column=0, sticky="w", padx=20, pady=(0, 15))
    
    def _add_estadisticas_api_card(self, parent_frame, estadisticas, row, col, span):
        stats_frame = ctk.CTkFrame(parent_frame, fg_color=CARD_COLOR, corner_radius=10, 
                                   border_color=GRID_COLOR, border_width=1)
        stats_frame.grid(row=row, column=col, columnspan=span, sticky="nsew", padx=5, pady=5)
        stats_frame.grid_columnconfigure(0, weight=1)
        
        ctk.CTkLabel(stats_frame, 
                     text="📊 Estadísticas de API", 
                     text_color=TEXT_COLOR_DARK, 
                     font=ctk.CTkFont(size=14, weight="bold")
        ).grid(row=0, column=0, sticky="nw", padx=15, pady=(15, 10))
        
        total_requests = estadisticas.get('totalRequests', estadisticas.get('peticionesTotales', 0))
        successful = estadisticas.get('successfulRequests', estadisticas.get('exitosas', 0))
        failed = estadisticas.get('failedRequests', estadisticas.get('fallos', 0))
        avg_time = estadisticas.get('averageResponseTime', 0.0)
        
        info_text = f"Total: {total_requests}\nExitosas: {successful}\nFallidas: {failed}\nTiempo promedio: {avg_time:.2f}ms"
        info_label = ctk.CTkLabel(stats_frame, 
                     text=info_text, 
                     text_color=TEXT_COLOR_DARK, 
                     font=ctk.CTkFont(size=11),
                     justify="left"
        )
        info_label.grid(row=1, column=0, sticky="nw", padx=15, pady=(0, 10))
        
        # Botón para resetear estadísticas
        reset_btn = CTkButton(stats_frame, 
                              text="🔄 Resetear Estadísticas",
                              command=lambda: self._on_resetear_estadisticas(info_label),
                              fg_color="#CC0000", 
                              hover_color="#AA0000",
                              font=ctk.CTkFont(size=11))
        reset_btn.grid(row=2, column=0, padx=15, pady=(0, 15), sticky="w")
    
    def _add_informes_section(self, parent_frame, row, col, span):
        informes_frame = ctk.CTkFrame(parent_frame, fg_color=CARD_COLOR, corner_radius=10,
                                     border_color=GRID_COLOR, border_width=1)
        informes_frame.grid(row=row, column=col, columnspan=span, sticky="nsew", padx=5, pady=5)
        informes_frame.grid_columnconfigure((0, 1, 2), weight=1)
        
        ctk.CTkLabel(informes_frame, 
                     text="📄 Generar Informes", 
                     text_color=TEXT_COLOR_DARK, 
                     font=ctk.CTkFont(size=14, weight="bold")
        ).grid(row=0, column=0, columnspan=3, sticky="nw", padx=15, pady=(15, 10))
        
        btn_clientes = CTkButton(informes_frame, text="Informe de Clientes",
                                command=lambda: self._generar_informe('clientes'),
                                fg_color=LINE_COLORS[0], hover_color="#006BBF")
        btn_clientes.grid(row=1, column=0, padx=10, pady=10, sticky="ew")
        
        btn_facturas = CTkButton(informes_frame, text="Informe de Facturas",
                                command=lambda: self._generar_informe('facturas'),
                                fg_color=LINE_COLORS[1], hover_color="#CC6B4E")
        btn_facturas.grid(row=1, column=1, padx=10, pady=10, sticky="ew")
        
        btn_completo = CTkButton(informes_frame, text="Informe Completo",
                                command=lambda: self._generar_informe('completo'),
                                fg_color=LINE_COLORS[2], hover_color="#2A9D5F")
        btn_completo.grid(row=1, column=2, padx=10, pady=10, sticky="ew")
        
        ctk.CTkLabel(informes_frame, 
                     text="Los informes se generan en segundo plano. Revise los logs del servidor.", 
                     text_color=GRID_COLOR, 
                     font=ctk.CTkFont(size=10),
                     wraplength=600
        ).grid(row=2, column=0, columnspan=3, padx=15, pady=(0, 15))
    
    def _generar_informe(self, tipo):
        def generar():
            resultado = arrancar_informe(tipo)
            if resultado:
                tk_messagebox.showinfo("Informe", 
                    f"Informe de {tipo} iniciado. Se está generando en segundo plano.")
            else:
                tk_messagebox.showerror("Error", 
                    f"No se pudo iniciar el informe de {tipo}.")
        
        thread = threading.Thread(target=generar)
        thread.daemon = True
        thread.start()
    
    def _on_resetear_estadisticas(self, info_label):
        def resetear():
            try:
                resultado = resetear_estadisticas()
                if resultado:
                    # Obtener las estadísticas actualizadas (deberían estar en 0)
                    estadisticas = obtener_estadisticas_api() or {}
                    total_requests = estadisticas.get('totalRequests', 0)
                    successful = estadisticas.get('successfulRequests', 0)
                    failed = estadisticas.get('failedRequests', 0)
                    avg_time = estadisticas.get('averageResponseTime', 0.0)
                    
                    # Actualizar el label desde el hilo principal usando after()
                    info_text = f"Total: {total_requests}\nExitosas: {successful}\nFallidas: {failed}\nTiempo promedio: {avg_time:.2f}ms"
                    self.after(0, lambda: info_label.configure(text=info_text))
                    self.after(0, lambda: tk_messagebox.showinfo("Éxito", "Estadísticas reseteadas correctamente."))
                else:
                    self.after(0, lambda: tk_messagebox.showerror("Error", "No se pudieron resetear las estadísticas."))
            except Exception as e:
                self.after(0, lambda: tk_messagebox.showerror("Error", f"Error al resetear estadísticas: {str(e)}"))
        
        thread = threading.Thread(target=resetear)
        thread.daemon = True
        thread.start()


    def _add_chart_to_dashboard(self, parent_frame, chart_function, row, column, columnspan, title_text, text_above, text_below):
        container = ctk.CTkFrame(parent_frame, fg_color=CARD_COLOR, corner_radius=10, border_color=GRID_COLOR, border_width=1)
        container.grid(row=row, column=column, columnspan=columnspan, sticky="nsew", padx=5, pady=5)
        container.grid_columnconfigure(0, weight=1)
        
        current_row = 0
        PAD_X_INNER = 15
        
        label = ctk.CTkLabel(container, text=title_text, text_color=TEXT_COLOR_DARK, font=ctk.CTkFont(size=14, weight="bold"))
        label.grid(row=current_row, column=0, sticky="w", padx=PAD_X_INNER, pady=(15, 5))
        current_row += 1

        chart_frame = ctk.CTkFrame(container, fg_color="transparent")
        chart_frame.grid(row=current_row, column=0, sticky="nsew", padx=5, pady=5)
        container.grid_rowconfigure(current_row, weight=1) 
        current_row += 1
        
        self._create_matplotlib_widget(chart_frame, chart_function())

        if text_above or text_below:
            info_text = text_above if text_above else text_below
            label = ctk.CTkLabel(container, text=info_text, text_color=GRID_COLOR, wraplength=450, font=ctk.CTkFont(size=10))
            label.grid(row=current_row, column=0, sticky="w", padx=PAD_X_INNER, pady=(0, 10))
            current_row += 1

    def _create_matplotlib_widget(self, parent_frame, fig):
        fig.patch.set_alpha(0.0)
        canvas_widget = FigureCanvasTkAgg(fig, master=parent_frame)
        canvas_widget.draw()
        widget = canvas_widget.get_tk_widget()
        
        widget.pack(fill="both", expand=True, padx=0, pady=0)
        

    def create_invoice_status_pie(self, conteo_facturas):
        fig, ax = plt.subplots(figsize=(2.5, 2))
        
        labels = ['Pagadas', 'Pendientes', 'Canceladas']
        sizes = [conteo_facturas['pagada'], conteo_facturas['pendiente'], conteo_facturas['cancelada']]
        colors = [LINE_COLORS[2], LINE_COLORS[1], LINE_COLORS[3]] # Verde, Naranja, Púrpura
        
        labels_filt = [labels[i] for i, size in enumerate(sizes) if size > 0]
        sizes_filt = [size for size in sizes if size > 0]
        colors_filt = [colors[i] for i, size in enumerate(sizes) if size > 0]
        
        if not sizes_filt: 
            ax.text(0.5, 0.5, 'Sin Datos', ha='center', va='center', color=TEXT_COLOR_DARK, fontsize=14)
            return fig
            
        ax.pie(sizes_filt, labels=None, colors=colors_filt, autopct='%1.1f%%', startangle=90,
               wedgeprops={'edgecolor': CARD_COLOR, 'linewidth': 3}, pctdistance=0.85,
               textprops={'fontsize': 13, 'color': TEXT_COLOR_DARK, 'weight': 'bold'})

        # Círculo central (Donut)
        centre_circle = plt.Circle((0,0), 0.65, fc=CARD_COLOR)
        ax.add_artist(centre_circle)
        ax.axis('equal')
        
        ax.legend(
            labels_filt,
            loc="center left",
            bbox_to_anchor=(-0.15, 0.5),
            fontsize=12,
            frameon=True,
            framealpha=0.9,
            facecolor=CARD_COLOR,
            edgecolor=GRID_COLOR,
            shadow=True
        )
        
        fig.subplots_adjust(left=0.15, right=0.95, top=0.95, bottom=0.05)
        return fig
    
    def create_top_chart(self, periodos, ingresos):
        fig, ax = plt.subplots(figsize=(3, 2))
        if not ingresos: 
            ax.text(0.5, 0.5, 'Sin Datos', ha='center', va='center', color=TEXT_COLOR_DARK, fontsize=14)
            return fig
        
        x_indices = np.arange(len(periodos))
        
        ax.plot(x_indices, ingresos, color=LINE_COLORS[0], linewidth=3, marker='o', markersize=8, markerfacecolor=LINE_COLORS[0], markeredgecolor=CARD_COLOR, markeredgewidth=1.5)
        
        for i, (x, y) in enumerate(zip(x_indices, ingresos)):
            ax.annotate(f'{y:,.0f}€', (x, y), textcoords="offset points", xytext=(0,10), 
                       ha='center', fontsize=10, color=TEXT_COLOR_DARK, weight='bold')
        
        # Configuración de ejes
        ax.set_xticks(x_indices)
        periodos_truncados = [p[:12] if len(p) > 12 else p for p in periodos]
        ax.set_xticklabels(periodos_truncados, rotation=30, ha='right', color=TEXT_COLOR_DARK, fontsize=11)
        ax.tick_params(axis='y', labelsize=11, colors=TEXT_COLOR_DARK)
        ax.set_ylabel('Ingresos (€)', fontsize=12, color=TEXT_COLOR_DARK, weight='bold')
        ax.grid(axis='y', linestyle='--', alpha=0.5, linewidth=1)
        ax.grid(axis='x', linestyle='--', alpha=0.3, linewidth=0.5)
        
        ax.spines['top'].set_visible(False)
        ax.spines['right'].set_visible(False)
        ax.spines['left'].set_color(GRID_COLOR)
        ax.spines['bottom'].set_color(GRID_COLOR)
        
        fig.subplots_adjust(left=0.1, right=0.95, top=0.92, bottom=0.2)
        return fig
    
    def create_bar_chart(self, nombres, valores):
        fig, ax = plt.subplots(figsize=(2, 2))
        if not valores: 
            ax.text(0.5, 0.5, 'Sin Datos', ha='center', va='center', color=TEXT_COLOR_DARK, fontsize=14)
            return fig
        
        categorias = np.arange(len(nombres))
        colores_barras = [LINE_COLORS[0]] * len(nombres) # Usar color primario
        
        bars = ax.bar(categorias, valores, color=colores_barras, edgecolor=CARD_COLOR, linewidth=1.5, alpha=0.8)
        
        for bar in bars:
            height = bar.get_height()
            ax.annotate(f'{height:,.0f}',
                       xy=(bar.get_x() + bar.get_width() / 2, height),
                       xytext=(0, 3),
                       textcoords="offset points",
                       ha='center', va='bottom', fontsize=10, color=TEXT_COLOR_DARK, weight='bold')
        
        nombres_truncados = []
        for nombre in nombres:
            if len(nombre) > 15:
                nombres_truncados.append(nombre[:13] + '...')
            else:
                nombres_truncados.append(nombre)
        
        ax.set_xticks(categorias)
        ax.set_xticklabels(nombres_truncados, rotation=30, ha='right', color=TEXT_COLOR_DARK, fontsize=11)
        ax.tick_params(axis='y', labelsize=11, colors=TEXT_COLOR_DARK)
        ax.grid(axis='y', linestyle='--', alpha=0.5, linewidth=1)
        
        ax.spines['top'].set_visible(False)
        ax.spines['right'].set_visible(False)
        ax.spines['left'].set_color(GRID_COLOR)
        ax.spines['bottom'].set_color(GRID_COLOR)
        
        num_items = len(nombres)
        if num_items > 5:
            bottom_margin = 0.4
        elif num_items > 3:
            bottom_margin = 0.35
        else:
            bottom_margin = 0.3
        
        fig.subplots_adjust(left=0.12, right=0.95, top=0.92, bottom=bottom_margin)
        return fig