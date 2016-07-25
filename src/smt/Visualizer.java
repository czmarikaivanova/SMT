package smt;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.File;

import javax.swing.JPanel;

public class Visualizer extends JPanel {

    File instFile;
    int dstCount;
    boolean[][] z;
    
    Node[] nodes;
    
    private final String delimiter = "---------";
    
    
    public Visualizer(boolean[][] z, Node[] nodes, int dstCount) {        
        this.z = z;
        this.nodes = nodes;        
        this.dstCount = dstCount;
    }
    
    @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;

    for (int i = 0; i < nodes.length; i++) {
        Color color;        
        if (i < dstCount) {
            color = Color.black;
        } else {
            color = Color.lightGray;
        }        
        g2d.setColor(color);   
        Node p = nodes[i];
        float xu = Math.round(p.getPoint().getX() * 10);
        float yu = Math.round(p.getPoint().getY() * 10);
        g2d.fillOval((int) xu-5,(int) yu-5, 10, 10);
        g2d.drawString(Integer.toString(i), xu + 8, yu);
        g2d.setStroke(new BasicStroke(2));      
        for (int j = 0; j < nodes.length; j++) {
    	//for (int j = i + 1; j < nodes.length; j++) {
        	if (z[i][j]) {
                float xv = Math.round(nodes[j].getPoint().getX() * 10);
                float yv = Math.round(nodes[j].getPoint().getY() * 10);
                g2d.draw(new Line2D.Float(xu, yu, xv, yv ));
                g2d.setColor(Color.BLUE);      
                int dst = Math.round(Main.dst(nodes[i].getPoint(), nodes[j].getPoint()));
                g2d.drawString(Integer.toString(dst), (xu + xv)/2, (yu + yv)/2-5);
                g2d.setColor(color);        		
        	}
        }

    }
  }
    


}

   
    
    