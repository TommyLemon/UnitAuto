#include "unitauto/method_util.hpp"
// #include "unitauto/server.hpp"
#include <iostream>

#include "unitauto/test/test_util.hpp"

using json = unitauto::json;

// 模拟你的业务项目中需要测试的函数、自定义类型等 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<

// 普通函数 Demo
int add(int a, int b) {
    return a + b;
}

double divide(double a, double b) {
    return a / b;
}


// 类和方法(成员函数) Demo

struct User {
    int id;
    int sex;
    std::string name;
    std::time_t date;

    long getId() {
        return id;
    }

    void setId(long id_) {
        id = id_;
    }

    std::string getName() {
        return name;
    }

    void setName(std::string name_) {
        name = name_;
    }

    std::time_t getDate() {
        return date;
    }

    void setDate(std::time_t date_) {
        date = date_;
    }

    // TODO FIXME 对 char 等部分类型无效
    NLOHMANN_DEFINE_TYPE_INTRUSIVE_WITH_DEFAULT(User, id, sex, name, date)

    bool is_male()
    {
        return sex == 0;
    }
};

// void to_json(json& j, const User& p) {
//     j = json{{"id", p.id}, {"sex", p.sex}, {"name", p.name}, {"date", p.date}};
// }
//
// void from_json(const json& j, User& p) {
//     j.at("id").get_to(p.id);
//     j.at("sex").get_to(p.sex);
//     j.at("name").get_to(p.name);
//     j.at("date").get_to(p.date);
// }

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

    NLOHMANN_DEFINE_TYPE_INTRUSIVE_WITH_DEFAULT(Moment, id, userId, content)
};

Moment newMoment(long id) {
    return Moment(id);
}

User newUser(long id, std::string name) {
    User u = User();
    u.id = id;
    u.setName(name);
    return u;
}


// 自定义类型示例
struct Person {
    std::string name;
    int age;

    static bool testStatic() {
        return true;
    }

    NLOHMANN_DEFINE_TYPE_INTRUSIVE_WITH_DEFAULT(Person, name, age)
};


static int compare(User u, User u2) {
    if (u.id < u2.id) {
        return -1;
    }

    if (u.id > u2.id) {
        return 1;
    }

    return 0;
}

// 模拟你的业务项目中需要测试的函数、自定义类型等 >>>>>>>>>>>>>>>>>>>>>>>>>>>>

// 测试 UnitAuto 功能
int test() {
    try {
        //显式转换字符串字面量为 std::string
        auto ret = unitauto::invoke("add", {1, 2});
        std::cout << "invoke(\"add\", {1, 2}) = " << std::any_cast<int>(ret) << std::endl;

        auto divideRet = unitauto::invoke("divide", {5.6, static_cast<double>(3)});
        std::cout << "invoke(\"divide\", {5.6, 3}) = " << std::any_cast<double>(divideRet) << std::endl;

        auto moment2Ret = unitauto::invoke("main.newMoment", {12L});
        Moment moment2 = std::any_cast<Moment>(moment2Ret);
        std::cout << "invoke(\"main.newMoment\", {12}).id = " << moment2.id << std::endl;
        json j;
        j["type"] = "main.Moment";
        unitauto::invoke_method(j, "main.Moment.setId", {static_cast<long>(123)});

        auto getIdRet = unitauto::invoke_method(j, "main.Moment.getId", {});
        std::cout << "invoke(\"main.Moment.getId\", {}) = " << std::any_cast<long>(getIdRet) << std::endl;

        User user = User();
        auto userRet = unitauto::invoke("main.User.setId",{static_cast<long>(225)});
        std::cout << "invoke(\"main.Moment.getId\", {}) = " << std::any_cast<long>(getIdRet) << std::endl;

        std::vector<std::any> v;
        v.emplace_back(3.08);
        v.emplace_back(0.5);

        auto dr = unitauto::invoke("unitauto.test.divide", v);
        std::cout << "invoke(\"unitauto.test.divide\", {3.08, 0.5}) = " << std::any_cast<double>(dr) << std::endl;
    } catch (const std::exception e) {
        unitauto::printlnErr("Exception: ", e.what());
    }

    std::string str = R"({"id":1, "sex":1, "name":"John Doe", "date":1705293785163})";
    json j = json::parse(str);

    User u = j.get<User>();
    User* userPtr;
    try {
        // obj = unitauto::json_2_obj(j, "User");
        userPtr = unitauto::json_2_obj<User>(j, "User");
    } catch (const std::exception& e) {
        unitauto::printlnErr("json_2_obj<User> failed:", e.what());
        try {
            // obj = unitauto::json_2_obj(j, "User");
            User user = unitauto::json_2_val<User>(j, "User");
            userPtr = &user;
        } catch (const std::exception& e2) {
            unitauto::printlnErr("json_2_val<User> failed:", e2.what());
        }
    }

    if (userPtr) {
        unitauto::println("\nUser: {");
        unitauto::println("  id: ", userPtr->id);
        unitauto::println("  sex: ", userPtr->sex);
        unitauto::println("  name: ", userPtr->name);
        unitauto::println("  date: ", userPtr->date);
        unitauto::println("}");

        // malloc: *** error for object 0x16cf96110: pointer being freed was not allocated unitauto::del_obj<User>(obj);
    } else {
        unitauto::printlnErr("Type match error, have u added type with add_type?");
    }

    return 0;
}


