package com.sketchpad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Sketchpad extends JFrame {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int lastX, lastY;

    public Sketchpad() {
        setTitle("Sketchpad");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(canvas, 0, 0, null);
            }
        };

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                g2d.drawLine(lastX, lastY, e.getX(), e.getY());
                lastX = e.getX();
                lastY = e.getY();
                panel.repaint();
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        add(panel, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            g2d.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            panel.repaint();
        });
        toolBar.add(clearButton);
        add(toolBar, BorderLayout.NORTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Sketchpad().setVisible(true);
        });
    }
}
