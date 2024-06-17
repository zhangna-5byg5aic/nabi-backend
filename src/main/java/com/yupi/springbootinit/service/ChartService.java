package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author 张娜
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2024-05-22 18:41:21
*/
public interface ChartService extends IService<Chart> {

    String saveTable(long chartId,List<Map<Integer, String>> excelData);
    List<Map<String, Object>> getAllChartData(String tableName);
}
