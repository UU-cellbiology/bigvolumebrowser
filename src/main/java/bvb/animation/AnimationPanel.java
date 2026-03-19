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
package bvb.animation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bvb.animation.utils.Timeline;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.NumberField;
import bvb.io.dto.BVBObjectsDTO;
import bvb.io.dto.StoryDTO;
import ij.IJ;
import ij.Prefs;

public class AnimationPanel extends JPanel implements ChangeListener												
{
	final BigVolumeBrowser bvb;
	
	final JButton butRecord;
	final JButton butPlayStop;
	final JButton butSnapshot;
	final JButton butSettings;
	final JSlider timeSlider;
	
	final JButton butAdd;
	final JButton butReplace;
	final JButton butEdit;
	final JButton butDelete;
	final JButton butSave;
	final JButton butLoad;
	
	//keyFrame list
	final public DefaultListModel<KeyFrameScene> listModel; 
	
	final public JList<KeyFrameScene> jlist;
	
	final JScrollPane listScroller;
	
	final DrawKeyPoints keyMarks;
	
	final public NumberField nfTotalTime;
		
	public static final int ANIMTIME_START = 0, ANIMTIME_END = 1, ANIMTIME_STRETCH = 2;
	
	int nChangeTotalTimeMode = (int)Prefs.get("BVB.nChangeTotalTimeMode", ANIMTIME_END);
	
	final public KeyFrameAnimation kfAnim;
	
	final JToggleButton butUpdateSlider;

	/** slider span in slider units**/
	int tsSpan = 100;
	
	/** play preview **/
	final AnimationPlayer player;
	
	ImageIcon tabIconRecord;
	
	public ImageIcon tabIconPlay;
	
	public ImageIcon tabIconStop;
	
	boolean bPlayerBackForth = Prefs.get("BVB.bPlayerBackForth", false);
	
	boolean bUpdateSlider = true;
	
	/** keyframe render **/
	AnimationRender render;
	
	int nRenderFPS = (int)Prefs.get("BVB.nRenderFPS", 30.0);
	
	int nRenderWidth = (int)Prefs.get("BVB.nRenderWidth", 1280);
	
	int nRenderHeight = (int)Prefs.get("BVB.nRenderHeight", 720);
	
	boolean bRenderCurrentWindowSize = Prefs.get("BVB.bRenderCurrentWindowSize", false);
	
	boolean bRenderMultiBox =  Prefs.get("BVB.bRenderMultiBox", false);
	
	boolean bRenderScaleBar =  Prefs.get("BVB.bRenderScaleBar", false);
	
	boolean bRenderAxesGizmo =  Prefs.get("BVB.bRenderAxesGizmo", false);
	
	int nRenderFrameTimeLimit = (int)Prefs.get("BVB.nRenderFrameTimeLimit", 60);
	
	String sRenderSavePath = null;
	
	final AnimationPanelDialogs dialogsAnim;
	
	public Timeline timeline;
	
	final ArrayList<Component> allComp = new ArrayList<>();
	
	final JPanel glassPanel = new JPanel();
	
	boolean bShowAnimationWarning = Prefs.get( "BVB.bShowAnimationWarning", true );

