package com.jn;

import com.alibaba.fastjson.JSON;
import com.weaver.common.base.entity.result.WeaResult;
import com.weaver.ebuilder.datasource.api.entity.SqlParamEntity;
import com.weaver.ebuilder.datasource.api.enums.SourceType;
import com.weaver.esb.api.rpc.EsbServerlessRpcRemoteInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("esbDemoAction")
public class DemoAction implements EsbServerlessRpcRemoteInterface {

    @Autowired
    private static DataSetUtil dataSetUtil;

    @Override
    public WeaResult<Map<String, Object>> execute(Map<String, Object> params) {
        log.info("params: {}", JSON.toJSONString(params));
        params.put("extral", "123");
        //先声明SQL语句
        //库存数据
        String getRepertorySql = "SELECT Item_ItemCode,Temp_PAB FROM v_u9_wlkczsl";
        //备料数据
        String getDemandSql = "SELECT mo,DocNo,ItemCode,wlsl,StartDate FROM v_u9_scblb ORDER BY StartDate ASC";
        MultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
        List<SqlParamEntity> sqlParams = new ArrayList<>();
        dataSetUtil.executeExternalSql(SourceType.EXTERNAL,getRepertorySql, getDemandSql,sqlParams);
        return WeaResult.success(params);
    }
}
