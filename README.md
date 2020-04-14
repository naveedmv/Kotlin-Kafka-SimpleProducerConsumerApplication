## Kotlin/Java/Scala developer assignment
### Description
In this assignment the applicant should implement a simple application that streams data through Kafka.
The preferred language of the assignment is Kotlin or Java or Scala.

Content of the packages.json:
* affectedNode: the node that sent the alarm
* vnocAlarmID: the alarm Id that represents the alarm type
* alarmEventTime: the time when the event was generated

### Requirements
Create an application that streams the content of package.json through Kafka server, and presents the following:
* Histogram about the most frequent alarms
* Histogram about the nodes that got the most alarms
* Timeline about the ERA015 alarms per hour

Representation can be text based or graphical. No limitation about the technology used.

Kafka can be run easily with docker command:
```
docker-compose -f kafka.yaml up -d
```

The application can use any of the kafka client libraries:
* simple producer-consumer
* kafka streams
* kafka-connect