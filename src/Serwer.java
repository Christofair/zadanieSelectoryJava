import javax.swing.*;
import javax.swing.text.Style;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Vector;


public class Serwer {

    private Selector selector;
    private Vector<Klient> klienci = null;
    private static InetSocketAddress addr_for_sender;
    ServerSocketChannel server_channel;


    private void setAddrForSender(InetSocketAddress addr) {
        addr_for_sender = addr;
    }

    Serwer(InetSocketAddress sender_addr, int port) throws IOException {
        setAddrForSender(sender_addr);

        selector = Selector.open();
        server_channel = ServerSocketChannel.open();
        server_channel.configureBlocking(false);
        server_channel.bind(new InetSocketAddress("0.0.0.0", port));
        server_channel.register(selector, SelectionKey.OP_ACCEPT);
        klienci = new Vector<>();
    }
    void loop() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        String[] received;
        int res = 0;
        //it's a main loop to serve SocketChannels
        selector.selectNow();
        for (SelectionKey klucz : selector.selectedKeys()) {
            if(!klucz.isValid()) continue;
            if (klucz.isAcceptable()) {
                ServerSocketChannel odbior = (ServerSocketChannel) klucz.channel();
                SocketChannel channel = odbior.accept();
                if(channel != null) {
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                }
//                continue;
            }
            if (klucz.isReadable()) {
                SocketChannel channel = (SocketChannel) klucz.channel();
                res = channel.read(buffer);
                received = Serializer.getData(buffer.array(), res);
                if(received[0].equals("sub") || received[0].equals("unsub")) {
                    Klient klient = klientExist(Integer.parseInt(received[1]));
                    if(klient != null) {
                        if (received[0].equals("sub")) {
                            klient.subskrypcje.add(received[2]);
                        } else {
                            int index = -1;
                            for (String i : klient.subskrypcje) {
                                if (i.equals(received[2])) {
                                    index = klient.subskrypcje.indexOf(i);
                                }
                            }
                            if (index != -1) klient.subskrypcje.remove(index);
                        }
                    }
                } else if(received[0].equals("msg")) {
                    Temat msg = new Temat(received[1], received[2]);
                    rozeslijWiad(msg);
                }
                else if(received[0].equals("login")) {
                    JOptionPane.showMessageDialog(null,"zaakceptowano połączenie na adresie: " + channel.getRemoteAddress());
                    if(klientExist(Integer.parseInt(received[1])) == null) {
                        klienci.add(new Klient(Integer.parseInt(received[1])));
                        klienci.lastElement().setClientAddr(new InetSocketAddress(Integer.parseInt(received[2])));
/*                        SocketChannel old_tops = SocketChannel.open(addr_for_sender);
                        old_tops.configureBlocking(false);
                        old_tops.write(ByteBuffer.wrap(("want:" + klienci.lastElement().getClientAddr().toString()).getBytes()));*/
                    }
                    else {
//                        channel.write(ByteBuffer.wrap("err:już jesteś zalogowany!".getBytes()));
                    }
                }
                else if(received[0].equals("logout")) {
                    Klient k = klientExist(Integer.parseInt(received[1]));
                    klienci.remove(k);
                }
//                klucz.cancel();
                channel.close();
                continue;
            }
            if (klucz.isWritable()) {
                SocketChannel channel = (SocketChannel) klucz.channel();
                String[] rodzaj = (String[]) klucz.attachment();
                if (Objects.equals(rodzaj[0], "add")) {
                    //Można by było uprościć ten kod, ale tak lepiej widać o co w nim chodzi :S
                    String[] nowy = new String[]{"add", rodzaj[1], rodzaj[2]};
                    String a = String.join(":", nowy);
                    channel.write(ByteBuffer.wrap(a.getBytes()));
                    //Uwaga tutaj powiadominie klienta o dodanym temacie
                    infoKlient(rodzaj[1] + ":" + rodzaj[2]);
                } else if (Objects.equals(rodzaj[0], "del")) {
                    //Można by było uprościć ten kod, ale tak lepiej widać o co w nim chodzi :S
                    String[] nowy = new String[]{"del", rodzaj[1]};
                    String a = String.join(":", nowy);
                    channel.write(ByteBuffer.wrap(a.getBytes()));
                    channel.close();
                    //Uwaga tutaj powiadominie klienta o usuniętym temacie
                    infoKlient(a);
                }
                // msgfc - message for client, it means that this message is from server to client
                else if (Objects.equals(rodzaj[0], "msgfc")) {
                    String nowy = String.join(":", rodzaj[0], rodzaj[1], rodzaj[2]);
                    channel.write(ByteBuffer.wrap(nowy.getBytes()));
                }
                if (rodzaj[0].equals("notify")) {
                    // nowy have to add to watch after, when we will be testing it.
                    String nowy = String.join(":", rodzaj[0], rodzaj[1]);
                    channel.write(ByteBuffer.wrap(nowy.getBytes()));
                }
                channel.close(); //ale chyba jednak to może być to jest coś nie tak z akceptowaniem klienta
//                klucz.cancel();
            }
            klucz.cancel();
            if(!server_channel.isRegistered()) {
                server_channel.register(selector, SelectionKey.OP_ACCEPT);
            }
        }
    }

    /**
     * This method is sending topics to clients, who wants these information.
     * @param temat
     * @throws IOException
     */
    private void rozeslijWiad(Temat temat) throws IOException {
        String[] dane = new String[]{"msgfc", temat.nazwa, temat.opis};
        if(klienci != null) {
            for (Klient k : klienci) {
                if (k.subskrypcje.contains(temat.nazwa)) {
                    SocketChannel write_channel = SocketChannel.open(k.getClientAddr());
                    write_channel.configureBlocking(false);
                    write_channel.register(selector, SelectionKey.OP_WRITE).attach(dane);
                }
            }
        }
    }

    /**
     * This method informed clients, that the topic have been added or deleted
     * @param komunikat
     * @throws IOException
     */
    private void infoKlient(String komunikat) throws IOException {
        String[] info = new String[]{"notify", komunikat};
        if(klienci != null) {
            for (Klient klient : klienci) {
                SocketChannel channel = SocketChannel.open(klient.getClientAddr());
                if(channel != null) {
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_WRITE).attach(info);
                }
            }
        }
    }

    /**
     * This method check whether client exist
     * @param id
     * @return
     */
    private Klient klientExist(int id){
        Klient k = null;
        for(Klient klient : klienci) {
            if(id == klient.getID()) {
                k = klient;
                break;
            }
        }
        return k;
    }

    void guiDodajTemat(String nazwa, String opis) throws IOException {
        SocketChannel add_channel = SocketChannel.open(addr_for_sender);
        add_channel.configureBlocking(false);
        add_channel.register(selector, SelectionKey.OP_WRITE).attach(new String[]{"add", nazwa, opis});
    }
    void guiUsunTemat(String nazwa) throws IOException {
        SocketChannel del_channel = SocketChannel.open(addr_for_sender);
        del_channel.configureBlocking(false);
        del_channel.register(selector, SelectionKey.OP_WRITE).attach(new String[]{"del", nazwa});
    }
}
