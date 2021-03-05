import os
import copy
import json


# todo !!!! currently don't use . in directory names !!!
# todo also dont use name 'directory__'


class Directory:
    # "c" is used in loaded files as content variable containing the loaded file
    def __init__(self):
        pass

    def __str__(self):
        return "directory__"

    def append_data(self, name, data, auto_convert=True):  # usually data about file
        if name != "":  # ignoring .smt files

            if auto_convert:
                if type(data) != type({}):
                    data = {
                        "directory__": False,
                        "c": data
                    }
                else:
                    if "directory__" not in data.keys():
                        data["directory__"] = False

            self.__setattr__(name, data)

    def append_directory(self, name, path=None, load_local=False):
        new_directory = Directory()

        if load_local:

            result = new_directory.load_local(path)
            if result == "not_directory":
                data = {
                    "path": path,
                    "filename": path.split("\\")[-1],
                    "filetype": "unknown",
                    "directory__": False
                }

                self.__setattr__(data["filename"], data)

        self.__setattr__(name, new_directory)

    def load(self, data_):
        for data_key in data_.keys():
            if data_key != "directory__":

                if self.check_for_json_content(data_[data_key]):
                    data_[data_key]["c"] = self.unpack_dictionary(data_[data_key]["c"])

                if hasattr(self, data_key):
                    obj = getattr(self, data_key)
                    if str(obj) == "directory__":
                        obj.load(data_[data_key])
                    else:

                        self.__setattr__(data_key, data_[data_key])

                else:
                    if data_[data_key]["directory__"] is True:
                        new_dir = Directory()
                        self.__setattr__(data_key, new_dir)
                        new_dir.load(data_[data_key])
                    else:
                        self.__setattr__(data_key, data_[data_key])

    def load_local(self, path=""):

        current_directory = os.getcwd()

        if path != "":
            try:
                os.chdir(path)
            except:
                return "not_directory"

        new_directory = os.getcwd()

        for file in os.listdir():

            if len(file) == len(file.split(".")[0]):
                self.append_directory(file, f"{new_directory}\\{file}", True)
            else:
                data = {
                    "path": f"{new_directory}\\{file}",
                    "filename": "".join(file.split(".")[:-1]),
                    "filetype": file.split(".")[-1],
                    "directory__": False
                }

                if data["filetype"] == "txt":

                    try:
                        with open(file, "r") as f:
                            data["c"] = f.read().split("\n")
                    except:
                        pass

                elif data["filetype"] == "json":

                    try:
                        with open(file, "r") as f:
                            data["c"] = json.load(f)

                        data["c"] = self.unpack_dictionary(data["c"])
                    except:
                        pass

                # making sure we don't overwrite something

                if data["filename"] in os.listdir():
                    data["filename"] += "_"

                while hasattr(self, data["filename"]):
                    data["filename"] += "_"

                self.append_data(data["filename"], data)

        os.chdir(current_directory)

        return "successful"

    def get_dictionary(self, root=True):
        dictionary = {}
        if root is False:
            dictionary["directory__"] = True

        for attribute in vars(self):
            obj = getattr(self, attribute)
            if str(obj) == "directory__":
                dictionary[attribute] = obj.get_dictionary(False)
            else:
                dictionary[attribute] = copy.copy(obj)

                if self.check_for_json_content(obj):
                    dictionary[attribute]["c"] = self.pack_to_dictionary(obj["c"])

        return dictionary

    def unpack_dictionary(self, dict_):
        root = Directory()

        for key in dict_.keys():
            if type(dict_[key]) == type({}):
                obj = self.unpack_dictionary(dict_[key])

                root.__setattr__(key, obj)

            else:
                root.append_data(key, dict_[key], auto_convert=False)

        return root

    def pack_to_dictionary(self, obj):
        dict_ = {}

        for attribute in vars(obj):
            if str(getattr(obj, attribute)) == "directory__":
                dict_[attribute] = self.pack_to_dictionary(getattr(obj, attribute))
            else:
                dict_[attribute] = getattr(obj, attribute)

        return dict_

    def search(self, name, directories=True, filetype="any", exact=True):
        # this func only searches for files
        # for unpacked json use search_iuj as search in unpacked json
        occurrences = []

        for attribute in vars(self):

            search = True
            is_directory = False

            obj = getattr(self, attribute)

            if str(obj) == "directory__":
                is_directory = True
                if directories is False or (filetype != "any" and filetype != "directory"):
                    search = False
                occurrences += obj.search(name, directories, filetype, exact)
            else:
                if filetype != "any":
                    if type(obj) == type({}):
                        if "filetype" in obj.keys():
                            if obj["filetype"] != filetype:
                                search = False

            if search:
                if exact:
                    if name == attribute.strip("_"):
                        if str(obj) == "directory__":
                            obj.name__ = attribute
                            occurrences.append(obj)
                        elif type(obj) == type({}):
                            if obj.keys() != ["directory__", "c"]:
                                occurrences.append(obj)
                            else:
                                occurrences.append({attribute: obj["c"]})
                        else:
                            occurrences.append({attribute: obj})

                else:
                    if name in attribute:
                        if str(obj) == "directory__":
                            obj.name__ = attribute
                            occurrences.append(obj)
                        elif type(obj) == type({}):
                            if obj.keys() != ["directory__", "c"]:
                                occurrences.append(obj)
                            else:
                                occurrences.append({attribute: obj["c"]})
                        else:
                            occurrences.append({attribute: obj})

        return occurrences

    def search_iuj(self, name, exact=True, in_content=False):
        occurrences = []

        if in_content is False:
            for attribute in vars(self):
                obj = getattr(self, attribute)

                if str(obj) == "directory__":
                    occurrences += obj.search_iuj(name, exact=exact)
                else:
                    if self.check_for_json_content(obj):
                        occurrences += obj["c"].search_iuj(name, exact=exact, in_content=True)

        else:
            for attribute in vars(self):
                obj = getattr(self, attribute)

                if str(obj) == "directory__":
                    occurrences += obj.search_iuj(name, exact=exact, in_content=True)

                if exact:
                    if name == attribute:
                        if str(obj) == "directory__":
                            obj.name__ = attribute
                            occurrences.append(obj)
                        else:
                            occurrences.append({attribute: obj})

                else:
                    if name in attribute:
                        if str(obj) == "directory__":
                            obj.name__ = attribute
                            occurrences.append(obj)
                        else:
                            occurrences.append({attribute: obj})

        return occurrences

    @staticmethod
    def check_for_json_content(data):
        if type(data) == type({}):
            if "filetype" in data.keys():
                if data["filetype"] == "json":
                    if "c" in data.keys():
                        return True

        return False

    def save_files(self):  # saves txt and json files
        for attribute in vars(self):
            obj = getattr(self, attribute)

            if str(obj) == "directory__":
                obj.save_files()
            else:
                if obj["filetype"] == "txt":
                    with open(obj["path"], "w") as f:
                        f.write("".join(obj["c"]))
                elif obj["filetype"] == "json":
                    with open(obj["path"], "w") as f:
                        json.dump(self.pack_to_dictionary(obj["c"]), f, indent=4)
