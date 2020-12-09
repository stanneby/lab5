package bsu.rfe.java.group9.lab5.Nebyshinets.varC;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
@SuppressWarnings("serial")

public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showTicks = true;
    private boolean scaleMode = false;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    Double[][] viewport = new Double[2][2];
    List<Double[][]> history = new ArrayList<>();
    private double scaleX;
    private double scaleY;
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke ticksStroke;
    private BasicStroke selectionStroke;
    private Font axisFont;
    private java.awt.geom.Rectangle2D.Double selectionRect = new java.awt.geom.Rectangle2D.Double();
    private double[] originalPoint = new double[2];

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
        selectionStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);
        this.addMouseListener(new GraphicsDisplay.MouseHandler());
        this.addMouseMotionListener(new GraphicsDisplay.MouseMotionHandler());
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;

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

        viewport[0][0] = minX;
        viewport[0][1] = maxX;
        viewport[1][0] = minY;
        viewport[1][1] = maxY;

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

        this.scaleX = getSize().getWidth() / (viewport[0][1] - viewport[0][0]);
        this.scaleY = getSize().getHeight() / (viewport[1][1] - viewport[1][0]);

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
        if (showAxis) paintAxis(canvas);
        if (showTicks) paintTicks(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);
        if (this.scaleMode) paintSelection(canvas);
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

    private void paintSelection(Graphics2D canvas) {
        canvas.setStroke(this.selectionStroke);
        canvas.setColor(Color.BLACK);
        canvas.draw(this.selectionRect);
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

            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + i*deltaY), 1));
            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + (i + 1)*deltaY), 1));
            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + (i + 2)*deltaY), 1));
            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + (i + 3)*deltaY), 1));
            canvas.draw(composeHorizontalTick(xyToPoint(0, minY + (i + 4)*deltaY), 2));
        }
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - this.viewport[0][0];
        double deltaY = this.viewport[1][1] - y;
        return new Point2D.Double(deltaX*scaleX, deltaY*scaleY);
    }

    protected double[] pointToXY(double x, double y) {
        return new double[]{this.viewport[0][0] + (double)x / this.scaleX, this.viewport[0][1] - (double)y / this.scaleY};
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

    public void zoomToRegion(double x1, double y1, double x2, double y2) {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = x2;
        this.viewport[1][0] = y2;
        this.viewport[1][1] = y1;
        this.repaint();
    }

    public class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }

        public void mouseClicked(MouseEvent ev) {
            if(ev.getButton() == 3){
                if(GraphicsDisplay.this.history.size() > 0){
                    GraphicsDisplay.this.viewport = GraphicsDisplay.this.history.get(GraphicsDisplay.this.history.size() - 1);
                    GraphicsDisplay.this.history.remove(GraphicsDisplay.this.history.size() - 1);
                }
                GraphicsDisplay.this.repaint();
            }
        }

        public void mousePressed(MouseEvent ev) {
            if (ev.getButton() == 1) {
                GraphicsDisplay.this.originalPoint = GraphicsDisplay.this.pointToXY(ev.getX(), ev.getY());

                GraphicsDisplay.this.scaleMode = true;
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));
                GraphicsDisplay.this.selectionRect.setFrame((double) ev.getX(), (double) ev.getY(), 1.0D, 1.0D);
            }
        }

        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() == 1) {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));

                GraphicsDisplay.this.scaleMode = false;
                history.add(viewport);
                double[] finalPoint = GraphicsDisplay.this.pointToXY(ev.getX(), ev.getY());
                GraphicsDisplay.this.viewport = new Double[2][2];
                GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.originalPoint[0], GraphicsDisplay.this.originalPoint[1], finalPoint[0], finalPoint[1]);
                GraphicsDisplay.this.repaint();
            }
        }
    }

    public class MouseMotionHandler implements MouseMotionListener {
        public MouseMotionHandler() {
        }

        public void mouseMoved(MouseEvent ev) {
        }

        public void mouseDragged(MouseEvent ev) {
            double width = (double)ev.getX() - GraphicsDisplay.this.selectionRect.getX();
            if (width < 5.0D) {
                width = 5.0D;
            }

            double height = (double)ev.getY() - GraphicsDisplay.this.selectionRect.getY();
            if (height < 5.0D) {
                height = 5.0D;
            }

            GraphicsDisplay.this.selectionRect.setFrame(GraphicsDisplay.this.selectionRect.getX(), GraphicsDisplay.this.selectionRect.getY(), width, height);
            GraphicsDisplay.this.repaint();
        }
    }
}

