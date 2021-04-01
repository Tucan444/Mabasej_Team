import threading
import uuid
import time

zoznam = []
rovnake = 0
run = True
start = time.time()

def generate():
    global zoznam, rovnake, run
    while run:
        cache = uuid.uuid4().hex[24:]
        if cache in zoznam:
            rovnake += 1
        zoznam.append(cache)
        pocet = len(zoznam)
        print(f"{pocet} : {rovnake} rovnakých - {cache}")
        if pocet > 50000:
            break


generate()
print(f"process lasted {time.time()-start}")