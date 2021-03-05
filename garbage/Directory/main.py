from Python.MyLibraries.Directory.directory import Directory


filesystem = Directory()
filesystem.load_local()  # loads local directory and unpacks jsons

filesystem.files.welcome["c"].D_text = "info o hrade!"  # changes attribute D_text in welcome
# "c" as content
filesystem.save_files()  # saves all changes to txt, json files


filesystem_dictionary = filesystem.get_dictionary()  # saves as dictionary

"""send(filesystem_dictionary)"""  # pseudocode

filesystem_2 = Directory()
filesystem_2.load(filesystem_dictionary)  # loads from dictionary

welcome = filesystem.search("welcome")  # searches for welcome
zaujimavosti = filesystem.search("mavosti", exact=False)  # searches for names that include mavosti
json_files = filesystem.search("", filetype="json", exact=False)  # searches for filetype json
folders = filesystem.search("", filetype="directory", exact=False)  # searches for directories/folders
files = filesystem.search("files", directories=False)  # search doesnt include directories
test = filesystem.search("test", filetype="json")

test_dict = test[0]["c"].get_dictionary()

oo = filesystem.search_iuj("oo")
attributes = test[0]["c"].search_iuj("o", exact=False, in_content=True)

print("run in debugger please")  # use breakpoints, for less confusion try keeping urself in main.py
