import tkinter
from tkinter.filedialog import askopenfilename
import requests
import json
import tkinter.messagebox
import os

window = tkinter.Tk()
window.title("WikiSpot Editor")
sellected = None
sellected_index = None
filename = None
textn1 = None
textn2 = None
textn3 = None
ip = "127.0.0.1"
data = None
rpi_active = []


# commands

def save_changes():
    data[sellected_index]["description"]["title"] = text1.get()
    data[sellected_index]["location"] = text2.get()
    data[sellected_index]["description"]["description_s"]
    data[sellected_index]["description"]["description_l"]


def update_server_refresh(text_window, description):
    text_window.configure(state="normal")
    text_window.delete('1.0', "end")
    text_window.insert("end", description)
    text_window.configure(state="disable")


def update_server_func(version, servers_all=True):
    if servers_all:
        if tkinter.messagebox.askyesno(title="Warning", message=f"WikiSpot Network will update to\n V{version}"):
            r = requests.get(f"http://{ip}:8000/admin/get/update-{version}")
            tkinter.messagebox.showinfo(title="Done", message=r.text)

    else:
        if tkinter.messagebox.askyesno(title="Warning", message=f"WikiSpot Server will update to\n V{version}"):
            r = requests.get(f"http://{ip}:8000/admin/get/update-one-{version}")
            tkinter.messagebox.showinfo(title="Done", message=r.text)


def update_server():
    r = requests.get(f"http://{ip}:8000/admin/get/get_updates")
    versions = json.loads(r.text)
    versions_server = list(versions[1].keys())
    window_update_server = tkinter.Toplevel()
    window_update_server.title("server update")
    variable = tkinter.StringVar(window_update_server)
    variable.set(versions_server[-1])  # default value
    version_menu = tkinter.OptionMenu(window_update_server, variable, *versions_server)
    label1 = tkinter.Label(window_update_server, text="WikiSpot verison: ")
    label2 = tkinter.Label(window_update_server, text=versions[0]["version"])
    label3 = tkinter.Label(window_update_server, text="Versions")
    label4 = tkinter.Label(window_update_server, text="Release news")
    b1 = tkinter.Button(window_update_server, text="Update all", command=lambda: update_server_func(variable.get()))
    b2 = tkinter.Button(window_update_server, text="Update one", command=lambda: update_server_func(variable.get(), servers_all=False))
    b3 = tkinter.Button(window_update_server, text="Exit", command=lambda: window_update_server.destroy())
    text = tkinter.Text(window_update_server, width=50, height=10)
    text.insert("end", versions[1][variable.get()]["change_list"])
    variable.trace("w", lambda *args: update_server_refresh(text, versions[1][variable.get()]["change_list"]))
    label1.grid(row=0, column=0)
    label2.grid(row=0, column=1)
    label3.grid(row=1, column=0)
    label4.grid(row=2, column=0)
    version_menu.grid(row=1, column=1)
    text.grid(row=2, column=1)
    b1.grid(row=3, column=0)
    b2.grid(row=3, column=1)
    b3.grid(row=3, column=2)
    window_update_server.mainloop()




def upload_new_file(name, window, file_format, description, *patch: str):
    global data
    if " " in name:
        tkinter.messagebox.showerror(title="Error", message="File name needs to be without spaces.\nApp on mobile will "
                                                            "change `_` to space.")
    else:
        data[sellected_index]["files"].append({
            "name": name,
            "format": file_format,
            "description": description.get("1.0", "end").replace("\n", "")
        })
        print("saving")
        with open("./test.json", "w", encoding='utf-8') as fp:
            json.dump(dict(data[sellected_index]), fp, indent=2)
        with open("test.json", "rb") as fp:
            requests.post(f"http://{ip}:8000/admin/{sellected}/upload_file", files={"uploaded_file": fp})
        os.remove("test.json")
        if patch:
            with open(patch[0], "rb") as fp:
                requests.post(f"http://{ip}:8000/admin/{sellected}/upload_file", files={"uploaded_file": fp, "patch": "files/"}, params={"patch": "files/"})
    window.destroy()


