/**
 * Author - Prakash Mishra
 */

import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.*;


public class Sender3 extends Thread {
    /**
     * Sender 3 acts as a main thread.
     * Which starts the receiver thread first.
     *
     * Sender will wait for 5 seconds before sendinf the next update.
     */
    static String INET_ADDR;
    static int PORT;
    static List<Byte> message = new ArrayList<Byte>();
    static ArrayList learnedfrom = new ArrayList();
    static int counter = 0;
    static int myroverid;
    static String destnadress;

    static boolean delete = false;
    static int indexcounter = -4;
    static boolean rvridinRtbl = false;
    static String destination;
    static boolean sender = false;
    //static File file = new File("abc.txt");
    static String file_name;

    public static synchronized void deleterows() {
        /**
         * Deletes rows, if the neighbour does not send packets within time limit.
         * Thus by this function we will aim to delete the Entry from routing table info.
         */
        int index =0;
        for (int i = 0; i < RoutingTable.globallist.size() - 1; i++) {
            rvridinRtbl = false;
            indexcounter = -4;
            if (!learnedfrom.contains(RoutingTable.globallist.get(i))) {

                while (indexcounter < RoutingTable.Routinginfo.size() - 4) {
                    indexcounter = indexcounter + 4;
                    if (RoutingTable.Routinginfo.get(indexcounter).equals(RoutingTable.globallist.get(i))) {
                        rvridinRtbl = true;
                        index = indexcounter;
                        indexcounter = -4;
                        break;
                    }
                }
                if (rvridinRtbl) {
                    //learnedfrom.remove(RoutingTable.globallist.get(i));
                    //int index = RoutingTable.Routinginfo.indexOf(RoutingTable.globallist.get(i));
                    //System.out.println("Trying to delete rover id at index " +index+"with rover id" + RoutingTable.globallist.get(i) );
                    RoutingTable.Routinginfo.remove(index + 3);
                    RoutingTable.Routinginfo.remove(index + 2);
                    RoutingTable.Routinginfo.remove(index + 1);
                    RoutingTable.Routinginfo.remove(index);
                    delete = true;
                } else {
                    delete = false;
                }

            }
            if (delete) {
                System.out.println("After deleting from table ");
                RoutingTable.printRoutingTable();
            }
        }
    }

