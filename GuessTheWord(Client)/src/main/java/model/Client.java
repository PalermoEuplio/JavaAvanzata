package model;

import java.io.Serializable;
import java.util.function.Consumer;

public class Client extends NetworkConnection {
    @Override
    public boolean isServer(){
        return false;
    }

    public Client(String ip, int port, Consumer<Serializable> onRecive) {
        super(ip, port, onRecive);
    }
}
