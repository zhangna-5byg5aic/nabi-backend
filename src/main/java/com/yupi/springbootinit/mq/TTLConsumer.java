package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TTLConsumer {

    private final static String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argv) throws Exception {
        //创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        //声明队列
        Channel channel = connection.createChannel();
        //创建队列，指定消息过期参数TTL
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-message-ttl", 5000);
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        //定义了如何处理消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        //消费消息，会持续阻塞
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    }
}