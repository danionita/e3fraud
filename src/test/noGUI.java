/*
 * Copyright (C) 2015 Dan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package test;



import com.hp.hpl.jena.rdf.model.Resource;
import e3fraud.parser.FileParser;
import e3fraud.model.E3Model;
import e3fraud.model.ModelRanker;
import e3fraud.model.SubIdealModelGenerator;
import e3fraud.vocabulary.E3value;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author Dan
 */
public class noGUI {

    public static void main(String[] args) {
        File file = new File("D://temp/Scenario1.rdf");
        E3Model baseModel = FileParser.parseFile(file);
        Map<Resource, XYSeries> actorSeriesMap = new HashMap();
        Map<E3Model, Map<Resource, XYSeries>> generatedModels = new HashMap();
        Map<String, Resource> needsMap = baseModel.getNeedsMap();
        String selectedNeedString = "Need to make call";
        String selectedActorString = "Provider A";
        Set<Resource> actors = baseModel.getActors();
        Map<String, Resource> actorsMap = baseModel.getActorsMap();
        int startValue = 0;
        int endValue = 500;

        Resource mainActor = actorsMap.get(selectedActorString);
        Resource selectedNeed = needsMap.get(selectedNeedString);

        //generate sub-ideal models
        SubIdealModelGenerator subIdealModelGenerator = new SubIdealModelGenerator();
        Set<E3Model> subIdealModels = subIdealModelGenerator.generateAll(baseModel,mainActor);
        //Set<E3Model> subIdealModels = subIdealModelGenerator.generateHiddenTransactions(baseModel, mainActor);
        System.out.println("\nI GENERATED : " + subIdealModels.size() + " sub-ideal models!!!\n\n");
        
        List<E3Model> sortedSubIdealModels = ModelRanker.sortByLossandGain(baseModel, subIdealModels, mainActor, selectedNeed, startValue, endValue, false);
        //List<E3Model> sortedSubIdealModels = ModelRanker.sortByLoss(subIdealModels, mainActor, selectedNeed, startValue, endValue, false);
       

        System.out.println("RANKED LIST OF MODELS BASED ON LOSS FOR " + selectedActorString + " and on \u0394gain of the other actors in the model:\n");
        int i = 0;
        for (E3Model model : sortedSubIdealModels) {
            i++;
            System.out.println(i + ":" + model.getDescription() + "");
            //System.out.println(model.getActorsMap().size() + " actors in model");
            for(Resource actor : model.getActors()){        
            System.out.println("\t\tAverage for "+ actor.getProperty(E3value.e3_has_name).getString() + "\t = \t"+ model.getLastKnownAverages().get(actor)+ "( \u0394 "+ (model.getLastKnownAverages().get(actor) -baseModel.getLastKnownAverages().get(actor))+ " )");
            //System.out.println("\tAverage for "+ actor.getProperty(E3value.e3_has_name).getString() + " = "+ model.getAverageForActor(actor, selectedNeed, startValue, endValue, true) + "\n\n");
            }
            System.out.println("\n");   
            //FileParser.writeFile("d:/TEMP/filename.rdf", model);
        }

        
        
        FileParser.writeFile("d:/TEMP/baseModel.rdf", baseModel);

    }
}
