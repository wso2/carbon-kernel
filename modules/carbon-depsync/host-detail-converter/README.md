A custom converter for adding ip and hostname to the log message. Please add configuration below to ge the ip/hostname. 

<PatternLayout pattern="[%d] %5p {%c} [%hostname] - [%ip]- %m%ex%n"/>
