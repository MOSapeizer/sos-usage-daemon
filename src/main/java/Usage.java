import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import packet.HttpPacket;
import packet.HttpParser;

import java.io.IOException;
import java.net.Inet4Address;
import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zil on 2016/7/13.
 */
public class Usage {

    private static final Logger _log= Logger.getLogger( Usage.class.getName() );
    private static final int COUNT = 1000;
    private static final int READ_TIMEOUT = 10; // [ms]
    private static final int SNAPLEN = 65535; // [bytes]
    private static DBManager conn = DBManager.getInstance();
    private final PcapHandle handle;
    private String filterPort;
    private PacketListener listener;
    private byte[] httpRawBytes;

    public static int getCount(){
        return COUNT;
    }

    public static int getTimeout(){
        return READ_TIMEOUT;
    }

    public static int getSnaplen(){
        return SNAPLEN;
    }

    public Usage(PcapNetworkInterface nif) throws PcapNativeException {
        conn.init();
        this.handle = nif.openLive(SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
        this.filterPort = "port 8080"; // default for http;
        bindFilter();
    }

    public void setFilterPort(String port){
        this.filterPort = "port " + port;
        bindFilter();
    }

    public void listen() throws PcapNativeException, InterruptedException, NotOpenException {
        createPacketListener();
        handle.loop(COUNT, listener);
    }

    public void close(){
        handle.close();
    }

    private void bindFilter() {
        try {
            handle.setFilter( filterPort, BpfProgram.BpfCompileMode.OPTIMIZE );
        } catch (PcapNativeException e) {
            _log.log(Level.OFF, "Filter can't properly set.");
        } catch (NotOpenException e) {
            _log.log(Level.OFF, "Network card not open.");
        }
    }

    private void createPacketListener(){
        this.listener = new PacketListener() {
            public void gotPacket(Packet packet) {
                IpV4Packet ip = parseIpv4Packet(packet);
                TcpPacket tcp = parseTcpPacket(ip);
                String[] httpRequest = filterHttpRequest(tcp);
                Inet4Address srcAddr = ip.getHeader().getSrcAddr();
                System.out.println("connection: " + srcAddr.getHostAddress() + " " + httpRequest[0] + " " + httpRequest[1] );
                if( isSosRequest(httpRequest) ){
                    conn.write(srcAddr.getHostAddress(), httpRequest);
                }else{
                    System.out.println("Other connection: " + srcAddr.getHostAddress() + " " + httpRequest[0] + " " + httpRequest[1] );
                }
            }
        };
    }

    private boolean isSosRequest(String[] request) {
        if( request[0].equals("GET") || request[0].equals("POST") )
            return request[1].matches("(^/\\w+/service(\\?\\w+)?$)");
        return false;
    }

    private EthernetPacket parseEthernetPacket(Packet packet){
        EthernetPacket ethernetPacket = null;
        try {
            if( packet != null )
                ethernetPacket = EthernetPacket.newPacket(packet.getRawData(), 0, packet.length());
        } catch (IllegalRawDataException e) {
            _log.log(Level.WARNING, "can't read the Ethernet raw data." + packet);
        }
        return ethernetPacket;
    }

    private TcpPacket parseTcpPacket(Packet packet) {
        TcpPacket tcp = null;

        if( packet == null )
            return null;

        int length = packet.getHeader().length();
        try {
            tcp = TcpPacket.newPacket(packet.getRawData(), length, packet.length() - length);
        } catch (IllegalRawDataException e) {
            _log.log(Level.WARNING, "can't read the tcp raw data." + packet);
        }
        return tcp;
    }

    private IpV4Packet parseIpv4Packet(Packet packet) {
        IpV4Packet ipv4packet = null;

        if( packet == null )
            return null;

        int length = packet.getHeader().length();
        try {
            ipv4packet = IpV4Packet.newPacket(packet.getRawData(), length, packet.length()- length );
        } catch (IllegalRawDataException e) {
            _log.log(Level.WARNING, "can't read the ip raw data." + packet );
        }
        return ipv4packet;
    }

    private String[] filterHttpRequest(TcpPacket tcpPacket){
        String[] request = new String[2];
        this.httpRawBytes = Arrays.copyOfRange(tcpPacket.getRawData(), tcpPacket.getHeader().length(), tcpPacket.length());
        if( httpRawBytes.length > 0 ){
            HttpParser httpParser = initHttpParser();
            Boolean isHttp = filterMethodGetAndPost( httpParser);
            if( isHttp ){
                request[0] = httpParser.getMethod();
                request[1] = httpParser.getRequestURL();
            }
        }
        return request;
    }

    private Boolean filterMethodGetAndPost( HttpParser httpParser){
        return httpParser.getMethod().equals("GET") || httpParser.getMethod().equals("POST");
    }

    private HttpParser initHttpParser(){
        HttpPacket httpPacket  = parseHttpPacket();
        HttpParser httpParser = httpPacket.getHttpParser();
        try {
            httpParser.parseRequest();
        } catch (IOException e) {
            _log.log(Level.WARNING, "can't read the http payload.");
        }
        return httpParser;
    }

    private HttpPacket parseHttpPacket() {
        HttpPacket httpPacket = null;
        try {
            httpPacket = HttpPacket.newPacket(httpRawBytes, 0, httpRawBytes.length);
        } catch (IllegalRawDataException e) {
            _log.log(Level.WARNING, "can't read the http raw data.");
        }
        return httpPacket;
    }

}
