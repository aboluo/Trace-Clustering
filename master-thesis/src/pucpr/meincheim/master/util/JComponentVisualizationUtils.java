package pucpr.meincheim.master.util;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class JComponentVisualizationUtils {
	
	public static void visualize(JComponent component) {
		JFrame jFrame = new JFrame();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1, 2));
		mainPanel.add(component);
		jFrame.setSize(400, 400);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.add(mainPanel, BorderLayout.CENTER);
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);
	}
}
