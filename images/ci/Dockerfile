FROM adoptopenjdk:8-jdk-hotspot

RUN apt update -y && apt install -y \
    git \
    openssh-client \
    python3 \
    python3-pip \
    unzip \
    wget \
    docker \
    && apt clean
    && systemctl enable docker
    && systemctl restart docker
