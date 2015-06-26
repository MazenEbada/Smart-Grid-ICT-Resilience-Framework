package smartgrid.impactanalysis;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Tarjan {
	/**
	 * expects the graph to be BIDRECTIONAL (simplification of tarjan)
	 * @param adjacentMatrix
	 * @return
	 */
	public static List<List<Integer>> getClusters(double[][] adjacentMatrix, Map<Integer, Integer> internalToExternal) {
		List<List<Integer>> result = new LinkedList<List<Integer>>();
		LinkedList<Integer> unprogressedNodes = new LinkedList<Integer>();
		boolean[] visited = new boolean[adjacentMatrix.length];
		for (int i = 0; i < adjacentMatrix.length; i++) {
			unprogressedNodes.add(i);
			visited[i] = false;
		}
		System.out.println("Algorithmus startet mit");
		
		System.out.println(printConvertedList(unprogressedNodes,internalToExternal));
		System.out.println(Arrays.toString(unprogressedNodes.toArray()));
		//System.out.println(Arrays.toString(unprogressedNodes.toArray()));
		while (unprogressedNodes.size() > 0) {
			//start building a new cluster
			
			
			
			LinkedList<Integer> nodesToCheck = new LinkedList<Integer>();
			int starter = unprogressedNodes.pollFirst();
			nodesToCheck.add(starter);
			visited[starter] = true;
			System.out.println("neuer Cluster mit " + internalToExternal.get(starter));
			
			
			LinkedList<Integer> cluster = new LinkedList<Integer>();
			while(nodesToCheck.size() > 0) {
				int n = nodesToCheck.pollFirst();
				cluster.add(n);
				System.out.println("Finde Nachbarn von : " + internalToExternal.get(n));
				for (int i = 0; i < adjacentMatrix.length; i++) {
					if (adjacentMatrix[n][i] > 0) System.out.println("Gefundener Nachbar: " + internalToExternal.get(i));
					if (adjacentMatrix[n][i] > 0 && !visited[i]) {
						System.out.println("Unbesuchter Nachbar: " + internalToExternal.get(i));
						int m = i;
						
						visited[m] = true;
						nodesToCheck.add(m);
						
						unprogressedNodes.remove(new Integer(m));
						
					}
				}
			}
			result.add(cluster);
			
			
			
		}
		return result;
	}
	static String printConvertedList(List<Integer> l, Map<Integer, Integer> internalToExternal) {
		String s = "";
		
		for (Integer i : l) {
			s +=  internalToExternal.get(i) + " ";
		}
		
		return s;
	}
}