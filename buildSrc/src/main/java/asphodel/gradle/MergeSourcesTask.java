package asphodel.gradle;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class MergeSourcesTask extends DefaultTask {

    private static final Pattern DIRECTIVE_PATTERN = Pattern.compile(
            "^\\s*//\\s*asphodel::merge\\s*->\\s*loader\\s*=\\s*([A-Za-z_*]+)\\s*,\\s*version\\s*=\\s*([A-Za-z0-9._-]+|\\*)\\s*->\\s*([A-Za-z_][A-Za-z0-9_.]*)\\s*$",
            Pattern.MULTILINE);

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getJavaSourceDirs();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getResourceSourceDirs();

    @Input
    public abstract Property<String> getTargetLoader();

    @Input
    public abstract Property<String> getTargetVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputJavaDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputResourceDir();

    @TaskAction
    public void execute() throws IOException {
        Path outJava = getOutputJavaDir().get().getAsFile().toPath();
        Path outResource = getOutputResourceDir().get().getAsFile().toPath();
        clearDirectory(outJava);
        clearDirectory(outResource);
        Files.createDirectories(outJava);
        Files.createDirectories(outResource);

        List<CompilationUnitMerger.SourceFile> allFiles = parseSourceFiles();
        Map<String, CompilationUnitMerger.SourceFile> byFQN = indexByFQN(allFiles);
        Map<String, List<String>> contributions = resolveContributions(allFiles);
        Set<String> consumed = collectConsumed(contributions, byFQN);
        for (String eachConsumed : consumed) contributions.remove(eachConsumed);

        CompilationUnitMerger merger = new CompilationUnitMerger();
        for (CompilationUnitMerger.SourceFile sourceFile : allFiles) {
            if (consumed.contains(sourceFile.fqn())) continue;
            Path dest = outJava.resolve(sourceFile.fqn().replace('.', '/') + ".java");
            Files.createDirectories(dest.getParent());
            List<String> targets = contributions.get(sourceFile.fqn());
            if (targets == null || targets.isEmpty()) {
                Files.writeString(dest, sourceFile.rawSource());
            } else {
                List<CompilationUnitMerger.SourceFile> parts = new ArrayList<>(targets.size());
                for (String t : targets) parts.add(byFQN.get(t));
                Files.writeString(dest, merger.merge(sourceFile, parts).toString());
            }
        }

        copyResources(outResource);
    }

    private List<CompilationUnitMerger.SourceFile> parseSourceFiles() throws IOException {
        List<CompilationUnitMerger.SourceFile> out = new ArrayList<>();
        for (File root : getJavaSourceDirs().getFiles()) {
            if (!root.isDirectory()) continue;
            Path rootPath = root.toPath();
            try (Stream<Path> stream = Files.walk(rootPath)) {
                List<Path> files = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .sorted()
                        .toList();
                for (Path p : files) out.add(parseSourceFile(p));
            }
        }
        return out;
    }

    private CompilationUnitMerger.SourceFile parseSourceFile(Path path) throws IOException {
        String raw = Files.readString(path);
        CompilationUnit unit;
        try {
            unit = StaticJavaParser.parse(raw);
        } catch (Exception ex) {
            throw new GradleException("Failed to parse " + path + ": " + ex.getMessage(), ex);
        }
        if (unit.getTypes().isEmpty()) throw new GradleException("No top-level type declaration in " + path);
        String packageDec = unit.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("");
        String name = unit.getTypes().get(0).getNameAsString();
        String fqn = packageDec.isEmpty() ? name : packageDec + "." + name;
        return new CompilationUnitMerger.SourceFile(path, unit, parseDirectives(raw), fqn, raw);
    }

    private Map<String, CompilationUnitMerger.SourceFile> indexByFQN(List<CompilationUnitMerger.SourceFile> allFiles) {
        Map<String, CompilationUnitMerger.SourceFile> out = new LinkedHashMap<>();
        for (CompilationUnitMerger.SourceFile sf : allFiles) {
            CompilationUnitMerger.SourceFile existing = out.putIfAbsent(sf.fqn(), sf);
            if (existing != null) {
                throw new GradleException("Duplicate class '" + sf.fqn() + "' declared in:\n  "
                        + existing.path() + "\n  " + sf.path());
            }
        }
        return out;
    }

    private Map<String, List<String>> resolveContributions(List<CompilationUnitMerger.SourceFile> allFiles) {
        String loader = getTargetLoader().get();
        String version = getTargetVersion().get();
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (CompilationUnitMerger.SourceFile sf : allFiles) {
            List<String> matches = new ArrayList<>();
            for (MergeDirective d : sf.directives()) {
                if (d.matches(loader, version)) matches.add(d.targetFqn());
            }
            if (!matches.isEmpty()) out.put(sf.fqn(), matches);
        }
        return out;
    }

    private Set<String> collectConsumed(Map<String, List<String>> contributions, Map<String, CompilationUnitMerger.SourceFile> byFqn) {
        Set<String> out = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : contributions.entrySet()) {
            for (String target : entry.getValue()) {
                if (!byFqn.containsKey(target)) {
                    throw new GradleException("Merge directive in '" + entry.getKey()
                            + "' references unknown class '" + target + "'. Known classes: " + byFqn.keySet());
                }
                out.add(target);
            }
        }
        return out;
    }

    private static List<MergeDirective> parseDirectives(String source) {
        List<MergeDirective> out = new ArrayList<>();
        Matcher matcher = DIRECTIVE_PATTERN.matcher(source);
        while (matcher.find()) {
            out.add(new MergeDirective(matcher.group(1), matcher.group(2), matcher.group(3)));
        }
        return out;
    }

    private void copyResources(Path outResource) throws IOException {
        Set<String> copied = new HashSet<>();
        for (File root : getResourceSourceDirs().getFiles()) {
            if (!root.isDirectory()) continue;
            Path rootPath = root.toPath();
            try (Stream<Path> stream = Files.walk(rootPath)) {
                for (Path p : stream.filter(Files::isRegularFile).sorted().toList()) {
                    String relative = rootPath.relativize(p).toString().replace(File.separatorChar, '/');
                    if (!copied.add(relative)) continue;
                    Path destination = outResource.resolve(relative);
                    Files.createDirectories(destination.getParent());
                    Files.copy(p, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void clearDirectory(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path p : stream.filter(p -> !p.equals(root)).sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
        }
    }
}