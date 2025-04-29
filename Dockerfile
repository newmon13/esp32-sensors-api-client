FROM eclipse-mosquitto
RUN echo "listener 1883 0.0.0.0" > /mosquitto/config/mosquitto.conf
RUN echo "allow_anonymous true" > /mosquitto/config/mosquitto.conf