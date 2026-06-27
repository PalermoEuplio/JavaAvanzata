package model.connection;

import java.io.Serializable;
import java.util.function.Consumer;

public class Server extends NetworkConnection{
    @Override
    public boolean isServer(){
        return true;
    }

    public Server(int port, Consumer<Serializable> onRecive) {
        super(null, port, onRecive);
    }
}
