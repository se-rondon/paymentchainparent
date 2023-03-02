# El archivo dockerfile es aquel que define las propiedades para crear una imagen de docker.

# To change this license header, choose License Headers in Project Properties.
# To change this template file, choose Tools | Templates
# and open the template in the editor.
FROM openjdk:17-oracle
#RUN addgroup -S paymentchain && adduser -S admin -G paymentchain
# Por seguridad no se ejecuta con el usuario root así que, creamos uno nuevo.
#USER admin:paymentchain
VOLUME /tmp
#Indicamos dónde está el ejecutable
ARG JAR_FILE=target/*.jar
#Renombramos ejecutable.
ADD target/${JAR_FILE} app.jar
# Variables de entorno
ENV JAVA_OPTS=""
# Comando que se ejecutan cuando se genere el contenedor
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
