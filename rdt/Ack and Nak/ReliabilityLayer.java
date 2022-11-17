import java.net.*;
import java.io.*;
import java.util.Arrays;

public class ReliabilityLayer {

    private int last_packet_seq = 1;

    public ReliabilityLayer() {
        super();
    }

    public void start(int portti) throws SocketException {
        // Luodaan soketti
        VirtualSocket soketti = new VirtualSocket(portti);
        boolean listening = true;
        System.out.println("Listening...");
        while (listening) {
            try {
                byte[] rec = new byte[256];
                DatagramPacket paketti = new DatagramPacket(rec, rec.length);
                soketti.receive(paketti);
                String viesti = new String(rec, 0, paketti.getLength() - 1);

                byte[] data = Arrays.copyOfRange(paketti.getData(), 0, paketti.getLength());

                byte seq = paketti.getData()[0];

                // CRC8 virheen tunnistus-----------------------------------
                boolean bittivirhe = Crc8.onBittivirhe(data);
                if (bittivirhe) {
                    System.out.println("bit error in: " + viesti);
                    // ---------------------------------------------------
                } else if (seq == last_packet_seq) {
                    System.out.println("duplicate: " + viesti);
                } else {
                    System.out.println(viesti);
                    last_packet_seq = seq;
                }

                // System.out.println(rec[paketti.getLength()-1]);// crc8

                // Voidaan valita mitä protokollaa käytetään.
                // Vastataanko Ack, Nak vai molemmilla
                soketti.responseACKAndNak(paketti, bittivirhe);
                // soketti.responseOnlyACK(paketti,bittivirhe);
                // soketti.responseOnlyNAK(paketti,bittivirhe);

            } catch (IOException e) {
                listening = false;
                System.out.println("catch");
                soketti.close();
                break;
            }
        }
    }
}