// 点左侧运行按钮 运行/调试
int main() {
    unitauto::DEFAULT_MODULE_PATH = "unitauto"; // TODO 改为你项目的默认包名

    // 注册函数
    UNITAUTO_ADD_FUNC(add, divide, newMoment, unitauto::test::divide, unitauto::test::contains, unitauto::test::index, unitauto::test::is_contain, unitauto::test::index_of);

    // 注册类型(class/struct)及方法(成员函数)
    UNITAUTO_ADD_METHOD(Moment, &Moment::getId, &Moment::setId, &Moment::getUserId, &Moment::setUserId, &Moment::getContent, &Moment::setContent);
    UNITAUTO_ADD_METHOD(User, &User::getId, &User::setId, &User::getName, &User::setName, &User::getDate, &User::setDate);
    UNITAUTO_ADD_METHOD(unitauto::test::TestUtil, &unitauto::test::TestUtil::divide);
    // UNITAUTO_ADD_METHOD(Person, Person::testStatic);

    // 自定义注册类型
    unitauto::add_struct<Person>("Person");

    // 自定义注册函数路径
    unitauto::add_func("main.newMoment", std::function(newMoment));
    unitauto::add_func("main.newUser", std::function(newUser));
    unitauto::add_func("main.compare", std::function(compare));
    unitauto::add_func("Person.testStatic", std::function(Person::testStatic));

    // 自定义注册方法(成员函数)路径
    User user = User();
    user.setId(1);
    user.name = "Test User";
    user.date = time(nullptr);

    unitauto::add_func("unitauto.test.User.is_male", user, &User::is_male);
    unitauto::add_func("main.User.setId", user, &User::setId);
    unitauto::add_func("main.User.getId", user, &User::getId);
    unitauto::add_func("main.User.setName", &user, &User::setName);
    unitauto::add_func("main.User.getName", &user, &User::getName);
    unitauto::add_func("main.User.setDate", (User *) nullptr, &User::setDate);
    unitauto::add_func("main.User.getDate", User(), &User::getDate);

    // unitauto::add_func("unitauto.test.TestUtil.divide", (unitauto::test::TestUtil *) nullptr, &unitauto::test::TestUtil::divide);

    test();

    unitauto::println("\n\nUnitAuto-最先进、最省事、ROI 最高的单元测试，机器学习 零代码、全方位、自动化 测试 方法/函数");
    unitauto::println("https://github.com/TommyLemon/UnitAuto");
    unitauto::println("https://gitee.com/TommyLemon/UnitAuto");
    unitauto::println("打开以下链接开始 零代码单元测试 吧 ^_^");
    unitauto::println("http://apijson.cn/unit/?language=C++&host=http://localhost:8084");

    unitauto::start(8084);

    return 0;
}

