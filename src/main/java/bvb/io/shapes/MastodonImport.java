package bvb.io.shapes;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import ij.IJ;

public class MastodonImport extends SwingWorker<Void, String> 
{
	public File filein = null;
	int nCount = 0;
	public boolean bSpotsRead = false;
	
	@Override
    protected void process(List<String> chunks) 
	{
		String message = chunks.get( chunks.size() - 1 );
		if(message.startsWith( "Progress " ))
		{
			IJ.showProgress(Double.parseDouble( message.substring( 9, message.length() )));
		}
    }
	
	@Override
	protected Void doInBackground() throws Exception
	{
		//finish later
//        MamutProject project = MamutProjectIO.load(filein);
//        Model model = new Model();
//        
//        MamutModelExternalIO.read(model, project);
//        ModelGraph graph = model.getGraph();

		return null;
	}
	
    @Override
    public void done() 
    {
    	try
		{
			get();
		}
		catch ( InterruptedException | ExecutionException exc )
		{
			exc.printStackTrace();
		}
     	catch (Exception e)
     	{
     		System.err.println("Error spots import: " + e.toString() );
     	}
    	
    	IJ.log("Loaded " + nCount +" points from " + filein.getName());
    	IJ.showProgress( 1.0 );
    }

}
