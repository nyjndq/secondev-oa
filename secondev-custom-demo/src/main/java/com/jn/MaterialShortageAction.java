package com.jn;

import cn.hutool.core.codec.Base64;

import cn.hutool.json.JSONUtil;
import com.weaver.common.base.entity.result.WeaResult;
import com.weaver.datasource.utils.rest.CommonRestService;
import com.weaver.ebuilder.datasource.api.entity.ExecuteSqlEntity;
import com.weaver.ebuilder.datasource.api.entity.SqlParamEntity;
import com.weaver.ebuilder.datasource.api.enums.SourceType;
import com.weaver.ebuilder.datasource.api.enums.SqlParamType;
import com.weaver.esb.api.rpc.EsbServerlessRpcRemoteInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("materialShortageAction")
public class MaterialShortageAction implements EsbServerlessRpcRemoteInterface {
    private static final  String groupId = "1007148763742642176";

    @Autowired
    private CommonRestService commonRestService;

    public static String base64(String sql) {
        return Base64.encode(sql);
    }

    @Override
    public WeaResult<Map<String, Object>> execute(Map<String, Object> params) {
        //先声明SQL语句
        //库存数据
        String getRepertorySql = "SELECT Item_ItemCode,Temp_PAB FROM v_u9_wlkczsl";
        //备料数据
        String getDemandSql = "SELECT mo,DocNo,ItemCode,wlsl,StartDate FROM v_u9_scblb ORDER BY StartDate ASC";
        Map<String, Object> DemanMap = new HashMap<>();
        Map<String, Object>  RepertoryMap = new HashMap<>();
        List<SqlParamEntity> sqlParams = new ArrayList<>();
        DemanMap = executeExternalSql(SourceType.EXTERNAL,groupId, getDemandSql,sqlParams);//备料表
        RepertoryMap = executeExternalSql(SourceType.EXTERNAL,groupId, getRepertorySql,sqlParams);//库存表
        log.info("DemanMap:",DemanMap.toString());log.info("RepertoryMap:",RepertoryMap.toString());

        return WeaResult.success(params);
    }

    /**
     * 执行外部数据库sql: /sapi/datasearch/external/data/executeSql
     * sourceType  : EXTERNAL: 外部数据源
     * groupId  :   842668710322556928L 通过group接口获取数据加工内配置的连接id
     * sql :      select * from ebda_app limit  10
     *
     * @param
     * @return
     */
    public Map<String, Object>  executeExternalSql(SourceType sourceType, String groupId, String sql, List<SqlParamEntity> sqlParams) {
        //执行sql  参数sourceType   groupId  sql
        ExecuteSqlEntity executeSqlEntity = new ExecuteSqlEntity();
        executeSqlEntity.setSql(base64(sql));
        executeSqlEntity.setGroupId(groupId);
        executeSqlEntity.setSourceType(sourceType);

        //若通过占位符方式查询外部数据库,需增加此参数
        //占位符list 的顺序  要与 sql 的?占位符顺序一致
        if (sqlParams != null && !sqlParams.isEmpty()) {
            executeSqlEntity.setParams(sqlParams);
        }

        //拼接参数
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("params", JSONUtil.toJsonStr(executeSqlEntity));

        try {
            return commonRestService.postForForm("/sapi/datasearch/external/data/executeSql", Map.class, valueMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 构建占位符参数
     * 注意:list的参数顺序 必须要与 sql内的占位符参数顺序一致
     * 此案例构建的json字符串 是提供给sql:select * from table where tenant_key = ? and delete_dype = ? and id in (?, ?, ?) 使用
     * @return
     */
    public List<SqlParamEntity> getSqlParams() {
        List<SqlParamEntity> sqlParams = new ArrayList<>();

        SqlParamEntity sqlParam1 = new SqlParamEntity();
        sqlParam1.setParamType(SqlParamType.VARCHAR);
        sqlParam1.setValue("thsv5s4n2c");
        sqlParams.add(sqlParam1);

        SqlParamEntity sqlParam2 = new SqlParamEntity();
        sqlParam2.setParamType(SqlParamType.INTEGER);
        sqlParam2.setValue("0");
        sqlParams.add(sqlParam2);

        List<Long> ids = new ArrayList<>();
        ids.add(1054407582048280585L);
        ids.add(1054407345976074241L);
        ids.add(1054406667262189570L);
        for (Long id : ids) {
            SqlParamEntity sqlParam3 = new SqlParamEntity();
            sqlParam3.setParamType(SqlParamType.LONG);
            sqlParam3.setValue(String.valueOf(id));
            sqlParams.add(sqlParam3);
        }

        return sqlParams;
    }
}
