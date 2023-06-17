# encoding=utf-8

import json
from unitauto import methodutil
from unitauto.methodutil import null, true, false, to_json_str, list_method, invoke_method, KEY_PACKAGE, KEY_CLASS, \
    KEY_CONSTRUCTOR, KEY_CLASS_ARGS, KEY_THIS, KEY_METHOD, KEY_METHOD_ARGS, KEY_TYPE, KEY_VALUE


if __name__ == '__main__':
    # rsp0 = invoke_method({
    #     KEY_PACKAGE: 'unitauto.test',
    #     KEY_CLASS: 'testutil',
    #     KEY_METHOD: 'test'
    # })
    # print('unitauto.test.test() = \n' + to_json_str(rsp0))
    #
    # rsp1 = invoke_method({
    #     KEY_PACKAGE: 'unitauto.test',
    #     KEY_CLASS: 'testutil',
    #     KEY_METHOD: 'minus',
    #     KEY_METHOD_ARGS: [
    #         {
    #             KEY_TYPE: 'int',
    #             KEY_VALUE: 2
    #         },
    #         {
    #             KEY_TYPE: 'int',
    #             KEY_VALUE: 3
    #         }
    #     ]
    # })
    # print('unitauto.test.add(2, 3) = \n' + to_json_str(rsp1))

    rsp2 = invoke_method({
        KEY_PACKAGE: 'unitauto.test',
        KEY_CLASS: 'testutil$Test',
        KEY_THIS: {
            KEY_TYPE: 'unitauto.test.testutil$Test',
            KEY_VALUE: {
                'id': 1,
                'sex': 0,
                'name': 'UnitAuto'
            }
        },
        KEY_METHOD: 'get_sex_str'
    })
    print('unitauto.test.testutil.Test.get_sex_str() = \n' + to_json_str(rsp2))
