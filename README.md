# 项目名称


## 简介

基于 Spring Boot、RabbitMQ、AIGC 和 React 的智能数据分析平台

## 目录

- [快速开始](#快速开始)
- [系统架构](#系统架构)
- [使用说明](#使用说明)
- [技术细节](#技术细节)

## 快速开始
### 环境要求
- Java 8
- Redis 服务器
- MySQL 8.1.0
- RabbitMq 3.13.2
## 系统架构
- 整体架构图
![img.png](https://github.com/zhangna-5byg5aic/nabi-backend/blob/1430b43b8b11c2aad56feb8805dc7f1092a84d76/img/img_1.png)
##  使用说明
- 登录页面
![img_1.png](https://github.com/zhangna-5byg5aic/nabi-backend/blob/1430b43b8b11c2aad56feb8805dc7f1092a84d76/img/img_1.png)
- 注册页面
![img_2.png](https://github.com/zhangna-5byg5aic/nabi-backend/blob/1430b43b8b11c2aad56feb8805dc7f1092a84d76/img/img_2.png)
- 智能分析页面
![img_5.png](https://github.com/zhangna-5byg5aic/nabi-backend/blob/1430b43b8b11c2aad56feb8805dc7f1092a84d76/img/img_5.png)
- 图表管理页面
![img_6.png](https://github.com/zhangna-5byg5aic/nabi-backend/blob/1430b43b8b11c2aad56feb8805dc7f1092a84d76/img/img_6.png)
## 技术细节
- **自定义 Prompt 预设模板：**
  使用prompt模板控制输入格式，明确输出格式，给定语境和背景，有助于模型更好地理解并生成贴近预期的内容
```java
private static String prompt_template(String goal,String data)
{
    String template = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n" +
            "## 分析目标:\n" +
            "{0}\n" +
            "## 原始数据:\n" +
            "{1}\n"+
            "请根据这两部分内容，按照以下指定格式生成内容(此外不要输出任何多余的开头、结尾、注释)：\n" +
            "【【【【【\n" +
            "(前端 Echarts v5 的 option 配置对象js代码，合理地将数据进行可视化，确保代码正确，代码JSON格式正确，不要生成任何多余的开头、结尾、注释)\n" +
            "【【【【【\n" +
            "(明确的数据分析结论、越详细越好，不要生成多余的注释)";
    return MessageFormat.format(template, goal, data);
}
```
- **分布式限流：** 
针对每个用户，在使用AI能力时进行限流
```java
/**
 * 限流操作
 * @param key 区分不同的限流器，比如不同的用户ID应该分别统计
 */
public void doRateLimit(String key)
{
    RRateLimiter redisLimiter = redissonClient.getRateLimiter(key);
    redisLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
    boolean canOp = redisLimiter.tryAcquire(1);
    if(!canOp)
    {
        throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
    }
}
```
- **分表存储：** 
自定义SQL为原始数据分别创建表
```java
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
```
- **并发和异步处理：**
定义一个Spring Bean，创建并配置一个ThreadPoolExecutor实例
```java
@Bean
  public ThreadPoolExecutor threadPoolExecutor(){
      ThreadFactory threadFactory = new ThreadFactory(){
          private int count=1;

          @Override
          public Thread newThread(@NotNull Runnable r) {
              Thread thread = new Thread(r);
              thread.setName("线程："+count);
              count++;
              return null;
          }
      };
      ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,4,100, TimeUnit.SECONDS,new ArrayBlockingQueue<>(4),threadFactory);
      return threadPoolExecutor;
  }
```
- **消息队列持久化：**
  任务提交时向队列发消息，原本调用AI服务代码放到消费者中
```java 
// 生产者代码：
@Component
public class BiMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message)
    {
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME,BiMqConstant.BI_ROUTING_KEY,message);
    }
}
```

```java
// 消费者代码
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
```

