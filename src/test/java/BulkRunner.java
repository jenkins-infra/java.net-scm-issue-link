import com.cloudbees.javanet.cvsnews.cli.UpdateCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class BulkRunner {
    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".eml");
            }
        });

        Set<String> excludes = loadExcludes();

        for (File file : files) {
            System.out.println(file);
            if (excludes.contains(file.getPath())) {
                System.out.println("   Skipping");
                continue;
            }

            UpdateCommand uc = new UpdateCommand();
            FileInputStream in = new FileInputStream(file);
            try {
                uc.execute(uc.parse(in));
            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            } finally {
                in.close();
            }
        }
    }

    private static Set<String> loadExcludes() throws IOException {
        Set<String> r = new HashSet<String>();
        File excludes = new File("excludes.txt");
        if (excludes.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(excludes));
            String line;
            while ((line=br.readLine())!=null) {
                r.add(line.trim());
            }
        }
        return r;
    }
}
