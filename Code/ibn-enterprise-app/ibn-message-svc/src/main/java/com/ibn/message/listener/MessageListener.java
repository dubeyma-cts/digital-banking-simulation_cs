package com.ibn.message.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @JmsListener(destination = "yourQueueName")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
        // Add your message processing logic here
    }
}