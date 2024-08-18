#include "unitauto/method_util.hpp"
// #include "unitauto/server.hpp"
#include <iostream>

#include "unitauto/test/test_util.hpp"

using json = unitauto::json;

// 普通函数 Demo
int add(int a, int b) {
    return a + b;
}

double divide(double a, double b) {
    return a / b;
}

void print(const std::string &message) {
    std::cout << message << std::endl;
}

// 类和方法(成员函数) Demo

struct User {
    int id;
    int sex;
    std::string name;
    std::time_t date;

    // TODO FIXME 对 char 等部分类型无效
    // NLOHMANN_DEFINE_TYPE_INTRUSIVE(User, id, sex, name, date)

    int is_male()
    {
        return sex == 0;
    }
};

void to_json(json& j, const User& p) {
    j = json{{"id", p.id}, {"sex", p.sex}, {"name", p.name}, {"date", p.date}};
}

void from_json(const json& j, User& p) {
    j.at("id").get_to(p.id);
    j.at("sex").get_to(p.sex);
    j.at("name").get_to(p.name);
    j.at("date").get_to(p.date);
}

class Moment {
public:
    long id;
    long userId;
    std::string content;

    Moment() {
    }

    Moment(long id) {
      setId(id);
    }

    void setId(long id) {
        std::cout << "setId: " << id << std::endl;
        this->id = id;
    }

    long getId() {
        return this->id;
    }

    void setUserId(long userId) {
        this->userId = userId;
    }

    long getUserId() {
        return this->userId;
    }

    void setContent(std::string content) {
        std::cout << "setContent: " << content << std::endl;
        this->content = content;
    }

    std::string getContent() {
        return this->content;
    }

    NLOHMANN_DEFINE_TYPE_INTRUSIVE(Moment, id, userId, content)
};

Moment newMoment(long id) {
    return Moment(id);
}



// 自定义类型示例
struct Person {
    std::string name;
    int age;
};

// 自定义类型的 to_json 函数
void to_json(json& j, const Person& p) {
    j = json{{"name", p.name}, {"age", p.age}};
}

// // 特化自定义类型的转换函数
// template<>
// void register_converter<Person>() {
//     unitauto::converters[typeid(Person).name()] = [](std::any value) -> json {
//         return std::any_cast<Person>(value);
//     };
// }
//



 int test() {
    // 示例值
    std::any ret = Person{"Alice", 30};
    auto type = typeid(ret).name();

    unitauto::register_converter<Person>();

    json result;
    result["code"] = 200;
    result["msg"] = "success";
    result["type"] = type;
    result["return"] = unitauto::any_to_json(ret);
    result["methodArgs"] = json::array({{{"type", "long"}, {"value", 1}}, {{"type", "long"}, {"value", 2}}});

    std::cout << "\n\ntest return " << result.dump(4) << std::endl;

    return 0;
}


