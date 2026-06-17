# Stage 1: Runtime OS Environment Provision
FROM ubuntu:22.04

# Stop interactive configuration prompts
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies: JDK 21, NGINX, OpenSSH Server, and utilities
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    nginx \
    openssh-server \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

# Configure SSH Engine server setup
RUN mkdir /var/run/sshd
RUN echo 'root:Hello@123' | chpasswd
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# Setup custom local Nginx Proxy settings to redirect incoming traffic
RUN echo 'server {\n\
    listen 80;\n\
    server_name localhost;\n\
    location / {\n\
        proxy_pass http://localhost:8081;\n\
        proxy_set_header Host $host;\n\
        proxy_set_header X-Real-IP $remote_addr;\n\
    }\n\
}' > /etc/nginx/sites-available/default

# Clone, configure working directory and copy local Spring Boot target app
WORKDIR /app
COPY . .

# Run Maven build inside container wrapper to generate final jar file
RUN ./mvnw clean package -DskipTests

# Build entry execution script to start services together concurrently
RUN echo '#!/bin/bash\n\
service ssh start\n\
service nginx start\n\
java -jar target/*.jar --server.port=8081\n\
' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 80 22
CMD ["/app/start.sh"]