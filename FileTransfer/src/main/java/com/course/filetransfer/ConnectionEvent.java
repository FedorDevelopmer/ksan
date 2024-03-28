package com.course.filetransfer;

import javafx.event.Event;
import javafx.event.EventType;

public class ConnectionEvent extends Event {

    private ConnectionStatus status;
    private static EventType<ConnectionEvent> connectionEventType = new EventType<>("Connected");

    public ConnectionEvent(ConnectionStatus status) {
        super(connectionEventType);
        this.status=status;

    }

    public boolean getStatus(ConnectionStatus eventStatus){
        return status.equals(eventStatus);
    }

    public static EventType<ConnectionEvent> EventType(){
        return connectionEventType;
    }
}
