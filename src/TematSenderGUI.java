import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;

public class TematSenderGUI {
    private JPanel main_panel;
    private JButton sendButton;
    private JTextArea message;
    JList<String> dost_tematy;
    DefaultListModel<String> listModel;
    private TematSender temat_sender;

    public TematSenderGUI(InetSocketAddress my_addr, InetSocketAddress serwer_addr) throws IOException {
        temat_sender = new TematSender(my_addr, serwer_addr);
        temat_sender.gui = this;
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    try {
                        if(message.getText().equals("") || dost_tematy.getSelectedValue().equals("")){ throw new NullPointerException(); }
                        temat_sender.wyslijWiadomosc(new Temat(dost_tematy.getSelectedValue(),message.getText()));
                        message.setText("");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (NullPointerException el) {
                        JOptionPane.showMessageDialog(null,"proszę wypełnić pola.");
                    }
            }
        });
        listModel = new DefaultListModel<>();
        dost_tematy.setModel(listModel);
        dost_tematy.setVisible(false);
        dost_tematy.setVisible(true);
        initFrame();
        while(true) {
            temat_sender.loop();
        }
    }

    void initFrame() {
        JFrame frame = new JFrame("TematSender");
        frame.setContentPane(main_panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(500,300));
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        int my_port = Integer.parseInt(args[0]);
        String addr_remote = args[1];
        int port_remote = Integer.parseInt(args[2]);
        TematSenderGUI t = new TematSenderGUI(new InetSocketAddress(my_port), new InetSocketAddress(addr_remote, port_remote));
    }
}
