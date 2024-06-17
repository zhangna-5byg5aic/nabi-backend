package com.yupi.springbootinit.service.impl;

import com.yupi.springbootinit.service.ChartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ChartServiceImplTest {
    @Autowired
    private ChartService chartService;
    @Test
    void getAllChartData() {
        List<Map<String, Object>> chartData = chartService.getAllChartData("chartdata_1801467152268894210");
        System.out.println(chartData.size());
//        for (Map<String, Object> map : chartData) {
//            for (Map.Entry<String, Object> entry : map.entrySet()) {
//                String key = entry.getKey();
//                Object value = entry.getValue();
//                System.out.println("Key: " + key + ", Value: " + value);
//                // 在这里可以对每个键值对进行操作
//            }
//        }
        // 创建一个StringBuilder来存储总的结果
        StringBuilder finalResult = new StringBuilder();

        // 将每个map的value连接成一个字符串
        for (Map<String, Object> map : chartData) {
            String result = String.join(",", map.values().stream()
                    .map(Object::toString)
                    .toArray(String[]::new));
            if (finalResult.length() > 0) {
                finalResult.append("\n");
            }
            finalResult.append(result);
        }

        // 打印总的结果字符串
        System.out.println(finalResult.toString());
    }
}