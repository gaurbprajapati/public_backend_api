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

    # Change here for new repository
    secret_names = ['fractional/common/candidate']

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
            secrets[key] = replace_placeholder(secrets[key], "{{ APP_ENV }}", 'jenkinsci')

        secrets[key] = replace_placeholder(secrets[key], "{{ BASE_DOMAIN }}", 'recruitcrm.io')
        secrets[key] = replace_placeholder(secrets[key], "{{ APP_DEBUG }}", 'false')
        secrets[key] = replace_placeholder(secrets[key], "{{ ENABLE_ES_LOGGING_AND_NR }}", 'false')

    secrets['AWS_REGION'] = "eu-west-1"

    application_properties = {
        'spring.application.name': 'candidate-microservice',
        'spring.application.datasource.read-consistency': 'true',

        # Writer Datasource
        'spring.datasource.writer.url': f"jdbc:mysql://tf-neo-mr-db-write.recruitcrm.io:3306/{os.environ['DB_DATABASE']}?tinyInt1isBit=false",
        'spring.datasource.writer.username': os.environ['DB_USERNAME'],
        'spring.datasource.writer.password': os.environ['DB_PASSWORD'],
        'spring.datasource.writer.driver-class-name': 'com.mysql.cj.jdbc.Driver',

        # Reader Datasource
        'spring.datasource.reader.url': f"jdbc:mysql://tf-neo-mr-db-read.recruitcrm.io:3306/{os.environ['DB_DATABASE']}?tinyInt1isBit=false",
        'spring.datasource.reader.username': os.environ['DB_USERNAME'],
        'spring.datasource.reader.password': os.environ['DB_PASSWORD'],
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
        'spring.jpa.open-in-view' : 'false',

        # CORS
        'application.cors.allowed-origins': '*',

        # JWT
        'security.jwt.secret-key': os.environ['JWT_SECRET_KEY'],
        'security.jwt.expiration-time': '2592000000',

        # Flagsmith
        'flagsmith.api.key': secrets['FLAGSMITH_API_KEY'],
        'flagsmith.webhook.secret': secrets['FLAGSMITH_WEBHOOK_SECRET'],

        # Redis
        'spring.data.redis.host': 'tf-neo-mr-redis.recruitcrm.io',

        # Kafka
        'spring.kafka.bootstrap-servers': secrets['KAFKA_BOOTSTRAP_SERVERS'],
        'spring.kafka.security.protocol': secrets['KAFKA_SECURITY_PROTOCOL'],
        'spring.kafka.sasl.mechanism': secrets['KAFKA_SASL_MECHANISM'],
        'spring.kafka.sasl.username': secrets['KAFKA_SASL_USERNAME'],
        'spring.kafka.sasl.password': secrets['KAFKA_SASL_PASSWORD'],
        'spring.kafka.consumer.group-id': secrets['KAFKA_CONSUMER_GROUP_ID'],
        'spring.kafka.consumer.enable-partition-eof': secrets['KAFKA_CONSUMER_ENABLE_PARTITION_EOF'],
        'spring.kafka.consumer.session-timeout-ms': secrets['KAFKA_CONSUMER_SESSION_TIMEOUT_MS'],

        #RabbitMQ
        # 'spring.rabbitmq.ssl.enabled': secrets['RABBITMQ_SSL_ENABLED'],
        # 'spring.rabbitmq.host': secrets['RABBITMQ_HOST'],
        # 'spring.rabbitmq.port': secrets['RABBITMQ_PORT'],
        # 'spring.rabbitmq.username': secrets['RABBITMQ_USERNAME'],
        # 'spring.rabbitmq.password': secrets['RABBITMQ_PASSWORD'],

        #Auditlog
        'auditlog.rabbitmq.queue-name' : "jenkinsci-auditlog",
        'auditlog.rabbitmq.direct-exchange-name' : "jenkinsci-auditlog-direct-exchange",
        'auditlog.rabbitmq.routing-key' : "jenkinsci-auditlog-routing-key",

        'auditlog.rabbitmq.dead-letter-queue-name' : "jenkinsci-auditlog-dead-letter",
        'auditlog.rabbitmq.dead-letter-direct-exchange-name' : "jenkinsci-auditlog-dead-letter-direct-exchange",
        'auditlog.rabbitmq.dead-letter-routing-key' : "jenkinsci-auditlog-dead-letter-routing-key",
        # TTL in milliseconds (1 hour)
        'auditlog.rabbitmq.dead-letter-message-ttl' : "3600000",

        #Server Config
        'server.port': '8080',
        'spring.autoconfigure.exclude': 'org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration',
    }

    output_path = 'src/main/resources/application.properties'
    cwd = os.getcwd()
    full_output_path = os.path.join(cwd, output_path)

    with open(full_output_path, 'w') as env_file:
        for key, value in application_properties.items():
            env_file.write(f"{key}={value}\n")


if __name__ == '__main__':
    main()
