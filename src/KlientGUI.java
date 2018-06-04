import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;

public class KlientGUI {
    private Klient klient;
    JList<String> suby;
    JList<Temat> dost_tematy;
    JList<String> aktywnosc;
    private JPanel main_panel;
    DefaultListModel<String> lm_suby;
    DefaultListModel<String> lm_akty;
    DefaultListModel<Temat> lm_dost_tem;



    private JFrame frame;


    void initMenuBar() {
        JMenuBar menu_bar = new JMenuBar();
        JMenu menu = new JMenu("Topic stuff");
        JMenuItem menu_log = new JMenu("Klient stuff");
        JMenuItem menu_item1 = new JMenuItem("pokaż opis");
        JMenuItem menu_item2 = new JMenuItem("subskrybuj");
        JMenuItem menu_item3 = new JMenuItem("odsubskrybuj");
        JMenuItem login = new JMenuItem("zaloguj się");
        JMenuItem logout = new JMenuItem("wyloguj się");

        menu_item2.addActionListener(e -> {
            try {
                String name = dost_tematy.getSelectedValue().nazwa;
                boolean isSubscribed = false;
                for (int i = 0; i < lm_suby.getSize(); i++) {
                    if(name.equals(lm_suby.get(i))) {
                        JOptionPane.showMessageDialog(null, "już subskrybujesz ten temat");
                        isSubscribed = true;
                        break;
                    }
                }
                if(!isSubscribed) {
                    klient.sub(name);
                    lm_suby.addElement(name);
                }

            } catch (ConnectException ignored){
                JOptionPane.showMessageDialog(null, "Błąd z połączeniem do serwera o_O");
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        try {
            menu_item1.addActionListener(e -> JOptionPane.showMessageDialog(null, dost_tematy.getSelectedValue().opis));
        } catch (NullPointerException e) {
            //Wiem o tym! Więc nie pokazuj mi tego w konsoli.
        }

        menu_item3.addActionListener(e -> {
            try {
                klient.unSub(suby.getSelectedValue());
                lm_suby.remove(suby.getSelectedIndex());
            } catch(ConnectException ignored) {
                JOptionPane.showMessageDialog(null, "wystąpił błąd z połączeniem do serwera ");
            }
            catch (IOException e1) {
                e1.printStackTrace();
            } catch (NullPointerException | ArrayIndexOutOfBoundsException el) {
                //nie ma co subskrybować LIPTON o_O
            }
        });
        login.addActionListener(e -> {
            try {
                klient.login();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        logout.addActionListener(e -> {
            try {
                klient.logout();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        menu.add(menu_item1);
        menu.add(menu_item2);
        menu.add(menu_item3);
        menu_log.add(login);
        menu_log.add(logout);
        menu_bar.add(menu);
        menu_bar.add(menu_log);

        frame.setJMenuBar(menu_bar);
    }
    void delDostTemat(String name) {
        for (int i = 0; i < lm_dost_tem.getSize() ; i++) {
            if(lm_dost_tem.get(i).nazwa.equals(name)){
                lm_dost_tem.remove(i);
            }
        }
    }
    void initFrame(){
        frame = new JFrame("KlientGUI");
        frame.setContentPane(main_panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initMenuBar();
        frame.setVisible(true);
        frame.pack();
    }

    KlientGUI(int id, int local_port) throws IOException {
        this.klient = new Klient(id, local_port);
        klient.gui = this;
        Klient.setSerwer(Klient.serwer);

//        aktywnosc.setCellRenderer(new TematComp());

        lm_dost_tem = new DefaultListModel<>();
        lm_akty = new DefaultListModel<>();
        lm_suby = new DefaultListModel<>();

        suby.setModel(lm_suby);
        aktywnosc.setModel(lm_akty);
        dost_tematy.setModel(lm_dost_tem);
        aktywnosc.setEnabled(false);

        initFrame();
        while(true) {
            klient.loop();
        }
    }

    public static void main(String[] args) throws IOException {
        InetSocketAddress address = new InetSocketAddress(args[2], Integer.parseInt(args[3]));
        Klient.setSerwer(address);
        int id = Integer.parseInt(args[0]);
        int local_port = Integer.parseInt(args[1]);
        KlientGUI gui = new KlientGUI(id, local_port);
    }
}
