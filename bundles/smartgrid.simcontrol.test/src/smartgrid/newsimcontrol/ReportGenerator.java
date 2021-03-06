package smartgrid.newsimcontrol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import smartgridoutput.EntityState;
import smartgridoutput.On;
import smartgridoutput.ScenarioResult;

public class ReportGenerator {
    public static void saveScenarioResult(File resultFile, ScenarioResult scenarioResult) {
        try {
            if (resultFile.exists()) {
                return;
            }
            resultFile.createNewFile();
            FileWriter fw = new FileWriter(resultFile);

            fw.write(getScenarioResultStats(scenarioResult));

            fw.close();
        } catch (IOException e) {
            System.err.print("Could not write ScenarioResult report to " + resultFile.getAbsolutePath());
        }
    }

    /**
     * Gathers stats and returns them as CSV formatted String
     * 
     * @return String of stats in CSV format
     */
    private static String getScenarioResultStats(ScenarioResult scenarioResult) {
        final String total = "Total";
        String headlines = "";
        String content = "";
        String hackedTitle = "Hacked";

        Map<String, Integer> stats = new HashMap<>();
        stats.put("Total", 0);
        stats.put(hackedTitle, 0);

        for (EntityState state : scenarioResult.getStates()) {
            String name = state.getClass().getSimpleName();
            if (stats.get(name) == null) {
                stats.put(name, 0);
            }
            stats.replace(name, stats.get(name).intValue() + 1);
            
            //Count hacked
			if(state instanceof On && ((On)state).isIsHacked()){
				stats.replace(hackedTitle, stats.get(hackedTitle) + 1);
			}
            //Count Total
            stats.replace(total, stats.get(total).intValue() + 1);
        }
        for (String key : stats.keySet()) {
            if (content != "" && headlines != "") {
                content += ";";
                headlines += ";";
            }
            headlines += key;
            content += stats.get(key).intValue();
        }
        return headlines + "\n" + content;
    }
}
