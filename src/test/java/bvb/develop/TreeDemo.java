package bvb.develop;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeDemo
{
	public static void main( final String[] args )
	{
		JTree tree;
		DefaultMutableTreeNode top =
		        new DefaultMutableTreeNode("The Java Series");
		    createNodes(top);
		    tree = new JTree(top);
		    JScrollPane treeView = new JScrollPane(tree);
		    JPanel dd = new JPanel();
		    dd.add( tree );
		   JFrame dsd =  new JFrame("BVB");
		   dsd.add( dd );
		   
		   dsd.setVisible( true );
		   
	}
	
	public static void createNodes(DefaultMutableTreeNode top) {
	    DefaultMutableTreeNode category = null;
	    DefaultMutableTreeNode book = null;
	    
	    category = new DefaultMutableTreeNode("Books for Java Programmers");
	    top.add(category);
	    
	    //original Tutorial
	    book = new DefaultMutableTreeNode("test");
	    category.add(book);
	    
	    //Tutorial Continued
	    book = new DefaultMutableTreeNode("tesr2");
	    category.add(book);
	    
	    //...add more books for programmers...

	    category = new DefaultMutableTreeNode("Books for Java Implementers");
	    top.add(category);

	    //VM
	    book = new DefaultMutableTreeNode("");
	    category.add(book);

	    //Language Spec
	    book = new DefaultMutableTreeNode("fdfdfd");
	    category.add(book);
	}
}
