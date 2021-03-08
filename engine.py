from datetime import datetime


class Log():
    def __init__(self, save_e=True, save_w=False, save_m=False, print_e=True, print_w=True, print_m=False, debug=False):
        self.save_error = save_e
        self.save_warning = save_w
        self.save_messages = save_m
        self.print_error = print_e
        self.print_warning = print_w
        self.print_messages = print_m
        self.debug_e = debug

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
