package customerservicewsserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

class RoomUser {

    String room;
    WebSocket conn;

    public RoomUser(WebSocket conn, String room) {
        this.conn = conn;
        this.room = room;
    }
}

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class CustomerServiceWsServer extends WebSocketServer {

    static int userNum = 0;
    LinkedList<RoomUser> list = new LinkedList<RoomUser>();
    String nowServerRoom = null;

    public CustomerServiceWsServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public CustomerServiceWsServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        userNum++;
        String userNumString = Integer.toString(userNum);
        RoomUser user = new RoomUser(conn, userNumString);
        list.add(user);
        System.out.println("list[1]============================="+list);
        user.conn.send(user.room);
        //System.out.println("list===="+list.element());
        //System.out.println("nowServerRoomnowServerRoomnowServerRoomnowServer=" + nowServerRoom);
        this.sendToRoom(nowServerRoom, "訪客 " + userNum + " 已連線");//sendto客服人員所在的ROOM
        //this.sendToAll("訪客 "+ userNum + " 已連線");
        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
        System.out.println(conn + "has enter the room!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        RoomUser user = this.findRoomUser(conn);
        list.remove(user);
        System.out.println(list);
        
        //this.sendToAll( conn + " has left the room!" );
        //userNum--;
        //this.sendToRoom(nowServerRoom, "訪客 " + userNum + " 已離開");
        this.sendToRoom(nowServerRoom, "訪客 " + user.room + " 已離開");
        System.out.println(conn + " has left the room!");
        System.out.println("nowServerRoomnowServerRoomnowServerRoom" + nowServerRoom);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        RoomUser user = this.findRoomUser(conn);

        // ROOM:abcd
        System.out.println("========MES==========");
        System.out.println(message);

        if (message.startsWith("server:")) {
            String room = message.substring(7);
            user.room = room;
            nowServerRoom = user.room;//抓客服人員所在的ROOM
            //NowServerRoom(user.room);
            //userNum--;//if客服人員與訪客在同一個房間時避免又+1
            return;
        }
        if (user.room.equals(nowServerRoom)) {
            this.sendToRoom(user.room, message);
        } else {
            this.sendToRoom(user.room, message);
            this.sendToRoom(nowServerRoom, message);
        }

        if (message.equals("客服人員已離開")) {
            this.sendToAll("客服人員已離開");
        }
        //this.sendToAll(message);
        //this.sendToRoom(nowServerRoom, "訪客 "+userNum + " 已離開");

        System.out.println("aaaaaaaaaaaa");
        System.out.println("userRoom:" + user.room + "___" + conn + ": " + message);
        if (message.endsWith("exit")) {
            onClose(conn, 1, "exit", true);//呼叫onClose函數
        }
    }

    public void onFragment(WebSocket conn, Framedata fragment) {
        System.out.println("received fragment: " + fragment);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        WebSocketImpl.DEBUG = true;
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt(args[ 0]);
        } catch (Exception ex) {
        }
        CustomerServiceWsServer s = new CustomerServiceWsServer(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            s.sendToAll(in);
            if (in.equals("exit")) {
                s.stop();
                break;
            } else if (in.equals("restart")) {
                s.stop();
                s.start();
                break;
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     * @throws InterruptedException When socket related I/O errors occur.
     */
    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }

    public void sendToRoom(String room, String text) {
        for (int i = 0; i < list.size(); i++) {
            RoomUser user = list.get(i);
            if (user.room.equals(room)) {

                user.conn.send(text);
            }
        }
    }

    public RoomUser findRoomUser(WebSocket conn) {
        for (int i = 0; i < list.size(); i++) {
            RoomUser user = list.get(i);
            if (user.conn == conn) {
                return user;
            }
        }
        return null;
    }
    /*
     public void NowServerRoom(String nowServerRoom){
     String ServerRoom="";
     ServerRoom = nowServerRoom;
     }
     */
}
