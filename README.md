# Hystrix HTTP Client

HystrixCommand extension to support JSON Http Client.


### Test Drive

#### 1. Build From Source

```sh
git clone https://github.intuit.com/payments/http-hystrix && cd http-hystrix
./gradlew uberJar
```

#### 2. Run Demo

```sh
java -jar build/libs/http-hystrix-all-0.0.1-SNAPSHOT.jar
```


### How to Use in Your Application?

##### Gradle
```groovy
dependencies {
    compile 'com.intuit.payments:http-hystrix:0.0.1'
    // Dependencies
    compile 'com.netflix.hystrix:hystrix-core:1.5.3'
    compile 'com.google.code.gson:gson:2.3.1'
    compile 'org.apache.httpcomponents:httpclient:4.5.2'
}
```

##### Maven
```xml
   <dependency>
      <groupId>com.intuit.payments</groupId>
      <artifactId>http-hystrix</artifactId>
      <version>0.0.1</version>
      <scope>compile</scope>
   </dependency>
   <!-- Dependencies -->
   <dependency>
      <groupId>com.netflix.hystrix</groupId>
      <artifactId>hystrix-core</artifactId>
      <version>1.5.3</version>
      <scope>compile</scope>
   </dependency>
   <dependency>
      <groupId>com.google.guava</groupId>
      <artifactId>guava</artifactId>
      <version>18.0</version>
      <scope>compile</scope>
   </dependency>
   <dependency>
      <groupId>org.apache.httpcomponents</groupId>
      <artifactId>httpclient</artifactId>
      <version>4.5.2</version>
      <scope>compile</scope>
   </dependency>
```

##### How to POST?
```java
// 1. Construct a HttpHystrix Command
HttpHystrixCommand httpHystrixCommand = new HttpHystrixCommand(
        Http.POST,
        "http://jsonplaceholder.typicode.com/posts",
        "PostJSONCommand",
        "HTTPGroup",
        100000,
        100000
);

// 2. Set a Request Body (HttpHystrix will convert Map to JSON internally)
httpHystrixCommand.body(new HashMap<String, Object>() {{
    put("foo", "bar");
}});

// 3. Execute the command
Map<String, Object> response = httpHystrixCommand.execute();
```

##### How to GET?
```java
// 1. Construct a HttpHystrix Command
HttpHystrixCommand httpHystrixCommand = new HttpHystrixCommand(
        Http.GET,
        "https://httpbin.org/get",
        "GetHttpBinCommand",
        "HTTPGroup",
        100000,
        100000
);

// 2. Set any additional request headers.
Map<String, String> headers = new HashMap<String, String>() {{
        put("_foo1", "bar1");
}};
httpHystrixCommand.headers(headers);
        
// 3. Execute the command
Map<String, Object> response = httpHystrixCommand.execute();
```

### Jenkins 

* [Publish to Nexus Maven Repo](http://paymentsjenkinsm1.corp.intuit.net/view/Product%20Views/view/gopayment/job/http-hystrix-commit/)