def new_file(*itnpu):
    global filename, sellected
    window_new = tkinter.Toplevel()
    window_new.title("New File")
    if sellected not in rpi_active:
        tkinter.messagebox.showerror(title="Error", message="Server is not selected!")
        window_new.destroy()
    else:
        filename = askopenfilename()
        while not filename:
            if not tkinter.messagebox.askretrycancel(title="Warning", message="File not selected."):
                window_new.destroy()
                break
            filename = askopenfilename()
        if filename:
            label1 = tkinter.Label(window_new, text="FileName")
            label2 = tkinter.Label(window_new, text="FileExtension")
            label3 = tkinter.Label(window_new, text="Description")
            label4 = tkinter.Label(window_new, text=str(filename.split("/")[-1].split(".")[0]))
            label5 = tkinter.Label(window_new, text=str(filename.split("/")[-1].split(".")[1]))
            in3 = tkinter.Text(window_new, height=8, width=40)
            b1 = tkinter.Button(window_new, text="UPLOAD", command=lambda: upload_new_file(
                str(filename.split("/")[-1].split(".")[0]), window_new,
                str(filename.split("/")[-1].split(".")[1]), in3, filename), width=40, height=2)
            b2 = tkinter.Button(window_new, text="EXIT", command=window_new.destroy, height=2)
            label1.grid(row=0, column=0)
            label2.grid(row=1, column=0)
            label3.grid(row=2, column=0)
            label4.grid(row=0, column=1)
            label5.grid(row=1, column=1)
            in3.grid(row=2, column=1)
            b1.grid(row=3, column=1)
            b2.grid(row=3, column=0)
            window_new.mainloop()


def edit_file(file_name):
    window_edit = tkinter.Toplevel()
    window_edit.title("edit_file")
    try:
        for file in data[sellected_index]["files"]:
            if file["name"] == file_name:
                sellected_file_index = data[sellected_index]["files"].index(file)
                break
            else:
                sellected_file_index = None
        label1 = tkinter.Label(window_edit, text="Name\n(you can change name and\nformat only by uploading\nnew file)")
        label2 = tkinter.Label(window_edit, text="Format")
        label3 = tkinter.Label(window_edit, text="Description")
        label4 = tkinter.Label(window_edit, text=data[sellected_index]["files"][sellected_file_index]["name"])
        label5 = tkinter.Label(window_edit, text=data[sellected_index]["files"][sellected_file_index]["format"])
        text = tkinter.Text(window_edit, height=8, width=40)
        text.insert("end", data[sellected_index]["files"][sellected_file_index]["description"])
        button1 = tkinter.Button(window_edit, text="Exit", command=lambda: window_edit.destroy())
        button2 = tkinter.Button(window_edit, text="Save", command=lambda: upload_new_file(
            data[sellected_index]["files"][sellected_file_index]["name"], window_edit,
            data[sellected_index]["files"][sellected_file_index]["format"], text))
        button3 = tkinter.Button(window_edit, text="Remove", command=lambda: upload_new_file(
            data[sellected_index]["files"][sellected_file_index]["name"], window_edit, "_REMOVE_", text))
        label1.grid(row=0, column=0)
        label2.grid(row=1, column=0)
        label3.grid(row=2, column=0)
        label4.grid(row=0, column=1)
        label5.grid(row=1, column=1)
        text.grid(row=2, column=1)
        button1.grid(row=3, column=0)
        button2.grid(row=3, column=1)
        button3.grid(row=3, column=2)
        window_edit.mainloop()
    except TypeError:
        tkinter.messagebox.showerror(title="Error", message="No file sellected!\n"
                                                            "Select server and then file you want to edit.")
        window_edit.destroy()



def update_inputs(self):
    global sellected, sellected_index, data
    try:
        if int(list_rpi.selection_get()) in rpi_active:
            sellected = int(list_rpi.selection_get())
            for rpi in data:
                if rpi["ID"] == sellected:
                    sellected_index = data.index(rpi)
            text1.set(data[sellected_index]["ID"])
            text2.set(data[sellected_index]["location"])
            list2.clipboard_clear()
            list2.delete(0, "end")
            for file in data[sellected_index]["files"]:
                list2.insert("end", file["name"])
    except ValueError or tkinter.TclError:
        print(f"Exception: list_rpi sellection = {list_rpi.selection_get()}")


