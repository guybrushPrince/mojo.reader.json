/**
 * Copyright 2016 mojo Friedrich Schiller University Jena
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
package de.jena.uni.mojo.plugin.json;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import de.jena.uni.mojo.analysis.information.AnalysisInformation;
import de.jena.uni.mojo.interpreter.IdInterpreter;
import de.jena.uni.mojo.plugin.SourcePlugin;
import de.jena.uni.mojo.plugin.json.interpreter.JSONIdInterpreter;
import de.jena.uni.mojo.plugin.json.reader.JSONReader;
import de.jena.uni.mojo.reader.Reader;

/**
 * 
 * @author Dipl.-Inf. Thomas Prinz
 *
 */
public class JSONSourcePlugin implements SourcePlugin {

	@Override
	public String getName() {
		return "Mojo Source Plugin Network";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getFileExtension() {
		return "json";
	}

	@Override
	public Reader getReader(String processName, File file, AnalysisInformation information, Charset encoding)
			throws IOException {
		String stream = String.join("", Files.readAllLines(file.toPath(), encoding));
		return new JSONReader(processName, stream, information, encoding);
	}

	@Override
	public Reader getReader(String processName, String stream, AnalysisInformation information, Charset encoding) {
		return new JSONReader(processName, stream, information, encoding);
	}

	@Override
	public IdInterpreter getIdInterpreter() {
		return new JSONIdInterpreter();
	}

}
