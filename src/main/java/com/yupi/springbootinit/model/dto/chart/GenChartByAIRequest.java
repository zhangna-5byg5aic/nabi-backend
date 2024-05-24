package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能分析
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class GenChartByAIRequest implements Serializable {

    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表名称
     */
    private String chartName;
    /**
     * 图表类型
     */
    private String chartType;
    /**
     * 业务
     */
    private String biz;

    private static final long serialVersionUID = 1L;
}