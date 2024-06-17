package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AIManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class BiMessageConsumer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ChartService chartService;

    @Resource
    private AIManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
    {
        log.info("Received message:{}", message);
        if(StringUtils.isBlank(message))
        {
            //失败，消息拒绝
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart == null)
        {
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        Boolean updateResult = chartService.updateById(updateChart);
        if(!updateResult)
        {
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"更新图表运行状态失败");
        }
       //查找原始数据，并拼合
        List<Map<String, Object>> chartData = chartService.getAllChartData("chartdata_"+chart.getId());
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
        //调用AI生成结果
//        String chatResult = aiManager.doChat(CommonConstant.BI_MODEL_ID,buildUserInput(chart));
        String chatResult = aiManager.doQingChat(chart.getGoal(),finalResult.toString());

        List<String> res = AIManager.extractTexts(chatResult);
        String genChart = res.get(0);
        String genResult = res.get(1);

        Chart aiChart = new Chart();
        aiChart.setId(chart.getId());
        aiChart.setGenChart(genChart);
        aiChart.setGenResult(genResult);
        //TODO: 状态定义为枚举值
        aiChart.setStatus("succeed");
        boolean aiChartUpdated = chartService.updateById(aiChart);
        if(!aiChartUpdated)
        {
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"更新图标成功状态失败");
        }

        //成功消息确认
        channel.basicAck(deliveryTag,false);
    }

    /**
     * 构建用户输入
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart)
    {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();
        //拼接用户输入
        StringBuilder userInput = new StringBuilder();
        String userGoal = goal;
        if(StringUtils.isNotBlank(chartType))
        {
            userGoal = goal+"请使用"+chartType;
        }
        userInput.append("分析需求：").append(userGoal).append("\n");
        userInput.append("原始数据：").append(csvData).append("\n");
        return userInput.toString();
    }
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage("execMessage");
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}
