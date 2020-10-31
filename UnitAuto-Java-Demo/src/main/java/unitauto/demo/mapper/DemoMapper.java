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

package unitauto.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


/**
 * @author Lemon
 */
@Mapper
public interface DemoMapper {

	@Select("select table_name, table_type from information_schema.tables limit 10")
	List<Object> listTables();
	
	@Select("select column_name as `name`, column_type as `type` from information_schema.columns where table_name = ${table} limit 10")
	List<Object> listColumns(@Param("table") String table);

}