	public AnimationPanel(final BigVolumeBrowser bvb_)
	{
		this.bvb = bvb_;
	
		dialogsAnim = new AnimationPanelDialogs(bvb, this);
		
		int nInitialTotalTime = 5;
		
		listModel = new  DefaultListModel<>();
		jlist = new JList<>(listModel);
		
		kfAnim = new KeyFrameAnimation(listModel);
		kfAnim.setTotalTime( nInitialTotalTime );
		
		glassPanel.addMouseListener(new MouseAdapter() {});
	
		this.player = new AnimationPlayer(bvb, this);
		
		JPanel panAnimTools = new JPanel(new GridBagLayout());  
		//panAnimTools.setBorder(new PanelTitle(" Animation "));
		
		int nButtonSize = 40;		
		GridBagConstraints gbc = new GridBagConstraints();
		
		URL icon_path = this.getClass().getResource(BVBSettings.sIconPath + "render.png");
		tabIconRecord = new ImageIcon(icon_path);
		butRecord = new JButton(tabIconRecord);
		butRecord.setToolTipText("Render");
		butRecord.setPreferredSize(new Dimension(nButtonSize , nButtonSize ));
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "play.png");
		tabIconPlay = new ImageIcon(icon_path);		
		butPlayStop = new JButton(tabIconPlay);
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "cancel.png");
		tabIconStop = new ImageIcon(icon_path);		
		butPlayStop.setToolTipText("Play");
		butPlayStop.setPreferredSize(new Dimension(nButtonSize , nButtonSize ));
		
		butPlayStop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (SwingUtilities.isRightMouseButton(evt))
				{
					dialogsAnim.dialPlayerSettings();
				} 
			}
		});		
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "snapshot.png");
		ImageIcon tabIcon = new ImageIcon(icon_path);
		butSnapshot = new JButton(tabIcon);
		butSnapshot.setToolTipText("Make a snapshot\n(shortcut Ctrl + S)");
		butSnapshot.setPreferredSize(new Dimension(nButtonSize, nButtonSize));
		
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "settings.png");
		tabIcon = new ImageIcon(icon_path);
		butSettings = new JButton(tabIcon);
		butSettings.setToolTipText("Settings");
		butSettings.setPreferredSize(new Dimension(nButtonSize, nButtonSize));
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		panAnimTools.add(butRecord,gbc);
		
		gbc.gridx++;
		panAnimTools.add(butPlayStop,gbc);
		gbc.gridx++;
		panAnimTools.add(butSnapshot,gbc);
		
		JPanel panTotTime = new JPanel(new GridBagLayout());
		//panTotTime.setBorder(new PanelTitle(""));
		GridBagConstraints cr = new GridBagConstraints();
		cr.gridx = 0;
		cr.gridy = 0;
		panTotTime.add(new JLabel("Total time (s)"),cr);
		cr.gridx++;
		nfTotalTime = new NumberField(4);
		nfTotalTime.setIntegersOnly( true );
		nfTotalTime.setText(Integer.toString( (int)Math.ceil( kfAnim.getTotalTime())));
		nfTotalTime.setMinimumSize(nfTotalTime.getPreferredSize());
		nfTotalTime.addListener((t) -> setNewTotalTime(t) );
		
		panTotTime.add(nfTotalTime, cr);
				
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(0,10,0,10);

		panAnimTools.add(panTotTime,gbc);
		
		gbc.insets = new Insets(0,0,0,0);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;

		gbc.gridx++;
		gbc.weightx = 0.0;
		panAnimTools.add(butSettings,gbc);
		
		
		JPanel panAnimPlot = new JPanel(new GridBagLayout());
		//panAnimPlot.setBorder(new PanelTitle(" Key Frames "));
		
		JPanel sliderPanel = new JPanel(new BorderLayout());
		//sliderPanel.setPreferredSize(new Dimension(50, 1250));
		
		timeSlider = new JSlider(SwingConstants.VERTICAL, 0, tsSpan, 1);
		
		timeSlider.setInverted( true );
		setSliderTotalTime();
		timeSlider.setValue( 0 );
		
		timeSlider.setPaintTicks(true);
		timeSlider.setPaintLabels(true);
		timeSlider.addChangeListener( this );

		sliderPanel.add(timeSlider);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 7;

		gbc.fill  = GridBagConstraints.BOTH;
		gbc.weighty = 0.99;
		

		keyMarks = new DrawKeyPoints();
		keyMarks.setMinimumSize( new Dimension(30,250));
	    keyMarks.setPreferredSize( new Dimension(30,250));
		//keyMarks.setBorder(new PanelTitle(" Keys"));
		
		panAnimPlot.add( keyMarks,gbc );
		gbc.gridx++;
		panAnimPlot.add( sliderPanel,gbc );
		
		gbc.gridx++;
		///keyframes list and buttons

		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlist.setLayoutOrientation(JList.VERTICAL);
		jlist.setVisibleRowCount(-1);
		jlist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) 
				{
					// Double-click detected
					SceneView.setSceneView( bvb.bvvViewer, jlist.getSelectedValue().getSceneView());
					int nPos = Math.round( tsSpan*(jlist.getSelectedValue().fMovieTimePoint/kfAnim.getTotalTime()));
					timeSlider.setValue( nPos);			
				} 
				if (SwingUtilities.isRightMouseButton(evt))
				{
					editSelectedKeyFrame();
				}
			}
		});		
		
		listScroller = new JScrollPane(jlist);
		listScroller.setPreferredSize(new Dimension(170, 250));	
	
		gbc.weightx = 0.5;
		panAnimPlot.add(listScroller,gbc);
		
		//BUTTONS
		gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.gridx = 3;
		gbc.fill = GridBagConstraints.NONE;
		
		butAdd = new JButton("Add");
		panAnimPlot.add( butAdd, gbc );
		
		gbc.gridy++;
		butReplace = new JButton("Replace");
		panAnimPlot.add( butReplace, gbc );

		gbc.gridy++;
		butEdit = new JButton("Edit");
		panAnimPlot.add( butEdit, gbc );		
		
		gbc.gridy++;
		butDelete = new JButton("Delete");
		panAnimPlot.add( butDelete, gbc );

		gbc.gridy++;
		butSave = new JButton("Save");
		panAnimPlot.add( butSave, gbc );
		
		gbc.gridy++;
		butLoad = new JButton("Load");
		panAnimPlot.add( butLoad, gbc );
		
		gbc.gridy++;
		butUpdateSlider = new JToggleButton("<html><center>Slider<br>update</center></html>");
		butUpdateSlider.setSelected( true );

		panAnimPlot.add( butUpdateSlider, gbc );
		
		allComp.add( butUpdateSlider );
		allComp.add( butLoad );
		allComp.add( butSave );
		allComp.add( butDelete );
		allComp.add( butEdit );
		allComp.add( butReplace );
		allComp.add( butAdd );
	
		allComp.add( listScroller );
		allComp.add( timeSlider );
		allComp.add( butPlayStop );
		allComp.add( butRecord );
		allComp.add( butSettings );
		allComp.add( nfTotalTime);
		
		// Blank/filler component
		gbc.gridy++;
		gbc.weightx = 0.01;
		gbc.weighty = 0.05;
		panAnimPlot.add(new JLabel(), gbc);	
		
		//put all panels together
		gbc = new GridBagConstraints();
		setLayout(new GridBagLayout());
		gbc.insets = new Insets(4,4,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		//TOP BUTTONS MENU 
		add(panAnimTools,gbc);
		
		//KEYFRAMES list
		gbc.gridy++;
		gbc.weighty = 0.99;
		gbc.fill = GridBagConstraints.BOTH;
		add(panAnimPlot,gbc);		
		
		// Blank/filler component
		gbc.gridy++;
		gbc.weightx = 0.01;
		gbc.weighty = 0.01;
		add(new JLabel(), gbc);    
		
		// a solution for now
		final Dimension butDim = butReplace.getPreferredSize();
		butAdd.setMinimumSize(butDim);
		butAdd.setPreferredSize(butDim);
		butReplace.setMinimumSize(butDim); 
		butReplace.setPreferredSize(butDim); 
		butEdit.setMinimumSize(butDim);
		butEdit.setPreferredSize(butDim); 
		butDelete.setMinimumSize(butDim);
		butDelete.setPreferredSize(butDim); 
		butSave.setMinimumSize(butDim);
		butSave.setPreferredSize(butDim); 		
		butLoad.setMinimumSize(butDim);
		butLoad.setPreferredSize(butDim); 
		
		// LISTENERS ASSIGNMENT
		butRecord.addActionListener  ( (e) -> recordRenderButtonAction() );
		butPlayStop.addActionListener( (e) -> playStopButtonAction() );
		butSnapshot.addActionListener( (e) -> makeSnapshot() );
		butSettings.addActionListener( (e) -> dialogsAnim.dialPanelSettings());
		butAdd.addActionListener     ( (e) -> addCurrentKeyFrame());
		butReplace.addActionListener ( (e) -> replaceSelectedKeyFrame());
		butEdit.addActionListener    ( (e) -> editSelectedKeyFrame());
		butDelete.addActionListener  ( (e) -> deleteSelectedKeyFrame());
		butSave.addActionListener    ( (e) -> dialogsAnim.dialStorylineSave());
		butLoad.addActionListener    ( (e) -> dialogsAnim.dialStorylineLoad());
		butUpdateSlider.addActionListener((e)-> {bUpdateSlider = butUpdateSlider.isSelected();});
		
	}
	
	public void initTimeline()
	{
		timeline = new Timeline(bvb);
	}
	
	void runRender()
	{
		if(player.isPlaying())
			player.stop();
		
		if(bShowAnimationWarning)
			dialogsAnim.showAnimationModeWarning();
		
		render = new AnimationRender(bvb, this);
		
		render.addPropertyChangeListener( (evt) ->
		{
			if ("progress".equals(evt.getPropertyName())) {
                IJ.showProgress( (Integer)evt.getNewValue(), 100);
            }
		});
		
		initGlassPanel(render);

		render.glass = glassPanel;
		IJ.log( "BVB: starting rendering to " + sRenderSavePath);
		IJ.log( "BVB: press Esc to interrupt");
		
		render.execute();
	}
	
	public void makeSnapshot()
	{
		if(player.isPlaying())
			player.stop();	
		
		final AnimationSnapshot snapshot = new AnimationSnapshot(bvb, this);
		
		initGlassPanel( snapshot );
		snapshot.glass = glassPanel;
		snapshot.execute();
	}
	
	/** run or stop player **/
	void playStopButtonAction()
	{
		if(listModel.size() > 0)
		{
			if(!player.isPlaying())
				player.play();
			else
				player.stop();
		}
		else
		{
			IJ.showStatus( "cannot play: add at least one key frame." );
		}
	}
	
	void recordRenderButtonAction()
	{
		if(listModel.size() > 0)
		{
			if(!bvb.getInputLock() )
			{
				if(dialogsAnim.dialRenderSettings())
				{
					runRender();
				}
			}
			else
			{
				//bt.bInputLock
				if(render != null )
				{
					if( !render.isCancelled() && !render.isDone())
					{
						render.cancel( false );
					}
				}
			}
		}
		else
		{
			IJ.showStatus( "cannot render: add at least one key frame." );
		}
	}
	
	void initGlassPanel (final SwingWorker< ?, ? > worker )
	{
		glassPanel.setOpaque(false);
		KeyListener[] kl = glassPanel.getKeyListeners();
		for(int i = 0; i< kl.length; i++)
			glassPanel.removeKeyListener( kl[i] );
		
		glassPanel.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed( KeyEvent e )
			{
				
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					worker.cancel( true );
					IJ.log( "BVB: Animation rendering was interrupted." );
                }	
			}

			@Override
			public void keyReleased( KeyEvent e )
			{			
			}

			@Override
			public void keyTyped( KeyEvent e )
			{
				
			}});
	}
	
	void addCurrentKeyFrame()
	{
		float fTimeMovie =  ((float)timeSlider.getValue() / (float)(tsSpan)) * kfAnim.getTotalTime();
		KeyFrameScene newKeyFrame = new KeyFrameScene(SceneView.getCurrentSceneView( bvb.bvvViewer ), 
				fTimeMovie, Timeline.easingRegistry.get( "linear" ));
		
		if(listModel.size() == 0)
		{
			listModel.addElement(newKeyFrame);
		}
		else
		{
			boolean bAdded = false;
			for(int i = 0; i < listModel.size(); i++)
			{
				if(listModel.get( i ).fMovieTimePoint > fTimeMovie)
				{
					listModel.add(i,newKeyFrame);
					bAdded = true;
					break;
				}
			}
			if(!bAdded)
			{
				listModel.addElement(newKeyFrame);
			}
		}
		updateKeyIndices();
		updateKeyMarks();
	
		kfAnim.updateTransitionTimeline();
	
	    timeline.addKeyframeBVB(bvb, newKeyFrame);
	}
	
	void replaceSelectedKeyFrame()
	{
		int nInd = jlist.getSelectedIndex();
		if(nInd >= 0)
		{
			final KeyFrameScene keyFrameScene = listModel.get( nInd );
			keyFrameScene.setScene( SceneView.getCurrentSceneView( bvb.bvvViewer ) );
			kfAnim.updateTransitionTimeline();
			timeline.deleteKeyframe( keyFrameScene );
			timeline.addKeyframeBVB(bvb, keyFrameScene);
		}
	}
	
	void editSelectedKeyFrame()
	{
		final int nInd = jlist.getSelectedIndex();
		if(nInd >= 0)
		{
			final KeyFrameScene editedKeyFrameScene = dialogsAnim.dialEditKeyFrame(nInd);
			
			//timepoint changed
			if(editedKeyFrameScene != null)
			{
				updateKeyIndices();
				updateKeyMarks();
				kfAnim.updateTransitionTimeline();
				timeline.updateKeyframe( editedKeyFrameScene );
			}
		}
	}
	
	void deleteSelectedKeyFrame()
	{
		final int nInd = jlist.getSelectedIndex();
		if(nInd >= 0)
		{
			timeline.deleteKeyframe( listModel.get( nInd ) );
			listModel.remove( nInd );
			updateKeyIndices();
			updateKeyMarks();
			kfAnim.updateTransitionTimeline();
		}
	}

	/** total time of the animation changed **/
	public void setNewTotalTime(final double nNewTime_)
	{
		int nNewTime = ( int ) Math.round( Math.abs( nNewTime_ ) );
		if(nNewTime_ < 1)
		{
			if(nNewTime < 1 )
				nNewTime = 1;
		}
		nfTotalTime.setText( Integer.toString( nNewTime ) );

		int nOldTime = kfAnim.nTotalTime;
				
		if(listModel.size() > 0)
		{		
			if(!dialogsAnim.dialChangeTotalTime(nNewTime >= nOldTime))
				return;
			
			kfAnim.setTotalTime(nNewTime);			
			
			setSliderTotalTime();
			
			switch(nChangeTotalTimeMode)
			{
				case ANIMTIME_START:
					for (int i = 0; i < listModel.size(); i++)
					{
						listModel.get( i ).fMovieTimePoint += nNewTime - nOldTime;
					}
					break;
					
				case ANIMTIME_STRETCH:
					for (int i = 0; i < listModel.size(); i++)
					{
						listModel.get( i ).fMovieTimePoint *= (nNewTime/(float)(nOldTime));
					}
					break;
			}
			
			if(nChangeTotalTimeMode != ANIMTIME_STRETCH)
			{
				//check that keyframes are still in range
				for (int i = 0; i < listModel.size(); i++)
				{
					if(listModel.get( i ).fMovieTimePoint > nNewTime)
					{
						listModel.get( i ).fMovieTimePoint = nNewTime;
					}
					if(listModel.get( i ).fMovieTimePoint < 0)
					{
						listModel.get( i ).fMovieTimePoint = 0;
					}
				}
			}
			updateKeyMarks();
			kfAnim.updateTransitionTimeline();
		}
		else
		{
			kfAnim.setTotalTime(nNewTime);						
			setSliderTotalTime();
		}
	}
	
	public void setSliderTotalTime()
	{
		
		int nTickTime = getTickTime();
		
		if( kfAnim.getTotalTime() < 100)
			tsSpan = kfAnim.getTotalTime() * 10;
		else
			tsSpan = kfAnim.getTotalTime();
		
		timeSlider.setMaximum( tsSpan );
		
		int oneTick = Math.round( nTickTime * tsSpan/kfAnim.getTotalTime() );
		
		timeSlider.setMajorTickSpacing(oneTick);

		Hashtable< Integer, JLabel > labelTable = new Hashtable<>();
		
		for(int i = 0; i <= kfAnim.getTotalTime(); i += nTickTime)
		{
			int kk = i * tsSpan / kfAnim.getTotalTime();
			labelTable.put( new Integer(kk ), new JLabel(Integer.toString( i )) );	
		}
		timeSlider.setLabelTable( labelTable );
	}
	
	int getTickTime()
	{
		int nTickTime = ( int ) Math.ceil( kfAnim.getTotalTime() / 10. );
		int nDigits = Integer.toString( nTickTime ).length();
		int firstDigit = Integer.parseInt(Integer.toString(nTickTime).substring(0, 1));
		
		if(firstDigit == 1)
			return ( int ) ( Math.pow( 10, nDigits - 1 ) );
		if(firstDigit < 4)
			return ( int ) ( 2 * Math.pow( 10, nDigits - 1 ) );
		if(firstDigit <= 5)
			return ( int ) ( 5 * Math.pow( 10, nDigits - 1 ) );
		return ( int ) ( Math.pow( 10, nDigits ) );
		
	}
	
	/** updates scene timeline display **/
	public void updateKeyMarks()
	{
		ArrayList<Float> keyPoints = new ArrayList<>();
		for (int i = 0; i < listModel.size(); i++)
		{
			keyPoints.add( listModel.get( i ).fMovieTimePoint/ kfAnim.getTotalTime());
		}
		keyMarks.setKeyPoints( keyPoints );
		keyMarks.repaint();
	}
	
	/** updates numbering of keyframes **/
	public void updateKeyIndices()
	{
		for(int i = 0; i < listModel.size(); i++)
		{
			listModel.get( i ).nIndex = i;
		}
		
	}
	
	class DrawKeyPoints extends JPanel
	{
		boolean bLocked = false;
		
		/** values as fraction of total time **/
		ArrayList<Float> keyPoints = new ArrayList<>();
		
		DrawKeyPoints()
		{
			super();
		}
		
		public void setKeyPoints(ArrayList<Float> keyPoints_)
		{
			if(bLocked)
			{
				while (bLocked)
				{
					try
					{
						Thread.sleep( 1 );
					}
					catch ( InterruptedException exc )
					{
						exc.printStackTrace();
					}
				}
			}	
			bLocked = true;
			
			keyPoints = keyPoints_;
			
			bLocked = false;
		}

	    @Override
	    protected void paintComponent(Graphics g)
	    {
	    	
	        super.paintComponent(g);
	        
	        Rectangle bounds = this.getBounds();

	        Graphics2D g2 = (Graphics2D) g;
	        g2.setRenderingHint(
	                RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	        

	        Font currentFont = g.getFont();
	        FontMetrics m = g.getFontMetrics(currentFont);
	        int nMaxAscent = m.getAscent();
	        int nMaxDescent = m.getDescent();
	        int nTotalRange = (int)bounds.getHeight() - nMaxDescent - nMaxAscent;
	        if(bLocked)
	        {
	    		while (bLocked)
	    		{
	    			try
	    			{
	    				Thread.sleep( 1 );
	    			}
	    			catch ( InterruptedException exc )
	    			{
	    				exc.printStackTrace();
	    			}
	    		}
	        }	
	        bLocked = true;
	        for(int i = 0; i < keyPoints.size(); i++)
	        {
	        	g.drawString("["+Integer.toString( i )+"]", 0, 
	        			Math.round( nMaxAscent + keyPoints.get( i ).floatValue() * nTotalRange));
	        }
	        bLocked = false;

	    }
	}

	public synchronized void updateScene(final float fTimePoint)
	{			
		if(listModel.size() > 1)
		{		
			SceneView.setSceneView( bvb.bvvViewer, kfAnim.getSceneView( fTimePoint ) );
			timeline.apply( fTimePoint );	
			bvb.bvbCards.clipPanel.updateGUI();
			bvb.bvbCards.transformPanel.updateGUI();
			bvb.bvbCards.panelShapesProperties.updateGUI();
			bvb.repaintBVV();
		}
	}
	
	
	@Override
	public void stateChanged( ChangeEvent e )
	{
		if(e.getSource() == timeSlider)
		{
			if(bUpdateSlider)
			{
				float fTimePoint = (kfAnim.getTotalTime()) * (timeSlider.getValue() / (float)tsSpan);
				updateScene(fTimePoint);
			}
		}
		
	}
	
	void sortListModel()
	{
		if(listModel.size() > 0)
		{
			final float [][] timeNindex = new float [listModel.size()][2];
			for(int i = 0; i < listModel.size(); i++)
			{
				timeNindex [i][0] = listModel.get( i ).fMovieTimePoint;
				timeNindex [i][1] = i;
			}
    		Arrays.sort(timeNindex, (a, b) -> Float.compare(a[0], b[0]));
    		final ArrayList<KeyFrameScene> storeKF = new ArrayList<>(listModel.size());
    		for(int i = 0; i < listModel.size(); i++)
    		{
    			storeKF.add( listModel.get( (int)timeNindex[i][1] ) );
    		}
    		
    		listModel.clear();
    		for(int i = 0; i < storeKF.size(); i++)
    		{
    			listModel.addElement( storeKF.get( i ) );
    		}
		}
	}

	
	public void restoreStory(final StoryDTO storyDTO)
	{
		listModel.clear();
		setNewTotalTime( storyDTO.keyFrameAnimation.nTotalTime );
    	final Map< String, KeyFrameScene > mapKF = kfAnim.restoreFromDTO( storyDTO.keyFrameAnimation );
    	updateKeyIndices();
		updateKeyMarks();        	
		kfAnim.updateTransitionTimeline();
		setSliderTotalTime();
		timeline.restoreFromDTO( storyDTO.timeline, mapKF );
		
	}
	
	public void checkObjectsPresence(final BVBObjectsDTO dto, String filename)
	{
		for(String hash:dto.presentObjectsNames)
		{
			if(bvb.objectHashStorage.getObjectFromHash( hash ) == null)
			{
				IJ.log( "WARNING: BVB animation timeline, while loading " + filename);
				IJ.log( "Cannot find object " + hash  );
				IJ.log( "It was not loaded or moved/renamed? Can be ignored? Skipping for now."  );
			}
		}
	}
	
	@Override
	public void setEnabled( boolean bEnabled )
	{
		for(final Component nC:allComp)
		{
			nC.setEnabled( bEnabled );
		}
	}

}
