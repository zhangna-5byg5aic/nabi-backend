package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.mapper.ChartMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 张娜
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-05-22 18:41:21
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

    @Override
    public String saveTable(long chartId,List<Map<Integer, String>> excelData) {
        String tableName = "chartdata_" + chartId;

        //获取表头
        LinkedHashMap<Integer,String> headerMap = (LinkedHashMap)excelData.get(0);
        List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        int columnCount = headerList.size();
        //创建新表
        createChartTable(tableName,columnCount);
        insertChartData(tableName,columnCount,headerList);
        //按行读取数据
        for(int i =1;i<excelData.size();i++)
        {
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap)excelData.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            insertChartData(tableName,columnCount,dataList);
//            System.out.println(StringUtils.join( dataList,","));
        }
        return tableName;
    }

    @Override
    public List<Map<String, Object>> getAllChartData(String tableName) {
        return baseMapper.selectAllChartData(tableName);
    }

    private void createChartTable(String tableName, int columnCount) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (");

        for (int i = 1; i <= columnCount; i++) {
            sql.append("column").append(i).append(" VARCHAR(255)");
            if (i != columnCount) {
                sql.append(", ");
            }
        }

        sql.append(")");
        baseMapper.createChartTable(sql.toString());
    }

    private void insertChartData(String tableName,int columnCount,List<String> values) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" VALUES (");

        for (int i = 0; i < columnCount; i++) {
            sql.append("'").append(values.get(i)).append("'");
            if (i != columnCount - 1) {
                sql.append(", ");
            }
        }

        sql.append(")");
        baseMapper.insertChartData(sql.toString());
    }
}




