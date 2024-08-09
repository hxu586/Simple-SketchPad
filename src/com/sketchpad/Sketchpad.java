package com.sketchpad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Sketchpad extends JFrame {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int lastX, lastY;
    private boolean straightLineMode = false;
    private boolean rectangleMode = false;
    private boolean ellipseMode = false;
    private boolean selectMode = false; // New mode for selection
    private boolean groupSelectMode = false; // New mode for group selection
    private Point startPoint = null;
    private Point currentPoint = null;
    private Color currentColor = Color.BLACK; // Default drawing color
    private List<GraphicalObject> objects = new ArrayList<>(); // List of graphical objects
    private GraphicalObject selectedObject = null; // Currently selected object
    private Rectangle selectionRectangle = null; // Selection rectangle for group select

    // Declare buttons as instance variables
    private JToggleButton straightLineButton;
    private JToggleButton rectangleButton;
    private JToggleButton ellipseButton;
    private JToggleButton selectButton;
    private JToggleButton groupSelectButton; // New button for group select
    private JButton colorButton;
    private JButton clearButton;
    private JButton cutButton;
    private JButton pasteButton;

    public Sketchpad() {
        setTitle("Sketchpad");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(currentColor); // Set initial drawing color to currentColor
        clearCanvas(); // Initialize canvas with a white background

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(canvas, 0, 0, null);
                // Draw all objects
                for (GraphicalObject obj : objects) {
                    obj.draw((Graphics2D) g);
                }
                // Draw the temporary object
                if (straightLineMode && startPoint != null && currentPoint != null) {
                    g.setColor(currentColor); // Set the color for the temporary line
                    ((Graphics2D) g).setStroke(new BasicStroke(2)); // Set the stroke for the temporary line
                    g.drawLine(startPoint.x, startPoint.y, currentPoint.x, currentPoint.y);
                } else if (rectangleMode && startPoint != null && currentPoint != null) {
                    g.setColor(currentColor); // Set the color for the temporary rectangle
                    ((Graphics2D) g).setStroke(new BasicStroke(2)); // Set the stroke for the temporary rectangle
                    int x = Math.min(startPoint.x, currentPoint.x);
                    int y = Math.min(startPoint.y, currentPoint.y);
                    int width = Math.abs(startPoint.x - currentPoint.x);
                    int height = Math.abs(startPoint.y - currentPoint.y);
                    g.drawRect(x, y, width, height);
                } else if (ellipseMode && startPoint != null && currentPoint != null) {
                    g.setColor(currentColor); // Set the color for the temporary ellipse
                    ((Graphics2D) g).setStroke(new BasicStroke(2)); // Set the stroke for the temporary ellipse
                    int x = Math.min(startPoint.x, currentPoint.x);
                    int y = Math.min(startPoint.y, currentPoint.y);
                    int width = Math.abs(startPoint.x - currentPoint.x);
                    int height = Math.abs(startPoint.y - currentPoint.y);
                    g.drawOval(x, y, width, height);
                }
                // Draw the selection rectangle
                if (groupSelectMode && selectionRectangle != null) {
                    g.setColor(Color.BLUE); // Set the color for the selection rectangle
                    ((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0)); // Dashed stroke
                    g.drawRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
                }
            }
        };

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (groupSelectMode) {
                    currentPoint = e.getPoint();
                    selectionRectangle = new Rectangle(
                            Math.min(startPoint.x, currentPoint.x),
                            Math.min(startPoint.y, currentPoint.y),
                            Math.abs(startPoint.x - currentPoint.x),
                            Math.abs(startPoint.y - currentPoint.y)
                    );
                    panel.repaint();
                } else if (selectMode) {
                    int dx = e.getX() - startPoint.x;
                    int dy = e.getY() - startPoint.y;

                    // Move all selected objects
                    for (GraphicalObject obj : objects) {
                        if (obj.isSelected()) {
                            obj.setX1(obj.getX1() + dx);
                            obj.setY1(obj.getY1() + dy);
                            obj.setX2(obj.getX2() + dx);
                            obj.setY2(obj.getY2() + dy);
                        }
                    }

                    startPoint = e.getPoint();
                    panel.repaint();
                } else if (!selectMode) {
                    if (straightLineMode || rectangleMode || ellipseMode) {
                        currentPoint = e.getPoint();
                        panel.repaint();
                    } else {
                        g2d.setColor(currentColor); // Set the color for freehand drawing
                        g2d.drawLine(lastX, lastY, e.getX(), e.getY());
                        lastX = e.getX();
                        lastY = e.getY();
                        panel.repaint();
                    }
                }
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (groupSelectMode) {
                    startPoint = e.getPoint();
                    selectionRectangle = null;
                } else if (selectMode) {
                    startPoint = e.getPoint();
                    if (selectedObject != null) {
                        selectedObject.setSelected(false); // Deselect the previously selected object
                    }
                    selectedObject = null;
                    for (GraphicalObject obj : objects) {
                        if (obj.contains(startPoint.x, startPoint.y)) {
                            selectedObject = obj;
                            selectedObject.setSelected(true); // Select the new object
                            break;
                        }
                    }
                    panel.repaint();
                } else {
                    lastX = e.getX();
                    lastY = e.getY();
                    if (straightLineMode || rectangleMode || ellipseMode) {
                        startPoint = e.getPoint();
                        currentPoint = e.getPoint();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (groupSelectMode) {
                    selectionRectangle = new Rectangle(
                            Math.min(startPoint.x, e.getX()),
                            Math.min(startPoint.y, e.getY()),
                            Math.abs(startPoint.x - e.getX()),
                            Math.abs(startPoint.y - e.getY())
                    );
                    for (GraphicalObject obj : objects) {
                        if (selectionRectangle.intersects(obj.getBounds())) {
                            obj.setSelected(true); // Select the object
                        }
                    }
                    selectionRectangle = null;
                    panel.repaint();
                } else if (straightLineMode && startPoint != null) {
                    objects.add(new GraphicalObject(GraphicalObject.ObjectType.LINE, startPoint.x, startPoint.y, e.getX(), e.getY(), currentColor));
                    startPoint = null;
                    currentPoint = null;
                    panel.repaint();
                } else if (rectangleMode && startPoint != null) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int width = Math.abs(startPoint.x - e.getX());
                    int height = Math.abs(startPoint.y - e.getY());
                    objects.add(new GraphicalObject(GraphicalObject.ObjectType.RECTANGLE, x, y, x + width, y + height, currentColor));
                    startPoint = null;
                    currentPoint = null;
                    panel.repaint();
                } else if (ellipseMode && startPoint != null) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int width = Math.abs(startPoint.x - e.getX());
                    int height = Math.abs(startPoint.y - e.getY());
                    objects.add(new GraphicalObject(GraphicalObject.ObjectType.ELLIPSE, x, y, x + width, y + height, currentColor));
                    startPoint = null;
                    currentPoint = null;
                    panel.repaint();
                }
            }
        });

        add(panel, BorderLayout.CENTER);

        // Create a vertical toolbar and place it on the left
        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.Y_AXIS)); // Set layout to BoxLayout

        // Customize button appearance and add action listeners
        clearButton = createStyledButton("Clear");
        clearButton.addActionListener(e -> {
            clearCanvas();
            objects.clear(); // Clear the list of objects
            panel.repaint();
            resetModeButtons();
        });
        toolBar.add(clearButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons

        // Initialize toggle buttons
        straightLineButton = createStyledToggleButton("Straight Line");
        straightLineButton.addActionListener(e -> {
            if (straightLineButton.isSelected()) {
                straightLineMode = true;
                rectangleMode = false; // Ensure rectangle mode is off
                ellipseMode = false; // Ensure ellipse mode is off
                selectMode = false; // Ensure select mode is off
                groupSelectMode = false; // Ensure group select mode is off
                rectangleButton.setSelected(false);
                ellipseButton.setSelected(false);
                selectButton.setSelected(false);
                groupSelectButton.setSelected(false);
            } else {
                straightLineMode = false;
            }
            updateButtonStyles();
        });
        toolBar.add(straightLineButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons

        rectangleButton = createStyledToggleButton("Rectangle");
        rectangleButton.addActionListener(e -> {
            if (rectangleButton.isSelected()) {
                rectangleMode = true;
                straightLineMode = false; // Ensure straight line mode is off
                ellipseMode = false; // Ensure ellipse mode is off
                selectMode = false; // Ensure select mode is off
                groupSelectMode = false; // Ensure group select mode is off
                straightLineButton.setSelected(false);
                ellipseButton.setSelected(false);
                selectButton.setSelected(false);
                groupSelectButton.setSelected(false);
            } else {
                rectangleMode = false;
            }
            updateButtonStyles();
        });
        toolBar.add(rectangleButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons

        ellipseButton = createStyledToggleButton("Ellipse");
        ellipseButton.addActionListener(e -> {
            if (ellipseButton.isSelected()) {
                ellipseMode = true;
                straightLineMode = false; // Ensure straight line mode is off
                rectangleMode = false; // Ensure rectangle mode is off
                selectMode = false; // Ensure select mode is off
                groupSelectMode = false; // Ensure group select mode is off
                straightLineButton.setSelected(false);
                rectangleButton.setSelected(false);
                selectButton.setSelected(false);
                groupSelectButton.setSelected(false);
            } else {
                ellipseMode = false;
            }
            updateButtonStyles();
        });
        toolBar.add(ellipseButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons

        selectButton = createStyledToggleButton("Select&Move");
        selectButton.addActionListener(e -> {
            if (selectButton.isSelected()) {
                selectMode = true;
                straightLineMode = false; // Ensure straight line mode is off
                rectangleMode = false; // Ensure rectangle mode is off
                ellipseMode = false; // Ensure ellipse mode is off
                groupSelectMode = false; // Ensure group select mode is off
                straightLineButton.setSelected(false);
                rectangleButton.setSelected(false);
                ellipseButton.setSelected(false);
                groupSelectButton.setSelected(false);
            } else {
                selectMode = false;
                if (selectedObject != null) {
                    selectedObject.setSelected(false); // Deselect the selected object
                    selectedObject = null;
                }
            }
            updateButtonStyles();
            panel.repaint();
        });
        toolBar.add(selectButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons

        groupSelectButton = createStyledToggleButton("Group Select");
        groupSelectButton.addActionListener(e -> {
            if (groupSelectButton.isSelected()) {
                groupSelectMode = true;
                straightLineMode = false; // Ensure straight line mode is off
                rectangleMode = false; // Ensure rectangle mode is off
                ellipseMode = false; // Ensure ellipse mode is off
                selectMode = false; // Ensure select mode is off
                straightLineButton.setSelected(false);
                rectangleButton.setSelected(false);
                ellipseButton.setSelected(false);
                selectButton.setSelected(false);
            } else {
                groupSelectMode = false;
            }
            updateButtonStyles();
        });
        toolBar.add(groupSelectButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons

        // Add a button to open the color picker
        colorButton = createStyledButton("Color");
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(null, "Choose a color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
                colorButton.setBackground(newColor); // Update the color button to show the selected color
            }
        });
        toolBar.add(colorButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons

        // Add cut and paste buttons
        cutButton = createStyledButton("Delete");
        cutButton.addActionListener(e -> {
            if (selectedObject != null) {
                objects.remove(selectedObject);
                selectedObject = null;
                panel.repaint();
            }
        });
        toolBar.add(cutButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons

        pasteButton = createStyledButton("Copy&Paste");
        pasteButton.addActionListener(e -> {
            if (selectedObject != null) {
                objects.add(new GraphicalObject(selectedObject.getType(), selectedObject.getX1(), selectedObject.getY1(), selectedObject.getX2(), selectedObject.getY2(), selectedObject.getColor()));
                selectedObject = null;
                panel.repaint();
            }
        });
        toolBar.add(pasteButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons

        // Add the toolbar to the left side of the frame
        add(toolBar, BorderLayout.WEST);
    }

    private void clearCanvas() {
        g2d.setColor(Color.WHITE); // Set the background color to white
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Fill the canvas with white
        g2d.setColor(currentColor); // Reset the drawing color to the current color
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 40)); // Set fixed size
        button.setMaximumSize(new Dimension(100, 40)); // Ensure maximum size is respected
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return button;
    }

    private JToggleButton createStyledToggleButton(String text) {
        JToggleButton toggleButton = new JToggleButton(text);
        toggleButton.setPreferredSize(new Dimension(100, 40)); // Set fixed size
        toggleButton.setMaximumSize(new Dimension(100, 40)); // Ensure maximum size is respected
        toggleButton.setBackground(Color.LIGHT_GRAY);
        toggleButton.setForeground(Color.BLACK);
        toggleButton.setFocusPainted(false);
        toggleButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return toggleButton;
    }

    private void resetModeButtons() {
        straightLineMode = false;
        rectangleMode = false;
        ellipseMode = false;
        selectMode = false;
        groupSelectMode = false;
        selectedObject = null;
        straightLineButton.setSelected(false);
        rectangleButton.setSelected(false);
        ellipseButton.setSelected(false);
        selectButton.setSelected(false);
        groupSelectButton.setSelected(false);
        updateButtonStyles();
    }

    private void updateButtonStyles() {
        if (straightLineButton.isSelected()) {
            straightLineButton.setBackground(Color.DARK_GRAY);
        } else {
            straightLineButton.setBackground(Color.LIGHT_GRAY);
        }

        if (rectangleButton.isSelected()) {
            rectangleButton.setBackground(Color.DARK_GRAY);
        } else {
            rectangleButton.setBackground(Color.LIGHT_GRAY);
        }

        if (ellipseButton.isSelected()) {
            ellipseButton.setBackground(Color.DARK_GRAY);
        } else {
            ellipseButton.setBackground(Color.LIGHT_GRAY);
        }

        if (selectButton.isSelected()) {
            selectButton.setBackground(Color.DARK_GRAY);
        } else {
            selectButton.setBackground(Color.LIGHT_GRAY);
        }

        if (groupSelectButton.isSelected()) {
            groupSelectButton.setBackground(Color.DARK_GRAY);
        } else {
            groupSelectButton.setBackground(Color.LIGHT_GRAY);
        }

        // Repaint buttons to ensure the changes are visible
        straightLineButton.repaint();
        rectangleButton.repaint();
        ellipseButton.repaint();
        selectButton.repaint();
        groupSelectButton.repaint();
    }

    public static void main(String[] args) {
        // Set look and feel to cross-platform to avoid any native L&F issues
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new Sketchpad().setVisible(true);
        });
    }
}
