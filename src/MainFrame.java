import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame{
    public MainFrame() {
        setTitle("Pixel Art Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MainPanel panel = new MainPanel();
        panel.setPreferredSize(new Dimension(480, 260));
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }
}
