import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Client_AckAndNak {

    public static void main(String[] args) throws IOException {
        // Luetaan komentorivi argumentit yhdeksi merkkijonoksi
        // Varataan ainakin yksi tavu crc8:lle
        String message = "0";
        if (args.length > 0) {
            message = message + args[0];
        }
        for (int i = 1; i < args.length; i++) {
            message = message + " " + args[i];
        }

        // Luodaan soketti
        DatagramSocket soketti = new DatagramSocket(5678);
        boolean listening = true;

        // Luodaan lähetettävä paketti ja viestin crc8 tarkistustavu
        InetAddress address = InetAddress.getByName("localhost");
        int port = 6666;
        byte[] message_bytes = message.getBytes();
        message_bytes[0] = 0;
        byte crc8 = Crc8.generoiCrc8(message_bytes);
        message_bytes = Arrays.copyOf(message_bytes, message_bytes.length + 1);
        message_bytes[message_bytes.length - 1] = crc8;
        DatagramPacket message_packet = new DatagramPacket(message_bytes, message_bytes.length, address, port);

        while (listening) {
            // Lähetetään paketti
            soketti.send(message_packet);

            try {
                byte[] rec = new byte[256];
                DatagramPacket paketti = new DatagramPacket(rec, rec.length);
                soketti.receive(paketti);
                String ack = new String(rec, 0, paketti.getLength());
                System.out.println(ack);
                if (ack.substring(1, ack.length() - 1).equals("NAK")) {
                    // Jos vastaus on NACK jatketaan kuuntelua ja lähetetään paketti uudestaan
                    listening = true;
                } else if (ack.substring(1, ack.length() - 1).equals("ACK")) {
                    // suljetaan soketti ja lopettaan kuuntelu jos tuli ack
                    listening = false;
                    soketti.close();
                    break;
                }
            }

            catch (IOException e) {
                listening = false;
                System.out.println("catch");
                soketti.close();
                break;
            }
        }
    }
}