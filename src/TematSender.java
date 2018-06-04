import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Vector;

public class TematSender {
    private Vector<Temat> tematy;
    private SocketChannel writeChannel;
    private ServerSocketChannel major_channel;
    private InetSocketAddress serwer_address;
    private Selector ogarniam;
    TematSenderGUI gui;

    TematSender(InetSocketAddress my_addr, InetSocketAddress serwer_addr) throws IOException {
        tematy = new Vector<>();
        serwer_address = serwer_addr;

        ogarniam = Selector.open();
        major_channel = ServerSocketChannel.open();
        major_channel.bind(my_addr);
        major_channel.configureBlocking(false);

        major_channel.register(ogarniam, SelectionKey.OP_ACCEPT);
    }
    void loop() throws IOException {
        ByteBuffer received = ByteBuffer.allocate(4096);
        int res = 0;
        String[] dane;

        ogarniam.selectNow();
        for(SelectionKey key : ogarniam.selectedKeys()){
            if(key.isAcceptable()) {
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                SocketChannel read_channel = channel.accept();
                if (read_channel != null) {
                    read_channel.configureBlocking(false);
                    read_channel.register(ogarniam, SelectionKey.OP_READ);
                }
                key.cancel();
                continue;
            }
            if(key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                received.clear();
                res = channel.read(received);
                dane = Serializer.getData(received.array(), res);
                if (dane[0].equals("add")) {
                    dodajTemat(dane[1], dane[2]);
                    gui.listModel.addElement(tematy.get(tematy.size()-1).nazwa);
                } else if (dane[0].equals("del")) {
                    usunTemat(dane[1]);
                    for (int i = 0; i < gui.listModel.getSize(); i++) {
                        if(gui.listModel.get(i).equals(dane[1])) {
                            gui.listModel.remove(i);
                        }
                    }
                }/* else if(dane[0].equals("want")) {
                    String[] stare_tematy;

                    SocketChannel chan = SocketChannel.open(new InetSocketAddress(dane[1], Integer.parseInt(dane[2])));
                    chan.configureBlocking(true);
                    for(Temat t : tematy){
                        stare_tematy = new String[]{"notify", t.nazwa, t.opis};
                        chan.write(ByteBuffer.wrap(String.join(":",stare_tematy).getBytes()));
                    }

                }*/
                key.cancel();
                channel.close();
                major_channel.register(ogarniam, SelectionKey.OP_ACCEPT);
                continue;
            }
            if(key.isWritable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                channel.write(ByteBuffer.wrap(((String)key.attachment()).getBytes()));
                key.cancel();
                channel.close();
            }
        }
    }

    void wyslijWiadomosc(Temat msg) throws IOException {
        if(writeChannel == null || !writeChannel.isConnected()) {
            writeChannel = SocketChannel.open(serwer_address);
            writeChannel.configureBlocking(false);
        }
        String[] dane = new String[]{"msg", msg.nazwa, msg.opis};
        String data = String.join(":",dane);
        writeChannel.register(ogarniam, SelectionKey.OP_WRITE).attach(data);
    }

    private void dodajTemat(String nazwa, String opis){
        tematy.add(new Temat(nazwa, opis));
    }
    private int usunTemat(String name) {
        int index = -1;
        for(Temat t : tematy) {
            if(t.nazwa.equals(name)){
                index = tematy.indexOf(t);
                break;
            }
        }
        if(index != -1) tematy.remove(index);
        return index;
    }

    public static void main(String[] args) throws IOException {
            int my_port = Integer.parseInt(args[0]);
            String addr_remote = args[1];
            int port_remote = Integer.parseInt(args[2]);
            TematSender t = new TematSender(new InetSocketAddress(my_port), new InetSocketAddress(addr_remote, port_remote));
    }
}
