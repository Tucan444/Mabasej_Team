import requests
json_list = []


def init():
    global json_list
    json_list = eval(requests.get("http://192.168.1.120:8000/devices_list").text)


def get_length():
    return len(json_list)


def get_json(i):
    return json_list[i]
