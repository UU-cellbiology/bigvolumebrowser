/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package bvb.io;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import bvb.core.BVBSettings;
import ij.Prefs;

public class OmeZarrSelectionDialog
{

	JPanel pURIDialog = null;
	final JTextField jlFileName = new JTextField();
	
	public String finalURI = null;
	
	final int panelWidth = 600;
	final int panelHeight = 50;
	
	final int textBoxWidth = 480;
    final JButton butOk;
    final JButton butCancel;
    
    private final String PLACEHOLDER = "Paste URI or use Browse button...";
    private final Color PLACEHOLDER_COLOR = Color.GRAY;
    private final Color DEFAULT_COLOR = Color.BLACK;
    
    public OmeZarrSelectionDialog()
    {
		///OK/CANCEL BUTTONS		
		butOk = new JButton("OK");
		GridBagConstraints gbc = new GridBagConstraints();
		butOk.setEnabled( false );
		butOk.addActionListener(e -> {
			//bAllSuccess = true;
			JOptionPane pane = getOptionPane((JComponent)e.getSource());
            pane.setValue(butOk);
			}); 
		butCancel = new JButton("Cancel");
		butCancel.addActionListener(e -> {
            JOptionPane pane = getOptionPane((JComponent)e.getSource());
            finalURI = null;
            pane.setValue(butCancel);
			}); 

		//FILE SELECTION PANEL
		JPanel pFileSelect = new JPanel(new GridBagLayout());
		jlFileName.setHorizontalAlignment( SwingConstants.LEFT );
		
		Dimension minTxtFileDim = jlFileName.getMinimumSize();
		
		minTxtFileDim.width = textBoxWidth;
		
		jlFileName.setMinimumSize( minTxtFileDim );
		jlFileName.setPreferredSize( minTxtFileDim );
		jlFileName.setEditable( true );
		
		setPlaceholderState();
		
		jlFileName.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (isPlaceholderActive()) {
					jlFileName.setText("");
					setNormalState();
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (jlFileName.getText().trim().isEmpty()) {
					setPlaceholderState();
				}
			}
		});

		// Listen to typed or pasted input dynamically
		jlFileName.getDocument().addDocumentListener(new DocumentListener() {
			private void checkText() {
				String text = jlFileName.getText().trim();
				boolean hasValidValue = !text.isEmpty() && !text.equals(PLACEHOLDER);

				if (hasValidValue) {
					// Ensure style is normal if valid path is pasted/typed
					setNormalState();
					finalURI = text;
					butOk.setEnabled(true);
				} else if (!isPlaceholderActive()) {
					// If cleared down to empty string while typing, disable OK button
					finalURI = null;
					butOk.setEnabled(false);
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) { checkText(); }
			@Override
			public void removeUpdate(DocumentEvent e) { checkText(); }
			@Override
			public void changedUpdate(DocumentEvent e) { checkText(); }
		});
		//jlFileName.setText( "Paste URI or use Browse button..." );		

		JButton butSelectFile = new JButton("Browse");
		butSelectFile.addActionListener( (e)-> {
			JFileChooser chooser = new JFileChooser(BVBSettings.lastDir);
	        chooser.setDialogTitle( "Open OME-Zarr from disk" );
	        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        
	        int returnVal = chooser.showOpenDialog(null);
	        
	        if(returnVal == JFileChooser.APPROVE_OPTION) 
	        {
	        	BVBSettings.lastDir = chooser.getSelectedFile().getParent();
	            Prefs.set( "BVB.lastDir",  BVBSettings.lastDir );
	            finalURI = chooser.getSelectedFile().getAbsolutePath();
	            
	            // Revert style to standard black and plain text before populating field
	            setNormalState();
	            jlFileName.setText( finalURI );
	            jlFileName.setCaretPosition( 0 );
	            updateWindow();
	        }
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.7;
		pFileSelect.add(jlFileName, gbc);
		gbc.gridx ++;
		gbc.weightx = 0.01;
		gbc.fill = GridBagConstraints.NONE;
		pFileSelect.add(butSelectFile, gbc);
		
		///FINAL PANEL
		pURIDialog = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		pURIDialog.add(pFileSelect, gbc);
		pURIDialog.setPreferredSize( new Dimension(panelWidth,panelHeight) );

    }
    
	public void show()
	{
		  JOptionPane.showOptionDialog(
                  null, 
                  pURIDialog, 
                  "Open OME-Zarr", 
                  JOptionPane.YES_NO_OPTION, 
                  JOptionPane.PLAIN_MESSAGE, 
                  null, 
                  new Object[]{butOk, butCancel}, 
                  butOk);
	}
	
	private void setPlaceholderState() {
		jlFileName.setText(PLACEHOLDER);
		jlFileName.setFont(jlFileName.getFont().deriveFont(Font.ITALIC));
		jlFileName.setForeground(PLACEHOLDER_COLOR);
    }

    private boolean isPlaceholderActive() {
		return jlFileName.getText().equals(PLACEHOLDER) && jlFileName.getForeground().equals(PLACEHOLDER_COLOR);
    }
    
    private void setNormalState() {
		jlFileName.setFont(jlFileName.getFont().deriveFont(Font.PLAIN));
		jlFileName.setForeground(DEFAULT_COLOR);
    }
    
    protected JOptionPane getOptionPane(JComponent parent) {
        JOptionPane pane = null;
        if (!(parent instanceof JOptionPane)) {
            pane = getOptionPane((JComponent)parent.getParent());
        } else {
            pane = (JOptionPane) parent;
        }
        return pane;
    }
    
	void updateWindow()
	{
		if (finalURI != null)
		{
			butOk.setEnabled( true );
		}
	}
}
