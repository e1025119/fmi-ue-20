package e1025119.block1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

import e1025119.block1.Coloring.Color;

/**
 * Defines the Reductions
 * 
 * Assumptions:
 *  - verteces are numbered from 1 to n without missing values inbetween.
 */
public class SatEncodings {

	/**
	 * Gets a graph as input and generates a (representation of a) propositional formula that is satisfiable iff the graph has a valid coloring. 
	 *  
	 * @param graph
	 * @return cnf formula
	 */
	public IVec<IVecInt> validColoring(DefaultDirectedGraph<Integer, DefaultEdge> graph) {
		IVec<IVecInt> cnf = new Vec<IVecInt>();
		VecInt clause = new VecInt();
		int varCnt = 0;

		/* create all clauses for the 1st rule, dealing with nodes only having one color */
		for(Integer vertex : graph.vertexSet()) {
			varCnt++;
			int blue = varCnt, red = ++varCnt, green = ++varCnt;

			clause.push(blue);
			clause.push(red);
			clause.push(green);
			cnf.push(clause);
			clause = new VecInt();
			clause.push(-red);
			clause.push(-blue);
			cnf.push(clause);
			clause = new VecInt();
			clause.push(-red);
			clause.push(-green);
			cnf.push(clause);
			clause = new VecInt();
			clause.push(-green);
			clause.push(-blue);
			cnf.push(clause);
			clause = new VecInt();
		}

		/* create all clauses for the 2nd rule, dealing with green nodes having red successors only */
		for(Integer vertex : graph.vertexSet()) {
			for(DefaultEdge edge : graph.outgoingEdgesOf(vertex)) {
				Integer target = graph.getEdgeTarget(edge);
				clause = new VecInt();
				clause.push(-(vertex*3));
				clause.push(target*3-1);
				cnf.push(clause);
			}
		}

		/* create all clauses for the 3rd rule, dealing with red nodes having at least one green predecessor */
		for(Integer vertex : graph.vertexSet()) {
			List<Integer> sourceList = new ArrayList<>();
			for(DefaultEdge edge : graph.incomingEdgesOf(vertex)) {
				sourceList.add(graph.getEdgeSource(edge));
			}
			if(!sourceList.isEmpty()) {
				clause = new VecInt();
				clause.push(-(vertex*3-1));
				for(Integer source : sourceList) {
					clause.push(source*3);
				}
				cnf.push(clause);
			} else {
				clause = new VecInt();
				clause.push(-(vertex*3-1));
				cnf.push(clause);
			}
		}
		return cnf;
	}

	/**
	 * Gets a graph as input and generates a (representation of a) propositional formula that is satisfiable iff the graph has a valid coloring without coloring any vertex blue. 
	 *  
	 * @param graph
	 * @return cnf formula
	 */
	public IVec<IVecInt> validColoringNoBlue(DefaultDirectedGraph<Integer, DefaultEdge> graph) {
		IVec<IVecInt> cnf = validColoring(graph);
		VecInt clause;

		/* create all clauses for the additional 4th rule, dealing with nodes not being colored blue */
		for(Integer vertex : graph.vertexSet()) {
			int blue = vertex*3-2;
			clause = new VecInt();		
			clause.push(-blue);
			cnf.push(clause);
		}
		return cnf;
	}


	/**
	 * Generates an CNF that for a given instance graph and valid coloring test whether the coloring is minimal w.r.t. blue colored vertices.
	 * The formula is unsatisfiable iff the coloring is minimal, and the models of the formula
	 * correspond to valid colorings whose blue colored vertices are a strict subset of the vertices colored blue by the given coloring.  
	 * 
	 * @param graph
	 * @param coloring
	 * @return CNF formula
	 */
	public IVec<IVecInt> minColoring(DefaultDirectedGraph<Integer, DefaultEdge> graph, Coloring coloring) {
		Set<Integer> blueVerteces = new HashSet<>(), otherVerteces = new HashSet<>();
		for(Integer vertex : graph.vertexSet()) {
			if(coloring.getColor(vertex).equals(Color.BLUE)) {
				blueVerteces.add(vertex);
			} else {
				otherVerteces.add(vertex);
			}
		}
		VecInt clause = new VecInt();
		IVec<IVecInt> cnf = validColoring(graph);

		/* add clause to ensure that the set of blue nodes in the
		 * new coloring will be a strict subset of these blue verteces */
		for(Integer blue : blueVerteces) {
			clause.push(-(blue*3-2));
		}
		if(!clause.isEmpty()) {
			cnf.push(clause);
		}

		/* add clauses to ensure that no formerly non-blue node will
		 * be colored blue in the next iteration */
		for(Integer other : otherVerteces) {
			clause = new VecInt();
			clause.push(-(other*3-2));
			cnf.push(clause);
		}

		return cnf;
	}


	/**
	 * Takes a model of the formula generated via one of the other methods and a coloring. 
	 * Updates the coloring to a coloring corresponding to the model.
	 * 
	 * @param coloring
	 * @param model
	 */
	public void getColoringFromModel(Coloring coloring, int[] model) {
		for(int atom : model) {
			/* "only 1 color per node" from definition */
			if(atom > 0) {
				Color color = null;
				switch(atom%3) {
				case 0:
					color = Color.GREEN;
					break;
				case 1:
					color = Color.BLUE;
					break;
				case 2:
					color = Color.RED;
					break;
				}
				coloring.setColor((int)(Math.ceil((float)atom/3)), color);
			}
		}
	}
}
