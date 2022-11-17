import java.io.*;
class Server {
    //private static VirtualSocket soketti = null;
    private static ReliabilityLayer reliabilityLayer = null;

    public static void main(String[] args) throws IOException {
        // ReliabilityLayer hoitaa sokettien avaamisen yms.
        reliabilityLayer = new ReliabilityLayer();
        reliabilityLayer.start(6666);
    }
}