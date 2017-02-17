package smt;


import graph.Edge;
import graph.Graph;
import graph.Node;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Visualizer extends JPanel {

    File instFile;
    int dstCount;
    Double[][] z;
    Double[][][] f;
    Graph graph;
    Graph tree;
    boolean useArrows;
    
    public Visualizer(Object solution, Graph graph, boolean useArrows) {
    	if (solution instanceof Graph) {
    		this.tree = (Graph) solution;
    		this.z = null;
    		this.f = null;
    	}
    	else if (solution instanceof Double[][]) {
    		this.z = (Double[][]) solution;
    		this.tree = null;
    		this.f = null;
    	} else {
    		this.f = (Double[][][]) solution;
    		this.tree = null;
    		this.z = null;
    	}
        this.graph = graph;        
        this.useArrows = useArrows;
    }
    
    @Override
	  public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    if (tree != null) {
	    	paintFromTree(g);
	    }
	    else if (z != null) {
	    	paintFromZVar(g);
	    } else {
	    	paintFromFVar(g);
	    }
	  }

    private void paintFromFVar(Graphics g) {
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
            	for (int k = 0; k < graph.getDstCount(); k++) {
        	//for (int j = i + 1; j < nodes.length; j++) {
	            	if ((k == 2) && (f[i][j][k] != null) && (f[i][j][k] > 0) ) {
	                    float xv = Math.round(graph.getNode(j).getPoint().getX() * 10);
	                    float yv = Math.round(graph.getNode(j).getPoint().getY() * 10);
	
	                    if (useArrows) {
	                        drawArrowLine(g2d, (int) xu, (int) yu, (int) xv, (int) yv, 15, 5);                	
	                    }
	                    else {
	                    	g2d.draw(new Line2D.Float(xu, yu, xv, yv ));                	
	                    }
	                    g2d.setColor(Color.BLUE);      
	                    double dst = Miscellaneous.round(f[i][j][k],2);
	//                    int dst = Math.round(graph.getRequir(i, j));
	                    g2d.drawString(Double.toString(dst), (xu + xv)/2, (yu + yv)/2-5);
	                    g2d.setColor(color);        		
	            	}
            	}
            }
        } 
	}

	private void paintFromTree(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        for (int i = 0; i < tree.getVertexCount(); i++) {
            Color color;
            Node u = tree.getNode(i);
            if (i < graph.getDstCount()) {
                color = Color.black;
            } else {
                color = Color.lightGray;
            }        
            g2d.setColor(color);            
            float xu = Math.round(u.getPoint().getX() * 10);
            float yu = Math.round(u.getPoint().getY() * 10);
            g2d.fillOval((int) xu-5,(int) yu-5, 10, 10);
            g2d.drawString(Integer.toString(u.getId()), xu + 8, yu);
            g2d.setStroke(new BasicStroke(2));              
            for (Edge e: u.getIncidentEdges()) {
                float xv = Math.round(e.getV().getPoint().getX() * 10);
                float yv = Math.round(e.getV().getPoint().getY() * 10);
                g2d.draw(new Line2D.Float(xu, yu, xv, yv ));
                g2d.setColor(Color.BLUE);       
                g2d.drawString(Integer.toString(Math.round(e.getCost())), (xu + xv)/2, (yu + yv)/2-5);
                g2d.setColor(color);
            }
        }
      }    
    
    private void paintFromZVar(Graphics g) {
    	BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g2d = bi.createGraphics();
    	g2d.setColor(Color.WHITE);
    	g2d.fillRect(0, 0, 1000, 1000);
//        Graphics2D g2d = (Graphics2D) g;
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
            	if ((z[i][j] != null) && (z[i][j] > 0) ) {
                    float xv = Math.round(graph.getNode(j).getPoint().getX() * 10);
                    float yv = Math.round(graph.getNode(j).getPoint().getY() * 10);

                    if (useArrows) {
                        drawArrowLine(g2d, (int) xu, (int) yu, (int) xv, (int) yv, 15, 5);                	
                    }
                    else {
                    	g2d.draw(new Line2D.Float(xu, yu, xv, yv ));                	
                    }
                    g2d.setColor(Color.BLUE);      
//                    double dst = Miscellaneous.round(z[i][j],2);
                    int dst = Math.round(graph.getRequir(i, j));
                    g2d.drawString(Double.toString(dst), (xu + xv)/2, (yu + yv)/2-5);
                    g2d.setColor(color);        		
            	}
            }
        } 
        try {
			ImageIO.write(bi, "PNG", new File("c:\\yourImageName.PNG"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

   
    
    