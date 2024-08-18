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

namespace unitauto {
    using json = nlohmann::json;

    static std::unordered_map<std::string, std::function<void*(const std::string&)>> TYPE_MAP;

    // 对象转 JSON 字符串
    // static std::string obj_2_json(const std::any& obj) {
    //     auto j = nlohmann::to_json(obj);
    // }

    // JSON 字符串转对应类型的对象
    static void* json_2_obj(const std::string& str, const std::string& type) {
        auto it = TYPE_MAP.find(type);
        if (it != TYPE_MAP.end()) {
            return it->second(str);
        }

        throw std::runtime_error("Unknown type: "+ type + ", call add_type firstly!");
    }

    // 对象转对象
    // static void* obj_2_obj(const std::any& obj, const std::string& type) {
    //     auto str = obj_2_json(obj);
    //     return json_2_obj(str, type);
    // }

    // 删除对象
    template<typename T>
    static void del_obj(void* obj) {
        delete static_cast<T*>(obj);
    }

    // 注册类型
    template<typename T>
    static void add_type(const std::string& type) {
        TYPE_MAP[type] = [](const std::string& str) -> void* {
            json j = json::parse(str);
            T* obj = new T(j.get<T>());
            return static_cast<void*>(obj);
        };
    }

    // 取消注册类型
    static void remove_type(const std::string& type) {
        TYPE_MAP.erase(type);
    }


    // 函数与方法(成员函数) <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    using FT = std::function<std::any(std::vector<std::any>)>;
    static std::map<std::string, FT> FUNC_MAP;

