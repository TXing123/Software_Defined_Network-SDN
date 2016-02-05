import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.*;
import java.lang.Math;
 
class Widest_Shortest_Path implements Runnable {
    private int          distances[];
    private int          width[];
    private Set<Integer> settled;
    private Set<Integer> unsettled;
    private int          number_of_nodes;
    private int          adjacencyMatrix[][];
    private int          adjacencyMatrix2[][];
    private int          prev[];
    private int          source;

    public Widest_Shortest_Path (int adjacency_matrix[][], int adjacency_matrix2[][], int number_of_nodes, int source ,HashSet<Integer> dead_nodes){
      this.number_of_nodes= number_of_nodes;
      distances = new int[number_of_nodes + 1];
      width = new int[number_of_nodes + 1];
      prev = new int[number_of_nodes + 1];
      settled = new HashSet<Integer>();
      unsettled = new HashSet<Integer>();
      this.source=source;
      adjacencyMatrix = new int[number_of_nodes + 1][number_of_nodes + 1];
      adjacencyMatrix2 = new int[number_of_nodes + 1][number_of_nodes + 1];
      for (int i = 1; i <= number_of_nodes; i++) 
        for (int j = 1; j <= number_of_nodes; j++) {
          adjacencyMatrix[i][j] = adjacency_matrix[i][j];
          adjacencyMatrix2[i][j] = adjacency_matrix2[i][j];
        }


      for (int i = 1; i <= number_of_nodes; i++) {
        for (int j = 1; j <= number_of_nodes; j++) {
          if (adjacencyMatrix[i][j] == 0 ) {
            adjacencyMatrix[i][j] = Integer.MAX_VALUE;
          }
          if (adjacencyMatrix2[i][j] == 0) {
            adjacencyMatrix2[i][j] = Integer.MAX_VALUE;
          }
        }
      }

      for (int i: dead_nodes){
        for(int j=1;j<number_of_nodes;j++){
          adjacencyMatrix2[i][j]=Integer.MAX_VALUE;
          adjacencyMatrix2[j][i]=Integer.MAX_VALUE;
          adjacencyMatrix[i][j]=Integer.MAX_VALUE;
          adjacencyMatrix[j][i]=Integer.MAX_VALUE;
        }
      }
    }

    public void run()
    {
      int evaluationNode;


      for (int i = 1; i <= number_of_nodes; i++)
      {
        prev[i] = 0;
        width[i] = Integer.MIN_VALUE;
      }

      unsettled.add(source);
      width[source] = Integer.MAX_VALUE;
      distances[source] = 0;
      prev[source] = 0;
      while (!unsettled.isEmpty())
      {
        evaluationNode = getNodeWithMaximumWidthFromUnsettled();
        unsettled.remove(evaluationNode);
        settled.add(evaluationNode);
        evaluateNeighbours(evaluationNode);
      }

      controller.report_routing(source,prev,width,distances);

    }
 
    private int getNodeWithMaximumWidthFromUnsettled()
    {
        int max;
        int node = 0;
 
        Iterator<Integer> iterator = unsettled.iterator();
        node = iterator.next();
        max = width[node];
        for (int i = 1; i <= distances.length; i++)
        {
            if (unsettled.contains(i))
            {
                if (width[i] >  max)
                {
                    max = width[i];
                    node = i;
                    //System.out.println("max = " + max + " node number = " + node);
                }
            }
        }
        return node;
    }
 
    private void evaluateNeighbours(int evaluationNode)
    {
        int edgeWidth = -1;
        int newWidth = -1;
        int edgeDistance = -1;
        int newDistance = -1;
 
        for (int destinationNode = 1; destinationNode <= number_of_nodes; destinationNode++)
        {
            if (!settled.contains(destinationNode))
            {
                if (adjacencyMatrix[evaluationNode][destinationNode] != Integer.MAX_VALUE)
                {
                    edgeWidth = adjacencyMatrix[evaluationNode][destinationNode];
                    newWidth = Math.min(edgeWidth, width[evaluationNode]);
                    edgeDistance = adjacencyMatrix2[evaluationNode][destinationNode];
                    newDistance = distances[evaluationNode] + edgeDistance;
                     
                    if (newWidth > width[destinationNode]) {
                        width[destinationNode] = newWidth; 
                        distances[destinationNode] = newDistance;
                        prev[destinationNode] = evaluationNode;
                    }

                    if (newWidth == width[destinationNode]) {
                        distances[destinationNode] = Math.min(newDistance, distances[destinationNode]);
                        if (newDistance < distances[destinationNode]) {
                          prev[destinationNode] = evaluationNode;
                        }
                    }
                    unsettled.add(destinationNode);
                }
            }
        }
    }

}

