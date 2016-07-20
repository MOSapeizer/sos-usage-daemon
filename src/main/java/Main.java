import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zil on 2016/7/19.
 */
public class Main {

    private static Logger _log = Logger.getLogger( Main.class.getName() );

    public static PcapNetworkInterface selectNetworkInterface(){
        try {
            return new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            _log.log(Level.WARNING, "Can't find network card");
            return null;
        }
    }

    public static void main(String[] args) throws PcapNativeException {

        System.out.println("Count: " + Usage.getCount());
        System.out.println("Time out: " + Usage.getTimeout());
        System.out.println("Snap length: " + Usage.getSnaplen());

        PcapNetworkInterface nif = selectNetworkInterface();

        if( nif == null )
            return;

        System.out.println(nif.getName() + "(" + nif.getDescription() + ")");
        Usage usage = new Usage(nif);
        try {
            usage.listen();
        } catch (PcapNativeException ignored) {
        } catch (InterruptedException ignored) {
        } catch (NotOpenException e) {
            usage.close();
        }

    }
}
