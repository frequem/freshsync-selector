package com.frequem.freshsync;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

public class Selector {

	public static void main(String[] args) {
		JFrame frame = new JFrame("freshsync-selector v0.2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setSize(640, 480);
		
		JMenuBar menuBar;
		JMenu fileMenu;
		JMenuItem openMenuItem;
		JMenuItem saveMenuItem;
		JMenuItem saveAsMenuItem;
		JMenuItem quitMenuItem;
		
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		openMenuItem = new JMenuItem("Open...");
		saveMenuItem = new JMenuItem("Save...");
		saveAsMenuItem = new JMenuItem("Save As...");
		quitMenuItem = new JMenuItem("Quit");
		
		
		fileMenu.add(openMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveAsMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(quitMenuItem);
		
		frame.setJMenuBar(menuBar);
		
		JCheckBoxTree tree = new JCheckBoxTree();
		frame.add(new JScrollPane(tree));

		final JFileChooser fc = new JFileChooser();
		
		openMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fc.showOpenDialog(frame);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            tree.loadFile(file);
		            frame.setTitle(file.toString());
		        }
			}
		});
		
		saveMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File file = fc.getSelectedFile();
				if(file != null){
			        tree.saveFile(file);
				}
			}
		});
		
		saveAsMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fc.showSaveDialog(frame);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            tree.saveFile(file);
		            frame.setTitle(file.toString());
		        }
			}
		});
		
		quitMenuItem.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
			
		});
		
		frame.setVisible(true);
		
		if(args.length > 0){
			File f = new File(args[0]);
			if(f.exists()){
				fc.setSelectedFile(f);
				tree.loadFile(f);
				frame.setTitle(f.toString());
			}
		}
	}

}
