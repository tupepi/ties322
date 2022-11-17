import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Client_onlyAck {

    public static void main(String[] args) throws IOException {
        // Luetaan komentorivi argumentit yhdeksi merkkijonoksi
        // Varataan crc8:lle yksi tavu
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

        // Luodaan kaksi eri pakettia ja viestien crc8 tarkistustavut
        InetAddress address = InetAddress.getByName("localhost");
        int port = 6666;
        byte[] message_bytes0 = message.getBytes();
        message_bytes0 = Arrays.copyOf(message_bytes0, message_bytes0.length + 1);
        message_bytes0[0] = 0x00;
        byte crc8 = Crc8.generoiCrc8(Arrays.copyOfRange(message_bytes0, 1, message_bytes0.length - 1));
        message_bytes0[message_bytes0.length - 1] = crc8;
        DatagramPacket message_packet0 = new DatagramPacket(message_bytes0, message_bytes0.length, address, port);

        byte[] message_bytes1 = Arrays.copyOf(message_bytes0, message_bytes0.length);
        message_bytes1[0] = 0x01;
        byte crc81 = Crc8.generoiCrc8(Arrays.copyOfRange(message_bytes1, 1, message_bytes1.length - 1));
        message_bytes1[message_bytes1.length - 1] = crc81;
        DatagramPacket message_packet1 = new DatagramPacket(message_bytes1, message_bytes1.length, address, port);

        boolean sekvenssi0 = true;

        while (listening) {
            // Lähetetään paketti, oikealla sekvenssillä
            if (sekvenssi0) {
                soketti.send(message_packet0);
            } else {
                soketti.send(message_packet1);
            }
            try {
                byte[] rec = new byte[256];
                DatagramPacket paketti = new DatagramPacket(rec, rec.length);
                soketti.receive(paketti);
                String ack = new String(rec, 0, paketti.getLength() - 1);
                System.out.println(ack);
                System.out.println(rec[0] + " " + sekvenssi0);

                if (ack.substring(1, ack.length()).equals("ACK")) {

                    if (rec[0] == 0x00 && sekvenssi0 || rec[0] == 0x01 && !sekvenssi0) {
                        // Jos on tullut ack paketti ja on haluttu sekvenssi niin lopetetaan kuuntelu
                        listening = false;
                        soketti.close();
                        break;
                    }

                }
                sekvenssi0 = sekvenssi0 ^ true;
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