    // 执行已注册的函数/方法(成员函数)
    static std::any invoke(const std::string &name, std::vector<std::any> args) {
        auto it = FUNC_MAP.find(name);
        if (it != FUNC_MAP.end()) {
            return it->second(args);
        }
        throw std::runtime_error("Unkown func: " + name + ", call add_func/add_func firstly!");
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

    // 执行非 void 方法(成员函数)
    template<typename Ret, typename T, typename... Args, std::size_t... I>
    static std::any invoke(T *instance, Ret (T::*func)(Args...), std::vector<std::any> &args, std::index_sequence<I...>) {
        return (instance->*func)(std::any_cast<Args>(args[I])...);
    }

    // 执行 void 方法(成员函数)
    template<typename T, typename... Args, std::size_t... I>
    static void invoke_void(T *instance, void (T::*func)(Args...), std::vector<std::any> &args, std::index_sequence<I...>) {
        (instance->*func)(std::any_cast<Args>(args[I])...);
    }

    // 注册函数
    template<typename Ret, typename... Args>
    static void add_func(const std::string &name, std::function<Ret(Args...)> func) {
        FUNC_MAP[name] = [func](std::vector<std::any> args) -> std::any {
            if constexpr (std::is_void_v<Ret>) {
                invoke_void(func, args, std::index_sequence_for<Args...>{});
                return {};
            } else {
                return invoke(func, args, std::index_sequence_for<Args...>{});
            }
        };
    }

    // 注册方法(成员函数)
    template<typename Ret, typename T, typename... Args>
    static void add_func(const std::string &name, T *instance, Ret (T::*func)(Args...)) {
        // if (instance == nullptr) {
        //     FUNC_MAP[name] = [func](std::vector<std::any> args) -> std::any {
        //         if constexpr (std::is_void_v<Ret>) {
        //             invoke_void(func, args, std::index_sequence_for<Args...>{});
        //             return {};
        //         } else {
        //             return invoke(func, args, std::index_sequence_for<Args...>{});
        //         }
        //     };
        //     return;
        // }

        FUNC_MAP[name] = [instance, func](std::vector<std::any> args) -> std::any {
            if constexpr (std::is_void_v<Ret>) {
                invoke_void(instance, func, args, std::index_sequence_for<Args...>{});
                return {};
            } else {
                return invoke(instance, func, args, std::index_sequence_for<Args...>{});
            }
        };
    }

    // 函数与方法(成员函数) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    static const auto TYPE_ID_BOOL = typeid(bool).name();
    static const auto TYPE_ID_INT = typeid(int).name();
    static const auto TYPE_ID_LONG = typeid(long).name();
    static const auto TYPE_ID_FLOAT = typeid(float).name();
    static const auto TYPE_ID_DOUBLE = typeid(double).name();
    static const auto TYPE_ID_STR = typeid(std::string).name();
    // static const auto TYPE_ID_ARR = typeid(std::array).name();
    // static const auto TYPE_ID_OBJ = typeid(std::any).name();

    std::map<std::string, std::any> TYPE_ID_MAP = {
    };



    // 类型转换函数映射
    std::map<std::string, std::function<json(const std::any&)>> converters;

    // 注册类型转换函数
    template<typename T>
    void register_converter() {
        converters[typeid(T).name()] = [](std::any value) -> json {
            return std::any_cast<T>(value);
        };
    }


    // any_to_json 函数
    json _any_to_json(const std::any& value) {
        auto it = converters.find(value.type().name());
        if (it != converters.end()) {
            return it->second(value);
        } else {
            throw std::runtime_error("Unsupported type");
        }
    }

    // any_to_json 函数模板
    json any_to_json(const std::any& value) {
        if (value.type() == typeid(int)) {
            return std::any_cast<int>(value);
        } else if (value.type() == typeid(double)) {
            return std::any_cast<double>(value);
        } else if (value.type() == typeid(std::string)) {
            return std::any_cast<std::string>(value);
        } else if (value.type() == typeid(bool)) {
            return std::any_cast<bool>(value);
        } else if (value.type() == typeid(std::vector<int>)) {
            return std::any_cast<std::vector<int>>(value);
        } else if (value.type() == typeid(std::map<std::string, int>)) {
            return std::any_cast<std::map<std::string, int>>(value);
        } else {
            return _any_to_json(value);
            // throw std::runtime_error("Unsupported type");
        }
    }

    static std::any json_to_any(json j) {
        if (j.is_null()) {
            return NULL;
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

            if (type == "int") {
                return std::stoi(vs);
            }
            if (type == "long") {
                return std::stol(vs);
            }
            if (type == "float") {
                return std::stof(vs);
            }
            if (type == "double") {
                return std::stod(vs);
            }
            if (type == "string" || type == "std::string") {
                return vs;
            }

            return json_2_obj(vs, type);
        }

        if (j.is_object()) {
            json type = j["type"];
            if (type.is_null() || type.empty()) {
                return j;
            }

            auto value = j["value"];
            if (type == "int") {
                return value.get<int>();
            }
            if (type == "long") {
                return value.get<long>();
            }
            if (type == "float") {
                return value.get<float>();
            }
            if (type == "double") {
                return value.get<double>();
            }
            if (type == "string" || type == "std::string") {
                return value.get<std::string>();
            }
            // if (type == "any" || type == "std::any") {
            //     return j;
            // }

            return json_2_obj(value.dump(), type);
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

    static void init() {
        // TYPE_ID_MAP["int"] = typeid(0);

    }


    static nlohmann::json invoke_json(nlohmann::json j) {
        nlohmann::json result;

        try {
            json method = j["method"];
            std::string mtd = method.empty() ? "" : method.get<std::string>();
            if (mtd.empty()) {
                throw "method cannot be empty! should be a string!";
            }

            json is_static = j["static"];
            bool is_sttc = is_static.empty() ? false : is_static.get<bool>();

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
                try {
                    auto type_cs = typeid(a).name();  // type_id.name();
                    std::string type(type_cs);
                    if (type_cs == 0 || type.empty()) {
                        ma["type"] = arg.type_name();
                    } else {
                        ma["type"] = type;
                    }
                } catch (const std::exception& e) {
                    std::cout << "invoke_json  try { \n auto type_cs = typeid(a).name();... \n } catch (const std::exception& e) = " << e.what() << " >> ma[\"type\"] = arg.type_name();" << std::endl;
                    ma["type"] = arg.type_name();
                }

                try {
                    ma["value"] = any_to_json(a);
                } catch (const std::exception& e) {
                    std::cout << "invoke_json  try { \n ma[\"value\"] = any_to_json(a); \n } catch (const std::exception& e) = " << e.what() << " >> ma[\"value\"] = arg;" << std::endl;
                    ma["value"] = arg;
                }

                methodArgs.push_back(ma);
            }

            std::any ret = invoke(path, args);

            // auto type_id = typeid(ret);
            auto type_cs = typeid(ret).name();  // type_id.name();
            std::string type(type_cs);

            result["code"] = 200;
            result["msg"] = "success";
            if (! type.empty()) {
                result["type"] = type;  // type_cs;
            }

            result["return"] = any_to_json(ret);

            // if (ret.has_value()) {
            //     if (type_cs == TYPE_ID_BOOL) {
            //         result["return"] = std::__convert_to_bool<>(ret); //  static_cast<int>(ret);
            //     }
            //     else if (type_cs == TYPE_ID_INT) {
            //         result["return"] = std::to_integer(ret); //  static_cast<int>(ret);
            //     }
            //     else if (type_cs == TYPE_ID_STR) {
            //         result["return"] = std::to_string(ret); //  static_cast<int>(ret);
            //     }
            // }


            result["methodArgs"] = methodArgs; // any_to_json(args);
        } catch (const std::exception& e) {
            result["code"] = 500;
            result["msg"] = e.what();
        }

        return result;
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
            while (! request_stream.eof()) {
                std::string line;
                std::getline(request_stream, line); // 跳过空行
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

            // 处理数据并生成响应 JSON
            std::string response_json = R"({
                "code": 200,
                "msg": "success"
            })";


            if (method != "post" && method != "POST") {
                response_json = R"({
                    "code": 400,
                    "msg": "Only support HTTP POST Method！"
                })";
            }
            else if (path != "/method/invoke" && method != "/method/list") {
                response_json = R"({
                    "code": 404,
                    "msg": "Only support POST /method/invoke and POST /method/list ！"
                })";
            }
            else {
                json j = json::parse(json_data);
                json result = invoke_json(j);
                response_json = result.dump();
            }


            // 构建 HTTP 响应
            std::ostringstream response;
            response << "HTTP/1.1 200 OK\r\n";
            response << "Content-Type: application/json\r\n";
            response << "Access-Control-Allow-Origin: *\r\n";
            response << "Content-Length: " << response_json.size() << "\r\n";
            response << "\r\n";
            response << response_json;

            // 发送响应
            send(client_socket, response.str().c_str(), response.str().size(), 0);
        }
        close(client_socket);
    }

    static int start(int port) { // C++ 不支持重载方法
        port = port <= 0 ? 8084 : port;

        int server_socket = socket(AF_INET, SOCK_STREAM, 0);
        sockaddr_in server_addr;
        server_addr.sin_family = AF_INET;
        server_addr.sin_addr.s_addr = INADDR_ANY;
        server_addr.sin_port = htons(port);

        bind(server_socket, (sockaddr*)&server_addr, sizeof(server_addr));
        listen(server_socket, SOMAXCONN);

        std::cout << "Server is running on port " << port << "..." << std::endl;

        while (true) {
            int client_socket = accept(server_socket, nullptr, nullptr);
            handle_request(client_socket);
        }

        close(server_socket);
        return 0;
    }


}
