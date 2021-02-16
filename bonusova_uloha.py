import tkinter as tk
import time
c = tk.Canvas(bg="grey", width=500, height=400)
c.pack()

file = open("spokojnost.txt", "w").close()


def draw(rect_color0="yellow", rect_color1="yellow"):
    c.create_text(250, 150, text="Boli ste spokojny \ns nasimi sluzbami?", font="arial 30", fill="white")

    c.create_rectangle(50, 280, 150, 320, fill=rect_color0)
    c.create_text(100, 300, text="ano:)", font="arial 20")

    c.create_rectangle(350, 280, 450, 320, fill=rect_color1)
    c.create_text(400, 300, text="nie:(", font="arial 20")
    c.update()


def click(pos):
    file_ = open("spokojnost.txt", "a")

    if 280 <= pos.y <= 320:
        if 50 <= pos.x <= 150:
            file_.write("ano\n")
            draw("orange")

            time.sleep(0.1)

            draw()

        elif 350 <= pos.x <= 450:
            file_.write("nie\n")
            draw("yellow", "orange")

            time.sleep(0.1)

            draw()

    file_.close()


c.bind_all("<Button-1>", click)

draw()
c.mainloop()
