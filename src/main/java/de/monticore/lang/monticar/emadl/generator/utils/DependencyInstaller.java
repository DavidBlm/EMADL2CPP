package de.monticore.lang.monticar.emadl.generator.utils;

import de.monticore.lang.monticar.emadl.generator.emadlgen.Generator;
import de.monticore.lang.monticar.emadl.tagging.artifacttag.DatasetArtifactSymbol;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;

public class DependencyInstaller {



    public static void installDependency(String groupId, String artifactId, String version) {
        try {
            InvocationRequest request = new DefaultInvocationRequest();
            //request.setGoals(Collections.singletonList("dependency:resolve"));
            request.setGoals(Arrays.asList("dependency:get"));
            request.setUserSettingsFile(new File("settings.xml"));

            Properties properties = new Properties();
            properties.setProperty("groupId", groupId);
            properties.setProperty("artifactId", artifactId);
            properties.setProperty("version", version);
            properties.setProperty("classifier", "dataset");

            request.setProperties(properties);

            Invoker invoker = new DefaultInvoker();
            invoker.execute(request);
        } catch (MavenInvocationException e){

            //Log.error("Dependency Resolving Exception:" + e.getStackTrace().toString());
            //Log.info(e.toString(),"MODULAR_TEST_EXCEPTION");

            StackTraceElement[] stackTraceElements = e.getStackTrace();
            StringBuilder trace = new StringBuilder("Stack trace: \n");
            for (StackTraceElement element:stackTraceElements){
                trace.append(element.toString()).append("\n");
            }

            //Log.error("Dependency Resolving Exception: " + trace.toString());

            //throw new RuntimeException("Error resolving the maven dataset artifact " + groupId + ":" + artifactId + ":" + version);
        }
    }

    public static Set<Pair<Path, String>> resolveDependencies(String groupId, String artifactId, String version){
        String artifactVersion = artifactId + "-" + version;
        Path pomFile = getPathToMavenPackage(groupId, artifactId, version).resolve(artifactVersion + ".pom");
        List<Dependency> dependencies = new LinkedList<>();

        Set<Pair<Path, String>> dataPaths = new HashSet<>();
        dataPaths.add(new ImmutablePair<>(getPathToMavenPackage(groupId, artifactId, version), artifactVersion));

        try (InputStream in = Files.newInputStream(pomFile)) {
            dependencies = new MavenXpp3Reader().read(in).getDependencies();
        } catch (IOException | XmlPullParserException | NullPointerException e) {
            Log.info("Could not read pom.xml for artifact " + groupId + ":" + artifactId + ":" + version,
                    DependencyInstaller.class.getName());
            e.printStackTrace();
        }

        for(Dependency dependency : dependencies){
            dataPaths.addAll(resolveDependencies(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()));
        }

        return dataPaths;
    }

    public static Path getPathToMavenPackage(String groupId, String artifactId, String version){
        MavenSettings mavenSettings = new MavenSettings();
        List<String> pathElements = Stream.concat(
                Arrays.stream(groupId.split("\\.")),
                Arrays.stream(artifactId.split("\\.")))
                .collect(Collectors.toList());

        pathElements.add(version);

        return Paths.get(mavenSettings.getLocalRepository().toString(), pathElements.toArray(new String[0]));
    }
}

