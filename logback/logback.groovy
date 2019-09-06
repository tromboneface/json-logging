
import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter
import java.nio.charset.Charset
import net.logstash.logback.encoder.LogstashEncoder

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

final String applicationName = 'clarity-scripts'

// See http://logback.qos.ch/manual/groovy.html for details on configuration

final String defaultPattern = //"%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} [%thread] %-5level %logger{5} - %msg%n"
        '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                '%clr(%5p) ' + // Log level
                '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                '%m%n%wex' // Message


appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}


appender('ROLLER', RollingFileAppender) {
    file =  "${System.getProperty('user.home')}/logs/${applicationName}.log"
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "${System.getProperty('user.home')}/logs/${applicationName}.%i.log"
        minIndex = 1
        maxIndex = 99
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = '10MB'
    }
    encoder(PatternLayoutEncoder) {
        pattern = defaultPattern
    }
}

appender('JSON', RollingFileAppender) {
    file =  "${System.getProperty('user.home')}/json-logs/${applicationName}-json.log"
    rollingPolicy(FixedWindowRollingPolicy) {
        fileNamePattern = "${System.getProperty('user.home')}/json-logs/${applicationName}-json.log.%i"
        minIndex = 1
        maxIndex = 99
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = '10MB'
    }
    encoder(LogstashEncoder) {
        timeZone = 'UTC'
        customFields = '{"application-name":"' + applicationName + '"}'
    }
}

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}

root INFO, ['STDOUT','ROLLER','JSON']

