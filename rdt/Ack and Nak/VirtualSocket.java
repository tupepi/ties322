import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class VirtualSocket extends DatagramSocket {
    private static double p_drop = 0; // propability of dropped packet
    private static double p_delay = 0; // propability of delayed packet
    private static double p_biterror = 0.7; // propability of errored bit packet

    public VirtualSocket() throws SocketException {
        super();
    }

    public VirtualSocket(int portti) throws SocketException {
        super(portti);
    }

    public void close() {
        super.close();
    }

    /**
     * 
     * @param paketti    käytetään olemassa olevaa pakettia, jota muokataan
     * @param bittivirhe jos virhe lähetetään nack jos ei niin ack
     */
    public void responseACKAndNak(DatagramPacket paketti, boolean bittivirhe) throws IOException {
        String ACK = "0ACK1";
        byte[] ack_bytes = ACK.getBytes();

        if (bittivirhe) {
            ACK = "0NAK1";
            ack_bytes = ACK.getBytes();
        }
        ack_bytes[0] = paketti.getData()[0];
        ack_bytes[4] = Crc8.generoiCrc8(Arrays.copyOf(ack_bytes, 4));
        paketti.setData(ack_bytes);
        super.send(paketti);
    }

    /**
     * 
     * @param paketti    käytetään olemassa olevaa pakettia, jota muokataan
     * @param bittivirhe Muutetaan ackin sekvenssinumeroa jos on bittivirhe
     */
    public void responseOnlyACK(DatagramPacket paketti, boolean bittivirhe) throws IOException {
        String ACK = "0ACK1";
        byte[] ack_bytes = ACK.getBytes();
        ack_bytes[0] = paketti.getData()[0];

        // Muutetaan sekvenssinumero jos on bittivirhe
        if (bittivirhe) {
            ack_bytes[0] = (byte) (ack_bytes[0] ^ 0x01);
        }

        ack_bytes[4] = Crc8.generoiCrc8(Arrays.copyOf(ack_bytes, 4));

        paketti.setData(ack_bytes);
        super.send(paketti);
    }

    /**
     * 
     * @param paketti    käytetään olemassa olevaa pakettia, jota muokataan
     * @param bittivirhe Lähetetään Nak jos on bittivirhe muutoin ei mitään
     */
    public void responseOnlyNAK(DatagramPacket paketti, boolean bittivirhe) throws IOException {
        String ACK = "0NAK1";
        byte[] ack_bytes = ACK.getBytes();
        ack_bytes[0] = paketti.getData()[0];
        ack_bytes[4] = Crc8.generoiCrc8(Arrays.copyOf(ack_bytes, 4));

        paketti.setData(ack_bytes);
        if (bittivirhe) {
            super.send(paketti);
        }
    }

    /*
     * Vastaanotetaan paketti ja aiheutetaan viivettä,
     * bittivirhe tai pudotetaan paketti kokonaan
     */
    public void receive(DatagramPacket packet) throws IOException {
        while (true) {
            Random randGen = new Random();
            super.receive(packet);
            if (randGen.nextDouble() <= p_drop) {
                System.out.println("Dropped packet");
            } else if (randGen.nextDouble() <= p_delay) {
                // https://www.geeksforgeeks.org/timeunit-sleep-method-in-java-with-examples/
                TimeUnit time = TimeUnit.SECONDS;
                try {
                    time.sleep(5);
                    return;
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            } else if (randGen.nextDouble() <= p_biterror) {
                if (packet.getLength() <= 0)
                    return;
                byte[] rekisteri = packet.getData();
                int maskit[] = { 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80 };
                int maski = maskit[randGen.ints(0, 8).findFirst().getAsInt()]; // valitaan satunnainen maski
                /*
                 * Käännettään satunnaisesta tavusta satunnainen bitti
                 * 
                 * 10011011 10011010
                 * 00000001 00000001
                 * --------XOR --------XOR
                 * 10011010 10011011
                 */
                int tavu = randGen.ints(0, packet.getLength()).findFirst().getAsInt();
                int errorRekisteri = (rekisteri[tavu] ^ maski);
                rekisteri[tavu] = (byte) errorRekisteri;
                packet.setData(rekisteri);
                return;
            } else {
                return;
            }
        }
    }
}