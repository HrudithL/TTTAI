import javax.swing.*;
import java.awt.*;

public class TFrame extends JFrame
{
    private TPanel p;
    public TFrame(String frameName)
    {
        super(frameName);
        p = new TPanel();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        Insets frameInsets = getInsets();

        int frameWidth = p.getWidth() + (frameInsets.left + frameInsets.right);
        int frameHeight = p.getHeight() + (frameInsets.top + frameInsets.bottom);

        setPreferredSize(new Dimension(frameWidth, frameHeight));
        setLayout(null);

        add(p);
        pack();

        setVisible(true);
    }


    public TPanel getP()
    {
        return p;
    }

}
