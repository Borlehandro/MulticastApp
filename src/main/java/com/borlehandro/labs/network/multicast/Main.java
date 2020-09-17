package com.borlehandro.labs.network.multicast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    public static void main(String[] args) {

        Map<String, User> usersSet = new HashMap<>();

        SettingsLoader settingsLoader = new SettingsLoader(Thread.currentThread().getContextClassLoader().getResourceAsStream("settings.properties"));

        final long SAY_HELLO_INTERVAL = settingsLoader.loadProperty(SettingsLoader.Property.SAY_HELLO_INTERVAL);
        final long CHECK_USERS_INTERVAL = settingsLoader.loadProperty(SettingsLoader.Property.CHECK_USERS_INTERVAL);
        final long MAX_FAILED_CHECKS = settingsLoader.loadProperty(SettingsLoader.Property.MAX_FAILED_CHECKS);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            InetAddress address = InetAddress.getByName(settingsLoader.loadMulticastIp());

            int port = settingsLoader.loadPort();

            System.out.print("Enter network interface ip: ");
            InetAddress networkInterfaceAddress = InetAddress.getByName(reader.readLine());

            SocketManager socketManager = new SocketManager(address, port, networkInterfaceAddress);

            System.out.print("Enter your name: ");

            String name = reader.readLine();
            String UID = User.generateUid();

            long lastHelloTime = System.currentTimeMillis();
            long lastCheckTime = System.currentTimeMillis();

            long timeout = settingsLoader.loadProperty(SettingsLoader.Property.SOCKET_TIMEOUT);

            System.out.println("Waiting for users...");

            do {
                if (System.currentTimeMillis() - lastHelloTime > SAY_HELLO_INTERVAL) {
                    socketManager.sendHello(UID, name, address, port);
                    lastHelloTime = System.currentTimeMillis();
                }

                if (System.currentTimeMillis() - lastCheckTime > CHECK_USERS_INTERVAL) {

                    // True if user table was updated
                    AtomicBoolean tableUpdated = new AtomicBoolean();
                    tableUpdated.set(false);

                    socketManager.checkUsers((int) timeout, usersSet, UID, tableUpdated);

                    lastCheckTime = System.currentTimeMillis();

                    usersSet.forEach((uid, user) -> {
                        boolean prevOnline = user.isOnline();
                        user.setOnline(System.currentTimeMillis() - user.getLastResponseTime() <= CHECK_USERS_INTERVAL * MAX_FAILED_CHECKS);
                        tableUpdated.set(tableUpdated.get() | (user.isOnline() != prevOnline));
                    });

                    // Write table if it has been updated
                    if (tableUpdated.get()) {
                        System.out.println("----- Users -----");
                        usersSet.forEach((uid, user) -> {

                            System.out.println("UID : " + uid + "\n" + "Name : " + user.getName() + "\n" + (user.isOnline() ? "Online" : "Offline"));
                            System.out.println("-----------------");
                        });
                        System.out.println();
                    }
                }
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}