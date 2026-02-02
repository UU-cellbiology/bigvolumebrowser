package bvb.animation;

import java.awt.BorderLayout;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.gui.NumberField;
import ij.IJ;
import ij.Prefs;

public class AnimationPanel extends JPanel implements ChangeListener
													
{
	final BigVolumeBrowser bvb;
	
	final JButton butRecord;
	final JButton butPlayStop;
	final JButton butSettings;
	final JSlider timeSlider;
	
	final JButton butAdd;
	final JButton butReplace;
	final JButton butEdit;
	final JButton butDelete;
	final JButton butSave;
	final JButton butLoad;
	
	//keyFrame list
	final public DefaultListModel<KeyFrame> listModel; 
	
	final public JList<KeyFrame> jlist;
	
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
	AnimationPlayer player;
	
	ImageIcon tabIconRecord;
	
	ImageIcon tabIconPlay;
	
	ImageIcon tabIconStop;
	
	float fPlaySpeedFactor  = 1.0f ;
	
	boolean bPlayerBackForth = Prefs.get("BVB.bPlayerBackForth", false);
	
	boolean bUpdateSlider = true;
	
	/** keyframe render **/
	//AnimationRender render;
	
	int nRenderFPS = (int)Prefs.get("BVB.nRenderFPS", 24.0);
	
	int nRenderWidth = (int)Prefs.get("BVB.nRenderWidth", 1280);
	
	int nRenderHeight = (int)Prefs.get("BVB.nRenderHeight", 720);
	
	boolean bRenderMultiBox =  Prefs.get("BVB.bRenderMultiBox", false);
	
	boolean bRenderScaleBar =  Prefs.get("BVB.bRenderScaleBar", false);
	
	int nRenderFrameTimeLimit = (int)Prefs.get("BVB.nRenderFrameTimeLimit", 60);
	
	String sRenderSavePath = null;
	
	final AnimationPanelDialogs dialogsAnim;

