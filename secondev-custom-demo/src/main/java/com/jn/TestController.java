package com.jn;

import cn.hutool.core.codec.Base64;
import com.weaver.common.authority.annotation.WeaPermission;
import com.weaver.common.base.entity.result.WeaResult;
import com.weaver.ebuilder.datasource.api.entity.ExecuteSqlEntity;
import com.weaver.ebuilder.datasource.api.enums.SourceType;
import com.weaver.ebuilder.datasource.api.service.DataSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/sapi/secondev/test")
@WeaPermission(publicPermission = true)
public class TestController {

    @Autowired
    private DataSetService dataSetService;

    @GetMapping("/hello")
    public WeaResult<String> hello(@RequestParam("msg") String msg) {
        return WeaResult.success("Hello, " + msg);
    }

    @GetMapping("/getUser/byId")
    public WeaResult<Map<String, Object>> getUserById(@RequestParam("userId") String userId) {
        String groupId = "eteams_groupid";
        ExecuteSqlEntity executeSqlEntity = new ExecuteSqlEntity();
        executeSqlEntity.setSql(Base64.encode("select * from employee where id = " + userId));
        executeSqlEntity.setGroupId(groupId);
        executeSqlEntity.setSourceType(SourceType.ETEAMS);
        Map<String, Object> map = dataSetService.executeSql(executeSqlEntity);
        return WeaResult.success(map);
    }


}
