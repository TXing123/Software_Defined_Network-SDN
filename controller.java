import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;



class SwitchInfo {
  public int port;
  public InetAddress address;
  public int alive;
  public ArrayList<Integer> connect;//store the neighboring switchID
  public Timer receive_timer;
  SwitchInfo(int ID, int port, InetAddress address, int alive){
    this.port=port;
    this.address=address;
    this.alive=alive;
  }
  SwitchInfo(){
    this.port=-1;
    this.alive=0;
    this.connect=new ArrayList<Integer>();
    this.receive_timer=null;
  }

}

public class controller{
  private static Timer timer;
  private static ArrayList<SwitchInfo> list= new ArrayList<SwitchInfo>();
  private static DatagramSocket datagramSocket = null; 
  private static int M=5;
  private static int K=1000;
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

  
    init();
 
      while(true){  
        try {  
          byte[] buffer = new byte[1024 * 64]; 
          DatagramPacket packet = new DatagramPacket(buffer, buffer.length);  
          datagramSocket.receive(packet);
          new Thread(new packet_handler(packet)).start();  
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
    Timer timer=new Timer();
    timer.schedule(new Host_timer_task(switchID),M*K);
    current_switch.receive_timer=timer;
    System.out.println("switch " + switchID + " is alive");

    for(Integer connectP : current_switch.connect){
      String str;
      if(list.get(connectP-1).alive==1){
        str="REGISTER_RESPONSE "+connectP + " " + "1" + " " + list.get(connectP - 1).address.getHostName() + " " + list.get(connectP - 1).port;
      }
      else{
        str="REGISTER_RESPONSE "+connectP + " " + "0";
      }
      SendString(str,address,port);
    }

    routing_calculate();
  }

  public static void dead_switch(int switchID){
    try{
      SwitchInfo dead=list.get(switchID-1);
      dead.alive=0;
      dead.receive_timer.cancel();
      routing_calculate();
    }catch (Exception e){
      System.err.println("Exception caught in dead_switch:" + e);
    }
  }

  public static void alive_switch(int switchID){
    try{
      SwitchInfo alive=list.get(switchID-1);
      alive.receive_timer.cancel();
      Timer timer=new Timer();
      timer.schedule(new Host_timer_task(switchID),M*K);
      alive.receive_timer=timer;
    }catch (Exception e){
      System.err.println("Exception caught in dead_switch:" + e);
    }
  }

  public static void routing_calculate(){
    System.out.println("Routing table re-calculated");
  }
}

class packet_handler implements Runnable {  
    private DatagramPacket packet;  
    public packet_handler(DatagramPacket packet){  
        this.packet = packet;  
    }  

    public void run() {  
        try {  
          String str = new String(packet.getData()); 
          InetAddress address = packet.getAddress(); 
          int port = packet.getPort(); 
          System.out.println("received: "+ str);

          String[] word = str.split(" ");
          if(word[0].equals("REGISTER_REQUEST")){
            int switchID=Integer.parseInt(word[1].trim());
            controller.register(switchID,address,port);
          }
          else if(word[0].equals("TOPOLOGY_UPDATE")){
            controller.alive_switch(Integer.parseInt(word[1].trim()));
          }
          
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}

class Host_timer_task extends TimerTask { //handle all kinds of timer

  private int switchID;

  Host_timer_task (int switchID){// for type 0
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






