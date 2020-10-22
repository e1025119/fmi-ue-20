package e1025119.block1;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DIMACSImporter;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.ImportException;
import org.jgrapht.io.VertexProvider;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * Main Class instantiating and running the SAT solver
 *
 */
public class ComputeColoring {

	private static final String STANDARD="coloring";
	private static final String NOBLUE="coloringNoBlue";
	private static final String MIN="mincoloring";

	/**
	 * 
	 * @param args
	 * @throws ImportException
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public static void main(String[] args) throws ImportException, IOException, TimeoutException {

		// Deal with the command line parameters
		if(args.length<1)
		{
			System.out.println("USAGE: ComputeColoring problem path [redVertex]* ");
		} else
		{
			String problem, path;
			if(args.length==1) {
				path=args[0];
				problem=STANDARD;
			} else {
				path=args[1];
				problem=args[0];
			}

			// Read graph from File
			DefaultDirectedGraph<Integer,DefaultEdge> graph = readGraphFromFile(path);		

			switch (problem) {
			case STANDARD:
				//Initialize Reduction
				SatEncodings reduction= new SatEncodings();	  
				// Compute the CNF formula		
				IVec<IVecInt> cnf = reduction.validColoring(graph);
				computeColoringSAT(graph, reduction, cnf);
				break;
			case NOBLUE:
				//Initialize Reduction
				SatEncodings reductionNB= new SatEncodings();	  
				// Compute the CNF formula		
				IVec<IVecInt> cnfNB = reductionNB.validColoringNoBlue(graph);
				computeColoringnoBlue(graph, reductionNB, cnfNB);
				break;
			case MIN:
				computeMinColoring(graph);
				break;
			default:
				System.out.println("Task not supported: " + problem);
			}

		}
	}

	private static void computeColoringSAT(DefaultDirectedGraph<Integer,DefaultEdge> graph, SatEncodings reduction, IVec<IVecInt> cnf) throws TimeoutException, ImportException, IOException {
		// Output CNF
		System.out.println("CNF: "+ clauseForm(cnf));

		//  Initialize the SAT solver
		ISolver solver = SolverFactory.newDefault();

		try {
			// Feeds the formula to the solver
			solver.addAllClauses(cnf);		

			IProblem instance = solver;

			solver.setTimeout(3600);

			// Computes a model / a coloring  if possible
			if (instance.isSatisfiable()) {
				int model[] = instance.model();
				//System.out.println("Model: "+Arrays.toString(model));
				// Initialize empty Coloring 
				Coloring coloring = new Coloring(graph);
				// Compute a Coloring from the model
				reduction.getColoringFromModel(coloring, model);
				// Check whether the computed coloring is valid
				if(coloring.isValidColoringOf(graph)) {
					System.out.println("Coloring: " + coloring);
				} else {
					System.out.println("INVALID Coloring: " + coloring);
				}	
			}		
			else {
				System.out.println("There is no valid coloring.");
			}			
		} catch (ContradictionException e) {
			System.out.println("There is no valid coloring.");
		}

	}

	private static void computeColoringnoBlue(DefaultDirectedGraph<Integer,DefaultEdge> graph, SatEncodings reduction, IVec<IVecInt> cnf) throws TimeoutException, ImportException, IOException {
		// Output CNF
		System.out.println("CNF: "+ clauseForm(cnf));

		//  Initialize the SAT solver
		ISolver solver = SolverFactory.newDefault();

		try {
			// Feeds the formula to the solver
			solver.addAllClauses(cnf);		

			IProblem instance = solver;

			solver.setTimeout(3600);

			// Computes a model / a coloring  if possible
			if (instance.isSatisfiable()) {
				int model[] = instance.model();
				//System.out.println("Model: "+Arrays.toString(model));
				// Initialize empty Coloring 
				Coloring coloring = new Coloring(graph);
				// Compute a Coloring from the model
				reduction.getColoringFromModel(coloring, model);
				// Check whether the computed coloring is valid
				if(coloring.isValidColoringOf(graph)) {
					System.out.println("Coloring: " + coloring);
				} else {
					System.out.println("INVALID Coloring: " + coloring);
				}
			}		
			else {
				System.out.println("There is no valid coloring without coloring vertices blue.");
			}			
		} catch (ContradictionException e) {
			System.out.println("There is no valid coloring withtout coloring vertices blue.");
		}

	}

	private static void computeMinColoring(DefaultDirectedGraph<Integer,DefaultEdge> graph)throws TimeoutException, ImportException, IOException {
		SatEncodings reduction= new SatEncodings();
		int counter=1;
		/*
		 *  Compute a first coloring		
		 */
		IVec<IVecInt> cnf = reduction.validColoring(graph);
		//System.out.println("CNF: "+ clauseForm(cnf));	

