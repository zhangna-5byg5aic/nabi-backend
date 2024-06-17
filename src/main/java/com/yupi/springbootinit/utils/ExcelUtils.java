package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.*;
@Slf4j
public class ExcelUtils {
    public static String excelToCsv(MultipartFile multipartFile)
    {
        /*File file = null;
        try {
            file = ResourceUtils.getFile("classpath:test_excel.xlsx");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }*/
        //读取数据
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误",e);
//            throw new RuntimeException(e);
        }
        if(CollUtil.isEmpty(list))
        {
            return "";
        }
        //转换csv
        StringBuilder stringBuilder = new StringBuilder();
        //读取表头
        LinkedHashMap<Integer,String> headerMap = (LinkedHashMap)list.get(0);
        List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        stringBuilder.append(headerList).append("\n");
//        System.out.println(StringUtils.join( headerList,","));
        //按行读取数据
        for(int i =1;i<list.size();i++)
        {
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap)list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(dataList).append('\n');
//            System.out.println(StringUtils.join( dataList,","));
        }
//        System.out.println(list);
        return stringBuilder.toString();
    }
    public static List<Map<Integer, String>> getExcel(MultipartFile multipartFile)
    {
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误",e);
//            throw new RuntimeException(e);
        }
        return list;
    }
    public static void main(String[] args) throws Exception {
        ExcelUtils.excelToCsv(null);
    }
}
