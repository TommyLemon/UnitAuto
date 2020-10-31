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


package unitauto.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import unitauto.apk.UnitAutoActivity;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment fragment = new MainFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.flMain, fragment)
                .show(fragment)
                .commit();
    }

    // 用 UnitAuto 后台管理界面（http://apijson.org:8000/unit/）测试以下方法

    public boolean test() {
        return true;
    }

    public String hello(String name) {
        return "Hello, " + (name == null ? "Activity" : name) + "!";
    }

    public void onClickHello(View v) {
        Toast.makeText(this, ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
    }

    public void onClickUnit(View v) {
        startActivity(UnitAutoActivity.createIntent(this));
    }

}
