package bsu.rfe.java.group9.lab5.Nebyshinets.varC;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import javax.swing.JPanel;
@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showTicks = true;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double scale;
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke ticksStroke;
    private Font axisFont;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, new float[] {16, 4, 4, 4, 8, 4, 4, 4}, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        ticksStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }
    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    public void setShowTicks(boolean showTicks) {
        this.showTicks = showTicks;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData==null || graphicsData.length==0) return;
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length-1][0];
        minY = graphicsData[0][1];
        maxY = minY;
        for (int i = 1; i<graphicsData.length; i++) {
            if (graphicsData[i][1]<minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1]>maxY) {
                maxY = graphicsData[i][1];
            }
        }
        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);
        scale = Math.min(scaleX, scaleY);
        if (scale==scaleX) {
            double yIncrement = (getSize().getHeight()/scale - (maxY -
                    minY))/2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale==scaleY) {
            double xIncrement = (getSize().getWidth()/scale - (maxX -
                    minX))/2;
            maxX += xIncrement;
            minX -= xIncrement;
        }
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
        if (showAxis) paintAxis(canvas);
        if (showTicks) paintTicks(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.RED);

        GeneralPath graphics = new GeneralPath();
        for (int i=0; i<graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0],
                    graphicsData[i][1]);
            if (i>0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);
        for (Double[] point: graphicsData) {
            GeneralPath marker = composeMarker(point);
            if( (point[1] - point[1].intValue()) < 0.1 || (point[1] - point[1].intValue()) > 0.9  ){
                canvas.setColor(Color.BLUE);
            }

            canvas.draw(marker);
            canvas.setColor(Color.RED);
        }
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        if (minX<=0.0 && maxX>=0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()+5, arrow.getCurrentPoint().getY()+20);
            arrow.lineTo(arrow.getCurrentPoint().getX()-10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
        }
        if (minY<=0.0 && maxY>=0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                    xyToPoint(maxX, 0)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()-20, arrow.getCurrentPoint().getY()-5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY()+10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float)(labelPos.getX() -
                    bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
        }
    }

    protected void paintTicks(Graphics2D canvas) {

        canvas.setStroke(ticksStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);

        double deltaX = (maxX - minX)/100;
        double deltaY = (maxY - minY)/100;

        for(int i = 0; i < 100; i+=5){
            canvas.draw(composeVerticalTick(xyToPoint(minX + i*deltaX, 0), 1));
            canvas.draw(composeVerticalTick(xyToPoint(minX + (i + 1)*deltaX, 0), 1));
            canvas.draw(composeVerticalTick(xyToPoint(minX + (i + 2)*deltaX, 0), 1));
            canvas.draw(composeVerticalTick(xyToPoint(minX + (i + 3)*deltaX, 0), 1));
            canvas.draw(composeVerticalTick(xyToPoint(minX + (i + 4)*deltaX, 0), 2));

            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + i*deltaX), 1));
            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + (i + 1)*deltaX), 1));
            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + (i + 2)*deltaX), 1));
            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + (i + 3)*deltaX), 1));
            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + (i + 4)*deltaX), 2));
        }
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX,
                                        double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

    protected GeneralPath composeMarker(Double[] point){
        GeneralPath marker = new GeneralPath();
        Point2D.Double point2d = xyToPoint(point[0],
                point[1]);
        double x_0 = point2d.getX();
        double y_0 = point2d.getY();

        marker.moveTo(x_0 - 5, y_0 - 5);
        marker.lineTo(x_0 - 5, y_0 + 5);
        marker.lineTo(x_0 + 5, y_0 + 5);
        marker.lineTo(x_0 + 5, y_0 - 5);
        marker.lineTo(x_0 - 5, y_0 - 5);
        marker.append(new Line2D.Double(x_0 - 5, y_0 - 5, x_0 + 5, y_0 + 5), true);
        marker.append(new Line2D.Double(x_0 - 5, y_0 + 5, x_0 + 5, y_0 - 5), true);

        return marker;
    };

    protected GeneralPath composeVerticalTick(Point2D point, int type){
        GeneralPath tick = new GeneralPath();
        double x = point.getX();
        double y = point.getY();
        tick.append(new Line2D.Double(x, y - type*5, x, y + type*5), true);

        return tick;
    }

    protected GeneralPath composeHorizontalTick(Point2D point, int type){
        GeneralPath tick = new GeneralPath();
        double x = point.getX();
        double y = point.getY();
        tick.append(new Line2D.Double(x - type*5, y, x + type*5, y), true);

        return tick;
    }
}

