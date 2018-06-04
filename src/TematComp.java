import javax.swing.*;
import java.awt.*;

public class TematComp extends JLabel implements ListCellRenderer<Object> {
    private JTextArea wiad;

    public TematComp() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        wiad.setText(value.toString());
        wiad.enableInputMethods(false);
        wiad.setDisabledTextColor(Color.BLACK);

        JList.DropLocation dropLocation = list.getDropLocation();

        /*if (notification) {
            wiad.setBackground(Color.CYAN);
        } else {
            wiad.setBackground(Color.GREEN);
        }*/
        Dimension d = list.getSize();
        this.setSize(d.width, 100);

        return this;
    }
}
