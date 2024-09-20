/*Copyright ©2024 TommyLemon(https://github.com/TommyLemon)

Licensed under the Apache License, Version 2.0 (the "License")
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

#include <string>
#include <unordered_map>
#include "nlohmann/json.hpp"
#include <functional>
#include <any>
#include <map>
#include <vector>
#include <sstream>
#include <iostream>
#include <vector>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <typeinfo>
#include <cxxabi.h>
#include <fstream>
#include <cstdlib> // for system()
#include<csignal>
#include <regex>

/**@author Lemon
 */
namespace unitauto {
    // 用于解码类型名称的函数
    static std::string demangle(const char* name) {
        int status = 0;
        std::unique_ptr<char, void(*)(void*)> res{
            abi::__cxa_demangle(name, nullptr, nullptr, &status),
            std::free
        };
        return (status == 0) ? res.get() : name;
    }

    using json = nlohmann::json;

    static std::string DEFAULT_MODULE_PATH = "unitauto";

    static const std::string TYPE_ANY = "any"; // typeid(bool).name();
    static const std::string TYPE_BOOL = "bool"; // typeid(bool).name();
    static const std::string TYPE_CHAR = "char"; // typeid(char).name();
    static const std::string TYPE_BYTE = "byte"; // typeid(std::byte).name();
    static const std::string TYPE_SHORT = "short"; // typeid(bool).name();
    static const std::string TYPE_INT = "int"; // typeid(int).name();
    static const std::string TYPE_LONG = "long"; // typeid(long).name();
    static const std::string TYPE_LONG_LONG = "long long"; // typeid(long).name();
    static const std::string TYPE_FLOAT = "float"; // typeid(float).name();
    static const std::string TYPE_DOUBLE = "double"; // typeid(double).name();
    static const std::string TYPE_STRING = "string"; // typeid(std::string).name();
    // static const auto TYPE_ARR = typeid(std::array).name();
    static const std::string TYPE_ANY_ARR = "any[]"; // typeid(bool).name();
    static const std::string TYPE_BOOL_ARR = "bool[]"; // typeid(bool).name();
    static const std::string TYPE_CHAR_ARR = "char[]"; // typeid(char).name();
    static const std::string TYPE_BYTE_ARR = "byte[]"; // typeid(std::byte).name();
    static const std::string TYPE_SHORT_ARR = "short[]"; // typeid(bool).name();
    static const std::string TYPE_INT_ARR = "int[]"; // typeid(int).name();
    static const std::string TYPE_LONG_ARR = "long[]"; // typeid(long).name();
    static const std::string TYPE_LONG_LONG_ARR = "long long[]"; // typeid(long).name();
    static const std::string TYPE_FLOAT_ARR = "float[]"; // typeid(float).name();
    static const std::string TYPE_DOUBLE_ARR = "double[]"; // typeid(double).name();
    static const std::string TYPE_STRING_ARR = "string[]"; // typeid(std::string).name();

    std::string trim_type(std::string type) {
        type = std::regex_replace(type, std::regex(DEFAULT_MODULE_PATH + "::"), "");
        type = std::regex_replace(type, std::regex(DEFAULT_MODULE_PATH + "."), "");
        type = std::regex_replace(type, std::regex("std::"), "");
        type = std::regex_replace(type, std::regex("std."), "");
        type = std::regex_replace(type, std::regex("__1::"), "");
        type = std::regex_replace(type, std::regex("__1."), "");

        if (type.empty() || type == "v" || type == "Dn" || type == "NULL" || type == "null" || type == "nullptr" || type == "nullptr_t") {
            return "";
        }

        if (type == "b") {
            return TYPE_BOOL;
        }
        if (type == "c") {
            return TYPE_CHAR;
        }
        if (type == "i") {
            return TYPE_INT;
        }
        if (type == "l") {
            return TYPE_LONG;
        }
        if (type == "ll") {
            return TYPE_LONG_LONG;
        }
        if (type == "f") {
            return TYPE_FLOAT;
        }
        if (type == "d") {
            return TYPE_DOUBLE;
        }

        while (! type.empty()) {
            char c = type.at(0);
            if (c >= '0' && c <= '9') {
                type = type.substr(1);
            } else {
                break;
            }
        }

        return type;
    }

    std::string get_type(std::any a) {
        auto type = demangle(a.type().name()); // typeid(a).name();  // TYPE.name();
        type = trim_type(type);
        if (type.empty()) {
            type = demangle(typeid(a.type()).name());
            type = trim_type(type);
        }
        return type;
    }

    static std::map<std::string, std::string> TYEP_ALIAS_MAP;

    // 类型转换函数映射
    // template<typename T >
    static std::map<std::string, std::function<json(std::any)>> CAST_MAP;

    // 注册类型转换函数
    template<typename T>
    void add_cast(std::string type, json caster(std::any val)) {
        CAST_MAP[type] = caster != nullptr ? caster : [](std::any value) -> json {
            json j;
            try {
                j = std::any_cast<T>(value); // j = json::parse(value);
                return j;
            } catch (const nlohmann::json::parse_error& ex) {
                std::cout << "nlohmann::json::parse_error at byte " << ex.byte << ": " << ex.what() << std::endl;
            } catch (const nlohmann::json::type_error& ex) {
                std::cout << "nlohmann::json::type_error " << ex.what() << std::endl;
            } catch (const nlohmann::json::other_error& ex) {
                std::cout << "nlohmann::json::other_error " << ex.what() << std::endl;
            } catch (const std::exception& e) {
                std::cout << "add_cast j.dump() >> std::exceptione " << e.what() << std::endl;
            }

            std::stringstream ss;
            ss << &value;
            j["type"] = get_type(value);
            j["value"] = ss.str();
            return j;
        };

        std::string t = trim_type(demangle(typeid(T).name()));
        if (t != type) {
            CAST_MAP[t] = CAST_MAP[type];
            TYEP_ALIAS_MAP[t] = type;
            TYEP_ALIAS_MAP[type] = t;
        }
    }

    static std::unordered_map<std::string, std::function<void*(json &j)>> TYPE_MAP;
    static std::unordered_map<std::string, std::function<std::any(json &j)>> STRUCT_MAP;

    // 对象转 JSON 字符串
    // static std::string obj_2_json(const std::any& obj) {
    //     auto j = nlohmann::to_json(obj);
    // }

    // JSON 字符串转对应类型的对象
    static void* json_2_obj(json &j, const std::string& type) {
        auto it = TYPE_MAP.find(type);
        if (it == TYPE_MAP.end()) {
            std::string t = TYEP_ALIAS_MAP[type];
            if (t != type && ! t.empty()) {
                it = TYPE_MAP.find(t);
            }
        }

        if (it != TYPE_MAP.end()) {
            return it->second(j);
        }

        throw std::runtime_error("Unknown type: "+ type + ", call add_type firstly!");
    }


    // 对象转对象
    // static void* obj_2_obj(const std::any& obj, const std::string& type) {
    //     auto str = obj_2_json(obj);
    //     return json_2_obj(str, type);
    // }

    // JSON 字符串转对应类型的对象
    static std::any json_2_any(json &j, const std::string& type) {
        auto it = STRUCT_MAP.find(type);
        if (it == STRUCT_MAP.end()) {
            std::string t = TYEP_ALIAS_MAP[type];
            if (t != type && ! t.empty()) {
                it = STRUCT_MAP.find(t);
            }
        }

        if (it != STRUCT_MAP.end()) {
            return it->second(j);
        }

        throw std::runtime_error("Unknown type: "+ type + ", call add_type firstly!");
    }

