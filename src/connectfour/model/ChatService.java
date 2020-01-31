/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectfour.model;

import java.rmi.RemoteException;

/**
 *
 * @author Kris
 */
public class ChatService implements IChatService {

    private String name;

    public ChatService(String name) throws RemoteException {
        this.name = name;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public void send(String message) throws RemoteException {
        System.out.println(name + " received msg: " + message);
    }

    @Override
    public void setName(String name) throws RemoteException {
        this.name = name;
    }

}
