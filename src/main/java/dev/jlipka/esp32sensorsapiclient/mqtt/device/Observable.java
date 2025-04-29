package dev.jlipka.esp32sensorsapiclient.mqtt.device;

public interface Observable {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}
