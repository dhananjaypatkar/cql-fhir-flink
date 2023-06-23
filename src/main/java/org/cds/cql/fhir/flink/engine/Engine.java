package org.cds.cql.fhir.flink.engine;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cds.cql.fhir.flink.utils.BundleCreator;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.execution.ExpressionResult;
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.evaluator.CqlEvaluator;
import org.opencds.cqf.cql.evaluator.builder.CqlEvaluatorBuilder;
import org.opencds.cqf.cql.evaluator.cql2elm.content.InMemoryLibrarySourceProvider;
import org.opencds.cqf.cql.evaluator.engine.model.CachingModelResolverDecorator;
import org.opencds.cqf.cql.evaluator.engine.retrieve.BundleRetrieveProvider;
import org.opencds.cqf.cql.evaluator.engine.terminology.BundleTerminologyProvider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class Engine implements Serializable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 731726468397360316L;

	private final String measureId;
    
    private final String measureVersion;
    
	private final List<String> libList;

	private final List<String> valuesets;

	public Engine(final String measure,final String version ,final List<String> valuesets, final List<String> cqlLib) {
		this.measureId = measure;
		this.measureVersion = version;
		this.libList = cqlLib;
		this.valuesets = valuesets;
		
	}

	public void run(final String patientfhir) throws Exception {
		VersionedIdentifier libraryIdentifier; libraryIdentifier = new VersionedIdentifier().withId(this.measureId).withVersion(this.measureVersion);
		//TODO Make this variable...
		Interval measurementPeriod = new Interval(new DateTime(new BigDecimal("0"), 2022, 01, 01), true,
						new DateTime(new BigDecimal("0"), 2022, 12, 31), true);
		
		FhirContext fhirContext = FhirContext.forR4();
		IParser parser = fhirContext.newJsonParser(); 
		//  8.2.000
		BundleCreator bundleCreator = new BundleCreator(fhirContext);
		
		IBaseBundle vsBundle = bundleCreator.bundleFiles(this.valuesets);
		String valueSetBundleJson = parser.encodeResourceToString(vsBundle);

		// Set up a terminology provider that reads from a bundle
		Bundle terminologyBundle = (Bundle) parser.parseResource(valueSetBundleJson);
		TerminologyProvider terminologyProvider = new BundleTerminologyProvider(fhirContext, terminologyBundle);

		// Set up model resolution (this is this bit that translates between CQL types
		// and the HAPI FHIR java structures)
		R4FhirModelResolver rawModelResolver = new R4FhirModelResolver();
		ModelResolver modelResolver = new CachingModelResolverDecorator(rawModelResolver);
		IParser pparser = fhirContext.newJsonParser(); 
		
		Bundle dataBundle = (Bundle) pparser.parseResource(patientfhir);
	
		CqlEvaluatorBuilder builder = new CqlEvaluatorBuilder();

		builder.withLibrarySourceProvider(new InMemoryLibrarySourceProvider(libList));
		builder.withTerminologyProvider(terminologyProvider);

		BundleRetrieveProvider bundleRetrieveProvider = new BundleRetrieveProvider(fhirContext, dataBundle);

		builder.withModelResolverAndRetrieveProvider("http://hl7.org/fhir", modelResolver, bundleRetrieveProvider);

		CqlEvaluator cqlEvaluator = builder.build();

		EvaluationResult evalResult = cqlEvaluator.evaluate(libraryIdentifier,
				Collections.singletonMap("Measurement Period", measurementPeriod));

		StringBuilder stringBuilder = new StringBuilder();
		for (Map.Entry<String, ExpressionResult> entry : evalResult.expressionResults.entrySet()) {
			stringBuilder.append(entry.getKey() + ": " + this.toString(entry.getValue(), parser) + "\n");
		}

		System.out.println("Result ::: " + stringBuilder);
	}

	private String toString(ExpressionResult result, IParser parser) {
		if(result != null && result.value()!=null) {
			if (result.value() instanceof Patient) {
				Patient patient = (Patient) result.value();
				return patient.getName().get(0).getNameAsSingleString();	
			}
		}
		return String.valueOf(result.value());	
	}
}
