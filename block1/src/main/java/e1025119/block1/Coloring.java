package e1025119.block1;

import java.util.HashMap;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * 
 * A coloring of a graph.
 *
 */
public class Coloring {

	/**
	 * The possible colors
	 *
	 */
	public enum Color {
		RED, GREEN, BLUE
	}

	/**
	 * The actual mapping of vertices to colors 
	 */
	private Map<Integer,Color> coloring;

	public Coloring() {
		this.coloring=new HashMap<Integer, Color>();		
	}

	public Coloring(Graph<Integer, DefaultEdge> graph) {
		this.coloring=new HashMap<Integer, Color>(graph.vertexSet().size());		
	}

	/**
	 * Generates a new coloring equal to an existing one
	 * @param coloring
	 */
	public Coloring(Coloring coloring) {
		this.coloring=new HashMap<Integer, Color>(coloring.getMap());		
	}

	private Map<Integer, Color> getMap() {
		return this.coloring;
	}

	/**
	 * Colors the given vertex with the given color. Overwrites existing colors.
	 * @param vertex
	 * @param color
	 */
	public void setColor(Integer vertex, Color color) {
		coloring.put(vertex,color);
	}

	/**
	 * 
	 * @param vertex
	 * @return Returns the color of a vertex
	 */
	public Color getColor(Integer vertex) {
		return coloring.get(vertex);
	}

	/**
	 * removes a vertex from the coloring
	 * @param vertex
	 */
	public void remove (Integer vertex) {
		coloring.remove(vertex);
	}

	/**
	 * 
	 * @param vertex
	 * @return true if the vertex is already colored
	 */
	public boolean contains(Integer vertex) {
		return coloring.containsKey(vertex);
	}

	public String toString () {
		if (coloring.size()<30) return coloring.toString();
		int red=0,green=0,blue=0;
		for(Integer vertex: coloring.keySet()) {
			switch(coloring.get(vertex)) {
			case RED:
				red++;
				break;
			case GREEN:
				green++;
				break;
			case BLUE:
				blue++;
				break;
			}
		}

		return coloring.size()+" colored vertices: RED: " +red + ", GREEN: " + green + ", BLUE: " + blue;
	}

	/**
	 * Test whether the stored coloring is valid for the given graph
	 * 
	 * @param graph
	 * @return true if the coloring is valid for the given instance
	 */
	public boolean isValidColoringOf(Graph<Integer,DefaultEdge> graph) {
		for(Integer vertex : graph.vertexSet()) {
			/* check if every node has exactly one color */
			if(!this.contains(vertex)) {
				return false;
			}

			/* check if green nodes have red successors only */
			if(this.getColor(vertex).equals(Color.GREEN)) {
				for(DefaultEdge edge : graph.outgoingEdgesOf(vertex)) {
					Integer target = graph.getEdgeTarget(edge);
					if(!this.getColor(target).equals(Color.RED)) {
						return false;
					}
				}
			}

			/* check if every red node has at least one green predecessor */
			if(this.getColor(vertex).equals(Color.RED)) {
				boolean greenPred = false;
				for(DefaultEdge edge : graph.incomingEdgesOf(vertex)) {
					Integer source = graph.getEdgeSource(edge);
					if(this.getColor(source).equals(Color.GREEN)) {
						greenPred = true;
						break;
					}
				}
				if(!greenPred) {
					return false;
				}
			}
		}
		return true;
	}	

}
