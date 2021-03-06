package br.com.vraptor.client.classprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.util.StringUtils;

public class ClasspathScannerRestClassesProvider implements RestClassesProvider {

	final static private String SEP = ",";
	private List<String> packagesToScan;

	@Autowired
	public ClasspathScannerRestClassesProvider(String packageToScan) {
		this.packagesToScan = this.extractPackagesToScan(packageToScan);
	}

	private List<String> extractPackagesToScan(String packageToScan) {
		List<String> packages = new ArrayList<String>();
		if (packageToScan.contains(ClasspathScannerRestClassesProvider.SEP)) {
			packages = Arrays.asList(packageToScan.split(ClasspathScannerRestClassesProvider.SEP));
		} else {
			packages = Arrays.asList(new String[] { packageToScan });
		}
		return this.filterPackagesToScan(packages);
	}

	private List<String> filterPackagesToScan(List<String> packages) {
		List<String> packs = new ArrayList<String>();
		for (String pack : packages) {
			if (StringUtils.hasLength(StringUtils.trimAllWhitespace(pack))) {
				packs.add(pack);
			}
		}
		return packs;
	}

	@Override
	public Set<Class<?>> classes() {
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		for (BeanDefinition component : this.scanPackages(this.packagesToScan)) {
			classes.add(classFrom(component));
		}
		return classes;
	}

	private Class<?> classFrom(BeanDefinition component) {
		try {
			return Class.forName(component.getBeanClassName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<BeanDefinition> scanPackages(List<String> packagesToScan) {
		final ClassPathScanningCandidateComponentProvider provider = new CustomClassPathScanningCandidateComponentProvider(
				false);
		provider.addIncludeFilter(new IsInterfaceTypeFilter());

		Set<BeanDefinition> components = new HashSet<BeanDefinition>();
		for (String packageToScan : packagesToScan) {
			components.addAll(provider.findCandidateComponents(packageToScan));
		}
		return components;
	}

}
