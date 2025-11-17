/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvb.develop;

import javax.swing.JFrame;
import javax.swing.JPanel;
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
		   // JScrollPane treeView = new JScrollPane(tree);
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
