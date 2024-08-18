#include <iostream>
#include <string>
#include <sstream>
#include <vector>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <ctime>

namespace unitauto::test {

     static double divide(int a, int b) {
          return a/b;
     }

     class TestUtil {
          static double divide(double a, double b) {
               return a/b;
          }
     };

}