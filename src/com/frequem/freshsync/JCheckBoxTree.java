package com.frequem.freshsync;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class JCheckBoxTree extends JPanel{

	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	
	private JTree tree;
	
	public JCheckBoxTree(){
		super(new BorderLayout());
		root = new DefaultMutableTreeNode();
		model = new DefaultTreeModel(root);
		tree = new JTree(model);
		
		tree.setRootVisible(true);
		
		root.setUserObject(new Item("/", false));
		model.reload();
		
		tree.setCellRenderer(new ItemNodeRenderer());
		
		tree.setCellEditor(new ItemNodeEditor(tree));
	    tree.setEditable(true);
		
		this.add(BorderLayout.CENTER, tree);
	}
	
	public void loadFile(File f){
		root.setUserObject(new Item("/", false));
		root.removeAllChildren();
		
		String rootPath = "/";
		DefaultMutableTreeNode lastNode;
		boolean nodeSet;
		boolean isFolder;
		boolean selected;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while((line = br.readLine()) != null){
				if(line.startsWith("r:")){
					rootPath = line.substring(2);
					root.setUserObject(new Item(rootPath, false));
					continue;
				}
				selected = line.startsWith("1:");
				
				line = line.substring(2 + rootPath.length());
				if(line.length() == 0){
					((Item)root.getUserObject()).setSelected(selected);
					continue;
				}
				
				isFolder = line.endsWith("/");
				
				String[] sa = line.split("/");
				
				lastNode = root;
				for(int i = 0; i < sa.length; i++){
					nodeSet = false;
					for(int j = 0; j < lastNode.getChildCount(); j++){
						if(((Item)(((DefaultMutableTreeNode)lastNode.getChildAt(j)).getUserObject())).getFileName().equals(sa[i])){
							lastNode = (DefaultMutableTreeNode) lastNode.getChildAt(j);
							nodeSet = true;
						}
					}
					if(!nodeSet){
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
						newNode.setUserObject(new Item(sa[i], selected));
						if(isFolder){
							int j = 0; 
							for(; j < lastNode.getChildCount(); j++){
								if(lastNode.getChildAt(j).getChildCount() == 0)
									break;
							}
							model.insertNodeInto(newNode, lastNode, j);
						} else{
							lastNode.add(newNode);
						}
						lastNode = newNode;
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		model.reload();
	}

	
	public void saveFile(File f){
		try {
			PrintWriter out = new PrintWriter(f);
			out.println("r:" + ((Item)root.getUserObject()).getFileName());
			writeNodes(out, root, "", false);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void writeNodes(PrintWriter output, DefaultMutableTreeNode root, String path, boolean parentSelected){
		Item node = ((Item)root.getUserObject());
		path += node.getFileName();

		if(!path.endsWith("/") && root.getChildCount() > 0)
			path += "/";
		
		output.println(((node.isSelected() && !parentSelected)?"1:":"0:") + path);
		
		
		for(int i = 0; i < root.getChildCount(); i++){
			writeNodes(output, (DefaultMutableTreeNode) root.getChildAt(i), path, node.isSelected() || parentSelected);
		}
	}
	
	
	private class Item{
		private String fileName;
		private boolean selected;
		
		public Item(String fileName, boolean selected){
			this.fileName = fileName;
			this.selected = selected;
		}
		
		public void setSelected(boolean selected){
			this.selected = selected;
		}
		
		public String getFileName(){
			return this.fileName;
		}
		
		public boolean isSelected(){
			return this.selected;
		}
		
	}
	
	private class ItemNodeRenderer implements TreeCellRenderer{
		
		private JCheckBox renderer = new JCheckBox();
		
		private Color selectionForeground, selectionBackground, textForeground, textBackground;
		
		protected JCheckBox getRenderer(){
			return renderer;
		}
		
		public ItemNodeRenderer(){
		    selectionForeground = UIManager.getColor("Tree.selectionForeground");
		    selectionBackground = UIManager.getColor("Tree.selectionBackground");
		    textForeground = UIManager.getColor("Tree.textForeground");
		    textBackground = UIManager.getColor("Tree.textBackground");
		}
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
				boolean expanded, boolean leaf, int row, boolean hasFocus) {
			
		    renderer.setSelected(false);
		    renderer.setEnabled(tree.isEnabled());
		    
		    
		    if (selected) {
		    	renderer.setForeground(selectionForeground);
		    	renderer.setBackground(selectionBackground);
		    } else {
		    	renderer.setForeground(textForeground);
		    	renderer.setBackground(textBackground);
		    }
		    
		    if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
		    	Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		        if (userObject instanceof Item) {
		        	Item node = (Item) userObject;
		        	renderer.setText(node.getFileName());
		        	renderer.setSelected(node.isSelected());
		        }
		    }
		    return renderer;
		    
		}
		
	}
	
	private class ItemNodeEditor extends AbstractCellEditor implements TreeCellEditor {

		private ItemNodeRenderer renderer = new ItemNodeRenderer();
		
		private JTree tree;
		
		public ItemNodeEditor(JTree tree){
			this.tree = tree;
		}
		
		@Override
		public Object getCellEditorValue() {
			JCheckBox checkbox = renderer.getRenderer();
			Item node = new Item(checkbox.getText(), checkbox.isSelected());
			return node;
		}
		
		@Override
		public boolean isCellEditable(EventObject event) {
			boolean returnValue = false;
			if (event instanceof MouseEvent) {
				MouseEvent mouseEvent = (MouseEvent) event;
				TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
				if (path != null) {
					Object node = path.getLastPathComponent();
			        if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
			        	DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
			        	Object userObject = treeNode.getUserObject();
			        	returnValue = (userObject instanceof Item);
			        }
				}
			}
			return returnValue;
		};

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row) {
			Component editor = renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
			ItemListener itemListener = new ItemListener(){

				@Override
				public void itemStateChanged(ItemEvent itemEvent) {
					if (stopCellEditing()) {
						fireEditingStopped();
				    }
				}
			};
			if (editor instanceof JCheckBox) {
			      ((JCheckBox) editor).addItemListener(itemListener);
			}
			return editor;
		}
		
	}
}