	public AnimationPanel(final BigVolumeBrowser bvb_)
	{
		this.bvb = bvb_;
		
		dialogsAnim = new AnimationPanelDialogs(bvb, this);
		
		int nInitialTotalTime = 5;
		
		listModel = new  DefaultListModel<>();
		jlist = new JList<>(listModel);
		
		kfAnim = new KeyFrameAnimation(listModel);
		kfAnim.setTotalTime( nInitialTotalTime );
	
		this.player = null;
		
		JPanel panAnimTools = new JPanel(new GridBagLayout());  
		//panAnimTools.setBorder(new PanelTitle(" Animation "));
		
		int nButtonSize = 40;		
		GridBagConstraints cr = new GridBagConstraints();

		
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
				
		icon_path = this.getClass().getResource(BVBSettings.sIconPath + BVBSettings.sUITheme + "settings.png");
		ImageIcon tabIcon = new ImageIcon(icon_path);
		butSettings = new JButton(tabIcon);
		butSettings.setToolTipText("Settings");
		butSettings.setPreferredSize(new Dimension(nButtonSize, nButtonSize));
		
		butPlayStop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (SwingUtilities.isRightMouseButton(evt))
				{
					dialogsAnim.dialPlayerSettings();
				} 
			}
		});	
				
		cr.gridx = 0;
		cr.gridy = 0;
		panAnimTools.add(butRecord,cr);
		
		cr.gridx++;
		panAnimTools.add(butPlayStop,cr);
		
		cr.gridx++;
		JSeparator sp = new JSeparator(SwingConstants.VERTICAL);
		sp.setPreferredSize(new Dimension((int) (nButtonSize*0.5),nButtonSize));
		panAnimTools.add(sp,cr);
		
		//filler
		cr.gridx++;
		cr.weightx = 0.01;
		panAnimTools.add(new JLabel(), cr);
		cr.gridx++;
		cr.weightx = 0.0;
		panAnimTools.add(butSettings,cr);
		
		
		JPanel panAnimPlot = new JPanel(new GridBagLayout());
		//panAnimPlot.setBorder(new PanelTitle(" Key Frames "));
		
		JPanel sliderPanel = new JPanel(new BorderLayout());
		//sliderPanel.setPreferredSize(new Dimension(50, 1250));
		
		timeSlider = new JSlider(SwingConstants.VERTICAL,0,tsSpan,1);
		
		timeSlider.setInverted( true );
		setSliderTotalTime();
		timeSlider.setValue(0);
		
		timeSlider.setPaintTicks(true);
		timeSlider.setPaintLabels(true);
		timeSlider.addChangeListener( this );

		sliderPanel.add(timeSlider);
		
		cr = new GridBagConstraints();
		cr.gridx = 0;
		cr.gridy = 0;
		cr.gridheight = 7;

		cr.fill  = GridBagConstraints.BOTH;
		cr.weighty = 0.99;
		

		keyMarks = new DrawKeyPoints();
		keyMarks.setMinimumSize( new Dimension(30,250));
	    keyMarks.setPreferredSize( new Dimension(30,250));
		//keyMarks.setBorder(new PanelTitle(" Keys"));
		
		panAnimPlot.add( keyMarks,cr );
		cr.gridx++;
		panAnimPlot.add( sliderPanel,cr );
		
		cr.gridx++;
		///RoiLIST and buttons

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
	
		cr.weightx = 0.5;
		panAnimPlot.add(listScroller,cr);
		
		//BUTTONS
		cr = new GridBagConstraints();
		cr.gridy = 0;
		cr.gridx = 3;
		cr.fill = GridBagConstraints.NONE;
		
		butAdd = new JButton("Add");
		panAnimPlot.add( butAdd, cr );
		
		cr.gridy++;
		butReplace = new JButton("Replace");
		panAnimPlot.add( butReplace, cr );

		cr.gridy++;
		butEdit = new JButton("Edit");
		panAnimPlot.add( butEdit, cr );		
		
		cr.gridy++;
		butDelete = new JButton("Delete");
		panAnimPlot.add( butDelete, cr );

		cr.gridy++;
		butSave = new JButton("Save");
		panAnimPlot.add( butSave, cr );
		
		cr.gridy++;
		butLoad = new JButton("Load");
		panAnimPlot.add( butLoad, cr );
		
		cr.gridy++;
		butUpdateSlider = new JToggleButton("<html><center>Slider<br>update</center></html>");
		butUpdateSlider.setSelected( true );

		panAnimPlot.add( butUpdateSlider, cr );
		
		// Blank/filler component
		cr.gridy++;
		cr.weightx = 0.01;
		cr.weighty = 0.05;
		panAnimPlot.add(new JLabel(), cr);	
		
		// LISTENERS ASSIGNMENT
		butRecord.addActionListener( (e) -> recordRenderButtonAction() );
		butPlayStop.addActionListener( (e) -> playStopButtonAction() );
		butSettings.addActionListener( (e) -> dialogsAnim.dialPanelSettings());
		butAdd.addActionListener((e) -> addCurrentKeyFrame());
		butReplace.addActionListener((e) -> replaceSelectedKeyFrame());
		butEdit.addActionListener((e) -> editSelectedKeyFrame());
		butDelete.addActionListener( (e)-> deleteSelectedKeyFrame());
		butSave.addActionListener((e) -> dialStorylineSave());
		butLoad.addActionListener((e) -> dialStorylineLoad());
		butUpdateSlider.addActionListener((e)-> {bUpdateSlider = butUpdateSlider.isSelected();});
		
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

		
		JPanel panTotTime = new JPanel(new GridBagLayout());
		//panTotTime.setBorder(new PanelTitle(""));
		cr = new GridBagConstraints();
		cr.gridx = 0;
		cr.gridy = 0;
		panTotTime.add(new JLabel("Total time (s)"),cr);
		cr.gridx++;
		nfTotalTime = new NumberField(4);
		nfTotalTime.setIntegersOnly( true );
		nfTotalTime.setText(Integer.toString( (int)Math.ceil( kfAnim.getTotalTime())));
		nfTotalTime.setMinimumSize(nfTotalTime.getPreferredSize());
		nfTotalTime.addListener((t) -> setNewTotalTime(t) );
		
		panTotTime.add(nfTotalTime,cr);
			
		//put all panels together
		cr = new GridBagConstraints();
		setLayout(new GridBagLayout());
		cr.insets = new Insets(4,4,2,2);
		cr.gridx = 0;
		cr.gridy = 0;
		cr.fill = GridBagConstraints.HORIZONTAL;

		//TOP BUTTONS MENU 
		add(panAnimTools,cr);
		
		//KEYFRAMES list
		cr.gridy++;
		cr.weighty = 0.99;
		cr.fill = GridBagConstraints.BOTH;
		add(panAnimPlot,cr);

		cr.gridy++;
		cr.weighty = 0.0;
		cr.fill = GridBagConstraints.BOTH;
		add(panTotTime,cr);
		
		
		// Blank/filler component
		cr.gridy++;
		cr.weightx = 0.01;
		cr.weighty = 0.01;
		add(new JLabel(), cr);    
	}
	
	void runRender()
	{
//		render = new AnimationRender< >(bt, this);
//		bt.bInputLock = true;
//		bt.setLockMode(true);
//		render.addPropertyChangeListener( bt.btPanel );
//		
//		butRecord.setEnabled( true );
//		butRecord.setIcon( tabIconStop );
//		butRecord.setToolTipText( "Stop render" );
//		render.butRecord = butRecord;
//		render.tabIconRecord = tabIconRecord; 
//		render.execute();
	}
	
	void runPlayer()
	{
		player = new AnimationPlayer(bvb, this);
		bvb.setInputLock( true );		
		butPlayStop.setEnabled( true );
		butPlayStop.setIcon( tabIconStop );
		butPlayStop.setToolTipText( "Stop playing" );
		player.butPlayStop = butPlayStop;
		player.tabIconPlay = tabIconPlay; 
		player.execute();
	}
	
	/** run or stop player **/
	void playStopButtonAction()
	{
		if(listModel.size() > 0)
		{
			if( !bvb.getInputLock() )
			{
				runPlayer();
			}
			else
			{
				if(bvb.getInputLock() && butPlayStop.isEnabled() && player != null 
						&& !player.isCancelled() && !player.isDone())
				{
					player.cancel( false );
				}
			}
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
//				if(dialogsAnim.dialRenderSettings())
//				{
//					runRender();
//				}
			}
			else
			{
//				if(bt.bInputLock && butRecord.isEnabled() && render!=null && !render.isCancelled() && !render.isDone())
//				{
//					render.cancel( false );
//				}
			}
		}
		else
		{
			IJ.showStatus( "cannot render: add at least one key frame." );
		}
	}
	
	void addCurrentKeyFrame()
	{
		float nTimeMovie =  ((float)timeSlider.getValue() / (float)(tsSpan)) * kfAnim.getTotalTime();
		KeyFrame newKeyFrame = new KeyFrame(SceneView.getCurrentSceneView( bvb.bvvViewer ), nTimeMovie);
		
		if(listModel.size() == 0)
		{
			listModel.addElement(newKeyFrame);
		}
		else
		{
			boolean bAdded = false;
			for(int i = 0; i < listModel.size(); i++)
			{
				if(listModel.get( i ).fMovieTimePoint > nTimeMovie)
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
		
	}
	
	void replaceSelectedKeyFrame()
	{
		int nInd = jlist.getSelectedIndex();
		if(nInd >= 0)
		{
			float nTimeMovie = listModel.get( nInd ).fMovieTimePoint;
			String sName = listModel.get( nInd ).name;
			KeyFrame newKeyFrame = new KeyFrame(SceneView.getCurrentSceneView( bvb.bvvViewer ), nTimeMovie);
			newKeyFrame.nIndex = nInd;
			newKeyFrame.name = sName;
			listModel.set( nInd, newKeyFrame );
			kfAnim.updateTransitionTimeline();
		}
	}
	
	void editSelectedKeyFrame()
	{
		final int nInd = jlist.getSelectedIndex();
		if(nInd >= 0)
		{
			if(dialogsAnim.dialEditKeyFrame(nInd))
			{
				updateKeyIndices();
				updateKeyMarks();
				kfAnim.updateTransitionTimeline();
			}
		}
	}
	
	void deleteSelectedKeyFrame()
	{
		final int nInd = jlist.getSelectedIndex();
		if(nInd >= 0)
		{
			listModel.remove( nInd );
			updateKeyIndices();
			updateKeyMarks();
			kfAnim.updateTransitionTimeline();
		}
	}

	/** total time of the animation changed **/
	public void setNewTotalTime(final double nNewTime_)
	{
		final int nNewTime = ( int ) Math.round( Math.abs( nNewTime_ ) );
		int nOldTime = kfAnim.nTotalTime;
				
		if(listModel.size() > 0)
		{		
			if(!dialogsAnim.dialChangeTotalTime(nNewTime>=nOldTime))
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
			return ( int ) ( Math.pow( 10, nDigits-1 ) );
		if(firstDigit < 4)
			return ( int ) ( 2 * Math.pow( 10, nDigits-1 ) );
		if(firstDigit <= 5)
			return ( int ) ( 5 * Math.pow( 10, nDigits-1 ) );
		return ( int ) ( Math.pow( 10, nDigits ) );
		
	}
	
	/** updates timeline display **/
	public void updateKeyMarks()
	{
		ArrayList<Float> keyPoints = new ArrayList<>();
		for (int i = 0; i < listModel.size(); i++)
		{
			keyPoints.add( listModel.get( i ).fMovieTimePoint/ kfAnim.getTotalTime());
		}
		keyMarks.setkeyPoints( keyPoints );
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
		
		public void setkeyPoints(ArrayList<Float> keyPoints_)
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
	        	//int h = Math.round( nMaxAscent+keyPoints.get( i ).floatValue()*nTotalRange);
	        	g.drawString("["+Integer.toString( i )+"]", 0, 
	        			Math.round( nMaxAscent + keyPoints.get( i ).floatValue() * nTotalRange));
	        	//g.drawString( "top",  0, nMaxAscent);
	        	//g.drawString( "bottom",  0, (int)(bounds.getHeight())-nMaxDescent);
	        }
	        bLocked = false;

	    }
	}

	public void updateScene()
	{			
		if(listModel.size() > 1)
		{
			float fTimePoint = (kfAnim.getTotalTime()) * (timeSlider.getValue() / (float)tsSpan);
			SceneView.setSceneView( bvb.bvvViewer, kfAnim.getSceneView( fTimePoint ) );
		}
	}
	
	
	@Override
	public void stateChanged( ChangeEvent e )
	{
		if(e.getSource() == timeSlider)
		{
			if(bUpdateSlider)
			{
				updateScene();
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
    		final ArrayList<KeyFrame> storeKF = new ArrayList<>(listModel.size());
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
	
	void dialStorylineSave()
	{
//		if(listModel.size() > 0)
//		{
//			String filename;
//			
//			filename = bt.btData.sFileNameFullImg + "_btstory";
//			SaveDialog sd = new SaveDialog("Save storyline ", filename, ".csv");
//	        String path = sd.getDirectory();
//	        if (path == null)
//	        	return;
//	        filename = path + sd.getFileName();
//	        
//	        bt.setLockMode(true);
//	        bt.bInputLock = true;
//	        StorylineSave<T> stSave  = new StorylineSave<>(bt, this);
//	        stSave.saveAnimation( filename );
//	        bt.setLockMode(false);
//	        bt.bInputLock = false;
//	        
//		}
	}
	void dialStorylineLoad()
	{
//		String filename;
//		
//		OpenDialog openDial = new OpenDialog("Load BigTrace storyline",bt.btData.lastDir, "*.csv");
//		
//        String path = openDial.getDirectory();
//        if (path==null)
//        	return;
//        bt.btData.lastDir = path;
//        Prefs.set( "BigTrace.lastDir", bt.btData.lastDir );
//        
//        filename = path + openDial.getFileName();	
//        bt.setLockMode(true);
//        bt.bInputLock = true;
//        StorylineLoad<T> stLoad  = new StorylineLoad<>(bt, this);
//        stLoad.loadAnimation( filename );
//        bt.setLockMode(false);
//        bt.bInputLock = false;
	}

}
