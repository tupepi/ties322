import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {
        // Luodaan soketti
        DatagramSocket soketti = new DatagramSocket(5678);
        Scanner scanner = new Scanner(System.in);
        byte seq = 0;
        while (true) {
            try {
                String message;
                System.out.print("Client>");
                message = scanner.nextLine();
                message = "0" + message;
                // Luodaan lähetettävä paketti ja viestin crc8 tarkistustavu
                InetAddress address = InetAddress.getByName("localhost");
                int port = 6666;
                byte[] message_bytes = message.getBytes();
                message_bytes[0] = seq;
                byte crc8 = Crc8.generoiCrc8(message_bytes);
                message_bytes = Arrays.copyOf(message_bytes, message_bytes.length + 1);
                message_bytes[message_bytes.length - 1] = crc8;
                DatagramPacket message_packet = new DatagramPacket(message_bytes, message_bytes.length, address, port);

                boolean listening = true;
                while (listening) {
                    // Lähetetään paketti
                    soketti.send(message_packet);
                    soketti.setSoTimeout(1500);
                    try {
                        byte[] rec = new byte[256];
                        DatagramPacket paketti = new DatagramPacket(rec, rec.length);
                        soketti.receive(paketti);
                        String ack = new String(rec, 0, paketti.getLength());
                        System.out.println("Server: " + ack);
                        if (ack.substring(1, ack.length() - 1).equals("NAK")) {
                            // Jos ymmärsin oikein niin rdt3:ssä odotetaan aina timer loppuun
                            // Jos halutaan reagoida heti bittivirheisiin, eikä odottaa
                            // listening = true;
                        } else if (ack.substring(1, ack.length() - 1).equals("ACK") && seq == rec[0]) {
                            listening = false;
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("    resending: " + message);
                        listening = true;
                        continue;
                    }
                }
                // sekvenssi vaihdetaan nollasta ykköseen tai toisin päin
                seq = seq == 1 ? (byte) 0 : (byte) 1;
            } catch (IOException e) {
                soketti.close();
                scanner.close();
                System.out.println("catch");
                break;
            }
        }
    }
}