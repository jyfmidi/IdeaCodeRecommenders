package ru.spbau.jps.incremental.recommenders;

import com.intellij.compiler.instrumentation.InstrumentationClassFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.asm4.ClassReader;
import org.jetbrains.asm4.ClassWriter;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.incremental.BinaryContent;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.CompiledClass;
import org.jetbrains.jps.incremental.instrumentation.BaseInstrumentingBuilder;
import org.jetbrains.jps.incremental.messages.CustomBuilderMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class RecommendersBuilder extends BaseInstrumentingBuilder {
    public static final String BUILDER_ID = "6969";
    public static final String MESSAGE_TYPE = "696969";
    private StringSerializer<RecommendersMessageData> serializer = new StringSerializer<RecommendersMessageData>();

    public RecommendersBuilder() {
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Recommenders Analyzer";
    }

    @Override
    protected boolean isEnabled(CompileContext context, ModuleChunk chunk) {
        return true;
    }

    @Override
    protected boolean canInstrument(CompiledClass compiledClass, int classFileVersion) {
        return true;
    }

    @Nullable
    @Override
    protected BinaryContent instrument(CompileContext context, CompiledClass compiled, ClassReader reader, ClassWriter writer, InstrumentationClassFinder finder) {
        HashMap<String, Map<List<String>, Integer>> sequences = new HashMap<String, Map<List<String>, Integer>>();
        RecommendersClassVisitor instrumenter = new RecommendersClassVisitor(compiled.getClassName(), sequences);
        try {
            reader.accept(instrumenter, ClassReader.EXPAND_FRAMES);
            String sourceId = compiled.getOutputFile().getAbsolutePath();
            RecommendersMessageData messageData = new RecommendersMessageData(sourceId, sequences);
            String serializedData = serializer.serialize(messageData);
            context.processMessage(new CustomBuilderMessage(BUILDER_ID, MESSAGE_TYPE, serializedData));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected String getProgressMessage() {
        return "Collecting information for recommendations...";
    }
}
