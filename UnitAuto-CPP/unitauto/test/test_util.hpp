#include <iostream>
#include <string>
#include <sstream>
#include <vector>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <ctime>

namespace unitauto::test {

     static double divide(double a, double b) {
          return a/b;
     }

     static bool contains(long arr[], long l) {
          if (arr == nullptr) {
               return false;
          }

          for (int i = 0; i < sizeof arr; ++i) {
               if (arr[i] == l) {
                    return true;
               }
          }

          return false;
     }

     static bool is_contain(std::vector<int> arr, int n) {
          if (arr.empty()) {
               return false;
          }

          for (int i = 0; i < arr.size(); ++i) {
               if (arr[i] == n) {
                    return true;
               }
          }

          return false;
     }

     static int index(std::string arr[], std::string s) {
          if (arr == nullptr || arr->length() <= 0) {
               return -1;
          }

          for (int i = 0; i < arr->length(); ++i) {
               if (arr[i] == s) {
                    return i;
               }
          }

          return -1;
     }

     static int index_of(std::vector<std::string> arr, std::string s) {
          if (arr.size() <= 0) {
               return -1;
          }

          for (int i = 0; i < arr.size(); ++i) {
               if (arr[i] == s) {
                    return i;
               }
          }

          return -1;
     }


     class TestUtil {
     public:
          long id;
          double divide(long a, long b) {
               return 1.0f*a/b;
          }

          NLOHMANN_DEFINE_TYPE_INTRUSIVE(TestUtil, id)
     };

}