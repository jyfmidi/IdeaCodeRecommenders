import junit.framework.Assert;
import org.jetbrains.asm4.ClassReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.spbau.jps.incremental.recommenders.RecommendersClassVisitor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Osipov Stanislav
 */
public class ForClassTest {

    private static File out;
    private static ClassReader reader;
    private static InputStream inputStream;


    @BeforeClass
    public static void setUp() throws Exception {
        File src = new File("recommenders-jps-plugin/testData/ForClass.java");
        out = new File("recommenders-jps-plugin/testData/ForClass.class");
        RecommendersClassVisitorTestSuite.compile(src);
        inputStream = new BufferedInputStream(new FileInputStream(out));
        reader = new ClassReader(inputStream);
    }


    @AfterClass
    public static void tearDown() throws Exception {
        inputStream.close();
        while (!out.delete()) {
        }
    }


    @Test
    public void testForMethod() throws Exception {

        Map<String, Map<List<String>, Integer>> sequences = new HashMap<String, Map<List<String>, Integer>>();
        reader.accept(new RecommendersClassVisitor("ForClass", sequences), ClassReader.EXPAND_FRAMES);
        String check = "{java/lang/String={[toCharArray()char, charAt(int)char, length()int]=1, [toCharArray()char, length()int]=1}}";
        Assert.assertTrue(check.equals(sequences.toString()));
    }
}
