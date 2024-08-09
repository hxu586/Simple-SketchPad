package com.sketchpad;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class GraphicalObject {
    public enum ObjectType { LINE, RECTANGLE, ELLIPSE }

    private ObjectType type;
    private int x1, y1, x2, y2;
    private Color color;
    private boolean selected; // Flag to indicate if the object is selected

    public GraphicalObject(ObjectType type, int x1, int y1, int x2, int y2, Color color) {
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.selected = false;
    }

    public ObjectType getType() { return type; }
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public Color getColor() { return color; }
    public boolean isSelected() { return selected; }

    public void setX1(int x1) { this.x1 = x1; }
    public void setY1(int y1) { this.y1 = y1; }
    public void setX2(int x2) { this.x2 = x2; }
    public void setY2(int y2) { this.y2 = y2; }
    public void setColor(Color color) { this.color = color; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public void draw(Graphics2D g2d) {
        if (selected) {
            g2d.setColor(Color.GREEN); // Draw the selected object in green
        } else {
            g2d.setColor(color);
        }
        switch (type) {
            case LINE:
                g2d.drawLine(x1, y1, x2, y2);
                break;
            case RECTANGLE:
                g2d.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
                break;
            case ELLIPSE:
                g2d.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
                break;
        }

        if (selected) {
            drawHandles(g2d);
        }
    }

    private void drawHandles(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        int handleSize = 6;

        switch (type) {
            case LINE:
                g2d.fillRect(x1 - handleSize / 2, y1 - handleSize / 2, handleSize, handleSize);
                g2d.fillRect(x2 - handleSize / 2, y2 - handleSize / 2, handleSize, handleSize);
                break;
            case RECTANGLE:
            case ELLIPSE:
                g2d.fillRect(x1 - handleSize / 2, y1 - handleSize / 2, handleSize, handleSize);
                g2d.fillRect(x2 - handleSize / 2, y2 - handleSize / 2, handleSize, handleSize);
                g2d.fillRect(x1 - handleSize / 2, y2 - handleSize / 2, handleSize, handleSize);
                g2d.fillRect(x2 - handleSize / 2, y1 - handleSize / 2, handleSize, handleSize);
                break;
        }
    }

    public boolean contains(int x, int y) {
        // Check if a point is within the bounds of the graphical object
        switch (type) {
            case LINE:
                // Simplistic line hit detection
                return new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1)).contains(x, y);
            case RECTANGLE:
                return new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1)).contains(x, y);
            case ELLIPSE:
                Ellipse2D ellipse = new Ellipse2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
                return ellipse.contains(x, y);
            default:
                return false;
        }
    }

    public Rectangle getBounds() {
        // Return the bounding rectangle of the graphical object
        switch (type) {
            case LINE:
                return new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
            case RECTANGLE:
                return new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
            case ELLIPSE:
                return new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
            default:
                return new Rectangle();
        }
    }

    public boolean isHandle(int x, int y) {
        int handleSize = 6;
        Rectangle handle1, handle2, handle3 = null, handle4 = null;

        handle1 = new Rectangle(x1 - handleSize / 2, y1 - handleSize / 2, handleSize, handleSize);
        handle2 = new Rectangle(x2 - handleSize / 2, y2 - handleSize / 2, handleSize, handleSize);

        switch (type) {
            case RECTANGLE:
            case ELLIPSE:
                handle3 = new Rectangle(x1 - handleSize / 2, y2 - handleSize / 2, handleSize, handleSize);
                handle4 = new Rectangle(x2 - handleSize / 2, y1 - handleSize / 2, handleSize, handleSize);
                break;
        }

        return handle1.contains(x, y) || handle2.contains(x, y) || (handle3 != null && handle3.contains(x, y)) || (handle4 != null && handle4.contains(x, y));
    }

    public int getHandleIndex(int x, int y) {
        int handleSize = 6;
        Rectangle handle1, handle2, handle3 = null, handle4 = null;

        handle1 = new Rectangle(x1 - handleSize / 2, y1 - handleSize / 2, handleSize, handleSize);
        handle2 = new Rectangle(x2 - handleSize / 2, y2 - handleSize / 2, handleSize, handleSize);

        switch (type) {
            case RECTANGLE:
            case ELLIPSE:
                handle3 = new Rectangle(x1 - handleSize / 2, y2 - handleSize / 2, handleSize, handleSize);
                handle4 = new Rectangle(x2 - handleSize / 2, y1 - handleSize / 2, handleSize, handleSize);
                break;
        }

        if (handle1.contains(x, y)) return 1;
        if (handle2.contains(x, y)) return 2;
        if (handle3 != null && handle3.contains(x, y)) return 3;
        if (handle4 != null && handle4.contains(x, y)) return 4;

        return 0;
    }
}