		Coloring coloring = new Coloring(graph);
		ISolver solver = SolverFactory.newDefault();	

		try {
			// Feeds the formula to the solver
			solver.addAllClauses(cnf);					
			IProblem instance = solver;
			solver.setTimeout(3600);
			if (instance.isSatisfiable()) {
				int model[] = instance.model();				
				reduction.getColoringFromModel(coloring, model);
				System.out.println(counter +": Coloring: " + coloring);
				counter++;
			}		
			else {
				System.out.println("There is no valid coloring.");
			}			
		} catch (ContradictionException e) {
			System.out.println("There is no valid coloring.");
		}
		/*
		 *  Iteratively shrinks the coloring		
		 */				
		while (true) {		
			// CNF testing whether the given coloring is minimal
			cnf = reduction.minColoring(graph, coloring);		
			//System.out.println(counter + ": CNF: "+ clauseForm(cnf));
			solver = SolverFactory.newDefault();
			try {
				solver.addAllClauses(cnf);
			} catch (ContradictionException e) {
				// trivially unsatisfiable 
				break;
			}	
			IProblem instance = solver;
			solver.setTimeout(3600);
			if (!instance.isSatisfiable()) {break;}
			else {
				//updating the coloring
				int model[] = instance.model();
				coloring = new Coloring(graph);
				reduction.getColoringFromModel(coloring, model);
				System.out.println(counter + ": Coloring: " + coloring);
			}
		}
		System.out.println("Min Coloring: " + coloring);
	}

	/**
	 * Reads a directed graph in DIMACS format from file
	 * 
	 * @param path Path to the DIMACS file
	 * @return DirectedGraph object corresponding to graph in the DIMACS file
	 * @throws ImportException
	 * @throws IOException
	 */
	private static DefaultDirectedGraph<Integer,DefaultEdge> readGraphFromFile(String path) throws ImportException, IOException {
		VertexProvider<Integer> vertexProvider = new IntVertexProvider();
		EdgeProvider<Integer,DefaultEdge> edgeProvider = new IntEdgeProvider();

		DIMACSImporter<Integer,DefaultEdge> importer = new DIMACSImporter<Integer,DefaultEdge>(vertexProvider, edgeProvider);
		DefaultDirectedGraph<Integer,DefaultEdge> importDiGraph = new DefaultDirectedGraph<Integer,DefaultEdge>(DefaultEdge.class);
		Reader reader;
		reader = new FileReader(path);
		importer.importGraph(importDiGraph, reader);
		reader.close();

		return importDiGraph;		
	}

	private static class  IntVertexProvider implements VertexProvider<Integer>{
		@Override
		public Integer buildVertex(String arg0, Map<String, Attribute> arg1) {
			return Integer.valueOf(arg0);
		}	
	}

	private static class IntEdgeProvider implements EdgeProvider<Integer,DefaultEdge>{
		@Override
		public DefaultEdge buildEdge(Integer arg0, Integer arg1, String arg2, Map<String, Attribute> arg3) {
			return new DefaultEdge(); 
		}
	}

	/**
	 * Get a nice String representation of CNFs 
	 * @param cnf
	 * @return
	 */
	public static String clauseForm (IVec<IVecInt> cnf){
		StringBuffer clauseForm = new StringBuffer();
		clauseForm.append("{");

		Iterator<IVecInt> iterator =cnf.iterator();
		IVecInt clause;
		while(iterator.hasNext()){
			clause=iterator.next();
			clauseForm.append("{");
			clauseForm.append(clause);
			clauseForm.append("}");
			if(iterator.hasNext()) {
				clauseForm.append(",");
			}
		}	
		clauseForm.append("}");
		return clauseForm.toString();
	}

}
