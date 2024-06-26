# Simple logger

Welcome to Simple Logger, a Spring Boot plugin that will set up logging in your application with just one annotation! <br />
For this plugin to work in your project, it requires the following:
* Java 17 or higher
* Spring Boot 3.1.4 or higher

## Getting started

In order to set up basic logging with all default values in your application, you need to annotate your main method with the SimpleLogging annotation.


```
@SpringBootApplication
@SimpleLogging
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
```
This will enable logging for all your REST Controller methods, it will log the request and the response for each call of the method and any errors that occur during the process.
If you want to disable logging for a specific method or class, you can annotate it with IgnoreLogging annotation.
```
public class ExampleClass {

    @IgnoreLogging
    public void exampleMethod() {
       // this annotation can be used either above a method or above a class depending on your needs
    }
}
```
The SimpleLogging annotation comes with a set of default values that you can customise according to your needs, take a look at all the options explained:

```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SimpleLoggingRegistrar.class)
public @interface SimpleLogging {
    int maxFileSizeMb() default 50; // maximum file size of a singular log file in megabytes

    int maxStringSizeMb() default 5; // maximum string size of a loggable object, if the string takes more than that it is ignored

    String logFilePath() default "logs"; // path to where you want your logs to be saved

    String zippedLogFilePath() default "logs"; // path to where you want your zipped logs to be saved, your logs get zipped automatically after days defined in zipOldLogFilesOlderThanDays property have passed

    String charset() default "UTF-8"; // charset for logging (java.nio.charset.Charset is used here. If your custom charset is unrecognised by it, the logger will use the default charset)

    int maxCacheHistoryLogs() default 100; // the maximum number of logs to be cached in memory, used for CustomFileHandler

    int logRetentionLengthInDays() default 15; // after how many days logs get deleted

    int zipOldLogFilesOlderThanDays() default 4; // after how many days logs get zipped

    String logDeletionCronScheduler() default "0 0 0 * * ?"; // at which time you'd like to schedule log deletion, uses CRON format

    String applicationName() default "application"; // defines the prefix for your log files 

    boolean compressOldLogs() default true; // you are free to disable log compression if you don't need it

    String loggingLevel() default "ALL"; // defines what log levels are saved into a file

    boolean logToConsole() default false; // defines if you want your logs to be present in console as well

    int maxBackupFiles() default 5; // defines the maximum amount of logs in daily log rotation 

}
```

If you want to include a custom property while logging, you can define it like this:
```
import com.simple.logging.application.model.CustomLogProperties;
...

@RestController
public class ExampleController {

    private ExampleService exampleService;

    @PostMapping
    public ResponseEntity<List<ExampleDTO>> getExamples(@RequestBody @Valid final ExampleRequest examplerequest){
        CustomLogProperties.setProperty("userId", "1224");
        return new ResponseEntity<>(exampleService.getExamples(examplerequest), HttpStatus.OK);
    }
}
```
This way, everything logged during the flow of the method your custom property will also be logged along with your payloads: 
```
2024-06-25 15:00:48 INFO: 9c2bb054-ea33-4219-9b80-9c4bd71bb3e2 HTTP METHOD - POST
2024-06-25 15:00:48 INFO: 9c2bb054-ea33-4219-9b80-9c4bd71bb3e2 REQUEST URL - http://localhost:8080/api/example
2024-06-25 15:00:48 INFO: 9c2bb054-ea33-4219-9b80-9c4bd71bb3e2 REQUEST HANDLER - com.app.example.ExampleController#getExamples(ExampleRequest)
2024-06-25 15:00:48 INFO: 9c2bb054-ea33-4219-9b80-9c4bd71bb3e2 HTTP STATUS - 200
2024-06-25 15:00:48 INFO: 9c2bb054-ea33-4219-9b80-9c4bd71bb3e2 userId - 1224
2024-06-25 15:00:48 INFO: 9c2bb054-ea33-4219-9b80-9c4bd71bb3e2 REQUEST BODY: {"eligible":true,"sortField":"TITLE","direction":"ASC"}
2024-06-25 15:00:48 INFO: 9c2bb054-ea33-4219-9b80-9c4bd71bb3e2 RESPONSE BODY: []
```
You are also free to use our log implementation in your entire application so that all your custom logs use the same logic as the automatic ones.
```
import com.simple.logging.application.utility.Log;

...
Log.info("some example log");
...

```
