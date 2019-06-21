# HTTP for Humans

HC is designed to be simplest Http client for Java. By default, the calls are protected by [Hystrix](https://github.com/Netflix/Hystrix/wiki) circuit breakers.


### Test Drive

#### 1. Build From Source

```sh
git clone https://github.intuit.com/payments/http-hystrix && cd http-hystrix
./gradlew uberJar
```

#### 2. Run Demo

```sh
java -jar build/libs/hc-all-0.1.1-SNAPSHOT.jar
```


### How to Use in Your Application?

##### Gradle
```groovy
dependencies {
    compile 'com.intuit.payments.http:hc:1.0.8'
    // add HC's dependencies
    compile 'com.netflix.hystrix:hystrix-core:1.5.3'
    compile 'com.google.code.gson:gson:2.3.1'
    compile 'org.apache.httpcomponents:httpclient:4.5.2'
}
```

##### Maven
```xml
   <dependency>
      <groupId>com.intuit.payments.http</groupId>
      <artifactId>hc</artifactId>
      <version>1.0.8</version>
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

##### Making a HTTP GET Call

```java
Client client = new Client("https://httpbin.org");
Response response = client.Request("GetCommand","HttpGroup", "/get")
        .GET()
        .header("intuit_tid", "12345")
        .execute();

```


##### Making a HTTP POST Call

```java
Client client = new Client("http://jsonplaceholder.typicode.com");

Response response = client.Request("PostCommand", "HttpGroup","/posts")
        .POST()
        .body(
                new HashMap<String, Object>() {{
                    put("foo", "bar");
                }}
        ).execute();

```


### [Jenkins Build](https://build.intuit.com/payments/blue/organizations/jenkins/payments%2Fhttp-hystrix%2Fhttp-hystrix/activity) 
