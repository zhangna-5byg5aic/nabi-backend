package com.yupi.springbootinit.mapper;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
* @author 张娜
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2024-05-22 18:41:21
* @Entity com.yupi.springbootinit.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {
    @Update("${sql}")
    void createChartTable(@Param("sql") String sql);

    @Insert("${sql}")
    void insertChartData(@Param("sql") String sql);

    @Select("SELECT * FROM ${tableName}")
    List<Map<String, Object>> selectAllChartData(@Param("tableName") String tableName);
}




