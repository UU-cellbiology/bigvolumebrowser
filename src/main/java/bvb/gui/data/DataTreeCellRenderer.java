package bvb.gui.data;

import java.awt.Component;

import javax.swing.ImageIcon;
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

			final ImageIcon currIcon = ((DataTreeNode)value).getIcon();
			if(((DataTreeNode)value).getIcon() != null)
			{
				if(leaf)
				{
					this.setIcon( currIcon );
				}
				else
				{
					this.setIcon( currIcon );
				}
			}
		}
		return this;
	}

}
