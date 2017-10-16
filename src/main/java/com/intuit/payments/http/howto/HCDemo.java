package com.intuit.payments.http.howto;

import com.intuit.payments.http.Client;
import com.intuit.payments.http.Response;

import static java.lang.System.out;

public class HCDemo {

    public static void main(String[] args) {
        Client client = new Client("https://httpbin.org");
        Response response = client.Request("GetIpCommand", "HttpBinGroup", "/status/400")
                .GET()
                .execute();
        out.println("__Response__");
        out.println(response.statusCode());
        response.raise_for_status();
    }

}
