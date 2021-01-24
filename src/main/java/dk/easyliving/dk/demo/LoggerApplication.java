package dk.easyliving.dk.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import dk.easyliving.dto.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedWriter;
import java.io.FileWriter;

@SpringBootApplication
public class LoggerApplication implements CommandLineRunner {
    private final static String QUEUE_NAME = "logs";

    @Autowired
    public static void main(String[] args) {
        SpringApplication.run(LoggerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.240");
        Connection connection = factory.newConnection();

        Channel logChannel = connection.createChannel();
        logChannel.queueDeclare(QUEUE_NAME, true, false, false, null);

        DeliverCallback callBackAddController = (consumerTag, delivery) ->
        {
            //Put message in Json format in a string
            String message = new String(delivery.getBody(), "UTF-8");
            ObjectMapper obj = new ObjectMapper();

            //create a Log object from the string
            Log temp = obj.readValue(message, Log.class);

            //Create a log string from the log object
            String tempstr = temp.getTimestamp() + "," + temp.getHost() + ","+temp.getProcess() + "," + temp.getErrorLevel() + "," +temp.getMessage() +"\r\n";

            //write the line to a log file
            BufferedWriter writer = new BufferedWriter(new FileWriter(".\\log.txt", true));

            writer.append(tempstr);

            writer.close();
        };
        logChannel.basicConsume(QUEUE_NAME, true, callBackAddController, consumerTag -> { });
        System.out.println("Listning to channel \"" + QUEUE_NAME+"\"" );
    }
}
