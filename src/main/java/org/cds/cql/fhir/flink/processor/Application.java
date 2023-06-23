package org.cds.cql.fhir.flink.processor;

import java.io.File;
import java.time.Duration;
import java.util.List;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.cds.cql.fhir.flink.engine.Engine;
import org.cds.cql.fhir.flink.utils.FHIRInputTextFormat;
import org.cds.cql.fhir.flink.utils.Utils;

public class Application {
public static void main(String[] args) throws Exception{
	final List<String> cqlLib =   Utils.loadValuesets("C:\\work\\workspace\\cql-fhir-flink\\cql\\");
	final List<String> valueSets  =   Utils.loadValuesets("C:\\work\\workspace\\cql-fhir-flink\\valuesets\\");
	final Engine engine = new Engine("EXM124_FHIR4", "8.2.000",valueSets, cqlLib);
	 final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
	 final FileSource<String> source =
			    FileSource.forRecordStreamFormat(new FHIRInputTextFormat(), Path.fromLocalFile(new File("C:\\work\\workspace\\cql-fhir-flink\\input\\")))
			  .monitorContinuously(Duration.ofSeconds(10L))
			  .build();
			final DataStream<String> stream =
			  env.fromSource(source, WatermarkStrategy.noWatermarks(), "file-source");
			
			stream.process( new ProcessFunction<String, String>() {
				
			
				private static final long serialVersionUID = 1L;

				@Override
				public void processElement(String arg0, ProcessFunction<String, String>.Context arg1,
						Collector<String> arg2) throws Exception {
					
					if(arg0 != null && !arg0.isEmpty()) {
					System.out.println(" ===================================================================== ");
				
					engine.run(arg0);
					System.out.println(" ===================================================================== ");
					}
					
				}
			});
			env.execute("FHIR CQL Processing");
}
}
