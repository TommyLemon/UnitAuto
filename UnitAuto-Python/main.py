# encoding=utf-8
from unitauto import methodutil
from unitauto.server import start, test

if __name__ == '__main__':
    def callback():
        pass

    # methodutil.config.DEFAULT_MODULE_PATH = "unitauto"
    # methodutil.listener.callback = callback

    test()
    start()

