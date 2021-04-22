import requests
import time
send_n = 100
wait_t = 0.5

for x in range(send_n):
    r = requests.post("http://192.168.1.99:8000/messages/post", json={"m_sender": "server", "message": str(x)})
    print(f"Sent message {x+1}/{send_n}\nResponse: {r.text}")
    time.sleep(wait_t)