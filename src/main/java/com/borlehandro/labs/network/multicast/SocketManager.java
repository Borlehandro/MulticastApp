package com.borlehandro.labs.network.multicast;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketManager {

    private final MulticastSocket socket;

    public SocketManager(InetAddress multicastAddress, int port, InetAddress networkInterfaceAddress) throws IOException {
        socket = new MulticastSocket(port);
        socket.joinGroup(new InetSocketAddress(multicastAddress, port), NetworkInterface.getByInetAddress(networkInterfaceAddress));
    }

    public void sendHello(String UID, String name, InetAddress address, int port) throws IOException {
        String msg = "Hello " + UID + " " + name;
        DatagramPacket datagram = new DatagramPacket(msg.getBytes(), msg.length(), address, port);
        socket.send(datagram);
    }

    public void checkUsers(int timeout, Map<String, User> userSet, String myUID, AtomicBoolean tableUpdater) throws IOException {
        socket.setSoTimeout(timeout);
        while (true) {
            try {
                byte[] buf = new byte[1000];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                socket.receive(recv);
                String[] data = (new String(recv.getData())).split(" ");
                String UID = data[1];
                String name = data[2];
                if(!UID.equals(myUID)) {
                    User user;
                    if((user = userSet.getOrDefault(UID, null)) != null) {
                        String prevName = user.getName();
                        boolean prevOnline = user.isOnline();

                        user.setOnline(true);
                        user.setName(name);

                        tableUpdater.set(tableUpdater.get() | (!user.getName().equals(prevName) | user.isOnline() != prevOnline));

                        user.setLastResponseTime(System.currentTimeMillis());
                    } else {
                        userSet.put(UID, new User(name, true));
                        tableUpdater.set(true);
                    }
                }
            } catch (SocketTimeoutException e) {
                // System.out.println("Socket timeout");
                return;
            }
        }
    }

}