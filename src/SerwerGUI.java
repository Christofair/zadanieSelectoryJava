import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;

public class SerwerGUI {
    private JTextArea description;
    private JButton add;
    private JButton del;
    private JTextArea name;
    private JPanel main_panel;
    private Serwer serwer;
    private JFrame frame;

    void initButtons() {
        add.addActionListener(e -> {
            try {
                if(name.getText().equals("")) { throw new ValueException("puste pole nazwa"); }
                else if(description.getText().equals("")) { throw new ValueException("puste pole opisu");}
                serwer.guiDodajTemat(name.getText(), description.getText());
                name.setText("");
                description.setText("");
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(null, "Brak połączenie z senderem");
            }
            catch(ValueException el) {
                if(el.getMessage().equals("puste pole nazwa")) {
                    JOptionPane.showMessageDialog(null, "proszę wypełnić pole nazwy");
                }
                else if(el.getMessage().equals("puste pole opisu")){
                    JOptionPane.showMessageDialog(null, "proszę wypełnić pole opisu");
                }
            }
        });
        del.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    serwer.guiUsunTemat(name.getText());
                    name.setText("");
                    description.setText("");
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "Please enter text in name field");
                }
            }
        });
    }
    void initFrame() {
        frame = new JFrame("SerwerGUI");
        frame.setContentPane(main_panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //It's very important command XD :D
        frame.setVisible(true);
        frame.pack();
    }
    public SerwerGUI(InetSocketAddress remote, int local_port) throws IOException {
        Serwer server = new Serwer(remote, local_port);
        this.serwer = server;
        initButtons();
        initFrame();
        main_loop();
    }
    void main_loop() throws IOException {
        while(true) {
            serwer.loop();
        }
    }

    public static void main(String[] args) throws IOException {
        int local_port, remote_port;
        String addr_remote;
        local_port = Integer.parseInt(args[0]);
        addr_remote = args[1];
        remote_port = Integer.parseInt(args[2]);
        SerwerGUI gui = new SerwerGUI(new InetSocketAddress(addr_remote, remote_port), local_port);
    }
}
