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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private ImageView ivAvatar;
    private TextView tvName;
    private TextView tvTitle;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvName = view.findViewById(R.id.tvName);
        tvTitle = view.findViewById(R.id.tvTitle);

        ivAvatar.setOnLongClickListener(this);
        tvName.setOnClickListener(this);
        tvTitle.setOnClickListener(this);

        return view;
    }

    // 用 UnitAuto 后台管理界面（http://apijson.org:8000/unit/）测试以下方法

    public boolean test() {
        return true;
    }

    public String hello(String name) {
        return "Hello, " + (name == null ? "Fragment" : name) + "!";
    }


    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(
                getActivity(),
                getString(R.string.app_name) + " - " + getString(R.string.unit_testing_platform_for_coding_free)
                , Toast.LENGTH_LONG
        ).show();
        return true;
    }

    private boolean isML = false;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvName:
                Toast.makeText(getActivity(), getString(R.string.unit_testing_platform_for_coding_free), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tvTitle:
                isML = ! isML;
                tvTitle.setText(isML ? R.string.auto_assert_with_machine_learning : R.string.unit_testing_platform_for_coding_free);
                break;
        }
    }


}
