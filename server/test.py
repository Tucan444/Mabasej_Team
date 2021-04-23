import requests

file = open("files/test.jpg", "r")
r = requests.post(f"""http://192.168.1.99:8000/admin/1/upload_file""",
              files={"uploaded_file": file, "patch": ""})
print(r.text)