    // any_to_json 函数
    json _any_to_json(const std::any& value, std::string type) {
        type = trim_type(type);
        std::string type2 = get_type(value);

        if (type.empty()) {
            type = type2;
        }

        auto it = CAST_MAP.find(type);
        if (it == CAST_MAP.end()) {
            std::string t = TYEP_ALIAS_MAP[type];
            if (t != type && ! t.empty()) {
                it = CAST_MAP.find(t);
            }

            if (it == CAST_MAP.end()) {
                it = CAST_MAP.find(type2);

                if (it == CAST_MAP.end()) {
                    t = TYEP_ALIAS_MAP[type2];
                    if (t != type2 && ! t.empty()) {
                        it = CAST_MAP.find(t);
                    }
                }
            }
        }

        if (it != CAST_MAP.end()) {
            return it->second(value);
        }

        try {
            auto j = std::any_cast<json>(value);
            return j;
        } catch (const nlohmann::json::parse_error& ex) {
            std::cout << "nlohmann::json::parse_error at byte " << ex.byte << ": " << ex.what() << std::endl;
        } catch (const nlohmann::json::type_error& ex) {
            std::cout << "nlohmann::json::type_error " << ex.what() << std::endl;
        } catch (const nlohmann::json::other_error& ex) {
            std::cout << "nlohmann::json::other_error " << ex.what() << std::endl;
        } catch (const std::exception& e) {
            std::cout << "add_cast j.dump() >> std::exceptione " << e.what() << std::endl;
        }

        std::stringstream ss;
        ss << &value;
        json j;
        j["type"] = get_type(value);
        j["value"] = ss.str();

        return j; //
        // return (json&) value;
    }

    // any_to_json 函数模板
    json any_to_json(const std::any& value, std::string type) {
        if (! value.has_value()) {
            json j;
            return j;
            // return nullptr;
        }

        try {
            if (value.type() == typeid(bool)) {
                return std::any_cast<bool>(value);
            }
            if (value.type() == typeid(std::byte)) {
                return std::any_cast<std::byte>(value);
            }
            if (value.type() == typeid(char)) {
                return std::any_cast<char>(value);
            }
            if (value.type() == typeid(short)) {
                return std::any_cast<short>(value);
            }
            if (value.type() == typeid(int)) {
                return std::any_cast<int>(value);
            }
            if (value.type() == typeid(long)) {
                return std::any_cast<long>(value);
            }
            if (value.type() == typeid(long long)) {
                return std::any_cast<long long>(value);
            }
            if (value.type() == typeid(float)) {
                return std::any_cast<float>(value);
            }
            if (value.type() == typeid(double)) {
                return std::any_cast<double>(value);
            }
            if (value.type() == typeid(std::string)) {
                return std::any_cast<std::string>(value);
            }

            // if (value.type() == typeid(bool&)) {
            //     return std::any_cast<bool&>(value);
            // }
            // if (value.type() == typeid(std::byte&)) {
            //     return std::any_cast<std::byte&>(value);
            // }
            // if (value.type() == typeid(char&)) {
            //     return std::any_cast<char&>(value);
            // }
            // if (value.type() == typeid(short&)) {
            //     return std::any_cast<short&>(value);
            // }
            // if (value.type() == typeid(int&)) {
            //     return std::any_cast<int&>(value);
            // }
            // if (value.type() == typeid(long&)) {
            //     return std::any_cast<long&>(value);
            // }
            // if (value.type() == typeid(long long&)) {
            //     return std::any_cast<long long&>(value);
            // }
            // if (value.type() == typeid(float&)) {
            //     return std::any_cast<float&>(value);
            // }
            // if (value.type() == typeid(double&)) {
            //     return std::any_cast<double&>(value);
            // }
            // if (value.type() == typeid(std::string&)) {
            //     return std::any_cast<std::string&>(value);
            // }

            if (value.type() == typeid(std::vector<bool>)) {
                return std::any_cast<std::vector<bool>>(value);
            }
            if (value.type() == typeid(std::vector<char>)) {
                return std::any_cast<std::vector<char>>(value);
            }
            if (value.type() == typeid(std::vector<std::byte>)) {
                return std::any_cast<std::vector<std::byte>>(value);
            }
            if (value.type() == typeid(std::vector<short>)) {
                return std::any_cast<short>(value);
            }
            if (value.type() == typeid(std::vector<int>)) {
                return std::any_cast<std::vector<int>>(value);
            }
            if (value.type() == typeid(std::vector<long>)) {
                return std::any_cast<std::vector<long>>(value);
            }
            if (value.type() == typeid(std::vector<long long>)) {
                return std::any_cast<std::vector<long long>>(value);
            }
            if (value.type() == typeid(std::vector<float>)) {
                return std::any_cast<std::vector<float>>(value);
            }
            if (value.type() == typeid(std::vector<double>)) {
                return std::any_cast<std::vector<double>>(value);
            }
            if (value.type() == typeid(std::vector<std::string>)) {
                return std::any_cast<std::vector<std::string>>(value);
            }
            // if (value.type() == typeid(std::vector<std::any>)) {
            //     return (std::vector<std::any>) value; //  std::any_cast<std::vector<std::any>>(value);
            // }

            if (value.type() == typeid(std::map<std::string, bool>)) {
                return std::any_cast<std::map<std::string, bool>>(value);
            }
            if (value.type() == typeid(std::map<std::string, std::byte>)) {
                return std::any_cast<std::map<std::string, std::byte>>(value);
            }
            if (value.type() == typeid(std::map<std::string, char>)) {
                return std::any_cast<std::map<std::string, char>>(value);
            }
            if (value.type() == typeid(std::map<std::string, short>)) {
                return std::any_cast<std::map<std::string, short>>(value);
            }
            if (value.type() == typeid(std::map<std::string, int>)) {
                return std::any_cast<std::map<std::string, int>>(value);
            }
            if (value.type() == typeid(std::map<std::string, long>)) {
                return std::any_cast<std::map<std::string, long>>(value);
            }
            if (value.type() == typeid(std::map<std::string, long long>)) {
                return std::any_cast<std::map<std::string, long long>>(value);
            }
            if (value.type() == typeid(std::map<std::string, std::string>)) {
                return std::any_cast<std::map<std::string, std::string>>(value);
            }
            // if (value.type() == typeid(std::map<std::string, std::any>)) {
            //     return std::any_cast<std::map<std::string, std::any>>(value);
            // }
        } catch (const std::exception& e) {
            std::cout << e.what() << std::endl;
        } catch (const nlohmann::json::parse_error& ex) {
            std::cout << "nlohmann::json::parse_error at byte " << ex.byte << ": " << ex.what() << std::endl;
        } catch (const nlohmann::json::type_error& ex) {
            std::cout << "nlohmann::json::type_error " << ex.what() << std::endl;
        } catch (const nlohmann::json::other_error& ex) {
            std::cout << "nlohmann::json::other_error " << ex.what() << std::endl;
        }

        return _any_to_json(value, type);
    }

