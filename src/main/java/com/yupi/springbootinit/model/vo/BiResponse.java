package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * BI返回结果
 */
@Data
public class BiResponse {
    /**
     * id
     */
    private Long id;
    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的结论分析
     */
    private String genResult;
}
