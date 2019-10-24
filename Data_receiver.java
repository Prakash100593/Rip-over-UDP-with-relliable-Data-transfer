import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Data_receiver extends Sender3 implements Runnable
{
    /**
     * Receiver Thread will be started for all routers that is not a sender.
     */
    static byte[] receive = new byte[1000];
    static byte[] datarcvdtoSend = new byte[1000];
    static List<Byte> datarsList = new ArrayList<Byte>();
    static List<Byte> Ackdata = new ArrayList<Byte>();
    int PORT_receiver = 1568;
    static int counter = 0;
    public static String INET_ADDR;
    public static File file_output = new File("Output.txt");

    Data_receiver(String address)
    {
        this.INET_ADDR = address;
    }

    //public static void main(String[] args) throws IOException
    public void run() {
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(PORT_receiver);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        DatagramPacket DpReceive = new DatagramPacket(receive, receive.length);
        int expctdsqncnumber = 0;
        boolean finish = false;
        /**
         * The receiver thread will be run till the finish bit is set to 1.
         */
        while (!finish)
        {
            try {
                ds.receive(DpReceive);
                byte [] rcvddata = DpReceive.getData();
                /*if (String.valueOf(getfinishbit(0,rcvddata)).equals("1"))
                {
                    finish = true;
                }*/
                /**
                 * If this router is the destination, it will print the contents of the file and will store
                 * the content in output text file.
                 * Also it will send an acknowledgement for the recevied packet sequence number by switching the
                 * Source port and destination port.
                 */
                if (getprtclDestAdress(0,rcvddata).equals(Sender3.destination))
                {
                    if (getsqncno(0,rcvddata)==expctdsqncnumber + 1) {
                        expctdsqncnumber = expctdsqncnumber + 1;
                    }

                    if (String.valueOf(expctdsqncnumber).equals(String.valueOf(getsqncno(0,rcvddata)))){
                        //System.out.println("Client:-" + data(receive));
                        byte[] recv = new byte[rcvddata.length - 10];
                        int k = 0;
                        for (int i =10; i<rcvddata.length;i++)
                        {

                            recv[k] = rcvddata[i];
                            if (i == rcvddata.length - 2){
                                System.out.println();
                            }
                            k++;
                        }
                        String s = new String(recv);
                        System.out.println(s);
                        FileOutputStream out = new FileOutputStream(file_output,true);
                        out.write(recv);
                        out.close();

                    }
                    String [] srcaddress = getprtclDestAdress(0,rcvddata).split("\\.");

                    Ackdata.add((byte) Integer.parseInt(srcaddress[0]));
                    Ackdata.add((byte) Integer.parseInt(srcaddress[1]));
                    Ackdata.add((byte) Integer.parseInt(srcaddress[2]));
                    Ackdata.add((byte) Integer.parseInt(srcaddress[3]));

                    String [] destddress = getprtclSourceAdress(0,rcvddata).split("\\.");
                    //destnAddress
                    Ackdata.add((byte) Integer.parseInt(destddress[0]));
                    Ackdata.add((byte) Integer.parseInt(destddress[0]));
                    Ackdata.add((byte) Integer.parseInt(destddress[0]));
                    Ackdata.add((byte) Integer.parseInt(destddress[0]));

                    //Ackdata.add((byte) 1);
                    Ackdata.add(rcvddata[8]);

                    //Ackdata.add((byte) 1);
                    Ackdata.add(rcvddata[9]);

                    byte[] datatoSend;
                    datatoSend = new byte[Ackdata.size()];

                    for (int i = 0; i < Ackdata.size(); i++) {
                        datatoSend[i] = Ackdata.get(i);
                    }

                    int destnindex = (int) RoutingTable.Routinginfo.indexOf(String.valueOf(getprtclSourceAdress(0,rcvddata)));
                    int desired_index = destnindex - 2;
                    InetAddress ipack = InetAddress.getLocalHost();
                    ipack = InetAddress.getByName((String) RoutingTable.Routinginfo.get(desired_index));


                    DatagramPacket DprcvSend = new DatagramPacket(datatoSend, datatoSend.length, ipack, 1234);
                    ds.send(DprcvSend);
                    //System.out.println("Acknowledgement sent for sequence no " + expctdsqncnumber);
                    receive = new byte[1000];
                }
                else
                {
                    /**
                     * if the router is not a destination, we will simply forward the packet by looking up for
                     * the next hop address from the destination in routing table info.
                     */
                    if (RoutingTable.Routinginfo.contains(String.valueOf(getprtclDestAdress(0,rcvddata)))) {
                        int destn_index = (int) RoutingTable.Routinginfo.indexOf(String.valueOf(getprtclDestAdress(0,rcvddata)));
                        int desired_index = destn_index - 2;
                        InetAddress ip_new = InetAddress.getLocalHost();
                        ip_new = InetAddress.getByName((String) RoutingTable.Routinginfo.get(desired_index));
                            DatagramPacket DprcvSend = new DatagramPacket(receive, receive.length, ip_new, PORT_receiver);
                            ds.send(DprcvSend);
                            System.out.println("Packet forwarded with sequence number" + expctdsqncnumber);
                            counter = counter + 1;
                            sleep(2000);

                        }
                    else
                    {
                        System.out.println("No route");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static String getprtclDestAdress(int offset,byte[] rcvddata) {
        /**
         * @offset - based on the no of packets, offset value is set and passed.
         *
         * returns Nexthop address of the received IPaddress family entry of the packet.
         */
        String srcAdress = "";
        for (int i =4;i<=7;i++) {
            srcAdress = srcAdress + String.valueOf(BinaryToDecimal(1, offset+i,rcvddata));
            if (i!=7)
                srcAdress = srcAdress + ".";
        }
        //System.out.println("currently read IP address" + srcAdress);
        return srcAdress;
    }

    public static String getprtclSourceAdress(int offset,byte[] rcvddata) {
        /**
         * @offset - based on the no of packets, offset value is set and passed.
         *
         * returns Nexthop address of the received IPaddress family entry of the packet.
         */
        String srcAdress = "";
        for (int i =0;i<=3;i++) {
            srcAdress = srcAdress + String.valueOf(BinaryToDecimal(1, offset+i,rcvddata));
            if (i!=3)
                srcAdress = srcAdress + ".";
        }
        //System.out.println("currently read IP address" + srcAdress);
        return srcAdress;
    }



    public static long BinaryToDecimal(int noOfBytes, int startbyteposn, byte [] rcvddata) {
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
                if ((rcvddata[byteposn] &  (1<<j)) != 0) {
                    finalnumber = finalnumber + (int) Math.pow(2, i);
                }
            }
            byteposn = byteposn +1;
            highestpower = highestpower - 8;
        }
        return finalnumber;
    }


    public static int getsqncno(int offset,byte[] rcvddata) {
        /**
         * @offset - based on the no of packets, offset value is set and passed.
         *
         * returns Roverid address of the received IPaddress family entry of the packet.
         */
        String seqncno = "";
        seqncno = seqncno + String.valueOf(BinaryToDecimal(1, offset + 9,rcvddata));
        return Integer.valueOf(seqncno);
    }

    public static int getfinishbit(int offset,byte[] rcvddata) {
        /**
         * @offset - based on the no of packets, offset value is set and passed.
         *
         * returns Roverid address of the received IPaddress family entry of the packet.
         */
        String finbit = "";
        finbit = finbit + String.valueOf(BinaryToDecimal(1, offset + 8,rcvddata));
        return Integer.valueOf(finbit);
    }

    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 8+2;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

}