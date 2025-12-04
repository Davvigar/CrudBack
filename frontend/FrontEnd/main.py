from customtkinter import *
from ui.login import LoginPage
from ui.dashboard import VentanaDashboard

set_appearance_mode("light")
set_default_color_theme("blue")

def open_dashboard_callback(user_name):
    app.withdraw() 
    VentanaDashboard(app, username=user_name)

if __name__ == "__main__":
    app = CTk()
    app.title("CRM XTART")
    app.geometry("1000x600")
    
    login_view = LoginPage(
        master=app,
        open_dashboard_callback=open_dashboard_callback
    )
    
    login_view.pack(fill="both", expand=True)
    
    app.mainloop()