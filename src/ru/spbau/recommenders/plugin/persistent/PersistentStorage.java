package ru.spbau.recommenders.plugin.persistent;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.Function;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.PersistentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.recommenders.plugin.data.Suggestions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static ru.spbau.recommenders.plugin.persistent.Externalizers.CLASS_AND_CALL_SEQUENCE_KEY_DESCRIPTOR;
import static ru.spbau.recommenders.plugin.persistent.Externalizers.SUGGESTIONS_EXTERNALIZER;

/**
 * @author Pavel Talanov
 */
public final class PersistentStorage {

    private static final Logger LOG = Logger.getInstance("#ru.spbau.recommenders.plugin.persistent.PersistentStorage");


    public static final String RECOMMENDERS_DIR_NAME = "recommenders";
    public static final String RECOMMENDATIONS_FILE_NAME = "recommendations.data";
    private PersistentHashMap<ClassAndCallSequence, Suggestions> storage;

    public PersistentStorage(@NotNull Project project) {
        try {
            storage = new PersistentHashMap<ClassAndCallSequence, Suggestions>(
                    getStorageFile(project), CLASS_AND_CALL_SEQUENCE_KEY_DESCRIPTOR, SUGGESTIONS_EXTERNALIZER);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @NotNull
    private static File getStorageFile(@NotNull Project project) {
        String systemDirPath = PathUtil.getCanonicalPath(PathManager.getSystemPath());
        File recommendersDir = new File(systemDirPath, RECOMMENDERS_DIR_NAME);
        //TODO: see CompilerPaths#getPresentableName()
        File projectDir = new File(recommendersDir, project.getName() + "." + project.getLocationHash());
        File result = new File(projectDir, RECOMMENDATIONS_FILE_NAME);
        FileUtil.createIfDoesntExist(result);
        return result;
    }

    public void close() {
        try {
            storage.close();
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public void registerCallSequence(@NotNull String typeName, @NotNull List<String> callSequence, @NotNull String methodToSuggest) {
        ClassAndCallSequence key = new ClassAndCallSequence(typeName, callSequence);
        try {
            Suggestions value = storage.get(key);
            if (value == null) {
                value = new Suggestions();
            }
            value.registerUsage(methodToSuggest);
            storage.put(key, value);
        } catch (IOException e) {
            LOG.error(e);
        }

    }

    @Nullable
    public Suggestions getSuggestions(@NotNull String typeName, @NotNull List<String> callSequence) {
        try {
            return storage.get(new ClassAndCallSequence(typeName, callSequence));
        } catch (IOException e) {
            LOG.error(e);
            return null;
        }
    }

    @Override
    public String toString() {
        return "PersistentStorage{" +
                "storage=" + storage +
                '}';
    }

    //TODO: this a just a draft
    public void proccessDiff(@NotNull Map<String, Map<List<String>, Integer>> result) {
        for (Map.Entry<String, Map<List<String>, Integer>> stringMapEntry : result.entrySet()) {
            String typeName = stringMapEntry.getKey();
            String realTypeName = typeName.replace("/", ".");
            for (Map.Entry<List<String>, Integer> sequenceAndCount : stringMapEntry.getValue().entrySet()) {
                List<String> sequence = sequenceAndCount.getKey();
                List<String> realSequence = ContainerUtil.map(sequence, new Function<String, String>() {
                    @Override
                    public String fun(String s) {
                        return s.substring(0, s.indexOf("("));
                    }
                });
                Integer count = sequenceAndCount.getValue();
                for (int i = 0; i < sequence.size(); ++i) {
                    for (int j = 0; j < count; ++j) {
                        registerCallSequence(realTypeName, realSequence.subList(0, i), realSequence.get(i));
                    }
                }
            }
        }
    }
}
