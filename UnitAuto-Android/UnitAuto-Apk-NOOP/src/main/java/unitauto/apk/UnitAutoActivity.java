/*Copyright ©2020 TommyLemon(https://github.com/TommyLemon/UnitAuto)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/


package unitauto.apk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;


/**自动单元测试管理界面，需要用 UnitAuto 发请求到这个设备
 * https://github.com/TommyLemon/UnitAuto
 * @author Lemon
 */
public class UnitAutoActivity extends Activity {
    public static final String TAG = "UnitAutoActivity";

    /**
     * @param context
     * @return
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, UnitAutoActivity.class);
    }

}