import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

public class Main {

    public static class User {
        private String name;
        private boolean online;
        private long lastResponseTime = System.currentTimeMillis();

        public User(String name, boolean online) {
            this.name = name;
            this.online = online;
        }

        public String getName() {
            return name;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getLastResponseTime() {
            return lastResponseTime;
        }

        public void setLastResponseTime(long lastResponseTime) {
            this.lastResponseTime = lastResponseTime;
        }
    }

    public static void checkHello(MulticastSocket socket, int timeout, Map<String, User> userSet, String myUID) throws IOException {
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
                        user.setOnline(true);
                        user.setName(name);
                        user.setLastResponseTime(System.currentTimeMillis());
                    } else {
                        userSet.put(UID, new User(name, true));
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Socket timeout");
                return;
            }
        }
    }

    public static void sendHello(MulticastSocket socket, String UID, String name, InetAddress address, int port) throws IOException {
        String msg = "Hello " + UID + " " + name;
        DatagramPacket datagram = new DatagramPacket(msg.getBytes(), msg.length(), address, port);
        socket.send(datagram);
    }

    public static void main(String[] args) {

        Map<String, User> usersSet = new HashMap<>();

        final int SEND_HELLO_TIME = 3;
        final int CHECK_HELLO_TIME = 9;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter multicast address: ");
            InetAddress address = InetAddress.getByName(reader.readLine());
            System.out.print("Enter port: ");
            int port = Integer.parseInt(reader.readLine());
            MulticastSocket s = new MulticastSocket(port);
            System.out.print("Enter network interface ip: ");
            s.joinGroup(new InetSocketAddress(address, port), NetworkInterface.getByInetAddress(InetAddress.getByName(reader.readLine())));
            System.out.print("Enter your name: ");

            String name = reader.readLine();
            String UID = generateUid();

            long lastHelloTime = System.currentTimeMillis();
            long lastCheckTime = System.currentTimeMillis();

            do {
                if(System.currentTimeMillis() - lastHelloTime > SEND_HELLO_TIME * 1000) {
                    sendHello(s, UID, name, address, port);
                    lastHelloTime = System.currentTimeMillis();
                }
                if(System.currentTimeMillis() - lastCheckTime > CHECK_HELLO_TIME * 1000) {
                    checkHello(s, 2000, usersSet, UID);
                    lastCheckTime = System.currentTimeMillis();
                    usersSet.forEach((uid, user) -> {
                        user.setOnline(System.currentTimeMillis() - user.getLastResponseTime() <= CHECK_HELLO_TIME * 2 * 1000);
                        System.out.println("UID : " + uid + "\n" + "Name : " + user.getName() + "\n" + (user.isOnline() ? "Online" : "Offline"));
                    });
                }
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateUid() {
        return new String(Base64.getEncoder().encode(Long.toString(System.nanoTime()).getBytes()));
    }

}