    public void run()
    {
        Data_transfer tsender = new Data_transfer(INET_ADDR, myroverid, file_name, destination, destnadress);
        Data_receiver treceiver = new Data_receiver(INET_ADDR);
        boolean found = false;

        if (sender)
        {
            while(!found) {
                if (RoutingTable.Routinginfo.contains(destnadress)) {
                    found = true;
                    //Thread tsender = new Thread(new Data_transfer(INET_ADDR, myroverid, file, destination, destnadress));
                    tsender.start();
                } else {
                    System.out.println("Still No route found to destination from sender");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else
        {
            //Thread treceiver = new Thread(new Data_receiver(INET_ADDR));
            treceiver.start();
        }


    }

    public static void main(String[] args) throws UnknownHostException, InterruptedException, IOException {
        //public static void rip_sender(String INET_ADDR , int PORT, int myroverid) throws UnknownHostException {
        /**
         * We will take three arguments as input from command line argument.
         * 1) Multicast IP address
         * 2)Port Number
         * 3) Rover ID
         */

        INET_ADDR = args[0];
        PORT = Integer.parseInt(args[1]);
        myroverid = Integer.parseInt(args[2]);

        if (args.length > 3)
        {
            destnadress = args[3];
            file_name = args[4];
            //File file = new File(file_name);
            //file = new File("C:\\Users\\mishmala\\test7.txt");
            sender = true;
        }
        /**
         * Assigning the Destination address of this rover based on rover id
         */
        //own address destination for others to reach out to me.
        destination = "10."+myroverid+".0.0";
        InetAddress addr = InetAddress.getByName(INET_ADDR);

        try {
            byte[] msgbyte = new byte[2];
            MulticastSocket socket = new MulticastSocket(PORT);
            socket.joinGroup(addr);
            Thread t1 = new Thread(new Receiver3(socket, addr, PORT , myroverid, destination,INET_ADDR));
            t1.start();

            int prakash = 0;
            Sender3 sSender = new Sender3();
            sSender.start();

            /**
             * Creating a message for the first time with its oww info and then updating the message array
             * as per the no of IP addtress family entries received in the packet.
             */
            while (true) {
                int packet_counter = 0;
                if (message.size()!=0)
                {
                    message.clear();
                }
                message.add((byte) 2);
                message.add((byte) 2);
                message.add((byte) 0);
                message.add((byte) 0);

                message.add((byte) 0);
                message.add((byte) 2);
                message.add((byte) 0);
                message.add((byte) 0);

                String [ ] Multicastarray  = INET_ADDR.split("\\.");
                message.add((byte) 10);
                message.add((byte) myroverid);
                message.add((byte) 0);
                message.add((byte) 0);

                message.add((byte) 255);
                message.add((byte) 0);
                message.add((byte) 0);
                message.add((byte) 0);

                String [] nextHop1 = String.valueOf(InetAddress.getLocalHost().getHostAddress()).split("\\.");

                message.add((byte) Integer.parseInt(nextHop1[0]));
                message.add((byte) Integer.parseInt(nextHop1[1]));
                message.add((byte) Integer.parseInt(nextHop1[2]));
                message.add((byte) Integer.parseInt(nextHop1[3]));

                message.add((byte) 0);
                message.add((byte) 0);
                message.add((byte) 0);

                message.add((byte) 0);

                if (RoutingTable.Routinginfo.size() > 4) {
                    for (int i = 1; i <= RoutingTable.Routinginfo.size() / 4 - 1; i++) {
                        /**
                         * For each packet received we will append the entry in the next message to be sent.
                         * We will fetch the destination address, nexthop address and Metric cost from the routing table.
                         */
                        int byteposn = i*4;
                        packet_counter = packet_counter + 1;
                        message.set(3,(byte)packet_counter);

                        message.add((byte) 0);
                        message.add((byte) 2);
                        message.add((byte) 0);
                        message.add((byte) 0);

                        String ipaddress = (String) RoutingTable.Routinginfo.get(byteposn + 3);
                        String[] iparray = ipaddress.split("\\.");


                        message.add((byte) Integer.parseInt(iparray[0]));
                        message.add((byte) Integer.parseInt(iparray[1]));
                        message.add((byte) Integer.parseInt(iparray[2]));
                        message.add((byte) Integer.parseInt(iparray[3]));

                        message.add((byte) 255);
                        message.add((byte) 255);
                        message.add((byte) 255);
                        message.add((byte) 252);

                        String nextHop = (String) RoutingTable.Routinginfo.get(byteposn+1);
                        String[] nextHoparray = nextHop.split("\\.");

                        message.add((byte) Integer.parseInt(nextHoparray[0]));
                        message.add((byte) Integer.parseInt(nextHoparray[1]));
                        message.add((byte) Integer.parseInt(nextHoparray[2]));
                        message.add((byte) Integer.parseInt(nextHoparray[3]));

                        message.add((byte) 0);
                        message.add((byte) 0);
                        message.add((byte) 0);

                        String metric = String.valueOf(RoutingTable.Routinginfo.get(byteposn + 2));
                        message.add((byte) Integer.parseInt(metric));

                    }
                }
                byte[] messagearray;
                messagearray = new byte[message.size()];

                for (int i = 0; i < message.size(); i++) {
                    messagearray[i] = message.get(i);
                }

                int counter = 0;


                DatagramPacket msgPacket = new DatagramPacket(messagearray, messagearray.length, addr, PORT);

                socket.send(msgPacket);
                counter++;
                prakash++;
                learnedfrom.clear();
                Thread.sleep(7000);
                learnedfrom.add(myroverid);
                deleterows();


            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Receiver3 implements Runnable {

    static String INET_ADDR;
    static int PORT;
    String msg;
    static byte[] buf = new byte[256];
    InetAddress address;
    MulticastSocket clientSocket;



    Receiver3(MulticastSocket socket, InetAddress addr, int PORT , int myroverid,String destination,String Inet_ADDr) throws UnknownHostException {

        this.clientSocket = socket;
        this.address = addr;
        this.PORT = PORT;
        new RoutingTable(myroverid,String.valueOf(InetAddress.getLocalHost().getHostAddress()),0,destination,0);
        System.out.println("Inital Routing table for this server with its own information - ");
        RoutingTable.printRoutingTable();
        Sender3.learnedfrom.add(myroverid);
        this.INET_ADDR = Inet_ADDr;
    }

    @Override
    public void run() {
        /**
         * Overrides the run method.
         * Receives the packet sent to multicast address and decodes the packet.
         */
        try (MulticastSocket clientSocket = new MulticastSocket(PORT)){
            clientSocket.joinGroup(address);
            //Thread.sleep(100);

            while (true) {
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(msgPacket);
                /**
                 * If my next Hop address is same as packet source address, it specifies that to reach destination it has to come to self
                 * to avoid the same, we can ignore the packet details and  not update the routing table.
                 */
                if (!(String.valueOf(msgPacket.getAddress()).substring(1).equals(String.valueOf(InetAddress.getLocalHost().getHostAddress())))) {
                    String packetadress = String.valueOf(msgPacket.getAddress());

                    int noofpackets = getnoofpackets();

                    int packetoffset = 0;

                    for (int kk = 0; kk <= noofpackets; kk++) {


                        packetoffset = kk * 20;
                        if (!(getNextHop(packetoffset).equals(String.valueOf(InetAddress.getLocalHost().getHostAddress())))) {

                            /**
                             * If the message is from self, do not decode the packet or else continue with decoding.
                             */
                            if (getRoverid(packetoffset) != Sender3.myroverid) {
                                String msg = new String(buf, 0, buf.length);

                                String iptofind = getIpDestAdress(packetoffset);

                                /**
                                 Not from self and cost is 0 - implies that this is the neighbour.
                                 if the message is from neighbour update routingtable with cost 1.
                                 **/
                                if (getMetric(packetoffset) == 0) {
                                    if (!(RoutingTable.Srcrvrid.containsKey(getRoverid(packetoffset)))) {
                                        RoutingTable.Srcrvrid.put(String.valueOf(msgPacket.getAddress()).substring(1), getRoverid(packetoffset));
                                    }
                                    if (!(Sender3.learnedfrom.contains(getRoverid(packetoffset)))) {
                                        Sender3.learnedfrom.add(getRoverid(packetoffset));

                                    }
                                    if (!(RoutingTable.Routinginfo.contains(iptofind))) {
                                        new RoutingTable(getRoverid(packetoffset), packetadress.substring(1), 1, getIpDestAdress(packetoffset));
                                        System.out.println("Added neighbour in routing table");
                                        RoutingTable.printRoutingTable();
                                        //System.out.println(RoutingTable.Routinginfo);
                                    }
                                    else if (RoutingTable.Routinginfo.contains(iptofind))
                                    {
                                        int destnindex = RoutingTable.Routinginfo.indexOf(iptofind);
                                        RoutingTable.Routinginfo.remove(destnindex);
                                        RoutingTable.Routinginfo.remove(destnindex-1);
                                        RoutingTable.Routinginfo.remove(destnindex-2);
                                        RoutingTable.Routinginfo.remove(destnindex-3);

                                        new RoutingTable(getRoverid(packetoffset), packetadress.substring(1), 1, getIpDestAdress(packetoffset));
                                        System.out.println("Updated neighbour information in routing table");
                                        RoutingTable.printRoutingTable();
                                    }
                                } else {
                                    /**
                                     * If the message is not for neighbour, check if we already have the entry for destination address
                                     * in my Routing table, in this case check the cost with the existing metric(destination reachable metric) with
                                     * Source reachable metric + newly received metric.
                                     *
                                     * Only if the updated metric is less, update the routing table or else do not enter the info in routing table.
                                     */
                                    int sourcemetric = 1;
                                    if (RoutingTable.Routinginfo.contains(String.valueOf(msgPacket.getAddress()).substring(1))) {
                                        int sourceroverid = RoutingTable.Srcrvrid.get(String.valueOf(msgPacket.getAddress()).substring(1));
                                        int sourcemetricindex = (int) RoutingTable.Routinginfo.indexOf(sourceroverid);
                                        sourcemetric = (int) RoutingTable.Routinginfo.get(sourcemetricindex + 2);
                                    }
                                    if (RoutingTable.Routinginfo.contains(iptofind)) {
                                        int indexvalue = RoutingTable.Routinginfo.indexOf(getIpDestAdress(packetoffset));
                                        int existingmetric = (int) RoutingTable.Routinginfo.get(indexvalue - 1);


                                        int updatedmetric = getMetric(packetoffset) + sourcemetric;

                                        if (updatedmetric < existingmetric && existingmetric != 16) {
                                            RoutingTable.updateRoutingTable(indexvalue, getRoverid(packetoffset), packetadress.substring(1), updatedmetric, getIpDestAdress(packetoffset));
                                            System.out.println("Updated Routing table for this rover is - ");
                                            RoutingTable.printRoutingTable();
                                        }

                                    } else {
                                        /**
                                         * If Destination address is not present in Routing table, add a new entry for the same
                                         */
                                        new RoutingTable(getRoverid(packetoffset), packetadress.substring(1), getMetric(packetoffset) + sourcemetric, getIpDestAdress(packetoffset));
                                        System.out.println("New entry in routing table - ");
                                        RoutingTable.printRoutingTable();
                                    }


                                }

                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }



    public static int getMetric(int offset) {
        /**
         * @offset - based on the no of packets, offset value is set and passed.
         *
         * returns Metric cosr address of the received IPaddress family entry of the packet.
         */
        String Metric = "";
        Metric = Metric + String.valueOf(BinaryToDecimal(1, offset + 23));
        return Integer.valueOf(Metric);
    }

    public static int getRoverid(int offset) {
        /**
         * @offset - based on the no of packets, offset value is set and passed.
         *
         * returns Roverid address of the received IPaddress family entry of the packet.
         */
        String roverid = "";
        roverid = roverid + String.valueOf(BinaryToDecimal(1, offset + 9));
        return Integer.valueOf(roverid);
    }




    public static int getnoofpackets() {
        /**
         *
         * returns No of packets based on the no of IP address family Entries in the packet.
         */
        String noofpackets = "";
        noofpackets = noofpackets + String.valueOf(BinaryToDecimal(1,  3));
        return Integer.valueOf(noofpackets);
    }



    public static String getIpDestAdress(int offset) {
        /**
         * @offset - based on the no of packets, offset value is set and passed.
         *
         * returns Nexthop address of the received IPaddress family entry of the packet.
         */
        String srcAdress = "";
        for (int i =8;i<=11;i++) {
            srcAdress = srcAdress + String.valueOf(BinaryToDecimal(1, offset+i));
            if (i!=11)
                srcAdress = srcAdress + ".";
        }
        //System.out.println("currently read IP address" + srcAdress);
        return srcAdress;
    }

    public static String getNextHop(int offset) {
        /**
         * @offset - based on the no of packets, offset value is set and passed.
         *
         * returns Nexthop address of the received IPaddress family entry of the packet.
         */
        String nextHop = "";
        for (int i =16;i<=19;i++) {
            nextHop = nextHop + String.valueOf(BinaryToDecimal(1, offset+i));
            if (i!=19)
                nextHop = nextHop + ".";
        }

        return nextHop;
    }


    public static long BinaryToDecimal(int noOfBytes, int startbyteposn) {
        /**
         * Converts the Binary format to decimal format.
         * @parameters : - noofBytes and Start byte position.
         *
         * depending on 4/8/16/32/.. bits to be converted , noofBytes to
         * be passed.
         * From starting Byte position of the byte adress we will read the
         * bits and multiply it with respective power of 2 to get the actual
         * decimal value of that particular bit.
         *
         * This is a generalized method to convert binary to decimal format
         * wherever needed.
         */

        int byteposn = startbyteposn;
        long finalnumber = 0;
        int highestpower = (noOfBytes * (int) Math.pow(2, 3)) - 1;

        for (int jj = noOfBytes; jj >= 1; jj--) {
            for (int i = highestpower,j=7; i >= highestpower-7; i--,j--) {
                if ((buf[byteposn] &  (1<<j)) != 0) {
                    finalnumber = finalnumber + (int) Math.pow(2, i);
                }
            }
            byteposn = byteposn +1;
            highestpower = highestpower - 8;
        }
        return finalnumber;
    }



}

