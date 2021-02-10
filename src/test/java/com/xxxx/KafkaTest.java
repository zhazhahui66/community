package com.xxxx;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@SpringBootTest
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void kafkaTest(){
        kafkaProducer.sendMessage("test","Nihc");
        kafkaProducer.sendMessage("test","在吗?");
        try {
            Thread.sleep(1000 *10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
@Component
class KafkaProducer{
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}
@Component
class KafkaConsumer{

    @KafkaListener(topics = {"test"})
    public void hadnlerMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}
