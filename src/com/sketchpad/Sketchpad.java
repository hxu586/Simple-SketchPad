package com.sketchpad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Sketchpad extends JFrame {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int lastX, lastY;
    private boolean straightLineMode = false;
    private boolean rectangleMode = false;
    private boolean ellipseMode = false;
    private Point startPoint = null;
    private Point currentPoint = null;
    private Color currentColor = Color.BLACK; // Default drawing color
    
    // Declare buttons as instance variables
    private JToggleButton straightLineButton;
    private JToggleButton rectangleButton;
    private JToggleButton ellipseButton;
    private JButton colorButton;

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
            }
        };

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
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
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
                if (straightLineMode || rectangleMode || ellipseMode) {
                    startPoint = e.getPoint();
                    currentPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (straightLineMode && startPoint != null) {
                    g2d.setColor(currentColor); // Set the color for the final line
                    g2d.drawLine(startPoint.x, startPoint.y, e.getX(), e.getY());
                    startPoint = null;
                    currentPoint = null;
                    panel.repaint();
                } else if (rectangleMode && startPoint != null) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int width = Math.abs(startPoint.x - e.getX());
                    int height = Math.abs(startPoint.y - e.getY());
                    g2d.setColor(currentColor); // Set the color for the final rectangle
                    g2d.drawRect(x, y, width, height);
                    startPoint = null;
                    currentPoint = null;
                    panel.repaint();
                } else if (ellipseMode && startPoint != null) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int width = Math.abs(startPoint.x - e.getX());
                    int height = Math.abs(startPoint.y - e.getY());
                    g2d.setColor(currentColor); // Set the color for the final ellipse
                    g2d.drawOval(x, y, width, height);
                    startPoint = null;
                    currentPoint = null;
                    panel.repaint();
                }
            }
        });

        add(panel, BorderLayout.CENTER);

        // Create a vertical toolbar and place it on the left
        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);

        // Customize button appearance and add action listeners
        JButton clearButton = createStyledButton("Clear");
        clearButton.addActionListener(e -> {
            clearCanvas();
            panel.repaint();
            resetModeButtons();
        });
        toolBar.add(clearButton);

        // Initialize toggle buttons
        straightLineButton = createStyledToggleButton("Straight Line");
        straightLineButton.addActionListener(e -> {
            if (straightLineButton.isSelected()) {
                straightLineMode = true;
                rectangleMode = false; // Ensure rectangle mode is off
                ellipseMode = false; // Ensure ellipse mode is off
                rectangleButton.setSelected(false);
                ellipseButton.setSelected(false);
            } else {
                straightLineMode = false;
            }
            updateButtonStyles();
        });
        toolBar.add(straightLineButton);

        rectangleButton = createStyledToggleButton("Rectangle");
        rectangleButton.addActionListener(e -> {
            if (rectangleButton.isSelected()) {
                rectangleMode = true;
                straightLineMode = false; // Ensure straight line mode is off
                ellipseMode = false; // Ensure ellipse mode is off
                straightLineButton.setSelected(false);
                ellipseButton.setSelected(false);
            } else {
                rectangleMode = false;
            }
            updateButtonStyles();
        });
        toolBar.add(rectangleButton);

        ellipseButton = createStyledToggleButton("Ellipse");
        ellipseButton.addActionListener(e -> {
            if (ellipseButton.isSelected()) {
                ellipseMode = true;
                straightLineMode = false; // Ensure straight line mode is off
                rectangleMode = false; // Ensure rectangle mode is off
                straightLineButton.setSelected(false);
                rectangleButton.setSelected(false);
            } else {
                ellipseMode = false;
            }
            updateButtonStyles();
        });
        toolBar.add(ellipseButton);

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
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return button;
    }

    private JToggleButton createStyledToggleButton(String text) {
        JToggleButton toggleButton = new JToggleButton(text);
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
        straightLineButton.setSelected(false);
        rectangleButton.setSelected(false);
        ellipseButton.setSelected(false);
        updateButtonStyles();
    }

    private void updateButtonStyles() {
        System.out.println("Updating button styles..."); // Debug statement
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
        
        // Repaint buttons to ensure the changes are visible
        straightLineButton.repaint();
        rectangleButton.repaint();
        ellipseButton.repaint();
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
