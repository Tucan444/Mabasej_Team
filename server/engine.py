from datetime import datetime


class Log:
    def __init__(self, settings=None):
        if settings is None:
            settings = {"save_error": True, "print_error": True, "save_warning": True, "print_warning": True,
                        "save_message": False, "print_message": True, "enable_debug": False}
        self.save_error = settings["save_error"]
        self.save_warning = settings["save_warning"]
        self.save_messages = settings["save_message"]
        self.print_error = settings["print_error"]
        self.print_warning = settings["print_warning"]
        self.print_messages = settings["print_message"]
        self.debug_e = settings["enable_debug"]

    def error(self, error):
        if self.print_error:
            print(f"{datetime.now()} -> ERROR: {error}")
        if self.save_error:
            with open("log.txt", "a") as file:
                file.write(f"\n{datetime.now()} -> ERROR: {error}")

    def warning(self, warning):
        if self.print_warning:
            print(f"{datetime.now()} -> Warning: {warning}")
        if self.save_warning:
            with open("log.txt", "a") as file:
                file.write(f"\n{datetime.now()} -> Warning: {warning}")

    def message(self, message):
        if self.print_messages:
            print(f"{datetime.now()} -> message: {message}")
        if self.save_messages:
            with open("log.txt", "a") as file:
                file.write(f"\n{datetime.now()} -> message: {message}")

    def debug(self, debug):
        if self.debug_e:
            print(f"{datetime.now()} -> DEBUG: {debug}")
