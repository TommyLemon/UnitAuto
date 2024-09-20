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
    NLOHMANN_DEFINE_TYPE_INTRUSIVE(User, id, sex, name, date)

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

    NLOHMANN_DEFINE_TYPE_INTRUSIVE(Moment, id, userId, content)
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

    NLOHMANN_DEFINE_TYPE_INTRUSIVE(Person, name, age)
};


 int test() {
    // 示例值
    std::any ret = Person{"Alice", 30};
    auto type = typeid(ret).name();

    unitauto::add_struct<Person>("Person");

    // 执行函数
    try {
        //显式转换字符串字面量为 std::string
        unitauto::invoke("print", {std::string("Hello, UnitAuto C++ !")});

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
        std::cerr << "Exception: " << e.what() << std::endl;
    }

    std::string str = R"({"id":1, "sex":1, "name":"John Doe", "date":1705293785163})";
    json j = json::parse(str);

    User u = j.get<User>();
    void* obj;
    try {
        obj = unitauto::json_2_obj(j, "User");
    } catch (const std::exception& e) {
        std::cerr << "json 2 obj failed:" << e.what() << std::endl;
    }

    User* userPtr = static_cast<User*>(obj);
    if (userPtr) {
        std::cout << "\nUser: {" << std::endl;
        std::cout << "  id: " << userPtr->id << std::endl;
        std::cout << "  sex: " << userPtr->sex << std::endl;
        std::cout << "  name: " << userPtr->name << std::endl;
        std::cout << "  date: " << userPtr->date << std::endl;
        std::cout << "}" << userPtr->id << std::endl;

        // malloc: *** error for object 0x16cf96110: pointer being freed was not allocated unitauto::del_obj<User>(obj);
    } else {
        std::cerr << "Type match error, have u added type with add_type?" << std::endl;
    }


    // json result;
    // result["code"] = 200;
    // result["msg"] = "success";
    // result["type"] = type;
    // result["return"] = unitauto::any_to_json(ret);
    // result["methodArgs"] = json::array({{{"type", "long"}, {"value", 1}}, {{"type", "long"}, {"value", 2}}});

    // std::cout << "\n\ntest return " << result.dump(4) << std::endl;

    return 0;
}

static int compare(User u, User u2) {
     if (u.id < u2.id) {
         return -1;
     }

     if (u.id > u2.id) {
         return 1;
     }

     return 0;
 }


int main() {
    unitauto::DEFAULT_MODULE_PATH = "unitauto"; // TODO 改为你项目的默认包名

    // 注册函数
    UNITAUTO_ADD_FUNC(print, add, divide, newMoment, unitauto::test::divide, unitauto::test::contains, unitauto::test::index, unitauto::test::is_contain, unitauto::test::index_of);

    // 注册类型(class/struct)及方法(成员函数)
    UNITAUTO_ADD_METHOD(Moment, &Moment::getId, &Moment::setId, &Moment::getUserId, &Moment::setUserId, &Moment::getContent, &Moment::setContent);
    UNITAUTO_ADD_METHOD(User, &User::getId, &User::setId, &User::getName, &User::setName, &User::getDate, &User::setDate);
    UNITAUTO_ADD_METHOD(unitauto::test::TestUtil, &unitauto::test::TestUtil::divide);

    // 自定义注册函数路径
    unitauto::add_func("main.newMoment", std::function<Moment(long)>(newMoment));
    unitauto::add_func("main.newUser", std::function<User(long, std::string)>(newUser));

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
    unitauto::add_func("main.compare", std::function<int(User,User)>(compare));

    unitauto::start(8084);

    return 0;
}

