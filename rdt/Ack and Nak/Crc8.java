import java.util.Arrays;
public class Crc8 {
     /**
     * @param data tarkistettava
     * @return true jos virhe
     */
    public static boolean onBittivirhe(byte[] data) {
        byte[] viesti = Arrays.copyOfRange(data, 0, data.length-1);
        byte viestinCrc8 = data[data.length-1];
        byte crc8 = generoiCrc8(viesti);
        return crc8 != viestinCrc8;
    }

    /**
     * @param rec data, josta generoidaan crc8
     * @return crc8
     */
    public static byte generoiCrc8(byte[] data) {
        int rekisteri = 0;
        int maski[] = {0x01,0x02,0x04,0x08,0x10,0x20,0x40,0x80};
        for (int i=0;i<data.length;i++) {
            byte tavu = data[i];
            
            // Loppuun vielä 8 kappaletta 0-bittejä
            if (i == data.length-1) {
                tavu = 0;
            } 

            for (int j=7; j >= 0;j--) {

                int uusiPositio0 = (((tavu & maski[j]) >>> j)^(rekisteri & 0x80) >>> 7);
                int uusiPositio1 = (((rekisteri & 0x01))^(rekisteri & 0x80) >>> 7);
                int uusiPositio2 = (((rekisteri &  0x02) >>> 1)^(rekisteri & 0x80) >>> 7);
                
                
                rekisteri = (rekisteri << 1);
                
                uusiPositio2 = (uusiPositio2 << 2);
                rekisteri = (rekisteri & ~0x04) | uusiPositio2;
                
                uusiPositio1 = (uusiPositio1 << 1);
                rekisteri = (rekisteri & ~0x02) | uusiPositio1;
                
                rekisteri = (rekisteri & ~0x01) | uusiPositio0;
            }

        }
        rekisteri = rekisteri & 0xFF;

        return (byte) rekisteri;
    }
}
