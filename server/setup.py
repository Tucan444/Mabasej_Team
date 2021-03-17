import tkinter
import json
height = 750
width = 1200
with open("settings.json", "r") as file:
    settings = json.load(file)
with open("settings.json", "r") as file:
    filesystem = json.load(file)
canvas = tkinter.Canvas(height=height, width=width)
canvas.pack()

canvas.mainloop()