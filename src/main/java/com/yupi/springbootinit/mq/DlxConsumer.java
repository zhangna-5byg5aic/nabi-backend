package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxConsumer {

  private static final String WORK_EXCHANGE_NAME = "direct2-exchange";

  private static final String DEAD_EXCHANGE_NAME = "dlx-direct-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

    //指定死信队列参数
    Map<String, Object> args = new HashMap<String, Object>();
    //指定死信交换机
    args.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
    //指定死信处理队列
    args.put("x-dead-letter-routing-key", "waibao");

    //创建两个队列
    String queueName = "dog_queue";
    channel.queueDeclare(queueName, true, false, false, args);
    channel.queueBind(queueName, WORK_EXCHANGE_NAME, "dog");

    Map<String, Object> args2 = new HashMap<String, Object>();
    args2.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
    args2.put("x-dead-letter-routing-key", "laoban");
    //创建两个队列
    String queueName2 = "cat_queue";
    channel.queueDeclare(queueName2, true, false, false, args2);
    channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "cat");


    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        //拒绝消息
        channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
        System.out.println(" [dog] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
      DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          //拒绝消息
          channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
          System.out.println(" [cat] Received '" +
                  delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
      };
    channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> { });

    //处理死信队列的消息
    DeliverCallback laobandeliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      //拒绝消息
      channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
      System.out.println(" [laoban] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    DeliverCallback waibaodeliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      //拒绝消息
      channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
      System.out.println(" [waibao] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    channel.basicConsume(queueName, false, laobandeliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, false, waibaodeliverCallback, consumerTag -> { });
  }
}