class SwitchInfo {
  public int port;
  public InetAddress address;
  public int alive;
  public HashSet<Integer> connect;//store the neighboring switchID
  public HashSet<Integer> alive_neighbor;
  public Timer receive_timer;

  SwitchInfo(){
    this.port=-1;
    this.alive=0;
    this.connect=new HashSet<Integer>();
    this.receive_timer=null;
    this.alive_neighbor=new HashSet<Integer>();
  }

}

public class controller{
  private static Timer timer;
  private static ArrayList<SwitchInfo> list= new ArrayList<SwitchInfo>();
  private static DatagramSocket datagramSocket = null; 
  private static int M=5;
  private static int K=1000;
  private static boolean quiet=true;
  private static int num;
  private static int ori_adjacency_matrix[][];
  private static int ori_adjacency_matrix2[][];
  private static int adjacency_matrix[][];
  private static int adjacency_matrix2[][];
  public static void main (String[] args) {

    // Read topo file and put the result in an ArrayList
    ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
    try {
        BufferedReader in = new BufferedReader(new FileReader("topo_config.txt"));
        String line;
        while((line = in.readLine()) != null)
        {
            ArrayList<Integer> temp = new ArrayList<Integer>();
            for (String element : line.split(" ")) {
                //System.out.println(Integer.parseInt(element));
                temp.add(Integer.parseInt(element));
            }
            result.add(temp);
            
        }
        in.close();
    } catch (IOException e) {
        
    }


    int n=result.get(0).get(0);//the number of swithes
    num=n;
      for(int i = 0; i < n; i++)//initialize
    {
      SwitchInfo node=new SwitchInfo();
      // assign ID number from 1 to 6
      list.add(node);
    }

    // construct SwitchInfo.connect
    for (int i = 1; i < result.size(); i++) {
      
      list.get(result.get(i).get(0) - 1).connect.add(result.get(i).get(1));
      list.get(result.get(i).get(1) - 1).connect.add(result.get(i).get(0));

    }

    ori_adjacency_matrix = new int[num + 1][ num + 1];
    ori_adjacency_matrix2 = new int[num + 1][num + 1];
    adjacency_matrix = new int[num + 1][ num + 1];
    adjacency_matrix2 = new int[num + 1][num + 1];
    for (int i = 1; i < result.size(); i++) {
      ori_adjacency_matrix[result.get(i).get(0)][result.get(i).get(1)] = result.get(i).get(2);
      ori_adjacency_matrix[result.get(i).get(1)][result.get(i).get(0)] = result.get(i).get(2); 
      ori_adjacency_matrix2[result.get(i).get(0)][result.get(i).get(1)] = result.get(i).get(3);
      ori_adjacency_matrix2[result.get(i).get(1)][result.get(i).get(0)] = result.get(i).get(3); 

      adjacency_matrix[result.get(i).get(0)][result.get(i).get(1)] = result.get(i).get(2);
      adjacency_matrix[result.get(i).get(1)][result.get(i).get(0)] = result.get(i).get(2); 
      adjacency_matrix2[result.get(i).get(0)][result.get(i).get(1)] = result.get(i).get(3);
      adjacency_matrix2[result.get(i).get(1)][result.get(i).get(0)] = result.get(i).get(3); 
    }
  
    init();
 
      while(true){  
        try {  
          byte[] buffer = new byte[1024 * 64]; 
          DatagramPacket packet = new DatagramPacket(buffer, buffer.length);  
          datagramSocket.receive(packet);
          new Thread(new packet_handler(packet,quiet)).start();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
      }
  }

  public static void init(){
    try {  
      InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 5000);  
      datagramSocket = new DatagramSocket(socketAddress);  
      datagramSocket.setSoTimeout(1000 * 1000);  
      System.out.println("controller start success!");  
    } catch (Exception e) {  
      datagramSocket = null;  
      System.err.println("controller start fail!");  
      e.printStackTrace();  
    }  
  }  
 

