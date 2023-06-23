package org.cds.cql.fhir.flink.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.api.BundleInclusionRule;
import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.IVersionSpecificBundleFactory;
import ca.uhn.fhir.util.BundleUtil;

public class BundleCreator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8977336562839648084L;

	private FhirContext fhirContext;
	// private IParser xml = null;
	private IParser json = null;

	public BundleCreator(final FhirContext fhirContext) {
		this.fhirContext = fhirContext;
		this.json = fhirContext.newJsonParser();
	}

	public IBaseBundle bundleFiles(final String data) {
		List<IBaseResource> resources = new ArrayList<>();
		IBaseResource resource = parseData(data);
		if (resource instanceof IBaseBundle) {
			List<IBaseResource> innerResources = flatten(fhirContext, (IBaseBundle) resource);
			resources.addAll(innerResources);
		} else {
			resources.add(resource);
		}
		IVersionSpecificBundleFactory bundleFactory = fhirContext.newBundleFactory();
		// BundleLinks bundleLinks = new BundleLinks(rootPath, null, true,
		// BundleTypeEnum.COLLECTION);
		// bundleFactory.addRootPropertiesToBundle("bundled-directory", bundleLinks,
		// resources.size(), null);
		bundleFactory.addResourcesToBundle(resources, BundleTypeEnum.DOCUMENT, "",
				BundleInclusionRule.BASED_ON_INCLUDES, null);
		return (IBaseBundle) bundleFactory.getResourceBundle();
	}

	public IBaseBundle bundleFiles(final List<String> datas) {
		List<IBaseResource> resources = new ArrayList<>();
		IVersionSpecificBundleFactory bundleFactory = fhirContext.newBundleFactory();
		try {
			for (String data : datas) {
				// System.out.println("Data for creating bundle "+data);
				IBaseResource resource = parseData(data);
				if (resource instanceof IBaseBundle) {
					List<IBaseResource> innerResources = flatten(fhirContext, (IBaseBundle) resource);
					/*for (IBaseResource iBaseResource : resources) {
						try {
							System.out.println("iBaseResource.element id " + iBaseResource.getIdElement() != null
									? iBaseResource.getIdElement()
									: "null id element");
						} catch (Exception e) {
							System.out.println("data in error " + data);
							e.printStackTrace();
						}

					}*/
					resources.addAll(innerResources);
				} else {
					/*try {
						System.out.println(
								"resource.element id " + resource.getIdElement() != null ? resource.getIdElement()
										: "null id element");
					} catch (Exception e) {
						System.out.println("Data in error " + data);
						e.printStackTrace();
					}*/

					resources.add(resource);
					if (resource.getIdElement() == null) {
						System.out.println("resource " + data + " ");
					}
				}
			}

			bundleFactory.addResourcesToBundle(resources, BundleTypeEnum.COLLECTION, "",
					BundleInclusionRule.BASED_ON_INCLUDES, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (IBaseBundle) bundleFactory.getResourceBundle();
	}

	private IBaseResource parseData(final String resource) {
		try {
			return json.parseResource(resource);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<IBaseResource> flatten(FhirContext fhirContext, IBaseBundle bundle) {
		List<IBaseResource> resources = new ArrayList<>();

		List<IBaseResource> bundleResources = BundleUtil.toListOfResources(fhirContext, bundle);
		for (IBaseResource r : bundleResources) {
			if (r instanceof IBaseBundle) {
				List<IBaseResource> innerResources = flatten(fhirContext, (IBaseBundle) r);
				resources.addAll(innerResources);
			} else {
				resources.add(r);
			}
		}

		return resources;
	}

}
