package com.intuit.payments.http.howto;

import com.intuit.payments.http.Client;
import com.intuit.payments.http.Response;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.System.out;

public class TalkDemo {

//    static {
//        HystrixPlugins.getInstance().registerMetricsPublisher(HystrixServoMetricsPublisher.getInstance());
//    }

    public static void main(String[] args) {
        Client client = new Client("https://httpbin.org");


        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                out.println("__Request__");

                Response response = client.Request("GetCommand","HttpGroup", "/get")
                        .GET()
                        .execute();

                out.println("__Response__\n" +
                        response.statusCode() + " " +
                        response.statusReason() + "\n" + response.map());

            }

        };


        Timer timer = new Timer(); // 5 second pause
        timer.schedule(task, 10,10000);
    }
}
