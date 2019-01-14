package io.dancmc.livestream;

import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame {

        private JPanel panel1;
        private JLabel label1;

        public Gui(){
            setContentPane(this.panel1);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
            setMinimumSize(new Dimension(1920 , 1080));
        }


    private void executeOnEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (Exception e) {
                System.out.println("update GUI failed : " + e.getMessage());
            }
        }
    }

    public void setImage(byte[] bytes){
        executeOnEdt(new Runnable() {
            @Override
            public void run() {
                label1.setIcon(new ImageIcon(bytes));
            }
        });
    }


}
