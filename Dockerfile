# jenkins/inbound-agent:jdk21 pinned to an immutable digest (see CWE-494 supply-chain fix)
FROM jenkins/inbound-agent@sha256:75e7c4f8ea2978657efb128477cb947f224fbcfa279864cc6f27ce4038885e64

USER root

RUN apt-get update
RUN apt-get install -y zip unzip apt-transport-https ca-certificates curl gnupg2 software-properties-common redis-tools default-mysql-client htop gettext

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    build-essential \
    git \
    wget

# Install maven
RUN apt-get update
RUN apt-get install -y --no-install-recommends maven

# Create virtual environment
RUN apt update
RUN apt install -y python3-venv
RUN python3 -m venv /opt/env
RUN chown -R jenkins:jenkins /opt/

# Install pipx
RUN apt update
RUN apt install -y pipx
RUN pipx ensurepath

# Install AWS CLI
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install
RUN rm -rf awscliv2.zip aws

# Install sonar-scanner
# ENV SONAR_SCANNER_VERSION=5.0.1.3006
# ENV SONAR_SCANNER_HOME /opt/sonar-scanner-$SONAR_SCANNER_VERSION-linux
# RUN curl --create-dirs -sSLo /opt/sonar-scanner.zip https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SONAR_SCANNER_VERSION-linux.zip
# RUN unzip -o /opt/sonar-scanner.zip -d /opt/
# RUN rm -rf /opt/sonar-scanner.zip
# ENV PATH $SONAR_SCANNER_HOME/bin:$PATH
# ENV SONAR_SCANNER_OPTS "-server"

# Set Jenkins user
USER jenkins