int main() {
    // 注册函数
    unitauto::add_func("print", std::function<void(const std::string &)>(print));
    unitauto::add_func("add", std::function<int(int, int)>(add));
    unitauto::add_func("divide", std::function<double(double, double)>(divide));
    unitauto::add_func("newMoment", std::function<Moment(long)>(newMoment));

    // 注册方法(成员函数)
    Moment moment;
    unitauto::add_func("setId", &moment, &Moment::setId);
    unitauto::add_func("getId", &moment, &Moment::getId);

    // 执行函数
    try {
        //显式转换字符串字面量为 std::string
        unitauto::invoke("print", {std::string("Hello, UnitAuto C++ !")});

        auto ret = unitauto::invoke("add", {1, 2});
        std::cout << "invoke(\"add\", {1, 2}) = " << std::any_cast<int>(ret) << std::endl;

        auto divideRet = unitauto::invoke("divide", {5.6, static_cast<double>(3)});
        std::cout << "invoke(\"divide\", {5.6, 3}) = " << std::any_cast<double>(divideRet) << std::endl;

        auto moment2Ret = unitauto::invoke("newMoment", {12L});
        Moment moment2 = std::any_cast<Moment>(moment2Ret);
        std::cout << "invoke(\"newMoment\", {12}).id = " << moment2.id << std::endl;

        unitauto::invoke("setId", {static_cast<long>(123)});

        auto getIdRet = unitauto::invoke("getId", {});
        std::cout << "invoke(\"getId\", {}) = " << std::any_cast<long>(getIdRet) << std::endl;
    } catch (const std::exception e) {
        std::cerr << "Exception: " << e.what() << std::endl;
    }


    // 必须先注册类型
    unitauto::add_type<User>("User");
    // add_func("add", std::function<int(int,int)>(add));
    // add_func("add", std::bind(add));

    std::string str = R"({"id":1, "sex":1, "name":"John Doe", "date":1705293785163})";
    json j = json::parse(str);

    User u = j.get<User>();
    void* obj;
    try {
        obj = unitauto::json_2_obj(str, "User");
    } catch (const std::exception& e) {
        std::cerr << "json 2 obj failed:" << e.what() << std::endl;
        return 1;
    }

    User* userPtr = static_cast<User*>(obj);
    if (userPtr) {
        std::cout << "\nUser: {" << std::endl;
        std::cout << "  id: " << userPtr->id << std::endl;
        std::cout << "  sex: " << userPtr->sex << std::endl;
        std::cout << "  name: " << userPtr->name << std::endl;
        std::cout << "  date: " << userPtr->date << std::endl;
        std::cout << "}" << userPtr->id << std::endl;

        unitauto::del_obj<User>(obj);
    } else {
        std::cerr << "Type match error, have u added type with add_type?" << std::endl;
    }

    // add_type<Func<int(int, int)>>("add", new Func<int(int, int)>(add) {});


    // auto FUNC_MAP = Func<void*(*)>.FUNC_MAP;

    // auto funcMap = getFuncMap<>()
    //
    // FUNC_MAP["add"] = &add;
    // FUNC_MAP["minus"] = &minus;

    // auto add2 = FUNC_MAP["add"];
    // std::function<void*(*)>


    // for (auto pair : FUNC_MAP) {
    //     auto key = pair.first;
    //     auto val = pair.second;
    //
    //     std::cout << "\nkey = " << key << "; val = " << val;
    //
    // }

    // User user;
    //
    // auto f3 = std::mem_fn(&User::is_male);
    // auto r3 = f3(&user);
    // std::cout << "\nf3(&user) = " << r3;

    // auto f4 = std::bind(ptr_to_print_sum, &user, 95, 1);
    // int r4 = f4();
    // std::cout << "\nf4(5) = " << r4;

    // auto ps = &User::print_sum;
    // int r = ps(&user, 1, 2);
    // std::cout << "\nps(User(), 1, 2) = " << r;


    // std::function func = minus;
    // std::vector<std::any> arr = {};
    // arr.push_back(5);
    // arr.push_back(2);

    // std::tuple args2 = {2, 6};

    // auto c = func((int) arr.at(0), arr.at(1));


    // auto c = func(arr...);
    // std::cout << "\nfunc(5, 2) = " << c;

    test();

    // unitauto::add_type<unitauto::test::TestUtil>("unitauto.test.TestUtil");
    unitauto::add_func("unitauto.test.divide", std::function<double(double,double)>(unitauto::test::divide));
    std::vector<std::any> v;
    v.push_back(3.08);
    v.push_back(0.5);

    auto dr = unitauto::invoke("unitauto.test.divide", v);
    std::cout << "invoke(\"unitauto.test.divide\", {3.08, 0.5}) = " << std::any_cast<double>(dr) << std::endl;

    User user;
    unitauto::add_func("unitauto.test.User.is_male", &user, &User::is_male);

    unitauto::start(8084);

    return 0;
}

