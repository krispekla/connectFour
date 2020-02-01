/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kris
 */
public class ChatService implements IChatService {

    private String name;
    private List<IChatListener> listeners = new ArrayList<>();

    public ChatService(String name) throws RemoteException {
        this.name = name;
    }

    public void addListener(IChatListener listener) {
        listeners.add(listener);
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public void send(String message) throws RemoteException {
        for (IChatListener listener : listeners) {
            listener.onMesssageChange(message);
        }
    }
}