    static std::any json_to_any(json &j) {
        if (j.is_null()) {
            return nullptr;
        }

        if (j.is_number_integer()) {
            return j.get<long>();
        }

        if (j.is_number_float()) {
            return j.get<float>();
        }

        if (j.is_number()) {
            return j.get<double>();
        }

        if (j.is_string()) {
            // return j.get<std::string>();
            std::string val = j.get<std::string>();
            size_t ind = val.find(':');
            // const char *s = val.c_str();
            // auto pc = strchr(s, ':');
            // int ind = pc - s;
            if (ind == std::string::npos || ind < 0) {
                return val;
            }

            std::string type = val.substr(0, ind);
            std::string vs = val.substr(ind + 1);

            if (type == TYPE_ANY) {
                if (vs == "nullptr") { // || vs == "null") {
                    return nullptr;
                }
                if (vs == "NULL") {
                    return NULL;
                }
                return vs;
            }
            if (type == TYPE_BOOL) {
                if (vs == "true") {
                    return true;
                }
                if (vs == "false") { //  || vs == "") {
                    return false;
                }
                throw vs + " cannot be cast to bool! only true, false illegal!";
            }
            if (type == TYPE_CHAR) {
                if (vs.size() != 1) {
                    throw vs + " size != 1 ! cannot be cast to char!";
                }
                return vs.at(0);
            }
            if (type == TYPE_BYTE || type == TYPE_SHORT || type == TYPE_INT) {
                return std::stoi(vs);
            }
            if (type == TYPE_LONG) {
                return std::stol(vs);
            }
            if (type == TYPE_LONG_LONG) {
                return std::stoll(vs);
            }
            if (type == TYPE_FLOAT) {
                return std::stof(vs);
            }
            if (type == TYPE_DOUBLE) {
                return std::stod(vs);
            }
            if (type == TYPE_STRING) {
                return vs;
            }

            json j = vs;
            try {
                return json_2_obj(j, type);
            } catch (const std::exception& e) {
                return json_2_any(j, type);
            } catch (const nlohmann::json::parse_error& ex) {
                std::cout << "nlohmann::json::parse_error at byte " << ex.byte << ": " << ex.what() << std::endl;
            } catch (const nlohmann::json::type_error& ex) {
                std::cout << "nlohmann::json::type_error " << ex.what() << std::endl;
            } catch (const nlohmann::json::other_error& ex) {
                std::cout << "nlohmann::json::other_error " << ex.what() << std::endl;
            }
        }

        if (j.is_object()) {
            json type = j["type"];
            if (type.is_null() || type.empty()) {
                return j;
            }

            type = trim_type(type.get<std::string>());

            auto value = j["value"];
            if (type == TYPE_BOOL) {
                return value.get<bool>();
            }
            if (type == TYPE_CHAR) {
                return value.get<char>();
            }
            if (type == TYPE_BYTE) {
                return value.get<std::byte>();
            }
            if (type == TYPE_SHORT) {
                return value.get<short>();
            }
            if (type == TYPE_INT) {
                return value.get<int>();
            }
            if (type == TYPE_LONG) {
                return value.get<long>();
            }
            if (type == TYPE_LONG_LONG) {
                return value.get<long long>();
            }
            if (type == TYPE_FLOAT) {
                return value.get<float>();
            }
            if (type == TYPE_DOUBLE) {
                return value.get<double>();
            }
            if (type == TYPE_STRING) {
                return value.get<std::string>();
            }
            // if (type == "any" || type == "std::any") {
            //     return j;
            // }

            std::string type_s = type;
            int l = type_s.size();
            if (l > 2 && type_s.substr( l - 2, l) == "[]") {
                if (type == TYPE_BOOL_ARR) {
                    auto vec = value.get<std::vector<bool>>();
                    bool arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }

                    return *arr;
                }

                if (type == TYPE_CHAR_ARR) {
                    auto vec = value.get<std::vector<char>>();
                    char arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }
                    return *arr;
                }

                if (type == TYPE_BYTE_ARR) {
                    auto vec = value.get<std::vector<std::byte>>();
                    std::byte arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }
                    return *arr;
                }

