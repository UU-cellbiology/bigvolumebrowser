package bvb.gui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class DataTreeCellRenderer extends DefaultTreeCellRenderer
{
	@Override
	public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) 
	{

		super.getTreeCellRendererComponent(
				tree, value, sel,
				expanded, leaf, row,
				hasFocus);
		if(value instanceof DataTreeNode)
		{
			String test = this.toString();
			int i=0;
			i++;
			//this.setText( ((DataTreeNode)value).getDescription() );
		}
		return this;
	}

}