  public static void SendString(String str, InetAddress address, int port){
    try{
      byte[] sendData  = new byte[1024];
      sendData=str.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port); 
      datagramSocket.send(sendPacket);
    }
    catch (SocketException e) {
      System.out.println("send packet fail");
    }
    catch (IOException e){
      System.out.println("send packet fail");
    }
  }

  public static void register(int switchID, InetAddress address, int port){
    SwitchInfo current_switch=list.get(switchID-1);
    current_switch.alive=1;
    current_switch.address=address;
    current_switch.port=port;
    current_switch.alive_neighbor=new HashSet<Integer>();
    Timer timer=new Timer();
    timer.schedule(new Host_timer_task(switchID),M*K);
    current_switch.receive_timer=timer;
    System.out.println("get register request from switch "+ switchID);
    System.out.println("send response to switch "+ switchID);
    for(Integer connectP : current_switch.connect){
      String str;
      if(list.get(connectP-1).alive==1){
        str="REGISTER_RESPONSE "+connectP + " " + "1" + " " + list.get(connectP - 1).address.getHostName() + " " + list.get(connectP - 1).port;
        current_switch.alive_neighbor.add(connectP);
      }
      else{
        str="REGISTER_RESPONSE "+connectP + " " + "0";
      }
      SendString(str,address,port);
    }
    SendString("REGISTER_RESPONSE_END "+ switchID, address,port);
    routing_calculate();
  }

  public static void dead_switch(int switchID){
    try{
      SwitchInfo dead=list.get(switchID-1);
      dead.alive=0;
      dead.receive_timer.cancel();
      dead.alive_neighbor=null;
      routing_calculate();
    }catch (Exception e){
      System.err.println("Exception when switchID= "+ switchID);
      System.err.println("Exception caught in dead_switch:" + e);

    }
  }

  public static void alive_switch(int switchID, String str){
    try{
      SwitchInfo alive=list.get(switchID-1);
      alive.receive_timer.cancel();
      Timer timer=new Timer();
      timer.schedule(new Host_timer_task(switchID),M*K);
      alive.receive_timer=timer;
      
      HashSet<Integer> alive_neighbor = new HashSet<Integer>();
      String[] word = str.split(" ");
      for(int i=2;i<word.length; i++){
        alive_neighbor.add(Integer.parseInt(word[i].trim()));
      }

      if(alive_neighbor!=null && alive.alive_neighbor!=null&&  alive_neighbor.containsAll(alive.alive_neighbor) && alive.alive_neighbor.containsAll(alive_neighbor)){
        return;
      }
      for(Integer i: alive.alive_neighbor){ 
        adjacency_matrix[switchID][i] = 0;
        adjacency_matrix[i][switchID] = 0; 
        adjacency_matrix2[switchID][i]= 0;
        adjacency_matrix2[i][switchID]= 0; 
      }

      for(Integer i : alive_neighbor){
        adjacency_matrix[switchID][i] = ori_adjacency_matrix[switchID][i];
        adjacency_matrix[i][switchID] = ori_adjacency_matrix[i][switchID]; 
        adjacency_matrix2[switchID][i]= ori_adjacency_matrix2[switchID][i];
        adjacency_matrix2[i][switchID]= ori_adjacency_matrix2[i][switchID]; 
      }
      alive.alive_neighbor=alive_neighbor;
      routing_calculate();
    }catch (Exception e){
      System.err.println("Exception when switchID= "+ switchID);
      System.err.println("Exception caught in alive_switch:" + e);
    }
  }

  public static void routing_calculate(){
    HashSet<Integer> dead_nodes=new HashSet<Integer>();
    for(int i=0;i<num;i++){
      if(list.get(i).alive==0){
        dead_nodes.add(i+1);
      }
    }
    
    System.out.println("re-calculating routing table");
    for(int i=0;i<num;i++){
      if(list.get(i).alive==1){
        new Thread(new Widest_Shortest_Path(adjacency_matrix,adjacency_matrix2,num,i+1,dead_nodes)).start();  
      }
    }
    
  }

  public static void report_routing(int source, int[] prev, int [] width, int[] leg){

    for(int i=1;i<=num;i++){
      //dest router width leg
      SendString("NEW_ROUTING_INFO "+i+" "+ prev[i]+ " "+width[i]+" "+leg[i],list.get(source-1).address, list.get(source-1).port);
    }
  }

}

class packet_handler implements Runnable {  
    private DatagramPacket packet; 
    boolean quiet; 
    public packet_handler(DatagramPacket packet, boolean quiet){  
        this.packet = packet;  
        this.quiet=quiet;
    }  

    public void run() {  
        try {  
          String str = new String(packet.getData()); 
          InetAddress address = packet.getAddress(); 
          int port = packet.getPort(); 
          if(!quiet){
            System.out.println("received: "+ str);
          }

          String[] word = str.split(" ");
          if(word[0].equals("REGISTER_REQUEST")){
            int switchID=Integer.parseInt(word[1].trim());
            controller.register(switchID,address,port);
          }
          else if(word[0].equals("TOPOLOGY_UPDATE")){
            controller.alive_switch(Integer.parseInt(word[1].trim()),str);
          }
          
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}

class Host_timer_task extends TimerTask { 

  private int switchID;

  Host_timer_task (int switchID){
    this.switchID=switchID;
  }

    public void run() {
      try {
          System.out.println("switch " + switchID +" is down");
          controller.dead_switch(switchID);
      } catch (Exception e) {
        System.err.println("Exception caught in TimerTask:" + e);
      }
    }
}






