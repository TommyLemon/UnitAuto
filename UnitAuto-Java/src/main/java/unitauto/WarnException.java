/*Copyright ©2019 TommyLemon(https://github.com/TommyLemon/UnitAuto)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package unitauto;

/**带警告的异常类，可以通过抛异常同时把 return 值和 warning 内容一起返回，例如
 * Long id;
 * try {
 *   id = getId();
 * } catch (WarnException e) {
 *   id = e.getReturn();
 *   e.printStackTrace();
 * }
 * @author Lemon
 */
public class WarnException extends RuntimeException { // cannot have generic when extend Throwable   WarnException<R> extends RuntimeException {

    private Object returnValue; // R returnValue;
    public WarnException(Object returnValue, Throwable e) {
        super(e);
        this.returnValue = returnValue;
    }

    public WarnException(Object returnValue, String msg) {
        super(msg);
        this.returnValue = returnValue;
    }

    public WarnException(Object returnValue, String msg, Throwable e) {
        super(msg, e);
        this.returnValue = returnValue;
    }

    public Object getReturn() {
        return returnValue;
    }

}
