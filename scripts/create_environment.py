import boto3
import json
import os


def get_secret(secret_name, region_name):
    session = boto3.session.Session()
    client = session.client(service_name='secretsmanager', region_name=region_name)
    response = client.get_secret_value(SecretId=secret_name)
    secret_string = response['SecretString']
    return json.loads(secret_string)


def replace_placeholder(value, placeholder, custom_name):
    replaced_value = ""
    try:
        replaced_value = value.replace(placeholder, custom_name)
    except:
        replaced_value = value
    return replaced_value


def main():
    region_name = "ap-south-1"

    secret_names = ['jenkins/staging/contract-staffing-timesheet']

    secrets = {}

    for secret_name in secret_names:
        secret_dict = get_secret(secret_name, region_name)
        secrets.update(secret_dict)

    for key in secrets:
        if key == "DB_HOST_WRITER":
            secrets[key] = replace_placeholder(secrets[key], "{{ APP_ENV }}", 'neo-mr')
        elif key == "DB_HOST_READER":
            secrets[key] = replace_placeholder(secrets[key], "{{ APP_ENV }}", 'neo-mr')
        else:
            secrets[key] = replace_placeholder(secrets[key], "{{ APP_ENV }}", 'neo-mr')

    application_properties = {
        'application.env': 'jenkinsci',
        'spring.application.name': 'timesheet-microservice',
        'spring.application.datasource.read-consistency': 'true',

        'spring.datasource.reader.schema': f"{secrets['DB_DATABASE']}",

        # Writer Datasource
        'spring.datasource.writer.url': f"jdbc:mysql://{secrets['DB_HOST_WRITER']}:3306/{secrets['DB_DATABASE']}?tinyInt1isBit=false",
        'spring.datasource.writer.username': secrets['DB_USERNAME'],
        'spring.datasource.writer.password': secrets['DB_PASSWORD'],
        'spring.datasource.writer.driver-class-name': 'com.mysql.cj.jdbc.Driver',

        # Reader Datasource
        'spring.datasource.reader.url': f"jdbc:mysql://{secrets['DB_HOST_READER']}:3306/{secrets['DB_DATABASE']}?tinyInt1isBit=false",
        'spring.datasource.reader.username': secrets['DB_USERNAME'],
        'spring.datasource.reader.password': secrets['DB_PASSWORD'],
        'spring.datasource.reader.driver-class-name': 'com.mysql.cj.jdbc.Driver',

        # JPA/Hibernate Batching
        # https://www.baeldung.com/jpa-hibernate-batch-insert-update
        # https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/chapters/batch/Batching.html
        'spring.jpa.properties.hibernate.jdbc.batch_size': '50',
        'spring.jpa.properties.hibernate.order_inserts': 'true',
        'spring.jpa.properties.hibernate.order_updates': 'true',
        'spring.jpa.properties.hibernate.jdbc.batch_versioned_data': 'true',

        # JPA Properties
        'spring.jpa.hibernate.ddl-auto': 'none',
        'spring.jpa.generate-ddl': 'false',
        'spring.jpa.show-sql': 'true',
        'spring.jpa.properties.hibernate.format_sql': 'true',
        'spring.jpa.open-in-view': 'false',

        # CORS
        'application.cors.allowed-origins': '',
        'application.cors.allowed-origin-patterns': '',

        # Auth
        'security.recruitcrm.jwt.secret-key': secrets['RECRUITCRM_JWT_SECRET_KEY'],
        'security.recruitcrm.jwt.expiration-time': '2592000000',

        # Flagsmith
        'flagsmith.api.key': secrets['FLAGSMITH_API_KEY'],
        'flagsmith.webhook.secret': secrets['FLAGSMITH_WEBHOOK_SECRET'],

        # Redis
        'spring.data.redis.host': secrets['REDIS_HOST'],

        # Kafka (producer + shared client settings; consumers use their own group.id in other services)
        'app.service.name': 'contract-staffing-service',
        'app.kafka.env-name': secrets['APP_ENV'],
        'app.kafka.topic.notification-timesheet': f"{secrets['APP_ENV']}-reminder-notification",
        'spring.kafka.bootstrap-servers': secrets['KAFKA_BOOTSTRAP_SERVERS'],
        'spring.kafka.producer.client-id': 'timesheet-service-producer',
        'spring.kafka.producer.key-serializer': 'org.apache.kafka.common.serialization.StringSerializer',
        'spring.kafka.producer.value-serializer': 'org.apache.kafka.common.serialization.StringSerializer',
        'spring.kafka.producer.acks': 'all',
        'spring.kafka.producer.retries': '3',
        'spring.kafka.producer.properties.max.in.flight.requests.per.connection': '5',
        'spring.kafka.producer.properties.request.timeout.ms': '30000',
        'spring.kafka.producer.properties.delivery.timeout.ms': '120000',
        'spring.kafka.producer.properties.retry.backoff.ms': '1000',
        'spring.kafka.security.protocol': secrets['KAFKA_SECURITY_PROTOCOL'],
        'spring.kafka.sasl.mechanism': secrets['KAFKA_SASL_MECHANISM'],
        'spring.kafka.sasl.username': secrets['KAFKA_SASL_USERNAME'],
        'spring.kafka.sasl.password': secrets['KAFKA_SASL_PASSWORD'],
        'spring.kafka.consumer.group-id': secrets['KAFKA_CONSUMER_GROUP_ID'],
        'spring.kafka.consumer.enable-partition-eof': secrets['KAFKA_CONSUMER_ENABLE_PARTITION_EOF'],
        'spring.kafka.consumer.session-timeout-ms': secrets['KAFKA_CONSUMER_SESSION_TIMEOUT_MS'],

        # Auditlog
        'auditlog.sqs.queue-name': f"{secrets['APP_ENV']}-auditlog-events.fifo",
        'auditlog.transport.driver': "aws-sqs",
        'auditlog.library.context': "producer",

        # Server Config
        'server.port': '8080',
        'spring.autoconfigure.exclude': 'org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration',

        #Logging
        'logger.log-source': 'jenkins-timesheet-microservice',
        'logger.context':'sync',
        'logging.file.path':'log',

        #SQS
        'spring.cloud.aws.credentials.access-key':f"{secrets['SERVICE_AWS_ACCESS_KEY']}",
        'spring.cloud.aws.credentials.secret-key':f"{secrets['SERVICE_AWS_SECRET_ACCESS_KEY']}",
        'spring.cloud.aws.region.static':f"{secrets['SERVICE_AWS_REGION']}",


         # Swagger Authentication
         'swagger.username': 'internal_dev',
         'swagger.password': secrets.get('SWAGGER_PASSWORD', 'posting1-starlight-deflector'),
    }

    output_path = 'src/main/resources/application.properties'
    cwd = os.getcwd()
    full_output_path = os.path.join(cwd, output_path)

    with open(full_output_path, 'w') as env_file:
        for key, value in application_properties.items():
            env_file.write(f"{key}={value}\n")


if __name__ == '__main__':
    main()