def update_listbox():
    global data, rpi_active
    r = requests.get(f"http://{ip}:8000/devices_list")
    data = json.loads(r.text)
    del data[0]
    print(data)
    list_rpi.delete(0, "end")
    rpi_active.clear()
    for rpi in data:
        list_rpi.insert("end", rpi["ID"])
        rpi_active.append(rpi["ID"])
    # for rpi in zoznam:
    #    list_rpi.insert("end", rpi)


def clear_listbox():
    list_rpi.delete(0, "end")


def cmd1():
    print(list_rpi.selection_get())


# menu
menu = tkinter.Menu(window)
menu.master.config(menu=menu)

# file
menu_submenu1 = tkinter.Menu(menu, tearoff=0)
menu.add_cascade(label="File", menu=menu_submenu1)
menu_submenu1.add_command(label="refresh", command=update_listbox)
menu_submenu1.add_separator()
menu_submenu1.add_command(label="exit", command=exit)

# about
menu_submenu2 = tkinter.Menu(menu, tearoff=0)
menu.add_cascade(label="About", menu=menu_submenu2)
menu_submenu2.add_command(label="Check server updates", command=update_server)
menu_submenu2.add_command(label="Check app updates", command=update_listbox)
menu_submenu2.add_separator()
menu_submenu2.add_command(label="About WikiSpot", command=exit)
menu_submenu2.add_command(label="About Editor", command=exit)


# labels

l1 = tkinter.Label(window, text='label1')
l1.grid(row=0, column=0)

l2 = tkinter.Label(window, text='label2')
l2.grid(row=0, column=2)

l3 = tkinter.Label(window, text='label3')
l3.grid(row=1, column=0)

l4 = tkinter.Label(window, text='label4')
l4.grid(row=1, column=2)

l5 = tkinter.Label(window, text='Servers:')
l5.grid(row=2, column=0)

l6 = tkinter.Label(window, text='Files:  ')
l6.grid(row=2, column=5)

# entries

text1 = tkinter.StringVar()
en1 = tkinter.Entry(window, textvariable=text1)
en1.grid(row=0, column=1)

text2 = tkinter.StringVar()
en2 = tkinter.Entry(window, textvariable=text2)
en2.grid(row=0, column=3)

text3 = tkinter.StringVar()
en3 = tkinter.Entry(window, textvariable=text3)
en3.grid(row=1, column=1)

text4 = tkinter.StringVar()
en4 = tkinter.Entry(window, textvariable=text4)
en4.grid(row=1, column=3)

# listbox

list_rpi = tkinter.Listbox(window, height=6, width=35)
list_rpi.grid(row=3, column=0, rowspan=6, columnspan=2)
list_rpi.bind("<<ListboxSelect>>", update_inputs)

list2 = tkinter.Listbox(window, height=6, width=35)
list2.grid(row=3, column=5, rowspan=6, columnspan=2)
# list_rpi.bind("<<ListboxSelect>>", update_inputs)

# scrollbar

sb1 = tkinter.Scrollbar(window)
sb1.grid(row=2, column=2, rowspan=6)

list_rpi.configure(yscrollcommand=sb1.set)
sb1.configure(command=list_rpi.yview)

sb2 = tkinter.Scrollbar(window)
sb2.grid(row=2, column=7, rowspan=6)

list2.configure(yscrollcommand=sb2.set)
sb2.configure(command=list2.yview)

# buttons

btn1 = tkinter.Button(window, text="exit", width=12, command=update_listbox)
btn1.grid(row=6, column=3)

btn2 = tkinter.Button(window, text="add server", width=12, command=clear_listbox)
btn2.grid(row=3, column=3)

btn3 = tkinter.Button(window, text="place", width=12, command=new_file)
btn3.grid(row=4, column=3)

btn4 = tkinter.Button(window, text="place", width=12, command=lambda: edit_file(list2.selection_get()))
btn4.grid(row=5, column=3)

btn5 = tkinter.Button(window, text="save", width=12, command=update_listbox)
btn5.grid(row=6, column=4)

btn5 = tkinter.Button(window, text="new file", width=12, command=new_file)
btn5.grid(row=3, column=4)

btn5 = tkinter.Button(window, text="edit file", width=12, command=new_file)
btn5.grid(row=4, column=4)

btn5 = tkinter.Button(window, text="place", width=12, command=lambda: window.destroy())
btn5.grid(row=5, column=4)
update_listbox()
window.mainloop()
