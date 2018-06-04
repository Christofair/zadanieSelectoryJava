import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Vector;

class Klient {

    static InetSocketAddress serwer;
    private int ID;
    //pojebane połączenie, pole (subskrypcje) tylko po to żeby w serwerze się zgadzało
    //this fields is needed for GUI too;
    Vector<String> subskrypcje = new Vector<>(5);
    Vector<Temat> dostepne_tematy = new Vector<>(5);

    //use in serwer to contact with client inner_serwer OMG :S
    private InetSocketAddress client_serwer_address;

    private Selector selector;
    private SocketChannel client;
    private ServerSocketChannel inner_server;
    KlientGUI gui;


    Klient(int id) {
        ID = id;
    }
    Klient(int id, int local_port) throws IOException {
        ID = id;
        selector = Selector.open();


        inner_server = ServerSocketChannel.open();
        inner_server.configureBlocking(false);
        client_serwer_address = new InetSocketAddress(local_port);
        inner_server.bind(client_serwer_address);
        inner_server.register(selector, SelectionKey.OP_ACCEPT);

    }



    void loop() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        String[] dane;
        String wiad = "";
        int res = 0;


        selector.selectNow();
        for (SelectionKey key : selector.selectedKeys()) {
            if(!key.isValid()) continue;
            if (key.isAcceptable()) {
                ServerSocketChannel inner_s_channel = (ServerSocketChannel) key.channel();
                SocketChannel serwer = inner_s_channel.accept();
                if (serwer != null) {
                    serwer.configureBlocking(false);
                    serwer.register(selector, SelectionKey.OP_READ);
                }
//                key.cancel();
//                continue;
            }
            if (key.isReadable()) {
                SocketChannel serwer = (SocketChannel) key.channel();
                res = serwer.read(buffer);
                dane = Serializer.getData(buffer.array(), buffer.position());

                //dane[0] - komenda
                //dane[1] - nazwa tematu
                //dane[2] - opis albo wiadomosc

                if (dane[0].equals("notify")) {
                    if (!dane[1].equals("del")) {
                        // Here may be occur fucking error if someone adds 'temat.nazwa' equal "del" :S
                        Temat t = new Temat(dane[1], dane[2]);
                        addToDostepne(t);
                        gui.lm_dost_tem.addElement(t);
                        wiad = "dodano temat o nazwie: ";
                        gui.lm_akty.addElement(wiad + dane[1]);
                    } else {
                        delFromDostepne(dane[2]);
                        gui.delDostTemat(dane[2]);
                        wiad = "usunieto temat o nazwie: ";
                        gui.lm_akty.addElement(wiad + dane[2]);
                    }
                }
                if (dane[0].equals("msgfc")) {
                    gui.lm_akty.addElement(dane[1]+":   " + dane[2]);
                }
//                key.cancel();
            }
            key.cancel();
            if(!inner_server.isRegistered()) {
                inner_server.register(selector, SelectionKey.OP_ACCEPT);
            }
        }
    }


    void login() throws IOException {
        String[] dane = new String[] {"login", String.valueOf(ID), String.valueOf(client_serwer_address.getPort())};
//        client.register(selector, SelectionKey.OP_WRITE).attach(new String[] {"login",String.valueOf(ID)});
        client = SocketChannel.open(serwer);
        client.write(ByteBuffer.wrap(String.join(":", dane[0], dane[1], dane[2]).getBytes()));
        client.close();
    }
    void logout() throws IOException {
        String[] dane = new String[] {"logout", String.valueOf(ID), String.valueOf(client_serwer_address.getPort())};
//        client.register(selector, SelectionKey.OP_WRITE).attach(new String[]{"logout", String.valueOf(ID)});
        client = SocketChannel.open(serwer);
        client.write(ByteBuffer.wrap(String.join(":", dane[0], dane[1], dane[2]).getBytes()));
        client.close();
    }

    static void setSerwer(InetSocketAddress addr) {
        serwer = addr;
    }
    void setClientAddr(InetSocketAddress addr) {
        client_serwer_address = addr;
    }
    InetSocketAddress getClientAddr() {
        return client_serwer_address;
    }

    int getID() { return ID; }

    //This method is to working in server instances

    /**
     * Wysyła do serwera komunikat, że chce dostawać komunikaty na dany temat.
     *
     * @param temat_name nazwa tematu o którym chce dostawać informacje
     * @throws IOException
     */

    //This method serve client stuff (in GUI on static panel)
    void sub(String temat_name) throws IOException {
        client = SocketChannel.open(serwer);
        String[] dane_funkcyjne =  new String[] {"sub", String.valueOf(ID), temat_name};
//        client.register(selector, SelectionKey.OP_WRITE).attach(dane_funkcyjne);
        client.write(ByteBuffer.wrap(String.join(":",dane_funkcyjne[0], dane_funkcyjne[1], dane_funkcyjne[2]).getBytes()));
        client.close();
    }

    /**
     * Przekazuje komunikat, że chce zakończyć usługę dla danego tematu.
     *
     * @param temat_name - is a name of topic, at which client finished service
     * @throws IOException
     */
    void unSub(String temat_name) throws IOException {
        client = SocketChannel.open(serwer);
        String[] dane_funkcyjne =  new String[] {"unsub", String.valueOf(ID),  temat_name};
        client.write(ByteBuffer.wrap(String.join(":",dane_funkcyjne[0], dane_funkcyjne[1], dane_funkcyjne[2]).getBytes()));
        client.close();
        // W tym miejscu uzaje że jebać sytuacje kiedy serwer jest zajęty i nie otrzyma subskrypcji, pozdrawiam :D
    }

    //This method for thread in Client, which receive information from Server (in GUI on activity panel)
    void addToDostepne(Temat t) {
        dostepne_tematy.add(t);
    }
    void delFromDostepne(String name) {
        int index = -1;
        for(Temat t : dostepne_tematy) {
            if(t.nazwa.equals(name)) {
                index = dostepne_tematy.indexOf(t);
            }
        }
        if(index != -1) dostepne_tematy.remove(index);
    }
}
