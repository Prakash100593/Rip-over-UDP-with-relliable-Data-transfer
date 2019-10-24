import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RoutingTable {
    /**
     * It initiliazes Routing table for each rover and add the entries in routing table for each of the rover entry.
     */
    public static ArrayList Routinginfo;

    public String NextHopRouterId;
    public String destaddr;
    public int hopcount;
    private int MyRouterId;
    public static ArrayList globallist = new ArrayList(Arrays.asList(1,2,3,4,5,6,7,8,9,10));
    //static ArrayList globallist = new ArrayList();
    static Map<String, Integer> Srcrvrid = new HashMap<String , Integer>();

    public RoutingTable(int id,String nextHopRouterId,
                        int linkCost, String destRouterId,int start) {
/**
 * Constructor of Routing table for the rover id starting for some time and adding the self entry
 * for its routing table
 */
        NextHopRouterId = nextHopRouterId;
        destaddr = destRouterId;
        hopcount = linkCost;
        MyRouterId = id;
        Routinginfo = new ArrayList();

        Routinginfo.add(MyRouterId);
        Routinginfo.add(NextHopRouterId);
        Routinginfo.add(hopcount);
        Routinginfo.add(destaddr);
    }

    public RoutingTable(int id,String nextHopRouterId,
                        int linkCost, String destRouterId) {

        /**
         * For every new entry in the routing table except self.
         */
        NextHopRouterId = nextHopRouterId;
        destaddr = destRouterId;
        hopcount = linkCost;
        MyRouterId = id;

        Routinginfo.add(MyRouterId);
        Routinginfo.add(NextHopRouterId);
        Routinginfo.add(hopcount);
        Routinginfo.add(destaddr);

        try {
            Srcrvrid.put(String.valueOf(InetAddress.getLocalHost().getHostAddress()),MyRouterId);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }




    public int getMyRouterId() {
        return MyRouterId;
    }




    public synchronized ArrayList getRows() {
        return Routinginfo;
    }

    public static void updateRoutingTable(int index,int id,String nextHopRouterId,
                                          int linkCost, String destRouterId)
    /**
     * Updating the routing table as per the update received from the receiver.
     */
    {
        //int idtoupdate = (int) Routinginfo.indexOf(int index,);
        Routinginfo.set(index-3, id);
        Routinginfo.set(index -2 , nextHopRouterId);
        Routinginfo.set(index -1, linkCost);
        Routinginfo.set(index , destRouterId);
    }

    public static void printRoutingTable() {
        System.out.println("NextHop Address        \t    Metric        \t DestinationAddress");
        for (int j =0;j < RoutingTable.Routinginfo.size();j=j+4)
        {
            System.out.println("" + RoutingTable.Routinginfo.get(j+1) + "\t\t\t\t" + RoutingTable.Routinginfo.get(j+2) + "\t\t\t\t\t\t" + RoutingTable.Routinginfo.get(j+3));

        }
        System.out.println();

    }



}
