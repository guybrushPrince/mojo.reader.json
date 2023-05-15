/**
 * Copyright 2015 mojo Friedrich Schiller University Jena
 * 
 * This file is part of mojo.
 * 
 * mojo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * mojo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with mojo. If not, see <http://www.gnu.org/licenses/>.
 */
package de.jena.uni.mojo.plugin.json.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.error.Annotation;
import de.jena.uni.mojo.error.ParseAnnotation;
import de.jena.uni.mojo.model.WGNode;
import de.jena.uni.mojo.model.WorkflowGraph;
import de.jena.uni.mojo.model.sub.Data;
import de.jena.uni.mojo.reader.Reader;
import de.jena.uni.mojo.util.store.ErrorAndWarningStore;

import org.json.JSONObject;
import org.json.JSONArray;

/**
 * This mojo own JSON parser reads in json files and transforms them into a workflow graph.
 * 
 * @author Dr. Dipl.-Inf. Thomas M. Prinz
 * 
 */
public class JSONReader extends Reader {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -4368518287089471825L;

	/**
	 * A list of annotations that are important information.
	 */
	private final List<Annotation> annotations = new ArrayList<Annotation>();
	
	/**
	 * An input stream of the file.
	 */
	private InputStream input;

	/**
	 * The constructor defines a new network reader.
	 * 
	 * @param processName
	 * 			  The name of the process.
	 * @param stream
	 *            An xml string.
	 * @param analysisInformation
	 *            The analysis information.
	 * @param encoding
	 * 			  The encoding used in the stream.
	 */
	public JSONReader(String processName, String stream, AnalysisInformation analysisInformation, Charset encoding) {
		super(processName, analysisInformation);
		this.input = new ByteArrayInputStream(stream.getBytes(encoding));
	}

	@Override
	public List<Annotation> analyze() {
		// Read in the file
		try {
			int n = this.input.available();
			byte[] content = new byte[n];
			this.input.read(content, 0, n);
			String jsonProcessModel = new String(content, StandardCharsets.UTF_8);

			// Parse the json process model
			JSONObject processModel = new JSONObject(jsonProcessModel);

			int nodeCounter = 0;
			HashMap<Integer, WGNode> nodes = new HashMap<>();
			WorkflowGraph graph = new WorkflowGraph();

			for (Object oNode: processModel.getJSONArray("nodes")) {
				JSONObject node = (JSONObject) oNode;
				// Transform every place and transition to a workflow process node
				String type = node.getString("type");
				WGNode.Type wgType = WGNode.Type.ACTIVITY;
				switch (type) {
					case "XOR": {
						if (node.getString("gateway").equals("SPLIT")) {
							wgType = WGNode.Type.SPLIT;
						} else {
							wgType = WGNode.Type.MERGE;
						}
					} break;
					case "OR": {
						if (node.getString("gateway").equals("SPLIT")) {
							wgType = WGNode.Type.OR_FORK;
						} else {
							wgType = WGNode.Type.OR_JOIN;
						}
					} break;
					case "AND": {
						if (node.getString("gateway").equals("SPLIT")) {
							wgType = WGNode.Type.FORK;
						} else {
							wgType = WGNode.Type.JOIN;
						}
					} break;
					case "TASK":
					case "EVENT": {
						wgType = WGNode.Type.ACTIVITY;
					} break;
					case "START": {
						wgType = WGNode.Type.START;
					} break;
					case "END": {
						wgType = WGNode.Type.END;
					} break;
				}
				WGNode wgNode = new WGNode(nodeCounter++, wgType);
				nodes.put(node.getInt("id"), wgNode);

				if (wgType == WGNode.Type.START) graph.setStart(wgNode);
				else if (wgType == WGNode.Type.END) graph.setEnd(wgNode);
				else graph.addNode(wgNode);
			}
			for (Object oEdge: processModel.getJSONArray("edges")) {
				JSONObject edge = (JSONObject) oEdge;
				int from = edge.getInt("from");
				int to = edge.getInt("to");
				WGNode fromNode = nodes.get(from);
				WGNode toNode = nodes.get(to);
				fromNode.addSuccessor(toNode);
				toNode.addPredecessor(fromNode);
			}

			for (WGNode node : graph.getNodeListInclusive()) {
				node.setExtraInformation(new Data(nodes.size()));
			}

			this.graphs = Collections.singletonList(graph);

			return annotations;
		} catch (IOException e) {
			// There is an exception. Annotate it.
			ParseAnnotation annotation = new ParseAnnotation(this);
			annotations.add(annotation);

			return annotations;
		}
	}

	@Override
	public ErrorAndWarningStore getStore() {
		return new ErrorAndWarningStore();
	}

}
