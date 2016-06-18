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
    compile "com.intuit.payments:http-hystrix:0.0.1-SNAPSHOT"
}
```

##### Maven
```xml
<dependency>
   <groupId>com.intuit.payments</groupId>
   <artifactId>http-hystrix</artifactId>
   <version>0.0.1-SNAPSHOT</version>
</dependency>
```