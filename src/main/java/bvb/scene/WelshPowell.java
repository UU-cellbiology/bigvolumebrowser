package bvb.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class WelshPowell
{
	private ArrayList<Vertex> vertices = new ArrayList<>();
	private ArrayList<Graph> graphs = new ArrayList<>();
	
	public WelshPowell(final int nVertexN, final int [] trindices)
	{
		for(int i=0;i<nVertexN;i++)
		{
			vertices.add( new Vertex(i) );
		}
		for(int i=0; i<trindices.length; i+=3)
		{
			vertices.get( trindices[i]).addNeighbor( trindices[i+1] );
			vertices.get( trindices[i]).addNeighbor( trindices[i+2] );
			vertices.get( trindices[i+1]).addNeighbor( trindices[i] );
			vertices.get( trindices[i+1]).addNeighbor( trindices[i+2] );
			vertices.get( trindices[i+2]).addNeighbor( trindices[i] );
			vertices.get( trindices[i+2]).addNeighbor( trindices[i+1] );
			addEdge(trindices[i], trindices[i+1]);
			addEdge(trindices[i+1], trindices[i+2]);
		}
		int nMaxColorN = 0;
		int nMinColorN = 10000;
		double nAver = 0.0;
		for(Vertex v:vertices)
		{
			nAver += v.neighbors.size();
			if(v.neighbors.size()>nMaxColorN)
			{
				nMaxColorN = v.neighbors.size();
			}
			if(v.neighbors.size()<nMinColorN)
			{
				nMinColorN = v.neighbors.size();
			}

		}
		nAver /= vertices.size();
		System.out.println("triangles " + Integer.toString( trindices.length/3 ));
		System.out.println("min color " + Integer.toString( nMinColorN ));
		System.out.println("max color " + Integer.toString( nMaxColorN ));
		System.out.println("aver color " + Double.toString( nAver ));
		System.out.println("graphs total "+Integer.toString(graphs.size()));
	}

	private void addEdge(Integer node1, Integer node2)
	{
		boolean bAdded = false;
		for(Graph gr :graphs)
		{
			if(gr.addEdge( node1, node2 ))
			{
				bAdded = true;
				break;
			}
		}
		if(!bAdded)
		{
			graphs.add( new Graph(node1, node2) );
		}
	}
	
	private static class Graph
	{
		public ArrayList<Integer> nodes = new ArrayList<>();
		
		public Graph(Integer node1, Integer node2)
		{
			nodes.add( node1 );
			nodes.add( node2 );
		}
		
		public boolean addEdge(Integer node1, Integer node2)
		{
			if(nodes.contains(node1) && nodes.contains(node2))
			{
				return true;
			}
			if(nodes.contains(node1))
			{
				nodes.add( node2 );
				return true;
			}
			if(nodes.contains(node2))
			{
				nodes.add( node1 );
				return true;
			}
			return false;
		}
	}
	
	public static class Vertex
	{
		public Integer node;
		public ArrayList<Integer> neighbors = new ArrayList<>();
		
		public Vertex(int node)
		{
			this.node = node;		
		}
		public void addNeighbor(Integer neighbor)
		{
			for(Integer i:neighbors)
			{
				if(i.intValue()==neighbor.intValue())
					return;
			}
			neighbors.add( neighbor );
		}
	}
	
	public Map<Vertex, Integer> colourVertices()
	{
		Collections.sort(vertices, new VertexComparator()); // arrange vertices in order of descending valence
		final Map<Vertex, Integer> vertex_color_index = new HashMap<>(); //create Map<Vertex, Color>
		for (int i = 0; i < vertices.size(); i++)
		{
			if ((vertex_color_index.containsKey(vertices.get(i).node)))
			{	
				continue;
			}
			vertex_color_index.put(vertices.get(i), i%3); //color first vertex in list with color 1
			final Map<Vertex, Integer> curr_color = new HashMap<>(); 
			curr_color.put( vertices.get(i), i%3);
			for (int j = i+1; j < vertices.size(); j++)
			{
//				if (!(vertices.get(i).neighbors.contains(vertices.get(j).node)) && !(vertex_color_index.containsKey(vertices.get(j).node)))
//				{
//					vertex_color_index.put(vertices.get(j).node, i%3);
//				}
				//for()
				if(!(vertex_color_index.containsKey(vertices.get(j))))
				{
					boolean bNeighborColored = true;
					for (Map.Entry<Vertex, Integer> entry : curr_color.entrySet()) 
					{
						if(entry.getKey().neighbors.contains( j ))
							bNeighborColored = false;
					    //if(entry.getKey())
					}
					if(bNeighborColored)
					{
						vertex_color_index.put(vertices.get(j), i%3);
						curr_color.put(vertices.get(j), i%3);
					}
				}

//				if (!(vertices.get(i).neighbors.contains(vertices.get(j).node)) 
////					&& !(vertex_color_index.containsKey(vertices.get(j).node)))
//				{
//					vertex_color_index.put(vertices.get(j).node, i%3);
//				}

				else
				{
					continue;
				}
			}	
		}
		return vertex_color_index;
		
	}
	
	class VertexComparator implements Comparator<Vertex>{

		@Override
		public int compare(Vertex a, Vertex b) 
		{
			return a.neighbors.size() < b.neighbors.size() ? 1 : a.neighbors.size() == b.neighbors.size() ? 0 : -1;
		}
		
	}
}