                if (type == TYPE_SHORT_ARR) {
                    auto vec = value.get<std::vector<short>>();
                    short arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }
                    return *arr;
                }

                if (type == TYPE_INT_ARR) {
                    auto vec = value.get<std::vector<int>>();
                    int arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }
                    return *arr;
                }

                if (type == TYPE_LONG_ARR) {
                    auto vec = value.get<std::vector<long>>();
                    long arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }
                    return *arr;
                }

                if (type == TYPE_LONG_LONG_ARR) {
                    auto vec = value.get<std::vector<long long>>();
                    long long arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }
                    return *arr;
                }

                if (type == TYPE_FLOAT_ARR) {
                    auto vec = value.get<std::vector<float>>();
                    float arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }
                    return *arr;
                }

                if (type == TYPE_DOUBLE_ARR) {
                    auto vec = value.get<std::vector<double>>();
                    double arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }
                    return *arr;
                }

                if (type == TYPE_STRING_ARR) {
                    auto vec = value.get<std::vector<std::string>>();
                    std::string arr[vec.size()];
                    for (int i = 0; i < vec.size(); ++i) {
                        arr[i] = vec.at(i);
                    }
                    return *arr;
                }
            }
            else if (l > strlen("vector<")) { // && type_s.substr(0, strlen("vector<")) == "vector<") { // && type_s.find('<') < type_s.find_last_of('>')) {
                if (type == "vector<bool>") {
                    auto vec = value.get<std::vector<bool>>();
                    return vec;
                }

                if (type == "vector<char>") {
                    auto vec = value.get<std::vector<char>>();
                    return vec;
                }

                if (type == "vector<byte>") {
                    auto vec = value.get<std::vector<std::byte>>();
                    return vec;
                }

                if (type == "vector<short>") {
                    auto vec = value.get<std::vector<short>>();
                    return vec;
                }

                if (type == "vector<int>") {
                    auto vec = value.get<std::vector<int>>();
                    return vec;
                }

                if (type == "vector<long>") {
                    auto vec = value.get<std::vector<long>>();
                    return vec;
                }

                if (type == "vector<long long>") {
                    auto vec = value.get<std::vector<long long>>();
                    return vec;
                }

                if (type == "vector<float>") {
                    auto vec = value.get<std::vector<float>>();
                    return vec;
                }

                if (type == "vector<double>") {
                    auto vec = value.get<std::vector<double>>();
                    return vec;
                }

                if (type_s == "vector<string>") {
                    auto vec = value.get<std::vector<std::string>>();
                    return vec;
                }
            }

            try {
                return json_2_obj(value, type);
            } catch (const std::exception& e) {
                return json_2_any(value, type);
            } catch (const nlohmann::json::parse_error& ex) {
                std::cout << "nlohmann::json::parse_error at byte " << ex.byte << ": " << ex.what() << std::endl;
            } catch (const nlohmann::json::type_error& ex) {
                std::cout << "nlohmann::json::type_error " << ex.what() << std::endl;
            } catch (const nlohmann::json::other_error& ex) {
                std::cout << "nlohmann::json::other_error " << ex.what() << std::endl;
            }
        }

        if (j.is_array()) {
            std::vector<std::any> vec;
            for (int i = 0; i < j.size(); ++i) {
                auto arg = j[i];
                vec.push_back(json_to_any(arg));
            }
            return vec;
        }

        return static_cast<std::any>(j);
    }


    // 删除对象
    template<typename T>
    static void del_obj(void* obj) {
        try {
            delete static_cast<T*>(obj);
        } catch (const std::exception& e) {
            std::cout << e.what() << std::endl;
        }
    }

    template<typename T>
    T INSTANCE_GETTER(json &j) {
        if (j.empty()) {
            T obj = T();
            return obj;
        }

        T obj = T(j.get<T>());
        return obj;
    };

    template<typename T>
    T* INSTANCE_PTR_GETTER(json &j) {
        if (j.empty()) {
            T* obj = new T();
            return obj;
        }

        T* obj = new T(j.get<T>());
        return obj;
    };

    // 注册类型
    template<typename T>
    static void add_type(const std::string& type, T callback(json& j), json caster(std::any val)) {
        // typeid(T).name() 会得到 4User 这种带了其它字符的名称
        auto cb = [callback](json& j) -> void* {
            // if (callback == nullptr) {
            //     callback = INSTANCE_GETTER<T>;
            // }
            auto obj = callback != nullptr ? callback(j) : INSTANCE_GETTER<T>(j);
            auto p = &obj;
            return static_cast<void*>(p);
        };
        TYPE_MAP[type + "*"] = TYPE_MAP[type + "&"] = cb;

        std::string t = trim_type(demangle(typeid(T).name()));
        if (t != type) {
            TYPE_MAP[t + "*"] = TYPE_MAP[t + "&"] = cb;
            TYEP_ALIAS_MAP[t] = type;
            TYEP_ALIAS_MAP[type] = t;
        }

        add_cast<T>(type, caster);
    }

    template<typename T>
    static void add_type(const std::string& type, T callback(json& j)) {
        add_type<T>(type, callback, nullptr);
    }

    // 注册类型
    template<typename T>
    static void add_type(const std::string& type) {
        add_type<T>(type, nullptr);
    }


    // 取消注册类型
    static void remove_type(const std::string& type) {
        TYPE_MAP.erase(type);
    }

    // 注册类型
    template<typename T>
    static void add_struct(const std::string& type, T callback(json& j), json caster(std::any val)) {
        // typeid(T).name() 会得到 4User 这种带了其它字符的名称
        STRUCT_MAP[type] = [callback](json &j) -> std::any {
            auto obj = callback != nullptr ? callback(j) : INSTANCE_GETTER<T>(j);
            return std::any_cast<T>(obj);
        };

        std::string t = trim_type(demangle(typeid(T).name()));
        if (t != type) {
            STRUCT_MAP[t] = STRUCT_MAP[type];
            TYEP_ALIAS_MAP[t] = type;
            TYEP_ALIAS_MAP[type] = t;
        }

        add_type<T>(type, callback, caster);
    }

    // 注册类型
    template<typename T>
    static void add_struct(const std::string& type, T callback(json& j)) {
        add_struct<T>(type, callback, nullptr);
    }

    // 注册类型
    template<typename T>
    static void add_struct(const std::string& type) {
        add_struct<T>(type, nullptr);
    }

    // 取消注册类型
    static void remove_struct(const std::string& type) {
        STRUCT_MAP.erase(type);
    }


    // 函数与方法(成员函数) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    using FT = std::function<std::any(json &j, std::vector<std::any>)>;
    static std::map<std::string, FT> FUNC_MAP;

    // 执行已注册的函数/方法(成员函数)
    static std::any invoke(const std::string &name, std::vector<std::any> args) {
        auto it = FUNC_MAP.find(name);
        if (it != FUNC_MAP.end()) {
            json j;
            return it->second(j, args);
        }
        throw std::runtime_error("Unkown func: " + name + ", call add_func firstly!");
    }

    // 执行已注册的函数/方法(成员函数)
    static std::any invoke_method(json &thiz, const std::string &name, std::vector<std::any> args) {
        json type = thiz["type"];
        json value = thiz["value"];
        if (! type.empty()) {
            std::string t = trim_type(type.get<std::string>());
            auto it = TYPE_MAP.find(t);
            if (it != TYPE_MAP.end()) {
                it->second(value);
            } else {
                auto it2 = STRUCT_MAP.find(t);
                if (it2 != STRUCT_MAP.end()) {
                    it2->second(value);
                }
            }
        }

        auto it = FUNC_MAP.find(name);
        if (it != FUNC_MAP.end()) {
            return it->second(thiz, args);
        }
        throw std::runtime_error("Unkown func: " + name + ", call add_func firstly!");
    }

    // 执行非 void 函数
    template<typename Ret, typename... Args, std::size_t... I>
    static std::any invoke(std::function<Ret(Args...)> func, std::vector<std::any> &args, std::index_sequence<I...>) {
        return func(std::any_cast<Args>(args[I])...);
    }

    // 执行 void 函数
    template<typename... Args, std::size_t... I>
    static void invoke_void(std::function<void(Args...)> func, std::vector<std::any> &args, std::index_sequence<I...>) {
        func(std::any_cast<Args>(args[I])...);
    }

    // 执行非 void 方法(成员函数)，针对 class 等的指针方式
    template<typename Ret, typename T, typename... Args, std::size_t... I>
    static std::any invoke(T *instance, Ret (T::*func)(Args...), std::vector<std::any> &args, std::index_sequence<I...>) {
        return (instance->*func)(std::any_cast<Args>(args[I])...);
    }

    // 执行 void 方法(成员函数)
    template<typename T, typename... Args, std::size_t... I>
    static void invoke_void(T *instance, void (T::*func)(Args...), std::vector<std::any> &args, std::index_sequence<I...>) {
        (instance->*func)(std::any_cast<Args>(args[I])...);
    }

    // 执行非 void 方法(成员函数)，针对 struct 等的值类型方式
    template<typename Ret, typename T, typename... Args, std::size_t... I>
    static std::any invoke_struct(T instance, Ret (T::*func)(Args...), std::vector<std::any> &args, std::index_sequence<I...>) {
        return (instance.*func)(std::any_cast<Args>(args[I])...);
    }

    // 执行 void 方法(成员函数)
    template<typename T, typename... Args, std::size_t... I>
    static void invoke_struct_void(T instance, void (T::*func)(Args...), std::vector<std::any> &args, std::index_sequence<I...>) {
        (instance.*func)(std::any_cast<Args>(args[I])...);
    }


    // 注册函数
    template<typename Ret, typename... Args>
    static void add_func(const std::string &name, std::function<Ret(Args...)> func) {
        FUNC_MAP[name] = [func](json &j, std::vector<std::any> args) -> std::any {
            if constexpr (std::is_void_v<Ret>) {
                invoke_void(func, args, std::index_sequence_for<Args...>{});
                return nullptr;
            } else {
                return invoke(func, args, std::index_sequence_for<Args...>{});
            }
        };
    }

    // 注册方法(成员函数)，针对 class 等的指针方式
    template<typename Ret, typename T, typename... Args>
    static void add_func(const std::string &name, T *instance, Ret (T::*func)(Args...)) {
        FUNC_MAP["&" + name] = [&instance, func](json &j, std::vector<std::any> args) -> std::any {
            std::string type = j["type"];
            json value = j["value"];
            if (instance == nullptr || ! value.empty()) {
                auto ins = INSTANCE_GETTER<T>(value); // static_cast<T>(ins);
                instance = &ins;
            }

            std::any ret = nullptr;
            if constexpr (std::is_void_v<Ret>) {
                invoke_void(instance, func, args, std::index_sequence_for<Args...>{});
            } else {
                ret = invoke(instance, func, args, std::index_sequence_for<Args...>{});
            }

            if (! j.empty()) {
                json v = any_to_json(instance, type);
                std::string t = get_type(instance);
                if (! t.empty()) {
                    j["type"] = t;
                    if (v.empty()) {
                        v = any_to_json(instance, type);
                    }
                }

                if (! v.empty()) {
                    j["value"] = v;
                }
            }

            return ret;
        };
    }

    // 注册方法(成员函数)，针对 struct 等的值类型方式
    template<typename Ret, typename T, typename... Args>
    static void add_func(const std::string &name, T instance, Ret (T::*func)(Args...)) {
        FUNC_MAP[name] = [instance, func](json &j, std::vector<std::any> args) -> std::any {
            // if (! j.empty()) {
            //     j.get_to(instance);
            // }

            if constexpr (std::is_void_v<Ret>) {
                invoke_struct_void(instance, func, args, std::index_sequence_for<Args...>{});
                return nullptr;
            } else {
                return invoke_struct(instance, func, args, std::index_sequence_for<Args...>{});
            }
        };

        add_func(name, &instance, func);
    }

    // 函数与方法(成员函数) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    static std::map<std::string, void*> INSTANCE_MAP; // = {
    //     {TYPE_BOOL, false},
    //     {TYPE_CHAR, ''},
    //     {TYPE_SHORT, static_cast<short>(1)},
    //     {TYPE_INT, 1},
    //     {TYPE_LONG, 0L},
    //     {TYPE_LONG, 0L},
    //     {TYPE_LONG, 0L},
    //     {TYPE_LONG, 0L}
    // };
    // INSTANCE_MAP[TYPE_BOOL] = false;

    static void init() {
        // TYPE_MAP[TYPE_INT] = typeid(0);

    }


    template <typename Func>
    struct function_traits;

    template <typename Ret, typename... Args>
    struct function_traits<std::function<Ret(Args...)>> {
        using return_type = Ret;
        using argument_types = std::tuple<Args...>;
    };

    static nlohmann::json list_json(nlohmann::json j) {
        nlohmann::json result;

        try {
            json method = j["method"];
            std::string mtd = method.empty() ? "" : method.get<std::string>();

            json package = j["package"];
            std::string pkg = package.empty() ? "" : package.get<std::string>();

            json clazz = j["class"];
            std::string cls = clazz.empty() ? "" : clazz.get<std::string>();

            int packageTotal = 0;
            int classTotal = 0;
            int methodTotal = 0;

            json packageList;

            for (const auto& kv : FUNC_MAP) {
                auto key = kv.first;
                auto value = kv.second;

                auto ind = key.find_last_of('.');
                std::string pkg2 = "";
                std::string cls2 = "";
                std::string mtd2 = "";
                while (ind != std::string::npos && ind >= 0) {
                    if (mtd2.empty()) {
                        mtd2 = key.substr(ind + 1);
                    } else {
                        auto last = key.substr(ind + 1);
                        auto first = cls2.empty() ? last.at(0) : '0';
                        if (first >= 'A' && first <= 'Z') {
                            cls2 = last;
                        }
                        else {
                            pkg2 += (pkg2.empty() ? "" : ".") + last;
                        }
                    }

                    key = key.substr(0, ind);
                    ind = key.find_last_of('.');
                }

                if (mtd2.empty()) {
                    mtd2 = key;
                }

                if ((pkg2 != pkg && ! pkg.empty()) || (cls2 != cls && ! cls.empty()) || (mtd2 != mtd && ! mtd.empty())) {
                    continue;
                }

                std::string path2 = "";
                if (! cls.empty()) {
                    path2 = cls + "." + path2;
                }
                if (! pkg.empty()) {
                    path2 = pkg + "." + path2;
                }

                auto it = FUNC_MAP.find(path2);
                if (it == FUNC_MAP.end()) {
                    it = FUNC_MAP.find(mtd2);
                }

                if (it == FUNC_MAP.end()) {
                    continue;
                }

                auto func = it->second;
                if (func == nullptr) {
                    continue;
                }

                using traits = function_traits<decltype(func)>;
                // std::cout << "Argument types: ";
                // std::apply( { ((std::cout << typeid(args).name() << " "), ...); }, traits::argument_types{});
                // std::cout << std::endl;

                json mtdObj;
                mtdObj["name"] = mtd2;
                // mtdObj["parameterTypeList"] = parameterTypeList;
                // mtdObj["genericParameterTypeList"] = genericParameterTypeList;
                mtdObj["returnType"] = trim_type(demangle(typeid(traits::return_type).name()));
                mtdObj["genericReturnType"] = trim_type(demangle(typeid(traits::return_type).name()));
                // mtdObj["static"] = is_static;
                // mtdObj["exceptionTypeList"] = exceptionTypeList;
                // mtdObj["genericExceptionTypeList"] = genericExceptionTypeList;
                // mtdObj["parameterDefaultValueList"] = parameterDefaultValueList;


                json pkgObj;
                int pkgInd = -1;
                for (int i = 0; i < packageList.size(); ++i) {
                    json po = packageList[i];
                    if (po["package"] == pkg2) {
                        pkgObj = po;
                        pkgInd = i;
                        break;
                    }
                }

                if (pkgObj.empty()) {
                    pkgObj["package"] = pkg2;
                    pkgObj["classTotal"] = 1;

                    packageList.push_back(pkgObj);
                    packageTotal ++;
                } else {
                    auto t = pkgObj["classTotal"] ;
                    pkgObj["classTotal"] = t.get<int>() + 1;
                }

                if (packageList.empty()) {
                    packageList.push_back(pkgObj);
                }

                json classList = pkgObj["classList"];

                json clsObj;
                int clsInd = -1;
                for (int i = 0; i < classList.size(); ++i) {
                    json co = classList[i];
                    if (co["class"] == cls2) {
                        clsObj = co;
                        clsInd = i;
                        break;
                    }
                }

                if (clsObj.empty()) {
                    // auto clsConf = json_2_obj("", pkg2.empty() ? cls2 : pkg2 + "." + cls2);
                    // if (clsConf == nullptr) {
                    //     clsConf = json_2_obj("", cls2);
                    // }

                    clsObj["class"] = cls2;
                    // 父类 clsObj["type"] = typeid(clsConf).name();
                    clsObj["methodTotal"] = 1;

                    classList.push_back(clsObj);
                    classTotal ++;
                } else {
                    auto t = clsObj["methodTotal"] ;
                    pkgObj["methodTotal"] = t.get<int>() + 1;
                }

                if (classList.empty()) {
                    classList.push_back(clsObj);
                }

                json methodList = clsObj["methodList"];
                methodList.push_back(mtdObj);
                methodTotal ++;

                clsObj["methodList"] = methodList;

                if (clsInd < 0) {
                    clsInd = static_cast<int>(classList.size()) - 1;
                }
                classList[clsInd] = clsObj;
                pkgObj["classList"] = classList;

                if (pkgInd < 0) {
                    pkgInd = static_cast<int>(packageList.size()) - 1;
                }
                packageList[pkgInd] = pkgObj;
            }

            result["code"] = 200;
            result["msg"] = "success";
            result["packageTotal"] = packageTotal;
            result["classTotal"] = classTotal;
            result["methodTotal"] = methodTotal;
            result["packageList"] = packageList;
        } catch (const std::exception& e) {
            result["code"] = 500;
            result["msg"] = e.what();
        } catch (const nlohmann::json::parse_error& ex) {
            std::cout << "nlohmann::json::parse_error at byte " << ex.byte << ": " << ex.what() << std::endl;
            result["code"] = 500;
            result["msg"] = ex.what();
        } catch (const nlohmann::json::type_error& ex) {
            std::cout << "nlohmann::json::type_error " << ex.what() << std::endl;
            result["code"] = 500;
            result["msg"] = ex.what();
        } catch (const nlohmann::json::other_error& ex) {
            std::cout << "nlohmann::json::other_error " << ex.what() << std::endl;
            result["code"] = 500;
            result["msg"] = ex.what();
        }

        return result;
    }


    static nlohmann::json invoke_json(nlohmann::json j) {
        nlohmann::json result;

        try {
            json method = j["method"];
            std::string mtd = method.empty() ? "" : method.get<std::string>();
            if (mtd.empty()) {
                throw std::runtime_error("method cannot be empty! should be a string!");
            }

            json package = j["package"];
            std::string pkg = package.empty() ? "" : package.get<std::string>();

            json clazz = j["class"];
            std::string cls = clazz.empty() ? "" : clazz.get<std::string>();

            std::string path = mtd;
            if (! cls.empty()) {
                path = cls + "." + path;
            }
            if (! pkg.empty()) {
                path = pkg + "." + path;
            }

            json clsArgs = j["classArgs"];

            json thiz = j["this"];

            json is_static = j["static"];
            bool is_sttc = is_static.empty() ? thiz.empty() && clsArgs.empty() && (cls.empty() || cls.at(0) < 'A' || cls.at(0) > 'Z') : is_static.get<bool>();

            json tt;
            if (! is_sttc) {
                tt = thiz["type"];
                if (tt.empty()) {
                    thiz["type"] = tt = (pkg.empty() ? "" : pkg + ".") + cls;
                }

                std::string type = tt.get<std::string>();
                json value = thiz["value"];
                // this_ = json_2_obj(value, type);
            } else if (! (thiz.empty() && clsArgs.empty())) {
                throw std::runtime_error("static: true 时，this 和 classArgs 都必须不传或为空！");
            }

            nlohmann::json args_ = j["args"];
            if (args_.empty()) {
                args_ = j["methodArgs"];
            }

            std::vector<std::any> args;
            json methodArgs;

            for (int i = 0; i < args_.size(); ++i) {
                auto arg = args_.at(i);
                std::any a = json_to_any(arg);
                // std::any a = static_cast<std::any>(arg);
                args.push_back(a);

                json ma;
                std::string t;
                try {
                    t = get_type(a);
                    if (t.empty()) {
                        t = arg.type_name();
                    }
                    ma["type"] = t;
                } catch (const std::exception& e) {
                    std::cout << "invoke_json  try { \n auto type_cs = typeid(a).name();... \n } catch (const std::exception& e) = " << e.what() << " >> ma[\"type\"] = arg.type_name();" << std::endl;
                    t = arg.type_name();
                    ma["type"] = t;
                } catch (const nlohmann::json::parse_error& ex) {
                    std::cout << "nlohmann::json::parse_error at byte " << ex.byte << ": " << ex.what() << std::endl;
                } catch (const nlohmann::json::type_error& ex) {
                    std::cout << "nlohmann::json::type_error " << ex.what() << std::endl;
                } catch (const nlohmann::json::other_error& ex) {
                    std::cout << "nlohmann::json::other_error " << ex.what() << std::endl;
                }

                try {
                    ma["value"] = any_to_json(a, ma["type"].get<std::string>());
                } catch (const std::exception& e) {
                    std::cout << "invoke_json  try { \n ma[\"value\"] = any_to_json(a); \n } catch (const std::exception& e) = " << e.what() << " >> ma[\"value\"] = arg;" << std::endl;
                    ma["value"] = arg;
                } catch (const nlohmann::json::parse_error& ex) {
                    std::cout << "nlohmann::json::parse_error at byte " << ex.byte << ": " << ex.what() << std::endl;
                } catch (const nlohmann::json::type_error& ex) {
                    std::cout << "nlohmann::json::type_error " << ex.what() << std::endl;
                } catch (const nlohmann::json::other_error& ex) {
                    std::cout << "nlohmann::json::other_error " << ex.what() << std::endl;
                }

                methodArgs.push_back(ma);
            }

            std::any ret = invoke_method(thiz, path, args);

            result["code"] = 200;
            result["msg"] = "success";
            std::string type = get_type(ret);
            if (! type.empty()) {
                result["type"] = type;  // type_cs;

                json v = any_to_json(ret, type);
                result["return"] = v;
            }

            if (! is_sttc) {
                std::string type = tt.get<std::string>();
                result["this"] = any_to_json(thiz, type);
            }

            result["methodArgs"] = methodArgs; // any_to_json(args);
        } catch (const std::exception& e) {
            result["code"] = 500;
            result["msg"] = e.what();
        } catch (const nlohmann::json::parse_error& ex) {
            std::cout << "nlohmann::json::parse_error at byte " << ex.byte << ": " << ex.what() << std::endl;
            result["code"] = 500;
            result["msg"] = ex.what();
        } catch (const nlohmann::json::type_error& ex) {
            std::cout << "nlohmann::json::type_error " << ex.what() << std::endl;
            result["code"] = 500;
            result["msg"] = ex.what();
        } catch (const nlohmann::json::other_error& ex) {
            std::cout << "nlohmann::json::other_error " << ex.what() << std::endl;
            result["code"] = 500;
            result["msg"] = ex.what();
        }

        return result;
    }

    // 生成覆盖率报告
    void generate_coverage_report() {
        // 生成覆盖率数据
        system("lcov --capture --directory . --output-file coverage.info");
        // 过滤掉不需要的数据
        system("lcov --remove coverage.info '/usr/*' '*/tests/*' '*/external/*' --output-file coverage_filtered.info");
        // 生成 HTML 报告
        system("genhtml coverage_filtered.info --output-directory coverage");
    }

    static bool coverage_enabled = false;

    // 清除之前的覆盖率数据
    void reset_coverage_data() {
        system("lcov --directory . --zerocounters");
    }

    // 启动覆盖率统计
    void start_coverage() {
        if (! coverage_enabled) {
            reset_coverage_data();
            coverage_enabled = true;
            std::cout << "Coverage collection started." << std::endl;
        }
    }

    // 停止覆盖率统计并生成报告
    void stop_coverage() {
        if (coverage_enabled) {
            coverage_enabled = false;
            std::cout << "Coverage collection stopped." << std::endl;
            generate_coverage_report();
        }
    }

    // 读取文件内容
    std::string read_file(const std::string& file_path) {
        std::ifstream file(file_path);
        std::stringstream buffer;
        buffer << file.rdbuf();
        return buffer.str();
    }

    // 处理请求并生成响应
    inline void handle_request(int client_socket) {
        char buffer[1024];
        int bytes_received = recv(client_socket, buffer, sizeof(buffer), 0);
        if (bytes_received > 0) {
            std::string request(buffer, bytes_received);

            // 简单解析 HTTP 请求
            std::istringstream request_stream(request);
            std::string method, path, http_version;
            request_stream >> method >> path >> http_version;

            // 解析 JSON 数据（假设数据在请求体中）
            std::string json_data;
            // std::getline(request_stream, json_data); // 跳过空行
            // std::getline(request_stream, json_data);

            bool first = true;
            bool start = false;
            std::string host = "";
            int len = static_cast<int>(strlen("Origin:"));

            while (! request_stream.eof()) {
                std::string line;
                std::getline(request_stream, line); // 跳过空行
                std::string pre = line.length() < len ? "" : line.substr(0, len);
                if (host.empty() && (pre == "Origin:" || pre == "origin:")) {
                    host = line.substr(len + 1);
                }

                if (start) {
                    json_data += "\n" + line;
                } else if (! first) {
                    line.erase(std::remove_if(line.begin(), line.end(), ::isspace), line.end());
                    if (line.empty()) {
                        start = true;
                    }
                    else if (line.substr(0, 1) == "{") {
                        start = true;
                        json_data += line;
                    }
                }

                first = false;
            }

            int status = 200;
            std::string location = "";

            // 处理数据并生成响应 JSON
            std::string response_json = R"({
                "code": 200,
                "msg": "success"
            })";

            bool isOpt = method == "options" || method == "OPTIONS";
            bool isPost = method == "post" || method == "POST";
            bool isGet = method == "get" || method == "GET";
            bool isGetOrPost = isGet || isPost;

            if (isGetOrPost && path == "/coverage/start") {
                start_coverage();
                json result;
                result["code"] = 200;
                result["msg"] = "Coverage collection started";
                response_json = result.dump();
            }
            else if (isGetOrPost && path == "/coverage/stop") {
                stop_coverage();
                json result;
                result["code"] = 200;
                result["msg"] = "Coverage collection stopped and report generated";
                result["url"] = "/coverage_report/index.html";
                result["html"] = read_file("coverage_report/index.html");
                result["json"] = read_file("coverage_filtered.info");
                response_json = result.dump();
            }
            else if (isGetOrPost && path == "/coverage/save") {
                generate_coverage_report();
            }
            else if (isGetOrPost && path == "/coverage/report") {
                json result;
                result["code"] = 200;
                result["msg"] = "Coverage generated";
                result["url"] = "/coverage/index.html";
                result["html"] = read_file("coverage/index.html");
                result["json"] = read_file("coverage_filtered.info");
                response_json = result.dump();
            }
            else if (isGet && (path == "/coverage" || path == "/coverage/index.html")) {
                status = 301;
                location = "Location: " + host + "/coverage/index.html";
            }
            else if (isPost) {
                json j = json::parse(json_data);
                if (path == "/method/invoke") {
                    json result = invoke_json(j);
                    response_json = result.dump(-1, ' ', false, nlohmann::detail::error_handler_t::ignore);
                }
                else if (path == "/method/list") {
                    json result = list_json(j);
                    response_json = result.dump(-1, ' ', false, nlohmann::detail::error_handler_t::ignore);
                }
                else {
                    response_json = R"({
                    "code": 404,
                    "msg": "Only support POST /method/invoke, POST /method/list, POST /coverage/save ！"
                })";
                }
            }
            else if (! isOpt) {
                response_json = R"({
                    "code": 400,
                    "msg": "Only support HTTP POST Method！"
                })";
            }

            // 构建 HTTP 响应
            std::ostringstream response;
            response << "HTTP/1.1 " << status << " OK\r\n";
            response << "Content-Type: application/json\r\n";
            response << "Access-Control-Allow-Origin:" + host + "\n";
            response << "Access-Control-Allow-Credentials: true\r\n";
            response << "Access-Control-Allow-Headers: content-type\r\n";
            response << "Access-Control-Request-Method: POST\r\n";
            response << "Content-Length: " << response_json.size() << "\r\n";
            if (location.length() > 0) {
                response << location << "\r\n";
            }

            response << "\r\n";
            response << response_json;

            // 发送响应
            send(client_socket, response.str().c_str(), response.str().size(), 0);
        }
        close(client_socket);
    }

    static bool running = true;
    static void handle_signal(int signal) {
        if (signal == SIGINT) {
            running = false;
        }
    }

    static int start(int port) { // C++ 不支持重载方法
        port = port <= 0 ? 8084 : port;

        int server_socket = socket(AF_INET, SOCK_STREAM, 0);
        sockaddr_in server_addr;
        server_addr.sin_family = AF_INET;
        server_addr.sin_addr.s_addr = INADDR_ANY;
        server_addr.sin_port = htons(port);

        if (bind(server_socket, (sockaddr*)&server_addr, sizeof(server_addr)) == -1) {
            std::cerr << "Failed to bind socket: " << strerror(errno) << std::endl;
            close(server_socket);
            return -1;
        }

        if (listen(server_socket, SOMAXCONN) == -1) {
            std::cerr << "Failed to listen socket: " << strerror(errno) << std::endl;
            close(server_socket);
            return -1;
        }

        std::cout << "Server is running on port " << port << "..." << std::endl;
        signal(SIGINT, handle_signal);

        fd_set read_fds;
        int max_fd = server_socket;

        while (running) {
            FD_ZERO(&read_fds);
            FD_SET(server_socket, &read_fds);

            struct timeval timeout;
            timeout.tv_sec = 1;
            timeout.tv_usec = 0;

            int activity = select(max_fd + 1, &read_fds, nullptr, nullptr, &timeout);
            if (activity < 0 && errno != EINTR) {
                std::cerr << "Server select error: " << strerror(errno) << std::endl;
                break;
            }

            if (activity > 0 && FD_ISSET(server_socket, &read_fds)) {
                int client_socket = accept(server_socket, nullptr, nullptr);
                if (client_socket >= 0) {
                    handle_request(client_socket);
                } else {
                    std::cerr << "Server sccept error: " << strerror(errno) << std::endl;
                }
            }
        }

        close(server_socket);
        std::cout << "Server stopped!" << std::endl;
        return 0;
    }

    // template<typename Type>
    // static void add_func(Type ins, ...) {
    //     auto tup = std::make_tuple(__VA_ARGS__); \
    //     unitauto::add_func(f, ins, std::get<0>(tup)); \
    // }

    static std::vector<std::string> splitMethodDef(std::string fs) {
        auto vec = std::vector<std::string>();

        fs = std::regex_replace(fs, std::regex(" "), "");
        fs = std::regex_replace(fs, std::regex("::"), ".");
        while (true) {
            int ind = fs.find(',');
            bool no = ind < 0 || ind == std::string::npos;
            std::string f = no ? fs : fs.substr(0, ind);

            vec.push_back(f);
            if (no) {
                break;
            }
            fs = fs.substr(ind + 1);
        }

        return vec;
    }


    #define UNITAUTO_EXPAND( x ) x
    #define UNITAUTO_GET_MACRO(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, _21, _22, _23, _24, _25, _26, _27, _28, _29, _30, _31, _32, _33, _34, _35, _36, _37, _38, _39, _40, _41, _42, _43, _44, _45, _46, _47, _48, _49, _50, _51, _52, _53, _54, _55, _56, _57, _58, _59, _60, _61, _62, _63, _64, NAME,...) NAME
    #define UNITAUTO_PASTE(...) UNITAUTO_EXPAND(UNITAUTO_GET_MACRO(__VA_ARGS__, \
            UNITAUTO_PASTE64, \
            UNITAUTO_PASTE63, \
            UNITAUTO_PASTE62, \
            UNITAUTO_PASTE61, \
            UNITAUTO_PASTE60, \
            UNITAUTO_PASTE59, \
            UNITAUTO_PASTE58, \
            UNITAUTO_PASTE57, \
            UNITAUTO_PASTE56, \
            UNITAUTO_PASTE55, \
            UNITAUTO_PASTE54, \
            UNITAUTO_PASTE53, \
            UNITAUTO_PASTE52, \
            UNITAUTO_PASTE51, \
            UNITAUTO_PASTE50, \
            UNITAUTO_PASTE49, \
            UNITAUTO_PASTE48, \
            UNITAUTO_PASTE47, \
            UNITAUTO_PASTE46, \
            UNITAUTO_PASTE45, \
            UNITAUTO_PASTE44, \
            UNITAUTO_PASTE43, \
            UNITAUTO_PASTE42, \
            UNITAUTO_PASTE41, \
            UNITAUTO_PASTE40, \
            UNITAUTO_PASTE39, \
            UNITAUTO_PASTE38, \
            UNITAUTO_PASTE37, \
            UNITAUTO_PASTE36, \
            UNITAUTO_PASTE35, \
            UNITAUTO_PASTE34, \
            UNITAUTO_PASTE33, \
            UNITAUTO_PASTE32, \
            UNITAUTO_PASTE31, \
            UNITAUTO_PASTE30, \
            UNITAUTO_PASTE29, \
            UNITAUTO_PASTE28, \
            UNITAUTO_PASTE27, \
            UNITAUTO_PASTE26, \
            UNITAUTO_PASTE25, \
            UNITAUTO_PASTE24, \
            UNITAUTO_PASTE23, \
            UNITAUTO_PASTE22, \
            UNITAUTO_PASTE21, \
            UNITAUTO_PASTE20, \
            UNITAUTO_PASTE19, \
            UNITAUTO_PASTE18, \
            UNITAUTO_PASTE17, \
            UNITAUTO_PASTE16, \
            UNITAUTO_PASTE15, \
            UNITAUTO_PASTE14, \
            UNITAUTO_PASTE13, \
            UNITAUTO_PASTE12, \
            UNITAUTO_PASTE11, \
            UNITAUTO_PASTE10, \
            UNITAUTO_PASTE9, \
            UNITAUTO_PASTE8, \
            UNITAUTO_PASTE7, \
            UNITAUTO_PASTE6, \
            UNITAUTO_PASTE5, \
            UNITAUTO_PASTE4, \
            UNITAUTO_PASTE3, \
            UNITAUTO_PASTE2, \
            UNITAUTO_PASTE1)(__VA_ARGS__))
    #define UNITAUTO_PASTE2(func, v1) func(v1)
    #define UNITAUTO_PASTE3(func, v1, v2) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE2(func, v2)
    #define UNITAUTO_PASTE4(func, v1, v2, v3) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE3(func, v2, v3)
    #define UNITAUTO_PASTE5(func, v1, v2, v3, v4) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE4(func, v2, v3, v4)
    #define UNITAUTO_PASTE6(func, v1, v2, v3, v4, v5) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE5(func, v2, v3, v4, v5)
    #define UNITAUTO_PASTE7(func, v1, v2, v3, v4, v5, v6) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE6(func, v2, v3, v4, v5, v6)
    #define UNITAUTO_PASTE8(func, v1, v2, v3, v4, v5, v6, v7) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE7(func, v2, v3, v4, v5, v6, v7)
    #define UNITAUTO_PASTE9(func, v1, v2, v3, v4, v5, v6, v7, v8) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE8(func, v2, v3, v4, v5, v6, v7, v8)
    #define UNITAUTO_PASTE10(func, v1, v2, v3, v4, v5, v6, v7, v8, v9) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE9(func, v2, v3, v4, v5, v6, v7, v8, v9)
    #define UNITAUTO_PASTE11(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE10(func, v2, v3, v4, v5, v6, v7, v8, v9, v10)
    #define UNITAUTO_PASTE12(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE11(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)
    #define UNITAUTO_PASTE13(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE12(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12)
    #define UNITAUTO_PASTE14(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE13(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13)
    #define UNITAUTO_PASTE15(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE14(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14)
    #define UNITAUTO_PASTE16(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE15(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15)
    #define UNITAUTO_PASTE17(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE16(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16)
    #define UNITAUTO_PASTE18(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE17(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17)
    #define UNITAUTO_PASTE19(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE18(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18)
    #define UNITAUTO_PASTE20(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE19(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19)
    #define UNITAUTO_PASTE21(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE20(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20)
    #define UNITAUTO_PASTE22(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE21(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21)
    #define UNITAUTO_PASTE23(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE22(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22)
    #define UNITAUTO_PASTE24(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE23(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23)
    #define UNITAUTO_PASTE25(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE24(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24)
    #define UNITAUTO_PASTE26(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE25(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25)
    #define UNITAUTO_PASTE27(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE26(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26)
    #define UNITAUTO_PASTE28(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE27(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27)
    #define UNITAUTO_PASTE29(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE28(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28)
    #define UNITAUTO_PASTE30(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE29(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29)
    #define UNITAUTO_PASTE31(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE30(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30)
    #define UNITAUTO_PASTE32(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE31(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31)
    #define UNITAUTO_PASTE33(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE32(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32)
    #define UNITAUTO_PASTE34(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE33(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33)
    #define UNITAUTO_PASTE35(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE34(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34)
    #define UNITAUTO_PASTE36(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE35(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35)
    #define UNITAUTO_PASTE37(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE36(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36)
    #define UNITAUTO_PASTE38(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE37(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37)
    #define UNITAUTO_PASTE39(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE38(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38)
    #define UNITAUTO_PASTE40(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE39(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39)
    #define UNITAUTO_PASTE41(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE40(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40)
    #define UNITAUTO_PASTE42(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE41(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41)
    #define UNITAUTO_PASTE43(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE42(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42)
    #define UNITAUTO_PASTE44(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE43(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43)
    #define UNITAUTO_PASTE45(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE44(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44)
    #define UNITAUTO_PASTE46(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE45(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45)
    #define UNITAUTO_PASTE47(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE46(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46)
    #define UNITAUTO_PASTE48(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE47(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47)
    #define UNITAUTO_PASTE49(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE48(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48)
    #define UNITAUTO_PASTE50(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE49(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49)
    #define UNITAUTO_PASTE51(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE50(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50)
    #define UNITAUTO_PASTE52(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE51(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51)
    #define UNITAUTO_PASTE53(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE52(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52)
    #define UNITAUTO_PASTE54(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE53(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53)
    #define UNITAUTO_PASTE55(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE54(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54)
    #define UNITAUTO_PASTE56(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE55(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55)
    #define UNITAUTO_PASTE57(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE56(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56)
    #define UNITAUTO_PASTE58(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE57(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57)
    #define UNITAUTO_PASTE59(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE58(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58)
    #define UNITAUTO_PASTE60(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE59(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59)
    #define UNITAUTO_PASTE61(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59, v60) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE60(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59, v60)
    #define UNITAUTO_PASTE62(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59, v60, v61) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE61(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59, v60, v61)
    #define UNITAUTO_PASTE63(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59, v60, v61, v62) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE62(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59, v60, v61, v62)
    #define UNITAUTO_PASTE64(func, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59, v60, v61, v62, v63) UNITAUTO_PASTE2(func, v1) UNITAUTO_PASTE63(func, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59, v60, v61, v62, v63)


    //  Compile Error #define UNITAUTO_ADD_FUNC(Type, ...) \
    //     unitauto::add_struct<Type>(#Type); \
    //     Type ins = Type(); \
    //     std::string fs = #__VA_ARGS__; \
    //     while (true) { \
    //         int ind = fs.find(','); \
    //         bool no = ind < 0 || ind == std::string::npos; \
    //         std::string f = no ? fs : fs.substr(0, ind); \
    //         f = std::regex_replace(f, std::regex(" "), ""); \
    //         f = std::regex_replace(f, std::regex("::"), "."); \
    //         \
    //         if (ind == 0) { \
    //             auto tup = std::make_tuple(__VA_ARGS__); \
    //             unitauto::add_func(f, ins, std::get<0>(tup)); \
    //         } \
    //         else if (ind == 1) { \
    //             auto tup = std::make_tuple(__VA_ARGS__); \
    //             unitauto::add_func(f, ins, std::get<1>(tup)); \
    //         } \
    //         else if (ind == 2) { \ Compile Error
    //             auto tup2 = std::make_tuple(__VA_ARGS__); \
    //             unitauto::add_func(f, ins, std::get<2>(tup2)); \
    //         } \
    //         \
    //         if (no) { \
    //             break; \
    //         } \
    //         fs = fs.substr(ind + 1); \
    //     } \
    //     \


    #define UNITAUTO_ADD_FUNC_1(...) \
        s = #__VA_ARGS__; \
        s = std::regex_replace(s, std::regex(" "), ""); \
        s = std::regex_replace(s, std::regex("::"), "."); \
        unitauto::add_func(s, std::function(__VA_ARGS__)); \

    #define UNITAUTO_ADD_METHOD_1(...) \
        s = #__VA_ARGS__; \
        s = s.substr(1); \
        s = std::regex_replace(s, std::regex(name), path); \
        s = std::regex_replace(s, std::regex(" "), ""); \
        s = std::regex_replace(s, std::regex("::"), "."); \
        unitauto::add_func(s, ins, __VA_ARGS__); \


    // #define UNITAUTO_ADD_FUNC_2(Type, ...) \
    //     unitauto::add_struct<Type>(#Type); \
    //     Type ins = Type(); \
    //     std::string fs = #__VA_ARGS__; \
    //     std::vector<std::string> vec = unitauto::splitMethodDef(#__VA_ARGS__); \
    //     auto tup = std::make_tuple(__VA_ARGS__); \
    //     unitauto::add_func(vec.at(0), ins, std::get<0>(tup)); \
    //     unitauto::add_func(vec.at(1), ins, std::get<1>(tup)); \
    //
    // too many codes  #define UNITAUTO_ADD_FUNC_20(Type, ...) \
    //     unitauto::add_struct<Type>(#Type); \
    //     Type ins = Type(); \
    //     std::string fs = #__VA_ARGS__; \
    //     std::vector<std::string> vec = unitauto::splitMethodDef(#__VA_ARGS__); \
    //     auto tup = std::make_tuple(__VA_ARGS__); \
    //     unitauto::add_func(vec.at(0), ins, std::get<0>(tup)); \
    //     unitauto::add_func(vec.at(1), ins, std::get<1>(tup)); \
    //     unitauto::add_func(vec.at(2), ins, std::get<2>(tup)); \
    //     unitauto::add_func(vec.at(3), ins, std::get<3>(tup)); \
    //     unitauto::add_func(vec.at(4), ins, std::get<4>(tup)); \
    //     unitauto::add_func(vec.at(5), ins, std::get<5>(tup)); \
    //     unitauto::add_func(vec.at(6), ins, std::get<6>(tup)); \
    //     unitauto::add_func(vec.at(7), ins, std::get<7>(tup)); \
    //     unitauto::add_func(vec.at(8), ins, std::get<8>(tup)); \
    //     unitauto::add_func(vec.at(9), ins, std::get<9>(tup)); \
    //     unitauto::add_func(vec.at(1), ins, std::get<10>(tup)); \
    //     unitauto::add_func(vec.at(11), ins, std::get<11>(tup)); \
    //     unitauto::add_func(vec.at(12), ins, std::get<12>(tup)); \
    //     unitauto::add_func(vec.at(13), ins, std::get<13>(tup)); \
    //     unitauto::add_func(vec.at(14), ins, std::get<14>(tup)); \
    //     unitauto::add_func(vec.at(15), ins, std::get<15>(tup)); \
    //     unitauto::add_func(vec.at(16), ins, std::get<16>(tup)); \
    //     unitauto::add_func(vec.at(17), ins, std::get<17>(tup)); \
    //     unitauto::add_func(vec.at(18), ins, std::get<18>(tup)); \
    //     unitauto::add_func(vec.at(19), ins, std::get<19>(tup)); \


    #define UNITAUTO_ADD_FUNC(...) \
        { \
            std::string s; \
            UNITAUTO_EXPAND(UNITAUTO_PASTE(UNITAUTO_ADD_FUNC_1, __VA_ARGS__)) \
        } \

    #define UNITAUTO_ADD_METHOD(Type, ...) \
        { \
            std::string name = #Type; \
            unitauto::add_struct<Type>(name); \
            Type ins; /* = Type(); */ \
            const std::type_info& ti = typeid(ins); \
            std::string path = unitauto::demangle(ti.name()); \
            std::string s; \
            UNITAUTO_EXPAND(UNITAUTO_PASTE(UNITAUTO_ADD_METHOD_1, __VA_ARGS__)) \
        } \

}
