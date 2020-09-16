import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static class User {
        private final String name;
        private boolean online;
        private int timesWithoutResponse = 0;

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

        public void noResponse() {
            if(timesWithoutResponse > 3) {
                online = false;
            } else
                timesWithoutResponse++;
        }

        public void response() {
            if(!online) {
                timesWithoutResponse = 0;
                online = true;
            } else {
                timesWithoutResponse--;
            }
        }
    }

    public static void checkHello(MulticastSocket socket, int timeout) throws IOException {
        socket.setSoTimeout(timeout);
        while (true) {
            try {
                byte[] buf = new byte[1000];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                socket.receive(recv);
            } catch (SocketTimeoutException e) {
                
            }
        }
    }

    public static void main(String[] args) {

        Set<User> usersSet = new HashSet<>();

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
            String msg = "Hello " + UID + " " + name;
            while(!msg.isEmpty()) {
                msg = reader.readLine();
                DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(), address, port);
                s.send(hi);
                System.out.println("Get " + new String(buf));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateUid() {
        return new String(Base64.getEncoder().encode(Long.toString(System.nanoTime()).getBytes()));
    }

}
