package com.geleeq.program;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.geleeq.pubsub.Publisher;
import com.geleeq.pubsub.Subscriber;

public class Program {

    public static final String CHANNEL_NAME = "commonChannel";

    public static void main(String[] args) throws Exception {

        JedisPoolConfig poolConfig = new JedisPoolConfig();

        JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379, 3000);

        final Jedis subscriberJedis = jedisPool.getResource();

        final Subscriber subscriber = new Subscriber();
        //订阅的客户端线程，监听发布信息
        new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("Subscribing to \"commonChannel\". This thread will be blocked.");
                    subscriberJedis.subscribe(subscriber, CHANNEL_NAME);
                    System.out.println("Subscription ended.");
                } catch (Exception e) {
                    System.out.println("Subscribing failed: "+e.getMessage());
                }
            }
        }).start();

        Jedis publisherJedis = jedisPool.getResource();
        //发布，循环读取输入流，quit停止
        new Publisher(publisherJedis, CHANNEL_NAME).start();
        //取消订阅
        subscriber.unsubscribe();
        //回收资源，关闭连接池
        jedisPool.returnResource(subscriberJedis);
        jedisPool.returnResource(publisherJedis);
        jedisPool.close();
    }
}
