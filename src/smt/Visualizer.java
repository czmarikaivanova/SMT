package smt;


import graph.Graph;
import graph.Node;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.io.File;

import javax.swing.JPanel;

public class Visualizer extends JPanel {

    File instFile;
    int dstCount;
    Double[][] z;
    Graph graph;
    boolean useArrows;
    
    public Visualizer(Double[][] z, Graph graph, boolean useArrows) {        
        this.z = z;
        this.graph = graph;        
        this.useArrows = useArrows;
    }
    
    @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;

    for (int i = 0; i < graph.getVertexCount(); i++) {
        Color color;        
        if (i < graph.getDstCount()) {
            color = Color.black;
        } else {
            color = Color.lightGray;
        }        
        g2d.setColor(color);   
        Node p = graph.getNode(i);
        float xu = Math.round(p.getPoint().getX() * 10);
        float yu = Math.round(p.getPoint().getY() * 10);
        g2d.fillOval((int) xu-5,(int) yu-5, 10, 10);
        g2d.drawString(Integer.toString(i), xu + 8, yu);
        g2d.setStroke(new BasicStroke(2));      
        for (int j = 0; j < graph.getVertexCount(); j++) {
    	//for (int j = i + 1; j < nodes.length; j++) {
        	if ((z[i][j] != null) && (z[i][j] > 0)) {
                float xv = Math.round(graph.getNode(j).getPoint().getX() * 10);
                float yv = Math.round(graph.getNode(j).getPoint().getY() * 10);

                if (useArrows) {
                    drawArrowLine(g2d, (int) xu, (int) yu, (int) xv, (int) yv, 15, 5);                	
                }
                else {
                	g2d.draw(new Line2D.Float(xu, yu, xv, yv ));                	
                }
                g2d.setColor(Color.BLUE);      
                int dst = Math.round(graph.getRequir(i, j));
                g2d.drawString(Integer.toString(dst), (xu + xv)/2, (yu + yv)/2-5);
                g2d.setColor(color);        		
        	}
        }
    }      
  }
    
    /**
     * Draw an arrow line betwwen two point 
     * @param g the graphic component
     * @param x1 x-position of first point
     * @param y1 y-position of first point
     * @param x2 x-position of second point
     * @param y2 y-position of second point
     * @param d  the width of the arrow
     * @param h  the height of the arrow
     */
    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h){
       int dx = x2 - x1, dy = y2 - y1;
       double D = Math.sqrt(dx*dx + dy*dy);
       double xm = D - d, xn = xm, ym = h, yn = -h, x;
       double sin = dy/D, cos = dx/D;

       x = xm*cos - ym*sin + x1;
       ym = xm*sin + ym*cos + y1;
       xm = x;

       x = xn*cos - yn*sin + x1;
       yn = xn*sin + yn*cos + y1;
       xn = x;

       int[] xpoints = {x2, (int) xm, (int) xn};
       int[] ypoints = {y2, (int) ym, (int) yn};

       g.drawLine(x1, y1, x2, y2);
       g.fillPolygon(xpoints, ypoints, 3);
    }

}

   
    
    