import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
/**
To send the UDP data across routers.
This thread is started after the RIP sender and receiver is started.
 The sender thread will be started , only for the router which acts as a sener.
 For rest only the receiver thread will be started.
*/
public class Data_transfer extends Sender3 implements Runnable {
    public static String INET_ADDR;
    public static int PORT_transfer;
    public static int myroverid;
    String filepath = null;
    static byte[] readBytes;
    static List<Byte> dataList = new ArrayList<Byte>();
    static String ultimate_destn;
    static String source;
    File file = new File("");
    Data_transfer(String INET_ADDR , int myroverid, String file1, String source, String destination)
    {
        /**
         * Initilazies the constructor with the myroverid, file path, destination address and source address
         */
        this.INET_ADDR = INET_ADDR;
        this.myroverid = myroverid;
        if (file1!="") {
            this.file = new File(file1);
        }
        this.ultimate_destn = destination;
        this.source = source;
    }

    public void createHeader(String [] srcaddress,String [] deddress, int sqnc)
    {
        /**
         * Creates custom Protocol header with Senders address, receivers address, ACK flag and Sequence no.
         */
        dataList.add((byte) Integer.parseInt(srcaddress[0]));
        dataList.add((byte) Integer.parseInt(srcaddress[1]));
        dataList.add((byte) Integer.parseInt(srcaddress[2]));
        dataList.add((byte) Integer.parseInt(srcaddress[3]));
        //destnAddress
        dataList.add((byte) Integer.parseInt(deddress[0]));
        dataList.add((byte) Integer.parseInt(deddress[1]));
        dataList.add((byte) Integer.parseInt(deddress[2]));
        dataList.add((byte) Integer.parseInt(deddress[3]));

        //dataList.add((byte) 0);
        //Finish flag bit
        dataList.add((byte) 0);

        //dataList.add((byte) 1);
        //Sequence number bit
        dataList.add((byte) sqnc);

    }



    public void run() {
        /**\
         * This thread is started after the RIP sender and Receiver started
         */
        FileInputStream fin = null;
        int len=0;
        Sender3 s3 = new Sender3();

        PORT_transfer = 1234;

        byte fileContent[] = new byte[948];
        try {
            DatagramSocket ds = new DatagramSocket(PORT_transfer);
            /**
             * Fints the destination address in routing table info.
             */
            int srcindex = RoutingTable.Routinginfo.indexOf(ultimate_destn);
            int desiredindex =  srcindex -2;

            InetAddress ip = InetAddress.getLocalHost();
            ip = InetAddress.getByName(String.valueOf( RoutingTable.Routinginfo.get(desiredindex)));
            //ip = InetAddress.getByName("10.180.57.119");
            fin = new FileInputStream(file);

            InetAddress addr = InetAddress.getByName(INET_ADDR);
            //src Address
            String [] srcaddress = Sender3.destination.split("\\.");
            String [] deddress = Sender3.destnadress.split("\\.");
            try {
                fin = new FileInputStream(file);
                byte[] datarcvd;
                datarcvd = new byte[dataList.size()];
                int squncno = 0;
                long noofpackets = ( (file.length()/948)) + 1;
                //while(true) {
                    while ((len = fin.read()) != -1) {
                        squncno = squncno + 1;
                        java.util.Arrays.fill(fileContent, ( byte) 0);
                        createHeader(srcaddress,deddress,squncno);

                        /**
                         * In case of last packet, the finish bit is set to 1.
                         */
                        if (squncno == noofpackets)
                        {
                            dataList.set(8,(byte) 1);
                        }

                        fin.read(fileContent);
                        /**
                         * Adds the complete file contents to the data list array with the custom protocol header.
                         */
                        for (byte b : fileContent) {
                            dataList.add(b);
                        }

                        byte[] datatoSend;
                        datatoSend = new byte[dataList.size()];
                        /**
                         * Creates a byte array to be sent to the receiver, with custom protocol header
                         * data from file content.
                         */
                        for (int i = 0; i < dataList.size(); i++) {
                            datatoSend[i] = dataList.get(i);
                        }
                        String s = new String(datatoSend);
                        System.out.println("File content: at senders end " + s);

                        DatagramPacket DpSend = new DatagramPacket(datatoSend, datatoSend.length, ip, 1568);
                        ds.send(DpSend);
                        System.out.println("PAcket sent with sequence number" + squncno);
                        DatagramPacket received = new DatagramPacket(datarcvd, datarcvd.length);

                        ds.setSoTimeout(2000);
                        boolean timedout = true;
                        /**
                         * Set a timeout value to 2 seconds, if the sender is not receiving back
                         * acknowledement from the receiver within this timeout period, it will again resend
                         * the packet and will wait for acknowledgement.
                         */

                        while(timedout) {
                            try {
                                ds.receive(received);
                                String s1 = new String(String.valueOf(received));
                                System.out.println("FROM SERVER: ACK received for sequence no " + squncno);
                                dataList.clear();
                                timedout = false;
                            } catch (SocketTimeoutException e) {
                                ds.send(DpSend);
                            }
                        }
                    }
                //}

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        }
        finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
    }
}
