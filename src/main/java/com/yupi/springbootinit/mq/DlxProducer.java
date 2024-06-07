package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Scanner;

public class DlxProducer {
    private static final String WORK_EXCHANGE_NAME = "direct2-exchange";
  private static final String DEAD_EXCHANGE_NAME = "dlx-direct-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");

        String queueName = "laoban_dlx_queue";
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, DEAD_EXCHANGE_NAME, "laoban");

        String queueName2 = "waibao_dlx_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "waibao");

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



        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()) {
            String input = scanner.nextLine();
            String[] strings=input.split(" ");
            if(strings.length<1)
                continue;
            String message = strings[0];
            String routingKey = strings[1];
            channel.basicPublish(WORK_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "' with rotingKey:"+routingKey);
        }
    }
  }
}