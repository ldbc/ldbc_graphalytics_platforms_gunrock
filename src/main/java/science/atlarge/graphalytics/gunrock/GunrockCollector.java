/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package science.atlarge.graphalytics.gunrock;

import org.apache.commons.io.output.TeeOutputStream;
import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;
import science.atlarge.graphalytics.report.result.BenchmarkMetric;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 *
 * @author Owens Group
 */
public class GunrockCollector {

	protected static final Logger LOG = LogManager.getLogger();

	private static PrintStream defaultSysOut;
	private static PrintStream deafultSysErr;

	private static Path gunrockJson = null;

	public static void startPlatformLogging(Path fileName) {
		defaultSysOut = System.out;
		deafultSysErr = System.err;
		try {
			File file = fileName.toFile();
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			TeeOutputStream bothStream = new TeeOutputStream(System.out, fos);
			PrintStream ps = new PrintStream(bothStream);
			System.setOut(ps);
			System.setErr(ps);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Failed to redirect to log file %s", fileName);
			throw new GraphalyticsExecutionException("Failed to log the benchmark run. Benchmark run aborted.");
		}
	}

	public static void stopPlatformLogging() {
		System.setOut(defaultSysOut);
		System.setErr(deafultSysErr);
	}

	public static void match(String glob, Path location) throws IOException {

		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
		LOG.info("The location is: {} ", location.toString());
		Files.walkFileTree(location, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				if (pathMatcher.matches(path)) {
					LOG.info("The path that matched is: {}", path.toString());
					gunrockJson = path;
					return FileVisitResult.TERMINATE;
				}
				else {
					LOG.info("The path that didn't match is: {}", path.toString());
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
						return FileVisitResult.CONTINUE;
					}
		});

	}

	public static BenchmarkMetric collectMakeSpanTime(Path logPath) throws Exception {
		
		LOG.info("Returning default makespan of 1s");
	
		return new BenchmarkMetric(new BigDecimal(1.0), "s");

	}

	public static BenchmarkMetric collectProcessingTime(Path logPath) throws Exception {

		GunrockCollector.match("glob:**.json", logPath);

		if (gunrockJson == null){
			throw new NullPointerException("Gunrock JSON file not found: " + gunrockJson.toString());
		}

		LOG.info("Gunrock JSON file is {}", gunrockJson.toString());

		InputStream jsonStream = Files.newInputStream(gunrockJson);

		if (jsonStream == null){
			throw new NullPointerException("Cannot open Gunrock JSON file: " + gunrockJson.toString());
		} 

		JSONTokener tokenizer = new JSONTokener(jsonStream);

		try {
			JSONObject jsonObject = new JSONObject(tokenizer);
			BigDecimal processingTime = jsonObject.getBigDecimal("process-times");
			return new BenchmarkMetric(processingTime, "s");
		}
		catch(Exception e){
			LOG.info("The Exception is: {} and the stack trace is {} ", e.toString(), e.getStackTrace().toString());
		}
		

		// Parse the JSON that should be there in the logpath as some XYZ.json, this XYZ can be set in execute-job.sh
		// Next read the processing time from the json and send it to Graphalytics with the correct name and metric type
		// Path resourceName = logPath.resolve("/info.json");
			
		LOG.info("Returning default processing time of 1s");

		return new BenchmarkMetric(new BigDecimal(1.0), "s");
	}

	public static BenchmarkMetric collectLoadTime(Path logPath) throws Exception {

		GunrockCollector.match("glob:**.json", logPath);

		if (gunrockJson == null){
			throw new NullPointerException("Gunrock JSON file not found: " + gunrockJson.toString());
		}

		LOG.info("Gunrock JSON file is {}", gunrockJson.toString());

		InputStream jsonStream = Files.newInputStream(gunrockJson);

		if (jsonStream == null){
			throw new NullPointerException("Cannot open Gunrock JSON file: " + gunrockJson.toString());
		} 

		JSONTokener tokenizer = new JSONTokener(jsonStream);

		try {
			JSONObject jsonObject = new JSONObject(tokenizer);
			BigDecimal processingTime = jsonObject.getBigDecimal("load-time");
			return new BenchmarkMetric(processingTime, "s");
		}
		catch(Exception e){
			LOG.info("The Exception is: {} and the stack trace is {} ", e.toString(), e.getStackTrace().toString());
		}
		

		// Parse the JSON that should be there in the logpath as some XYZ.json, this XYZ can be set in execute-job.sh
		// Next read the processing time from the json and send it to Graphalytics with the correct name and metric type
		// Path resourceName = logPath.resolve("/info.json");
		
		LOG.info("Returning default loadtime of 1.0s");	
		return new BenchmarkMetric(new BigDecimal(1.0), "s");
	}

}
